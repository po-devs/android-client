package com.podevs.android.utilities;

/**
 * Basic Array utilities
 */

public class ArrayUtilities {
	/**
	 * indexOf arbitrary set and object
	 *
	 * @param needle what to find
	 * @param haystack where to find
	 * @param <T> Object type
	 * @return index of needle in haystack
	 */
	public static <T> int indexOf(T needle, T[] haystack)
	{
		for (int i=0; i<haystack.length; i++)
		{
			if (haystack[i] != null && haystack[i].equals(needle)
					|| needle == null && haystack[i] == null) return i;
		}

		return -1;
	}

	/**
	 * indexOf
	 *
	 * @param needle short needle
	 * @param haystack short haystack
	 * @return index of needle in haystack
	 */
	public static int indexOf(short needle, short[] haystack)
	{
		for (int i=0; i<haystack.length; i++)
		{
			if (haystack[i] == needle) {
				return i;
			}
		}

		return -1;
	}

	public static <E> String join(E[] e, String separator) {
		String s = "";
		int length = e.length;
		for (int i = 0; i < length; i++) {
			s += e[i].toString();
			if (!(i == length -1)) {
				s += separator;
			}
		}
		return s;
	}

	public static String join(byte[] e, String separator) {
		String s = "";
		int length = e.length;
		for (int i = 0; i < length; i++) {
			s += e[i];
			if (!(i == length -1)) {
				s += separator;
			}
		}
		return s;
	}
}
