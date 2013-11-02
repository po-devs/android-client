package com.podevs.android.pokemononline.pokeinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.content.Context;

import com.podevs.android.pokemononline.poke.UniqueID;

public class InfoFiller {
	static void fill(String file, Filler filler) {
		InputStream assetsDB = null;
		try {
			assetsDB = getContext().getAssets().open(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new InputStreamReader(assetsDB, "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			while (buf.ready()) {
				String str = buf.readLine();
				/*
				 * Test for BOM
				 */
				if (str.length() > 0 && (int)str.charAt(0) == 65279) {
					str = str.substring(1);
				}

				int spaceIndex = str.indexOf(' ');

				if (spaceIndex < 0) {
					break;
				}

				filler.fill(Integer.parseInt(str.substring(0, spaceIndex)), str.substring(spaceIndex + 1));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			assetsDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void uIDfill(String file, Filler filler, boolean readAll) {

		InputStream assetsDB = null;
		try {
			assetsDB = getContext().getAssets().open(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new InputStreamReader(assetsDB, "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			while (buf.ready()) {
				String str = buf.readLine();
				/*
				 * Test for BOM
				 */
				if (str.length() > 0 && (int)str.charAt(0) == 65279) {
					str = str.substring(1);
				}

				int spaceIndex = str.indexOf(' ');

				if (spaceIndex < 0) {
					if (!readAll) {
						break;
					} else {
						filler.fill(new UniqueID(str).hashCode(), "");
						continue;
					}
				}

				filler.fill(new UniqueID(str.substring(0, spaceIndex)).hashCode(), 
						str.substring(spaceIndex + 1));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			assetsDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void uIDfill(String file, OptionsFiller filler) {

		InputStream assetsDB = null;
		try {
			assetsDB = getContext().getAssets().open(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new InputStreamReader(assetsDB, "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		try {
			while (buf.ready()) {
				String str = buf.readLine();
				/*
				 * Test for BOM
				 */
				if (str.length() > 0 && (int)str.charAt(0) == 65279) {
					str = str.substring(1);
				}

				int spaceIndex = str.indexOf(' ');

				if (spaceIndex < 0) {
					break;
				}

				int index1 = str.indexOf(':');
				int index2 = str.lastIndexOf(':', spaceIndex);

				if (index2 == index1) {
					filler.fill(new UniqueID(str.substring(0, spaceIndex)).hashCode(),
							str.substring(spaceIndex + 1), null);
				} else {
					filler.fill(new UniqueID(str.substring(0, index2)).hashCode(),
							str.substring(spaceIndex + 1), str.substring(index2+1, spaceIndex));
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			assetsDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void uIDfill(String file, Filler filler) {
		uIDfill(file, filler, false);
	}

	private static Context getContext() {
		return InfoConfig.context;
	}

	public static interface Filler {
		void fill(int i, String s);
	}

	public static interface OptionsFiller {
		void fill(int i, String s, String options);
	}

	public static abstract class FillerByte implements Filler {
		public void fill(int i, String s) {
			fillByte(i, (byte)Integer.parseInt(s));
		}

		abstract void fillByte(int i, byte b);
	}

	public static abstract class FillerInt implements Filler {
		public void fill(int i, String s) {
			fillInt(i, Integer.parseInt(s));
		}

		abstract void fillInt(int i, int b);
	}
}
