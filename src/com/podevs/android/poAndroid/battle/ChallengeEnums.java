package com.podevs.android.poAndroid.battle;

public class ChallengeEnums {
	public enum ChallengeDesc
	{
		Sent,
		Accepted,
		Cancelled,
		Busy,
		Refused,
		InvalidTeam,
		InvalidGen,
		InvalidTier,

		ChallengeDescLast
	};

	public enum Clauses
	{
		SleepClause { public final int mask() { return 1; }
		public final String toString() { return "Sleep Clause"; }
		public final String battleText() {return "Sleep Clause prevented the sleep inducing effect of the move from working."; } },
		FreezeClause { public final int mask() { return 2; }
		public final String toString() { return "Freeze Clause"; }
		public final String battleText() { return "Freeze Clause prevented the freezing effect of the move from working."; } },
		DisallowSpectator { public final int mask() { return 4; }
		public final String toString() { return "Disallow Spects"; }
		public final String battleText() { return ""; } },
		ItemClause { public final int mask() { return 8; }
		public final String toString() { return "Item Clause"; }
		public final String battleText() { return ""; } },
		ChallengeCup { public final int mask() { return 16; }
		public final String toString() { return "Challenge Cup"; }
		public final String battleText() { return ""; } },
		NoTimeOut { public final int mask() { return 32; }
		public final String toString() { return "No Timeout"; }
		public final String battleText() { return "The battle ended by timeout."; } },
		SpeciesClause { public final int mask() { return 64; }
		public final String toString() { return "Species Clause"; }
		public final String battleText() { return ""; } },
		RearrangeTeams { public final int mask() { return 128; }
		public final String toString() { return "Team Preview"; }
		public final String battleText() { return ""; } },
		SelfKO { public final int mask() { return 256; }
		public final String toString() { return "Self-KO Clause"; }
		public final String battleText() { return "The Self-KO Clause acted as a tiebreaker."; } },
		InvertedBattle { public final int mask() { return 512; }
		public final String toString() { return "Inverted Battle"; }
		public final String battleText() { return ""; } };

		public abstract int mask();
		public abstract String battleText();
		};

		public enum Mode
		{
			Singles { public final String toString() { return "Singles"; }
			public final int numberOfSlots() { return 1; } },
			Doubles { public final String toString() { return "Doubles"; }
			public final int numberOfSlots() { return 2; } },
			Triples { public final String toString() { return "Triples"; }
			public final int numberOfSlots() { return 3; } },
			Rotation { public final String toString() { return "Rotation"; }
			public final int numberOfSlots() { return 3; } };

        public abstract int numberOfSlots();
		};

	public static String clausesToStringHtml(int clauses) {
		String s = "";
		for (int i = 0; i < Clauses.values().length; i++) {
			//String clauseNumber = Integer.toBinaryString(1 << i);
			//String clausesID = Integer.toBinaryString(clauses);
			//Log.e("Clauses", "" + clauseNumber);
			//Log.e("Clauses", "" + clausesID);
			if ((clauses & (1 << i)) > 0) {
				//Log.e("Clauses", "true");
				s += "<br />" + Clauses.values()[i].toString();
			}
		}
		return s;
	}
}
