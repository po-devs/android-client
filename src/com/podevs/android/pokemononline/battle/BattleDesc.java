package com.podevs.android.pokemononline.battle;

/**
 * Contains basic data about a battle
 * @author coyotte508
 *
 */
public class BattleDesc {
	public int p1;
	public int p2;
	public int mode;
	
	public BattleDesc(int p1, int p2) {
		init(p1, p2, 0);
	}
	
	public BattleDesc(int p1, int p2, int mode) {
		init(p1,p2,mode);
	}

	private void init(int p1, int p2, int mode) {
		this.p1 = p1;
		this.p2 = p2;
		this.mode = mode;
	}
	
	public int opponent(int p) {
		return p == p2 ? p1 : p2;
	}
}
