package com.pokebros.android.pokemononline;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ListIterator;

import com.pokebros.android.pokemononline.player.PlayerInfo;
import com.pokebros.android.pokemononline.poke.ShallowShownPoke;
import com.pokebros.android.pokemononline.poke.UniqueID;
import com.pokebros.android.pokemononline.battle.ChallengeEnums.*;
import de.marcreichelt.android.ChatRealViewSwitcher;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.view.KeyEvent;

public class ChatActivity extends Activity {
	
	static final String TAG = "ChatActivity";

	private enum ChatDialog {
		Challenge,
		AskForPass,
		ConfirmDisconnect,
		FindBattle,
		TierSelection,
		PlayerInfo,
		ChallengeMode
	}
	
	public final static int SWIPE_TIME_THRESHOLD = 100;
	
	private enum ChatContext {
		ChallengePlayer,
		ViewPlayerInfo,
		JoinChannel,
		LeaveChannel;
	}
	
	private PlayerListAdapter playerAdapter = null;
	private ChannelListAdapter channelAdapter = null;
	private MessageListAdapter messageAdapter = null;
	
	public ProgressDialog progressDialog;

	private ChatListView players;
	private ListView channels;
	private NetworkService netServ = null;
	private ListView chatView;
	private EditText chatInput;
	private ChatRealViewSwitcher chatViewSwitcher;
	private String packName = "com.pokebros.android.pokemononline";
	private PlayerInfo lastClickedPlayer;
	private Channel lastClickedChannel;
	private boolean loading = true;
	private SharedPreferences prefs;
	
	class TierAlertDialog extends AlertDialog {
		public Tier parentTier = null;
		public ListView dialogListView = null;
		
		protected TierAlertDialog(Context context, Tier t) {
			super(context);
			parentTier = t;
			dialogListView = makeTierListView();
			setTitle("Tier Selection");
			setView(dialogListView);
			setIcon(0); // Don't want an icon
		}
		
		@Override
		public void onBackPressed() {
			if(parentTier.parentTier == null) { // this is the top level
				dismiss();
			}
			else {
				dialogListView.setAdapter(new ArrayAdapter<Tier>(ChatActivity.this, R.layout.tier_list_item, parentTier.parentTier.subTiers));
				parentTier = parentTier.parentTier;
			}
		}
		
