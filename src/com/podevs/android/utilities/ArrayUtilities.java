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
}
