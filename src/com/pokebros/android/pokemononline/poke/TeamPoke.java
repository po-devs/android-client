package com.pokebros.android.pokemononline.poke;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.SerializeBytes;

// This class is how a poke is represented in the teambuilder.
public class TeamPoke extends SerializeBytes {
	protected UniqueID uID;
	protected String nick;
	protected short item;
	protected short ability;
	protected byte nature;
	protected byte gender;
	protected byte gen;
	protected boolean shiny;
	protected byte happiness;
	protected byte level;
	protected int[] moves = new int[4];
	protected byte[] DVs = new byte[6];
	protected byte[] EVs = new byte[6];
	
	public TeamPoke(Bais msg) {
		uID = new UniqueID(msg);
		nick = msg.readQString();
		item = msg.readShort();
		ability = msg.readShort();
		nature = msg.readByte();
		gender = msg.readByte();
		//gen = msg.readByte();
		shiny = msg.readBool();
		happiness = msg.readByte();
		level = msg.readByte();
		
		for(int i = 0; i < 4; i++)
			moves[i] = msg.readInt();
		for(int i = 0; i < 6; i++)
			DVs[i] = msg.readByte();
		for(int i = 0; i < 6; i++)
			EVs[i] = msg.readByte();
	}
	
	public TeamPoke() {
		uID = new UniqueID();
		nick = "LOLZ";
		item = 71;
		ability = 98;
		nature = 0;
		gender = 1;
		gen = 5;
		shiny = true;
		happiness = 127;
		level = 100;
		/*moves[0] = 331;
		moves[1] = 213;
		moves[2] = 412;
		moves[3] = 210;*/
		moves[0] = 118;
		moves[1] = 227;
		moves[2] = 150;
		moves[3] = 271;
		DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
		EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 10;
	}

	public TeamPoke (String[][] mTP, int[][] mM, byte[][] mDV, byte[][] mEV, int i) {
		uID = new UniqueID((short)(Integer.parseInt(mTP[i][0])), (byte)(Integer.parseInt(mTP[i][1])));
		nick = mTP[i][2];
		item = (short)(Integer.parseInt(mTP[i][3]));
		ability = (short)(Integer.parseInt(mTP[i][4]));
		nature = (byte)(Integer.parseInt(mTP[i][5]));
		gender = (byte)(Integer.parseInt(mTP[i][6]));
		if (mTP[i][7].equals("0")) {
			shiny = false;
		}
		else {
			shiny = true;
		}
		happiness = (byte)(Integer.parseInt(mTP[i][8]));
		level = (byte)(Integer.parseInt(mTP[i][9]));
		for (int j = 0; j < 4; j++) {
			moves[j] = mM[i][j];
		}
		for (int j = 0; j < 6; j++) {
			DVs[j] = mDV[i][j];
		}
		for (int j = 0; j < 6; j++) {
			EVs[j] = mEV[i][j];
		}
	}
	
	public Baos serializeBytes() {
		Baos bytes = new Baos();
		bytes.putBaos(uID);
		bytes.putString(nick);
		bytes.putShort(item);
		bytes.putShort(ability);
		bytes.write(nature);
		bytes.write(gender);
		// bytes.write(gen); XXX Gen would go here 
		bytes.putBool(shiny);
		bytes.write(happiness);
		bytes.write(level);
		for (int i = 0; i < 4; i++) {
			bytes.putInt(moves[i]);
		}
		for (int i = 0; i < 6; i++) bytes.write(DVs[i]);
		for (int i = 0; i < 6; i++) bytes.write(EVs[i]);
		return bytes;
	}
}
