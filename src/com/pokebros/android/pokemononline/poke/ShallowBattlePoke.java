package com.pokebros.android.pokemononline.poke;

import java.util.LinkedList;

import android.text.Html;
import android.text.SpannableStringBuilder;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.DataBaseHelper;
import com.pokebros.android.pokemononline.SerializeBytes;
import com.pokebros.android.pokemononline.battle.Type;
import com.pokebros.android.pokemononline.poke.PokeEnums.Status;

// This class represents the Opponent's poke during a battle.
public class ShallowBattlePoke implements SerializeBytes {
	public String rnick, nick = "", pokeName = "";
	int fullStatus = 0;
	public UniqueID uID = new UniqueID();
	public Type[] types = new Type[2];
	public boolean shiny = false;
	public byte gender = 0;
	public byte lifePercent = 0;
	public byte level = 0;
	public byte lastKnownPercent = 0;
	public boolean sub = false;
	public LinkedList<UniqueID> specialSprites = new LinkedList<UniqueID>();
	
	public ShallowBattlePoke() {}; // For pokes who have not been sent out;
	
	public ShallowBattlePoke(Bais msg, boolean isMe, DataBaseHelper db, Gen gen) {
		uID = new UniqueID(msg);
		rnick = nick = msg.readString();
		if (!isMe) {
			nick = "the foe's " + nick;
			
			// A little optimization; these only matter if it's not your poke
			getName(db);
			getTypes(db, gen.num);
		}
		lifePercent = msg.readByte();
		fullStatus = msg.readInt();
		gender = msg.readByte();
		shiny = msg.readBool();
		level = msg.readByte();
	}
	
	public void serializeBytes(Baos b) {
		b.putBaos(uID);
		b.putString(nick);
		b.write(lifePercent);
		b.putInt(fullStatus);
		b.write(gender);
		b.putBool(shiny);
		b.write(level);
	}
	
	public SpannableStringBuilder nameAndType() {
		SpannableStringBuilder s = new SpannableStringBuilder(Html.fromHtml("<b>" + pokeName + "</b>"));
		s.append("\n" + types[0]);
		if(types[1] != Type.Curse) s.append("/" + types[1]);
		return s;
	}
	
	void getName(DataBaseHelper db) {
		pokeName = db.query("SELECT Name from [Pokemons] WHERE (Num = " + 
				uID.pokeNum + ") AND (Forme = " + uID.subNum + ")");
	}
	
	
	void getTypes(DataBaseHelper db, byte gen) {
		for(int i = 0; i < 2; i++) {
			String res = db.query("SELECT G" + gen + "T" + (i+1) + " from [Pokemons] WHERE (Num = " +
					uID.pokeNum + ") AND (Forme = " + uID.subNum + ")");
			if (uID.subNum != 0 && res.length() == 0)
				// No type specified for this forme,
				// attempt to lookup for base forme
				res = db.query("SELECT G" + gen + "T" + (i+1) + " from [Pokemons] WHERE (Num = " +
						uID.pokeNum + ") AND (Forme = 0)");
			if (res.length() == 0 || res.equals(DataBaseHelper.ERROR))
				// This should never happen but not having a type is probably bad
				// give it curse type at least
				res = Integer.valueOf(Type.Curse.ordinal()).toString();
			types[i] = Type.values()[Integer.valueOf(res)];
		}
	}
	
	public void changeStatus(byte status) {
		/* Clears past status */
		fullStatus = fullStatus & ~( (1 << Status.Koed.poValue()) | 0x3F);
		/* Adds new status */
		fullStatus = fullStatus | ( 1 << status);
	}
	
	public final int status() {
		if ((fullStatus & (1 << Status.Koed.poValue())) != 0)
			return Status.Koed.poValue();
		// intlog2(fullStatus & 0x3F)
		int x = fullStatus & 0x3F;
		int i;
		for (i = 0; x > 1; i++) {
			x/=2;
		}
		return i;
	}
}
