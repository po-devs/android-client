package com.podevs.android.poAndroid.battle;

public enum BattleCommand {
    SendOut, // 0
    SendBack, // 1
    UseAttack, // 2
    OfferChoice, // 3
    BeginTurn, // 4
    ChangePP, // 5
    ChangeHp, // 6
    Ko, // 7
    Effective, /* to tell how a move is effective 8 */
    Miss, // 9
    CriticalHit, // = 10,
    Hit, /* for moves like fury double kick etc. */ // 11
    StatChange, // 12
    StatusChange, // 13
    StatusMessage, // 14
    Failed, // 15
    BattleChat, // 16
    MoveMessage, // 17
    ItemMessage, // 18
    NoOpponent, // 19
    Flinch, // = 20,
    Recoil, // 21
    WeatherMessage, // 22
    StraightDamage, // 23
    AbilityMessage, // 24
    AbsStatusChange, // 25
    Substitute, // 26
    BattleEnd, // 27
    BlankMessage, // 28
    CancelMove, // 29
    Clause, // = 30,
    DynamicInfo, // = 31,
    DynamicStats, // = 32,
    Spectating, // 33
    SpectatorChat, // 34
    AlreadyStatusMessage, // 35
    TempPokeChange, // 36
    ClockStart, // = 37,
    ClockStop, // = 38,
    Rated, // 39
    TierSection, // = 40,
    EndMessage, // = 41
    PointEstimate, // = 42
    MakeYourChoice, // = 43
    Avoid, // = 44
    RearrangeTeam, // 45
    SpotShifts, // 46
    ChoiceMade, // 47
    UseItem, // 48
    ItemCountChange, // 49
    CappedStat, // 50
    UsePP, // 51
    Notice, // 52
    HtmlMessage, // 53
    TerrainMessage //54
}
