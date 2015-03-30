package com.podevs.android.poAndroid.poke;

import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

/**
 * Implementation of pokemon dex number into a readable form for coding
 */

public class UniqueID implements SerializeBytes {

	/**
	 * The international dex number
	 */

	public short pokeNum;

	/**
	 * The forme number
	 */

	public byte subNum;

	/**
	 * Constructor from Bais
	 *
	 * @see Bais
	 *
	 * @param msg Bais information
	 */

	public UniqueID(Bais msg) {
		pokeNum = msg.readShort();
		subNum = msg.readByte();
	}

	/**
	 * Constructor from two int
	 *
	 * @param s pokeNum
	 * @param b subNum
	 */

	public UniqueID(int s, int b) {
		pokeNum = (short) s;
		subNum = (byte) b;
	}

	/**
	 * Blank Constructor. num = 0 sub = 0
	 */
	public UniqueID() {
		pokeNum = 0;
		subNum = 0;
	}

	/**
	 * Constructor from string. Most likely from asset files
	 *
	 * @param str String of pokemon number. Example - "3:1" Mega Venasaur
	 *
	 */

	public UniqueID(String str) {
		int colon = str.indexOf(':');
		pokeNum = (short)Integer.parseInt(str.substring(0, colon));
		try  {
			subNum = (byte)Integer.parseInt(str.substring(colon+1));
		} catch (NumberFormatException e) {
			subNum = 0;
		}
	}

	/**
	 * Constructor from int. pokeNum = i modulo 65536. subNum = i right shift 16 bytes
	 *
	 * @param i int
	 */

	public UniqueID(int i) {
		pokeNum = (short) (i % (1 << 16));
		subNum = (byte) (i >> 16);
	}

	/**
	 * Overrides equals() method
	 *
	 * @param other Object to compare
	 *
	 * @return returns true if other equals this UniqueID
	 */

	@Override
	public boolean equals(Object other) {
		try {
			UniqueID o = (UniqueID) other;
			return pokeNum == o.pokeNum && subNum == o.subNum;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * Get hash code
	 *
	 * @return pokeNum + subNum shifted left 16 bytes
	 */

	@Override
	public int hashCode() {
		return (int)pokeNum + subNum*65536;
	}

	/**
	 * @return pokeNum
	 */

	public int originalHashCode() {
		return (int)pokeNum;
	}

	/**
	 * Unused.
	 *
	 * @param pokeNum pokNum
	 * @param subNum subNum
	 * @return pokeNum + subNum shifted left 16 bytes
	 */
	
	public int hashCode(int pokeNum, int subNum) {
		return (int)pokeNum + subNum*65536;
	}

	/**
	 * Writes Unique ID to Baos
	 *
	 * @see com.podevs.android.utilities.Baos
	 *
	 * @param bytes
	 */

	public void serializeBytes(Baos bytes) {
		bytes.putShort(pokeNum);
		bytes.write(subNum);
	}

	/**
	 * @return new UniqueID with same pokeNum but 0 subNum
	 */

	public UniqueID original() {
		return new UniqueID(pokeNum);
	}

	/**
	 *
	 * @return pokeNum + (subNum == 0 ? "" : "_" + subNum);
	 */
	@Override
	public String toString() {
		return "" + this.pokeNum + (this.subNum == 0 ? "" : "_" + this.subNum);
	}
}
