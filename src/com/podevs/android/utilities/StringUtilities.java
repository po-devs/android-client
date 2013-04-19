package com.podevs.android.utilities;

public class StringUtilities {
	public static String escapeHtml(String seq) {
		return seq.replace("&", "&amp;").replace("<", "&lt;");
	}
}
