package com.pokebros.android.pokemononline;

public class QColor implements SerializeBytes {
	protected byte spec;
	protected short alpha;
	protected short red;
	protected short green;
	protected short blue;
	protected short pad;
	public String html;
	
	public QColor(Bais msg) {
		spec = msg.readByte();
		alpha = msg.readShort();
		red = msg.readShort();
		green = msg.readShort();
		blue = msg.readShort();
		pad = msg.readShort();
		html = "color = #" + String.format("%02X", (byte)red) + String.format("%02X", (byte)green) + String.format("%02X", (byte)blue) + ">";
	}
	
	public QColor() {
			spec = 0;
			alpha |= 0xffff;
			red = green = blue = 0;
			pad = 0;
			html = ">";
	}
	
	public void serializeBytes(Baos bytes) {
		bytes.write(spec);
		
		bytes.putShort(alpha);
		bytes.putShort(red);
		bytes.putShort(green);
		bytes.putShort(blue);
		bytes.putShort(pad);
	}
	
	@Override
	public String toString() {
		return html; 
	}
	
	       
	public String toHexString() {
		if (spec == 1) {
			int red = 0 > this.red ? this.red + 32767 + 32768 : this.red;
			int green = 0 > this.green ? this.red + 32767 + 32768 : this.green;
			int blue = 0 > this.blue ? this.red + 32767 + 32768 : this.blue;
			return String.format("#%02x%02x%02x", red >> 8, green >> 8, blue >> 8);
		} else {
			return null; // TODO: fix other color formats
		}
	}
}

