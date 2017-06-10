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

	public boolean clicked;
	public boolean[] allowSwitch = new boolean[3];
	public boolean[] allowAttack = new boolean[3];
	public boolean[] allowMega = new boolean[3];
	public boolean[] allowZMove = new boolean[3];
	public boolean[][] allowAttacks = new boolean[3][4];
	public boolean[][] allowZMoves = new boolean[3][4];
	public boolean[] shouldStruggle = new boolean[3];
	public BattleMove[] displayedMoves = new BattleMove[4];

	public Battle(BattleConf bc, Bais msg, PlayerInfo p1, PlayerInfo p2, int meID, int bID, NetworkService ns) {
		super(bc, p1, p2, bID, ns);

		MoveInfo.newGen();
		MoveInfo.forceSetGen(conf.gen.num, conf.gen.subNum);

		myTeam = new BattleTeam(msg, conf.gen);

		numberOfSlots = ChallengeEnums.Mode.values()[conf.mode].numberOfSlots();
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

	public Baos constructAttack(byte attack, boolean mega, boolean zmove) {
		Baos b = new Baos();
		b.putInt(bID);
		AttackChoice ac = new AttackChoice(attack, opp, mega, zmove);
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
	public void dealWithCommand(BattleCommand bc, byte num, Bais msg)  {
        //Log.e(TAG, "Received bc: " + bc.toString() + " at state time: " + (System.currentTimeMillis() - startTime));
		byte player = player(num);
		byte slot = slot(num, player);
		switch(bc) {
		case SendOut: {
			if (player != me) {
				super.dealWithCommand(bc, num, msg);
				return;
			}

			boolean silent = msg.readBool();
			byte fromSpot = msg.readByte();

			BattlePoke temp = myTeam.pokes[slot];

			myTeam.pokes[slot] = myTeam.pokes[fromSpot];
			myTeam.pokes[fromSpot] = temp;

			for (int i=0; i < 4; i++) {
				displayedMoves[i] = new BattleMove(myTeam.pokes[slot].moves[i]);
			}

			ShallowBattlePoke tempPoke = pokes[player][slot];
			pokes[player][slot] = pokes[player][fromSpot];
			pokes[player][fromSpot] = tempPoke;

			if (msg.available() > 0) // this is the first time you've seen it
				pokes[player][slot] = new ShallowBattlePoke(msg, (player == me), conf.gen);

			if(activity != null) {
				activity.samePokes[player] = false;
				activity.updatePokes(player, slot);
				activity.updatePokeballs();
			}

			SharedPreferences prefs = netServ.getSharedPreferences("battle", Context.MODE_PRIVATE);
			if (prefs.getBoolean("pokemon_cries", true)) {
				try {
					synchronized (this) {
						netServ.playCry(this, currentPoke(player, slot));
						if (!baked) wait(3000); else wait(1000);
					}
				} catch (InterruptedException e) { Log.e(TAG, "INTERRUPTED"); }
			}

			if(!silent)
				writeToHist("\n" + tu((players[player].nick() + " sent out " + 
						currentPoke(player, slot).rnick + "!")));

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
						activity.updatePokes(player, slot);
					activity.updatePokeballs();
				}
			}
			break;
		} case TempPokeChange: {
			byte id = msg.readByte();

			switch(TempPokeChange.values()[id]) {
			case TempMove:
			case DefMove: {
				byte moveslot = msg.readByte();
				BattleMove newMove = new BattleMove(msg.readShort());
				displayedMoves[moveslot] = newMove;
				if (id == TempPokeChange.DefMove.ordinal()) {
					myTeam.pokes[0].moves[moveslot] = newMove;
				}
				if (activity != null) {
					activity.updatePokes(player, slot);
				}
				break;
			} case TempPP: {
				byte moveslot = msg.readByte();
				byte PP = msg.readByte();
				displayedMoves[moveslot].currentPP = myTeam.pokes[slot].moves[moveslot].currentPP = PP;
				if (activity != null) {
					activity.updateMovePP(moveslot);
				}
				break;
			} case TempSprite:
				UniqueID sprite = new UniqueID(msg);
				if (sprite.pokeNum != 0)
					currentPoke(player, slot).specialSprites.addFirst(sprite);
				else if (currentPoke(player, slot).specialSprites.size() > 0)
					currentPoke(player, slot).specialSprites.removeFirst();
				if (activity !=null) {
					activity.samePokes[player] = false;
					activity.updatePokes(player, slot);
					activity.updatePokeBall(player, 0);
				}
				break;
			case DefiniteForme:
				byte poke = msg.readByte();
				UniqueID uID = new UniqueID(msg);
				pokes[player][poke].uID = uID;
				if (isOut(poke)) {
					pokes[player][poke].uID = uID;
					if (player == opp) {
						pokes[player][poke].setStats(conf.gen.num);
						pokes[player][poke].setTypes(conf.gen.num);
					}
					if (activity !=null) {
						activity.samePokes[player] = false;
						activity.updatePokes(player, slot);
						activity.updatePokeBall(player, poke);
					}
				}
				break;
			case AestheticForme:
				short newForm = msg.readShort();
				currentPoke(player, slot).uID.subNum = (byte) newForm;
				if (activity !=null) {
					activity.samePokes[player] = false;
					activity.updatePokes(player, slot);
					activity.updatePokeBall(player, 0);
				}
			default: break;
			}
			break;
		} case MakeYourChoice: {
			if (activity != null) {
				activity.updateButtons();
				if (allowSwitch[slot] && !allowAttack[slot])
					activity.switchToPokeViewer();
			}
			break;
		} case OfferChoice: {
			@SuppressWarnings("unused")
			byte numSlot = msg.readByte(); //Which poke the choice is for
			allowSwitch[slot] = msg.readBool();
			allowAttack[slot] = msg.readBool();

			for (int i = 0; i < 4; i++) {
				allowAttacks[slot][i] = msg.readBool();
			}

			allowMega[slot] = msg.readBool();
			allowZMove[slot] = msg.readBool();

			if (allowZMove[slot]) {
				for (int i = 0; i < 4; i++) {
					allowZMoves[slot][i] = msg.readBool();
				}
			}

			shouldStruggle[slot] = (allowAttack[slot] && !allowAttacks[slot][0] && !allowAttacks[slot][1] && !allowAttacks[slot][2] && !allowAttacks[slot][3]);

			clicked = false;

			if (activity != null) {
				activity.currentChoiceSlot = 0;
				activity.updateButtons();
				activity.invalidateOptionsMenu();
			}
			break;
		} case CancelMove: {
			clicked = false;
			if (activity != null)
				activity.updateButtons();
				break;
		} case ChangeHp: {
			short newHP = msg.readShort();
			if(player == me) {
				myTeam.pokes[slot].currentHP = newHP;
				currentPoke(player, slot).lastKnownPercent = currentPoke(player, slot).lifePercent;
				currentPoke(player, slot).lifePercent = (byte)(newHP * 100 / myTeam.pokes[slot].totalHP);
			} else {
				currentPoke(player, slot).lastKnownPercent = currentPoke(player, slot).lifePercent;
				currentPoke(player, slot).lifePercent = (byte)newHP;
			}
			if(activity != null) {
				// Block until the hp animation has finished
				// Timeout after 10s
				try {
					synchronized (this) {
						int change = currentPoke(player, slot).lastKnownPercent - currentPoke(player, slot).lifePercent;
						activity.animateHpBarTo(player, slot, currentPoke(player, slot).lifePercent, change);
                        //Log.e(TAG, "change " + change);
                        if (change < 0) change = -change;
                        if (change > 100) change = 100;
						if (!baked) wait(5000); else wait(change*43);
					}
				} catch (InterruptedException e) {}
				activity.updateCurrentPokeListEntry();
			}
			break;
		} case StraightDamage: {
			if (player != me) {
				super.dealWithCommand(bc, num, msg);
			} else {
				short damage = msg.readShort();
				writeToHist("\n" + tu(currentPoke(player, slot).nick + " lost " + damage + "HP! (" + (damage*100/myTeam.pokes[player/2].totalHP) + "% of its health)"));
			}
			break;
		} case SpotShifts: {
		    byte spot1 = msg.readByte();
		    byte spot2 = msg.readByte();
		    boolean silent = msg.readBool();
		    if (!silent) {
                if (pokes[player][spot1].status() == Status.Koed.poValue()) {
                    writeToHist(Html.fromHtml("<br>" + tu(pokes[player][spot2].nick +
                            " moved to the center!")));
                } else {
                    writeToHist(Html.fromHtml("<br>" + tu(pokes[player][spot2].nick +
                            " shifted spots with " + pokes[player][spot1].nick + "!")));
                }
            }
            ShallowBattlePoke tempPoke = pokes[player][spot1];
		    pokes[player][spot1] = pokes[player][spot2];
		    pokes[player][spot2] = tempPoke;
		    if(activity != null)
            {
                activity.updatePokes(player, spot1);
                activity.updatePokes(player, spot2);
            }
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
			displayedMoves[moveNum].currentPP = myTeam.pokes[slot].moves[moveNum].currentPP = newPP;
			if(activity != null)
				activity.updateMovePP(moveNum);
			break;
		} case DynamicStats: {
			for (int i = 0; i < 5; i++)
				myTeam.pokes[slot].stats[i] = msg.readShort();
			break;
		} case UseItem: {
			byte item = msg.readByte();
			Log.w("SpectatingBattle", bc.name() + item);
		} case ItemCountChange: {
			byte item = msg.readByte();
			byte count = msg.readByte();
			Log.w("SpectatingBattle", bc.name() + item + ":" + count);
		} default: {
			super.dealWithCommand(bc, num, msg);
			break;
		}
		}
	}
}
