package com.pokebros.android.pokemononline.battle;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;

import android.database.sqlite.SQLiteException;
import android.os.SystemClock;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.pokebros.android.pokemononline.Bais;
import com.pokebros.android.pokemononline.Baos;
import com.pokebros.android.pokemononline.NetworkService;
import com.pokebros.android.pokemononline.ColorEnums.*;
import com.pokebros.android.pokemononline.battle.ChallengeEnums.*;
import com.pokebros.android.pokemononline.player.PlayerInfo;
import com.pokebros.android.pokemononline.poke.BattlePoke;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;
import com.pokebros.android.pokemononline.poke.UniqueID;
import com.pokebros.android.pokemononline.poke.PokeEnums.*;

public class Battle {
	static final String TAG = "Battle";

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
	
	int mode = 0, numberOfSlots = 0;
	public byte me = 0, opp = 1;
	public int bID = 0;
	private static NetworkService netServ;
	public BattleTeam myTeam;
	public ShallowShownTeam oppTeam;
	public boolean gotEnd = false;
	public boolean allowSwitch, allowAttack, clicked = false;
	public boolean[] allowAttacks = new boolean[4];
	public int background;
	public boolean shouldShowPreview = false, shouldStruggle = false;
	public BattleMove[] displayedMoves = new BattleMove[4];
	public BattleConf conf;
	
	public ShallowBattlePoke[][] pokes = new ShallowBattlePoke[2][6];
	ArrayList<Boolean> pokeAlive = new ArrayList<Boolean>();
	
	public SpannableStringBuilder hist; //= new SpannableStringBuilder();
	public SpannableStringBuilder histDelta; //= new SpannableStringBuilder();
	
	public BattleDynamicInfo[] dynamicInfo = new BattleDynamicInfo[2];
	
	public void writeToHist(CharSequence text) {
		synchronized(histDelta) {
			histDelta.append(text);
		}
	}
	
	public Battle(BattleConf bc, Bais msg, PlayerInfo p1, PlayerInfo p2, int meID, int bID, NetworkService ns) {
		hist = new SpannableStringBuilder();
		histDelta = new SpannableStringBuilder();
		netServ = ns;
		conf = bc; // singles, doubles, triples
		this.bID = bID;
		myTeam = new BattleTeam(msg, netServ.db, conf.gen);
		
		// Only supporting singles for now
		numberOfSlots = 2;
		players[0] = p1;
		players[1] = p2;
		// Figure out who's who
		if(players[0].id == meID) {
			me = 0;
			opp = 1;
		}
		else {
			me = 1;
			opp = 0;
		}
		
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

		for (int i = 0; i < 4; i++)
			displayedMoves[i] = new BattleMove();
	}
	
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
	
	public ShallowBattlePoke currentPoke(int player) {
		return pokes[player][0];
	}
	
	public boolean isOut(Byte poke) {
		return poke < numberOfSlots / 2;
	}
	
	public int slot(int player, int poke) {
		return player + poke * 2;
	}
	
	public Baos constructCancel() {
		Baos b = new Baos();
		b.putInt(bID);
		b.putBaos(new BattleChoice(me, ChoiceType.CancelType));
		return b;
	}
	
	public Baos constructAttack(byte attack) {
		Baos b = new Baos();
		b.putInt(bID);
		AttackChoice ac = new AttackChoice(attack, opp);
		b.putBaos(new BattleChoice(me, ac, ChoiceType.AttackType));
		return b;
	}
	
	public Baos constructSwitch(byte toSpot) {
		Baos b = new Baos();
		b.putInt(bID);
		SwitchChoice sc = new SwitchChoice(toSpot);
		b.putBaos(new BattleChoice(me, sc, ChoiceType.SwitchType));
		return b;
	}
	
	public Baos constructRearrange() {
		Baos b = new Baos();
		b.putInt(bID);
		RearrangeChoice rc = new RearrangeChoice(myTeam);
		b.putBaos(new BattleChoice(me, rc, ChoiceType.RearrangeType));
		return b;
	}
	
