package com.podevs.android.poAndroid.player;

import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class UserInfo implements SerializeBytes {

    public static byte FLAG_ONLINE = 1;
    private static byte FLAG_BANNED = 1 << 1;
    private static byte FLAG_MUTED = 1 << 2;
    private static byte FLAG_NONEXISTANT = 1 << 3;
    private static byte FLAG_TEMPBANNED = 16;

    public byte flags;
    public byte auth;
    public String ip;
    public String name;
    public String date;
    public String os;

    public String statusText() {
        String ret;
        if (!exists()) {
            ret = "";
        } else if (banned()) {
            ret = "<font color=#C90000>Banned</font>";
        } else if (tempbanned()) {
            ret = "<font color=#EAA52C>TBanned</font>";
        } else if (online()) {
            ret = "<font color=#0A8815>On</font> - " + (os.length() > 0 ? os : "");
        } else {
            ret = "Offline";
        }

        if (muted()) {
            ret += " ";
            ret += "<font color=#C90000>[Muted]</font>";
        }

        return ret;
    }

    public boolean exists() {
        return !((flags & FLAG_NONEXISTANT) == FLAG_NONEXISTANT);
    }

    public boolean online() {
        return (flags & FLAG_ONLINE) == FLAG_ONLINE;
    }

    public boolean banned() {
        return (flags & FLAG_BANNED) == FLAG_BANNED;
    }

    public boolean muted() {
        return (flags & FLAG_MUTED) == FLAG_MUTED;
    }

    public boolean tempbanned() {
        return (flags & FLAG_TEMPBANNED) == FLAG_TEMPBANNED;
    }

    public UserInfo(Bais bias) {
        flags = bias.readByte();
        auth = bias.readByte();
        ip = bias.readString();
        name = bias.readString();
        date = bias.readString();
        os = bias.readString();
    }

    @Override
    public void serializeBytes(Baos b) {
        b.write(flags);
        b.write(auth);
        b.putString(ip);
        b.putString(name);
        b.putString(date);
        b.putString(os);
    }
}
