package com.podevs.android.poAndroid.battle;

import com.podevs.android.utilities.Bais;

/**
 * Contains basic data about a battle
 * @author coyotte508
 *
 */
public class BattleDesc {
	public int p1;
	public int p2;
	public int mode;
	public String tier;

	public BattleDesc() {
		tier = "";
	}

	public BattleDesc(int p1, int p2) {
		init(p1, p2, 0, "");
	}
	
	public BattleDesc(int p1, int p2, int mode, String tier) {
		init(p1,p2,mode,tier);
	}

	private void init(int p1, int p2, int mode, String tier) {
		this.p1 = p1;
		this.p2 = p2;
		this.mode = mode;
		this.tier = tier;
	}
	
	public int opponent(int p) {
		return p == p2 ? p1 : p2;
	}

	public void read(Bais msg) {
		Bais flags = msg.readFlags();
		mode = msg.readByte();
		p1 = msg.readInt();
		p2 = msg.readInt();
		if (flags.readBool()) tier = msg.readString();
	}

	public void readOld(Bais msg) {
		p1 = msg.readInt();
		p2 = msg.readInt();
	}
}
