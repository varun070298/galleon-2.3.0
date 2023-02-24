/*
 * Created on Dec 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.lnicholls.galleon.util;

/**
 * @author Owner
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code
 * Templates
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.FileLock;

import java.util.*;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class FileList extends AbstractList {
    private static Logger log = Logger.getLogger(FileList.class.getName());

    public static final String INDEX_FILE_SUFFIX = ".idx";

    public static final String DATA_FILE_SUFFIX = ".db";

    private static final String VERSION = "org.lnicholls.galleon.util.FileList.1.0";

    public static String filename = "d:/galleon/testing" + DATA_FILE_SUFFIX;

    public static String indexname = "d:/galleon/testing" + INDEX_FILE_SUFFIX;

    public FileList() {
        File file = new File(filename);
        if (file.exists())
            file.delete();
        file = new File(indexname);
        if (file.exists())
            file.delete();
    }

    public Object get(int pos) {
        if (pos > size())
            return null;
        else {
            try {
                /*
                 * RandomAccessFile file = new RandomAccessFile (new File(filename), "r"); IndexEntry entry =
                 * (IndexEntry)mIndex.get(index); file.seek(entry.mFilePosition); byte byteBuf[] = new
                 * byte[entry.mObjectSize]; int sizeRead = file.read (byteBuf); ObjectInputStream objStream = new
                 * ObjectInputStream (new ByteArrayInputStream (byteBuf));
                 * 
                 * Object object = objStream.readObject();
                 * 
                 * file.close();
                 */

                //IndexEntry entry = (IndexEntry)mIndex.get(index);
                IntBuffer index = getIndex();
                if (index != null) {
                    int filePosition = index.get(pos * 2);
                    //System.out.println("filePosition="+filePosition);
                    int objectSize = index.get(pos * 2 + 1);
                    //System.out.println("objectSize="+objectSize);
                    FileChannel roChannel = new RandomAccessFile(new File(filename), "rw").getChannel();
                    ByteBuffer buf = roChannel.map(FileChannel.MapMode.PRIVATE, filePosition, objectSize);
                    //buf.clear();
                    byte[] byteBuf = new byte[objectSize];
                    buf.get(byteBuf, 0, byteBuf.length);

                    ObjectInputStream objStream = new ObjectInputStream(new ByteArrayInputStream(byteBuf));

                    Object object = objStream.readObject();

                    roChannel.close();

                    return object;
                }
            } catch (Exception ex) {
                Tools.logException(FileList.class, ex);
                ex.printStackTrace();
            }
        }
        return null;
    }

    public int size() {
        return getIndex().capacity() / (4 * 2);
    }

    public Object set(int pos, Object element) {
        /*
         * try { ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); ObjectOutputStream objStream = new
         * ObjectOutputStream (byteStream); objStream.writeObject (element); int size = byteStream.size();
         * 
         * RandomAccessFile file = new RandomAccessFile (new File("filename), "rw");
         * 
         * int filePos = (int)file.length(); file.seek (filePos); file.write (byteStream.toByteArray());
         * 
         * file.close();
         * 
         * mIndex.add(new IndexEntry(filePos,size));
         *  } catch (FileNotFoundException e) { Tools.logException(FileList.class,e); e.printStackTrace(); } catch
         * (IOException e) { Tools.logException(FileList.class,e); e.printStackTrace(); }
         */
        try {
            // Obtain a channel
            File file = new File(filename);
            int filePos = 0;
            if (file.exists())
                filePos = (int) file.length();
            WritableByteChannel channel = new FileOutputStream(file, true).getChannel();

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(element);
            int size = byteStream.size();

            ByteBuffer buf = ByteBuffer.allocateDirect(size);

            buf.put(byteStream.toByteArray());

            buf.flip();

            int numWritten = channel.write(buf);

            // Close the file
            channel.close();
            buf = null;

            //mIndex.add(new IndexEntry(filePos,size));

            file = new File(indexname);
            channel = new FileOutputStream(file, true).getChannel();

            buf = ByteBuffer.allocateDirect(4 * 2);

            buf.putInt(filePos);
            buf.putInt(size);

            buf.flip();

            numWritten = channel.write(buf);

            // Close the file
            channel.close();
            mIndex = null;

        } catch (Exception e) {
        }

        // TODO Previous element
        return element;
    }

    public void add(int pos, Object element) {
        IntBuffer index = getIndex();
        //int filePosition = index.get(pos * 2);
        //int objectSize = index.get(pos * 2 + 1);

        // Remove current element from db and append new element to end

        set(pos, element);
    }

    public Object remove(int pos) {
        try {
            IntBuffer index = getIndex();
            if (index != null) {
                int filePosition = index.get(pos * 2);
                //System.out.println("filePosition="+filePosition);
                int objectSize = index.get(pos * 2 + 1);
                //System.out.println("objectSize="+objectSize);
                FileChannel roChannel = new RandomAccessFile(new File(filename), "rw").getChannel();
                ByteBuffer buf = roChannel.map(FileChannel.MapMode.PRIVATE, filePosition, objectSize);
                buf.clear();
                byte[] byteBuf = new byte[objectSize];
                buf.get(byteBuf, 0, byteBuf.length);

                ObjectInputStream objStream = new ObjectInputStream(new ByteArrayInputStream(byteBuf));

                Object object = objStream.readObject();

                buf = null;
                
                ByteBuffer before = roChannel.map(FileChannel.MapMode.PRIVATE, 0, filePosition-1);
                ByteBuffer after = null;
                if ((filePosition+objectSize)<(int) roChannel.size())
                    after = roChannel.map(FileChannel.MapMode.PRIVATE, filePosition+objectSize, (int) roChannel.size()-(filePosition+objectSize));
                //System.out.println("roChannel.size()="+roChannel.size());
                //System.out.println("before="+before.capacity());
                //System.out.println("after="+after.capacity());
                
                roChannel.close();
                
                ByteBuffer beforeCopy = ByteBuffer.allocateDirect(before.capacity());
                beforeCopy.put(before);
                beforeCopy.flip();
                before.clear();
                before = null;
                ByteBuffer afterCopy = null;
                if (after!=null)
                {
                    afterCopy = ByteBuffer.allocateDirect(after.capacity());
                    afterCopy.put(after);
                    afterCopy.flip();
                    after.clear();
                }
                after = null;                

                // Remove element from db
                FileChannel rwChannel = new RandomAccessFile(new File(filename), "rw").getChannel();
                int numWritten = rwChannel.write(beforeCopy);
                //System.out.println("numWritten1="+numWritten);
                if (afterCopy!=null)
                    numWritten = rwChannel.write(afterCopy);
                //System.out.println("numWritten2="+numWritten);
                rwChannel.force(false);
                rwChannel.close();
                before = null;
                after = null;
                /*
                File file = new File(filename);
                boolean result = file.delete();
                System.out.println("delete:"+result);
                File bak = new File(filename+".tmp");
                bak.renameTo(file);
                //bak.delete();
                 */
                
                // Remove element from index
                roChannel = new RandomAccessFile(new File(indexname), "rw").getChannel();
                before = roChannel.map(FileChannel.MapMode.PRIVATE, 0, pos*2-1);
                if ((pos*2+4*2)<(int) roChannel.size())
                    after = roChannel.map(FileChannel.MapMode.PRIVATE, pos*2+4*2, (int) roChannel.size()-(pos*2+4*2));
                roChannel.close();
                buf = null;
                
                beforeCopy = ByteBuffer.allocateDirect(before.capacity());
                beforeCopy.put(before);
                beforeCopy.flip();
                before.clear();
                before = null;
                afterCopy = null;
                if (after!=null)
                {
                    afterCopy = ByteBuffer.allocateDirect(after.capacity());
                    afterCopy.put(after);
                    afterCopy.flip();
                    after.clear();
                }
                after = null;
                
                rwChannel = new RandomAccessFile(new File(indexname), "rw").getChannel();
                //rwChannel.truncate(0);
                numWritten = rwChannel.write(beforeCopy);
                //System.out.println("numWritten3="+numWritten);
                if (afterCopy!=null)
                    numWritten = rwChannel.write(afterCopy);
                //System.out.println("numWritten4="+numWritten);
                rwChannel.force(false);
                rwChannel.close();
                before = null;
                after = null;
                /*
                file = new File(indexname);
                result = file.delete();
                System.out.println("delete:"+result);
                bak = new File(indexname+".tmp");
                bak.renameTo(file);
                //bak.delete();
                 */
                
                mIndex = null;

                return object;
            }
        } catch (Exception ex) {
            Tools.logException(FileList.class, ex);
            ex.printStackTrace();
        }

        return null;
    }

    private IntBuffer getIndex() {
        if (mIndex == null) {
            File file = new File(indexname);
            if (file.exists()) {
                try {
                    FileChannel roChannel = new RandomAccessFile(file, "rw").getChannel();
                    ByteBuffer buf = roChannel.map(FileChannel.MapMode.PRIVATE, 0, (int) roChannel.size());
                    buf.clear();
                    mIndex = buf.asIntBuffer();
                } catch (FileNotFoundException ex) {
                    Tools.logException(FileList.class, ex);
                    ex.printStackTrace();
                } catch (IOException ex) {
                    Tools.logException(FileList.class, ex);
                    ex.printStackTrace();
                }
            }
        }
        System.out.println(mIndex);
        return mIndex;
    }

    IntBuffer mIndex = null;
}