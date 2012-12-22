package com.pokebros.android.pokemononline;

import com.pokebros.android.pokemononline.poke.TeamPoke;

public class Team implements SerializeBytes {
	protected byte gen = 5;
	protected TeamPoke[] pokes = new TeamPoke[6];
	
	public Team(Bais msg) {
		gen = msg.readByte();
		for(int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke(msg);
	}
	
	public Team() {
		for (int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke();
	}
	
	public Team(PokeParser p) {
		gen = p.getGen();
		
		String[][] mTP = new String[6][10]; 
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 10; j++) {
				mTP[i][j] = p.getTeamPokes(i, j);
			}
		}
		int[][] mM = new int[6][4]; 
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 4; j++) {
				mM[i][j] = p.getMoves(i, j);
			}
		}
		byte[][] mDV = new byte[6][6]; 
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				mDV[i][j] = p.getDVs(i, j);
			}
		}
		byte[][] mEV = new byte[6][6];
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				mEV[i][j] = p.getEVs(i, j);
			}
		}
		
		for(int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke(mTP, mM, mDV, mEV, i);
	}
	
	public void serializeBytes(Baos bytes) {
		bytes.write(gen);
		for(int i = 0; i < 6; i++)
			bytes.putBaos(pokes[i]);
	}
}
