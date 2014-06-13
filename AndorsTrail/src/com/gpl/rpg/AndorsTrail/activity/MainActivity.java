package com.gpl.rpg.AndorsTrail.activity;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.Dialogs;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.controller.AttackResult;
import com.gpl.rpg.AndorsTrail.controller.CombatController;
import com.gpl.rpg.AndorsTrail.controller.listeners.CombatActionListener;
import com.gpl.rpg.AndorsTrail.controller.listeners.CombatTurnListener;
import com.gpl.rpg.AndorsTrail.controller.listeners.PlayerMovementListener;
import com.gpl.rpg.AndorsTrail.controller.listeners.WorldEventListener;
import com.gpl.rpg.AndorsTrail.model.actor.Monster;
import com.gpl.rpg.AndorsTrail.model.actor.Player;
import com.gpl.rpg.AndorsTrail.model.item.ItemContainer.ItemEntry;
import com.gpl.rpg.AndorsTrail.model.item.Loot;
import com.gpl.rpg.AndorsTrail.model.map.MapObject;
import com.gpl.rpg.AndorsTrail.model.map.PredefinedMap;
import com.gpl.rpg.AndorsTrail.savegames.Savegames;
import com.gpl.rpg.AndorsTrail.twinsprite.ToyxManager;
import com.gpl.rpg.AndorsTrail.util.Coord;
import com.gpl.rpg.AndorsTrail.view.CombatView;
import com.gpl.rpg.AndorsTrail.view.DisplayActiveActorConditionIcons;
import com.gpl.rpg.AndorsTrail.view.MainView;
import com.gpl.rpg.AndorsTrail.view.QuickButton.QuickButtonContextMenuInfo;
import com.gpl.rpg.AndorsTrail.view.QuickitemView;
import com.gpl.rpg.AndorsTrail.view.StatusView;
import com.gpl.rpg.AndorsTrail.view.ToolboxView;
import com.gpl.rpg.AndorsTrail.view.VirtualDpadView;
import com.twinsprite.TwinspriteException;
import com.twinsprite.callback.CreateSessionCallback;
import com.twinsprite.callback.GetCallback;
import com.twinsprite.entity.Toyx;

