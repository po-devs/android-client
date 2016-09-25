package com.podevs.android.poAndroid.player;

import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;

public class UserInfo implements SerializeBytes {

    byte flags;
    byte auth;
    String ip;
    String name;
    String date;
    String os;

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
