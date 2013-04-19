package com.podevs.android.utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


import android.util.Log;

public class Bais extends ByteArrayInputStream {
	final static String TAG = "BAIS";

	public Bais(byte[] b) {
		super(b);
	}
	
	public String readString() {
		int len = readInt();
		if (len == -1) // What Qt sends for null string
			return null;
		byte[] bytes = new byte[len];
		read(bytes, 0, len);
		
		String str = null;
		try {
			str = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupported encoding while trying to get string");
		}
		
		return str;
	}
	
	public short readShort() {
		short s = 0;
		s |= (read() << 8);
		s |=  read();
		
		return s;
	}
	
	public byte readByte() {
		return (byte)read();
	}
	
	public int readInt() {
		int i = 0;
		i |= (read() << 24);
		i |= (read() << 16);
		i |= (read() << 8);
		i |=  read();
		
		return i;
	}
	
	public boolean readBool() {
		boolean ret = false;
		if(read() == 1) ret = true;
		return ret;
	}
	
	public Bais readFlags() {
		byte readByte;
		Baos bools = new Baos();
		do {
			readByte = readByte();
			for (int i = 0; i < 7; i++) {
				bools.putBool(((readByte & 0x1) == 1 ? true : false));
				readByte = (byte)(((int)readByte & 0xff) >>> 1); // Silly java I just want to left shift one
			}
		} while (readByte == 1); // While MSB == 1
		return new Bais(bools.toByteArray());
	}
	
	public byte[] readVersionControlData() {
		return readQByteArray(true);
	}
	
	public byte[] readQByteArray() {
		return readQByteArray(false);
	}
	
	public byte[] readQByteArray(boolean isShort) {
		int len = isShort ? ((int) (readShort()+65536)) % 65536  : readInt();
		if (len < 0) {
			// XXX Bad things happened do something about them.
			return null;
		}
		byte tmpBuf[] = new byte[len];
		try {
			read(tmpBuf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmpBuf;
	}

	public ArrayList<String> readQStringList() {
		int len = readInt();
		if (len < 0) {
			// XXX Bad things happened do something about them.
			return null;
		}
		ArrayList<String> list = new ArrayList<String>(len);
		for (int i = 0; i < len; i++) {
			list.add(readString());
		}
		return list;
	}
}
