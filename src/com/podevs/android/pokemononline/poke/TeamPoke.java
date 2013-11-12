package com.podevs.android.pokemononline.poke;

import com.podevs.android.pokemononline.pokeinfo.HiddenPowerInfo;
import com.podevs.android.pokemononline.pokeinfo.ItemInfo;
import com.podevs.android.pokemononline.pokeinfo.PokemonInfo;
import com.podevs.android.pokemononline.pokeinfo.StatsInfo.Stats;
import com.podevs.android.utilities.ArrayUtilities;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
			nick = PokemonInfo.name(uID);
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
		reset();
	}

	public void reset() {
		uID = new UniqueID();
		nick = PokemonInfo.name(uID);
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

		try {
			boolean fullReset = uID.pokeNum != id.pokeNum;

			uID = id;
			if (fullReset) {
				nick = PokemonInfo.name(id);
				shiny = false;
				item = 15; //leftovers
			}

			/* Giratina-O */
			if (id.pokeNum == 487) {
				item =  (short) (id.subNum == 1 ? 213 : 15); // griseous orb
			} else if (id.pokeNum == 493) { /* Arceus */
				item =  id.subNum != 0 ? ItemInfo.plateForType(id.subNum) : 15;
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
				if (gen.num > 2) {
					EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 0;
				} else {
					EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = (byte) 252;
				}
			}
		}   catch (Exception e) {
			reset();
		}
	}

	public void runCheck() {
		if (!PokemonInfo.exists(uID(), gen())) {
			uID = new UniqueID();
			reset();

			if (gen.num <= 2) {
				for (int i = 0; i < 6; i++) {
					if (DVs[i] > 15) {
						DVs[i] = 15;
					}
				}
			} else {
				for (int i = 0; i < 6; i++) {
					if (DVs[i] == 15) {
						DVs[i] = 31;
					}
				}
			}

			short[] moves = PokemonInfo.moves(uID, gen.num);

			for (int i = 0; i < 4; i++) {
				if (ArrayUtilities.indexOf(move(i).num, moves) == -1) {
				    this.moves[i] = new TeamMove(0);
				}
			}
		}
	}

	public void setGen(Gen gen) {
		if (this.gen.equals(gen)) {
			return;
		}
		this.gen = gen;

		runCheck();
	}

	public void serializeBytes(Baos bytes) {
		Baos b = new Baos();
//		hasGen, hasNickname, hasPokeball, hasHappiness, hasPPups, hasIVs,
//      isShiny=0
		b.putFlags(new boolean[]{true, nick.length() > 0, false, happiness != 0, false, true});

		b.putBaos(gen);
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

	public int stat(int i) {
		return PokemonInfo.calcStat(this, i, gen.num);
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
		return HiddenPowerInfo.Type(this);
	}

	public int dv(int i) {
		return DVs[i];
	}

	public int ev(int i) {
		return (EVs[i]+256)%256;
	}

	public int level() {
		return level;
	}

	public void save(Document doc, Element poke) {
		if (nick.length() == 0) {
			nick = PokemonInfo.name(uID);
		}
		poke.setAttribute("Num", String.valueOf(uID.pokeNum));
		poke.setAttribute("Forme", String.valueOf(uID.subNum));
		poke.setAttribute("Nickname", nick);
		poke.setAttribute("Item", String.valueOf(item));
		poke.setAttribute("Ability", String.valueOf(ability));
		poke.setAttribute("Gender", String.valueOf(gender));
		poke.setAttribute("Lvl", String.valueOf(level));
		poke.setAttribute("Shiny", String.valueOf(shiny ? 1 : 0));
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

	public boolean hasMove(int move) {
		return move(0).num == move || move(1).num == move || move(2).num == move || move(3).num == move;
	}

	public boolean addMove(int move) {
		if (!hasMove(0)) {
			return false;
		}

		for (int i = 0; i < 4; i++) {
			if (move(i).num == 0) {
				moves[i] = new TeamMove(move);
				return true;
			}
		}

		//never reached
		return false;
	}

	public boolean removeMove(int move) {
		if (!hasMove(move)) {
			return false;
		}

		for (int i = 0; i < 4; i++) {
			if (move(i).num == move) {
				moves[i] = new TeamMove(0);
				return true;
			}
		}

		//never reached
		return false;
	}

	public int nature() {
		return nature;
	}

	public Gen gen() {
		return gen;
	}
}
