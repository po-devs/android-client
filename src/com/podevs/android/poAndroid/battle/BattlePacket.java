package com.podevs.android.poAndroid.battle;

import com.podevs.android.utilities.Bais;

public class BattlePacket {
    public BattleCommand bc;
    public byte num;
    public Bais msg;

    BattlePacket(BattleCommand bc, byte num, Bais msg) {
        this.bc = bc;
        this.num = num;
        this.msg = msg;
    }

    public String toCompactString() {
        return "Packet{" +
                "bc=" + bc.toString() +
                ", num=" + num +
                ", msg=" + msg.toBase64() +
                '}';
    }

    @Override
    public String toString() {
        return Debugger.readablePacket(this);
    }
}