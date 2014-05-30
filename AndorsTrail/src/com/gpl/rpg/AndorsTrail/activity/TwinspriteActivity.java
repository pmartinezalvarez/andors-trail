package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.model.ModelContainer;
import com.gpl.rpg.AndorsTrail.twinsprite.ToyxManager;
import com.twinsprite.Twinsprite;
import com.twinsprite.TwinspriteException;
import com.twinsprite.callback.CreateSessionCallback;
import com.twinsprite.callback.GetCallback;
import com.twinsprite.callback.SaveCallback;
import com.twinsprite.entity.Toyx;

public final class TwinspriteActivity extends Activity {

	public static final int INTENTREQUEST_SCAN = 2;
	
	private static final String TWINSPRITE_SCAN_BASE_URI = "https://scan.twinsprite.com/";

	private AndorsTrailApplication app;

	private ModelContainer model;
	public ProgressDialog progress = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = AndorsTrailApplication.getApplicationFromActivity(this);
		app.setWindowParameters(this);
		this.model = app.getWorld().model;

		this.progress = new ProgressDialog(this);

		setContentView(R.layout.twinsprite);
	}

	public void scan(View view) {
		try {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			this.startActivityForResult(intent, INTENTREQUEST_SCAN);
		} catch (ActivityNotFoundException e) {
			Toast toast = Toast.makeText(this, "ZXing Scanner not found.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}

	public void save(View view) {

		if (app.getToyx() != null) {

			// show progress dialog
			progress.setTitle("Loading");
			progress.setMessage("Saving toyx...");
			progress.show();

			ToyxManager.savePlayer(app.getToyx(), model.player);

			app.getToyx().saveInBackground(new SaveCallback() {

				@Override
				public void onSave(TwinspriteException e) {
					progress.dismiss();
					if (e == null) {
						Log.d("Twinsprite", "Toyx " + app.getToyx().getToyxId() + " saved successfully");
						TwinspriteActivity.this.finish();
					} else {
						TwinspriteActivity.this.showDialog(getResources().getString(R.string.twinsprite_save_failed),
								e.getDetailMessage());
					}
				}
			});

		} else {
			TwinspriteActivity.this.showDialog(getResources().getString(R.string.twinsprite_save_failed),
					getResources().getString(R.string.twinsprite_scan_before_save));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case INTENTREQUEST_SCAN:

			if (resultCode != Activity.RESULT_OK)
				break;

			String toyxid = data.getStringExtra("SCAN_RESULT");
			
			if(toyxid.startsWith(TWINSPRITE_SCAN_BASE_URI)){
				toyxid = toyxid.replace(TWINSPRITE_SCAN_BASE_URI, "");
			}

			// Initializes the Twinsprite SDK
			Twinsprite.initialize(this, getResources().getString(R.string.twinsprite_api_key), getResources()
					.getString(R.string.twinsprite_secret_key));

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
									ToyxManager.loadPlayer(app.getToyx(), model.player);
									TwinspriteActivity.this.finish();
								} else {
									TwinspriteActivity.this.showDialog(
											getResources().getString(R.string.twinsprite_fetch_failed),
											e.getDetailMessage());
								}
							}
						});
					} else {
						TwinspriteActivity.this.showDialog(
								getResources().getString(R.string.twinsprite_session_failed), e.getDetailMessage());
					}
				}
			});

			break;
		}
	}

	public void showDialog(final String title, final String message) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TwinspriteActivity.this);

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
