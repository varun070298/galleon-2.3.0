/*
 * Copyright (C) 2005 Leon Nicholls
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * See the file "COPYING" for more details.
 */

package org.lnicholls.galleon.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import org.apache.log4j.Logger;
import org.lnicholls.galleon.util.NameValue;
import org.lnicholls.galleon.util.Tools;

import com.tivo.hme.host.io.FastInputStream;
import com.tivo.hme.host.sample.DNSSDRequest;
import com.tivo.hme.host.sample.HostingVersion;
import com.tivo.hme.host.sample.JarClassLoader;
import com.tivo.hme.host.sample.Listener;
import com.tivo.hme.host.sample.Main;
import com.tivo.hme.host.util.ArgumentList;
import com.tivo.hme.host.util.Config;
import com.tivo.hme.host.util.Misc;
import com.tivo.hme.interfaces.IFactory;
import com.tivo.hme.interfaces.ILogger;

public class AppHost implements ILogger {
	private static Logger log = Logger.getLogger(AppHost.class.getName());

	public AppHost(ArgumentList argumentlist) throws IOException {
		this(argumentlist, true);
	}

	public AppHost(ArgumentList argumentlist, boolean flag) throws IOException {
		factories = new ArrayList();
		config = new Config();
		// config.put("listener.debug", "" + log.isDebugEnabled());
		mInterface = argumentlist.getInt("--port", 7288);
		config.put("http.ports", "" + mInterface);
		String s = "";
		mNoMDNS = argumentlist.getValue("--nomdns", null);
		if (mNoMDNS != null) {
			s = s + "," + mNoMDNS;
		} else {
			String s2 = argumentlist.getValue("--intf", null);
			if (s2 != null) {
				do {
					if (isIPAddress(s2)) {
						s = s + "," + s2;
					} else {
						NetworkInterface networkinterface = NetworkInterface.getByName(s2);
						if (networkinterface == null) {
							log
									.info("\""
											+ s2
											+ "\" is not a valid ipv4, ipv6, or network interface name. The\nnetwork interfaces on this machine are:");
							printNetworkInterfaces();
							throw new IOException("network interface not found: " + s2);
						}
						for (Enumeration enumeration = networkinterface.getInetAddresses(); enumeration
								.hasMoreElements();) {
							InetAddress inetaddress = (InetAddress) enumeration.nextElement();
							s = s + "," + inetaddress.getHostAddress();
						}

					}
					s2 = argumentlist.getValue("--intf", null);
				} while (s2 != null);
			} else {
				boolean flag1 = false;
				boolean flag2 = false;
				InetAddress ainetaddress[] = Misc.getInterfaces();
				for (int j = 0; j < ainetaddress.length; j++) {
					InetAddress inetaddress1 = ainetaddress[j];
					String s6 = inetaddress1.getHostAddress();
					if (!s6.equals("127.0.0.1"))
						if (s6.startsWith("169.254.")) {
							if (!flag2) {
								flag2 = true;
								s = s + "," + s6;
							}
						} else if (!flag1) {
							flag1 = true;
							s = s + "," + s6;
						}
				}

			}
		}
		if ("true".equals(System.getProperty("hme.loopback")))
			s = s + ",127.0.0.1";
		config.put("http.interfaces", s);
		String s3 = argumentlist.getValue("--launcher", null);
		String s4 = argumentlist.getValue("--jars", null);
		String s5 = argumentlist.getValue("--jar", null);
		/*
		 * try { if(s3 != null) loadLaunchFile(s3); else if(s4 != null)
		 * loadJarFiles(s4); else if(s5 != null) loadJarFile(new File(s5)); else
		 * if(flag) createFactory(argumentlist,
		 * ClassLoader.getSystemClassLoader()); if(flag && factories.size() ==
		 * 0) { System.out.println("Failed to instantiate any HME apps");
		 * return; } try { listener = new Listener(config, this);
		 * Listener.DEBUG_FLUSHES = true; } catch(BindException bindexception) {
		 * if(i == 7288) { config.put("http.ports", "0"); listener = new
		 * Listener(config, this); } else { throw bindexception; } } if(s1 ==
		 * null) { String as[] = listener.getInterfaces(); rv = new
		 * JmDNS[as.length]; for(int k = 0; k < as.length; k++) rv[k] = new
		 * JmDNS(InetAddress.getByName(as[k]));
		 *  } listener.setFactories(factories); IFactory ifactory; for(Iterator
		 * iterator = factories.iterator(); iterator.hasNext();
		 * register(ifactory)) ifactory = (IFactory)iterator.next();
		 *  } catch(Exception exception) { exception.printStackTrace();
		 * System.out.println("error: " + exception.getMessage()); usage(); }
		 */
	}