	public String tu (String toUpper) {
		// Makes the first letter of a string uppercase
		if (toUpper.length() <= 1)
			return toUpper;
		return toUpper.substring(0,1).toUpperCase()+toUpper.substring(1);
	}
	
	public void receiveCommand(Bais msg)  {
		synchronized (this) {
		BattleCommand bc = BattleCommand.values()[msg.readByte()];
		byte player = msg.readByte();
		System.out.println("Battle Command Received: " + bc.toString());
		switch(bc) {
		case SendOut: {
			boolean silent = msg.readBool();
			byte fromSpot = msg.readByte();
			
			if(player == me) {
				BattlePoke temp = myTeam.pokes[0];
				
				myTeam.pokes[0] = myTeam.pokes[fromSpot];
				myTeam.pokes[fromSpot] = temp;
				
				for (int i=0; i < 4; i++) {
					displayedMoves[i] = new BattleMove(myTeam.pokes[0].moves[i]);
				}
			}
			
			ShallowBattlePoke tempPoke = pokes[player][0];
			pokes[player][0] = pokes[player][fromSpot];
			pokes[player][fromSpot] = tempPoke;
			
			if(msg.available() > 0) // this is the first time you've seen it
				pokes[player][0] = new ShallowBattlePoke(msg, (player == me) ? true : false, netServ.db, conf.gen);
			
			if(netServ.battleActivity != null) {
				netServ.battleActivity.updatePokes(player);
				netServ.battleActivity.updatePokeballs();
			}

			boolean playCries = true; // XXX
			if (playCries) {
				try {
					synchronized (this) {
						netServ.playCry(currentPoke(player));
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
			Integer color;
			try {
				color = new Integer(netServ.db.query("SELECT type FROM [Moves] WHERE _id = " + attack));
			} catch (NumberFormatException e) {
				color = Type.Curse.ordinal();
			}
			boolean silent = msg.readBool();
			if (!silent) {
			writeToHist(Html.fromHtml("<br>" + tu(currentPoke(player).nick +
					" used <font color =" + TypeColor.values()[color] +
					netServ.db.query("SELECT name FROM [Moves] WHERE _id = " + attack) + "</font>!")));
			}
			break;
		} case BeginTurn: {
			int turn = msg.readInt();
			writeToHist(Html.fromHtml("<br><b><font color=" + QtColor.Blue + 
					"Start of turn " + turn + "</font></b>"));
			break;
		} case Ko: {
			boolean playCries = true; // XXX
			if (playCries) {
				try {
					synchronized (this) {
						netServ.playCry(currentPoke(player));
						wait(10000);
					}
				} catch (InterruptedException e) { Log.e(TAG, "INTERRUPTED"); }
			}

			writeToHist(Html.fromHtml("<br><b>" + tu(NetworkService.escapeHtml((currentPoke(player).nick))) +
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
				if (player == me)
					myTeam.pokes[poke].changeStatus(status);
				if (netServ.battleActivity != null) {
					if (isOut(poke))
						netServ.battleActivity.updatePokes(player);
					netServ.battleActivity.updatePokeballs();
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
		} case BattleChat: {
		} case EndMessage: {
			String message = msg.readQString();
			if (message.equals(""))
				break;
			writeToHist(Html.fromHtml("<br><font color=" + (player !=0 ? "#5811b1>" : QtColor.Green) +
					"<b>" + NetworkService.escapeHtml(players[player].nick()) + ": </b></font>" +
					NetworkService.escapeHtml(message)));
			break;
		} case Spectating: {
			boolean come = msg.readBool();
			int id = msg.readInt();
			String name = msg.readQString();
			// TODO addSpectator(come, id);
			break;
		} case SpectatorChat: {
			// TODO if (ignoreSpecs) break;
			int id = msg.readInt();
			String message = msg.readQString();
			writeToHist(Html.fromHtml("<br><font color=" + QtColor.Blue + netServ.players.get(id) + 
					": " + NetworkService.escapeHtml(message)));
			break;
		} case MoveMessage: {
			short move = msg.readShort();
			byte part = msg.readByte();
			byte type = msg.readByte();
			byte foe = msg.readByte();
			short other = msg.readShort();
			String q = msg.readQString();
			
			String s = netServ.db.query("SELECT EFFECT" + part + " FROM [Move_message] WHERE _id = " + move);
			s = s.replaceAll("%s", currentPoke(player).nick);
			s = s.replaceAll("%ts", players[player].nick());
			s = s.replaceAll("%tf", players[(player == 0 ? 1 : 0)].nick());
			if(type  != -1) s = s.replaceAll("%t", Type.values()[type].toString());
			if(foe   != -1) s = s.replaceAll("%f", currentPoke(foe).nick);
			if(other  != -1 && s.contains("%m")) s = s.replaceAll("%m", netServ.db.query("SELECT name FROM [Moves] WHERE _id = " + other));
			s = s.replaceAll("%d", new Short(other).toString());
			s = s.replaceAll("%q", q);
			if(other != -1 && s.contains("%i")) s = s.replaceAll("%i", itemName(other));
			if(other != -1 && s.contains("%a")) s = s.replaceAll("%a", netServ.db.query("SELECT name FROM [Abilities] WHERE _id = " + (other + 1)));
			if(other != -1 && s.contains("%p")) s = s.replaceAll("%p", netServ.db.query("SELECT name FROM [Pokemons] WHERE Num = " + other));
			
			writeToHist(Html.fromHtml("<br><font color =" + TypeColor.values()[type] + tu(NetworkService.escapeHtml(s)) + "</font>"));
			break;
		} case NoOpponent: {
			writeToHist("\nBut there was no target...");
			break;
		} case ItemMessage: {
			short item = msg.readShort();
			byte part = msg.readByte();
			byte foe = msg.readByte();
			short berry = msg.readShort();
			short other = msg.readShort();
			String s = itemMessage(item, part);
            if(other != -1 && s.contains("%st")) s = s.replaceAll("%st", Stat.values()[other].toString());
            s = s.replaceAll("%s", currentPoke(player).nick);
            if(foe   != -1) s = s.replaceAll("%f", currentPoke(foe).nick);
            if(berry != -1) s = s.replaceAll("%i", itemName(berry));
            if(other != -1 && s.contains("%m")) s = s.replaceAll("%m", netServ.db.query("SELECT name FROM [Moves] WHERE _id = " + other));
            /* Balloon gets a really special treatment */
            if (item == 35)
                writeToHist(Html.fromHtml("<br><b>" + tu(NetworkService.escapeHtml(s)) + "</b>"));
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
				default: message = "";
				}
				writeToHist(Html.fromHtml("<br><font color=" + color + message + "</font>"));
				break;
			}
			break;
		} case StraightDamage: {
			short damage = msg.readShort();
			if(player == me) {
				writeToHist("\n" + tu(currentPoke(player).nick + " lost " + damage + 
						" HP! (" + (damage * 100 / myTeam.pokes[0].totalHP) + "% of its health)"));
			}
			else {
				writeToHist("\n" + tu(currentPoke(player).nick + " lost " + damage + "% of its health!"));
			}
			break;
		} case AbilityMessage: {
			short ab = msg.readShort();
			byte part = msg.readByte();
			byte type = msg.readByte();
			byte foe = msg.readByte();
			short other = msg.readShort();
			System.out.println("OTHER IS: " + other);
			String s = netServ.db.query("SELECT Effect" + part + " FROM [Ability_message] WHERE _id = " + (ab + 1));
	        if(other != -1 && s.contains("%st")) s = s.replaceAll("%st", netServ.getResources().getString((Stat.values()[other].rstring())));
	        s = s.replaceAll("%s", currentPoke(player).nick);
	        // Below commented out in PO code
	        //            mess.replace("%ts", name(spot));
	        //            mess.replace("%tf", name(!spot));
	        if(type  != -1) s = s.replaceAll("%t", Type.values()[type].toString());
	        if(foe   != -1) s = s.replaceAll("%f", currentPoke(foe).nick);
	        if(other != -1 && s.contains("%m")) s = s.replaceAll("%m", netServ.db.query("SELECT Name FROM [Moves] WHERE _id = " + other));
	        // Below commented out in PO code
	        //            mess.replace("%d", QString: {:number(other));
	        if(other != -1 && s.contains("%i")) s = s.replaceAll("%i", itemName(other));
	        if(other != -1 && s.contains("%a")) s = s.replaceAll("%a", netServ.db.query("SELECT Name FROM [Abilities] WHERE _id = " + (other + 1)));
	        if(other != -1 && s.contains("%p")) s = s.replaceAll("%p", netServ.db.query("SELECT Name FROM [Pokemons] WHERE _id = " + other));
	        if (type == Type.Normal.ordinal()) {
	        	writeToHist("\n" + tu(NetworkService.escapeHtml(s)));
	        } else {
	        	writeToHist(Html.fromHtml("<br><font color =" + TypeColor.values()[type] + tu(NetworkService.escapeHtml(s)) + "</font>"));
	        }
			break;
		} case Substitute: {
			currentPoke(player).sub = msg.readBool();
			if (player == me) {
				if (netServ.battleActivity != null)
					netServ.battleActivity.updateMyPoke();
			} else {
				if (netServ.battleActivity != null)
					netServ.battleActivity.updateOppPoke();
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
		} case Clause: {
			if (player >= 0 && player < Clauses.values().length)
				writeToHist("\n" + Clauses.values()[player].battleText());
			break;
		} case Rated: {
			boolean rated = msg.readBool();
			writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue + "Rule: " +
					(rated ? "Rated" : "Unrated") + "</b></font>"));
            for (int i = 0; i < Clauses.values().length; i++) {
                if ((conf.clauses & (1 << i)) > 0 ? true : false) {
                    writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue + "Rule: " +
                    		Clauses.values()[i]));
                }
            }
			break;
		} case TierSection: {
			String tier = msg.readQString();
			writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
					"Tier: " + tier + "</b></font>"));
			break;
		} case TempPokeChange: {
			byte id = msg.readByte();
			System.out.println("Recieved: " + TempPokeChange.values()[id]);
			switch(TempPokeChange.values()[id]) {
			case TempMove:
			case DefMove:
				byte slot = msg.readByte();
				BattleMove newMove = new BattleMove(msg.readShort(), netServ.db);
				displayedMoves[slot] = newMove;
				if (id == TempPokeChange.DefMove.ordinal()) {
					myTeam.pokes[0].moves[slot] = newMove;
				}
				if (netServ.battleActivity != null) {
					netServ.battleActivity.updatePokes(player);
				}
				break;
			case TempPP:
				slot = msg.readByte();
				byte PP = msg.readByte();
				displayedMoves[slot].currentPP = PP;
				if (netServ.battleActivity !=null) {
					netServ.battleActivity.updateMovePP(slot);
				}
				break;
			case TempSprite:
				UniqueID sprite = new UniqueID(msg);
				if (sprite.pokeNum != 0)
					currentPoke(player).specialSprites.addFirst(sprite);
				else
					currentPoke(player).specialSprites.removeFirst();
				if (netServ.battleActivity !=null) {
					netServ.battleActivity.updatePokes(player);
				}
				break;
			case DefiniteForme:
				byte poke = msg.readByte();
				short newForm = msg.readShort();
				pokes[player][poke].uID.pokeNum = newForm;
				if (isOut(poke)) {
					currentPoke(slot(player, poke)).uID.pokeNum = newForm;
					if (netServ.battleActivity !=null) {
						netServ.battleActivity.updatePokes(player);
					}
				}
				break;
			case AestheticForme:
				newForm = msg.readShort();
				currentPoke(player).uID.subNum = (byte) newForm;
				if (netServ.battleActivity !=null) {
					netServ.battleActivity.updatePokes(player);
				}
			}
			break;
		} case MakeYourChoice: {
			if (netServ.battleActivity != null) {
				netServ.battleActivity.updateButtons();
				if (allowSwitch && !allowAttack)
					netServ.battleActivity.switchToPokeViewer();
			}
			break;
		} case OfferChoice: {
			byte numSlot = msg.readByte(); // XXX what is this?
			allowSwitch = msg.readBool();
			System.out.println("Switch allowed: " + allowSwitch);
			allowAttack = msg.readBool();
			System.out.println("Attacks allowed: " + allowAttack);
			for (int i = 0; i < 4; i++) {
					allowAttacks[i] = msg.readBool();
					System.out.print("Allow attack " + i + ": ");
					System.out.println(allowAttacks[i]);
			}
			
			if (allowAttack && !allowAttacks[0] && !allowAttacks[1] && !allowAttacks[2] && !allowAttacks[3])
				shouldStruggle = true;
			else
				shouldStruggle = false;
			
			clicked = false;
			
			if (netServ.battleActivity != null)
				netServ.battleActivity.updateButtons();
			break;
		} case CancelMove: {
			clicked = false;
			if (netServ.battleActivity != null)
				netServ.battleActivity.updateButtons();
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
			if(player == me) {
				myTeam.pokes[0].currentHP = newHP;
				currentPoke(player).lastKnownPercent = (byte)newHP;
				currentPoke(player).lifePercent = (byte)(newHP * 100 / myTeam.pokes[0].totalHP);
			}
			else {
				currentPoke(player).lastKnownPercent = (byte)newHP;
				currentPoke(player).lifePercent = (byte)newHP;
			}
			if(netServ.battleActivity != null) {
				// Block until the hp animation has finished
				// Timeout after 10s
				try {
					synchronized (this) {
						netServ.battleActivity.animateHpBarTo(player, currentPoke(player).lifePercent);
						wait(10000);
					}
				} catch (InterruptedException e) {}
				netServ.battleActivity.updateCurrentPokeListEntry();
			}
			break;
		} case SpotShifts: {
			// TODO
			break;
		} case RearrangeTeam: {
			oppTeam = new ShallowShownTeam(msg);
			shouldShowPreview = true;
			if(netServ.battleActivity != null && netServ.battleActivity.hasWindowFocus())
				netServ.battleActivity.notifyRearrangeTeamDialog();
			break;
		} case ChangePP: {
			byte moveNum = msg.readByte();
			byte newPP = msg.readByte();
			displayedMoves[moveNum].currentPP = myTeam.pokes[0].moves[moveNum].currentPP = newPP;
			if(netServ.battleActivity != null)
				netServ.battleActivity.updateMovePP(moveNum);
			break;
		} case DynamicInfo: {
			dynamicInfo[player] = new BattleDynamicInfo(msg);
			break;
		} case DynamicStats: {
			for (int i = 0; i < 5; i++)
				myTeam.pokes[player / 2].stats[i] = msg.readShort();
			break;
		} default: {
			System.out.println("Battle command unimplemented -- " + bc);
			break;
		}
		}
		}
	}
	
	
	public static String itemName(int itemnum) {
		// I don't know how Java is okay with me referencing the non-static
		// netServ in a static context, but I'll take it
		try {
			if (itemnum < 8000)
				return netServ.db.query("SELECT Name FROM [Items] WHERE _id = " + (itemnum + 1));
			else
				return netServ.db.query("SELECT Name FROM [Berries] WHERE _id = " + (itemnum - 7999));
		} catch (SQLiteException e) {
			return "";
		}
	}
	
	public static String itemMessage(int item, int part)
	{
		try {
			if (item < 8000)
				return netServ.db.query("SELECT EFFECT" + part + " FROM [Item_message] WHERE _id = " + (item + 1));
			else
				return netServ.db.query("SELECT EFFECT" + part + " FROM [Berry_message] WHERE _id = " + (item - 7999));
		} catch (SQLiteException e) {
			return "";
		}
	}
	
	enum TempPokeChange {
		TempMove,
		TempAbility,
		TempItem,
		TempSprite,
		DefiniteForme,
		AestheticForme,
		DefMove,
		TempPP
	}
}
