package com.pokebros.android.utilities;

public class StringUtilities {
	public static String escapeHtml(CharSequence seq) {
		return seq.toString().replace("<", "&lt;");
	}
}