public final class MainActivity extends Activity implements PlayerMovementListener, CombatActionListener,
		CombatTurnListener, WorldEventListener {

	public static final int INTENTREQUEST_MONSTERENCOUNTER = 2;
	public static final int INTENTREQUEST_CONVERSATION = 4;
	public static final int INTENTREQUEST_SAVEGAME = 8;
	public static final int INTENTREQUEST_TWINSPRITE = 10;

	private ControllerContext controllers;
	public WorldContext world;

	private MainView mainview;
	private StatusView statusview;
	private CombatView combatview;
	private QuickitemView quickitemview;
	private DisplayActiveActorConditionIcons activeConditions;
	private ToolboxView toolboxview;

	private TextView statusText;
	private WeakReference<Toast> lastToast = null;
	private ContextMenuInfo lastSelectedMenu = null;

	private static final Map<Byte, String> URI_PREFIX_MAP = new HashMap<Byte, String>();
	private Activity currentActivity;
	private NfcAdapter mAdapter = null;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	private AndorsTrailApplication app;
	private ProgressDialog progress = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		currentActivity = this;
		URI_PREFIX_MAP.put((byte) 0x00, "");
		URI_PREFIX_MAP.put((byte) 0x01, "http://www.");
		URI_PREFIX_MAP.put((byte) 0x02, "https://www.");
		URI_PREFIX_MAP.put((byte) 0x03, "http://");
		URI_PREFIX_MAP.put((byte) 0x04, "https://");
		URI_PREFIX_MAP.put((byte) 0x05, "tel:");
		URI_PREFIX_MAP.put((byte) 0x06, "mailto:");
		URI_PREFIX_MAP.put((byte) 0x07, "ftp://anonymous:anonymous@");
		URI_PREFIX_MAP.put((byte) 0x08, "ftp://ftp.");
		URI_PREFIX_MAP.put((byte) 0x09, "ftps://");
		URI_PREFIX_MAP.put((byte) 0x0A, "sftp://");
		URI_PREFIX_MAP.put((byte) 0x0B, "smb://");
		URI_PREFIX_MAP.put((byte) 0x0C, "nfs://");
		URI_PREFIX_MAP.put((byte) 0x0D, "ftp://");
		URI_PREFIX_MAP.put((byte) 0x0E, "dav://");
		URI_PREFIX_MAP.put((byte) 0x0F, "news:");
		URI_PREFIX_MAP.put((byte) 0x10, "telnet://");
		URI_PREFIX_MAP.put((byte) 0x11, "imap:");
		URI_PREFIX_MAP.put((byte) 0x12, "rtsp://");
		URI_PREFIX_MAP.put((byte) 0x13, "urn:");
		URI_PREFIX_MAP.put((byte) 0x14, "pop:");
		URI_PREFIX_MAP.put((byte) 0x15, "sip:");
		URI_PREFIX_MAP.put((byte) 0x16, "sips:");
		URI_PREFIX_MAP.put((byte) 0x17, "tftp:");
		URI_PREFIX_MAP.put((byte) 0x18, "btspp://");
		URI_PREFIX_MAP.put((byte) 0x19, "btl2cap://");
		URI_PREFIX_MAP.put((byte) 0x1A, "btgoep://");
		URI_PREFIX_MAP.put((byte) 0x1B, "tcpobex://");
		URI_PREFIX_MAP.put((byte) 0x1C, "irdaobex://");
		URI_PREFIX_MAP.put((byte) 0x1D, "file://");
		URI_PREFIX_MAP.put((byte) 0x1E, "urn:epc:id:");
		URI_PREFIX_MAP.put((byte) 0x1F, "urn:epc:tag:");
		URI_PREFIX_MAP.put((byte) 0x20, "urn:epc:pat:");
		URI_PREFIX_MAP.put((byte) 0x21, "urn:epc:raw:");
		URI_PREFIX_MAP.put((byte) 0x22, "urn:epc:");
		URI_PREFIX_MAP.put((byte) 0x23, "urn:nfc:");

		app = AndorsTrailApplication.getApplicationFromActivity(this);
		if (!app.isInitialized()) {
			finish();
			return;
		}
		AndorsTrailPreferences preferences = app.getPreferences();
		this.world = app.getWorld();
		this.controllers = app.getControllerContext();
		app.setWindowParameters(this);

		setContentView(R.layout.main);
		mainview = (MainView) findViewById(R.id.main_mainview);
		statusview = (StatusView) findViewById(R.id.main_statusview);
		combatview = (CombatView) findViewById(R.id.main_combatview);
		quickitemview = (QuickitemView) findViewById(R.id.main_quickitemview);
		activeConditions = new DisplayActiveActorConditionIcons(controllers, world, this,
				(RelativeLayout) findViewById(R.id.statusview_activeconditions));
		VirtualDpadView dpad = (VirtualDpadView) findViewById(R.id.main_virtual_dpad);
		toolboxview = (ToolboxView) findViewById(R.id.main_toolboxview);
		statusview.registerToolboxViews(toolboxview, quickitemview);

		statusText = (TextView) findViewById(R.id.statusview_statustext);
		statusText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				statusText.setVisibility(View.GONE);
			}
		});
		clearMessages();

		if (AndorsTrailApplication.DEVELOPMENT_DEBUGBUTTONS)
			new DebugInterface(controllers, world, this).addDebugButtons();

		quickitemview.setVisibility(View.GONE);
		quickitemview.registerForContextMenu(this);

		dpad.updateVisibility(preferences);
		quickitemview.setPosition(preferences);

		// Define which views are in front of each other.
		dpad.bringToFront();
		quickitemview.bringToFront();
		toolboxview.bringToFront();
		combatview.bringToFront();
		statusview.bringToFront();

		this.progress = new ProgressDialog(this);
		RunNFC();
	}

	public void RunNFC() {
		Log.i("TWINSPRITE", "RUNNFC");
		if (mAdapter != null) {
			return;
		}

		NfcManager manager = (NfcManager) currentActivity.getSystemService(Context.NFC_SERVICE);
		mAdapter = manager.getDefaultAdapter();

		// Create a generic PendingIntent that will be deliver to this activity.
		// The NFC stack
		// will fill in the intent with the details of the discovered tag before
		// delivering to
		// this activity.
		mPendingIntent = PendingIntent.getActivity(currentActivity, 0,
				new Intent(currentActivity, currentActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		// Setup an intent filter for all MIME based dispatches (TEXT);
		IntentFilter ndefText = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefText.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			Log.e("TWINSPRITE", "MALFORMED MIME TYPE!");
			throw new RuntimeException("fail", e);
		}

		// Setup an intent filter for all MIME based dispatches (URI);
		IntentFilter ndefURI = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefURI.addDataScheme("https");
		} catch (Exception e) {
			Log.e("TWINSPRITE", "URI SCHEME EXCEPTION!");
			throw new RuntimeException("fail", e);
		}

		mFilters = new IntentFilter[] { ndefText, ndefURI };

		// Setup a tech list for all NfcF tags
		mTechLists = new String[][] { new String[] { NfcF.class.getName() } };
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case INTENTREQUEST_MONSTERENCOUNTER:
			if (resultCode == Activity.RESULT_OK) {
				controllers.combatController.enterCombat(CombatController.BeginTurnAs.player);
			} else {
				controllers.combatController.exitCombat(false);
			}
			break;
		case INTENTREQUEST_CONVERSATION:
			controllers.mapController.applyCurrentMapReplacements(getResources(), true);
			break;
		case INTENTREQUEST_TWINSPRITE:
			if (resultCode != Activity.RESULT_OK)
				break;
			final int action = data.getIntExtra("action", -1);
			if (TwinspriteActivity.INTENTREQUEST_SCAN == action) {
				this.updateNewPlayer();
			}
			break;
		case INTENTREQUEST_SAVEGAME:
			if (resultCode != Activity.RESULT_OK)
				break;
			final int slot = data.getIntExtra("slot", 1);
			if (save(slot)) {
				Toast.makeText(this, getResources().getString(R.string.menu_save_gamesaved, slot), Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(this, R.string.menu_save_failed, Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	private void updateNewPlayer() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				controllers.actorStatsController.recalculatePlayerStats(world.model.player);
				statusview.updateIcon(world.model.player.canLevelup());
				world.model.player.nextPosition.x = world.model.player.position.x;
				world.model.player.nextPosition.y = world.model.player.position.y;
				controllers.movementController.moveToNextIfPossible();
			}
		});
	}

	private boolean save(int slot) {
		final Player player = world.model.player;
		return Savegames.saveWorld(
				world,
				this,
				slot,
				getString(R.string.savegame_currenthero_displayinfo, player.getLevel(), player.getTotalExperience(),
						player.getGold()));
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!AndorsTrailApplication.getApplicationFromActivity(this).getWorldSetup().isSceneReady)
			return;
		subscribeToModelChanges();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unsubscribeFromModel();
	}

	@Override
	protected void onPause() {
		super.onPause();
		controllers.gameRoundController.pause();
		controllers.movementController.stopMovement();

		save(Savegames.SLOT_QUICKSAVE);

		if (mAdapter != null) {
			mAdapter.disableForegroundDispatch(currentActivity);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!AndorsTrailApplication.getApplicationFromActivity(this).getWorldSetup().isSceneReady)
			return;

		controllers.gameRoundController.resume();

		updateStatus();

		if (mAdapter != null) {
			mAdapter.enableForegroundDispatch(currentActivity, mPendingIntent, mFilters, mTechLists);
		}
	}

	private void unsubscribeFromModel() {
		activeConditions.unsubscribe();
		combatview.unsubscribe();
		mainview.unsubscribe();
		quickitemview.unsubscribe();
		statusview.unsubscribe();
		controllers.movementController.playerMovementListeners.remove(this);
		controllers.combatController.combatActionListeners.remove(this);
		controllers.combatController.combatTurnListeners.remove(this);
		controllers.mapController.worldEventListeners.remove(this);
	}

	private void subscribeToModelChanges() {
		controllers.mapController.worldEventListeners.add(this);
		controllers.combatController.combatTurnListeners.add(this);
		controllers.combatController.combatActionListeners.add(this);
		controllers.movementController.playerMovementListeners.add(this);
		statusview.subscribe();
		quickitemview.subscribe();
		mainview.subscribe();
		combatview.subscribe();
		activeConditions.subscribe();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (quickitemview.isQuickButtonId(v.getId())) {
			createQuickButtonMenu(menu);
		}
		lastSelectedMenu = null;
	}

	private void createQuickButtonMenu(ContextMenu menu) {
		menu.add(Menu.NONE, R.id.quick_menu_unassign, Menu.NONE, R.string.inventory_unassign);
		SubMenu assignMenu = menu.addSubMenu(Menu.NONE, R.id.quick_menu_assign, Menu.NONE, R.string.inventory_assign);
		for (int i = 0; i < world.model.player.inventory.items.size(); ++i) {
			ItemEntry itemEntry = world.model.player.inventory.items.get(i);
			if (itemEntry.itemType.isUsable())
				assignMenu.add(R.id.quick_menu_assign_group, i, Menu.NONE,
						itemEntry.itemType.getName(world.model.player));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		QuickButtonContextMenuInfo menuInfo;
		if (item.getGroupId() == R.id.quick_menu_assign_group) {
			menuInfo = (QuickButtonContextMenuInfo) lastSelectedMenu;
			controllers.itemController.setQuickItem(world.model.player.inventory.items.get(item.getItemId()).itemType,
					menuInfo.index);
			return true;
		}
		switch (item.getItemId()) {
		case R.id.quick_menu_unassign:
			menuInfo = (QuickButtonContextMenuInfo) item.getMenuInfo();
			controllers.itemController.setQuickItem(null, menuInfo.index);
			break;
		case R.id.quick_menu_assign:
			menuInfo = (QuickButtonContextMenuInfo) item.getMenuInfo();
			lastSelectedMenu = menuInfo;
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void updateStatus() {
		statusview.updateStatus();
		quickitemview.refreshQuickitems();
		combatview.updateStatus();
		toolboxview.updateIcons();
	}

	private void message(String msg) {
		world.model.combatLog.append(msg);
		statusText.setText(world.model.combatLog.getLastMessages());
		statusText.setVisibility(View.VISIBLE);
	}

	private void clearMessages() {
		world.model.combatLog.appendCombatEnded();
		statusText.setVisibility(View.GONE);
	}

	private void showToast(String msg, int duration) {
		if (msg == null)
			return;
		if (msg.length() == 0)
			return;
		Toast t = null;
		if (lastToast != null)
			t = lastToast.get();
		if (t == null) {
			t = Toast.makeText(this, msg, duration);
			lastToast = new WeakReference<Toast>(t);
		} else {
			t.setText(msg);
			t.setDuration(duration);
		}
		t.show();
	}

	@Override
	public void onPlayerMoved(Coord newPosition, Coord previousPosition) {
	}

	@Override
	public void onPlayerEnteredNewMap(PredefinedMap map, Coord p) {
	}

	@Override
	public void onCombatStarted() {
		clearMessages();
	}

	@Override
	public void onCombatEnded() {
		clearMessages();
	}

	@Override
	public void onPlayerAttackMissed(Monster target, AttackResult attackResult) {
		message(getString(R.string.combat_result_heromiss));
	}

	@Override
	public void onPlayerAttackSuccess(Monster target, AttackResult attackResult) {
		final String monsterName = target.getName();
		if (attackResult.isCriticalHit) {
			message(getString(R.string.combat_result_herohitcritical, monsterName, attackResult.damage));
		} else {
			message(getString(R.string.combat_result_herohit, monsterName, attackResult.damage));
		}
		if (attackResult.targetDied) {
			message(getString(R.string.combat_result_herokillsmonster, monsterName, attackResult.damage));
		}
	}

	@Override
	public void onMonsterAttackMissed(Monster attacker, AttackResult attackResult) {
		message(getString(R.string.combat_result_monstermiss, attacker.getName()));
	}

	@Override
	public void onMonsterAttackSuccess(Monster attacker, AttackResult attackResult) {
		final String monsterName = attacker.getName();
		if (attackResult.isCriticalHit) {
			message(getString(R.string.combat_result_monsterhitcritical, monsterName, attackResult.damage));
		} else {
			message(getString(R.string.combat_result_monsterhit, monsterName, attackResult.damage));
		}
	}

	@Override
	public void onMonsterMovedDuringCombat(Monster m) {
		String monsterName = m.getName();
		message(getString(R.string.combat_result_monstermoved, monsterName));
	}

	@Override
	public void onPlayerKilledMonster(Monster target) {
	}

	@Override
	public void onNewPlayerTurn() {
	}

	@Override
	public void onMonsterIsAttacking(Monster m) {
	}

	@Override
	public void onPlayerStartedConversation(Monster m, String phraseID) {
		Dialogs.showConversation(this, controllers, phraseID, m);
	}

	@Override
	public void onScriptAreaStartedConversation(String phraseID) {
		Dialogs.showMapScriptMessage(this, controllers, phraseID);
	}

	@Override
	public void onPlayerSteppedOnMonster(Monster m) {
		Dialogs.showMonsterEncounter(this, controllers, m);
	}

	@Override
	public void onPlayerSteppedOnMapSignArea(MapObject area) {
		Dialogs.showMapSign(this, controllers, area.id);
	}

	@Override
	public void onPlayerSteppedOnKeyArea(MapObject area) {
		Dialogs.showKeyArea(this, controllers, area.id);
	}

	@Override
	public void onPlayerSteppedOnRestArea(MapObject area) {
		Dialogs.showConfirmRest(this, controllers, area);
	}

	@Override
	public void onPlayerSteppedOnGroundLoot(Loot loot) {
		final String msg = Dialogs.getGroundLootFoundMessage(this, loot);
		Dialogs.showGroundLoot(this, controllers, world, loot, msg);
	}

	@Override
	public void onPlayerPickedUpGroundLoot(Loot loot) {
		if (controllers.preferences.displayLoot == AndorsTrailPreferences.DISPLAYLOOT_NONE)
			return;
		if (!showToastForPickedUpItems(loot))
			return;

		final String msg = Dialogs.getGroundLootPickedUpMessage(this, loot);
		showToast(msg, Toast.LENGTH_LONG);
	}

	private boolean showToastForPickedUpItems(Loot loot) {
		switch (controllers.preferences.displayLoot) {
		case AndorsTrailPreferences.DISPLAYLOOT_TOAST:
		case AndorsTrailPreferences.DISPLAYLOOT_DIALOG_FOR_ITEMS_ELSE_TOAST:
			return true;
		case AndorsTrailPreferences.DISPLAYLOOT_TOAST_FOR_ITEMS:
			return loot.hasItems();
		}
		return false;
	}

	@Override
	public void onPlayerFoundMonsterLoot(Collection<Loot> loot, int exp) {
		final Loot combinedLoot = Loot.combine(loot);
		final String msg = Dialogs.getMonsterLootFoundMessage(this, combinedLoot, exp);
		Dialogs.showMonsterLoot(this, controllers, world, loot, combinedLoot, msg);
	}

	@Override
	public void onPlayerPickedUpMonsterLoot(Collection<Loot> loot, int exp) {
		if (controllers.preferences.displayLoot == AndorsTrailPreferences.DISPLAYLOOT_NONE)
			return;

		final Loot combinedLoot = Loot.combine(loot);
		if (!showToastForPickedUpItems(combinedLoot))
			return;

		final String msg = Dialogs.getMonsterLootPickedUpMessage(this, combinedLoot, exp);
		showToast(msg, Toast.LENGTH_LONG);
	}

	@Override
	public void onPlayerRested() {
		Dialogs.showRested(this, controllers);
	}

	@Override
	public void onPlayerDied(int lostExp) {
		message(getString(R.string.combat_hero_dies, lostExp));
	}

	@Override
	public void onPlayerStartedFleeing() {
		message(getString(R.string.combat_begin_flee));
	}

	@Override
	public void onPlayerFailedFleeing() {
		message(getString(R.string.combat_flee_failed));
	}

	@Override
	public void onPlayerDoesNotHaveEnoughAP() {
		message(getString(R.string.combat_not_enough_ap));
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Log.i("TWINSPRITE", "NEW INTENT");

		String nfcText = "";

		Parcelable[] ndefMessages = (Parcelable[]) (intent.getParcelableArrayExtra("android.nfc.extra.NDEF_MESSAGES"));
		if (ndefMessages != null) {
			Log.i("TWINSPRITE", "MESSAGES: " + ndefMessages.length);
			try {
				for (int i = 0; i < ndefMessages.length; i++) {
					NdefMessage ndefMessage = (NdefMessage) (ndefMessages[i]);
					NdefRecord[] ndefRecords = ndefMessage.getRecords();
					Log.i("TWINSPRITE", "RECORDS: " + ndefRecords.length);

					for (int j = 0; j < ndefRecords.length; j++) {
						Log.i("TWINSPRITE", "TNF: " + ndefRecords[j].getTnf());
						Log.i("TWINSPRITE", "TYPE: " + ndefRecords[j].getType());
						if (ndefRecords[j].getTnf() == NdefRecord.TNF_WELL_KNOWN
								&& Arrays.equals(ndefRecords[j].getType(), NdefRecord.RTD_TEXT)) {
							Log.i("TWINSPRITE", "TIPO TEXTO");
							/*
							 * payload[0] contains the "Status Byte Encodings"
							 * field, per the NFC Forum
							 * "Text Record Type Definition" section 3.2.1.
							 * 
							 * bit7 is the Text Encoding Field.
							 * 
							 * if (Bit_7 == 0): The text is encoded in UTF-8 if
							 * (Bit_7 == 1): The text is encoded in UTF16
							 * 
							 * Bit_6 is reserved for future use and must be set
							 * to zero.
							 * 
							 * Bits 5 to 0 are the length of the IANA language
							 * code.
							 */
							try {
								byte[] payLoad = ndefRecords[0].getPayload();

								// Get the Text Encoding
								String textEncoding = ((payLoad[0] & 0200) == 0) ? "UTF-8" : "UTF-16";

								// Get the Language Code
								int languageCodeLength = payLoad[0] & 0077;
								// String languageCode = new String(payLoad, 1,
								// languageCodeLength, "US-ASCII");

								// Get the Text
								nfcText = new String(payLoad, languageCodeLength + 1, payLoad.length
										- languageCodeLength - 1, textEncoding);

							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						} else if (ndefRecords[j].getTnf() == NdefRecord.TNF_WELL_KNOWN
								&& Arrays.equals(ndefRecords[j].getType(), NdefRecord.RTD_URI)) {

							Log.i("TWINSPRITE", "TIPO URI");
							/*
							 * See NFC forum specification for
							 * "URI Record Type Definition" at 3.2.2
							 * 
							 * http://www.nfc-forum.org/specs/
							 * 
							 * payload[0] contains the URI Identifier Code
							 * payload[1]...payload[payload.length - 1] contains
							 * the rest of the URI.
							 */
							byte[] payload = ndefRecords[j].getPayload();
							String prefix = (String) URI_PREFIX_MAP.get(payload[0]);
							Log.i("TWINSPRITE", "URI PREFIX: " + prefix);
							byte prefBytes[] = prefix.getBytes(Charset.forName("UTF-8"));
							byte postBytes[] = Arrays.copyOfRange(payload, 1, payload.length);

							byte[] fullUri = new byte[prefBytes.length + postBytes.length];
							System.arraycopy(prefBytes, 0, fullUri, 0, prefBytes.length);
							System.arraycopy(postBytes, 0, fullUri, prefBytes.length, postBytes.length);

							nfcText = new String(fullUri, Charset.forName("UTF-8"));
						}
					}
				}

				Log.i("TWINSPRITE", "nfcText: " + nfcText);
			} catch (Exception e) {
				Log.e("TWINSPRITE", e.toString());
			}
		}

		if (!nfcText.isEmpty()) {
			this.loadToyx(nfcText);
		}
	}

	private void loadToyx(String toyxid) {

		if (toyxid.startsWith(TwinspriteActivity.TWINSPRITE_SCAN_BASE_URI)) {
			toyxid = toyxid.replace(TwinspriteActivity.TWINSPRITE_SCAN_BASE_URI, "");
		}

		// show progress dialog
		progress.setTitle("Loading");
		progress.setMessage("Fetching toyx...");
		progress.show();

		app.setToyx(new Toyx(toyxid));

		app.getToyx().createSessionInBackground(new CreateSessionCallback() {
			@Override
			public void onCreateSession(TwinspriteException e) {
				progress.dismiss();
				if (e == null) {
					app.getToyx().fetchInBackground(new GetCallback() {
						@Override
						public void onFetch(Toyx toyx, TwinspriteException e) {
							progress.dismiss();
							if (e == null) {
								app.setToyx(toyx);
								Log.i("Twinsprie", "Toyx " + toyx.getToyxId() + " fetched successfully.");
								ToyxManager.loadPlayer(app.getToyx(), world.model.player);
								updateNewPlayer();
							} else {
								MainActivity.this.showDialog(
										getResources().getString(R.string.twinsprite_fetch_failed),
										e.getDetailMessage());
							}
						}
					});
				} else {
					MainActivity.this.showDialog(getResources().getString(R.string.twinsprite_session_failed),
							e.getDetailMessage());
				}
			}
		});
	}

	public void showDialog(final String title, final String message) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

				// set title
				alertDialogBuilder.setTitle(title);

				// set dialog message
				alertDialogBuilder.setMessage(message).setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// close dialog
							}
						});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}
		});
	}
}
