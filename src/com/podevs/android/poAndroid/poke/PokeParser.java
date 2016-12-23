package com.podevs.android.poAndroid.poke;

import android.content.Context;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class PokeParser
{
	private Team parsedTeam = new Team();
	
	public PokeParser(Context context, String TextOrFile, boolean isFile) {
		init(context, TextOrFile, isFile);
	}
	
	public PokeParser(Context context) {
		init(context, context.getSharedPreferences("team", 0).getString("file", "team.xml"), true);
	}
	
	private void init(Context context, String TextOrFile, boolean isFile) {

		if (isFile) {
			FileInputStream in;

			try {
				in = context.openFileInput(TextOrFile);
			} catch (FileNotFoundException e) {
				return;
			}

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = null;
			try {
				sp = spf.newSAXParser();
			} catch (ParserConfigurationException|SAXException e) {
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
			} catch (Exception e) {
				e.printStackTrace();
			}

			parsedTeam = myHandler.getParsedData();

			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = null;
			try {
				sp = spf.newSAXParser();
			} catch (ParserConfigurationException|SAXException e) {
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
				xr.parse(new InputSource(new StringReader(TextOrFile)));
			} catch (Exception e) {
				e.printStackTrace();
			}

			parsedTeam = myHandler.getParsedData();
		}
	}

	public Team getTeam() {
		return parsedTeam;
	}
}