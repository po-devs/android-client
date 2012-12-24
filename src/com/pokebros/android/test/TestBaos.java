package com.pokebros.android.test;

import junit.framework.TestCase;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;

public class TestBaos extends TestCase {	
	public void testPutFlags() {
		/* The flags we are testing */
		boolean flags[] = new boolean[] {true,true,false,true,
				false,false,true,false,false,true,false};
		
		Baos output = new Baos();
		output.putFlags(flags);
		Bais input = new Bais(output.toByteArray());
		Bais read = input.readFlags();
		
		boolean result[] = new boolean[flags.length];
		for (int i = 0; i < result.length; i++) {
			if (read.available() != 0) {
				result[i] = read.readBool();
			} else {
				result[i] = false;
			}
		}
		
		for (int i = 0; i < flags.length; i++)
		    assertEquals("mismatch at " + i, flags[i], result[i]);
	}

}
