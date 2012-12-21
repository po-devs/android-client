package com.pokebros.android.pokemononline;

public class XMLDataSet {
	String nick, info, loseMsg, winMsg, defaultTier;
	short avatar;
	boolean ladderEnabled, showTeam;
	byte gen;
	short pokeNum;
	byte subNum;
	String pokenick;
	short item, ability;
	byte nature, gender, pokeGen;
	boolean shiny;
	byte happiness, level;
	private String[][] myTeamPokes = new String[6][10];
	private int[][] myMoves = new int[6][4];
	private byte[][] myDVs = new byte[6][6];
	private byte[][] myEVs = new byte[6][6];
	
	public String getNick() {return nick;}
	public String getInfo() {return info;}
	public String getLoseMsg() {return loseMsg;}
	public String getWinMsg() {return winMsg;}
	public String getDefaultTier() {return defaultTier;}
	public short getAvatar() {return avatar;}
	public boolean getLadderEnabled() {return ladderEnabled;}
	public boolean getShowTeam() {return showTeam;}
	public byte getGen() {return gen;}
	public short pokeNum() {return pokeNum;}
	public byte subNum() {return subNum;}
	public String getPokeNick() {return pokenick;}
	public short getItem() {return item;}
	public short getAbility() {return ability;}
	public byte getNature() {return nature;}
	public byte getGender() {return gender;}
	public byte getPokeGen() {return pokeGen;}
	public boolean getShiny() {return shiny;}
	public byte getHappiness() {return happiness;}
	public byte getLevel() {return level;}
	public String getTeamPokes(int i, int j) {return myTeamPokes[i][j];}
	public int getMoves(int i, int j) {return myMoves[i][j];}
	public byte getDVs(int i, int j) {return myDVs[i][j];}
	public byte getEVs(int i, int j) {return myEVs[i][j];}
	
	public void setNick(String s) {nick = s;}
	public void setInfo(String s) {info = s;}
	public void setLoseMsg(String s) {loseMsg = s;}
	public void setWinMsg(String s) {winMsg = s;}
	public void setDefaultTier(String s) {defaultTier = s;}
	public void setAvatar(short s) {avatar = s;}
	public void setLadderEnabled(boolean b) {ladderEnabled = b;}
	public void setShowTeam(boolean b) {showTeam = b;}
	public void setGen(byte b) {gen = b;}
	public void setPokeNum(short s) {pokeNum = s;}
	public void setSubNum(byte b) {subNum = b;}
	public void setPokeNick(String s) {pokenick = s;}
	public void setItem(short s) {item = s;}
	public void setAbility(short s) {ability = s;}
	public void setNature(byte b) {nature = b;}
	public void setGender(byte b) {gender = b;}
	public void setPokeGen(byte b) {pokeGen = b;}
	public void setShiny(boolean b) {shiny = b;}
	public void setHappiness(byte b) {happiness = b;}
	public void setLevel(byte b) {level = b;}
	public void setPokes(int i, int j, String s) {
		myTeamPokes[i][j] = s;
	}
	public void setMoves(int i, int j, int m) {
		myMoves[i][j] = m;
	}	
	public void setDVs(int i, int j, byte d) {
		myDVs[i][j] = d;
	}	
	public void setEVs(int i, int j, byte e) {
		myEVs[i][j] = e;
	}
}