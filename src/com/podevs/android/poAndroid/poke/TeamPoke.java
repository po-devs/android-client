package com.podevs.android.poAndroid.poke;

import android.os.Parcel;
import android.os.Parcelable;
import com.podevs.android.poAndroid.pokeinfo.HiddenPowerInfo;
import com.podevs.android.poAndroid.pokeinfo.ItemInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.StatsInfo.Stats;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo;
import com.podevs.android.utilities.ArrayUtilities;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// This class is how a poke is represented in the teambuilder.
public class TeamPoke implements SerializeBytes, Poke, Parcelable {
	public UniqueID uID;
	public String nick;
	public short item;
	public short pokeball;
	public short ability;
	public byte nature;
	public byte hiddenPowerType = (byte)TypeInfo.Type.Dark.ordinal();
	public byte gender;
	public Gen gen;
	public boolean shiny;
	public byte happiness;
	public byte level;
	public TeamMove[] moves = new TeamMove[4];
	public byte[] DVs = new byte[6];
	public byte[] EVs = new byte[6];
    public boolean isHackmon = false;

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

		boolean hasGen = network.readBool();
		boolean hasNickname = network.readBool();
		boolean hasPokeball = network.readBool();
		boolean hasHappiness = network.readBool();
		boolean hasPPups = network.readBool();
		boolean hasIVs = network.readBool();
		boolean hasHiddenPower = network.readBool();

		if (hasGen) {
			gen = new Gen(b);
		} else if (gen == null) {
			gen = new Gen();
		}
		uID = new UniqueID(b);
		level = b.readByte();

		Bais data = b.readFlags();
		shiny = data.readBool();
        isHackmon = data.readBool();

		if (hasNickname) {
			nick = b.readString();
		} else {
			nick = PokemonInfo.name(uID);
		}

		if (hasPokeball) {
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
			if (gen.num > 2 && hasHappiness) {
				happiness = b.readByte();
			}
			if (gen.num > 6 && hasHiddenPower) {
				hiddenPowerType = b.readByte();
			}
		}

		for (int i = 0; i < 4; i++) {
			if (hasPPups) {
				b.readByte(); // read the pp up for the move, but ignore it
			}
			moves[i] = new TeamMove(b.readShort());
		}

		for(int i = 0; i < 6; i++)
			EVs[i] = b.readByte();

		if (hasIVs || gen.num == 2) {
			for(int i = 0; i < 6; i++)
				DVs[i] = b.readByte();
		} else {
			if (gen.num > 2) {
				DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
			} else {
				DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 15;
			}
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
		hiddenPowerType = (byte)TypeInfo.Type.Dark.ordinal();
		gender = 1;
		gen = new Gen();
		shiny = false;
		happiness = 0;
        isHackmon = false;
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
		EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 0;
	}

