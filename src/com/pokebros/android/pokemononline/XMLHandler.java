package com.pokebros.android.pokemononline;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.widget.Toast;

public class XMLHandler extends DefaultHandler {
	private boolean inTrainer = false;
	private boolean inMove = false;
	private boolean inDV = false;
	private boolean inEV = false;
	private int numPoke = 0;
	private int numMove = 0;
	private int numEV = 0;
	private int numDV = 0;
	private XMLDataSet myParsedTeam = new XMLDataSet();

	public XMLDataSet getParsedData() {
		return this.myParsedTeam;
	}

	@Override
	public void startDocument() throws SAXException {
		this.myParsedTeam = new XMLDataSet();
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("Team")) {
			String gen = atts.getValue("gen");
			byte g = (byte)(Integer.parseInt(gen));
			myParsedTeam.setGen(g);
			String DefaultTier = atts.getValue("defaultTier");
			myParsedTeam.setDefaultTier(DefaultTier);
		}
		else if (localName.equals("Trainer")) {
			inTrainer = true;
			String loseMsg = atts.getValue("loseMsg");
			myParsedTeam.setLoseMsg(loseMsg);
			String avatar = atts.getValue("avatar");
			short a = (short) (Integer.parseInt(avatar));
			myParsedTeam.setAvatar(a);
			String winMsg = atts.getValue("winMsg");
			myParsedTeam.setWinMsg(winMsg);
			String infoMsg = atts.getValue("infoMsg");
			myParsedTeam.setInfo(infoMsg);
		}
		else if (localName.equals("Pokemon")) {
			myParsedTeam.setPokes(numPoke, 0, atts.getValue("Num"));
			myParsedTeam.setPokes(numPoke, 1, atts.getValue("Forme"));
			myParsedTeam.setPokes(numPoke, 2, atts.getValue("Nickname"));
			myParsedTeam.setPokes(numPoke, 3, atts.getValue("Item"));
			myParsedTeam.setPokes(numPoke, 4, atts.getValue("Ability"));
			myParsedTeam.setPokes(numPoke, 5, atts.getValue("Nature"));
			myParsedTeam.setPokes(numPoke, 6, atts.getValue("Gender"));
			myParsedTeam.setPokes(numPoke, 7, atts.getValue("Shiny"));
			myParsedTeam.setPokes(numPoke, 8, atts.getValue("Happiness"));
			myParsedTeam.setPokes(numPoke, 9, atts.getValue("Lvl"));
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
		if (localName.equals("Trainer")) {
			inTrainer = false;
		}
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
		if (inTrainer) {
			myParsedTeam.setNick(new String(ch, start, length));
		}
		else if (inMove) {
			myParsedTeam.setMoves(numPoke, numMove, Integer.parseInt(new String(ch, start, length)));
		}
		else if (inDV) {
			myParsedTeam.setDVs(numPoke, numDV, (byte)(Integer.parseInt(new String(ch, start, length))));
		}
		else if (inEV) {
			myParsedTeam.setEVs(numPoke, numEV, (byte)(Integer.parseInt(new String(ch, start, length))));
		}
	}
}