package com.podevs.android.poAndroid.battle;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.util.Log;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.player.PlayerInfo;
import com.podevs.android.poAndroid.poke.PokeEnums.Status;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.StringUtilities;

public class Battle extends SpectatingBattle {
	static private final String TAG = "Battle";	

	public BattleTeam myTeam;
	public ShallowShownTeam oppTeam;

	public boolean allowSwitch, allowAttack, clicked, allowMega = false;
	public boolean[] allowAttacks = new boolean[4];
	public boolean shouldStruggle = false;
	public BattleMove[] displayedMoves = new BattleMove[4];

	public Battle(BattleConf bc, Bais msg, PlayerInfo p1, PlayerInfo p2, int meID, int bID, NetworkService ns) {
		super(bc, p1, p2, bID, ns);

		MoveInfo.newGen();
		MoveInfo.forceSetGen(conf.gen.num, conf.gen.subNum);

		myTeam = new BattleTeam(msg, conf.gen);

		// Only supporting singles for now
		numberOfSlots = 2;
		players[0] = p1;
		players[1] = p2;
		// Figure out who's who
		if(players[0].id != meID) {
			me = 1;
			opp = 0;
		}

		for (int i = 0; i < 4; i++)
			displayedMoves[i] = new BattleMove();

        startTime = System.currentTimeMillis();
	}


	public Baos constructCancel() {
		Baos b = new Baos();
		b.putInt(bID);
		b.putBaos(new BattleChoice(me, ChoiceType.CancelType));
		return b;
	}

