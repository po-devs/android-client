package com.podevs.android.pokemononline.poke;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.podevs.android.pokemononline.pokeinfo.HiddenPowerInfo;
import com.podevs.android.pokemononline.pokeinfo.ItemInfo;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;
import com.podevs.android.pokemononline.pokeinfo.StatsInfo.Stats;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

// This class is how a poke is represented in the teambuilder.
public class TeamPoke implements SerializeBytes, Poke {
	public UniqueID uID;
	public String nick;
	public short item;
	public short pokeball;
	public short ability;
	public byte nature;
	public byte gender;
	public Gen gen;
	public boolean shiny;
	public byte happiness;
	public byte level;
	public TeamMove[] moves = new TeamMove[4];
	public byte[] DVs = new byte[6];
	public byte[] EVs = new byte[6];
	
	public TeamPoke(Bais msg) {
		loadFromBais(msg);
	}
	
	public TeamPoke(Bais msg, Gen gen) {
		this.gen = gen;
		loadFromBais(msg);
	}
	
	public void loadFromBais(Bais msg) {
		Bais b = new Bais(msg.readVersionControlData());
		int version = b.read();
		
		if (version != 0) {
		
		}
		
		Bais network = b.readFlags();

//		hasGen, hasNickname, hasPokeball, hasHappiness, hasPPups, hasIVs,
//      isShiny=0
		if (network.readBool()) { // gen flag
			gen = new Gen(b);
		} else if (gen == null) {
			gen = new Gen();
		}
		uID = new UniqueID(b);
		level = b.readByte();
		
		Bais data = b.readFlags();
		shiny = data.readBool();
		
		if (network.readBool()) { //nickname flag
			nick = b.readString();
		} else {
			nick = "";
		}

		if (network.readBool()) { //pokeball flag
			pokeball = b.readShort();
		} else {
			pokeball = 0;
		}
		
		if (gen.num > 1) {
			item = b.readShort();
			if (gen.num > 2) {
				ability = b.readShort();
				nature = b.readByte();
			}

			gender = b.readByte();
			if (gen.num > 2 && network.readBool()) { //happiness flag
				happiness = b.readByte();
			}
		}
		
		boolean ppups = network.readBool(); //ppup flags
		for (int i = 0; i < 4; i++) {
			if (ppups) {
				b.readByte(); // read the pp up for the move, but ignore it
			}
			moves[i] = new TeamMove(b.readShort());
		}
		
		for(int i = 0; i < 6; i++)
			EVs[i] = b.readByte();
		
		if (network.readBool()) { //Ivs flags
			for(int i = 0; i < 6; i++)
				DVs[i] = b.readByte();
		} else {
			DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
		}
	}
	
	public TeamPoke() {
		uID = new UniqueID();
		nick = "";
		item = 0;
		ability = 0;
		nature = 0;
		gender = 1;
		gen = new Gen();
		shiny = true;
		happiness = 0;
		level = 100;
		/*moves[0] = 331;
		moves[1] = 213;
		moves[2] = 412;
		moves[3] = 210;*/
		moves[0] = new TeamMove(0);
		moves[1] = new TeamMove(0);
		moves[2] = new TeamMove(0);
		moves[3] = new TeamMove(0);
		DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
		EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 80;
	}
	
	public void setNum(UniqueID id) {
		if (uID.equals(id)) {
			return;
		}
		
		boolean fullReset = uID.pokeNum != id.pokeNum;
		
		uID = id;
		if (fullReset) {
			nick = PokemonInfo.name(id);
			shiny = false;
			item = 15; //leftovers
		}
		if (id.subNum != 0) {
			if (id.pokeNum == 487) {
				/* Giratina-O */
				item =  (short) (id.subNum == 1 ? 213 : 15); // griseous orb
			} else if (id.pokeNum == 493 && id.subNum != 0) {
				/* Arceus */
				item = ItemInfo.plateForType(id.subNum);
			}
		}
		
		if (fullReset) {
			gender = 1;
			nature = 0;
		}
		
		if (gen.num > 2) {
			ability = PokemonInfo.abilities(id, gen.num)[0];
		}		
		
		if (fullReset) {
			happiness = 0;
			level = 100;
			moves[0] = new TeamMove(0);
			moves[1] = new TeamMove(0);
			moves[2] = new TeamMove(0);
			moves[3] = new TeamMove(0);
			if (gen.num > 2) {
				DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
			} else {
				DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 15;
			}
			EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 0;
		}
	}
	
