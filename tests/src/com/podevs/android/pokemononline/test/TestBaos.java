package com.podevs.android.poAndroid.test;

import android.test.AndroidTestCase;

import com.podevs.android.poAndroid.player.PlayerProfile;
import com.podevs.android.poAndroid.player.PlayerProfile.TrainerInfo;
import com.podevs.android.utilities.Bais;
import com.podevs.android.utilities.Baos;

/* Run while in this file to run the JUnit test instead of the application */
public class TestBaos extends AndroidTestCase {	
	public void testPutFlags() {
		/* The flags we are testing */
		boolean flags[] = new boolean[] {true,true,false,true,
				false,false,true,false,false,true,false};
		
		Baos output = new Baos();
		output.putFlags(flags);
		Bais input = new Bais(output.toByteArray());
		Bais read = input.readFlags();
		
		boolean result[] = new boolean[flags.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = read.available() != 0 && read.readBool();
		}
		
		for (int i = 0; i < flags.length; i++)
		    assertEquals("mismatch at " + i, flags[i], result[i]);
	}
	
	public void testVersionControl() {
		PlayerProfile p = new PlayerProfile();
		TrainerInfo trainerInfo = p.new TrainerInfo(75, "test info", "i won", "i lost", "i tied");
		
		Baos o2 = new Baos();
		o2.putBaos(trainerInfo);
		
		TrainerInfo income = p.new TrainerInfo(new Bais(o2.toByteArray()));
		
		assertEquals(trainerInfo.avatar, income.avatar);
		assertEquals(trainerInfo.info, income.info);
		assertEquals(trainerInfo.winMsg, income.winMsg);
		assertEquals(trainerInfo.loseMsg, income.loseMsg);
		assertEquals(trainerInfo.tieMsg, income.tieMsg);
	}
}
