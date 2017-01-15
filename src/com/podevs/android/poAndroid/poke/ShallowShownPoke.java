package com.podevs.android.poAndroid.poke;

import android.text.Html;
import android.text.SpannableStringBuilder;
import com.podevs.android.poAndroid.ColorEnums;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class ShallowShownPoke implements SerializeBytes {
	public boolean item;
	public UniqueID uID;
	public byte level;
	public byte gender;
	public String pokeName;
	public TypeInfo.Type[] types = new TypeInfo.Type[2];
	public Integer[][] stats = new Integer[2][6];
	
	public ShallowShownPoke(Bais msg, byte gen) {
		uID = new UniqueID(msg);
		level = msg.readByte();
		gender = msg.readByte();
		item = msg.readBool();
		pokeName = PokemonInfo.name(uID);
		types[0] = TypeInfo.Type.values()[PokemonInfo.type1(uID, gen)];
		types[1] = TypeInfo.Type.values()[PokemonInfo.type2(uID, gen)];
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < 6; i++) {
				stats[j][i] = PokemonInfo.calcMinMaxStat(uID, i, gen, level, j);
			}
		}
	}
	
	public void serializeBytes(Baos b) {
		b.putBaos(uID);
		b.write(level);
		b.write(gender);
		b.putBool(item);
	}

	public SpannableStringBuilder nameAndType() {
		SpannableStringBuilder s = new SpannableStringBuilder(Html.fromHtml("<b>" + pokeName + "</b>" + (gender == 0 ? "" : (gender == 1 ? " M." : " F.")) + " Lv. " + level));
		s.append(Html.fromHtml("<br>" + "<font color=\"" + ColorEnums.TypeColor.values()[(byte) types[0].ordinal()].toString().replaceAll(">", "") + "\">" + types[0].toString() + "</font>"));
		if(types[1] != TypeInfo.Type.Curse) s.append(Html.fromHtml("/" + "<font color=\"" + ColorEnums.TypeColor.values()[(byte) types[1].ordinal()].toString().replaceAll(">", "") + "\">" + types[1].toString() + "</font>"));
		return s;
	}

	public SpannableStringBuilder moves() {
		return movesString();
	}

	private static SpannableStringBuilder movesString() {
		SpannableStringBuilder s = new SpannableStringBuilder();
		for (int i = 0; i < 4; i++) {
			s.append(i == 0 ? "" : "\n");
			s.append("????" + "    " +"??/??");
		}
		return s;
	}

	public String statString() {
		String s = "";
		s += (stats[0][0] + "-" + stats[1][0]);
		for (int i = 1; i < 6; i++) {
			s += (i == 0 ? "" : "\n") + (int)(stats[0][i]) + "-" + (int)(stats[1][i]);
		}
		return s;
	}
}