	public Baos constructAttack(byte attack, boolean mega) {
		Baos b = new Baos();
		b.putInt(bID);
		AttackChoice ac = new AttackChoice(attack, opp, mega);
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

	public Baos constructDraw() {
		Baos b = new Baos();
		b.putInt(bID);
		b.putBaos(new BattleChoice(me, ChoiceType.DrawType));
		return b;
	}

	@Override
	public void dealWithCommand(BattleCommand bc, byte player, Bais msg)  {
        //Log.e(TAG, "Received bc: " + bc.toString() + " at state time: " + (System.currentTimeMillis() - startTime));
		switch(bc) {
		case SendOut: {
			if (player != me) {
				super.dealWithCommand(bc, player, msg);
				return;
			}

			boolean silent = msg.readBool();
			byte fromSpot = msg.readByte();

			BattlePoke temp = myTeam.pokes[0];

			myTeam.pokes[0] = myTeam.pokes[fromSpot];
			myTeam.pokes[fromSpot] = temp;

			for (int i=0; i < 4; i++) {
				displayedMoves[i] = new BattleMove(myTeam.pokes[0].moves[i]);
			}

			ShallowBattlePoke tempPoke = pokes[player][0];
			pokes[player][0] = pokes[player][fromSpot];
			pokes[player][fromSpot] = tempPoke;

			if (msg.available() > 0) // this is the first time you've seen it
				pokes[player][0] = new ShallowBattlePoke(msg, (player == me), conf.gen);

			if(activity != null) {
				activity.samePokes[player] = false;
				activity.updatePokes(player);
				activity.updatePokeballs();
			}

			SharedPreferences prefs = netServ.getSharedPreferences("battle", Context.MODE_PRIVATE);
			if (prefs.getBoolean("pokemon_cries", true)) {
				try {
					synchronized (this) {
						netServ.playCry(this, currentPoke(player));
						if (!baked) wait(5000); else wait(1000);
					}
				} catch (InterruptedException e) { Log.e(TAG, "INTERRUPTED"); }
			}

			if(!silent)
				writeToHist("\n" + tu((players[player].nick() + " sent out " + 
						currentPoke(player).rnick + "!")));

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
				if (activity != null) {
					if (isOut(poke))
						activity.updatePokes(player);
					activity.updatePokeballs();
				}
			}
			break;
		} case TempPokeChange: {
			byte id = msg.readByte();

			switch(TempPokeChange.values()[id]) {
			case TempMove:
			case DefMove:
				byte slot = msg.readByte();
				BattleMove newMove = new BattleMove(msg.readShort());
				displayedMoves[slot] = newMove;
				if (id == TempPokeChange.DefMove.ordinal()) {
					myTeam.pokes[0].moves[slot] = newMove;
				}
				if (activity != null) {
					activity.updatePokes(player);
				}
				break;
			case TempPP:
				slot = msg.readByte();
				byte PP = msg.readByte();
				displayedMoves[slot].currentPP = PP;
				if (activity !=null) {
					activity.updateMovePP(slot);
				}
				break;
			case TempSprite:
				UniqueID sprite = new UniqueID(msg);
				if (sprite.pokeNum != 0)
					currentPoke(player).specialSprites.addFirst(sprite);
				else if (currentPoke(player).specialSprites.size() > 0)
					currentPoke(player).specialSprites.removeFirst();
				if (activity !=null) {
					activity.samePokes[player] = false;
					activity.updatePokes(player);
				}
				break;
			case DefiniteForme:
				byte poke = msg.readByte();
				UniqueID uID = new UniqueID(msg);
				pokes[player][poke].uID = uID;
				if (isOut(poke)) {
					currentPoke(slot(player, poke)).uID = uID;
					if (player == opp) {
						currentPoke(slot(player, poke)).setStats(conf.gen.num);
						currentPoke(slot(player, poke)).setTypes(conf.gen.num);
					}
					if (activity !=null) {
						activity.samePokes[player] = false;
						activity.updatePokes(player);
					}
				}
				break;
			case AestheticForme:
				short newForm = msg.readShort();
				currentPoke(player).uID.subNum = (byte) newForm;
				if (activity !=null) {
					activity.samePokes[player] = false;
					activity.updatePokes(player);
				}
			default: break;
			}
			break;
		} case MakeYourChoice: {
			if (activity != null) {
				activity.updateButtons();
				if (allowSwitch && !allowAttack)
					activity.switchToPokeViewer();
			}
			break;
		} case OfferChoice: {
			@SuppressWarnings("unused")
			byte numSlot = msg.readByte(); //Which poke the choice is for
			allowSwitch = msg.readBool();
			allowAttack = msg.readBool();

			for (int i = 0; i < 4; i++) {
				allowAttacks[i] = msg.readBool();
			}

			allowMega = msg.readBool();

			if (allowAttack && !allowAttacks[0] && !allowAttacks[1] && !allowAttacks[2] && !allowAttacks[3])
				shouldStruggle = true;
			else
				shouldStruggle = false;

			clicked = false;

			if (activity != null)
				activity.updateButtons();
			break;
		} case CancelMove: {
			clicked = false;
			if (activity != null)
				activity.updateButtons();
			break;
		} case ChangeHp: {
			short newHP = msg.readShort();
			if(player == me) {
				myTeam.pokes[0].currentHP = newHP;
				currentPoke(player).lastKnownPercent = currentPoke(player).lifePercent;
				currentPoke(player).lifePercent = (byte)(newHP * 100 / myTeam.pokes[0].totalHP);
			} else {
				currentPoke(player).lastKnownPercent = currentPoke(player).lifePercent;
				currentPoke(player).lifePercent = (byte)newHP;
			}
			if(activity != null) {
				// Block until the hp animation has finished
				// Timeout after 10s
				try {
					synchronized (this) {
						int change = currentPoke(player).lastKnownPercent - currentPoke(player).lifePercent;
						activity.animateHpBarTo(player, currentPoke(player).lifePercent, change);
                        //Log.e(TAG, "change " + change);
                        if (change < 0) change = -change;
                        if (change > 100) change = 100;
						if (!baked) wait(6000); else wait(change*43);
					}
				} catch (InterruptedException e) {}
				activity.updateCurrentPokeListEntry();
			}
			break;
		} case StraightDamage: {
			if (player != me) {
				super.dealWithCommand(bc, player, msg);
			} else {
				short damage = msg.readShort();
				writeToHist("\n" + tu(currentPoke(player).nick + " lost " + damage + "HP! (" + (damage*100/myTeam.pokes[player/2].totalHP) + "% of its health)"));
			}
			break;
		} case SpotShifts: {
			// TODO
			break;
		} case RearrangeTeam: {
			oppTeam = new ShallowShownTeam(msg, conf.gen.num);
			shouldShowPreview = true;
			if(activity != null)
				activity.notifyRearrangeTeamDialog();

			String names[] = {PokemonInfo.name(oppTeam.poke(0).uID), PokemonInfo.name(oppTeam.poke(1).uID),
					PokemonInfo.name(oppTeam.poke(2).uID), PokemonInfo.name(oppTeam.poke(3).uID),
					PokemonInfo.name(oppTeam.poke(4).uID), PokemonInfo.name(oppTeam.poke(5).uID)};

			writeToHist(Html.fromHtml("<br><font color=\"blue\"><b>Opponent's team: </b></font>" + StringUtilities.join(names, " / ")));
			break;
		} case ChangePP: {
			byte moveNum = msg.readByte();
			byte newPP = msg.readByte();
			displayedMoves[moveNum].currentPP = myTeam.pokes[0].moves[moveNum].currentPP = newPP;
			if(activity != null)
				activity.updateMovePP(moveNum);
			break;
		} case DynamicStats: {
			for (int i = 0; i < 5; i++)
				myTeam.pokes[player / 2].stats[i] = msg.readShort();
			break;
		} case UseItem: {
			byte item = msg.readByte();
			Log.w("SpectatingBattle", bc.name() + item);
		} case ItemCountChange: {
			byte item = msg.readByte();
			byte count = msg.readByte();
			Log.w("SpectatingBattle", bc.name() + item + ":" + count);
		} default: {
			super.dealWithCommand(bc, player, msg);
			break;
		}
		}
	}
}
