package com.podevs.android.utilities;

/**
 * Basic Array utilities
 */

public class ArrayUtilities {
	public static <T> int indexOf(T needle, T[] haystack)
	{
		for (int i=0; i<haystack.length; i++)
		{
			if (haystack[i] != null && haystack[i].equals(needle)
					|| needle == null && haystack[i] == null) return i;
		}

		return -1;
	}

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
