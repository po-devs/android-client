package com.pokebros.android.pokemononline.battle;

import com.pokebros.android.pokemononline.DataBaseHelper;
import com.pokebros.android.pokemononline.poke.Gen;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;
import com.pokebros.android.pokemononline.poke.UniqueID;
import com.pokebros.android.utilities.Bais;
import com.pokebros.android.utilities.Baos;

// This class represents your poke during a battle.
public class BattlePoke extends ShallowBattlePoke {
	public short currentHP = 0;
	public short totalHP = 0;
	short item = 0;
	public String itemString;
	short ability = 0;
	public String abilityString;
	byte statusCount = 0;
	byte originalStatusCount = 0;
	byte nature = 0;
	byte happiness = 0;
	public byte teamNum;
	
	public short[] stats = new short[5];
	public BattleMove[] moves = new BattleMove[4];
	
	int[] DVs = new int[6];
	int[] EVs = new int[6];
	
	public BattlePoke(Bais msg, DataBaseHelper db, Gen gen) {
		uID = new UniqueID(msg);
		nick = msg.readString();
		totalHP = msg.readShort();
		currentHP = msg.readShort();
		gender = msg.readByte();
		shiny = msg.readBool();
		level = msg.readByte();
		item = msg.readShort();
		itemString = Battle.itemName(item); // XXX This is probably how we should do all of these instead of passing db around
		ability = msg.readShort();
		abilityString = db.query("SELECT Name FROM [Abilities] WHERE _id = " + (ability + 1));
		happiness = msg.readByte();
		getTypes(db, gen.num);
		getName(db);
		
		for(int i = 0; i < 5; i++)
			stats[i] = msg.readShort();
		for(int i = 0; i < 4; i++)
			moves[i] = new BattleMove(msg, db);
		/* EVs and DVs are QLists on the server end,
		 * so we need to discard the int representing
		 * the number of items in the list. */
		//msg.readInt();
		for(int i = 0; i < 6; i++) {
			EVs[i] = msg.readInt();
		//msg.readInt();
		}for(int i = 0; i < 6; i++)
			DVs[i] = msg.readInt();
	}
	
	@Override
	public void serializeBytes(Baos b) {
		b.putBaos(uID);
		b.putString(nick);
		b.putShort(totalHP);
		b.putShort(currentHP);
		b.write(gender);
		b.putBool(shiny);
		b.write(level);
		b.putShort(item);
		b.putShort(ability);
		b.write(happiness);
		for(int i = 0; i < 5; i++)
			b.putShort(stats[i]);
		for(int i = 0; i < 4; i++)
			b.putBaos(moves[i]);
		for(int i = 0; i < 6; i++)
			b.write(EVs[i]);
		for(int i = 0; i < 6; i++)
			b.write(DVs[i]);
	}
	
	public int hiddenPowerType() {
		return ( ( ( (DVs[0] & 1) + 2 * (DVs[1] & 1) + 4 * (DVs[2] & 1) + 8 * (DVs[5] & 1) + 16 * (DVs[3] & 1) + 32 * (DVs[4] & 1) ) * 15) / 63 ) + 1;
	}
	
	public String printStats() {
		String s = "";
		for (int i = 0; i < 5; i++)
			s += (i == 0 ? "" : "\n") + stats[i];
		return s;
	}
}
