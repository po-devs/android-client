package com.podevs.android.utilities;

import android.graphics.Color;

/**
 * QColor support
 */

public class QColor implements SerializeBytes {
	public int colorInt = Color.BLACK;
	public boolean invalid = true;
	private static final String[] nameColorList = new String[]{"#5811b1", "#399bcd", "#0474bb", "#f8760d", "#a00c9e", "#0d762b", "#5f4c00", "#9a4f6d", "#d0990f", "#1b1390", "#028678", "#0324b1"};

	public QColor(Bais msg) {
		byte spec = msg.readByte();
		/* Trick to convert signed short to unsigned short */
		int alpha = (msg.readShort() + 65536) % 65536;
		int red = (msg.readShort() + 65536) % 65536;
		int green = (msg.readShort() + 65536) % 65536;
		int blue = (msg.readShort() + 65536) % 65536;
		msg.readShort(); // read padding
		
		if (spec == 1) { //Rgb
			colorInt = Color.argb(alpha >> 8, red >> 8, green >> 8, blue >> 8);
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
	
	public void serializeBytes(Baos bytes) {
		if (invalid) {
			bytes.write(0); // Invalid spec
		} else {
			bytes.write(1); // RGB spec
		}
		
		bytes.putShort((short)(Color.alpha(colorInt) << 8));
		bytes.putShort((short)(Color.red(colorInt) << 8));
		bytes.putShort((short)(Color.green(colorInt) << 8));
		bytes.putShort((short)(Color.blue(colorInt) << 8));
		bytes.putShort((short)0);
	}
	
	public QColor() {
		invalid = true;
		colorInt = Color.BLACK;
	}
	
	public QColor(int colorInt) {
		invalid = false;
		this.colorInt = colorInt;
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

	public QColor(int id, int length) {
		colorInt = Color.parseColor(nameColorList[id % length]);
		invalid = false;
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
	
	public boolean equals(QColor c) {
		return (c.invalid && invalid) || (!c.invalid && !invalid && colorInt == c.colorInt);
	}
	
	public boolean isValid() {
		return !invalid;
	}
}

