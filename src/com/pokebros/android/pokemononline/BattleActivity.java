package com.pokebros.android.pokemononline;

import java.util.Locale;

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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.os.SystemClock;
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

import com.android.launcher.DragController;
import com.android.launcher.DragLayer;
import com.android.launcher.PokeDragIcon;
import com.pokebros.android.pokemononline.battle.Battle;
import com.pokebros.android.pokemononline.battle.BattleMove;
import com.pokebros.android.pokemononline.battle.SpectatingBattle;
import com.pokebros.android.pokemononline.battle.Type;
import com.pokebros.android.pokemononline.poke.BattlePoke;
import com.pokebros.android.pokemononline.poke.PokeEnums.Gender;
import com.pokebros.android.pokemononline.poke.PokeEnums.Status;
import com.pokebros.android.pokemononline.poke.ShallowBattlePoke;
import com.pokebros.android.pokemononline.poke.ShallowShownPoke;
import com.pokebros.android.pokemononline.poke.UniqueID;

class MyResultReceiver extends ResultReceiver {
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
public class BattleActivity extends Activity implements MyResultReceiver.Receiver{
	private MyResultReceiver mRecvr;
    private static ComponentName servName = new ComponentName("com.pokebros.android.pokemonresources", "com.pokebros.android.pokemonresources.SpriteService");
	
	public enum BattleDialog {
		RearrangeTeam,
		ConfirmForfeit, 
		OppDynamicInfo, 
		MyDynamicInfo,
		MoveInfo
	}

	public final static int SWIPE_TIME_THRESHOLD = 100;
    final static String pkgName = "com.pokebros.android.pokemononline";
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
	TextView[] pokeListNames = new TextView[6];
	TextView[] pokeListItems = new TextView[6];
	TextView[] pokeListAbilities = new TextView[6];
	TextView[] pokeListHPs = new TextView[6];
	ImageView[] pokeListIcons = new ImageView[6];
	
	PokeDragIcon[] myArrangePokeIcons = new PokeDragIcon[6];
	ImageView[] oppArrangePokeIcons = new ImageView[6];
	
	RelativeLayout[] pokeListButtons = new RelativeLayout[6];
	TextView[][] pokeListMovePreviews = new TextView[6][4];
	TextView infoView;
	ScrollView infoScroll;
	TextView[] names = new TextView[2];
	ImageView[][] pokeballs = new ImageView[2][6];
	WebView[] pokeSprites = new WebView[2];

	RelativeLayout struggleLayout;
	LinearLayout attackRow1;
	LinearLayout attackRow2;
	
	SpectatingBattle battle = null;
	public Battle activeBattle = null;
	
	boolean useAnimSprites = true;
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
					Thread.sleep(40);
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
					Thread.sleep(40);
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
				public void run() {
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
	private class MyAdapter extends PagerAdapter
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
			getPackageManager().getApplicationInfo("com.pokebros.android.pokemonresources", 0);
		} catch (NameNotFoundException e) {
			Log.d("BattleActivity", "Animated sprites not found");
			useAnimSprites = false;
		}
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        bindService(new Intent(BattleActivity.this, NetworkService.class), connection,
        		Context.BIND_AUTO_CREATE);
        
