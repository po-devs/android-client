package com.podevs.android.poAndroid.poke;

import android.content.Context;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;
import com.podevs.android.utilities.SerializeBytes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Team implements SerializeBytes {
	public Gen gen = new Gen();

	public String defaultTier = "";
	protected TeamPoke[] pokes = new TeamPoke[6];
	
	/* Used internally only */
	public Team(Bais msg) {
		Bais b = new Bais(msg.readVersionControlData());
		int version = (int) b.read();
		
		if (version == 0) {
			defaultTier = b.readBool() ? b.readString() : "";
			gen = new Gen(b);
			for(int i = 0; i < 6; i++) {
				pokes[i] = new TeamPoke(b, gen);
			}
		}
	}

	public void setGen(Gen g) {
		if (gen.equals(g)) {
			return;
		}

		gen = g;

		for (int i = 0; i < 6; i++) {
			pokes[i].setGen(g);
		}
	}

	public void runCheck() {
		for (TeamPoke poke : pokes) {
			poke.runCheck();
		}
	}
	
	public Team() {
		for (int i = 0; i < 6; i++)
			pokes[i] = new TeamPoke();
	}
	
	public TeamPoke poke(int i) {
		return pokes[i];
	}

	public void setPoke(int slot, TeamPoke poke) {
		pokes[slot] = poke;
		poke.setGen(gen);
	}
	
	public void serializeBytes(Baos bytes) {
		Baos b = new Baos();
		
		b.write(defaultTier.length() > 0 ? 1 : 0);
		if (defaultTier.length() > 0) {
			b.putString(defaultTier);
		}
		
		b.putBaos(gen);
		for(int i = 0; i < 6; i++)
			b.putBaos(pokes[i]);
		
		bytes.putVersionControl(0, b);
	}

	public boolean isValid() {
		return poke(0).uID().pokeNum != 0;
	}

	public void save(Context c) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
 
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Team");
		doc.appendChild(rootElement);
		
		if (defaultTier.length() > 0) {
			rootElement.setAttribute("defaultTier", defaultTier);
		}
		rootElement.setAttribute("gen", String.valueOf(gen.num));
		rootElement.setAttribute("subgen", String.valueOf(gen.subNum));
		
		for (int i = 0; i < 6; i++) {
			Element poke = doc.createElement("Pokemon");
			this.poke(i).save(doc, poke);
			rootElement.appendChild(poke);
		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result;
  //      StringWriter writer = new StringWriter();
   //     StreamResult result1 = new StreamResult(writer);
		try {
			result = new StreamResult(c.openFileOutput(c.getSharedPreferences("team", 0).getString("file", "team.xml"), Context.MODE_PRIVATE));
        } catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

        try {
			transformer.transform(source, result);
      //      transformer.transform(source, result1);
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		try {
			result.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        /* Bootleg
        try {
            String file = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + c.getSharedPreferences("team", 0).getString("file", "team.xml");
            BufferedWriter bos = new BufferedWriter(new FileWriter(file));
            bos.write(writer.toString());
            bos.flush();
            bos.close();
        } catch (Exception e) {
            return;
        }
        */
    }
}
