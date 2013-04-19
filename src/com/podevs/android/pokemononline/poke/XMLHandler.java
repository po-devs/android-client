package com.podevs.android.pokemononline.poke;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLHandler extends DefaultHandler {
	private boolean inMove = false;
	private boolean inDV = false;
	private boolean inEV = false;
	private int numPoke = 0;
	private int numMove = 0;
	private int numEV = 0;
	private int numDV = 0;
	private Team myParsedTeam;

	public Team getParsedData() {
		return this.myParsedTeam;
	}

	@Override
	public void startDocument() throws SAXException {
		this.myParsedTeam = new Team();
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("Team")) {
			myParsedTeam.gen.num = (byte)Integer.parseInt(atts.getValue("gen"));
			myParsedTeam.gen.subNum = (byte)Integer.parseInt(atts.getValue("subgen"));
			myParsedTeam.defaultTier = atts.getValue("defaultTier");
			
			for (int i = 0; i < 6; i++) {
				myParsedTeam.pokes[i].gen = myParsedTeam.gen;
			}
		}
//		else if (localName.equals("Trainer")) {
//			inTrainer = true;
//			String loseMsg = atts.getValue("loseMsg");
//			myParsedTeam.setLoseMsg(loseMsg);
//			String avatar = atts.getValue("avatar");
//			short a = (short) (Integer.parseInt(avatar));
//			myParsedTeam.setAvatar(a);
//			String winMsg = atts.getValue("winMsg");
//			myParsedTeam.setWinMsg(winMsg);
//			String infoMsg = atts.getValue("infoMsg");
//			myParsedTeam.setInfo(infoMsg);
//		}
		else if (localName.equals("Pokemon")) {
			TeamPoke poke = myParsedTeam.pokes[numPoke];
			
			poke.uID = new UniqueID(Integer.parseInt(atts.getValue("Num")), Integer.parseInt(atts.getValue("Forme")));
			poke.nick = atts.getValue("Nickname");
			poke.item = (short)Integer.parseInt(atts.getValue("Item"));
			
			poke.ability = (short) Integer.parseInt(atts.getValue("Ability"));
			poke.nature = (byte) Integer.parseInt(atts.getValue("Nature"));
			poke.gender = (byte) Integer.parseInt(atts.getValue("Gender"));
			poke.shiny = Integer.parseInt(atts.getValue("Shiny")) != 0;
			poke.happiness = (byte) Integer.parseInt(atts.getValue("Happiness"));
			poke.level = (byte) Integer.parseInt(atts.getValue("Lvl"));
		}
		else if (localName.equals("Move"))
			inMove = true;
		else if (localName.equals("DV"))
			inDV = true;
		else if (localName.equals("EV"))
			inEV = true;
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("Pokemon")) {
			if (numPoke != 5) numPoke++;
		}
		if (localName.equals("Move")) {
			inMove = false;
			if (numMove != 3) numMove++;
			else {numMove = 0;}
		}
		if (localName.equals("DV")) {
			inDV = false;
			if (numDV != 5) numDV++;
			else {numDV = 0;}
		}
		if (localName.equals("EV")) {
			inEV = false;
			if (numEV != 5) numEV++;
			else {numEV = 0;}
		}
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (inMove) {
			myParsedTeam.pokes[numPoke].moves[numMove] = (short)Integer.parseInt(new String(ch, start, length));
		}
		else if (inDV) {
			myParsedTeam.pokes[numPoke].DVs[numDV] = (byte)(Integer.parseInt(new String(ch, start, length)));
		}
		else if (inEV) {
			myParsedTeam.pokes[numPoke].EVs[numEV] = (byte)(Integer.parseInt(new String(ch, start, length)));
		}
	}
}