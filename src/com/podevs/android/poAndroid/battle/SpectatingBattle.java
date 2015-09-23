package com.podevs.android.poAndroid.battle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.SparseArray;
import com.podevs.android.poAndroid.ColorEnums.QtColor;
import com.podevs.android.poAndroid.ColorEnums.StatusColor;
import com.podevs.android.poAndroid.ColorEnums.TypeColor;
import com.podevs.android.poAndroid.ColorEnums.TypeForWeatherColor;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.battle.ChallengeEnums.Clauses;
import com.podevs.android.poAndroid.player.PlayerInfo;
import com.podevs.android.poAndroid.poke.PokeEnums.*;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.AbilityInfo;
import com.podevs.android.poAndroid.pokeinfo.ItemInfo;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo.Type;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.StringUtilities;

import java.util.Locale;
import java.util.Random;

public class SpectatingBattle {
	static protected final String TAG = "SpectatingBattle";

	public enum BattleResult {
		Forfeit,
		Win,
		Tie,
		Close
	}

	// 0 = you, 1 = opponent
	public PlayerInfo[] players = new PlayerInfo[2];

	public short[] remainingTime = new short[2];
	public boolean[] ticking = new boolean[2];
	public long[] startingTime = new long[2];

	public boolean gotEnd = false;

	protected int mode = 0, numberOfSlots = 0;
	public byte me = 0, opp = 1;
	public int bID = 0;
	protected static NetworkService netServ;

	public int background;

	public ShallowBattlePoke[][] pokes = new ShallowBattlePoke[2][6];
	// ArrayList<Boolean> pokeAlive = new ArrayList<Boolean>();

	public SpannableStringBuilder hist; //= new SpannableStringBuilder();
	public SpannableStringBuilder histDelta; //= new SpannableStringBuilder();

	public BattleDynamicInfo[] dynamicInfo = new BattleDynamicInfo[2];

	public void writeToHist(CharSequence text) {
		synchronized(histDelta) {
			histDelta.append(text);
		}
	}

	public BattleConf conf;
	public BattleActivity activity = null; //activity associated with the battle

	public boolean shouldShowPreview = false;

