package org.lnicholls.galleon.util;

/*
 * Copyright (C) 2005  Leon Nicholls
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See the file "COPYING" for more details.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.imageio.ImageIO;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.lob.BlobImpl;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.lnicholls.galleon.database.Thumbnail;
import org.lnicholls.galleon.database.ThumbnailManager;
import org.lnicholls.galleon.media.ImageManipulator;
import org.lnicholls.galleon.server.Constants;

import EDU.oswego.cs.dl.util.concurrent.Callable;
import EDU.oswego.cs.dl.util.concurrent.TimedCallable;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.tivo.hme.sdk.View;

/**
 * @author sthompso
 */
public class Tools {

	private static Logger log = Logger.getLogger(Tools.class.getName());

	private static Runtime runtime = Runtime.getRuntime();

	private static BufferedImage buffer = null;

	static {
		try {
			buffer = createBufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		} catch (Throwable ex) {
			System.out.println(ex.getMessage());
			System.out.println(ex.toString());
			ex.printStackTrace();
			Throwable cause = ex.getCause();
			while (cause != null) {
				System.out.println(cause.getMessage());
				System.out.println(cause.toString());
				cause.printStackTrace();
				cause = cause.getCause();
			}
		}
	}

	// TODO Is the good enough??
	private static Cipher EncryptionCipher = null;

	private static Cipher DecryptionCipher = null;

	static {
		try {
			byte[] seed = { (byte) 0xa1, (byte) 0x22, (byte) 0x78, (byte) 0x8f,
					(byte) 0x5c, (byte) 0x66, (byte) 0xdd, (byte) 0xa4 };
			PBEParameterSpec paramSpec = new PBEParameterSpec(seed, 5);
			SecretKey secretKey = SecretKeyFactory.getInstance(
					"PBEWithMD5AndDES").generateSecret(
					new PBEKeySpec("31oqcadaj4y5qs2vdo70dsp".toCharArray()));
			EncryptionCipher = Cipher.getInstance("PBEWithMD5AndDES");
			EncryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
			DecryptionCipher = Cipher.getInstance("PBEWithMD5AndDES");
			DecryptionCipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
		} catch (Exception ex) {
			logException(Tools.class, ex, "Could not initialize encryption");
		}
	}

	public static void logException(Class inClass, Throwable inException) {

		logException(inClass, inException, null);
	}

	public static void logException(Class inClass, Throwable inException,
			String message) {
		try {
			Logger log = Logger.getLogger(inClass.getName());

			if (message != null)
				log.error(inException.toString() + ": " + message);
			else
				log.error(inException.toString());

			if (log.isDebugEnabled()) {
				StringWriter writer = new StringWriter();
				inException.printStackTrace(new PrintWriter(writer));
				log.debug(writer.toString());
			}
		} catch (Throwable ex) {
		}
	}

	public static void addAll(Collection target, Object[] source) {
		for (int i = 0; i < source.length; i++)
			target.add(source[i]);
	}

	public static void redirectStandardStreams() {
		// Determine the path to the log dir
		String logDir = "";
		if (System.getProperty("logfile") == null) {
			File file = new File(".");
			logDir = file.getAbsolutePath() + "/../logs";
		} else {
			File logFile = new File(System.getProperty("logfile"));
			File dir = new File(logFile.getPath().substring(0,
					logFile.getPath().lastIndexOf(File.separator)));
			logDir = dir.getAbsolutePath();
		}

		// Create a new output stream for the standard output.
		PrintStream stdout = null;
		try {
			stdout = new PrintStream(new FileOutputStream(logDir
					+ "/Redirect.out"));
			System.setOut(stdout);
		} catch (Exception e) {
			log.error(e.toString());
		}

		// Create new output stream for the standard error output.
		PrintStream stderr = null;
		try {
			stderr = new PrintStream(new FileOutputStream(logDir
					+ "/Redirect.err"));
			System.setErr(stderr);
		} catch (Exception e) {
			log.error(e.toString());
		}
	}

	public static String dateToHex(Date date) {
		if (date != null)
			return Long.toHexString(date.getTime() / 1000).toUpperCase();
		else
			return "";
	}

	public static String encode(String url) {
		return URLEncoder.encode(url);
	}