	public void listen(boolean activate) {
		try {
			try {
				listener = new Listener(config, this);
				Listener.DEBUG_FLUSHES = true;
			} catch (BindException bindexception) {
				if (mInterface == 7288) {
					config.put("http.ports", "0");
					listener = new Listener(config, this);
				} else {
					throw bindexception;
				}
			}
			if (mNoMDNS == null) {
				String as[] = listener.getInterfaces();
				rv = new JmDNS[as.length];
				for (int k = 0; k < as.length; k++)
					rv[k] = new JmDNS(InetAddress.getByName(as[k]));

			}
			listener.setFactories(factories);
			IFactory ifactory;
			for (Iterator iterator = factories.iterator(); iterator.hasNext();) {
				ifactory = (IFactory) iterator.next();
				if (!activate) {
					if (ifactory.getClass().getName().endsWith("MenuFactory"))
						register(ifactory);
				} else
					register(ifactory);
			}
		} catch (Exception ex) {
			Tools.logException(AppFactory.class, ex);
		}
	}

	public List getAppUrls() {
		ArrayList list = new ArrayList();
		String as[] = listener.getInterfaces();
		int ai[] = listener.getPorts();

		IFactory ifactory;
		for (Iterator iterator = factories.iterator(); iterator.hasNext();) {
			ifactory = (IFactory) iterator.next();
			String name = ifactory.getAppTitle();
			if (ifactory instanceof AppFactory) {
				AppFactory appFactory = (AppFactory) ifactory;
				name = appFactory.getTitle();
			}
			list.add(new NameValue(name, "http://" + as[0] + ":" + ai[0] + ifactory.getAppName()));
		}
		return list;
	}

	public void listen(IFactory factory) {
		factory.setActive(true);
		listener.add(factory);
		try {
			register(factory);
		} catch (Exception ex) {
			Tools.logException(AppFactory.class, ex);
		}
	}

	public void remove(IFactory factory) {
		factory.setActive(false);
		listener.remove(factory);
		try {
			unregister(factory);
		} catch (Exception ex) {
			Tools.logException(AppFactory.class, ex);
		}
	}

	private void usage() {
		System.out.println("usage: Main [options] class");
		System.out.println();
		System.out.println("Options:");
		System.out.println(" --port <port>         listen on a specific port");
		System.out.println(" --intf <interface>    listen on a specific interface");
		System.out.println(" --nomdns <interface>  listen on a specific interface, without mdns");
		System.out.println(" --launcher <file>     start factories listed in file");
		System.out.println(" --jars <dir>          scan directory for HME app jar files");
		System.out.println(" --jar <jarfile>       start factory for the given jar");
		System.exit(1);
	}

	static boolean isIPAddress(String s) {
		return isIPv4Address(s) || isIPv6Address(s);
	}

	static boolean isIPv4Address(String s) {
		int i = 0;
		for (StringTokenizer stringtokenizer = new StringTokenizer(s, "."); stringtokenizer.hasMoreTokens();) {
			try {
				Integer.parseInt(stringtokenizer.nextToken());
			} catch (NumberFormatException numberformatexception) {
				return false;
			}
			i++;
		}

		return i == 4;
	}

	static boolean isIPv6Address(String s) {
		if (s.indexOf(':') == -1)
			return false;
		for (StringTokenizer stringtokenizer = new StringTokenizer(s, ":"); stringtokenizer.hasMoreTokens();)
			try {
				Integer.parseInt(stringtokenizer.nextToken(), 16);
			} catch (NumberFormatException numberformatexception) {
				return false;
			}

		return true;
	}

	void printNetworkInterfaces() throws IOException {
		for (Enumeration enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements(); System.out
				.println()) {
			NetworkInterface networkinterface = (NetworkInterface) enumeration.nextElement();
			System.out.print("  " + networkinterface.getName());
			InetAddress inetaddress;
			for (Enumeration enumeration1 = networkinterface.getInetAddresses(); enumeration1.hasMoreElements(); System.out
					.print(" " + inetaddress.getHostAddress()))
				inetaddress = (InetAddress) enumeration1.nextElement();

		}

	}

	public static StringBuffer addArg(StringBuffer stringbuffer, String s, String s1) {
		if (s1 != null) {
			if (s != null)
				stringbuffer.append(' ').append(s);
			stringbuffer.append(' ').append(s1);
		}
		return stringbuffer;
	}

	private void loadJarFiles(String s) throws IOException {
		File file = new File(s);
		String as[] = file.list();
		if (as == null)
			return;
		for (int i = 0; i < as.length; i++)
			if (as[i].endsWith(".jar")) {
				File file1 = new File(file, as[i]);
				loadJarFile(file1);
			}

	}

	private void loadJarFile(File file) throws IOException {
		try {
			JarFile jarfile = new JarFile(file, true);
			Manifest manifest = jarfile.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			StringBuffer stringbuffer = new StringBuffer(64);
			addArg(stringbuffer, "--class", attributes.getValue("HME-Class"));
			addArg(stringbuffer, null, attributes.getValue("HME-Arguments"));
			createFactory(new ArgumentList(stringbuffer.toString()), new JarClassLoader(jarfile, this, null));
		} catch (Exception exception) {
			log(3, "Ignoring jar file: " + file);
			log(3, "Exception occurred: " + exception);
			if (Listener.DEBUG)
				exception.printStackTrace();
		}
	}