        resources = getResources();
        realViewSwitcher = (ViewPager) new ViewPager(this);
		mainLayout = getLayoutInflater().inflate(R.layout.battle_mainscreen, null);
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
    			netServ.socket.sendMessage(activeBattle.constructAttack((byte)-1), Command.BattleMessage); // This is how you struggle
    		}
    	});
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
	
	public void animateHpBarTo(final int i, final int goal) {
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
					if (battle.histDelta.length() != 0 || true) {
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
	
	public void updatePokes(byte player) {
		if (player == me)
			updateMyPoke();
		else
			updateOppPoke(player);
	}
	
	public void updatePokeballs() {
		runOnUiThread(new Runnable() {
			public void run() {
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < 6; j++) {
						pokeballs[i][j].setImageResource(resources.getIdentifier("status" + battle.pokes[i][j].status(), "drawable", pkgName));
					}
				}
			}
		});
	}

	private String getSprite(ShallowBattlePoke poke, boolean front) {
        String res;
    	UniqueID uID;
    	if (poke.specialSprites.isEmpty())
    		uID = poke.uID;
    	else
    		uID = poke.specialSprites.peek();
    	if (uID.pokeNum < 0)
    		res = "empty_sprite.png";
    	else {
        	res = "p" + uID.pokeNum + (uID.subNum == 0 ? "" : "_" + uID.subNum) +
        			(front ? "_front" : "_back");
    		if (resources.getIdentifier(res + "f", "drawable", pkgName) != 0)
    			// Special female sprite
    			res = res + "f" + (poke.shiny ? "s" : "");
    	}
        System.out.println("SPRITE: " + res);
        int ident = resources.getIdentifier(res, "drawable", pkgName);
        if (ident == 0)
        	// No sprite found. Default to missingNo.
        	return "missingno.png";
        else
        	return res + ".png";
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
        			(poke.gender == Gender.Female.ordinal() ? "f" : "") + (front ? "_front" : "_back") + (poke.shiny ? "s" : "") + ".gif";
    	}
        return res;
	}
	
	public void updateCurrentPokeListEntry() {
		runOnUiThread(new Runnable() {
			public void run() {
				synchronized(battle) {
					BattlePoke battlePoke = activeBattle.myTeam.pokes[0];
					pokeListHPs[0].setText(battlePoke.currentHP +
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
        	pokeSprites[me].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
        } else {
        	pokeSprites[opp].loadDataWithBaseURL(path, "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + resultData.getString("sprite") + "');}</style><body></body>", "text/html", "utf-8", null);
        }
	}
	
	public boolean isSpectating() {
		return activeBattle == null;
	}
	
	public void updateMyPoke() {
		if (isSpectating()) {
			updateOppPoke(me);
			return;
		}
        runOnUiThread(new Runnable() {
		
			public void run() {
				ShallowBattlePoke poke = battle.currentPoke(me);
				// Load correct moveset and name
				if(poke != null) {
					currentPokeNames[me].setText(poke.rnick);
					currentPokeLevels[me].setText("Lv. " + poke.level);
					currentPokeGenders[me].setImageResource(resources.getIdentifier("battle_gender" + poke.gender, "drawable", pkgName));
					currentPokeStatuses[me].setImageResource(resources.getIdentifier("battle_status" + poke.status(), "drawable", pkgName));
					setHpBarTo(me, poke.lifePercent);
					BattlePoke battlePoke = activeBattle.myTeam.pokes[0];
			        for(int i = 0; i < 4; i++) {
			        	BattleMove move = activeBattle.displayedMoves[i];
			        	updateMovePP(i);
			        	attackNames[i].setText(move.toString());
			        	String type;
			        	if (move.num == 237)
			        		type = Type.values()[battlePoke.hiddenPowerType()].toString();
			        	else
			        		type = move.getTypeString();
			        	type = type.toLowerCase(Locale.UK);
			        	attackLayouts[i].setBackgroundResource(resources.getIdentifier(type + "_type_button",
					      		"drawable", pkgName));
			        }

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
			        		sprite = getSprite(poke, false);
			        	}
			        }
			        
			        if (sprite != null) {
						pokeSprites[me].loadDataWithBaseURL("file:///android_res/drawable/", "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + sprite + "');}</style><body></body>", "text/html", "utf-8", null);			        	
			        }
				}
			}
		});
		updateTeam();
	}
	
	public void updateOppPoke(final int opp) {
		runOnUiThread(new Runnable() {
			public void run() {
					ShallowBattlePoke poke = battle.currentPoke(opp);
					// Load correct moveset and name
					if(poke != null) {
						currentPokeNames[opp].setText(poke.rnick);
						currentPokeLevels[opp].setText("Lv. " + poke.level);
						currentPokeGenders[opp].setImageResource(resources.getIdentifier("battle_gender" + poke.gender, "drawable", pkgName));
						currentPokeStatuses[opp].setImageResource(resources.getIdentifier("battle_status" + poke.status(), "drawable", pkgName));
						setHpBarTo(opp, poke.lifePercent);

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
				        		sprite = getSprite(poke, opp != me);
				        	}
				        }
				        
				        if (sprite != null) {
							pokeSprites[opp].loadDataWithBaseURL("file:///android_res/drawable/", "<head><style type=\"text/css\">body{background-position:center bottom;background-repeat:no-repeat; background-image:url('" + sprite + "');}</style><body></body>", "text/html", "utf-8", null);			        	
				        }
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
							setAttackButtonEnabled(i, activeBattle.allowAttacks[i]);
						}
						else {
							setAttackButtonEnabled(i, false);
						}
					}
				}
				for(int i = 0; i < 6; i++) {
					if (activeBattle.myTeam.pokes[i].status() != Status.Koed.poValue() && !activeBattle.clicked)
						setPokeListButtonEnabled(i, activeBattle.allowSwitch);
					else
						setPokeListButtonEnabled(i, false);
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
	
	private Drawable getIcon(UniqueID uid) {
		int resID = resources.getIdentifier("pi" + uid.pokeNum +
				(uid.subNum == 0 ? "" : "_" + uid.subNum) +
				"_icon", "drawable", pkgName);
		if (resID == 0)
			resID = resources.getIdentifier("pi" + uid.pokeNum + "_icon",
					"drawable", pkgName);
		return resources.getDrawable(resID);
	}
	
	public void updateTeam() {
		runOnUiThread(new Runnable() {
		
			public void run() {
				for (int i = 0; i < 6; i++) {
					BattlePoke poke = activeBattle.myTeam.pokes[i];
					pokeListIcons[i].setImageDrawable(getIcon(poke.uID));
					pokeListNames[i].setText(poke.nick);
					pokeListHPs[i].setText(poke.currentHP +
							"/" + poke.totalHP);
					pokeListItems[i].setText(poke.itemString);
					pokeListAbilities[i].setText(poke.abilityString);
					for (int j = 0; j < 4; j++) {
						pokeListMovePreviews[i][j].setText(poke.moves[j].toString());
						pokeListMovePreviews[i][j].setShadowLayer((float)1, 1, 1, resources.getColor(activeBattle.allowSwitch && !activeBattle.clicked ? R.color.poke_text_shadow_enabled : R.color.poke_text_shadow_disabled));
			        	String type;
			        	if (poke.moves[j].num == 237)
			        		type = Type.values()[poke.hiddenPowerType()].toString();
			        	else
			        		type = poke.moves[j].getTypeString();
			        	type = type.toLowerCase(Locale.UK);
			        	pokeListMovePreviews[i][j].setBackgroundResource(resources.getIdentifier(type + "_type_button",
					      		"drawable", pkgName));
					}
				}
			}
		});
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
	
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ = ((NetworkService.LocalBinder)service).getService();
			
			int battleId = getIntent().getIntExtra("battleId", 0);
			battle = netServ.activeBattles.get(battleId);
			if (battle == null) {
				battle = netServ.spectatedBattles.get(battleId);
			}
			
			if (battle == null) {
	    		startActivity(new Intent(BattleActivity.this, ChatActivity.class));
				finish();
				
				netServ.closeBattle(battleId); //remove the possibly ongoing notification
				return;
			}
			
			/* Is it a spectating battle or not? */
			try {
				activeBattle = (Battle) battle;
			} catch (ClassCastException ex) {

			}
			
			if (isSpectating()) {
				/* If it's a spectating battle, we remove the info view's bottom margin */
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)infoScroll.getLayoutParams();
				params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, 
						((RelativeLayout.LayoutParams)attackRow2.getLayoutParams()).bottomMargin);
				infoScroll.setLayoutParams(params);
			} else {
				teamLayout = getLayoutInflater().inflate(R.layout.battle_teamscreen, null);
				
		        for(int i = 0; i < 4; i++) {
		        	attackNames[i] = (TextView)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "Name", "id", pkgName));
		        	attackPPs[i] = (TextView)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "PP", "id", pkgName));
		        	attackLayouts[i] = (RelativeLayout)mainLayout.findViewById(resources.getIdentifier("attack" + (i+1) + "Layout", "id", pkgName));
		        	attackLayouts[i].setOnClickListener(battleListener);
		        	attackLayouts[i].setOnLongClickListener(moveListener);
		        }
		        for(int i = 0; i < 6; i++) {
		        	pokeListNames[i] = (TextView)teamLayout.findViewById(resources.getIdentifier("pokename" + (i+1), "id", pkgName));
		        	pokeListHPs[i] = (TextView)teamLayout.findViewById(resources.getIdentifier("hp" + (i+1), "id", pkgName));
		        	pokeListItems[i] = (TextView)teamLayout.findViewById(resources.getIdentifier("item" + (i+1), "id", pkgName));
		        	pokeListAbilities[i] = (TextView)teamLayout.findViewById(resources.getIdentifier("ability" + (i+1), "id", pkgName));
		        	pokeListButtons[i] = (RelativeLayout)teamLayout.findViewById(resources.getIdentifier("pokeViewLayout" + (i+1), "id", pkgName));
		        	pokeListButtons[i].setOnClickListener(battleListener);
		        	pokeListIcons[i] = (ImageView)teamLayout.findViewById(resources.getIdentifier("pokeViewIcon" + (i+1), "id", pkgName));
		        	
		        	for(int j = 0; j < 4; j++)
		        		pokeListMovePreviews[i][j] = (TextView)teamLayout.findViewById(resources.getIdentifier("p" + (i+1) + "_attack" + (j+1), "id", pkgName));
		        }
			}

			battleView.setBackgroundResource(resources.getIdentifier("bg" + battle.background, "drawable", pkgName));
			
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
		        pokeballs[me][i] = (ImageView)mainLayout.findViewById(resources.getIdentifier("pokeball" + (i + 1) + "B", "id", pkgName));
		        pokeballs[opp][i] = (ImageView)mainLayout.findViewById(resources.getIdentifier("pokeball" + (i + 1) + "A", "id", pkgName));
	        }
	        updatePokeballs();
	        
	        names[me].setText(battle.players[me].nick());
	        names[opp].setText(battle.players[opp].nick());

	        hpBars[me] = (TextProgressBar)mainLayout.findViewById(R.id.hpBarB);
	        hpBars[opp] = (TextProgressBar)mainLayout.findViewById(R.id.hpBarA);
	        
	        currentPokeNames[me] = (TextView)mainLayout.findViewById(R.id.currentPokeNameB);
	        currentPokeNames[opp] = (TextView)mainLayout.findViewById(R.id.currentPokeNameA);

	        currentPokeLevels[me] = (TextView)mainLayout.findViewById(R.id.currentPokeLevelB);
	        currentPokeLevels[opp] = (TextView)mainLayout.findViewById(R.id.currentPokeLevelA);
	        
	        currentPokeGenders[me] = (ImageView)mainLayout.findViewById(R.id.currentPokeGenderB);
	        currentPokeGenders[opp] = (ImageView)mainLayout.findViewById(R.id.currentPokeGenderA);
	        
	        currentPokeStatuses[me] = (ImageView)mainLayout.findViewById(R.id.currentPokeStatusB);
	        currentPokeStatuses[opp] = (ImageView)mainLayout.findViewById(R.id.currentPokeStatusA);
	        
	        pokeSprites[me] = (WebView)mainLayout.findViewById(R.id.pokeSpriteB);
	        pokeSprites[opp] = (WebView)mainLayout.findViewById(R.id.pokeSpriteA);
	        for(int i = 0; i < 2; i++) {
	        	pokeSprites[i].setOnLongClickListener(spriteListener);
	        	pokeSprites[i].setBackgroundColor(0);
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
	        updateMyPoke();
	        updateOppPoke(opp);
	        
	        // Enable or disable buttons
	        updateButtons();
	        
	    	// Start timer updating
	        handler.postDelayed(updateTimeTask, 100);
	        
			checkRearrangeTeamDialog();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			battle.activity = null;
			netServ = null;
		}
	};
	
	public void end() {
		runOnUiThread(new Runnable() { public void run() { BattleActivity.this.finish(); } } );
	}
  
    @Override
    public void onDestroy() {
    	unbindService(connection);
    	super.onDestroy();
    }

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
    };
    
    public OnClickListener battleListener = new OnClickListener() {
    	public void onClick(View v) {
    		int id = v.getId();
    		// Check to see if click was on attack button
    		for(int i = 0; i < 4; i++)
    			if(id == attackLayouts[i].getId())
    				netServ.socket.sendMessage(activeBattle.constructAttack((byte)i), Command.BattleMessage);
    		// Check to see if click was on pokelist button
    		for(int i = 0; i < 6; i++) {
    			if(id == pokeListButtons[i].getId()) {
    				netServ.socket.sendMessage(activeBattle.constructSwitch((byte)i), Command.BattleMessage);
    				realViewSwitcher.setCurrentItem(0, true);
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
    				showDialog(BattleDialog.MoveInfo.ordinal());
    				return true;
    			}
    		return false;
    	}
    };
    
    public OnLongClickListener spriteListener = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			if(v.getId() == pokeSprites[me].getId())
				showDialog(BattleDialog.MyDynamicInfo.ordinal());
			else
				showDialog(BattleDialog.OppDynamicInfo.ordinal());
			return true;
		}	
    };
    
    void setPokeListButtonEnabled(int num, boolean enabled) {
    	setLayoutEnabled(pokeListButtons[num], enabled);
    	setTextViewEnabled(pokeListNames[num], enabled);
    	setTextViewEnabled(pokeListItems[num], enabled);
    	setTextViewEnabled(pokeListAbilities[num], enabled);
    	setTextViewEnabled(pokeListHPs[num], enabled);
    	//setTextViewEnabled(pokeListIcons[num], enabled);
    	for(int i = 0; i < 4; i++)
    		setTextViewEnabled(pokeListMovePreviews[num][i], enabled);
    }
    
    void setAttackButtonEnabled(int num, boolean enabled) {
    	attackLayouts[num].setEnabled(enabled);
		attackNames[num].setEnabled(enabled);
		attackNames[num].setShadowLayer((float)1.5, 1, 1, resources.getColor(enabled ? R.color.poke_text_shadow_enabled : R.color.poke_text_shadow_disabled));
		attackPPs[num].setEnabled(enabled);
		attackPPs[num].setShadowLayer((float)1.5, 1, 1, resources.getColor(enabled ? R.color.pp_text_shadow_enabled : R.color.pp_text_shadow_disabled));
    }
    
    void setLayoutEnabled(ViewGroup v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.getBackground().setAlpha(enabled ? 255 : 128);
    }
    
    void setTextViewEnabled(TextView v, boolean enabled) {
    	v.setEnabled(enabled);
    	v.setTextColor(v.getTextColors().withAlpha(enabled ? 255 : 128).getDefaultColor());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(isSpectating() ? R.menu.spectatingbattleoptions : R.menu.battleoptions, menu);
        
        menu.findItem(R.id.sounds).setChecked(getSharedPreferences("battle", MODE_PRIVATE).getBoolean("pokemon_cries", true));
        /* No point in cancelling if no action done */
        if (!isSpectating() && !activeBattle.clicked) {
        	menu.findItem(R.id.cancel).setVisible(false);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.cancel:
    		netServ.socket.sendMessage(activeBattle.constructCancel(), Command.BattleMessage);
    		break;
    	case R.id.forfeit:
    		if (netServ != null && netServ.isBattling() && !battle.gotEnd)
    			showDialog(BattleDialog.ConfirmForfeit.ordinal());
    		break;
    	case R.id.close:
    		netServ.stopWatching(battle.bID);
    		break;
    	case R.id.draw:
    		netServ.socket.sendMessage(activeBattle.constructDraw(), Command.BattleMessage);
    		break;
    	case R.id.sounds:
    		item.setChecked(!item.isChecked());
    		getSharedPreferences("battle", Context.MODE_PRIVATE).edit().putBoolean("pokemon_cries", item.isChecked()).commit();
    		break;
        }
        return true;
    }

	public void notifyRearrangeTeamDialog() {
		runOnUiThread(new Runnable() { public void run() { checkRearrangeTeamDialog(); } } );
	}
	
	private void checkRearrangeTeamDialog() {
		if (netServ != null && netServ.isBattling() && battle.shouldShowPreview) {
			showDialog(BattleDialog.RearrangeTeam.ordinal());
		}
	}
    
	void endBattle() {
		if (netServ != null && netServ.socket != null && netServ.socket.isConnected() && netServ.isBattling()) {
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
        	View layout = inflater.inflate(R.layout.rearrange_team_dialog, (LinearLayout)findViewById(R.id.rearrange_team_dialog));
        	builder.setView(layout)
        	.setPositiveButton("Done", new DialogInterface.OnClickListener(){
        		public void onClick(DialogInterface dialog, int which) {
        			netServ.socket.sendMessage(activeBattle.constructRearrange(), Command.BattleMessage);
        			battle.shouldShowPreview = false;
        			removeDialog(id);
        		}})
        		.setCancelable(false);
        	dialog = builder.create();

        	mDragLayer = (DragLayer)layout.findViewById(R.id.drag_my_poke);
        	for(int i = 0; i < 6; i++){
        		BattlePoke poke = activeBattle.myTeam.pokes[i];
        		myArrangePokeIcons[i] = (PokeDragIcon)layout.findViewById(resources.getIdentifier("my_arrange_poke" + (i+1), "id", pkgName));
        		myArrangePokeIcons[i].setOnTouchListener(dialogListener);
        		myArrangePokeIcons[i].setImageDrawable(getIcon(poke.uID));
        		myArrangePokeIcons[i].num = i;
        		myArrangePokeIcons[i].battleActivity = this;

        		ShallowShownPoke oppPoke = activeBattle.oppTeam.pokes[i];
        		oppArrangePokeIcons[i] = (ImageView)layout.findViewById(resources.getIdentifier("foe_arrange_poke" + (i+1), "id", pkgName));
        		oppArrangePokeIcons[i].setImageDrawable(getIcon(oppPoke.uID));
        	}
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
        	if(netServ != null) {
        		final Dialog simpleDialog = new Dialog(this);
        		simpleDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        		simpleDialog.setContentView(R.layout.dynamic_info_layout);

        		TextView t = (TextView)simpleDialog.findViewById(R.id.nameTypeView); 
        		t.setText((player == me ? activeBattle.myTeam.pokes[0] : battle.currentPoke(player)).nameAndType());
				t = (TextView)simpleDialog.findViewById(R.id.statNamesView);
        		t.setText(battle.dynamicInfo[player].statsAndHazards());
        		t = (TextView)simpleDialog.findViewById(R.id.statNumsView);
        		if (player == me)
	        		t.setText(activeBattle.myTeam.pokes[0].printStats());
        		else
        			t.setVisibility(View.GONE);
        		t = (TextView)simpleDialog.findViewById(R.id.statBoostView);
        		String s = battle.dynamicInfo[player].boosts(player == me);
        		if (!"\n\n\n\n".equals(s))
        			t.setText(s);
        		else
        			t.setVisibility(View.GONE);
        		
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
        case MoveInfo:
        	dialog = builder.setTitle(lastClickedMove.name)
        	.setMessage(lastClickedMove.descAndEffects())
        	.setOnCancelListener(new DialogInterface.OnCancelListener() {
        		public void onCancel(DialogInterface dialog) {
        			removeDialog(id);
        		}
        	})
        	.create();
        	dialog.setCanceledOnTouchOutside(true);
        	return dialog;
        default:
            return new Dialog(this);
        }
    }
}