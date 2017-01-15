package com.podevs.android.poAndroid.poke;

import com.podevs.android.poAndroid.pokeinfo.GenInfo;
import com.podevs.android.poAndroid.pokeinfo.PokemonInfo;
import com.podevs.android.utilities.StringUtilities;
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
		myParsedTeam = new Team();
	}

	@Override
	public void endDocument() throws SAXException {
		myParsedTeam.runCheck();
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals("Team")) {
			myParsedTeam.gen.num = (byte)Integer.parseInt(StringUtilities.def(atts.getValue("gen"), String.valueOf(GenInfo.genMax())));
			myParsedTeam.gen.subNum = (byte)Integer.parseInt(StringUtilities.def(atts.getValue("subgen"), String.valueOf((int)GenInfo.lastGen().subNum)));
			myParsedTeam.defaultTier = StringUtilities.def(atts.getValue("defaultTier"), "");
			
			for (int i = 0; i < 6; i++) {
				myParsedTeam.pokes[i].gen = myParsedTeam.gen;
			}
		} else if (localName.equals("Pokemon")) {
			TeamPoke poke = myParsedTeam.pokes[numPoke];
			
			poke.uID = new UniqueID(Integer.parseInt(StringUtilities.def(atts.getValue("Num"), "0")), 
					Integer.parseInt(StringUtilities.def(atts.getValue("Forme"), "0")));
			poke.nick = StringUtilities.def(atts.getValue("Nickname"), PokemonInfo.name(poke.uID));
			poke.item = (short)Integer.parseInt(StringUtilities.def(atts.getValue("Item"), "0"));
			
			poke.ability = (short) Integer.parseInt(StringUtilities.def(atts.getValue("Ability"), "0"));
			poke.nature = (byte) Integer.parseInt(StringUtilities.def(atts.getValue("Nature"), "0"));
			poke.hiddenPowerType = (byte) Integer.parseInt(StringUtilities.def(atts.getValue("HiddenPower"), "16"));
			poke.gender = (byte) Integer.parseInt(StringUtilities.def(atts.getValue("Gender"), "0"));
			poke.shiny = Integer.parseInt(StringUtilities.def(atts.getValue("Shiny"), "0")) != 0;
			poke.happiness = (byte) Integer.parseInt(StringUtilities.def(atts.getValue("Happiness"), "0"));
			poke.level = (byte) Integer.parseInt(StringUtilities.def(atts.getValue("Lvl"), "0"));
            poke.isHackmon = Integer.parseInt(StringUtilities.def(atts.getValue("Hackmon"), "0")) != 0;
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
			myParsedTeam.pokes[numPoke].moves[numMove].num = (short)Integer.parseInt(new String(ch, start, length));
		}
		else if (inDV) {
			myParsedTeam.pokes[numPoke].DVs[numDV] = (byte)(Integer.parseInt(new String(ch, start, length)));
		}
		else if (inEV) {
			myParsedTeam.pokes[numPoke].EVs[numEV] = (byte)(Integer.parseInt(new String(ch, start, length)));
		}
	}
}