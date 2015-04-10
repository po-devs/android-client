package com.podevs.android.poAndroid.poke;

import com.podevs.android.poAndroid.pokeinfo.MoveInfo;
import org.w3c.dom.Element;

public class TeamMove implements Move {
	short num;
	
	public TeamMove(int i) {
		num = (short)i;
	}

	public int num() {
		return num;
	}
	
	public String toString() {
		return MoveInfo.name(num);
	}

	public void save(Element move) {
		move.setTextContent(String.valueOf(num));
	}
}
