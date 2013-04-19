package com.podevs.android.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.podevs.android.pokemononline.SerializeBytes;

import android.util.Log;

public class Baos extends ByteArrayOutputStream {
	final static String TAG = "BAOS";
	
	public Baos() {
		super();
	}
	
	public Baos(int size) {
		super(size);
	}
	
	public Baos putBytes(byte [] bytes) {
		try {
			putInt(bytes.length);
			write(bytes);
		} catch (Exception e) {
			System.exit(-1);
		}
		return this;
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
	
	/**
	 * Flags serialized are a succession of 7 boolean bits, followed by one control
	 * bit telling if there's another 8 bits coming or not.
	 * @param flags An array of boolean to serialize
	 * @return the {@link Baos} instance for further serialization
	 */
	public Baos putFlags(boolean[] flags) {
		short data = 0;
		for (int i = 0, bytepos=0; i < flags.length; i++,bytepos++) {
			if (bytepos != 7) {
				data += (flags[i]?1:0) << bytepos;
			} else {
				bytepos = 0;
				data += (1 << 7);
				write((byte)data);
				data = (short) (flags[i] ? 1 : 0);
			}
		}
		write((byte)data);
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
		byte bytes [];
		try {
			bytes = s.getBytes("UTF-8");
			putInt(bytes.length);
			write(bytes);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupported encoding while trying to put string");
			putInt(0);
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
	
	/**
	 * Serializes a version-controlled structure.
	 * The structure just has to serialize itself in a bew baos,
	 * then it calls baos.versionControl(X, newBaos) on the give
	 * baos. (X obviously being the version of the serialization)
	 * 
	 * @param version The version of the serialization
	 * @param b The serialized content
	 * @return Self
	 */
	public Baos putVersionControl(int version, Baos b) {
		putShort((short)(b.size()+1));
		write((byte) version);
		write(b.buf, 0, b.count);
		return this;
	}
}
