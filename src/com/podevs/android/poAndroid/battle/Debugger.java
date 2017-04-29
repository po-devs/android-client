package com.podevs.android.poAndroid.battle;

import com.podevs.android.poAndroid.poke.PokeEnums;
import com.podevs.android.poAndroid.poke.PokeEnums.StatusFeeling;
import com.podevs.android.poAndroid.poke.PokeEnums.Weather;
import com.podevs.android.poAndroid.poke.PokeEnums.WeatherState;
import com.podevs.android.poAndroid.poke.PokeEnums.Terrain;
import com.podevs.android.poAndroid.poke.PokeEnums.TerrainState;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.GenInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.utilities.ArrayUtilities;
import com.podevs.android.utilities.Bais;

public class Debugger {
    // TODO: BE VERY SAFE

    private final static byte me = 0, opp = 1;
    private final static String sp = "   ";
    private final static String error = "Error:";

    private static byte player(byte num) {
        if ((num % 2) == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    private static byte slot(byte num, byte player) {
        if (player == me) {
            return (byte) (num / 2);
        } else {
            return (byte) ((num - 1) / 2);
        }
    }

    public static String readablePacket(BattlePacket p) {
        String readable = "";
        Bais msg = p.msg.cloneRemaining();
        byte player = player(p.num);
        byte slot = slot(p.num, player);
        readable = p.bc.toString();
        switch (p.bc) {
            case SendOut: {
                boolean silent = msg.readBool();
                byte fromSpot = msg.readByte();

                readable += sp + playerSlot(player, slot) + " {silent:" + silent + " from:" + fromSpot + "}";

                Bais copy = msg.cloneRemaining();
                try {
                    ShallowBattlePoke poke = new ShallowBattlePoke(msg, (player == me), GenInfo.lastGen());
                    poke.pokeName = PokemonInfo.name(poke.uID);
                    readable += " " + poke.toString();
                } catch (Exception e) {
                    readable += " " + error + copy.toString();
                }
                break;
            }
            case SendBack: {
                boolean silent = msg.readBool();
                readable += sp + silent;
                break;
            }
            case UseAttack: {
                short attack = msg.readShort();
                boolean silent = msg.readBool();
                readable += sp + "{attack:" + attack + " silent:" + silent + "}";
                break;
            }
            case BeginTurn: {
                int turn = msg.readInt();
                readable += sp + turn;
                break;
            }
            case Ko: {
                readable += sp + playerSlot(player, slot);
                break;
            }
            case Hit: {
                byte number = msg.readByte();
                readable += sp + number;
                break;
            }
            case Effective: {
                byte eff = msg.readByte();
                readable += sp + eff;
                break;
            }
            case CriticalHit: {
                readable += sp + playerSlot(player, slot);
                break;
            }
            case Miss: {
                readable += sp + playerSlot(player, slot);
                break;
            }
            case Avoid: {
                readable += sp + playerSlot(player, slot);
                break;
            }
            case StatChange: {
                byte stat = msg.readByte(), boost = msg.readByte();
                boolean silent = msg.readBool();
                readable += sp + "{stat:" + stat + " boost:" + boost + " silent:" + silent + "}";
                break;
            }
            case CappedStat: {
                byte stat = msg.readByte();
                boolean max = msg.readBool();
                readable += sp + "{stat:" + stat + " max:" + max + "}";
                break;
            }
            case UseItem: {
                byte item = msg.readByte();
                readable += sp + item;
                break;
            }
            case ItemCountChange: {
                byte item = msg.readByte();
                byte count = msg.readByte();
                readable += sp + "{item:" + item + " count:" + count + "}";
                break;
            }
            case StatusChange: {
                byte status = msg.readByte();
                boolean multipleTurns = msg.readBool();
                boolean silent = msg.readBool();
                readable += sp + "{status:" + status + " mult:" + multipleTurns + " silent:" + silent + "}";
                break;
            }
            case AbsStatusChange: {
                byte poke = msg.readByte();
                byte status = msg.readByte();
                readable += sp + "{player:" + player + " poke:" + poke + " count:" + status + "}";
                break;
            }
            case AlreadyStatusMessage: {
                byte status = msg.readByte();
                readable += sp + playerSlot(player, slot) + sp + status;
                break;
            }
            case StatusMessage: {
                byte status = msg.readByte();
                StatusFeeling feeling = StatusFeeling.values()[status];
                readable += sp + feeling.toString();
                break;
            }
            case Failed: {
                boolean silent = msg.readBool();
                readable += sp + silent;
                break;
            }
            case BattleChat: {
                break;
            }
            case EndMessage: {
                String message = msg.readString();
                if (message == null || message.equals(""))
                    break;
                readable += sp + message;
                break;
            }
            case Spectating: {
                boolean come = msg.readBool();
                int id = msg.readInt();
                String name = msg.readString();
                readable += sp + "{come:" + come + " id:" + id + " name:" + name + "}";
                break;
            }
            case SpectatorChat: {
                int id = msg.readInt();
                String message = msg.readString();
                readable += sp + "{id:" + id + " msg:" + message + "}";
                break;
            }
            case MoveMessage: {
                short move = msg.readShort();
                byte part = msg.readByte();
                byte type = msg.readByte();
                byte foe = msg.readByte();
                short other = msg.readShort();
                String q = msg.readString();

                readable += sp + "{move:" + move + " part:" + part + " type:" + type +" foe:" + foe + " other:" + other + " q:" + q + "}";
//                s = s.replaceAll("%s", playerSlot(player, slot));
//                s = s.replaceAll("%ts", "{p:" +player + " name}");
//                s = s.replaceAll("%tf", "{p:" +(player == 0 ? 1 : 0) + " name}");
//                if (type != -1) s = s.replaceAll("%t", TypeInfo.Type.values()[type].toString());
//                if (foe != -1) s = s.replaceAll("%f", playerSlot(foe, slot));
//                if (other != -1 && s.contains("%m")) s = s.replaceAll("%m", "{move:" + Short.toString(other) + "}");
//                s = s.replaceAll("%d", Short.toString(other));
//                s = s.replaceAll("%q", q);
//                if (other != -1 && s.contains("%i")) s = s.replaceAll("%i", "{item:" + Short.toString(other) + "}");
//                if (other != -1 && s.contains("%a")) s = s.replaceAll("%a", "{ability:" + Short.toString(other) + "}");
//                if (other != -1 && s.contains("%p")) s = s.replaceAll("%p", "{poke:" + (new UniqueID(other, 0).toString()) + "}");
                break;
            }
            case NoOpponent: {
                break;
            }
            case ItemMessage: {
                short item = msg.readShort();
                byte part = msg.readByte();
                byte foe = msg.readByte();
                short berry = msg.readShort();
                int other = msg.readInt();
                readable += sp + "{item:" + item + " part:" + part + " foe:" + foe + " berry:" + berry + " other:" + other + "}";
//                if (other != -1 && s.contains("%st"))
//                    s = s.replaceAll("%st", PokeEnums.Stat.values()[other].toString());
//                s = s.replaceAll("%s", playerSlot(player, slot));
//                if (foe != -1) s = s.replaceAll("%f", playerSlot(foe, slot));
//                if (berry != -1) s = s.replaceAll("%i", "{berry:" + Integer.toString(other) + "}");
//                if (other != -1 && s.contains("%m")) s = s.replaceAll("%m", "{move:" + Integer.toString(other) + "}");
//                if (other != -1 && s.contains("%p")) s = s.replaceAll("%p", "{poke:" + (new UniqueID(other, 0).toString()) + "}");
                break;
            }
            case Flinch: {
                readable += sp + playerSlot(player, slot);
                break;
            }
            case Recoil: {
                boolean damaging = msg.readBool();
                readable += sp + damaging;
                break;
            }
            case WeatherMessage: {
                byte wstatus = msg.readByte(), weather = msg.readByte();
                if (weather == PokeEnums.Weather.NormalWeather.ordinal()) break;
                readable += sp + WeatherState.values()[wstatus].toString();
                readable += sp + Weather.values()[weather];
                break;
            }
            case StraightDamage: {
                short damage = msg.readShort();
                readable += sp + damage;
                break;
            }
            case AbilityMessage: {
                short ab = msg.readShort();
                byte part = msg.readByte();
                byte type = msg.readByte();
                byte foe = msg.readByte();
                short other = msg.readShort();

                readable += sp + "{ability:" + ab + " part:" + part + " type:" + type +" foe:" + foe + " other:" + other + "}";
//                if (other != -1 && s.contains("%st"))
//                    s = s.replaceAll("%st", "{stat:" + other + "}");
//                s = s.replaceAll("%s", playerSlot(player, slot));
//                s = s.replaceAll("%tf", "{p:" +(player == 0 ? 1 : 0) + " name}");
//                if (type != -1) s = s.replaceAll("%t", TypeInfo.Type.values()[type].toString());
//                if (foe != -1) s = s.replaceAll("%f", playerSlot(foe, slot));
//                if (other != -1 && s.contains("%m")) s = s.replaceAll("%m", "{move:" + Short.toString(other) + "}");
//                if (other != -1 && s.contains("%i")) s = s.replaceAll("%i", "{item:" + Short.toString(other) + "}");
//                if (other != -1 && s.contains("%a")) s = s.replaceAll("%a", "{ability:" + Short.toString(other) + "}");
//                if (other != -1 && s.contains("%p")) s = s.replaceAll("%p", "{poke:" + (new UniqueID(other, 0).toString()) + "}");
                break;
            }
            case Substitute: {
                boolean sub = msg.readBool();
                readable += sp + playerSlot(player, slot);
                readable += sp + sub;
                break;
            }
            case BattleEnd: {
                byte res = msg.readByte();
                readable += sp + res;
                break;
            }
            case BlankMessage: {
                break;
            }
            case PointEstimate: {
                byte first = msg.readByte();
                byte second = msg.readByte();
                readable += sp + first + sp + second;
                break;
            }
            case Clause: {
                readable += sp + p.num;
                break;
            }
            case Rated: {
                boolean rated = msg.readBool();
                readable += sp + rated;
                break;
            }
            case TierSection: {
                String tier = msg.readString();
                readable += sp + tier;
                break;
            }
            case TempPokeChange: {
                byte id = msg.readByte();
                SpectatingBattle.TempPokeChange change = SpectatingBattle.TempPokeChange.values()[id];
                readable += sp + change.toString();
                switch (change) {
                    case TempMove:
                    case DefMove: {
                        byte moveslot = msg.readByte();
                        short move = msg.readShort();
                        readable += sp + "{s:" + moveslot + " m:" + move + "}";
                        break;
                    }
                    case TempPP: {
                        byte moveslot = msg.readByte();
                        byte PP = msg.readByte();
                        readable += sp + "{s:" + moveslot + " p:" + PP + "}";
                        break;
                    }
                    case TempSprite: {
                        UniqueID sprite = new UniqueID(msg);
                        readable += sp + playerSlot(player, slot) + sp + sprite.toString();
                        break;
                    }
                    case DefiniteForme: {
                        //byte poke = msg.readByte();
                        UniqueID newForm = new UniqueID(msg);
                        readable += sp + playerSlot(player, slot) + sp + newForm.toString();
                        break;
                    }
                    case AestheticForme:
                        short newForm = msg.readShort();
                        readable += sp + playerSlot(player, slot) + sp + newForm;
                    default:
                        break;
                }
                break;
            }
            case MakeYourChoice: {
                break;
            } case OfferChoice: {
                byte numSlot = msg.readByte();
                boolean allowSwitch = msg.readBool();
                boolean allowAttack = msg.readBool();
                Boolean[] allowAttacks = new Boolean[4];

                for (int i = 0; i < 4; i++) {
                    allowAttacks[i] = msg.readBool();
                }

                boolean allowMega = msg.readBool();
                readable += sp + "allow{switch:" + allowSwitch + " attack:" + allowAttack + " attacks:" + ArrayUtilities.join(allowAttacks, ",") + " mega:" + allowMega + " slot:" +numSlot + "}";
                break;
            }
            case ClockStart: {
                readable += sp + msg.readShort();
                break;
            }
            case ClockStop: {
                readable += sp + msg.readShort();
                break;
            }
            case ChangeHp: {
                short newHP = msg.readShort();
                readable += sp + playerSlot(player, slot) + sp + newHP;
                break;
            }
            case SpotShifts: {
                // TODO
                break;
            }
            case DynamicInfo: {
                Bais copy = msg.cloneRemaining();
                readable += sp + playerSlot(player, slot);
                try {
                    readable += sp + new BattleDynamicInfo(msg);
                } catch (Exception e) {
                    readable += sp + error + copy.toString();
                }
                break;
            } case DynamicStats: {
                Bais copy = msg.cloneRemaining();
                try {
                    readable += sp + playerSlot(player, slot) + "  {stats:";
                    for (int i = 0; i < 5; i++)
                        readable += msg.readShort() + " ";
                    readable += "}";
                } catch (Exception e) {
                    readable += sp + error + copy.toString();
                }
                break;
            }
            case UsePP: {
                short move = msg.readShort();
                byte usedpp = msg.readByte();
                readable += sp + move + sp + usedpp;
                break;
            }
            case Notice: {
                String rule = msg.readString();
                String message = msg.readString();
                readable += sp + rule + sp + message;
                break;
            }
            case HtmlMessage: {
                String message = msg.readString();
                readable += sp + message;
                break;
            }
            case TerrainMessage: {
                byte tstatus = msg.readByte(), terrain = msg.readByte();
                readable += sp + TerrainState.values()[tstatus].toString();
                readable += sp + Terrain.values()[terrain];
                break;
            }
            default: {
                readable += sp + "Battle command unimplemented";
                break;
            }
        }
        return readable;
    }

    private static String playerSlot(byte player, byte slot) {
        return "{p:" + player + " s:" + slot + "}";
    }
}