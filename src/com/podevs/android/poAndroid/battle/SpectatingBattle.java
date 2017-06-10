package com.podevs.android.poAndroid.battle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.SparseArray;

import com.podevs.android.poAndroid.ColorEnums.QtColor;
import com.podevs.android.poAndroid.ColorEnums.StatusColor;
import com.podevs.android.poAndroid.ColorEnums.TypeColor;
import com.podevs.android.poAndroid.ColorEnums.TypeForWeatherColor;
import com.podevs.android.poAndroid.ColorEnums.TypeForTerrainColor;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.battle.ChallengeEnums.Clauses;
import com.podevs.android.poAndroid.player.PlayerInfo;
import com.podevs.android.poAndroid.poke.PokeEnums;
import com.podevs.android.poAndroid.poke.PokeEnums.Stat;
import com.podevs.android.poAndroid.poke.PokeEnums.Status;
import com.podevs.android.poAndroid.poke.PokeEnums.StatusFeeling;
import com.podevs.android.poAndroid.poke.PokeEnums.Weather;
import com.podevs.android.poAndroid.poke.PokeEnums.WeatherState;
import com.podevs.android.poAndroid.poke.PokeEnums.Terrain;
import com.podevs.android.poAndroid.poke.PokeEnums.TerrainState;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.AbilityInfo;
import com.podevs.android.poAndroid.pokeinfo.ItemInfo;
import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.poAndroid.pokeinfo.TypeInfo.Type;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.FixedSizeStack;
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

    public long startTime;

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
    public final SpannableStringBuilder histDelta; //= new SpannableStringBuilder();

    public BattleDynamicInfo[] dynamicInfo = new BattleDynamicInfo[2];

    public void writeToHist(CharSequence text) {
        synchronized (histDelta) {
            histDelta.append(text);
        }
    }

    public boolean baked;

    public BattleConf conf;
    public BattleActivity activity = null; //activity associated with the battle

    public boolean shouldShowPreview = false;

    public SpectatingBattle(BattleConf bc, PlayerInfo p1, PlayerInfo p2, int bID, NetworkService ns) {
        hist = new SpannableStringBuilder();
        histDelta = new SpannableStringBuilder();
        netServ = ns;
        conf = bc; // singles, doubles, triples
        this.bID = bID;

        numberOfSlots = (conf.mode() + 1) * 2;
        players[0] = p1;
        players[1] = p2;

        remainingTime[0] = remainingTime[1] = 5 * 60;
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

        baked = PreferenceManager.getDefaultSharedPreferences(ns).getBoolean("baked", false);
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

    public ShallowBattlePoke currentPoke(int player, int slot) {
        return pokes[player][slot];
    }

    public boolean isOut(Byte poke) {
        return poke < numberOfSlots / 2;
    }

    public int slot(int player, int poke) {
        return player + poke * 2;
    }

    @SuppressLint("DefaultLocale")
    public String tu(String toUpper) {
        // Makes the first letter of a string uppercase
        if (toUpper.length() <= 1)
            return toUpper;
        return toUpper.substring(0, 1).toUpperCase(Locale.getDefault()) + toUpper.substring(1);
    }

    public byte player(byte num) {
        if ((num % 2) == 0) {
            return 0;
        } else {
            return 1;
        }

        // 0 me
        // 1 opp

        // 0 2 me
        // 1 3 opp

        // 0 2 4 me
        // 1 3 5 opp
    }

    public byte slot(byte num, byte player) {
        // 0 me (/2)
        // 1 opp (-1:/2)

        // 0 1 me (/2)
        // 0 1 opp (-1:/2)

        // 0 1 2 me (/2)
        // 0 1 2 opp (-1:/2)

        if (player == me) {
            return (byte) (num / 2);
        } else { // (player == opp)
            return (byte) ((num - 1) / 2);
        }
    }

    BattlePacket lastPacket = null;
    FixedSizeStack<BattlePacket> packetStack = new FixedSizeStack<BattlePacket>(25);

    public void receiveCommand(Bais msg) {
        synchronized (this) {
            byte command = msg.readByte();

            if (command < 0 || command >= BattleCommand.values().length) {
                Log.w("Spectating battle", "Battle command unknown " + String.valueOf((int) command));
                return;
            }

            BattleCommand bc = BattleCommand.values()[command];
            byte player = msg.readByte();
			/* Because we don't deal with double battles */
            //num = (bc == BattleCommand.Clause ? num : (byte) (num % 2));

            lastPacket = new BattlePacket(bc, player, msg.cloneRemaining());
            packetStack.push(lastPacket);

            try {
                dealWithCommand(bc, player, msg);
            } catch (Exception e) {
                Log.e(TAG, lastPacket.toString());
                e.printStackTrace();
            }
        }
    }

    //int mark;
    //int position;
    //int len;
    public void dealWithCommand(BattleCommand bc, byte num, Bais msg) {
        byte player = player(num);
        byte slot = slot(num, player);
        switch (bc) {
            case SendOut: {
                boolean silent = msg.readBool();
                byte fromSpot = msg.readByte();

                ShallowBattlePoke tempPoke = pokes[player][slot];
                tempPoke.sub = false;
                pokes[player][slot] = pokes[player][fromSpot];
                pokes[player][fromSpot] = tempPoke;

			/*
            if(msg.available() > 0) { // this is the first time you've seen it
				pokes[player][0] = new ShallowBattlePoke(msg, (player == me) ? true : false, conf.gen);
			}*/ //No Clue how this works, but it doesn't to the intended effect - MM
                //So I replaced it with the function below
                if (pokes[player][slot].level == 0 || silent) {
                    pokes[player][slot] = new ShallowBattlePoke(msg, (player == me), conf.gen);
                    if (activity == null) {
                        pokes[player][slot].pokeName = PokemonInfo.name(pokes[player][slot].uID);
                    }
                }

                if (activity != null) {
                    //activity.samePokes[player][slot] = false;
                    activity.updatePokes(player, slot);
                    activity.updatePokeballs();
                }

                SharedPreferences prefs = netServ.getSharedPreferences("battle", Context.MODE_PRIVATE);
                if (prefs.getBoolean("pokemon_cries", true)) {
                    try {
                        synchronized (this) {
                            netServ.playCry(this, pokes[player][slot]);
                            if (!baked) wait(3000);
                            else wait(1000);
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "INTERRUPTED");
                    }
                }


                if (!silent)
                    writeToHist("\n" + tu((players[player].nick() + " sent out " + pokes[player][slot].rnick + "!")));
                break;
            }
            case SendBack: {
                boolean silent = msg.readBool();
                if (!silent)
                    writeToHist("\n" + tu((players[player].nick() + " called " + pokes[player][slot].rnick + " back!")));
                break;
            }
            case UseAttack: {
                short attack = msg.readShort();
                int color;
                try {
                    color = MoveInfo.type(attack);
                } catch (NumberFormatException e) {
                    color = Type.Curse.ordinal();
                }
                boolean silent = msg.readBool();
                if (!silent) {
                    writeToHist(Html.fromHtml("<br>" + tu(pokes[player][slot].nick +
                            " used <font color =" + TypeColor.values()[color] + MoveInfo.name(attack) + "</font>!")));
                }
//				if (player == opp) {
//					activity.updateMoves(attack);
//				} else if (activity.isSpectating()) {
//					activity.updateMoves(attack);
//				}
                /*
			boolean special = msg.readBool();
			if (player == opp && ! special) {
				activity.updateMoves(attack);
			}
			*/
                break;
            }
            case BeginTurn: {
                int turn = msg.readInt();
                writeToHist(Html.fromHtml("<br><b><font color=" + QtColor.Blue +
                        "Start of turn " + turn + "</font></b>"));
                break;
            }
            case Ko: {
                SharedPreferences prefs = netServ.getSharedPreferences("battle", Context.MODE_PRIVATE);
                if (prefs.getBoolean("pokemon_cries", true)) {
                    try {
                        synchronized (this) {
                            netServ.playCry(this, pokes[player][slot]);
                            if (!baked) wait(7000);
                            else wait(2000);
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, "INTERRUPTED");
                    }
                }
                writeToHist(Html.fromHtml("<br><b>" + tu(StringUtilities.escapeHtml((pokes[player][slot].nick))) +
                        " fainted!</b>"));
                break;
            }
            case Hit: {
                byte number = msg.readByte();
                writeToHist("\nHit " + number + " time" + ((number > 1) ? "s!" : "!"));
                break;
            }
            case Effective: {
                byte eff = msg.readByte();
                switch (eff) {
                    case 0:
                        writeToHist(getString(R.string.no_effect_message));
                        break;
                    case 1:
                    case 2:
                        writeToHist(Html.fromHtml("<br><font color=" + QtColor.Gray +
                                getString(R.string.not_very_effective_message) + "</font>"));
                        break;
                    case 8:
                    case 16:
                        writeToHist(Html.fromHtml("<br><font color=" + QtColor.Blue +
                                getString(R.string.super_effective_message) + "!</font>"));
                        break;
                }
                break;
            }
            case CriticalHit: {
                writeToHist(Html.fromHtml("<br><font color=#6b0000>A critical hit!</font>"));
                break;
            }
            case Miss: {
                writeToHist("\nThe attack of " + pokes[player][slot].nick + " missed!");
                break;
            }
            case Avoid: {
                writeToHist("\n" + tu(pokes[player][slot].nick + " avoided the attack!"));
                break;
            }
            case StatChange: {
                byte stat = msg.readByte(), boost = msg.readByte();
                boolean silent = msg.readBool();
                if (!silent) {
                    writeToHist("\n" + tu(pokes[player][slot].nick + "'s " +
                            netServ.getString(Stat.values()[stat].rstring()) +
                            (Math.abs(boost) > 1 ? " sharply" : "") + (boost > 0 ? " rose!" : " fell!")));
                }
                break;
            }
            case CappedStat: {
                byte stat = msg.readByte();
                boolean max = msg.readBool();

                writeToHist("\n" + tu(pokes[player][slot].nick + "'s " +
                        netServ.getString(Stat.values()[stat].rstring()) +
                        (max ? " can't go any higher!" : " can't go any lower!")));
                break;
            }
            case UseItem: {
                byte item = msg.readByte();
                Log.w("SpectatingBattle", bc.name() + item);
            }
            case ItemCountChange: {
                byte item = msg.readByte();
                byte count = msg.readByte();
                Log.w("SpectatingBattle", bc.name() + item + ":" + count);
            }
            case StatusChange: {
                final String[] statusChangeMessages = netServ.getResources().getStringArray(R.array.status_change_array);

                byte status = msg.readByte();
                boolean multipleTurns = msg.readBool();
                boolean silent = msg.readBool();
                if (silent) {
                    // Print nothing
                } else if (status > Status.Fine.poValue() && status <= Status.Poisoned.poValue()) {
                    writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(status) + tu(
                            pokes[player][slot].nick + " " + statusChangeMessages[status - 1 +
                                    (status == Status.Poisoned.poValue() && multipleTurns ? 1 : 0)] + "</font>")));
                } else if (status == Status.Confused.poValue()) {
				/* The reason we need to handle confusion separately is because
				 * poisoned and badly poisoned are not separate values in the Status
				 * enum, so confusion does not correspond to the same value in the above
				 * string array as its enum value. */
                    writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(status) + tu(
                            pokes[player][slot].nick + " " + statusChangeMessages[status] + "</font>")));
                }
                break;
            }
            case AbsStatusChange: {
                byte poke = msg.readByte();
                byte status = msg.readByte();

                if (poke < 0 || poke >= 6)
                    break;

                if (status != Status.Confused.poValue()) {
                    pokes[player][poke].changeStatus(status);
                    if (activity != null) {
                        if (isOut(poke))
                            activity.updatePokes(player, slot);
                        activity.updatePokeballs();
                    }
                }
                break;
            }
            case AlreadyStatusMessage: {
                byte status = msg.readByte();
                writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(status) + tu(
                        pokes[player][slot].nick + " is already " + Status.poValues()[status] +
                                ".</font>")));
                break;
            }
            case StatusMessage: {
                byte status = msg.readByte();
                switch (StatusFeeling.values()[status]) {
                    case FeelConfusion:
                        writeToHist(Html.fromHtml("<br><font color=" + TypeColor.Ghost + tu(
                                pokes[player][slot].nick + " " + getString(R.string.feel_confusion_status_message) + "</font>")));
                        break;
                    case HurtConfusion:
                        writeToHist(Html.fromHtml("<br><font color=" + TypeColor.Ghost + tu(
                                getString(R.string.hurt_confusion_status_message) + "</font>")));
                        break;
                    case FreeConfusion:
                        writeToHist(Html.fromHtml("<br><font color=" + TypeColor.Dark + tu(
                                pokes[player][slot].nick + getString(R.string.free_confusion_status_message) + "</font>")));
                        break;
                    case PrevParalysed:
                        writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Paralysed.poValue()) + tu(
                                pokes[player][slot].nick + getString(R.string.prev_paralyzed_status_message) + "</font>")));
                        break;
                    case FeelAsleep:
                        writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Asleep.poValue()) + tu(
                                pokes[player][slot].nick + " " + getString(R.string.feel_asleep_status_message) + "</font>")));
                        break;
                    case FreeAsleep:
                        writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Asleep.poValue()) + tu(
                                pokes[player][slot].nick + " " + getString(R.string.free_asleep_status_message) + "</font>")));
                        break;
                    case HurtBurn:
                        writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Burnt.poValue()) + tu(
                                pokes[player][slot].nick + getString(R.string.hurt_burn_status_message) + "</font>")));
                        break;
                    case HurtPoison:
                        writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Poisoned.poValue()) + tu(
                                pokes[player][slot].nick + getString(R.string.hurt_poison_status_message) + "</font>")));
                        break;
                    case PrevFrozen:
                        writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Frozen.poValue()) + tu(
                                pokes[player][slot].nick + " " + getString(R.string.prev_frozen_status_message) + "</font>")));
                        break;
                    case FreeFrozen:
                        writeToHist(Html.fromHtml("<br><font color=" + new StatusColor(Status.Frozen.poValue()) + tu(
                                pokes[player][slot].nick + getString(R.string.free_frozen_status_message) + "</font>")));
                        break;
                }
                break;
            }
            case Failed: {
                boolean silent = msg.readBool();
                if (!silent)
                    writeToHist("\nBut it failed!");
                break;
            }
            case BattleChat:
            case EndMessage: {
                String message = msg.readString();
                if (message == null || message.equals(""))
                    break;
                writeToHist(Html.fromHtml("<br><font color=" + (player != 0 ? "#5811b1>" : QtColor.Green) +
                        "<b>" + StringUtilities.escapeHtml(players[player].nick()) + ": </b></font>" +
                        StringUtilities.escapeHtml(message)));
                break;
            }
            case Spectating: {
                boolean come = msg.readBool();
                int id = msg.readInt();
                String name = msg.readString();
                if (conf != null && conf.isInBattle(id)) {
                    onPlayerDisconnect(come, players[me].id == id);
                } else {
                    if (come) {
                        addSpectator(id, name);
                    } else {
                        removeSpectator(id);
                    }
                }
                break;
            }
            case SpectatorChat: {
                // TODO if (ignoreSpecs) break;
                int id = msg.readInt();
                String message = msg.readString();
                writeToHist(Html.fromHtml("<br><font color=" + QtColor.Blue + spectators.get(id) +
                        ":</font> " + StringUtilities.escapeHtml(message)));
                break;
            }
            case MoveMessage: {
                short move = msg.readShort();
                byte part = msg.readByte();
                byte type = msg.readByte();
                byte foe = msg.readByte();
                short other = msg.readShort();
                String q = msg.readString();

                String s = MoveInfo.message(move, part);
                s = s.replaceAll("%s", pokes[player][slot].nick);
                s = s.replaceAll("%ts", players[player].nick());
                s = s.replaceAll("%tf", players[(player == 0 ? 1 : 0)].nick());
                if (type != -1) s = s.replaceAll("%t", Type.values()[type].toString());
                if (foe != -1) s = s.replaceAll("%f", pokes[foe][slot].nick);
                if (foe != -1) s = s.replaceAll("%e", pokes[foe][slot].rnick);
                if (other != -1 && s.contains("%m")) s = s.replaceAll("%m", MoveInfo.name(other));
                s = s.replaceAll("%d", Short.toString(other));
                s = s.replaceAll("%q", q);
                if (other != -1 && s.contains("%i")) s = s.replaceAll("%i", ItemInfo.name(other));
                if (other != -1 && s.contains("%a"))
                    s = s.replaceAll("%a", AbilityInfo.name(other));
                if (other != -1 && s.contains("%p"))
                    s = s.replaceAll("%p", PokemonInfo.name(new UniqueID(other, 0)));
                if (s.contains("%st") || s.contains("%f") || s.contains("%i") || s.contains("%m") || s.contains("%p"))
                    Log.e(TAG, "Parsing Issue {move:" + move + " part:" + part + " type:" + type + " foe:" + foe + " other:" + other + " string:" + s + "}");
                writeToHist(Html.fromHtml("<br><font color =" + TypeColor.values()[type] + tu(StringUtilities.escapeHtml(s)) + "</font>"));
                break;
            }
            case NoOpponent: {
                writeToHist("\nBut there was no target...");
                break;
            }
            case ItemMessage: {
                short item = msg.readShort();
                byte part = msg.readByte();
                byte foe = msg.readByte();
                short berry = msg.readShort();
                int other = msg.readInt();
                String s = ItemInfo.message(item, part);
                if (other != -1 && s.contains("%st"))
                    s = s.replaceAll("%st", Stat.values()[other].toString());
                s = s.replaceAll("%s", pokes[player][slot].nick);
                if (foe != -1) s = s.replaceAll("%f", pokes[foe][slot].nick);
                if (berry != -1) s = s.replaceAll("%i", ItemInfo.name(berry));
                if (other != -1 && s.contains("%m")) s = s.replaceAll("%m", MoveInfo.name(other));
                if (other != -1 && s.contains("%p"))
                    s = s.replaceAll("%p", PokemonInfo.name(new UniqueID(other)));
                if (s.contains("%st") || s.contains("%f") || s.contains("%i") || s.contains("%m") || s.contains("%p"))
                    Log.e(TAG, "Parsing Issue {" + "item:" + item + " part:" + part + " foe:" + foe + " berry:" + berry + " other:" + other + " string:" + s + "}");
            /* Balloon gets a really special treatment */
                if (item == 35)
                    writeToHist(Html.fromHtml("<br><b>" + tu(StringUtilities.escapeHtml(s)) + "</b>"));
                else
                    writeToHist("\n" + tu(s));
                break;
            }
            case Flinch: {
                writeToHist("\n" + tu(pokes[player][slot].nick + " flinched!"));
                break;
            }
            case Recoil: {
                boolean damaging = msg.readBool();
                if (damaging)
                    writeToHist("\n" + tu(pokes[player][slot].nick + " is hit with recoil!"));
                else
                    writeToHist("\n" + tu(pokes[player][slot].nick + " had its energy drained!"));
                break;
            }
            case WeatherMessage: {
                byte wstatus = msg.readByte(), weather = msg.readByte();
                if (weather == Weather.NormalWeather.ordinal())
                    break;
                String message;
                String color = new TypeForWeatherColor(weather).toString();
                switch (WeatherState.values()[wstatus]) {
                    case EndWeather:
                        switch (Weather.values()[weather]) {
                            case Hail:
                                message = getString(R.string.end_hail_weather);
                                break;
                            case SandStorm:
                                message = getString(R.string.end_sandstorm_weather);
                                break;
                            case Sunny:
                                message = getString(R.string.end_sunny_weather);
                                break;
                            case Rain:
                                message = getString(R.string.end_rain_weather);
                                break;
                            case HeavySun:
                                message = getString(R.string.end_heavysun_weather);
                                break;
                            case HeavyRain:
                                message = getString(R.string.end_heavyrain_weather);
                                break;
                            case Delta:
                                message = getString(R.string.end_delta_weather);
                                break;
                            default:
                                message = "";
                        }
                        writeToHist(Html.fromHtml("<br><font color=" + color + message + "</font>"));
                        break;
                    case HurtWeather:
                        switch (Weather.values()[weather]) {
                            case Hail:
                                message = getString(R.string.hurt_hail_weather);
                                break;
                            case SandStorm:
                                message = getString(R.string.hurt_sandstorm_weather);
                                break;
                            default:
                                message = "";
                        }
                        writeToHist(Html.fromHtml("<br><font color=" + color + tu(
                                pokes[player][slot].nick + message + "</font>")));
                        break;
                    case ContinueWeather:
                        switch (Weather.values()[weather]) {
                            case Hail:
                                message = getString(R.string.continue_hail_weather);
                                break;
                            case SandStorm:
                                message = getString(R.string.continue_sandstorm_weather);
                                break;
                            case Sunny:
                                message = getString(R.string.continue_sunny_weather);
                                break;
                            case Rain:
                                message = getString(R.string.continue_rain_weather);
                                break;
                            case HeavySun:
                                message = getString(R.string.continue_heavysun_weather);
                                break;
                            case HeavyRain:
                                message = getString(R.string.continue_heavyrain_weather);
                                break;
                            case Delta:
                                message = getString(R.string.continue_delta_weather);
                                break;
                            default:
                                message = "";
                        }
                        writeToHist(Html.fromHtml("<br><font color=" + color + message + "</font>"));
                        break;
                }
                break;
            }
            case StraightDamage: {
                short damage = msg.readShort();
                writeToHist("\n" + tu(pokes[player][slot].nick + " lost " + damage + "% of its health!"));
                break;
            }
            case AbilityMessage: {
                short ab = msg.readShort();
                byte part = msg.readByte();
                byte type = msg.readByte();
                byte foe = msg.readByte();
                short other = msg.readShort();

                String s = AbilityInfo.message(ab, part);
                if (other != -1 && s.contains("%st"))
                    s = s.replaceAll("%st", netServ.getResources().getString((Stat.values()[other].rstring())));
                s = s.replaceAll("%s", pokes[player][slot].nick);
                s = s.replaceAll("%tf", players[(player == 0 ? 1 : 0)].nick());
                // Below commented out in PO code
                //            mess.replace("%ts", name(spot));
                //            mess.replace("%tf", name(!spot));
                if (type != -1) s = s.replaceAll("%t", Type.values()[type].toString());
                if (foe != -1) s = s.replaceAll("%f", pokes[foe][slot].nick);
                if (other != -1 && s.contains("%m")) s = s.replaceAll("%m", MoveInfo.name(other));
                // Below commented out in PO code
                //            mess.replace("%d", QString: {:number(other));
                if (other != -1 && s.contains("%i")) s = s.replaceAll("%i", ItemInfo.name(other));
                if (other != -1 && s.contains("%a"))
                    s = s.replaceAll("%a", AbilityInfo.name(other));
                if (other != -1 && s.contains("%p"))
                    s = s.replaceAll("%p", PokemonInfo.name(new UniqueID(other, 0)));
                if (type == Type.Normal.ordinal()) {
                    writeToHist("\n" + tu(StringUtilities.escapeHtml(s)));
                } else {
                    writeToHist(Html.fromHtml("<br><font color =" + TypeColor.values()[type] + tu(StringUtilities.escapeHtml(s)) + "</font>"));
                }
                break;
            }
            case Substitute: {
                pokes[player][slot].sub = msg.readBool();

                if (activity != null) {
                    //activity.samePokes[player][slot] = false;
                    activity.updatePokes(player, slot);
                }
                break;
            }
            case BattleEnd: {
                byte res = msg.readByte();
                if (res == BattleResult.Tie.ordinal())
                    writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
                            "Tie between " + players[0].nick() + " and " + players[1].nick() +
                            "!</b></font>")); // XXX Personally I don't think this deserves !
                else
                    writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
                            players[player].nick() + " won the battle!</b></font>"));
                gotEnd = true;
                break;
            }
            case BlankMessage: {
                // XXX This prints out a lot of extra space
                // writeToHist("\n");
                break;
            }
            case PointEstimate: {
                byte first = msg.readByte();
                byte second = msg.readByte();

                writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue + "Variation:</b></font> " +
                        first + ", " + second));
                break;
            }
            case Clause: {
                if (num >= 0 && num < Clauses.values().length) {
                    writeToHist("\n" + Clauses.values()[num].battleText());
                } else {
                    Log.e(TAG, "Invalid Clause:" + num);
                }
                break;
            }
            case Rated: {
                boolean rated = msg.readBool();
                writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue + "Rule:</b></font> " +
                        (rated ? "Rated" : "Unrated")));
                for (int i = 0; i < Clauses.values().length; i++) {
                    if ((conf.clauses & (1 << i)) > 0) {
                        writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue + "Rule: </b></font>" +
                                Clauses.values()[i]));
                    }
                }
                break;
            }
            case TierSection: {
                String tier = msg.readString();
                writeToHist(Html.fromHtml("<br><b><font color =" + QtColor.Blue +
                        "Tier: " + tier + "</b></font>"));
                break;
            }
            case TempPokeChange: {
                byte id = msg.readByte();

                switch (TempPokeChange.values()[id]) {
                    case TempSprite:
                        UniqueID sprite = new UniqueID(msg);
                        if (sprite.pokeNum != 0)
                            pokes[player][slot].specialSprites.addFirst(sprite);
                        else
                            pokes[player][slot].specialSprites.removeFirst();
                        if (activity != null) {
                            //activity.samePokes[player][slot] = false;
                            activity.updatePokes(player, slot);
                        }
                        break;
                    case DefiniteForme: {
                        byte poke = msg.readByte();
                        UniqueID newForm = new UniqueID(msg);
                        pokes[player][slot].uID = newForm;
                        pokes[player][slot].setStats(conf.gen.num);
                        if (isOut(slot)) {
                            pokes[player][slot].uID = newForm;
                            if (activity != null) {
                                //activity.samePokes[player][slot] = false;
                                activity.updatePokes(player, slot);
                            }
                        }
                        break;
                    }
                    case AestheticForme:
                        short newForm = msg.readShort();
                        pokes[player][slot].uID.subNum = (byte) newForm;
                        if (activity != null) {
                            //activity.samePokes[player][slot] = false;
                            activity.updatePokes(player, slot);
                        }
                    default:
                        break;
                }
                break;
            }
            case ClockStart: {
                remainingTime[player % 2] = msg.readShort();
                startingTime[player % 2] = SystemClock.uptimeMillis();
                ticking[player % 2] = true;
                break;
            }
            case ClockStop: {
                remainingTime[player % 2] = msg.readShort();
                ticking[player % 2] = false;
                break;
            }
            case ChangeHp: {
                short newHP = msg.readShort();
                pokes[player][slot].lastKnownPercent = pokes[player][slot].lifePercent;
                pokes[player][slot].lifePercent = (byte) newHP;

                if (activity != null) {
                    // Block until the hp animation has finished
                    // Timeout after 10s
                    try {
                        synchronized (this) {
                            int change = pokes[player][slot].lastKnownPercent - pokes[player][slot].lifePercent;
                            activity.animateHpBarTo(player, slot, pokes[player][slot].lifePercent, change);
                            if (change < 0) change = -change;
                            if (change > 100) change = 100;
                            if (!baked) wait(5000);
                            else wait(change * 43);
                        }
                    } catch (InterruptedException e) {}
                }
                break;
            }
            case SpotShifts: {
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
            }
            case DynamicInfo: {
                dynamicInfo[player] = new BattleDynamicInfo(msg);
                break;
            }
            case UsePP: {
                short move = msg.readShort();
                byte usedpp = msg.readByte();

                if (player == opp) {
                    activity.updateMoves(player, slot, move, usedpp);
                } else if (activity.isSpectating()) {
                    activity.updateMoves(player, slot, move, usedpp);
                }
                break;
            }
            case Notice: {
                String rule = msg.readString();
                String message = msg.readString();
                writeToHist(Html.fromHtml("<strong><font color='blue'>" + rule + "</font></strong>: " + message));
                break;
            }
            case HtmlMessage: {
                String message = msg.readString();
                writeToHist(Html.fromHtml(message));
                break;
            }
            case TerrainMessage: {
                byte tstatus = msg.readByte(), terrain = msg.readByte();
                if (terrain == Terrain.NoTerrain.ordinal())
                    break;
                String color = new TypeForTerrainColor(terrain).toString();
                String message = "";
                switch (TerrainState.values()[tstatus]) {
                    case EndTerrain:
                        switch (Terrain.values()[terrain]) {
                            case Electric:
                                message = getString(R.string.electric_terrain_end);
                                break;
                            case Grassy:
                                message = getString(R.string.grassy_terrain_end);
                                break;
                            case Misty:
                                message = getString(R.string.misty_terrain_end);
                                break;
                            case Psychic:
                                message = getString(R.string.psychic_terrain_end);
                                break;
                            default:
                                message = "";
                        }
                        break;
                }
                writeToHist(Html.fromHtml("<br><font color=" + color + message + "</font>"));
                break;
            }
            default: {
                Log.e(TAG, "Battle command unimplemented -- " + bc);
                break;
            }
        }
    }

    private void onPlayerDisconnect(boolean come, boolean player) {
        if (!come) {
            if (containsClause(Clauses.NoTimeOut.ordinal())) {
                writeToHist(Html.fromHtml("<br/><font color=" + QtColor.DarkBlue + players[player ? me : opp].nick() + getString(R.string.disconnected_no_timeout) + "</font>"));
            } else {
                writeToHist(Html.fromHtml("<br/><font color=" + QtColor.DarkBlue + players[player ? me : opp].nick() + getString(R.string.disconnected_timeout) + "</font>"));
            }
        } else {
            writeToHist(Html.fromHtml("<br/><font color=" + QtColor.DarkBlue + players[player ? me : opp].nick() + getString(R.string.logged_back_in) + "</font>"));
        }
    }

    private boolean containsClause(int clause) {
        for (int i = 0; i < Clauses.values().length; i++) {
            if ((conf.clauses & (1 << i)) > 0) {
                if (Clauses.values()[i].ordinal() == clause) return true;
            }
        }
        return false;
    }

    private SparseArray<String> spectators = new SparseArray<String>();

    private void addSpectator(int id, String name) {
        spectators.put(id, name);
        writeToHist(Html.fromHtml("<br/><font color=" + QtColor.DarkGreen + name + " is watching the battle</font>"));
    }

    private void removeSpectator(int id) {
        writeToHist(Html.fromHtml("<br/><font color=" + QtColor.DarkGreen + spectators.get(id) + " left the battle</font>"));
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

    private String getString(int id) {
        if (netServ != null) {
            return netServ.getString(id);
        } else if (activity != null) {
            return activity.getString(id);
        } else {
            return "";
        }
    }
}