	public void serializeBytes(Baos bytes) {
		Baos b = new Baos();
//		hasGen, hasNickname, hasPokeball, hasHappiness, hasPPups, hasIVs,
//      isShiny=0
		b.putFlags(new boolean[]{false, nick.length() > 0, false, happiness != 0, false, true});
		
		b.putBaos(uID);
		b.write(level);
		b.write(shiny ? 1 : 0);
		
		if (nick.length() > 0) {
			b.putString(nick);
		}
		
		if (gen.num > 1) {
			b.putShort(item);
			
			if (gen.num > 2) {
				b.putShort(ability);
				b.write(nature);
			}
		
			b.write(gender);
		
			if (gen.num > 2 && happiness != 0) {
				b.write(happiness);
			}
		}
		
		for (int i = 0; i < 4; i++) {
			b.putShort(moves[i].num);
		}
		
		for (int i = 0; i < 6; i++) b.write(EVs[i]);
		for (int i = 0; i < 6; i++) b.write(DVs[i]);
		
		bytes.putVersionControl(0, b);
	}

	public int ability() {
		return ability;
	}

	public int item() {
		return item;
	}

	public int totalHP() {
		return PokemonInfo.calcStat(this, Stats.Hp.ordinal(), gen.num);
	}

	public int currentHP() {
		return totalHP();
	}

	public CharSequence nick() {
		return nick;
	}

	public UniqueID uID() {
		return uID;
	}

	public TeamMove move(int j) {
		return moves[j];
	}

	public int hiddenPowerType() {
		return HiddenPowerInfo.hiddenPowerType(this);
	}

	public int dv(int i) {
		return DVs[i];
	}

	public int ev(int i) {
		return EVs[i] >= 0 ? EVs[i] : (EVs[i] + 255);
	}

	public int level() {
		return level;
	}

	public void save(Document doc, Element poke) {
		poke.setAttribute("Num", String.valueOf(uID.pokeNum));
		poke.setAttribute("Forme", String.valueOf(uID.subNum));
		poke.setAttribute("NickName", nick);
		poke.setAttribute("Item", String.valueOf(item));
		poke.setAttribute("Ability", String.valueOf(ability));
		poke.setAttribute("Gender", String.valueOf(gender));
		poke.setAttribute("Lvl", String.valueOf(level));
		poke.setAttribute("Shiny", String.valueOf(shiny));
		poke.setAttribute("Nature", String.valueOf(nature));
		poke.setAttribute("Happiness", String.valueOf(happiness));
		
		for (int i = 0; i < 4; i++) {
			Element move = doc.createElement("Move");
			this.move(i).save(move);
			poke.appendChild(move);
		}
		
		for (int i = 0; i < 6; i++) {
			Element ev = doc.createElement("EV");
			ev.setTextContent(String.valueOf(this.ev(i)));
			poke.appendChild(ev);
		}
		
		for (int i = 0; i < 6; i++) {
			Element iv = doc.createElement("DV");
			iv.setTextContent(String.valueOf(this.dv(i)));
			poke.appendChild(iv);
		}
	}

	public int totalEVs() {
		return ev(0) + ev(1) + ev(2) + ev(3) + ev(4) + ev(5);
	}

	public void setItem(short s) {
		item = s;
		
		if (uID.pokeNum == 487) {
			/* Giratina */
			setNum(new UniqueID(uID.pokeNum, item == 213 ? 1 : 0));
		} else if (uID.pokeNum == 493) {
			/* Arceus */
			setNum(new UniqueID(uID.pokeNum, ItemInfo.plateType(item)));
		}
	}
}