		ListView makeTierListView() {
			ListView lv = new ListView(ChatActivity.this);
			lv.setAdapter(new ArrayAdapter<Tier>(ChatActivity.this, R.layout.tier_list_item, parentTier.subTiers));
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Tier self = parentTier.subTiers.get((int)id);
					if(self.subTiers.size() > 0) {
						parentTier = self;
						((ListView)parent).setAdapter(new ArrayAdapter<Tier>(ChatActivity.this, 
								R.layout.tier_list_item, parentTier.subTiers));
					}
					else {
						Baos b = new Baos();
						b.putString(self.name);
						netServ.socket.sendMessage(b, Command.TierSelection);
						Toast.makeText(ChatActivity.this, "Tier Selected: " + self.name, Toast.LENGTH_SHORT).show();
						dismiss();
					}
				}
			});
			return lv;
		}
	}

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) { 
		System.out.println("CREATED CHAT ACTIVITY");
		if (loading) {
			progressDialog = ProgressDialog.show(ChatActivity.this, "","Loading. Please wait...", true);
			progressDialog.setCancelable(true);
			progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					disconnect();
				}
			});
		}
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        prefs = getPreferences(MODE_PRIVATE);
        chatView = (ListView) findViewById(R.id.chatView);
    	chatViewSwitcher = (ChatRealViewSwitcher)findViewById(R.id.chatPokeSwitcher);
    	chatViewSwitcher.setCurrentScreen(1);
 
    	//Player List Stuff**
    	players = (ChatListView)findViewById(R.id.playerlisting);
    	playerAdapter = new PlayerListAdapter(ChatActivity.this, 0);
    	registerForContextMenu(players);
        
        //Channel List Stuff**
        channels = (ListView)findViewById(R.id.channellisting);
        channelAdapter = new ChannelListAdapter(this, 0);
        registerForContextMenu(channels);
        channels.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Channel clicked = channelAdapter.getItem(position);
				if(netServ != null && netServ.joinedChannels.contains(clicked)) {
					// XXX remove is implemented as O(N) we could do it O(1) if we really had to
					netServ.joinedChannels.remove(clicked);
					netServ.joinedChannels.addFirst(clicked);
					populateUI(true);
				}
			}
		});
        
        bindService(new Intent(ChatActivity.this, NetworkService.class), connection,
        		Context.BIND_AUTO_CREATE);
        chatInput = (EditText) findViewById(R.id.chatInput);
		// Hide the soft-keyboard when the activity is created
        chatInput.setInputType(InputType.TYPE_NULL);
        chatInput.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				chatInput.setInputType(InputType.TYPE_CLASS_TEXT);
				chatInput.onTouchEvent(event);
				return true;
			}
		});
        chatInput.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
            	// and the socket is connected
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER) &&
                    netServ != null && netServ.socket != null &&
                    netServ.socket.isConnected()) {
                  // Perform action on key press
                	Baos b = new Baos();
                	b.putInt(netServ.joinedChannels.peek().id);
                	b.putString(chatInput.getText().toString());
                	//netServ.socket.sendMessage(b, Command.ChannelMessage);
                	chatInput.getText().clear();
                  return true;
                }
                return false;
            }
        });
	}
	
	@Override
	public void onResume() {
		super.onResume();
		chatViewSwitcher.setCurrentScreen(1);
		if (netServ != null && !netServ.hasBattle())
			netServ.showNotification(ChatActivity.class, "Chat");
		else if (netServ != null && netServ.hasBattle() && netServ.battle.gotEnd)
			netServ.battleActivity.endBattle();
		checkChallenges();
		checkAskForPass();
		checkFailedConnection();
	}

	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			netServ = ((NetworkService.LocalBinder)service).getService();
			if (!netServ.hasBattle())
				netServ.showNotification(ChatActivity.class, "Chat");
			updateTitle();
			netServ.chatActivity = ChatActivity.this;
			if (netServ.joinedChannels.peek() != null && !netServ.joinedChannels.isEmpty()) {
				populateUI(false);
				if (progressDialog.isShowing())
					progressDialog.dismiss();
				loading = false;
			}
	        checkChallenges();
	        checkAskForPass();
	        checkFailedConnection();
        }
		
		public void onServiceDisconnected(ComponentName className) {
			netServ.chatActivity = null;
			netServ = null;
		}
	};
	
	void updateTitle() {
		runOnUiThread(new Runnable() {
			public void run() {
				ChatActivity.this.setTitle(netServ.serverName);
			}
		});
	}
	
	public void populateUI(boolean clear) {
		Channel currentChannel = netServ.joinedChannels.peek();
		if (currentChannel != null) {
			// Populate the player list
			if (clear) {
				playerAdapter = new PlayerListAdapter(ChatActivity.this, 0);
				channelAdapter = new ChannelListAdapter(ChatActivity.this, 0);
			}
			messageAdapter = new MessageListAdapter(currentChannel, ChatActivity.this);
			Enumeration<PlayerInfo> e = netServ.joinedChannels.peek().players.elements();
			playerAdapter.setNotifyOnChange(false);
			while(e.hasMoreElements()) {
				playerAdapter.add(e.nextElement());
			}
			playerAdapter.sortByNick();
			playerAdapter.setNotifyOnChange(true);
			// Populate the Channel list
			Enumeration<Channel> c = netServ.channels.elements();
			channelAdapter.setNotifyOnChange(false);
			while(c.hasMoreElements())
				channelAdapter.addChannel(c.nextElement());
			channelAdapter.sortByName();
			channelAdapter.setNotifyOnChange(true);
			// Load scrollback
			runOnUiThread(new Runnable() {
				public void run() {
			    	players.setAdapter(playerAdapter);
			        channels.setAdapter(channelAdapter);
			        chatView.setAdapter(messageAdapter);
			        playerAdapter.notifyDataSetChanged();
					channelAdapter.notifyDataSetChanged();
					messageAdapter.notifyDataSetChanged();
					chatView.setSelection(messageAdapter.getCount() - 1);
					chatViewSwitcher.invalidate();
				}
			});
		}
	}
	
	public synchronized void updateChat() {
		if (messageAdapter == null)
			return;
		final int delta = messageAdapter.channel.lastSeen - messageAdapter.lastSeen;
		if (delta <= 0)
			return;
		Runnable update = new Runnable() {
			public void run() {
				LinkedList<SpannableStringBuilder> msgList = messageAdapter.channel.messageList;
				int top = messageAdapter.channel.messageList.size() - delta;
				ListIterator<SpannableStringBuilder> it = msgList.listIterator(top < Channel.HIST_LIMIT ? top : Channel.HIST_LIMIT);
				while (it.hasNext()) {
					SpannableStringBuilder next = it.next();
					messageAdapter.add(next);
					messageAdapter.lastSeen++;
				}
				messageAdapter.notifyDataSetChanged();
				if (chatView.getLastVisiblePosition() + delta == top || !ChatActivity.this.hasWindowFocus()) {
					chatView.setSelection(messageAdapter.getCount() - 1);
				}
				chatViewSwitcher.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				    public void onGlobalLayout() {
				    	chatViewSwitcher.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				        int heightDiff = chatViewSwitcher.getRootView().getHeight() - chatViewSwitcher.getHeight();
				        if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
				        	chatView.setSelection(messageAdapter.getCount() - 1);
				        }
				     }
				});
				
	            synchronized(this) {
	                this.notify();
	            }
			}
		};
		
		synchronized(update) {
			runOnUiThread(update);
			try {
				update.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void notifyChallenge() {
		runOnUiThread(new Runnable() { public void run() { checkChallenges(); } } );
	}
	
	private void checkChallenges() {
		if (netServ != null) {
			IncomingChallenge challenge = netServ.challenges.peek();
			if (challenge != null) {
				ChatActivity.this.showDialog(ChatDialog.Challenge.ordinal());
				netServ.noteMan.cancel(IncomingChallenge.note);
			}
		}
	}

	public void notifyAskForPass() {
		runOnUiThread(new Runnable() { public void run() { checkAskForPass(); } } );
	}
	
	private void checkAskForPass() {
		if (netServ != null && netServ.askedForPass)
			showDialog(ChatDialog.AskForPass.ordinal());
	}
	
	public void notifyFailedConnection() {
		disconnect();
	}
	
	private void checkFailedConnection() {
		if(netServ != null && netServ.failedConnect) {
			disconnect();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		switch (ChatDialog.values()[id]) {
		case Challenge: {
			if (netServ == null) {
				removeDialog(id);
				dismissDialog(id);
			}
			final IncomingChallenge challenge = netServ.challenges.poll();
			View challengedLayout = inflater.inflate(R.layout.player_info_dialog, (LinearLayout)findViewById(R.id.player_info_dialog));
			PlayerInfo opp = netServ.players.get(challenge.opponent);
			ImageView[] oppPokeIcons = new ImageView[6];
			TextView oppInfo, oppTeam, oppName, oppTier, oppRating;           
			builder.setView(challengedLayout)
			.setCancelable(false)
			.setNegativeButton(this.getString(R.string.decline), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Decline challenge
					if (netServ.socket.isConnected())
						netServ.socket.sendMessage(
								constructChallenge(ChallengeDesc.Refused.ordinal(),
										challenge.opponent,
										challenge.clauses,
										challenge.mode),
										Command.ChallengeStuff);
					removeDialog(id);
					checkChallenges();
				}
			})
			.setPositiveButton(this.getString(R.string.accept), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Accept challenge
					if (netServ.socket.isConnected())
						netServ.socket.sendMessage(
								constructChallenge(ChallengeDesc.Accepted.ordinal(),
										challenge.opponent,
										challenge.clauses,
										challenge.mode),
										Command.ChallengeStuff);
					// Without removeDialog() the dialog is reused and can only
					// be modified in onPrepareDialog(). This dialog changes
					// so much that I doubt it's worth the code to deal with
					// onPrepareDialog() but we should use it if we have complex
					// dialogs that only need to change a little
					removeDialog(id);
					checkChallenges();
				}
			});
			final AlertDialog oppInfoDialog = builder.create();


			for(int i = 0; i < 6; i++){
				oppPokeIcons[i] = (ImageView)challengedLayout.findViewById(getResources().getIdentifier("player_info_poke" + 
						(i+1), "id", packName));
				//oppPokeIcons[i].setImageDrawable(getIcon(opp.pokes[i]));
			}
			oppInfo = (TextView)challengedLayout.findViewById(getResources().getIdentifier("player_info", "id", packName));
			oppInfo.setText(Html.fromHtml("<b>Info: </b>" + NetworkService.escapeHtml(opp.info())));
			oppTeam = (TextView)challengedLayout.findViewById(getResources().getIdentifier("player_info_team", "id", packName));
			oppTeam.setText(opp.nick() + "'s team:");
			oppName = (TextView)challengedLayout.findViewById(getResources().getIdentifier("player_info_name", "id", packName));
			oppName.setText(this.getString(R.string.accept_challenge) + " " + opp.nick() + "?");
			oppName.setTextSize(18);
			oppTier = (TextView)challengedLayout.findViewById(getResources().getIdentifier("player_info_tier", "id", packName));
			//oppTier.setText(Html.fromHtml("<b>Tier: </b>" + NetworkService.escapeHtml(opp.tier)));
			oppRating = (TextView)challengedLayout.findViewById(getResources().getIdentifier("player_info_rating", "id", packName));
			//oppRating.setText(Html.fromHtml("<b>Rating: </b>" + NetworkService.escapeHtml(new Short(opp.rating).toString())));    

			return oppInfoDialog;
		} case AskForPass: {
        	//View layout = inflater.inflate(R.layout.ask_for_pass, null);
        	final EditText passField = new EditText(this);
        	passField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        	//passField.setTransformationMethod(PasswordTransformationMethod.getInstance());
			builder.setMessage("Please enter your password " + netServ.mePlayer.nick() + ".")
			.setCancelable(true)
			.setView(passField)
			.setPositiveButton("Done", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (netServ != null) {
						netServ.sendPass(passField.getText().toString());
					}
					removeDialog(id);
				}
			})
			.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					removeDialog(id);
					disconnect();
				}
			});
			final AlertDialog dialog = builder.create();
        	passField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					}
				}
			});
			return dialog;
		} case ConfirmDisconnect: {
			builder.setMessage("Really disconnect?")
			.setCancelable(true)
			.setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					disconnect();
				}
			})
			.setNegativeButton("Cancel", null);
			return builder.create();
		} case FindBattle: {
			final EditText range = new EditText(this);
			range.append("" + (prefs.contains("range") ? prefs.getInt("range", 0) : ""));
			range.setInputType(InputType.TYPE_CLASS_NUMBER);
			range.setHint("Range");
			builder.setTitle(R.string.find_a_battle)
			.setMultiChoiceItems(new CharSequence[]{"Force Rated", "Force Same Tier", "Only within range"}, new boolean[]{prefs.getBoolean("findOption0", false), prefs.getBoolean("findOption1", false), prefs.getBoolean("findOption2", false)}, new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					prefs.edit().putBoolean("findOption" + which, isChecked).commit();
				}
			})
			.setView(range)
			.setPositiveButton("Find", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (netServ != null && netServ.socket.isConnected()) {
						netServ.findingBattle = true;
						try {
							prefs.edit().putInt("range", new Integer(range.getText().toString()).intValue()).commit();
						} catch (NumberFormatException e) {
							prefs.edit().remove("range").commit();
						}
						netServ.socket.sendMessage(
								constructFindBattle(prefs.getBoolean("findOption0", false), prefs.getBoolean("findOption1", false), prefs.getBoolean("findOption2", false), prefs.getInt("range", 200), (byte) 0),
								Command.FindBattle);
					}
				}
			});
			return builder.create();
		} case TierSelection: {
			if (netServ == null) {
				dismissDialog(id);
			}
			return new TierAlertDialog(this, netServ.superTier);
		} case PlayerInfo: {
			View layout = inflater.inflate(R.layout.player_info_dialog, (LinearLayout)findViewById(R.id.player_info_dialog));
            ImageView[] pPokeIcons = new ImageView[6];
            TextView pInfo, pTeam, pName, pTier, pRating;           
			builder.setView(layout)
            .setNegativeButton("Back", new DialogInterface.OnClickListener(){
            	public void onClick(DialogInterface dialog, int which) {
            		removeDialog(id);
            	}
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
            	public void onCancel(DialogInterface dialog) {
            		removeDialog(id);
            	}
            })
            .setPositiveButton("Challenge", new DialogInterface.OnClickListener(){
            	public void onClick(DialogInterface dialog, int which) {
        			showDialog(ChatDialog.ChallengeMode.ordinal());
            		removeDialog(id);
            	}});
            final AlertDialog pInfoDialog = builder.create();
            

            for(int i = 0; i < 6; i++){
        	pPokeIcons[i] = (ImageView)layout.findViewById(getResources().getIdentifier("player_info_poke" + 
        			(i+1), "id", packName));
        	//pPokeIcons[i].setImageDrawable(getIcon(lastClickedPlayer.pokes[i]));
            }
        	pInfo = (TextView)layout.findViewById(getResources().getIdentifier("player_info", "id", packName));
        	pInfo.setText(Html.fromHtml("<b>Info: </b>" + NetworkService.escapeHtml(lastClickedPlayer.info())));
        	pTeam = (TextView)layout.findViewById(getResources().getIdentifier("player_info_team", "id", packName));
        	pTeam.setText(lastClickedPlayer.nick() + "'s team:");
        	pName = (TextView)layout.findViewById(getResources().getIdentifier("player_info_name", "id", packName));
        	pName.setText(lastClickedPlayer.nick());
        	pTier = (TextView)layout.findViewById(getResources().getIdentifier("player_info_tier", "id", packName));
        	//pTier.setText(Html.fromHtml("<b>Tier: </b>" + NetworkService.escapeHtml(lastClickedPlayer.tier)));
        	pRating = (TextView)layout.findViewById(getResources().getIdentifier("player_info_rating", "id", packName));
            //pRating.setText(Html.fromHtml("<b>Rating: </b>" + NetworkService.escapeHtml(new Short(lastClickedPlayer.rating).toString())));    
        	
            return pInfoDialog;
		} case ChallengeMode: {
            final Clauses[] clauses = Clauses.values();
            final int numClauses = clauses.length;
			final String[] clauseNames = new String[numClauses];
			final boolean[] checked = new boolean[numClauses];
			for (int i=0; i < numClauses; i++) {
				clauseNames[i] = clauses[i].toString();
				checked[i] = prefs.getBoolean("challengeOption" + i, false);
			}
            builder.setMultiChoiceItems(clauseNames, checked, new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					prefs.edit().putBoolean("challengeOption" + which, isChecked).commit();
				}
			})
			.setPositiveButton("Challenge", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					int clauses = 0;
					for (int i = 0; i < numClauses; i++)
						clauses |= (prefs.getBoolean("challengeOption" + i, false) ? Clauses.values()[i].mask() : 0);
					if (netServ != null && netServ.socket != null && netServ.socket.isConnected())
						netServ.socket.sendMessage(constructChallenge(ChallengeDesc.Sent.ordinal(), lastClickedPlayer.id, clauses, Mode.Singles.ordinal()), Command.ChallengeStuff);
					removeDialog(id);
				}
			})
            .setNegativeButton("Back", new DialogInterface.OnClickListener(){
            	public void onClick(DialogInterface dialog, int which) {
            		removeDialog(id);
            	}
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener(){
            	public void onCancel(DialogInterface dialog) {
            		removeDialog(id);
            	}
            })
            .setTitle("Select clauses");
            return builder.create();
		} default: {
			return new Dialog(this);
		}
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatoptions, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem findbattle = menu.findItem(R.id.findbattle);
    	if (netServ != null && netServ.findingBattle) {
    		findbattle.setTitle("Cancel Find Battle");
    	} 
    	else if(netServ != null && !netServ.hasBattle()) {
    		findbattle.setTitle(R.string.find_a_battle);
    	}
    	else if(netServ != null && netServ.hasBattle()) {
    		findbattle.setTitle(R.string.tobattlescreen);
    	}
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.chat_disconnect:
			showDialog(ChatDialog.ConfirmDisconnect.ordinal());
    		break;
		case R.id.findbattle:
			if (netServ.socket.isConnected()) {
				if (netServ.findingBattle && !netServ.hasBattle()) {
					netServ.findingBattle = false;
					netServ.socket.sendMessage(
							constructChallenge(ChallengeDesc.Cancelled.ordinal(), 0, Clauses.SleepClause.mask(), Mode.Singles.ordinal()),
							Command.ChallengeStuff);
				} else if(!netServ.findingBattle && netServ.hasBattle()) {
	    			Intent in = new Intent(this, BattleActivity.class);
	    			in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    			startActivity(in);
	    			break;
	    		} 
				else {
					showDialog(ChatDialog.FindBattle.ordinal());
				}
			}
			break;
		case R.id.preferences:
			//TODO: Make actual preferences menu
			// Launch Preference activity
			//Toast.makeText(ChatActivity.this, "Preferences not Implemented Yet",
            //        Toast.LENGTH_SHORT).show();
			showDialog(ChatDialog.TierSelection.ordinal());
			break;
    	}
    	return true;
    }

    public void makeToast(final String s, final String length) {
    	if (length == "long") {
    	runOnUiThread(new Runnable() {
    		public void run() {
    			Toast.makeText(ChatActivity.this, s, Toast.LENGTH_LONG).show();
    		}
    	});
    	}
    	else if(length == "short") {
        	runOnUiThread(new Runnable() {
        		public void run() {
        			Toast.makeText(ChatActivity.this, s, Toast.LENGTH_SHORT).show();
        		}
        	});
    	}
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	AdapterView.AdapterContextMenuInfo aMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
    	switch(v.getId()){
    	case R.id.playerlisting:
    		lastClickedPlayer = playerAdapter.getItem(aMenuInfo.position);
    		String pName = lastClickedPlayer.nick();
    		menu.setHeaderTitle(pName);
    		menu.add(Menu.NONE, ChatContext.ChallengePlayer.ordinal(), 0, "Challenge " + pName);
    		menu.add(Menu.NONE, ChatContext.ViewPlayerInfo.ordinal(), 0, "View Player Info");
    		break;
    	case R.id.channellisting:
    		lastClickedChannel = channelAdapter.getItem(aMenuInfo.position);
    		String cName = lastClickedChannel.name;
    		menu.setHeaderTitle(cName);
    		if (netServ.joinedChannels.contains(lastClickedChannel))
    			menu.add(Menu.NONE, ChatContext.LeaveChannel.ordinal(), 0, "Leave " + cName);
    		else
        		menu.add(Menu.NONE, ChatContext.JoinChannel.ordinal(), 0, "Join " + cName);
    		break;
    	}
    }
      
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	switch(ChatContext.values()[item.getItemId()]){
    	case ChallengePlayer:
    		showDialog(ChatDialog.ChallengeMode.ordinal());
    		break;
    	case ViewPlayerInfo:
    		showDialog(ChatDialog.PlayerInfo.ordinal());
    		break;
    	case JoinChannel:
    		Baos join = new Baos();
    		join.putString(lastClickedChannel.name);
    		if (netServ != null && netServ.socket != null && netServ.socket.isConnected())
    			netServ.socket.sendMessage(join, Command.JoinChannel);
    		break;
    	case LeaveChannel:
    		Baos leave = new Baos();
    		leave.putInt(lastClickedChannel.id);
    		if (netServ != null && netServ.socket != null && netServ.socket.isConnected())
    			netServ.socket.sendMessage(leave, Command.LeaveChannel);
    		break;
    	}
    	return true;
    }


    private void disconnect() {
		if (netServ != null)
			netServ.disconnect();
		if (progressDialog != null)
			progressDialog.dismiss();
		Intent intent = new Intent(this, RegistryActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("sticky", true);
		
		if(netServ == null || netServ.socket == null)
			intent.putExtra("failedConnect", true);
		startActivity(intent);
		ChatActivity.this.finish();
    }
    
    private Baos constructChallenge(int desc, int opp, int clauses, int mode) {
    	Baos challenge = new Baos();
    	challenge.write(desc);
    	challenge.putInt(opp);
    	challenge.putInt(clauses);
    	challenge.write(mode);
    	return challenge;
    }
    
    private Baos constructFindBattle(boolean forceRated, boolean forceSameTier,
    		boolean onlyInRange, int i, byte mode) {
		Baos find = new Baos();
		int flags = 0;
		flags |= (forceRated ? 1:0);
		flags |= (forceSameTier ? 2:0);
		flags |= (onlyInRange ? 4:0);
		find.putInt(flags);
		find.putShort((short)i);
		find.write(mode);
		return find;
    }

	public void removePlayer(final PlayerInfo pi){
		synchronized(players) {
			if (players.isPressed()) {
				try {
					players.wait();
				} catch (InterruptedException e) {}
			}
		}
		runOnUiThread(new Runnable() {
			public void run() {
				synchronized(playerAdapter) {
					playerAdapter.remove(pi);
				}
			}
		});
	}
	
	public void addPlayer(final PlayerInfo pi) {
		synchronized(players) {
			if (players.isPressed()) {
				try {
					players.wait();
				} catch (InterruptedException e) {}
			}
		}
		runOnUiThread(new Runnable() {
			public void run() {
				synchronized(playerAdapter) {
					playerAdapter.add(pi);
				}
			}
		});
	}
	
	public void updatePlayer(final PlayerInfo pi) {
		runOnUiThread(new Runnable() {
			public void run() {
				synchronized (playerAdapter) {
					playerAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	public void removeChannel(final Channel ch){
		runOnUiThread(new Runnable() {
			public void run() {
            	channelAdapter.removeChannel(ch);
			}
		});
	}
	
	public void addChannel(final Channel ch) {
		runOnUiThread(new Runnable() {
			public void run() {
            	channelAdapter.addChannel(ch);
			}
		});
	}
	
	private Drawable getIcon(UniqueID uid) {
		Resources resources = getResources();
		int resID = resources.getIdentifier("pi" + uid.pokeNum +
				(uid.subNum == 0 ? "" : "_" + uid.subNum) +
				"_icon", "drawable", packName);
		if (resID == 0)
			resID = resources.getIdentifier("pi" + uid.pokeNum + "_icon",
					"drawable", packName);
		return resources.getDrawable(resID);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		chatViewSwitcher.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				chatViewSwitcher.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				chatViewSwitcher.setCurrentScreen(1);
				if (messageAdapter != null)
					chatView.setSelection(messageAdapter.getCount() - 1);
			}
		});
	}
	
	@Override
    public void onDestroy() {
    	unbindService(connection);
    	super.onDestroy();
    }
}


