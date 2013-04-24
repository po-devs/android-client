package com.podevs.android.utilities;

import java.text.SimpleDateFormat;

public class StringUtilities {
	public static String escapeHtml(String seq) {
		return seq.replace("&", "&amp;").replace("<", "&lt;");
	}
	
	public static String join(String[] strings, String sep) {
		String ret = "";
		
		for (int i = 0; i < strings.length; i++) {
			ret = ret.concat(strings[i]);
			
			if (i + 1 < strings.length) {
				ret = ret.concat(sep);
			}
		}
		
		return ret;
	}
	
	public static String timeStamp() {
		return SimpleDateFormat.getTimeInstance().format(Long.valueOf(System.currentTimeMillis()));
	}
}
