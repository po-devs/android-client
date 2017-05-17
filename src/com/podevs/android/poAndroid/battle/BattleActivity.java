package com.podevs.android.poAndroid.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher.DragController;
import com.android.launcher.DragLayer;
import com.android.launcher.PokeDragIcon;
import com.podevs.android.poAndroid.Command;
import com.podevs.android.poAndroid.NetworkService;
import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.TextProgressBar;
import com.podevs.android.poAndroid.chat.ChatActivity;
import com.podevs.android.poAndroid.poke.PokeEnums.Status;
import com.podevs.android.poAndroid.poke.ShallowBattlePoke;
import com.podevs.android.poAndroid.poke.ShallowShownPoke;
import com.podevs.android.poAndroid.poke.UniqueID;
import com.podevs.android.poAndroid.pokeinfo.*;
import com.podevs.android.utilities.Baos;

class MyResultReceiver extends ResultReceiver {
    public static final Creator<MyResultReceiver> CREATOR = null;

    private Receiver mReceiver;

    public MyResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}

@SuppressLint("DefaultLocale")
public class BattleActivity extends FragmentActivity implements MyResultReceiver.Receiver{
    private MyResultReceiver mRecvr;
    private static ComponentName servName = new ComponentName("com.podevs.android.pokemonresources", "com.podevs.android.pokemonresources.SpriteService");

    public enum BattleDialog {
        RearrangeTeam,
        ConfirmForfeit,
        OppDynamicInfo,
        MyDynamicInfo,
        MoveInfo,
        Debug
    }

    // public final static int SWIPE_TIME_THRESHOLD = 100;
    private static final String TAG = "Battle";

    DragLayer mDragLayer;

    ViewPager realViewSwitcher;
    RelativeLayout battleView;
    TextProgressBar[] hpBars = new TextProgressBar[2];
    TextView[] currentPokeNames = new TextView[2];
    TextView[] currentPokeLevels = new TextView[2];
    ImageView[] currentPokeGenders = new ImageView[2];
    ImageView[] currentPokeStatuses = new ImageView[2];

    TextView[] attackNames = new TextView[4];
    TextView[] attackPPs = new TextView[4];
    RelativeLayout[] attackLayouts = new RelativeLayout[4];

    TextView[] timers = new TextView[2];

    PokeDragIcon[] myArrangePokeIcons = new PokeDragIcon[6];
    ImageView[] oppArrangePokeIcons = new ImageView[6];

    ListedPokemon pokeList[] = new ListedPokemon[6];

    TextView infoView;
    ScrollView infoScroll;
    TextView[] names = new TextView[2];
    ImageView[][] pokeballs = new ImageView[2][6];
    WebView[][] pokeSprites = new WebView[2][3];

    RelativeLayout struggleLayout;
    LinearLayout attackRow1;
    LinearLayout attackRow2;

    SpectatingBattle battle = null;
    public Battle activeBattle = null;

    boolean useAnimSprites = true;
    boolean megaClicked = false;
    boolean zmoveClicked = false;
    BattleMove lastClickedMove;

    Resources resources;
    public NetworkService netServ = null;
    int me, opp;

    class HpAnimator implements Runnable {
        int i, goal;
        boolean finished;

        public void setGoal(int i, int goal) {
            this.i = i;
            this.goal = goal;
            finished = false;
        }

