package com.podevs.android.utilities;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Custom ByteArrayInputStream
 */

public class Bais extends ByteArrayInputStream {
	final static String TAG = "BAIS";

	public Bais(byte[] b) {
		super(b);
	}
	
	public String readString() {
		String str = "";
		int len = readInt();
		// What Qt sends for null string
		if (len == -1) {
			return str;
		}
		if (len > available()) {
			//XXX Bad things happened do something about them.
			return str;
		}
		byte[] bytes = new byte[len];
		read(bytes, 0, len);
		
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
				bools.putBool(((readByte & 0x1) == 1));
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
			return new byte[0];
		}
		if (len > this.available()) {
			//XXX Bad things happened do something about them.
			return new byte[0];
		}
		byte tmpBuf[] = new byte[len];
		try {
			read(tmpBuf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tmpBuf;
	}

	public ArrayList<String> readQStringList() {
		int len = readInt();
		if (len < 0 || len > available()/4) {
			// XXX Bad things happened do something about them.
			return null;
		}
		ArrayList<String> list = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			list.add(readString());
		}
		return list;
	}

	public byte[] remaining() {
		return Arrays.copyOfRange(buf, pos, count);
	}

	public Bais cloneRemaining() {
		return new Bais(remaining());
	}

	@Override
	public String toString() {
		return "Bais{" + toBase64() + "}";
	}

	public String toBase64() {
		return Base64.encodeToString(remaining(), Base64.DEFAULT);
	}

	public static Bais fromBase64(String encoded) {
		return new Bais(Base64.decode(encoded, Base64.DEFAULT));
	}
}