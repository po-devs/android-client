package com.pokebros.android.pokemononline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.util.Log;

public class Baos extends ByteArrayOutputStream {
	final static String TAG = "BAOS";
	
	public Baos() {
		super();
	}
	
	public Baos(int size) {
		super(size);
	}
	
	public Baos putInt(int i) {
		byte[] bytes = new byte[4];
		
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putInt(i);
		bb.rewind();
		bb.get(bytes);
		
		try {
			write(bytes);
		} catch (Exception e) {
			System.exit(-1);
		}
		
		return this;
	}
	
	public Baos putShort(short s) {
		byte[] bytes = new byte[2];
		
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putShort(s);
		bb.rewind();
		bb.get(bytes);
		
		try {
			write(bytes);
		} catch (Exception e) {
			System.exit(-1);
		}
		
		return this;
	}
	
	public Baos putString(String s) {
		putInt(s.length());
		try {
			write(s.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupported encoding while trying to put string");
		} catch (IOException e) {
			Log.e(TAG, "IOException while trying to put string");
		}
		
		return this;
	}
	
	public Baos putBool(boolean bool) {
		write((byte)(bool ? 1 : 0));
		
		return this;
	}
	
	public Baos putBaos(SerializeBytes src) {
		try {
			src.serializeBytes(this);
		} catch (Exception e) {
			System.exit(-1);
		}
		return this;
	}
}
