package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.model.ModelContainer;
import com.gpl.rpg.AndorsTrail.twinsprite.ToyxManager;
import com.twinsprite.TwinspriteException;
import com.twinsprite.callback.CreateSessionCallback;
import com.twinsprite.callback.GetCallback;
import com.twinsprite.callback.SaveCallback;
import com.twinsprite.entity.Toyx;

public final class TwinspriteActivity extends Activity {

	public static final int INTENTREQUEST_SCAN = 2;
	public static final int INTENTREQUEST_SAVE = 4;

	public static final String TWINSPRITE_SCAN_BASE_URI = "https://scan.twinsprite.com/";

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
			this.showDownloadDialog();
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
						Intent i = new Intent();
						i.putExtra("action", INTENTREQUEST_SAVE);
						setResult(Activity.RESULT_OK, i);
						TwinspriteActivity.this.finish();
					} else {
						TwinspriteActivity.this.showDialog(getResources().getString(R.string.twinsprite_save_failed),
								e.getDetailMessage());
					}
				}
			});

		} else if (!model.player.toyxid.equals("")) {
			// show progress dialog
			progress.setTitle("Loading");
			progress.setMessage("Saving toyx...");
			progress.show();

			app.setToyx(new Toyx(model.player.toyxid));

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

									ToyxManager.savePlayer(app.getToyx(), model.player);

									app.getToyx().saveInBackground(new SaveCallback() {

										@Override
										public void onSave(TwinspriteException e) {
											progress.dismiss();
											if (e == null) {
												Log.d("Twinsprite", "Toyx " + app.getToyx().getToyxId()
														+ " saved successfully");
												Intent i = new Intent();
												i.putExtra("action", INTENTREQUEST_SAVE);
												setResult(Activity.RESULT_OK, i);
												TwinspriteActivity.this.finish();
											} else {
												TwinspriteActivity.this.showDialog(
														getResources().getString(R.string.twinsprite_save_failed),
														e.getDetailMessage());
											}
										}
									});

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

			if (toyxid.startsWith(TWINSPRITE_SCAN_BASE_URI)) {
				toyxid = toyxid.replace(TWINSPRITE_SCAN_BASE_URI, "");
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
									ToyxManager.loadPlayer(app.getToyx(), model.player);
									Intent i = new Intent();
									i.putExtra("action", INTENTREQUEST_SCAN);
									setResult(Activity.RESULT_OK, i);
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
	
	private AlertDialog showDownloadDialog() {
	    AlertDialog.Builder downloadDialog = new AlertDialog.Builder(this);
	    downloadDialog.setTitle("Install Barcode Scanner?");
	    downloadDialog.setMessage("This application requires Barcode Scanner. Would you like to install it?");
	    downloadDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	      @Override
	      public void onClick(DialogInterface dialogInterface, int i) {
	        String packageName =  "com.google.zxing.client.android";
	        Uri uri = Uri.parse("market://details?id=" + packageName);
	        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	        try {
	            TwinspriteActivity.this.startActivity(intent);
	        } catch (ActivityNotFoundException anfe) {
	          // Hmm, market is not installed
	          Log.w("Twinsprite", "Google Play is not installed; cannot install " + packageName);
	        }
	      }
	    });
	    downloadDialog.setNegativeButton("No", null);
	    downloadDialog.setCancelable(true);
	    return downloadDialog.show();
	  }

}
