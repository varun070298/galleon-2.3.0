package org.lnicholls.galleon.downloads;

/*
 * FileManager.java
 *
 * Created on 14 de abril de 2003, 12:46 PM
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class FileManager {

	static HashMap hm = new HashMap();

	public FileManager() {
	}

	public RandomAccessFile getFileFor(File f) throws IOException {

		RandomAccessFile raf = (RandomAccessFile) hm.get(f.getAbsolutePath());

		if (raf == null) {
			if (!f.exists())
				f.createNewFile();

			try {
				raf = new RandomAccessFile(f, "rwd");
			} catch (FileNotFoundException fnfe) {
			}
		}

		return raf;
	}
}