	public void setNum(UniqueID id) {
		if (uID.equals(id)) {
			return;
		}

		try {
			boolean fullReset = uID.pokeNum != id.pokeNum;
			uID = id;

			if (gen.num > 2) {
				ability = PokemonInfo.abilities(id, gen.num)[0];
			}
			if (fullReset) {
				nick = PokemonInfo.name(id);
				shiny = false;
				gender = 1;
				nature = 0;
				happiness = 0;
				hiddenPowerType = (byte)TypeInfo.Type.Dark.ordinal();
				level = 100;
				moves[0] = new TeamMove(0);
				moves[1] = new TeamMove(0);
				moves[2] = new TeamMove(0);
				moves[3] = new TeamMove(0);
				item = 15; //leftovers
				if (gen.num > 2) {
					DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 31;
					EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = 0;
				} else {
					DVs[0] = DVs[1] = DVs[2] = DVs[3] = DVs[4] = DVs[5] = 15;
					EVs[0] = EVs[1] = EVs[2] = EVs[3] = EVs[4] = EVs[5] = (byte) 252;

				}
			}
			if (!fullReset && id.subNum != 0) {
				if (id.pokeNum == 487) { /* Giratina-O */
					item = 213; // griseous orb
				} else if (id.pokeNum == 493) { /* Arceus */
					item = ItemInfo.plateForType(id.subNum);
				} else if (id.pokeNum == 773) { /* Silvally */
					item = ItemInfo.memoryChipForType(id.subNum);
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

			short[] moves = PokemonInfo.moves(uID, gen.num, gen.subNum);

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
//								hasGen, hasNickname, hasPokeball, hasHappiness,	hasPPups, hasIVs, hasHiddenPower
		b.putFlags(new boolean[]{true, nick.length() > 0, false, happiness != 0,false,	true,	hiddenPowerType != (byte)TypeInfo.Type.Dark.ordinal()});

		b.putBaos(gen);
		b.putBaos(uID);
		b.write(level);
		//b.write(shiny ? 1 : 0);

        b.putFlags(new boolean[]{shiny, isHackmon});

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

			if (gen.num > 6 && hiddenPowerType() != (byte)TypeInfo.Type.Dark.ordinal()) {
				b.write(hiddenPowerType);
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
		if (gen.num > 6) {
			return  hiddenPowerType;
		}
		return HiddenPowerInfo.Type(this);
	}

	public boolean validHiddenPowerType(int type) {
		if (this.gen.num > 6) {
			int minPossible = 0;
			int maxPossible = 0;
			for (int i = 0; i < 6; i++) {
				//Speed comes before sp.atk and sp.def
				int b = i == 5 ? 3 : (i > 2 ? i+1 : i);

				minPossible += DVs[i] == 31 ? 0 : (DVs[i] % 2) << b;
				maxPossible += DVs[i] == 31 ? 1 << b : (DVs[i] % 2) << b;
			}
			minPossible = (minPossible*15)/63 + 1;
			maxPossible = (maxPossible*15)/63 + 1;
			if (maxPossible < type || type < minPossible) {
				return false;
			}
		}
		return true;
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
		poke.setAttribute("HiddenPower", String.valueOf(hiddenPowerType));
		poke.setAttribute("Happiness", String.valueOf(happiness));
        poke.setAttribute("Hackmon", String.valueOf(isHackmon ? 1 : 0));

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

		if (uID.pokeNum == 487 && !isHackmon) {
			/* Giratina */
			setNum(new UniqueID(uID.pokeNum, item == 213 ? 1 : 0));
		} else if (uID.pokeNum == 493 && !isHackmon) {
			/* Arceus */
			setNum(new UniqueID(uID.pokeNum, ItemInfo.plateType(item)));
		} else if (uID.pokeNum == 773 && !isHackmon) {
			/* Silvally */
			setNum(new UniqueID(uID.pokeNum, ItemInfo.memoryType(item)));
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

	public int gender() {
		return gender;
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uID.pokeNum);dest.writeByte(uID.subNum);
        dest.writeString(nick);
        dest.writeValue(item);
        dest.writeValue(pokeball);
        dest.writeValue(ability);
        dest.writeByte(nature);
        dest.writeByte(hiddenPowerType);
        dest.writeByte(gender);
        dest.writeByte(gen.num);dest.writeByte(gen.subNum);
        dest.writeByte((byte) (shiny ? 0x01 : 0x00));
        dest.writeByte(happiness);
        dest.writeByte(level);
        for (int i = 0; i < 4; i++) {
            dest.writeInt(moves[i].num());
        }
        for (int i = 0; i < 6; i++) {
            dest.writeByte(DVs[i]);
        }
        for (int i = 0; i < 6; i++) {
            dest.writeByte(EVs[i]);
        }
        dest.writeByte((byte) (isHackmon ? 0x01 : 0x00));
    }

    protected TeamPoke(Parcel in) {
        uID = new UniqueID(in.readInt(), in.readByte());
        nick = in.readString();
        item = (Short) in.readValue(short.class.getClassLoader());
        pokeball = (Short) in.readValue(short.class.getClassLoader());
        ability = (Short) in.readValue(short.class.getClassLoader());
        nature = in.readByte();
        hiddenPowerType = in.readByte();
        gender = in.readByte();
        gen = new Gen(in.readByte(), in.readByte());
        shiny = in.readByte() != 0x00;
        happiness = in.readByte();
        level = in.readByte();
        for (int i = 0; i < 4; i++) {
            moves[i] = new TeamMove(in.readInt());
        }
        for (int i = 0; i < 6; i++) {
            DVs[i] = in.readByte();
        }
        for (int i = 0; i < 6; i++) {
            EVs[i] = in.readByte();
        }
        isHackmon = in.readByte() != 0x00;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public TeamPoke createFromParcel(Parcel source) {
            return new TeamPoke(source);
        }

        @Override
        public TeamPoke[] newArray(int size) {
            return new TeamPoke[size];
        }
    };
}
