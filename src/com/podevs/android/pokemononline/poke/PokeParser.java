package com.podevs.android.pokemononline.poke;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.ParseException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

public class PokeParser extends DefaultHandler
{
	private Team parsedTeam = new Team();
	
	public PokeParser(Context context) {
		FileInputStream in;
		
		try {
			in = context.openFileInput("team.xml");
		} catch (FileNotFoundException e) {
			return;
		}
		 
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
		
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Team getTeam() {
		return parsedTeam;
	}
}