	public static String hexToDate(String hex) {
		Date date = new Date();
		try {
			Long time = Long.decode(hex);
			date = new Date(time.longValue() * 1000);
		} catch (NumberFormatException ex) {
		}
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("EEE MMM d yyyy, hh:mm:ss a");
		return sdf.format(date);
	}

	public static Date hexDate(String hex) {
		try {
			Long time = Long.decode(hex);
			Date date = new Date(time.longValue() * 1000);
			return date;
		} catch (NumberFormatException ex) {
		}
		return null;
	}

	public static String millisecondsToTime(String milli) {
		try {
			int value = Integer.parseInt(milli) / 1000;
			int minutes = value / 60;
			int seconds = value % 60;
			return "" + minutes + ":" + seconds;
		} catch (NumberFormatException ex) {
		}
		return milli;
	}

	public static int parseInteger(String value, int defaultValue) {
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException ex) {
			}
		}

		return defaultValue;
	}

	public static double parseDouble(String value, double defaultValue) {
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException ex) {
			}
		}

		return defaultValue;
	}

	/*
	 * public static final String escapeXMLChars(String cdata) { return
	 * StringEscapeUtils.escapeXml(cdata); }
	 */

	public static final String unEscapeXMLChars(String cdata) {
		return StringEscapeUtils.unescapeXml(cdata);
	}

	public static final String escapeXMLChars(String cdata) {
		// Test for each character before calling replaceAll() to keep this
		// fast, as usually the escaped characters are not present

		if (cdata == null)
			return "";
		if (cdata.indexOf('&') != -1) {
			cdata = cdata.replaceAll("&", "&amp;"); // &#38;#38;
		}
		if (cdata.indexOf('<') != -1) {
			cdata = cdata.replaceAll("<", "&lt;"); // &#38;#60;
		}
		if (cdata.indexOf('>') != -1) {
			cdata = cdata.replaceAll(">", "&gt;"); // &#62;
		}
		if (cdata.indexOf('\'') != -1) {
			cdata = cdata.replaceAll("'", "&apos;"); // &#39;
		}
		if (cdata.indexOf('"') != -1) {
			cdata = cdata.replaceAll("\"", "&quot;"); // &#34;
		}
		return cdata;
	}

	public static String extractName(String path) {
		String fileName = path.replace(File.separatorChar, '/');
		int lastSeparator = fileName.lastIndexOf(File.separatorChar);
		if (lastSeparator != -1) {
			fileName = fileName.substring(lastSeparator + 1);
		} else {
			lastSeparator = fileName.lastIndexOf("/");
			fileName = fileName.substring(lastSeparator + 1);
		}
		int lastPeriod = fileName.lastIndexOf('.');
		if (lastPeriod != -1) {
			int suffixLength = fileName.length() - lastPeriod;
			if ((suffixLength == ".xxx".length())
					|| (suffixLength == ".xxxx".length())) {
				fileName = fileName.substring(0, lastPeriod);
			}
		}
		return fileName;
	}

	public static String clean(String value) {
		StringBuffer buffer = new StringBuffer(value.length());
		synchronized (buffer) {
			for (int i = 0; i < value.length(); i++) {
				if (value.charAt(i) == '’')
					buffer.append("'");
				else if (value.charAt(i) >= '0' && value.charAt(i) <= '9')
					buffer.append(value.charAt(i));
				else if (!Character.isISOControl(value.charAt(i)))
					buffer.append(value.charAt(i));
			}
		}
		return buffer.toString();
	}

	public static boolean isPrefixUrl(String url1, String url2) {
		boolean result = true;
		if (url1.equals(url2))
			result = true;
		else if (!url2.startsWith(url1))
			result = false;
		else {
			StringTokenizer token1 = new StringTokenizer(url1, "/");
			StringTokenizer token2 = new StringTokenizer(url2, "/");
			while (token1.hasMoreTokens() && token2.hasMoreTokens()) {
				if (!token1.nextToken().equals(token2.nextToken())) {
					result = false;
					break;
				}
			}
		}

		return result;
	}

	public static String encrypt(String value) {
		if (EncryptionCipher != null) {
			try {
				return bytesToString(EncryptionCipher.doFinal(value.getBytes()));
			} catch (BadPaddingException e) {
				logException(Tools.class, e);
			} catch (IllegalBlockSizeException e) {
				logException(Tools.class, e);
			}
		}
		return null;
	}

	public static String decrypt(String value) {
		if (DecryptionCipher != null) {
			try {
				return new String(DecryptionCipher
						.doFinal(stringToBytes(value)));
			} catch (BadPaddingException e) {
				logException(Tools.class, e);
			} catch (IllegalBlockSizeException e) {
				logException(Tools.class, e);
			}
		}
		return null;
	}

	public static String bytesToString(byte[] bytes) {
		String result = "";
		for (int i = 0; i < bytes.length; i++) {
			result = result + byteToHex(bytes[i]);
		}
		return result;
	}

	public static byte[] stringToBytes(String value) {
		if (value != null) {
			int length = value.length() / 2;
			byte[] bytes = new byte[length];
			for (int i = 0; i < length; i++) {
				bytes[i] = Integer.valueOf(value.substring(2 * i, 2 * i + 2),
						16).byteValue();
			}
			return bytes;
		}
		return new String().getBytes();
	}

	static char hex[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F' };

	static public String byteToHex(byte b) {
		char[] bytes = { hex[(b >> 4) & 0x0f], hex[b & 0x0f] };
		return new String(bytes);
	}

	public static Toolkit getDefaultToolkit() {
		// return Toolkit.getDefaultToolkit();
		try {
			String headless = System.getProperty("java.awt.headless");
			if (headless == null || !headless.equals("true"))
				try {
					if (SystemUtils.IS_OS_WINDOWS)
						return (Toolkit) Class.forName(
								"sun.awt.windows.WToolkit").newInstance();
					else if (SystemUtils.IS_OS_LINUX)
						return (Toolkit) Class
								.forName("sun.awt.motif.MToolkit")
								.newInstance();
					else if (SystemUtils.IS_OS_MAC_OSX)
						return (Toolkit) Class.forName("apple.awt.CToolkit")
								.newInstance();
				} catch (Throwable ex) {
				}
			return Toolkit.getDefaultToolkit();
		} catch (Throwable ex) {
			try {
				return (Toolkit) Class.forName("com.eteks.awt.PJAToolkit")
						.newInstance();
			} catch (Exception ex2) {

			}
		}
		return null;
	}

	public static Image getResourceAsImage(Class theClass, String resource) {
		try {
			if (!resource.startsWith("/"))
				resource = "/" + resource;
			InputStream is = theClass.getResourceAsStream(resource);
			BufferedInputStream bis = new BufferedInputStream(is);
			if (is != null) {
				byte[] byBuf = new byte[is.available()];
				int byteRead = bis.read(byBuf, 0, is.available());
				Image img = Toolkit.getDefaultToolkit().createImage(byBuf);
				if (img != null) {
					img.getWidth(null);
					is.close();
					return img;
				}
				is.close();
			}
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex, "Could not load resource: "
					+ resource);
		}
		return null;
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration interfaceEnum = NetworkInterface
					.getNetworkInterfaces(); interfaceEnum.hasMoreElements();) {
				NetworkInterface ni = (NetworkInterface) interfaceEnum
						.nextElement();
				Enumeration inetAddresses = ni.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = (InetAddress) inetAddresses
							.nextElement();
					if (inetAddress.getHostAddress().startsWith("192"))
						return inetAddress.getHostAddress();
				}
			}
			return InetAddress.getLocalHost().getHostAddress();
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex);
		}
		return "127.0.0.1";
	}

	public static boolean isLocalAddress(String address) {
		if (address != null) {
			try {
				for (Enumeration interfaceEnum = NetworkInterface
						.getNetworkInterfaces(); interfaceEnum
						.hasMoreElements();) {
					NetworkInterface ni = (NetworkInterface) interfaceEnum
							.nextElement();
					Enumeration inetAddresses = ni.getInetAddresses();
					while (inetAddresses.hasMoreElements()) {
						InetAddress inetAddress = (InetAddress) inetAddresses
								.nextElement();
						if (inetAddress.getHostAddress().equals(address))
							return true;
					}
				}
			} catch (Exception ex) {
				Tools.logException(Tools.class, ex);
			}
		}
		return false;
	}

	public static boolean isLocal(String address) {
		if (address != null) {
			try {
				if (address.equals("127.0.0.1"))
					return true;
				String prefix = null;
				StringTokenizer tokenizer = new StringTokenizer(address, ".");
				if (tokenizer.hasMoreTokens())
					prefix = tokenizer.nextToken();
				if (prefix != null) {
					for (Enumeration interfaceEnum = NetworkInterface
							.getNetworkInterfaces(); interfaceEnum
							.hasMoreElements();) {
						NetworkInterface ni = (NetworkInterface) interfaceEnum
								.nextElement();
						Enumeration inetAddresses = ni.getInetAddresses();
						while (inetAddresses.hasMoreElements()) {
							InetAddress inetAddress = (InetAddress) inetAddresses
									.nextElement();
							if (inetAddress.getHostAddress().startsWith(prefix))
								return true;
						}
					}
				}
			} catch (Exception ex) {
				Tools.logException(Tools.class, ex);
			}
		}
		return false;
	}

	public static String getVersion() {
		// TODO Handle development version
		String version = Constants.CURRENT_VERSION.getMajor() + "."
				+ Constants.CURRENT_VERSION.getRelease() + "."
				+ Constants.CURRENT_VERSION.getMaintenance();
		if (Constants.CURRENT_VERSION.getDevelopment() != 0)
			return version + " RC "
					+ Constants.CURRENT_VERSION.getDevelopment();
		else
			return version;
	}

	public static void logMemory() {
		logMemory(null);
	}

	public static void logMemory(String message) {
		if (message != null)
			log.info(message);
		log.info("Max Memory: " + runtime.maxMemory());
		log.info("Total Memory: " + runtime.totalMemory());
		log.info("Free Memory: " + runtime.freeMemory());
	}

	// If filename ends with .xxx or .xxxx, remove the suffix,
	// otherwise return the original value.
	public static String trimSuffix(String filename) {
		// Remove trailing suffix, e.g. .mp3 or .jpeg. Don't remove other length
		// suffixes as
		// they may be legitimate uses of period in a short-cut name.
		int lastPeriod = filename.lastIndexOf('.');
		if (lastPeriod != -1) {
			int suffixLength = filename.length() - lastPeriod;
			if ((suffixLength == ".xxx".length())
					|| (suffixLength == ".xxxx".length())) {
				filename = filename.substring(0, lastPeriod);
			}
		}
		return filename;
	}

	public static String getAttribute(Element element, String name) {
		String value = element.attributeValue(name);
		if (value == null) {
			Element child = element.element(name);
			if (child != null)
				return child.getTextTrim();
		} else
			value = value.trim();
		return value;
	}

	public static String getPage(URL url) {
		StringBuffer buffer = new StringBuffer();
		byte[] buf = new byte[1024];
		int amount = 0;
		try {
			InputStream input = url.openStream();
			while ((amount = input.read(buf)) > 0) {
				buffer.append(new String(buf, 0, amount));
			}
			input.close();
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex, url.getPath());
			return null;
		}
		return buffer.toString();
	}

	public static String[] layout(int width, FontMetrics metrics, String text) {
		ArrayList lines = new ArrayList();

		if (text != null) {
			String line = "";
			BreakIterator boundary = BreakIterator.getWordInstance();
			boundary.setText(text);
			int start = boundary.first();
			for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary
					.next()) {
				String word = text.substring(start, end);
				String trimmed = word.replaceAll(" ", "");
				int metricsWidth = (line + word).length() * 20;
				if (metrics != null)
					metricsWidth = metrics.stringWidth(line + word);

				if (trimmed.equals("\n") || trimmed.equals("\r")
						|| trimmed.equals("\r\n")) {
					lines.add(line.trim());
					line = "";
				} else if (metricsWidth > width) {
					lines.add(line.trim());
					line = word;
				} else
					line = line + word;
			}
			if (line.trim().length() > 0)
				lines.add(line.trim());
		}

		return (String[]) lines.toArray(new String[0]);
	}

	public static FontMetrics getFontMetrics(Font font) {
		Graphics2D graphics2D = (Graphics2D) buffer.getGraphics();
		try {
			FontMetrics fontMetrics = graphics2D.getFontMetrics(font);
			return fontMetrics;
		} finally {
			graphics2D.dispose();
		}
	}

	public static synchronized Image getImage(Image image) {
		return new ImageTracker(image).load();
	}

	private static final class ImageTracker implements ImageObserver {

		public ImageTracker(URL url) {
			this(Tools.getDefaultToolkit().getImage(url));
		}

		public ImageTracker(String filename) {
			this(Tools.getDefaultToolkit().getImage(filename));
		}

		public ImageTracker(Image image) {
			mImage = image;
		}

		public synchronized Image load() {
			try {
				if (!Tools.getDefaultToolkit().prepareImage(mImage, -1, -1,
						this)) {
					while (true) {
						wait(0);
						if (mLoaded) {
							if (log.isDebugEnabled())
								log.debug("Image Loaded");
							break;
						} else if (mError) {
							log.error("Image Error");
							return null;
						}
					}
					if (log.isDebugEnabled())
						log.debug("done waiting");
				}
				int width = mImage.getWidth(this);
				int height = mImage.getHeight(this);

				return mImage;
			} catch (Exception ex) {
				Tools.logException(Tools.class, ex);
				return null;
			}
		}

		public synchronized boolean imageUpdate(Image img, int infoflags,
				int x, int y, int w, int h) {
			if ((infoflags & ERROR) != 0) {
				mError = true;
				notifyAll();
				return false;
			} else if ((infoflags & ABORT) != 0) {
				mError = true;
				notifyAll();
				return false;
			} else if ((infoflags & (ALLBITS | FRAMEBITS)) != 0) {
				mLoaded = true;
				notifyAll();
				return false;
			}
			return true;
		}

		private Image mImage;

		private boolean mLoaded = false;

		private boolean mError = false;
	}

	public static void cacheImage(URL url) {
		cacheImage(url, null);
	}

	public static void cacheImage(URL url, String key) {
		cacheImage(url, -1, -1, key);
	}

	public static void cacheImage(URL url, int width, int height) {
		cacheImage(url, width, height, null);
	}

	public static void cacheImage(URL url, int width, int height, String key) {
		if (url != null) {
			Image image = getImage(url, width, height);
			if (key == null)
				key = url.toExternalForm();
			cacheImage(image, width, height, key);
		}
	}

	public static void cacheImage(Image image, int width, int height, String key) {
		if (image != null) {
			try {
				// TODO What if not bufferedimage??
				if (image instanceof BufferedImage) {
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					JPEGImageEncoder encoder = JPEGCodec
							.createJPEGEncoder(byteArrayOutputStream);
					encoder.encode((BufferedImage) image);
					byteArrayOutputStream.close();

					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
							byteArrayOutputStream.toByteArray());
					if (byteArrayOutputStream.size() < 102400) {
						BlobImpl blob = new BlobImpl(byteArrayInputStream,
								byteArrayOutputStream.size());

						Thumbnail thumbnail = null;
						try {
							List list = ThumbnailManager.findByKey(key);
							if (list != null && list.size() > 0)
								thumbnail = (Thumbnail) list.get(0);
						} catch (HibernateException ex) {
							log.error("Thumbnail create failed", ex);
						}

						try {
							if (thumbnail == null) {
								thumbnail = new Thumbnail("Cached", "jpg", key);
								thumbnail.setImage(blob);
								thumbnail.setDateModified(new Date());
								ThumbnailManager.createThumbnail(thumbnail);
							} else {
								thumbnail.setImage(blob);
								thumbnail.setDateModified(new Date());
								ThumbnailManager.updateThumbnail(thumbnail);
							}
						} catch (HibernateException ex) {
							log.error("Thumbnail create failed", ex);
						}
					}
				}
				image.flush();
				image = null;
			} catch (Exception ex) {
				Tools.logException(Tools.class, ex, key);
			}
		}
	}

	public static Image retrieveCachedImage(URL url) {
		return retrieveCachedImage(url.toExternalForm());
	}

	public static BufferedImage retrieveCachedImage(String key) {
		try {
			return ThumbnailManager.findImageByKey(key);
		} catch (HibernateException ex) {
			log.error("Image retrieve failed", ex);
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex, key);
		}
		return null;
	}

	public static void deleteCachedImage(String key) {
		try {
			List list = ThumbnailManager.findByKey(key);
			if (list != null && list.size() > 0) {
				Thumbnail thumbnail = (Thumbnail) list.get(0);
				ThumbnailManager.deleteThumbnail(thumbnail);
			}
		} catch (HibernateException ex) {
			log.error("Image delete failed", ex);
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex, key);
		}
	}

	public static Image getImage(URL url, int width, int height) {
		if (url != null) {
			// System.out.println(url);
			try {
				Image internetImage = null;
				if (log.isDebugEnabled())
					log.debug("Downloading internet image="
							+ url.toExternalForm());

				class TimedThread implements Callable {
					private URL mUrl;

					public TimedThread(URL url) {
						mUrl = url;
					}

					public synchronized java.lang.Object call()
							throws java.lang.Exception {
						return new ImageTracker(mUrl).load();
					}
				}

				TimedCallable timedCallable = new TimedCallable(
						new TimedThread(url), 2000 * 60);
				internetImage = (Image) timedCallable.call();

				// System.out.println("internetImage="+internetImage);
				if (internetImage == null) {
					log.error("Invalid internet image: " + url.getPath());
				} else {
					// System.out.println("width="+width);
					// System.out.println("height="+height);
					if (width == -1)
						width = internetImage.getWidth(null);
					if (height == -1)
						height = internetImage.getHeight(null);

					// System.out.println("width="+width);
					// System.out.println("height="+height);

					Image image = null;
					if (internetImage instanceof BufferedImage) {
						image = ImageManipulator.getScaledImage(
								(BufferedImage) internetImage, width, height);
						// System.out.println("image1="+image);
					} else {
						try {
							image = createBufferedImage(width, height,
									BufferedImage.TYPE_INT_RGB);
							Graphics2D graphics2D = ((BufferedImage) image)
									.createGraphics();
							graphics2D
									.setRenderingHint(
											RenderingHints.KEY_INTERPOLATION,
											RenderingHints.VALUE_INTERPOLATION_BILINEAR);
							graphics2D.drawImage(internetImage, 0, 0, width,
									height, null);
							graphics2D.dispose();
							graphics2D = null;
							// System.out.println("image2="+image);
						} catch (Throwable ex) {
							// ex.printStackTrace();
							image = internetImage.getScaledInstance(width,
									height, Image.SCALE_SMOOTH);
							// System.out.println("image3="+image);
						}
					}
					internetImage.flush();
					internetImage = null;

					return image;
				}
			} catch (Throwable ex) {
				// ex.printStackTrace();
				Tools.logException(Tools.class, ex, url.toExternalForm());
			}
		}
		return null;
	}

	public static String getPackage(String className) {
		String pkg = className;
		int last = pkg.lastIndexOf('.');
		if (last == -1) {
			return "";
		}
		return pkg.substring(0, last).replace('.', '/');
	}

	public static String trim(String value, int max) {
		if (value != null && value.length() > max)
			return clean(value).substring(0, max - 3) + "...";
		return value;
	}

	public static BufferedImage createBufferedImage(int width, int height,
			int imageType) {
		// return new BufferedImage(width, height, imageType);
		if (java.awt.GraphicsEnvironment.isHeadless())
			return new com.eteks.java2d.PJABufferedImage(width, height,
					imageType);
		else
			try {
				return new BufferedImage(width, height, imageType);
			} catch (Throwable ex) {
				return new com.eteks.java2d.PJABufferedImage(width, height,
						imageType);
			}
	}

	public static BufferedImage getDefaultImage() {
		return createBufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
	}

	public static Image createImage(java.awt.image.ImageProducer producer)
			throws Exception {
		try {
			return Toolkit.getDefaultToolkit().createImage(producer);
		} catch (Throwable th) {
			// return new com.eteks.awt.PJAImage (producer);
			return null;
		}
	}

	public static String md5(String value) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(value.getBytes());

			return bytesToString(messageDigest.digest());
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex, value);
		}
		return value;
	}

	public static int findAvailablePort(int port) {
		if (log.isDebugEnabled())
			log.debug("findAvailablePort: " + port);
		for (int i = 0; i < 100; i++) {
			try {
				if (log.isDebugEnabled())
					log.debug("Trying port " + port);
				ServerSocket serverSocket = new ServerSocket(port);
				serverSocket.close();
				serverSocket = null;
				break;
			} catch (Exception ex) {
				if (log.isDebugEnabled())
					log.debug("Port " + port + " is already in use.");
				port = port + 1;
			}
		}
		return port;
	}

	public static String cleanHTML(String data) {
		String result = "";
		if (data != null) {
			data = data.replaceAll("\n", " ").replaceAll("&nbsp;", " ");
			int pos1 = data.indexOf("<");
			if (pos1 != -1) {
				while (pos1 != -1) {
					int pos2 = data.indexOf(">");
					if (pos2 == -1) {
						result = result + data;
						break;
					}
					result = result + data.substring(0, pos1);
					data = data.substring(pos2 + 1);
					pos1 = data.indexOf("<");
				}
			} else
				result = data;
		}
		return StringEscapeUtils.unescapeXml(StringEscapeUtils.unescapeHtml(
				result).trim());
	}

	public static void copy(File src, File dst) {
		try {
			FileChannel srcChannel = new FileInputStream(src).getChannel();
			FileChannel dstChannel = new FileOutputStream(dst).getChannel();
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
			srcChannel.close();
			srcChannel = null;
			dstChannel.close();
			dstChannel = null;
		} catch (IOException ex) {
			Tools.logException(Tools.class, ex, dst.getAbsolutePath());
		}
	}

	public static InputStream getInputStream(File file) {
		try {
			FileChannel roChannel = new RandomAccessFile(file, "r")
					.getChannel();
			final ByteBuffer buf = roChannel.map(FileChannel.MapMode.READ_ONLY,
					0, (int) roChannel.size());
			return new InputStream() {
				public synchronized int read() throws IOException {
					if (!buf.hasRemaining()) {
						return -1;
					}
					return buf.get();
				}

				public synchronized int read(byte[] bytes, int off, int len)
						throws IOException {
					if (!buf.hasRemaining()) {
						return -1;
					}
					len = Math.min(len, buf.remaining());
					buf.get(bytes, off, len);
					return len;
				}
			};
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex, file.getAbsolutePath());
		}
		return null;
	}

	public static BufferedImage ImageIORead(File file) {
		System.gc();
		try {
			FileChannel roChannel = new RandomAccessFile(file, "r")
					.getChannel();
			final ByteBuffer buf = roChannel.map(FileChannel.MapMode.READ_ONLY,
					0, (int) roChannel.size());
			return ImageIO.read(new InputStream() {
				public synchronized int read() throws IOException {
					if (!buf.hasRemaining()) {
						return -1;
					}
					return buf.get();
				}

				public synchronized int read(byte[] bytes, int off, int len)
						throws IOException {
					if (!buf.hasRemaining()) {
						return -1;
					}
					len = Math.min(len, buf.remaining());
					buf.get(bytes, off, len);
					return len;
				}
			});
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex, file.getAbsolutePath());
		}

		try {
			return ImageIO.read(new FileInputStream(file));
		} catch (Exception ex) {
			Tools.logException(Tools.class, ex, file.getAbsolutePath());
		}
		return null;
	}

	public static String getCause(Throwable ex) {
		String message = ex.getMessage();
		Throwable cause = ex.getCause();
		while (cause != null) {
			message = cause.getMessage();
			if (!(cause instanceof GalleonException))
				cause = cause.getCause();
			else
				break;
		}
		return message;
	}

	public static Color darken(Color color) {
		return new Color((int) (color.getRed() * 0.59),
				(int) (color.getGreen() * 0.59), (int) (color.getBlue() * 0.59));
	}

	public static String getFile(File file) {
		try {
			FileInputStream input = new FileInputStream(file);
			if (input != null) {
				StringBuffer buffer = new StringBuffer();
				byte abyte0[] = new byte[1024];
				int i = 0;
				while ((i = input.read(abyte0, 0, abyte0.length)) > 0) {
					buffer.append(new String(abyte0, 0, i));
				}

				return buffer.toString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void clearResource(View view) {
		if (view != null && view.getResource() != null) {
			if (view.getApp().getContext()!=null)
			{
				view.getResource().flush();
				view.getResource().remove();
				System.gc();
			}
		}
	}
}