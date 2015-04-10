package com.podevs.android.utilities;

import android.content.Context;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Extra string methods
 */

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
		return SimpleDateFormat.getTimeInstance(SimpleDateFormat.MEDIUM, Locale.UK).format(System.currentTimeMillis());
	}
	
	public static String def(String s, String def) {
		return s == null ? def : s;
	}

	public static String getFileContent(Context ctx, String path) {
		FileInputStream in = null;
		try {
			in = ctx.openFileInput(path);
		} catch (FileNotFoundException e) {
			return "";
		}

		BufferedReader br;
		try {
			br = new BufferedReader( new InputStreamReader(in, "UTF-8" ));
		} catch (UnsupportedEncodingException e) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		String line;
		try {
			while(( line = br.readLine()) != null ) {
				sb.append( line );
				sb.append( '\n' );
			}
		} catch (IOException e) {
		}
		return sb.toString();
	}
}
