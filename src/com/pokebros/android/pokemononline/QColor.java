package com.pokebros.android.pokemononline;

import android.graphics.Color;

import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

public class QColor implements SerializeBytes {
	protected int colorInt = Color.BLACK;
	public boolean invalid = true;
	
	public QColor(Bais msg) {
		byte spec = msg.readByte();
		/* Trick to convert signed short to unsigned short */
		int alpha = (msg.readShort() + 65536) % 65536;
		int red = (msg.readShort() + 65536) % 65536;
		int green = (msg.readShort() + 65536) % 65536;
		int blue = (msg.readShort() + 65536) % 65536;
		msg.readShort(); // read padding
		
		if (spec == 1) { //Rgb
			colorInt = Color.argb(alpha, red, green, blue);
			invalid = false;
		} else if (spec == 2) { //HSV
			float hsv[] = { ((float)red) * 360 / 65536, ((float)green) * 360 / 65536, ((float)blue) / 65536};
			colorInt = Color.HSVToColor(alpha, hsv);
			invalid = false;
		} else { //unsupported spec
			colorInt = Color.BLACK;
			invalid = true;
		}
	}
	
	public QColor() {
	}
	
	public QColor(String hex) {
		if (hex.length() == 0) {
			invalid = true;
			colorInt = Color.BLACK;
		} else {
			colorInt = Color.parseColor(hex);
			invalid = false;
		}
	}
	
	public void serializeBytes(Baos bytes) {
		if (invalid) {
			bytes.write(0); // Invalid spec
		} else {
			bytes.write(1); // RGB spec
		}
		
		bytes.putShort((short)Color.alpha(colorInt));
		bytes.putShort((short)Color.red(colorInt));
		bytes.putShort((short)Color.green(colorInt));
		bytes.putShort((short)Color.blue(colorInt));
		bytes.putShort((short)0);
	}
	
	public String toHexString() {
		if (invalid) {
			return "";
		}
		if (Color.alpha(colorInt) == 255) {
			return String.format("#%02x%02x%02x", Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt));
		} else {
			return String.format("#%02x%02x%02x%02x", Color.alpha(colorInt), Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt));
		}
	}
}