	public SpectatingBattle (BattleConf bc, PlayerInfo p1, PlayerInfo p2, int bID, NetworkService ns) {
		hist = new SpannableStringBuilder();
		histDelta = new SpannableStringBuilder();
		netServ = ns;
		conf = bc; // singles, doubles, triples
		this.bID = bID;

		// Only supporting singles for now
		numberOfSlots = 2;
		players[0] = p1;
		players[1] = p2;

		remainingTime[0] = remainingTime[1] = 5*60;
		ticking[0] = ticking[1] = false;

		background = new Random().nextInt(11) + 1;

		synchronized (histDelta) {
			writeToHist("Battle between " + players[me].nick() +
							" and " + players[opp].nick() + " started!");
		}

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 6; j++) {
				pokes[i][j] = new ShallowBattlePoke();
			}
		}
	}

	/**
	 * Dealloc for garbage collector
	 */
	public void destroy() {
		if (activity != null) {
			activity.end();
			activity = null;
		}
	}

	/*
	public Boolean isMyTimerTicking() {
		return ticking[me];
	}

	public Boolean isOppTimerTicking() {
		return ticking[opp];
	}
	public long myStartingTime() {
		return startingTime[me];
	}

	public short myTime() {
		return remainingTime[me];

	}

	public short oppTime() {
		return remainingTime[opp];
	}
	*/
	public ShallowBattlePoke currentPoke(int player) {
		return pokes[player][0];
	}

	public boolean isOut(Byte poke) {
		return poke < numberOfSlots / 2;
	}

	public int slot(int player, int poke) {
		return player + poke * 2;
	}

	@SuppressLint("DefaultLocale")
	public String tu (String toUpper) {
		// Makes the first letter of a string uppercase
		if (toUpper.length() <= 1)
			return toUpper;
		return toUpper.substring(0,1).toUpperCase(Locale.getDefault())+toUpper.substring(1);
	}

	public void receiveCommand(Bais msg)  {
		synchronized (this) {
            byte command = msg.readByte();

            if (command < 0 || command > BattleCommand.values().length) {
                Log.w("Spectating battle", "Battle command unknown " + String.valueOf((int) command));
                return;
            }

			BattleCommand bc = BattleCommand.values()[command];
			byte player = msg.readByte();
			
			/* Because we don't deal with double battles */
			player = (bc == BattleCommand.Clause ? player : (byte) (player % 2));

			dealWithCommand(bc, player, msg);
		}
	}

	public void dealWithCommand(BattleCommand bc, byte player, Bais msg) {
		switch(bc) {
		case SendOut: {
			boolean silent = msg.readBool();
			byte fromSpot = msg.readByte();

			ShallowBattlePoke tempPoke = pokes[player][0];
			tempPoke.sub = false;
			pokes[player][0] = pokes[player][fromSpot];
			pokes[player][fromSpot] = tempPoke;

			/*
			if(msg.available() > 0) { // this is the first time you've seen it
				pokes[player][0] = new ShallowBattlePoke(msg, (player == me) ? true : false, conf.gen);
			}*/ //No Clue how this works, but it doesn't to the intended effect - MM
				//So I replaced it with the function below
			if (pokes[player][0].level == 0 || silent) {
				pokes[player][0] = new ShallowBattlePoke(msg, (player == me), conf.gen);
				if (activity == null) {
					pokes[player][0].pokeName = PokemonInfo.name(pokes[player][0].uID);
				}
			}

			if (activity != null) {
				activity.samePokes[player] = false;
				activity.updatePokes(player);
				activity.updatePokeballs();
               // Runtime.getRuntime().gc();
			}

			SharedPreferences prefs = netServ.getSharedPreferences("battle", Context.MODE_PRIVATE);
			if (prefs.getBoolean("pokemon_cries", true)) {
				try {
					synchronized (this) {
						netServ.playCry(this, currentPoke(player));
						wait(10000);
					}
				} catch (InterruptedException e) { Log.e(TAG, "INTERRUPTED"); }
			}

			if(!silent)
				writeToHist("\n" + tu((players[player].nick() + " sent out " +
						currentPoke(player).rnick + "!")));
			break;
		} case SendBack: {
			boolean silent = msg.readBool();
			if (!silent) {
				writeToHist("\n" + tu((players[player].nick() + " called " +
						currentPoke(player).rnick + " back!")));
			}
			break;
		} case UseAttack: {
			short attack = msg.readShort();
			int color;
			try {
				color = MoveInfo.type(attack);
			} catch (NumberFormatException e) {
				color = Type.Curse.ordinal();
			}
			boolean silent = msg.readBool();
			if (!silent) {
			writeToHist(Html.fromHtml("<br>" + tu(currentPoke(player).nick +
					" used <font color =" + TypeColor.values()[color] + MoveInfo.name(attack) + "</font>!")));
			}
				if (player == opp) {
					activity.updateMoves(attack);
				}
				/*
			boolean special = msg.readBool();
			if (player == opp && ! special) {
				activity.updateMoves(attack);
			}
			*/
			break;
		} case BeginTurn: {
			int turn = msg.readInt();
			writeToHist(Html.fromHtml("<br><b><font color=" + QtColor.Blue +
					"Start of turn " + turn + "</font></b>"));
			break;
		} case Ko: {
			SharedPreferences prefs = netServ.getSharedPreferences("battle", Context.MODE_PRIVATE);
			if (prefs.getBoolean("pokemon_cries", true)) {
				try {
					synchronized (this) {
						netServ.playCry(this, currentPoke(player));
						wait(10000);
					}
				} catch (InterruptedException e) { Log.e(TAG, "INTERRUPTED"); }
			}

			writeToHist(Html.fromHtml("<br><b>" + tu(StringUtilities.escapeHtml((currentPoke(player).nick))) +
					" fainted!</b>"));
			break;
		} case Hit: {
			byte number = msg.readByte();
			writeToHist("\nHit " + number + " time" + ((number > 1) ? "s!" : "!"));
			break;
		} case Effective: {
			byte eff = msg.readByte();
			switch (eff) {
			case 0:
				writeToHist("\nIt had no effect!");
				break;
			case 1:
			case 2:
				writeToHist(Html.fromHtml("<br><font color=" + QtColor.Gray +
						"It's not very effective...</font>"));
				break;
			case 8:
			case 16:
				writeToHist(Html.fromHtml("<br><font color=" + QtColor.Blue +
						"It's super effective!</fontColor>"));
				break;
			}
			break;
		} case CriticalHit: {
			writeToHist(Html.fromHtml("<br><font color=#6b0000>A critical hit!</font>"));
			break;
		} case Miss: {
			writeToHist("\nThe attack of " + currentPoke(player).nick + " missed!");
			break;
		} case Avoid: {
			writeToHist("\n" + tu(currentPoke(player).nick + " avoided the attack!"));
			break;
		} case StatChange: {
			byte stat = msg.readByte(), boost=msg.readByte();
			boolean silent = msg.readBool();
			if (!silent) {
				writeToHist("\n" + tu(currentPoke(player).nick + "'s " +
						netServ.getString(Stat.values()[stat].rstring()) +
						(Math.abs(boost) > 1 ? " sharply" : "") + (boost > 0 ? " rose!" : " fell!")));
			}
			break;
		} case CappedStat: {
            byte stat = msg.readByte();
            boolean max = msg.readBool();

            writeToHist("\n" + tu(currentPoke(player).nick + "'s " +
                    netServ.getString(Stat.values()[stat].rstring()) +
                    (max ? " can't go any higher!" : " can't go any lower!")));
            break;
        } case UseItem: {
				byte item = msg.readByte();
				Log.w("SpectatingBattle", bc.name() + item);
		} case ItemCountChange: {
				byte item = msg.readByte();
				byte count = msg.readByte();
				Log.w("SpectatingBattle", bc.name() + item + ":" + count);
		} case StatusChange: {
			final String[] statusChangeMessages = {
					" is paralyzed! It may be unable to move!",
					" fell asleep!",
					" was frozen solid!",
					" was burned!",
					" was poisoned!",
					" was badly poisoned!",
			};
			byte status = msg.readByte();
			boolean multipleTurns = msg.readBool();
			boolean silent = msg.readBool();
			if (silent) {
				// Print nothing
			} else if (status > Status.Fine.poValue() && status <= Status.Poisoned.poValue()) {
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(status) + tu(
						currentPoke(player).nick + statusChangeMessages[status-1 +
                        (status == Status.Poisoned.poValue() && multipleTurns ? 1 : 0)] + "</font>")));
			} else if(status == Status.Confused.poValue()){
				/* The reason we need to handle confusion separately is because 
				 * poisoned and badly poisoned are not separate values in the Status
				 * enum, so confusion does not correspond to the same value in the above
				 * string array as its enum value. */
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(status) + tu(
						currentPoke(player).nick + " became confused!</font>")));
			}
			break;
		} case AbsStatusChange: {
			byte poke = msg.readByte();
			byte status = msg.readByte();

			if (poke < 0 || poke >= 6)
				break;

			if (status != Status.Confused.poValue()) {
				pokes[player][poke].changeStatus(status);
				if (activity != null) {
					if (isOut(poke))
						activity.updatePokes(player);
					activity.updatePokeballs();
				}
			}
			break;
		} case AlreadyStatusMessage: {
			byte status = msg.readByte();
			writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(status) + tu(
					currentPoke(player).nick + " is already " + Status.poValues()[status] +
					".</font>")));
			break;
		} case StatusMessage: {
			byte status = msg.readByte();
			switch (StatusFeeling.values()[status]) {
			case FeelConfusion:
				writeToHist(Html.fromHtml("<br><font color=" + TypeColor.Ghost + tu(
						currentPoke(player).nick + " is confused!</font>")));
				break;
			case HurtConfusion:
				writeToHist(Html.fromHtml("<br><font color=" + TypeColor.Ghost + tu(
						"It hurt itself in its confusion!</font>")));
				break;
			case FreeConfusion:
				writeToHist(Html.fromHtml("<br><font color=" + TypeColor.Dark + tu(
						currentPoke(player).nick + " snapped out of its confusion!</font>")));
				break;
			case PrevParalysed:
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Paralysed.poValue()) + tu(
						currentPoke(player).nick + " is paralyzed! It can't move!</font>")));
				break;
			case FeelAsleep:
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Asleep.poValue()) + tu(
						currentPoke(player).nick + " is fast asleep!</font>")));
				break;
			case FreeAsleep:
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Asleep.poValue()) + tu(
						currentPoke(player).nick + " woke up!</font>")));
				break;
			case HurtBurn:
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Burnt.poValue()) + tu(
						currentPoke(player).nick + " is hurt by its burn!</font>")));
				break;
			case HurtPoison:
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Poisoned.poValue()) + tu(
						currentPoke(player).nick + " is hurt by poison!</font>")));
				break;
			case PrevFrozen:
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Frozen.poValue()) + tu(
						currentPoke(player).nick + " is frozen solid!</font>")));
				break;
			case FreeFrozen:
				writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Frozen.poValue()) + tu(
						currentPoke(player).nick + " thawed out!</font>")));
				break;
			}
			break;
		} case Failed: {
			boolean silent = msg.readBool();
			if (!silent)
				writeToHist("\nBut it failed!");
			break;
		} case BattleChat:
		case EndMessage: {
			String message = msg.readString();
			if (message == null || message.equals(""))
				break;
			writeToHist(Html.fromHtml("<br><font color=" + (player !=0 ? "#5811b1>" : QtColor.Green) +
					"<b>" + StringUtilities.escapeHtml(players[player].nick()) + ": </b></font>" +
					StringUtilities.escapeHtml(message)));
			break;
		} case Spectating: {
			boolean come = msg.readBool();
			int id = msg.readInt();
			String name = msg.readString();

			if (come) {
				addSpectator(id, name);
			} else {
				removeSpectator(id);
			}

			break;
		} case SpectatorChat: {
			// TODO if (ignoreSpecs) break;
			int id = msg.readInt();
			String message = msg.readString();
			writeToHist(Html.fromHtml("<br><font color=" + QtColor.Blue + spectators.get(id) +
					":</font> " + StringUtilities.escapeHtml(message)));
			break;
		} case MoveMessage: {
			short move = msg.readShort();
			byte part = msg.readByte();
			byte type = msg.readByte();
			byte foe = msg.readByte();
			short other = msg.readShort();
			String q = msg.readString();

			String s = MoveInfo.message(move, part);
			s = s.replaceAll("%s", currentPoke(player).nick);
			s = s.replaceAll("%ts", players[player].nick());
			s = s.replaceAll("%tf", players[(player == 0 ? 1 : 0)].nick());
			if(type  != -1) s = s.replaceAll("%t", Type.values()[type].toString());
			if(foe   != -1) s = s.replaceAll("%f", currentPoke(foe).nick);
			if(other  != -1 && s.contains("%m")) s = s.replaceAll("%m", MoveInfo.name(other));
			s = s.replaceAll("%d", new Short(other).toString());
			s = s.replaceAll("%q", q);
			if(other != -1 && s.contains("%i")) s = s.replaceAll("%i", ItemInfo.name(other));
			if(other != -1 && s.contains("%a")) s = s.replaceAll("%a", AbilityInfo.name(other));
			if(other != -1 && s.contains("%p")) s = s.replaceAll("%p", PokemonInfo.name(new UniqueID(other, 0)));
			if(s.contains("%st") || s.contains("%f") || s.contains("%i") || s.contains("%m") || s.contains("%p")) Log.e(TAG, "Parsing Issue {move:" + move + " part:" + part + " type:" + type + " foe:" + foe + " other:" + other + " string:" + s + "}");
			writeToHist(Html.fromHtml("<br><font color =" + TypeColor.values()[type] + tu(StringUtilities.escapeHtml(s)) + "</font>"));
			break;
		} case NoOpponent: {
			writeToHist("\nBut there was no target...");
			break;
		} case ItemMessage: {
			short item = msg.readShort();
			byte part = msg.readByte();
			byte foe = msg.readByte();
			short berry = msg.readShort();
			int other = msg.readInt();
			String s = ItemInfo.message(item, part);
            if(other != -1 && s.contains("%st")) s = s.replaceAll("%st", Stat.values()[other].toString());
            s = s.replaceAll("%s", currentPoke(player).nick);
            if(foe   != -1) s = s.replaceAll("%f", currentPoke(foe).nick);
            if(berry != -1) s = s.replaceAll("%i", ItemInfo.name(berry));
            if(other != -1 && s.contains("%m")) s = s.replaceAll("%m", MoveInfo.name(other));
            if(other != -1 && s.contains("%p")) s = s.replaceAll("%p", PokemonInfo.name(new UniqueID(other)));
			if(s.contains("%st") || s.contains("%f") || s.contains("%i") || s.contains("%m") || s.contains("%p")) Log.e(TAG, "Parsing Issue {" + "item:" + item + " part:" + part + " foe:" + foe + " berry:" + berry + " other:" + other + " string:" + s + "}" );
            /* Balloon gets a really special treatment */
            if (item == 35)
                writeToHist(Html.fromHtml("<br><b>" + tu(StringUtilities.escapeHtml(s)) + "</b>"));
            else
                writeToHist("\n" + tu(s));
			break;
		} case Flinch: {
			writeToHist("\n" + tu(currentPoke(player).nick + " flinched!"));
			break;
		} case Recoil: {
			boolean damaging = msg.readBool();
			if (damaging)
				writeToHist("\n" + tu(currentPoke(player).nick + " is hit with recoil!"));
			else
				writeToHist("\n" + tu(currentPoke(player).nick + " had its energy drained!"));
			break;
		} case WeatherMessage: {
			byte wstatus = msg.readByte(), weather = msg.readByte();
			if (weather == Weather.NormalWeather.ordinal())
				break;
			String message;
			String color = new TypeForWeatherColor(weather).toString();
			switch (WeatherState.values()[wstatus]) {
			case EndWeather:
				switch (Weather.values()[weather]) {
				case Hail: message = "The hail subsided!"; break;
				case SandStorm: message = "The sandstorm subsided!"; break;
				case Sunny: message = "The sunlight faded!"; break;
				case Rain: message = "The rain stopped!"; break;
                case HeavySun: message = "The intense sunlight faded!"; break;
                case HeavyRain: message = "The heavy downpour stopped!"; break;
				case Delta: message = "The mysterious air current has dissipated!"; break;
				default: message = "";
				}
				writeToHist(Html.fromHtml("<br><font color=" + color + message + "</font>"));
				break;
			case HurtWeather:
				switch (Weather.values()[weather]) {
				case Hail: message = " is buffeted by the hail!"; break;
				case SandStorm: message = " is buffeted by the sandstorm!"; break;
				default: message = "";
				}
				writeToHist(Html.fromHtml("<br><font color=" + color + tu(
						currentPoke(player).nick + message + "</font>")));
				break;
			case ContinueWeather:
				switch (Weather.values()[weather]) {
				case Hail: message = "Hail continues to fall!"; break;
				case SandStorm: message = "The sandstorm rages!"; break;
				case Sunny: message = "The sunlight is strong!"; break;
				case Rain: message = "Rain continues to fall!"; break;
                case HeavySun: message = "The intense sunlight continues to shine!"; break;
                case HeavyRain: message = "The heavy downpour continues!"; break;
					case Delta: message = "A mysterious air current is protecting Flying-type Pokémon."; break;
				default: message = "";
				}
				writeToHist(Html.fromHtml("<br><font color=" + color + message + "</font>"));
				break;
			}
			break;
		} case StraightDamage: {
			short damage = msg.readShort();
			writeToHist("\n" + tu(currentPoke(player).nick + " lost " + damage + "% of its health!"));
			break;
		} case AbilityMessage: {
			short ab = msg.readShort();
			byte part = msg.readByte();
			byte type = msg.readByte();
			byte foe = msg.readByte();
			short other = msg.readShort();

			String s = AbilityInfo.message(ab, part);
	        if(other != -1 && s.contains("%st")) s = s.replaceAll("%st", netServ.getResources().getString((Stat.values()[other].rstring())));
	        s = s.replaceAll("%s", currentPoke(player).nick);
	        s = s.replaceAll("%tf", players[(player == 0 ? 1 : 0)].nick());
	        // Below commented out in PO code
	        //            mess.replace("%ts", name(spot));
	        //            mess.replace("%tf", name(!spot));
	        if(type  != -1) s = s.replaceAll("%t", Type.values()[type].toString());
	        if(foe   != -1) s = s.replaceAll("%f", currentPoke(foe).nick);
	        if(other != -1 && s.contains("%m")) s = s.replaceAll("%m", MoveInfo.name(other));
	        // Below commented out in PO code
	        //            mess.replace("%d", QString: {:number(other));
	        if(other != -1 && s.contains("%i")) s = s.replaceAll("%i", ItemInfo.name(other));
	        if(other != -1 && s.contains("%a")) s = s.replaceAll("%a", AbilityInfo.name(other));
	        if(other != -1 && s.contains("%p")) s = s.replaceAll("%p", PokemonInfo.name(new UniqueID(other, 0)));
	        if (type == Type.Normal.ordinal()) {
	        	writeToHist("\n" + tu(StringUtilities.escapeHtml(s)));
	        } else {
	        	writeToHist(Html.fromHtml("<br><font color =" + TypeColor.values()[type] + tu(StringUtilities.escapeHtml(s)) + "</font>"));
	        }
			break;
		} case Substitute: {
			currentPoke(player).sub = msg.readBool();

			if (activity != null) {
				activity.samePokes[player] = false;
				activity.updatePokes(player);
			}
			break;
		} case BattleEnd: {
			byte res = msg.readByte();
			if (res == BattleResult.Tie.ordinal())
				writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
						"Tie between " + players[0].nick() + " and " + players[1].nick() +
						"!</b></font>")); // XXX Personally I don't think this deserves !
			else
				writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
						players[player].nick() +" won the battle!</b></font>"));
			gotEnd = true;
			break;
		} case BlankMessage: {
			// XXX This prints out a lot of extra space
			// writeToHist("\n");
			break;
		} case PointEstimate : {
			byte first = msg.readByte();
			byte second = msg.readByte();

			writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue + "Variation:</b></font> " +
					first + ", " + second));
			break;
		}
		case Clause: {
			if (player >= 0 && player < Clauses.values().length) {
				writeToHist("\n" + Clauses.values()[player].battleText());
			} else {
				Log.e(TAG, "Invalid Clause:" + player);
			}
			break;
		} case Rated: {
			boolean rated = msg.readBool();
			writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue + "Rule:</b></font> " +
					(rated ? "Rated" : "Unrated")));
            for (int i = 0; i < Clauses.values().length; i++) {
                if ((conf.clauses & (1 << i)) > 0 ? true : false) {
                    writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue + "Rule: </b></font>" +
                    		Clauses.values()[i]));
                }
            }
			break;
		} case TierSection: {
			String tier = msg.readString();
			writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
					"Tier: " + tier + "</b></font>"));
			break;
		} case TempPokeChange: {
			byte id = msg.readByte();

			switch(TempPokeChange.values()[id]) {
			case TempSprite:
				UniqueID sprite = new UniqueID(msg);
				if (sprite.pokeNum != 0)
					currentPoke(player).specialSprites.addFirst(sprite);
				else
					currentPoke(player).specialSprites.removeFirst();
				if (activity !=null) {
					activity.samePokes[player] = false;
					activity.updatePokes(player);
				}
				break;
			case DefiniteForme: {
				byte poke = msg.readByte();
				UniqueID newForm = new UniqueID(msg);
				pokes[player][poke].uID = newForm;
				if (isOut(poke)) {
					currentPoke(slot(player, poke)).uID = newForm;
					if (activity !=null) {
						activity.samePokes[player] = false;
						activity.updatePokes(player);
					}
				}
				break;
			} case AestheticForme:
				short newForm = msg.readShort();
				currentPoke(player).uID.subNum = (byte) newForm;
				if (activity !=null) {
					activity.samePokes[player] = false;
					activity.updatePokes(player);
				}
			default: break;
			}
			break;
		} case ClockStart: {
			remainingTime[player % 2] = msg.readShort();
			startingTime[player % 2] = SystemClock.uptimeMillis();
			ticking[player % 2] = true;
			break;
		} case ClockStop: {
			remainingTime[player % 2] = msg.readShort();
			ticking[player % 2] = false;
			break;
		} case ChangeHp: {
			short newHP = msg.readShort();

			currentPoke(player).lastKnownPercent = currentPoke(player).lifePercent;
			currentPoke(player).lifePercent = (byte)newHP;

			if(activity != null) {
				// Block until the hp animation has finished
				// Timeout after 10s
				try {
					synchronized (this) {
						activity.animateHpBarTo(player, currentPoke(player).lifePercent);
						wait(10000);
					}
				} catch (InterruptedException e) {}
			}
			break;
		} case SpotShifts: {
			// TODO
			break;
		} case DynamicInfo: {
			dynamicInfo[player] = new BattleDynamicInfo(msg);
			break;
		} default: {
			System.out.println("Battle command unimplemented -- " + bc);
			break;
		}
		}
	}

	private SparseArray<String> spectators = new SparseArray<String>();

	private void addSpectator(int id, String name) {
		spectators.put(id, name);
		writeToHist(Html.fromHtml("<br/><font color="+QtColor.DarkGreen+ name + " is watching the battle</font>"));
	}

	private void removeSpectator(int id) {
		writeToHist(Html.fromHtml("<br/><font color=" + QtColor.DarkGreen+ spectators.get(id) + " left the battle</font>"));
		spectators.remove(id);
	}

	protected enum TempPokeChange {
		TempMove,  //1
		TempAbility,  //2
		TempItem, //3
		TempSprite, //4
		DefiniteForme, //5
		AestheticForme, //6
		DefMove, //7
		TempPP //8
	}
}