        public void run() {
            while(goal < hpBars[i].getProgress()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        hpBars[i].incrementProgressBy(-1);
                        hpBars[i].setText(hpBars[i].getProgress() + "%");
                        checkHpColor();
                    }
                });
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                }
            }
            while(goal > hpBars[i].getProgress()) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        hpBars[i].incrementProgressBy(1);
                        hpBars[i].setText(hpBars[i].getProgress() + "%");
                        checkHpColor();
                    }
                });
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                }
            }

            synchronized (battle) {
                battle.notify();
            }
        }

        public void setHpBarToGoal() {
            runOnUiThread(new Runnable() {
                public  void run() {
                    hpBars[i].setProgress(goal);
                    hpBars[i].setText(hpBars[i].getProgress() + "%");
                    checkHpColor();
                }
            });
        }

        void checkHpColor() {
            runOnUiThread(new Runnable() {
                public void run() {
                    int progress = hpBars[i].getProgress();
                    Rect bounds = hpBars[i].getProgressDrawable().getBounds();
                    if(progress > 50)
                        hpBars[i].setProgressDrawable(resources.getDrawable(R.drawable.green_progress));
                    else if(progress <= 50 && progress > 20)
                        hpBars[i].setProgressDrawable(resources.getDrawable(R.drawable.yellow_progress));
                    else
                        hpBars[i].setProgressDrawable(resources.getDrawable(R.drawable.red_progress));
                    hpBars[i].getProgressDrawable().setBounds(bounds);
                    // XXX the hp bars won't display properly unless I do this. Spent many hours trying
                    // to figure out why
                    int increment = (hpBars[i].getProgress() == 100) ? -1 : 1;
                    hpBars[i].incrementProgressBy(increment);
                    hpBars[i].incrementProgressBy(-1 * increment);
                }
            });
        }
    };

    public HpAnimator hpAnimator = new HpAnimator();

    View mainLayout, teamLayout;
    public class MyAdapter extends PagerAdapter
    {
        @Override
        public int getCount() {
            return isSpectating() ? 1 : 2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0: container.addView(mainLayout);return mainLayout;
                case 1: container.addView(teamLayout);return teamLayout;
            }
            return null;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return (Object)arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.w(TAG, "Battle id: " + getIntent().getIntExtra("battleId", -1));
        super.onCreate(savedInstanceState);
        mRecvr = new MyResultReceiver(new Handler());
        mRecvr.setReceiver(this);
        try {
            getPackageManager().getApplicationInfo("com.podevs.android.pokemonresources", 0);
        } catch (NameNotFoundException e) {
            Log.d("BattleActivity", "Animated sprites not found");
            useAnimSprites = false;
        }

        bindService(new Intent(BattleActivity.this, NetworkService.class), connection,
                Context.BIND_AUTO_CREATE);

        resources = getResources();
        realViewSwitcher = new ViewPager(this);
        mainLayout = getLayoutInflater().inflate(R.layout.battle_mainscreen, null);

        //if (mainLayout.findViewById(R.id.smallBattleWindow) != null) {
			/* Small screen, set full screen otherwise pokemon are cropped */
          //  requestWindowFeature(Window.FEATURE_NO_TITLE);
        //}

        realViewSwitcher.setAdapter(new MyAdapter());
        setContentView(realViewSwitcher);

        infoView = (TextView)mainLayout.findViewById(R.id.infoWindow);
        infoScroll = (ScrollView)mainLayout.findViewById(R.id.infoScroll);
        battleView = (RelativeLayout)mainLayout.findViewById(R.id.battleScreen);

        struggleLayout = (RelativeLayout)mainLayout.findViewById(R.id.struggleLayout);
        attackRow1 = (LinearLayout)mainLayout.findViewById(R.id.attackRow1);
        attackRow2 = (LinearLayout)mainLayout.findViewById(R.id.attackRow2);

        struggleLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                netServ.socket.sendMessage(activeBattle.constructAttack((byte)-1, megaClicked, zmoveClicked), Command.BattleMessage); // This is how you struggle
            }
        });

        setUncaughtHandler();
    }

    private Handler handler = new Handler();

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            for(int i = 0; i < 2; i++) {
                int seconds;
                if (battle.ticking[i]) {
                    long millis = SystemClock.uptimeMillis()
                            - battle.startingTime[i];
                    seconds = battle.remainingTime[i] - (int) (millis / 1000);
                }
                else
                    seconds = battle.remainingTime[i];

                if(seconds < 0) seconds = 0;
                else if(seconds > 300) seconds = 300;

                int minutes = (seconds / 60);
                seconds = seconds % 60;
                timers[i].setText(String.format("%02d:%02d", minutes, seconds));
            }
            handler.postDelayed(this, 200);
        }
    };

    public void setHpBarTo(final int i, final int goal) {
        hpAnimator.setGoal(i, goal);
        hpAnimator.setHpBarToGoal();
    }

    public void animateHpBarTo(final int i, final int goal, int change) {
        hpAnimator.setGoal(i, goal);
        new Thread(hpAnimator).start();
    }

    public void updateBattleInfo(boolean scroll) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (battle == null)
                    return;
                synchronized (battle.histDelta) {
                    infoView.append(battle.histDelta);
                if (battle.histDelta != null) {
                        infoScroll.post(new Runnable() {
                            public void run() {
                                infoScroll.smoothScrollTo(0, infoView.getMeasuredHeight());
                            }   
                        });
		}
                    infoScroll.invalidate();
                    battle.hist.append(battle.histDelta);
                    battle.histDelta.clear();
                }
            }
        });
    }

    public void updatePokes(byte player, byte slot) {
        Log.e(TAG, PokemonInfo.cacheStatus());
        if (player == me)
            updateMyPoke(slot);
        else
            updateOppPoke(player, slot);
    }

    public int statusTint(int status) {
        switch (status) {
            case 0:
                return 1;
            case 31:
                return 0x7D000000;
            case 1:
                return 0x7DF8D030;
            case 2:
                return 0x7D888888;
            case 3:
                return 0x7D98D8D8;
            case 4:
                return 0x7DF08030;
            case 5:
                return 0x7DA040A0;
            case 6:
                return 0x7DC8C8C8;
            default:
                return 0;
        }
    }

    public void updatePokeballs() {
        runOnUiThread(new Runnable() {
            public void run() {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 6; j++) {
                        pokeballs[i][j].setImageDrawable((
                                battle.pokes[i][j].uID.pokeNum != 0
                                        ? PokemonInfo.iconDrawableCache(battle.pokes[i][j].uID)
                                        : PokemonInfo.iconDrawablePokeballStatus()
                        ));
                        pokeballs[i][j].setColorFilter(statusTint(battle.pokes[i][j].status()));
                    }
                }
                /// PorterDuff.Mode.MULTIPLY;
            }
        });
    }

    public void updatePokeBall(final int player, final int poke) {
        runOnUiThread(new Runnable() {
            public void run() {
                pokeballs[player][poke].setImageDrawable((
                        battle.pokes[player][poke].uID.pokeNum != 0
                                ? PokemonInfo.iconDrawableCache(battle.pokes[player][poke].uID)
                                : PokemonInfo.iconDrawablePokeballStatus()
                ));
                pokeballs[player][poke].setColorFilter(statusTint(battle.pokes[player][poke].status()));
            }
        });
    }

    private String getAnimSprite(ShallowBattlePoke poke, boolean front) {
        String res;
        UniqueID uID;
        if (poke.specialSprites.isEmpty())
            uID = poke.uID;
        else
            uID = poke.specialSprites.peek();

        if (poke.uID.pokeNum < 0)
            res = null;
        else {
            res = String.format("anim%03d", uID.pokeNum) + (uID.subNum == 0 ? "" : "_" + uID.subNum) +
                    (poke.gender == GenderInfo.Gender.Female.ordinal() ? "f" : "") + (front ? "_front" : "_back") + (poke.shiny ? "s" : "") + ".gif";
        }
        return res;
    }

    public void updateCurrentPokeListEntry() {
        runOnUiThread(new Runnable() {
            public void run() {
                synchronized(battle) {
                    BattlePoke battlePoke = activeBattle.myTeam.pokes[0];
                    pokeList[0].hp.setText(battlePoke.currentHP +
                            "/" + battlePoke.totalHP);
                }
                // TODO: Status ailments and stuff
            }
        });
    }

    public void updateMovePP(final int moveNum) {
        runOnUiThread(new Runnable() {
            public void run() {
                BattleMove move = activeBattle.displayedMoves[moveNum];
                attackPPs[moveNum].setText("PP " + move.currentPP + "/" + move.totalPP);
            }
        });
    }

    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode != Activity.RESULT_OK)
            return;
        String path = resultData.getString("path");
        if (resultData.getBoolean("me")) {
            pokeSprites[me][0].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
            pokeSprites[me][1].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
            pokeSprites[me][2].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
        } else {
            pokeSprites[opp][0].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
            pokeSprites[opp][1].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
            pokeSprites[opp][2].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
        }
    }

    public boolean isSpectating() {
        return activeBattle == null;
    }

    public void updateMyPoke(int slot) {
        if (isSpectating()) {
            updateOppPoke(me, slot);
            return;
        }
        runOnUiThread(new Runnable() {

            public void run() {
                ShallowBattlePoke poke = battle.currentPoke(me, slot);
                poke.shiny = activeBattle.myTeam.pokes[0].shiny; // This is a very stupid way to do it. ShallowBattleTeam never gives shiny correctly?
                // Load correct moveset and name
                if (poke != null) {
                    if (!samePokes[me]) {
                        currentPokeNames[me].setText(poke.rnick);
                        currentPokeLevels[me].setText("Lv. " + poke.level);
                        currentPokeGenders[me].setImageDrawable(PokemonInfo.genderDrawableCache((int) poke.gender));
                    }

                    currentPokeStatuses[me].setImageResource(resources.getIdentifier("battle_status" + poke.status(), "drawable", InfoConfig.pkgName));
                    setHpBarTo(me, poke.lifePercent);

                    for (int i = 0; i < 4; i++) {
                        BattleMove move = activeBattle.displayedMoves[i];
                        updateMovePP(i);
                        attackNames[i].setText(move.toString());
                        String type;
                        if (move.num == 237)
                            type = TypeInfo.name(activeBattle.myTeam.pokes[0].hiddenPowerType());
                        else
                            type = TypeInfo.name(MoveInfo.type(move.num()));
                        type = type.toLowerCase();
                        attackLayouts[i].setBackgroundResource(resources.getIdentifier(type + "_type_button",
                                "drawable", InfoConfig.pkgName));
                    }

                    if (!samePokes[me]) {
                        String sprite = null;
                        if (battle.shouldShowPreview || poke.status() == Status.Koed.poValue()) {
                            sprite = "empty_sprite.png";
                        } else if (poke.sub) {
                            sprite = "sub_back.png";
                        } else {
                            if (useAnimSprites) {
                                Intent data = new Intent();
                                data.setComponent(servName);
                                data.putExtra("me", true);
                                data.putExtra("sprite", getAnimSprite(poke, false));
                                data.putExtra("cb", mRecvr);
                                startService(data);
                            } else {
                                sprite = PokemonInfo.sprite(poke, false);
                            }
                        }

                        if (sprite != null) {
                            pokeSprites[me][slot].loadDataWithBaseURL("file:///android_res/drawable/", "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + sprite + "');}</style><body></body>", "text/html", "utf-8", null);
                        }
                    } else samePokes[me] = true;
                }
            }
        });
        updateTeam();
    }

    public boolean samePokes[] = new boolean[2];

    public void updateOppPoke(final int opp, int slot) {
        runOnUiThread(new Runnable() {
            public void run() {
                ShallowBattlePoke poke = battle.currentPoke(opp, slot);
                // Load correct moveset and name
                if(poke != null) {
                    if (!samePokes[opp]) {
                        currentPokeNames[opp].setText(poke.rnick);
                        currentPokeLevels[opp].setText("Lv. " + poke.level);
                        currentPokeGenders[opp].setImageDrawable(PokemonInfo.genderDrawableCache((int) poke.gender));
                    }

                    currentPokeStatuses[opp].setImageResource(resources.getIdentifier("battle_status" + poke.status(), "drawable", InfoConfig.pkgName));
                    setHpBarTo(opp, poke.lifePercent);

                    if (!samePokes[opp]) {
                        String sprite = null;
                        if (battle.shouldShowPreview || poke.status() == Status.Koed.poValue()) {
                            sprite = "empty_sprite.png";
                        } else if (poke.sub) {
                            sprite = opp == me ? "sub_back.png" : "sub_front.png";
                        } else {
                            if (useAnimSprites) {
                                Intent data = new Intent();
                                data.setComponent(servName);
                                data.putExtra("me", false);
                                data.putExtra("sprite", getAnimSprite(poke, opp != me));
                                data.putExtra("cb", mRecvr);
                                startService(data);
                            } else {
                                sprite = PokemonInfo.sprite(poke, opp != me);
                            }
                        }

                        if (sprite != null) {
                            pokeSprites[opp][slot].loadDataWithBaseURL("file:///android_res/drawable/", "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + sprite + "');}</style><body></body>", "text/html", "utf-8", null);
                        }
                    } else samePokes[opp] = true;
                }
            }
        });
    }

    public void updateButtons() {
        if (isSpectating()) {
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                if (!checkStruggle()) {
                    for (int i = 0; i < 4; i++) {
                        if (activeBattle.allowAttack && !activeBattle.clicked) {
                            BattleMove newMove = new BattleMove(activeBattle.displayedMoves[i]);
                            if (zmoveClicked) {
                                if(newMove.power > 0 && activeBattle.allowZMoves[i]) {
                                    newMove.num = ItemInfo.zCrystalMove(activeBattle.myTeam.pokes[0].item);
                                }
                                setAttackButtonEnabled(i, activeBattle.allowZMoves[i]);
                            }
                            else {
                                setAttackButtonEnabled(i, activeBattle.allowAttacks[i]);
                            }
                            attackNames[i].setText(MoveInfo.zName(newMove.num(), zmoveClicked));
                        }
                        else {
                            setAttackButtonEnabled(i, false);
                        }
                    }
                }
                megaClicked = false;
                for(int i = 0; i < 6; i++) {
                    if (activeBattle.myTeam.pokes[i].status() != Status.Koed.poValue() && !activeBattle.clicked)
                        pokeList[i].setEnabled(i, activeBattle.allowSwitch);
                    else
                        pokeList[i].setEnabled(i, false);
                }
            }
        });
    }

    public boolean checkStruggle() {
        // This method should hide moves, show the button if necessary and return whether it showed the button
        boolean struggle = activeBattle.shouldStruggle;
        if(struggle) {
            attackRow1.setVisibility(View.GONE);
            attackRow2.setVisibility(View.GONE);
            struggleLayout.setVisibility(View.VISIBLE);
        }
        else {
            attackRow1.setVisibility(View.VISIBLE);
            attackRow2.setVisibility(View.VISIBLE);
            struggleLayout.setVisibility(View.GONE);
        }
        return struggle;
    }

    public void updateTeam() {
        runOnUiThread(new Runnable() {

            public void run() {
                for (int i = 0; i < 6; i++) {
                    BattlePoke poke = activeBattle.myTeam.pokes[i];
                    pokeList[i].update(poke, activeBattle.allowSwitch && !activeBattle.clicked);
                }
            }
        });
    }

    public void updateMoves(short attack) {
        if (!isSpectating()) {
            activeBattle.pokes[opp][0].addMove(attack , (byte) 1);
        }
    }

    public void updateMoves(byte player, byte spot, short attack, byte pp) {
        activeBattle.pokes[player][spot].addMove(attack, pp);
    }

    public void switchToPokeViewer() {
        runOnUiThread(new Runnable() {
            public void run() {
                realViewSwitcher.setCurrentItem(1, true);
            }
        });
    }

    public void onResume() {
        // XXX we might want more stuff here
        super.onResume();
        if (battle != null)
            checkRearrangeTeamDialog();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(BattleActivity.this, ChatActivity.class));
        finish();
    }

    public ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            netServ = ((NetworkService.LocalBinder)service).getService();

            int battleId = getIntent().getIntExtra("battleId", 0);
            battle = netServ.activeBattles.get(battleId);
            if (battle == null) {
                battle = netServ.spectatedBattles.get(battleId);
            }
            Log.e(TAG, "Binding at State Time: " + (System.currentTimeMillis() - battle.startTime));

            if (battle == null) {
                startActivity(new Intent(BattleActivity.this, ChatActivity.class));
                finish();

                netServ.closeBattle(battleId); //remove the possibly ongoing notification
                return;
            }
			
			/* Is it a spectating battle or not? */
            try {
                activeBattle = (Battle) battle;
            } catch (ClassCastException ex) {}

            if (isSpectating()) {
				/* If it's a spectating battle, we remove the info view's bottom margin */
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)infoScroll.getLayoutParams();
                params.setMargins(params.leftMargin, params.topMargin, params.rightMargin,
                        ((RelativeLayout.LayoutParams)attackRow2.getLayoutParams()).bottomMargin);
                infoScroll.setLayoutParams(params);
            } else {
                teamLayout = getLayoutInflater().inflate(R.layout.battle_teamscreen, null);

                for(int i = 0; i < 4; i++) {
                    attackNames[i] = (TextView)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "Name", "id", InfoConfig.pkgName));
                    attackPPs[i] = (TextView)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "PP", "id", InfoConfig.pkgName));
                    attackLayouts[i] = (RelativeLayout)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "Layout", "id", InfoConfig.pkgName));
                    attackLayouts[i].setOnClickListener(battleListener);
                    attackLayouts[i].setOnLongClickListener(moveListener);
                }
                for(int i = 0; i < 6; i++) {
                    RelativeLayout whole = (RelativeLayout)teamLayout.findViewById(resources.getIdentifier("pokeViewLayout" + (i+1), "id", InfoConfig.pkgName));
                    pokeList[i] = new ListedPokemon(whole);
                    whole.setOnClickListener(battleListener);
                    whole.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            int id = v.getId();
                            for(int i = 0; i < 6; i++) {
                                if(id == pokeList[i].whole.getId()) {
                                    BattlePoke poke = activeBattle.myTeam.pokes[i];
                                    final Dialog simpleDialog = new Dialog(BattleActivity.this);
                                    simpleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    simpleDialog.setContentView(R.layout.dynamic_info_layout);

                                    TextView t = (TextView)simpleDialog.findViewById(R.id.nameTypeView);
                                    t.setText(poke.nameAndType());
                                    t = (TextView)simpleDialog.findViewById(R.id.statNamesView);
                                    t.setText(battle.dynamicInfo[me].stats());
                                    t = (TextView)simpleDialog.findViewById(R.id.statNumsView);
                                    t.setText(poke.printStats());

                                    simpleDialog.setCanceledOnTouchOutside(true);
                                    simpleDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        public void onCancel(DialogInterface dialog) {
                                            simpleDialog.dismiss();
                                        }
                                    });
                                    simpleDialog.findViewById(R.id.dynamic_info_layout).setOnClickListener(new OnClickListener() {
                                        public void onClick(View v) {
                                            simpleDialog.dismiss();
                                        }
                                    });
                                    simpleDialog.show();
                                }
                            }
                            return true;
                        }
                    });
                }

                /* Well it helps you keep track what your opponent has seen!
				// Pre-load PokeBall and sprite info
				for (int i = 0; i < 6; i++) {
					battle.pokes[battle.me][i].uID = activeBattle.myTeam.pokes[i].uID;
				}
				*/

		        /* Changed to two pages */
                realViewSwitcher.getAdapter().notifyDataSetChanged();
            }

            battleView.setBackgroundResource(resources.getIdentifier("bg" + battle.background, "drawable", InfoConfig.pkgName));

            // Set the UI to display the correct info
            me = battle.me;
            opp = battle.opp;
            // We don't know which timer is which until the battle starts,
            // so set them here.
            timers[me] = (TextView)mainLayout.findViewById(R.id.timerB);
            timers[opp] = (TextView)mainLayout.findViewById(R.id.timerA);

            names[me] = (TextView)mainLayout.findViewById(R.id.nameB);
            names[opp] = (TextView)mainLayout.findViewById(R.id.nameA);

            for (int i = 0; i < 6; i++) {
                pokeballs[me][i] = (ImageView)mainLayout.findViewById(resources.getIdentifier("pokeball" + (i + 1) + "B", "id", InfoConfig.pkgName));
                pokeballs[opp][i] = (ImageView)mainLayout.findViewById(resources.getIdentifier("pokeball" + (i + 1) + "A", "id", InfoConfig.pkgName));
            }
            updatePokeballs();

            names[me].setText(battle.players[me].nick());
            names[opp].setText(battle.players[opp].nick());

            hpBars[me] = (TextProgressBar) battleView.findViewById(R.id.hpBarB);
            hpBars[opp] = (TextProgressBar) battleView.findViewById(R.id.hpBarA);

            currentPokeNames[me] = (TextView) battleView.findViewById(R.id.currentPokeNameB);
            currentPokeNames[opp] = (TextView) battleView.findViewById(R.id.currentPokeNameA);

            currentPokeLevels[me] = (TextView) battleView.findViewById(R.id.currentPokeLevelB);
            currentPokeLevels[opp] = (TextView) battleView.findViewById(R.id.currentPokeLevelA);

            currentPokeGenders[me] = (ImageView) battleView.findViewById(R.id.currentPokeGenderB);
            currentPokeGenders[opp] = (ImageView) battleView.findViewById(R.id.currentPokeGenderA);

            currentPokeStatuses[me] = (ImageView) battleView.findViewById(R.id.currentPokeStatusB);
            currentPokeStatuses[opp] = (ImageView) battleView.findViewById(R.id.currentPokeStatusA);

            pokeSprites[me][0] = (WebView) battleView.findViewById(R.id.pokeSpriteB1);
            pokeSprites[opp][0] = (WebView) battleView.findViewById(R.id.pokeSpriteA1);
            pokeSprites[me][1] = (WebView) battleView.findViewById(R.id.pokeSpriteB2);
            pokeSprites[opp][1] = (WebView) battleView.findViewById(R.id.pokeSpriteA2);
            pokeSprites[me][2] = (WebView) battleView.findViewById(R.id.pokeSpriteB3);
            pokeSprites[opp][2] = (WebView) battleView.findViewById(R.id.pokeSpriteA3);

            for(int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++) {
                    pokeSprites[i][j].setOnLongClickListener(spriteListener);
                    pokeSprites[i][j].setBackgroundColor(0);
                }
            }


            infoView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View view) {
                    final EditText input = new EditText(BattleActivity.this);
                    new AlertDialog.Builder(BattleActivity.this)
                            .setTitle("Battle Chat")
                            .setMessage("Send Battle Message")
                            .setView(input)
                            .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface i, int j) {
                                    String message = input.getText().toString();
                                    if (message.length() > 0) {
                                        Baos msg = new Baos();
                                        msg.putInt(BattleActivity.this.battle.bID);
                                        msg.putString(message);
                                        if (activeBattle != null) {
                                            netServ.socket.sendMessage(msg, Command.BattleChat);
                                        } else {
                                            netServ.socket.sendMessage(msg, Command.SpectateBattleChat);
                                        }
                                    }
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).show();
                    return false;
                }
            });

            // Don't set battleActivity until after we've finished
            // getting UI elements. Otherwise there's a race condition if Battle
            // wants to update one of our UI elements we haven't gotten yet.
            synchronized(battle) {
                battle.activity = BattleActivity.this;
            }

            // Load scrollback
            infoView.setText(battle.hist);
            updateBattleInfo(true);

            // Prompt a UI update of the pokemon
            updateMyPoke(0);
            updateOppPoke(opp, 0);
            if (battle.conf.mode >= ChallengeEnums.Mode.Doubles.ordinal()) {
                updateMyPoke(1);
                updateOppPoke(opp, 1);
            }
            if (battle.conf.mode >= ChallengeEnums.Mode.Triples.ordinal()) {
                updateMyPoke(2);
                updateOppPoke(opp, 2);
            }

            // Enable or disable buttons
            updateButtons();

            // Start timer updating
            handler.postDelayed(updateTimeTask, 100);

            checkRearrangeTeamDialog();

            Log.e(TAG, "Connection finished at State Time: " + (System.currentTimeMillis() - battle.startTime));
            //    Runtime.getRuntime().gc();
        }

        public void onServiceDisconnected(ComponentName className) {
            battle.activity = null;
            netServ = null;
        }
    };

    public void end() {
        runOnUiThread(new Runnable() {
            public void run() {
                BattleActivity.this.finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

    /*
    public OnTouchListener dialogListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent e) {
            int id = v.getId();
            for(int i = 0; i < 6; i++) {
                if(id == myArrangePokeIcons[i].getId() && e.getAction() == MotionEvent.ACTION_DOWN) {
                    Object dragInfo = v;
                    mDragLayer.startDrag(v, myArrangePokeIcons[i], dragInfo, DragController.DRAG_ACTION_MOVE);
                    break;
                }
            }
            return true;
        }
    };*/

    public OnTouchListener dialogListener(final TextView nameAndType, final TextView statNames, final TextView statNums, final TextView moves) {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int id = v.getId();
                for(int i = 0; i < 6; i++) {
                    if(id == myArrangePokeIcons[i].getId() && event.getAction() == MotionEvent.ACTION_DOWN) {
                        int num = myArrangePokeIcons[i].num;
                        nameAndType.setText(activeBattle.myTeam.pokes[num].nameAndType());
                        String s = "HP:";
                        s += "\nAttack:";
                        s += "\nDefense:";
                        s += "\nSp. Att:";
                        s += "\nSp. Def:";
                        s += "\nSpeed:";
                        statNames.setText(s);
                        statNums.setText(activeBattle.myTeam.pokes[num].printStats());
                        moves.setText(activeBattle.myTeam.pokes[num].movesString());
                        mDragLayer.startDrag(v, myArrangePokeIcons[i], (Object) event, DragController.DRAG_ACTION_MOVE);
                        break;
                    }
                }
                return true;
            }
        };
    }

    public OnTouchListener oppdialogListener(final TextView nameAndType, final TextView statNames, final TextView statNums, final TextView moves, final int index) {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    nameAndType.setText(activeBattle.oppTeam.pokes[index].nameAndType());
                    String s = "HP:";
                    s += "\nAttack:";
                    s += "\nDefense:";
                    s += "\nSp. Att:";
                    s += "\nSp. Def:";
                    s += "\nSpeed:";
                    statNames.setText(s);
                    statNums.setText(activeBattle.oppTeam.pokes[index].statString());
                    moves.setText(activeBattle.oppTeam.pokes[index].moves());
                }
                return true;
            }
        };
    }


    public OnClickListener battleListener = new OnClickListener() {
        public void onClick(View v) {
            int id = v.getId();
            // Check to see if click was on attack button
            for(int i = 0; i < 4; i++)
                if(id == attackLayouts[i].getId()) {
                    netServ.socket.sendMessage(activeBattle.constructAttack((byte) i, megaClicked, zmoveClicked), Command.BattleMessage);
                    if (zmoveClicked) {
                        zmoveClicked = false;
                        updateButtons();
                    }
                }
            // Check to see if click was on pokelist button
            for(int i = 0; i < 6; i++) {
                if(id == pokeList[i].whole.getId()) {
                    netServ.socket.sendMessage(activeBattle.constructSwitch((byte)i), Command.BattleMessage);
                    realViewSwitcher.setCurrentItem(0, true);
                    zmoveClicked = false;
                }
            }
            activeBattle.clicked = true;
            updateButtons();
        }
    };


    public OnLongClickListener moveListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            int id = v.getId();
            for(int i = 0; i < 4; i++)
                if(id == attackLayouts[i].getId() && !attackNames[i].equals("No Move")) {
                    lastClickedMove = activeBattle.displayedMoves[i];
                    if (zmoveClicked) {
                        setAttackButtonEnabled(i, activeBattle.allowZMoves[i]);
                        if(lastClickedMove.power > 0 && activeBattle.allowZMoves[i]) {
                            lastClickedMove.power = MoveInfo.zPower(lastClickedMove.num());
                            lastClickedMove.accuracy = 101;
                            if (lastClickedMove.num == 237) { //Hidden Power
                                lastClickedMove.type = 0;
                            }
                            lastClickedMove.num = ItemInfo.zCrystalMove(activeBattle.myTeam.pokes[0].item);
                        }
                    }
                    showDialog(BattleDialog.MoveInfo.ordinal());
                    return true;
                }
            return false;
        }
    };

    public OnLongClickListener spriteListener = new OnLongClickListener() {
        public boolean onLongClick(View v) {
            if(v.getId() == pokeSprites[me][0].getId() || v.getId() == pokeSprites[me][1].getId() || v.getId() == pokeSprites[me][2].getId())
                showDialog(BattleDialog.MyDynamicInfo.ordinal());
            else
                showDialog(BattleDialog.OppDynamicInfo.ordinal());
            return true;
        }
    };

    void setAttackButtonEnabled(int num, boolean enabled) {
        /*
        if (enabled) {
            if (!attackLayouts[num].isEnabled()) {
                Log.e(TAG, "Enabling button " + num + " at State Time: " + (System.currentTimeMillis() - battle.startTime));
            }
        } else {
            if (attackLayouts[num].isEnabled()) {
                Log.e(TAG, "Disabling button " + num + " at State Time: " + (System.currentTimeMillis() - battle.startTime));
            }
        }
        */
        attackLayouts[num].setEnabled(enabled);
        attackNames[num].setEnabled(enabled);
        attackNames[num].setShadowLayer((float)1.5, 1, 1, resources.getColor(enabled ? R.color.poke_text_shadow_enabled : R.color.poke_text_shadow_disabled));
        attackPPs[num].setEnabled(enabled);
        attackPPs[num].setShadowLayer((float)1.5, 1, 1, resources.getColor(enabled ? R.color.pp_text_shadow_enabled : R.color.pp_text_shadow_disabled));
    }

	/*
    void setLayoutEnabled(ViewGroup v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.getBackground().setAlpha(enabled ? 255 : 128);
    }
    
    void setTextViewEnabled(TextView v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.setTextColor(v.getTextColors().withAlpha(enabled ? 255 : 128).getDefaultColor());
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(isSpectating() ? R.menu.spectatingbattleoptions : R.menu.battleoptions, menu);

        menu.findItem(R.id.sounds).setChecked(getSharedPreferences("battle", MODE_PRIVATE).getBoolean("pokemon_cries", true));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (!isSpectating()) {
            if (activeBattle.gotEnd) {
                menu.findItem(R.id.close).setVisible(true);
                menu.findItem(R.id.cancel).setVisible(false);
                menu.findItem(R.id.forfeit).setVisible(false);
                menu.findItem(R.id.draw).setVisible(false);
            } else {
    			/* No point in canceling if no action done */
                menu.findItem(R.id.close).setVisible(false);
                menu.findItem(R.id.cancel).setVisible(activeBattle.clicked);
            }
            menu.findItem(R.id.megavolve).setVisible(activeBattle.allowMega);
            menu.findItem(R.id.megavolve).setChecked(megaClicked);

            if (megaClicked) {
                menu.findItem(R.id.megavolve).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            } else {
                menu.findItem(R.id.megavolve).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
            menu.findItem(R.id.zmove).setVisible(activeBattle.allowZMove);
            menu.findItem(R.id.zmove).setChecked(zmoveClicked);
            if (zmoveClicked) {
                menu.findItem(R.id.zmove).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            } else {
                menu.findItem(R.id.zmove).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.zmove:
                item.setChecked(!item.isChecked());
                zmoveClicked = item.isChecked();
                if (zmoveClicked) {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                } else {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                }
                updateButtons();
                break;
            case R.id.megavolve:
                item.setChecked(!item.isChecked());
                megaClicked = item.isChecked();
                if (megaClicked) {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                } else {
                    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                }
                break;
            case R.id.cancel:
                netServ.socket.sendMessage(activeBattle.constructCancel(), Command.BattleMessage);
                break;
            case R.id.forfeit:
                if (netServ != null && netServ.isBattling() && !battle.gotEnd)
                    showDialog(BattleDialog.ConfirmForfeit.ordinal());
                break;
            case R.id.close:
                if (isSpectating()) {
                    netServ.stopWatching(battle.bID);
                } else {
                    endBattle();
                }
                break;
            case R.id.draw:
                netServ.socket.sendMessage(activeBattle.constructDraw(), Command.BattleMessage);
                break;
            case R.id.sounds:
                item.setChecked(!item.isChecked());
                getSharedPreferences("battle", Context.MODE_PRIVATE).edit().putBoolean("pokemon_cries", item.isChecked()).commit();
                break;
            case R.id.debug:
                showDialog(BattleDialog.Debug.ordinal());
                break;
        }
        return true;
    }

    public void notifyRearrangeTeamDialog() {
        runOnUiThread(new Runnable() {
            public void run() {
                checkRearrangeTeamDialog();
            }
        });
    }

    private void checkRearrangeTeamDialog() {
        if (netServ != null && netServ.isBattling() && battle.shouldShowPreview) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {}
            showDialog(BattleDialog.RearrangeTeam.ordinal());
        }
    }

    void endBattle() {
        if (netServ != null && netServ.socket != null && netServ.socket.isConnected()) {
            Baos bID = new Baos();
            bID.putInt(battle.bID);
            netServ.socket.sendMessage(bID, Command.BattleFinished);
        }
    }

    protected Dialog onCreateDialog(final int id) {
        int player = me;
        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        switch(BattleDialog.values()[id]) {
            case RearrangeTeam: {
                View layout = inflater.inflate(R.layout.rearrange_team_dialog, (RelativeLayout) findViewById(R.id.drag_my_poke_layout));
                final TextView nameAndType = (TextView) layout.findViewById(R.id.nameTypeView);
                final TextView statNames = (TextView) layout.findViewById(R.id.statNamesView);
                final TextView statNums = (TextView) layout.findViewById(R.id.statNumsView);
                final TextView moves = (TextView) layout.findViewById(R.id.moveString);

                builder.setView(layout)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                netServ.socket.sendMessage(activeBattle.constructRearrange(), Command.BattleMessage);
                                battle.shouldShowPreview = false;
                                removeDialog(id);
                            }
                        })
                        .setCancelable(false);
                dialog = builder.create();

                mDragLayer = (DragLayer)layout.findViewById(R.id.drag_my_poke);
                /*
                mDragLayer.addDragListener(new DragController.DragListener() {
                    @Override
                    public void onDragStart(View v, DragSource source, Object info, int dragAction) {

                    }

                    @Override
                    public void onDragEnd() {
                        nameAndType.setText(activeBattle.myTeam.pokes[0].nameAndType());
                        String s = "HP:";
                        s += "\nAttack:";
                        s += "\nDefense:";
                        s += "\nSp. Att:";
                        s += "\nSp. Def:";
                        s += "\nSpeed:";
                        statNames.setText(s);
                        statNums.setText(activeBattle.myTeam.pokes[0].printStats());
                        moves.setText(activeBattle.myTeam.pokes[0].movesString());
                    }
                });*/
                for(int i = 0; i < 6; i++){
                    BattlePoke poke = activeBattle.myTeam.pokes[i];
                    myArrangePokeIcons[i] = (PokeDragIcon)layout.findViewById(resources.getIdentifier("my_arrange_poke" + (i+1), "id", InfoConfig.pkgName));
                    myArrangePokeIcons[i].setOnTouchListener(dialogListener(nameAndType, statNames, statNums, moves));
                    myArrangePokeIcons[i].setImageDrawable(PokemonInfo.iconDrawableCache(poke.uID));
                    myArrangePokeIcons[i].num = i;
                    myArrangePokeIcons[i].battleActivity = this;

                    ShallowShownPoke oppPoke = activeBattle.oppTeam.pokes[i];
                    oppArrangePokeIcons[i] = (ImageView)layout.findViewById(resources.getIdentifier("foe_arrange_poke" + (i+1), "id", InfoConfig.pkgName));
                    oppArrangePokeIcons[i].setImageDrawable(PokemonInfo.iconDrawableCache(oppPoke.uID));
                    oppArrangePokeIcons[i].setOnTouchListener(oppdialogListener(nameAndType, statNames, statNums, moves, i));
                }
                nameAndType.setText(activeBattle.myTeam.pokes[0].nameAndType());
                String s = "HP:";
                s += "\nAttack:";
                s += "\nDefense:";
                s += "\nSp. Att:";
                s += "\nSp. Def:";
                s += "\nSpeed:";
                statNames.setText(s);
                statNums.setText(activeBattle.myTeam.pokes[0].printStats());
                moves.setText(activeBattle.myTeam.pokes[0].movesString());
                return dialog;
            }
            case ConfirmForfeit:
                builder.setMessage("Really Forfeit?")
                        .setCancelable(true)
                        .setPositiveButton("Forfeit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                endBattle();
                            }
                        })
                        .setNegativeButton("Cancel", null);
                return builder.create();
            case OppDynamicInfo:
                player = opp;
            case MyDynamicInfo:
                if(netServ != null && battle != null && battle.dynamicInfo[player] != null) {
                    final Dialog simpleDialog = new Dialog(this);
                    simpleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    simpleDialog.setContentView(R.layout.dynamic_info_layout);

                    TextView t = (TextView)simpleDialog.findViewById(R.id.nameTypeView);
                    t.setText(( (player == me && !isSpectating()) ? activeBattle.myTeam.pokes[0] : battle.currentPoke(player)).nameAndType());
                    t = (TextView)simpleDialog.findViewById(R.id.statNamesView);
                    t.setText(battle.dynamicInfo[player].statsAndHazards());
                    t = (TextView)simpleDialog.findViewById(R.id.statNumsView);
                    if (player == me && !isSpectating()) {
                        t.setText(activeBattle.myTeam.pokes[0].printStats());
                    }
                    else if (player != me && !isSpectating()) {
                        t.setText(activeBattle.currentPoke(player).statString(battle.dynamicInfo[player].boosts));
                        t = (TextView)simpleDialog.findViewById(R.id.moveString);
                        t.setText(activeBattle.currentPoke(opp).movesString());
                    }
                    //	t.setVisibility(View.GONE);
                    t = (TextView)simpleDialog.findViewById(R.id.statBoostView);
                    String s = battle.dynamicInfo[player].boosts();
                    if (!"\n\n\n\n".equals(s)) {
                        t.setText(s);
                    } else {
                        t.setVisibility(View.GONE);
                    }
                    simpleDialog.setCanceledOnTouchOutside(true);
                    simpleDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            removeDialog(id);
                        }
                    });
                    simpleDialog.findViewById(R.id.dynamic_info_layout).setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            simpleDialog.cancel();
                        }
                    });
                    return simpleDialog;
                }
                return null;
            case MoveInfo:
                dialog = builder.setTitle(lastClickedMove.toString())
                        .setMessage(lastClickedMove.descAndEffects())
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                removeDialog(id);
                            }
                        })
                        .create();
                dialog.setCanceledOnTouchOutside(true);
                return dialog;
            case Debug:
                String debug = battle.packetStack.toReadableString();
                final Dialog simpleDialog = new Dialog(this);
                simpleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                simpleDialog.setContentView(R.layout.debug_layout);

                TextView t = (TextView) simpleDialog.findViewById(R.id.debug_text);
                t.setText(debug);

                simpleDialog.setCanceledOnTouchOutside(true);
                simpleDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        removeDialog(id);
                    }
                });

                return simpleDialog;
            default:
                return new Dialog(this);
        }
    }

    private void setUncaughtHandler() {
        final BattleExceptionHandler exceptionHandler = new BattleExceptionHandler(this);
        Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
            }
        });
    }

    private class BattleExceptionHandler implements Thread.UncaughtExceptionHandler {
        BattleActivity mActivity;
        private Thread.UncaughtExceptionHandler defaultHandler;

        public BattleExceptionHandler(BattleActivity mActivity) {
            this.mActivity = mActivity;
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        }

        @Override
        public void uncaughtException(Thread thread, final Throwable ex) {
            boolean isSpectating = mActivity.isSpectating();

            final BattlePacket packet;
            if (isSpectating) {
                packet = mActivity.activeBattle.lastPacket;
            } else {
                packet = mActivity.battle.lastPacket;
            }
            boolean validPacket = (packet != null);

            if (validPacket) {
                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        Toast.makeText(mActivity, ex.getClass().getName() + "\n" + packet.toCompactString() + "\n" + packet.toString(), Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                }.start();

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException dontcare) {
                    // don't care
                }
            }

            defaultHandler.uncaughtException(thread, ex);
        }
    }
}
