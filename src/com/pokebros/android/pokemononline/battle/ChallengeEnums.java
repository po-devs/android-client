package com.pokebros.android.pokemononline.battle;

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
						public final String toString() { return "Wifi Battle"; }
						public final String battleText() { return ""; } },
		SelfKO { public final int mask() { return 256; }
						public final String toString() { return "Self-KO Clause"; }
						public final String battleText() { return "The Self-KO Clause acted as a tiebreaker."; } };
		
		public abstract int mask();
		public abstract String battleText();
	};

	public enum Mode
	{
		Singles,
		Doubles,
		Triples,
		Rotation,
	};
}
