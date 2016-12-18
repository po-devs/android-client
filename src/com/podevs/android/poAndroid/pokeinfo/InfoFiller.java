package com.podevs.android.poAndroid.pokeinfo;

import android.content.Context;
import com.podevs.android.poAndroid.poke.UniqueID;

import java.io.*;

public class InfoFiller {

	private static InputStream assetsDB = null;
	private static BufferedReader buf = null;

	static void fill(String file, Filler filler) {
		if (!loadInputStreamAndBufferedReader(file))
			return;

		try {
			while (buf.ready()) {
				String str = buf.readLine();
				/*
				 * Test for BOM
				 */
				if (str == null) {
					break;
				}
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
		closeInputStream();
	}

    static void plainFill(String file, Filler filler) {
		if (!loadInputStreamAndBufferedReader(file))
			return;

        try {
            while (buf.ready()) {
                String str = buf.readLine();
				/*
				 * Test for BOM
				 */
                if (str == null) {
                    break;
                }
                if (str.length() > 0 && (int)str.charAt(0) == 65279) {
                    str = str.substring(1);
                }

                filler.fill(Integer.parseInt(str), null);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        closeInputStream();
    }


	static void uIDfill(String file, Filler filler, boolean readAll) {
		if (!loadInputStreamAndBufferedReader(file))
			return;

		try {
			while (buf.ready()) {
				String str = buf.readLine();
				/*
				 * Test for BOM
				 */
				if (str == null) {
					break;
				}
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
		closeInputStream();
	}

	static void uIDfill(String file, OptionsFiller filler) {
		if (!loadInputStreamAndBufferedReader(file))
			return;

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
		closeInputStream();
	}

	private static boolean loadInputStreamAndBufferedReader(String file) {
		assetsDB = null;
		buf = null;

		try {
			assetsDB = getContext().getAssets().open(file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try {
			buf = new BufferedReader(new InputStreamReader(assetsDB, "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		return true;

	}

	private static void closeInputStream() {
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

	public interface Filler {
		void fill(int i, String s);
	}

	public interface OptionsFiller {
		void fill(int i, String s, String options);
	}

	public static abstract class FillerByte implements Filler {
		public void fill(int i, String s) {
			fillByte(i, (byte)Integer.parseInt(s));
		}

		abstract void fillByte(int i, byte b);
	}

	/*
	public static abstract class FillerInt implements Filler {
		public void fill(int i, String s) {
			fillInt(i, Integer.parseInt(s));
		}

		abstract void fillInt(int i, int b);
	}
	*/
}
