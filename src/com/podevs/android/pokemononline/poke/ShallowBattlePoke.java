package com.podevs.android.pokemononline.poke;

import java.util.LinkedList;

import android.text.Html;
import android.text.SpannableStringBuilder;

import com.podevs.android.pokemononline.battle.Type;
import com.podevs.android.pokemononline.poke.PokeEnums.Status;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

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
	
	public ShallowBattlePoke(Bais msg, boolean isMe, Gen gen) {
		uID = new UniqueID(msg);
		rnick = nick = msg.readString();
		if (!isMe) {
			nick = "the foe's " + nick;
			
			// A little optimization; these only matter if it's not your poke
			pokeName = PokemonInfo.name(uID);
			types[0] = Type.values()[PokemonInfo.type1(uID, gen.num)];
			types[1] = Type.values()[PokemonInfo.type2(uID, gen.num)];
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
