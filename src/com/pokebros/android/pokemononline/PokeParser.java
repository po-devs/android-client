package com.pokebros.android.pokemononline;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.ParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.net.Uri;

import com.pokebros.android.pokemononline.player.PlayerTeam;
import com.pokebros.android.pokemononline.poke.TeamPoke;

public class PokeParser extends DefaultHandler
{
	FileInputStream in;
	InputStreamReader isr;
	BufferedReader inRd;
	TeamPoke tp;
	PlayerTeam pt;
	XMLDataSet parsedTeam;
	
	public PokeParser(Context context) {
		try {
			in = context.openFileInput("team.xml");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		isr = new InputStreamReader(in);
		 
		inRd = new BufferedReader(isr);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = null;
		try {
			sp = spf.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		/* Get the XMLReader of the SAXParser we created. */
		XMLReader xr = null;
		try {
			xr = sp.getXMLReader();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		/* Create a new ContentHandler and apply it to the XML-Reader */
		XMLHandler myHandler = new XMLHandler();
		xr.setContentHandler(myHandler);
		
		try {
			xr.parse(new InputSource(in));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		parsedTeam = myHandler.getParsedData();
	}

	public String getNick() {return parsedTeam.getNick();}
	public String getInfo() {return parsedTeam.getInfo();}
	public String getLoseMsg() {return parsedTeam.getLoseMsg();}
	public String getWinMsg() {return parsedTeam.getWinMsg();}
	public short getAvatar() {return parsedTeam.getAvatar();}
	public String getDefaultTier() {return parsedTeam.getDefaultTier();}
	public byte getGen() {return parsedTeam.getGen();}
	public short getPokeNum() {return parsedTeam.pokeNum();}
	public byte getSubNum() {return parsedTeam.subNum();}
	public String getPokeNick() {return parsedTeam.getPokeNick();}
	public short getItem() {return parsedTeam.getItem();}
	public short getAbility() {return parsedTeam.getAbility();}
	public byte getNature() {return parsedTeam.getNature();}
	public byte getGender() {return parsedTeam.getGender();}
	public byte getPokeGen() {return parsedTeam.getPokeGen();}
	public boolean getShiny() {return parsedTeam.getShiny();}
	public byte getHappiness() {return parsedTeam.getHappiness();}
	public byte getLevel() {return parsedTeam.getLevel();}
	public String getTeamPokes(int i, int j) {return parsedTeam.getTeamPokes(i, j);}
	public int getMoves(int i, int j) {return parsedTeam.getMoves(i, j);}
	public byte getDVs(int i, int j) {return parsedTeam.getDVs(i, j);}
	public byte getEVs(int i, int j) {return parsedTeam.getEVs(i, j);}
}