	public void loadLaunchFile(String s) throws IOException {
		loadLaunchFile(s, ClassLoader.getSystemClassLoader());
	}

	public void loadLaunchFile(String s, ClassLoader classLoader) throws IOException {
		FastInputStream fastinputstream = new FastInputStream(new FileInputStream(s), 1024);
		LineNumberReader linenumberreader = new LineNumberReader(new InputStreamReader(fastinputstream));
		try {
			for (String s1 = linenumberreader.readLine(); s1 != null; s1 = linenumberreader.readLine()) {
				s1 = s1.trim();
				if (!s1.startsWith("#") && s1.length() > 0) {
					createFactory(new ArgumentList(s1), classLoader);
				}
			}

		} finally {
			linenumberreader.close();
		}
	}

	public IFactory createFactory(ArgumentList argumentlist, ClassLoader classloader) {
		try {
			String s = argumentlist.getValue("--class", null);
			if (s == null)
				s = argumentlist.shift();
			Class class1 = Class.forName(s, true, classloader);
			Class aclass[] = { java.lang.String.class, java.lang.ClassLoader.class,
					com.tivo.hme.interfaces.IArgumentList.class };
			Method method = class1.getMethod("getAppFactory", aclass);
			Object aobj[] = { s, classloader, argumentlist };
			IFactory ifactory = (IFactory) method.invoke(null, aobj);
			argumentlist.checkForIllegalFlags();
			factories.add(ifactory);
			return ifactory;
		} catch (ClassNotFoundException ex) {
			Tools.logException(AppFactory.class, ex, "check the classpath and access permissions");
		} catch (IllegalAccessException ex) {
			Tools.logException(AppFactory.class, ex,
					"make sure the class is public and has a public default constructor");
		} catch (NoSuchMethodException ex) {
			Tools.logException(AppFactory.class, ex,
					"make sure the class is public and has a public default constructor");
		} catch (IllegalArgumentException ex) {
			Tools
					.logException(AppFactory.class, ex,
							"make sure the class is public and has a public static getAppFactory method with correct parameters");
		} catch (InvocationTargetException ex) {
			Tools.logException(AppFactory.class, ex,
					"make sure the class is public and has a public static getAppFactory method");
		}
		return null;
	}

	public void register(IFactory ifactory) throws IOException {
		String as[] = listener.getInterfaces();
		int ai[] = listener.getPorts();
		for (int i = 0; i < as.length; i++) {
			for (int j = 0; j < ai.length; j++) {
				String s = "http://" + as[i] + ":" + ai[j] + ifactory.getAppName();
				if (rv == null) {
					log.info(s + " [no mdns]");
					continue;
				}
				log.info("MDNS: " + s);
				DNSSDRequest dnssdrequest = null;
				try {
					dnssdrequest = new DNSSDRequest();
				} catch (IOException ioexception) {
				}
				if (dnssdrequest != null)
					try {
						dnssdrequest.registerService(ifactory.getAppTitle(), "_tivo-hme._tcp", s);
						ifactory.getFactoryData().put("dnssd", dnssdrequest);
						continue;
					} catch (IOException ioexception1) {
						dnssdrequest.close();
						dnssdrequest = null;
					}
				rv[j].registerService(getServiceInfo(ifactory, ai[j]));
			}
		}
	}

	public void unregister(IFactory ifactory) throws IOException {
		String as[] = listener.getInterfaces();
		int ai[] = listener.getPorts();
		for (int i = 0; i < as.length; i++) {
			for (int j = 0; j < ai.length; j++) {
				String s = "http://" + as[i] + ":" + ai[j] + ifactory.getAppName();
				if (rv == null) {
					log.info(s + " [no mdns]");
				} else {
					log.info("MDNS REMOVE: " + s);
					DNSSDRequest dnssdrequest = (DNSSDRequest) ifactory.getFactoryData().get("dnssd");
					if (dnssdrequest != null)
						dnssdrequest.close();
					else
						rv[j].unregisterService(getServiceInfo(ifactory, ai[j]));
				}
			}

		}

	}

	protected ServiceInfo getServiceInfo(IFactory ifactory, int i) {
		Hashtable hashtable = new Hashtable();
		hashtable.put("path", ifactory.getAppName());
		hashtable.put("version", (String) ifactory.getFactoryData().get("version"));
		return new ServiceInfo("_tivo-hme._tcp.local.", ifactory.getAppTitle() + "." + "_tivo-hme._tcp.local.", i, 0,
				0, hashtable);
	}

	public void log(int i, String s) {
		log.info(s);
	}

	public static void main(String args[]) throws IOException {
		(new HostingVersion()).printVersion(System.out);
		new Main(new ArgumentList(args));
	}

	static final int DEFAULT_PORT = 7288;

	public static final String DNSSD_KEY = "dnssd";

	protected Config config;

	protected Listener listener;

	protected List factories;

	protected JmDNS rv[];

	private int mInterface;

	private String mNoMDNS;
}