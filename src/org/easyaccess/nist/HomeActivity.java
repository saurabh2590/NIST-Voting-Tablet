package org.easyaccess.nist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends Activity implements OnInitListener{

	//	flags
	int gFocusPosition = 1;
	int gMaxFocusableItem = 12;
	static boolean isButtonPressed = false;
	boolean isResultScan = false;
	
	Context gContext = null;
	AudioManager audioManager = null;
	HeadsetListener gHeadsetListener = null;
	
	//	views
	TextToSpeech gTTS = null;
	TextView gHelpText = null	, gBallotPage = null;
	ImageButton gHelp = null;
	ImageButton gNavigateRight = null;//gNavigateLeft = null,
	ImageButton gGoToSummary = null;// gGoToStart = null;
	ImageButton gFontDecrease = null, gFontIncrease = null;
	ImageButton gVolumeIncrease = null, gVolumeDecrease = null;
	Button gQrSetting = null, gNfcSetting = null, gPaperSetting = null, gManualSetting = null;
	View gBtmView = null,gTopView = null;
	ProgressDialog gDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.frnt_scrn);

		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.setPackage("org.easyaccess.qrapp");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, Constants.REQUESTCODE_CAPTUREACTIVITY);

		gContext = HomeActivity.this;
		gTopView = findViewById(R.id.v_scrn_top);
		gBtmView = findViewById(R.id.v_scrn_btm);
		gBallotPage = (TextView) findViewById(R.id.ballot_page);
		gHelpText = (TextView) findViewById(R.id.textView1);
		gHelp = (ImageButton) findViewById(R.id.btn_help);
		gNavigateRight = (ImageButton) findViewById(R.id.btn_right);
//		gNavigateLeft = (ImageButton) findViewById(R.id.btn_left);
		gFontDecrease = (ImageButton) findViewById(R.id.btn_font_decrease);
		gFontIncrease = (ImageButton) findViewById(R.id.btn_font_increase);
		gGoToSummary = (ImageButton) findViewById(R.id.btn_goto_end);
//		gGoToStart = (ImageButton) findViewById(R.id.btn_goto_start);
		gVolumeIncrease = (ImageButton) findViewById(R.id.btn_volume_increase);
		gVolumeDecrease = (ImageButton) findViewById(R.id.btn_volume_decrease);
		
		gQrSetting = (Button) findViewById(R.id.btn_qrcode_setting);
		gNfcSetting = (Button) findViewById(R.id.btn_nfctag_setting);
		gPaperSetting = (Button) findViewById(R.id.btn_paper_setting);
		gManualSetting = (Button) findViewById(R.id.btn_manual_setting);
		
		gGoToSummary.setVisibility(View.GONE);
	}
	
	private OnClickListener sOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_left:
				resetActivityVariable();
				navigateLeft();
				break;
			case R.id.btn_right:
				resetActivityVariable();
				navigateRight();
				isButtonPressed = false;
				break;
			case R.id.btn_qrcode_setting:
				resetActivityVariable();
				readQRSettings();
				break;
			case R.id.btn_nfctag_setting:
				resetActivityVariable();
				readNFCSettings();
				isButtonPressed = false;
				break;				
			case R.id.btn_manual_setting:
				resetActivityVariable();
				setManualSettings();
				break;
			case R.id.btn_paper_setting:
				resetActivityVariable();
				readPaperSettings();
				isButtonPressed = false;
				break;				
			case R.id.btn_goto_end:
				 loadSummary();
				break;
			case R.id.btn_font_decrease:
				 setFontSize(Constants.DECREASE);
				break;
			case R.id.btn_font_increase:
				 setFontSize(Constants.INCREASE);
				break;
			case R.id.btn_help:
				resetActivityVariable();
				 launchHelp();
				 isButtonPressed = false;
				break;
			case R.id.btn_volume_decrease:
				 setVolume(Constants.DECREASE);
				break;
			case R.id.btn_volume_increase:
				 setVolume(Constants.INCREASE);
				break;
			}
		}
	};
		
	private OnTouchListener gOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				switch (v.getId()) {
				case R.id.textView1:
					v.setBackgroundResource(R.drawable.focused);
					if(HeadsetListener.isHeadsetConnected){
						speakWord(gHelpText.getText().toString(), null);
					}
					break;
				case R.id.btn_qrcode_setting:
					if(HeadsetListener.isHeadsetConnected){
					}
					break;
				case R.id.btn_nfctag_setting:
					if(HeadsetListener.isHeadsetConnected){
					}
					break;
				case R.id.btn_manual_setting:
					if(HeadsetListener.isHeadsetConnected){
					}
					break;
				case R.id.btn_paper_setting:
					if(HeadsetListener.isHeadsetConnected){
						speakWord(getString(R.string.btn_vol_dec), null);
					}
					break;
				case R.id.btn_volume_decrease:
					v.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
					if(HeadsetListener.isHeadsetConnected){
						speakWord(getString(R.string.btn_vol_dec), null);
					}
					break;
				case R.id.btn_volume_increase:
					v.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
					if(HeadsetListener.isHeadsetConnected){
						speakWord(getString(R.string.btn_vol_inc), null);
					}
					break;
				case R.id.btn_font_decrease:
					v.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
					if(HeadsetListener.isHeadsetConnected){
						speakWord(getString(R.string.btn_font_dec), null);
					}
					break;
				case R.id.btn_font_increase:
					v.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
					if(HeadsetListener.isHeadsetConnected){
						speakWord(getString(R.string.btn_font_inc), null);
					}
					break;
				case R.id.btn_left:
					v.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
					if(HeadsetListener.isHeadsetConnected){
						speakWord(getString(R.string.btn_exit), null);
					}
					break;
				case R.id.btn_right:
					v.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
					if(HeadsetListener.isHeadsetConnected){
						speakWord(getString(R.string.next_ballot), null);
					}
					break;
				case R.id.btn_help:
					v.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
					if(HeadsetListener.isHeadsetConnected){
						speakWord(getString(R.string.btn_help), null);
					}
					break;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				switch (v.getId()) {
				case R.id.textView1:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.btn_qrcode_setting:
//					v.performClick();
					break;
				case R.id.btn_nfctag_setting:
//					v.performClick();
					break;
				case R.id.btn_manual_setting:
//					v.performClick();
					break;
				case R.id.btn_paper_setting:
//					v.performClick();
					break;
				case R.id.btn_volume_decrease:
					resetDeviceVarOnStateChange();
					v.setBackgroundColor(getResources().getColor(android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_volume_increase:
					resetDeviceVarOnStateChange();
					v.setBackgroundColor(getResources().getColor(android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_font_decrease:
					resetDeviceVarOnStateChange();
					v.setBackgroundColor(getResources().getColor(android.R.color.black));
//					v.performClick();					
					break;
				case R.id.btn_font_increase:
					resetDeviceVarOnStateChange();
					v.setBackgroundColor(getResources().getColor(android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_goto_end:
					resetActivityVariable();
					v.setBackgroundColor(getResources().getColor(android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_help:
					resetActivityVariable();
					v.setBackgroundColor(getResources().getColor(android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_left:
					resetActivityVariable();
					v.setBackgroundColor(getResources().getColor(android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_right:
					resetActivityVariable();
					v.setBackgroundColor(getResources().getColor(android.R.color.black));
//					v.performClick();
					break;
				}
			}
			return false;
		}
	};
	
	protected void navigateLeft() {
		
		if(HeadsetListener.isHeadsetConnected){
		}
	}
	
	protected void readPaperSettings() {
		
	}

	protected void setManualSettings() {
		Intent intent = new Intent(this, PreferencActivity.class);
		startActivity(intent);
	}

	protected void readNFCSettings() {
		
	}

	protected void readQRSettings() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.setPackage("org.easyaccess.qrapp");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, Constants.QR_REQUEST_CODE);
			}
		}).start();
	}

	protected void navigateRight() {
//		Toast.makeText(gContext, "from navigate right", Toast.LENGTH_SHORT).show();
		writeToFile(" right button pressed, focus position = " + gFocusPosition);
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
		startActivity(new Intent(this,ContestActivity.class));
	}

	protected void launchHelp() {
		Intent intent = new Intent(this, HelpScreen.class);
		intent.putExtra(Constants.MESSAGE, "homepage");
		startActivity(intent);
	}

	private void setVolume(int controlFlag) {
		// TODO Auto-generated method stub
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		switch (controlFlag) {
		case Constants.DECREASE :
			if(curVolume == Constants.MIN_VOLUME){
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
				if(HeadsetListener.isHeadsetConnected){

				}
			}else{
				curVolume = curVolume - Constants.V0LUME_DIFFERENCE;
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
				if(HeadsetListener.isHeadsetConnected){
					speakWord(getString(R.string.softer), null);
				}
			}
			break;
		case Constants.INCREASE :
			if(curVolume == maxVolume){
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
				if(HeadsetListener.isHeadsetConnected){
					speakWord(getString(R.string.loudest), null);
				}
			}else{
				curVolume = curVolume + Constants.V0LUME_DIFFERENCE;
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
				if(HeadsetListener.isHeadsetConnected){
					speakWord(getString(R.string.louder), null);
				}				
			}
			break;
		}
	}
	
	protected void setFontSize(int controlFlag) {
		// TODO Auto-generated method stub
		switch (controlFlag) {
		case Constants.DECREASE:
			if(Constants.SETTING_FONT_SIZE == Constants.MIN_FONT_SIZE){
				if(HeadsetListener.isHeadsetConnected){
					speakWord(getString(R.string.font_smallest), null);
				}
			}else{
				Constants.SETTING_FONT_SIZE = Constants.SETTING_FONT_SIZE - Constants.FONT_DIFFERENCE;
				gHelpText.setTextSize(Constants.SETTING_FONT_SIZE); 
				if(HeadsetListener.isHeadsetConnected){
					speakWord(getString(R.string.font_smaller), null);
				}
			}
			break;
		case Constants.INCREASE :
			if(Constants.SETTING_FONT_SIZE == Constants.MAX_FONT_SIZE){
				if(HeadsetListener.isHeadsetConnected){
					speakWord(getString(R.string.font_largest), null);
				}
			}else{
				Constants.SETTING_FONT_SIZE = Constants.SETTING_FONT_SIZE + Constants.FONT_DIFFERENCE;
				gHelpText.setTextSize(Constants.SETTING_FONT_SIZE); 
				if(HeadsetListener.isHeadsetConnected){
					speakWord(getString(R.string.font_larger), null);
				}
			}
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);

		if(keyCode == KeyEvent.KEYCODE_UNKNOWN){
			switch (event.getScanCode()) {
			case KeyEvent.KEYCODE_APP_SWITCH:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				
				gHelp.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
				break;
			case KeyEvent.KEYCODE_BUTTON_1:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				break;
			case KeyEvent.KEYCODE_BUTTON_2:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				break;
			case KeyEvent.KEYCODE_BUTTON_3:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				selectCurrentFocusItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_BUTTON_4:
				break;
			case KeyEvent.KEYCODE_BUTTON_5:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				gNavigateRight.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
				break;
			case KeyEvent.KEYCODE_BUTTON_6:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				gVolumeDecrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
				break;
			case KeyEvent.KEYCODE_BUTTON_7:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				gVolumeIncrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
				break;
			}
		}else{
			switch (keyCode) {
			case KeyEvent.KEYCODE_F1:
				gHelp.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				break;
			case KeyEvent.KEYCODE_ENTER:
//				if(!HomePage.isButtonPressed){
//					HomePage.isButtonPressed = true;
//				}
				selectCurrentFocusItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_PAGE_DOWN:
				gNavigateRight.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
				break;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		if(HomePage.isButtonPressed){
//			HomePage.isButtonPressed = false;
//		}
		
		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			switch (event.getScanCode()) {
			case KeyEvent.KEYCODE_APP_SWITCH:
				gHelp.setBackground(null);
				gHelp.performClick();
				break;
			case KeyEvent.KEYCODE_BUTTON_1:				
				gFocusPosition--;
				if (gFocusPosition <= 0) {
					gFocusPosition = gMaxFocusableItem;
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_BUTTON_2:
				gFocusPosition++;
				if (gFocusPosition >= gMaxFocusableItem + 1) {
					gFocusPosition = 1;
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_BUTTON_3:
				selectCurrentFocusItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
				break;
			case KeyEvent.KEYCODE_BUTTON_5:
				gNavigateRight.setBackground(null);
				gNavigateRight.performClick();
				break;
			case KeyEvent.KEYCODE_BUTTON_6:
				gFocusPosition = 3;
				gVolumeDecrease.performClick();
				gVolumeDecrease.setBackground(getResources().getDrawable(R.drawable.focused));
				break;
			case KeyEvent.KEYCODE_BUTTON_7:
				gFocusPosition = 4;
				gVolumeIncrease.performClick();
				gVolumeIncrease.setBackground(getResources().getDrawable(R.drawable.focused));
				break;
			}
		} else {
			switch (keyCode) {
			case KeyEvent.KEYCODE_F1:
				gHelp.setBackground(null);
				gHelp.performClick();
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				gFocusPosition--;
				if (gFocusPosition <= 0) {
					gFocusPosition = gMaxFocusableItem;
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				gFocusPosition++;
				if (gFocusPosition >= gMaxFocusableItem + 1) {
					gFocusPosition = 1;
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_ENTER:
				selectCurrentFocusItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
				break;
			case KeyEvent.KEYCODE_PAGE_DOWN:
				gNavigateRight.setBackground(null);
				gNavigateRight.performClick();
				break;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void navigateToOtherItem(int focusPosition, int reach_jump) {
		switch (reach_jump) {
		case Constants.JUMP_FROM_CURRENT_ITEM:
				if(focusPosition == 1){
					gHelpText.setBackground(null);
				}else if(focusPosition == 2){
					gHelp.setBackground(null);
				}else if(focusPosition == 3){
					gVolumeDecrease.setBackground(null);
				}else if(focusPosition == 4){
					gVolumeIncrease.setBackground(null);
				}else if(focusPosition == 5){
					gFontDecrease.setBackground(null);
				}else if(focusPosition == 6){
					gFontIncrease.setBackground(null);
				}else if(focusPosition == 7){
					gBtmView.setBackgroundColor(getResources().getColor(android.R.color.black));
				}else if(focusPosition == 8){
					gTopView.setBackgroundColor(getResources().getColor(android.R.color.black));
				}else if(focusPosition == 9){
					gNavigateRight.setBackground(null);
				}
			break;
		case Constants.REACH_NEW_ITEM:
				if (focusPosition == 1) {
					gHelpText.setBackground(getResources().getDrawable(R.drawable.focused));
					gHelpText.requestFocus();
					
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gHelpText.getText().toString(), null);
					}
				} else if (focusPosition == 2) {
					gHelp.setBackground(getResources().getDrawable(R.drawable.focused));
					gHelp.requestFocus();
					
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_help), null);
					}
				} else if (focusPosition == 3) {
					gVolumeDecrease.setBackground(getResources().getDrawable(R.drawable.focused));
					gVolumeDecrease.requestFocus();
					
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_vol_dec), null);
					}
				} else if (focusPosition == 4) {
					gVolumeIncrease.setBackground(getResources().getDrawable(R.drawable.focused));
					gVolumeIncrease.requestFocus();

					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_vol_inc), null);
					}
				} else if (focusPosition == 5) {
					gFontDecrease.setBackground(getResources().getDrawable(R.drawable.focused));
					gFontDecrease.requestFocus();

					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_font_dec), null);
					}
				} else if (focusPosition == 6) {
					gFontIncrease.setBackground(getResources().getDrawable(R.drawable.focused));
					gFontIncrease.requestFocus();

					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_font_inc), null);
					}
				} else if (focusPosition == 7) {
					gBtmView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
					gBtmView.requestFocus();

					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.scrn_bottom), null);
					}
				} else if(focusPosition == 8){
					gTopView.setBackgroundColor(getResources().getColor(	android.R.color.holo_orange_dark));
					gTopView.requestFocus();

					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.scrn_top), null);
					}
				} else if(focusPosition == 9){
					gNavigateRight.setBackground(getResources().getDrawable(R.drawable.focused));
					gNavigateRight.requestFocus();
					
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.next_ballot), null);
					}
				}
			break;
		}
 	}

	private void selectCurrentFocusItem(int focusPosition, int pressed_released) {
		switch (pressed_released) {
		case Constants.REACH_NEW_ITEM:
			if (focusPosition == 1) {
				gHelpText.setBackground(getResources().getDrawable(R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected && !HomeActivity.isButtonPressed) {
					speakWord(gHelpText.getText().toString(), null);
				}
			} else if (focusPosition == 2) {
				gHelp.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
			} else if (focusPosition == 3) {
				gVolumeDecrease.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
			} else if (focusPosition == 4) {
				gVolumeIncrease.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
			} else if (focusPosition == 5) {
				gFontDecrease.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
			} else if (focusPosition == 6) {
				gFontIncrease.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
			} else if (focusPosition == 7) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
			} else if (focusPosition == 8) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
			} else if (focusPosition == 9) {
				gNavigateRight.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
			}

			HomeActivity.isButtonPressed = true;
			break;	
		case Constants.JUMP_FROM_CURRENT_ITEM:
				HomeActivity.isButtonPressed = false;
				
				if(focusPosition == 1){
				}else if(focusPosition == 2){
					gHelp.setBackground(null);
					gHelp.performClick();
				}else if (focusPosition == 3 ) {
					gVolumeDecrease.setBackground(getResources().getDrawable(R.drawable.focused));
					gVolumeDecrease.performClick();
				} else if (focusPosition == 4 ) {
					gVolumeIncrease.setBackground(getResources().getDrawable(R.drawable.focused));
					gVolumeIncrease.performClick();
				} else if (focusPosition == 5 ) {
					gFontDecrease.setBackground(getResources().getDrawable(R.drawable.focused));
					gFontDecrease.performClick();
				} else if (focusPosition == 6 ) {
					gFontIncrease.setBackground(getResources().getDrawable(R.drawable.focused));
					gFontIncrease.performClick();
				} else if(focusPosition == 9 ){
					gNavigateRight.setBackground(null);
					gNavigateRight.performClick();
				}
			break;
		}
	}
	
	protected void loadSummary() {
		// TODO Auto-generated method stub
		if(HeadsetListener.isHeadsetConnected){
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {
			if(Constants.SETTING_LANGUAGE == Constants.DEFAULT_LANG_SETTING){
				gTTS.setLanguage(Locale.US);
			}else{
				gTTS.setLanguage(new Locale("spa","ESP"));
			}

			gTTS.setSpeechRate(Constants.SETTING_TTS_SPEED);
			speakWord(gHelpText.getText().toString(), null);
		} else if (status == TextToSpeech.ERROR) {
			Toast.makeText(gContext, getString(R.string.failed),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.TTS_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				if(Constants.SETTING_TTS_VOICE == Constants.DEFAULT_TTS_VOICE){
					gTTS = new TextToSpeech(gContext, this, "com.svox.classic");
				}else{
					gTTS = new TextToSpeech(gContext, this, "com.ivona.tts");
				}
			} else {
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		} else if(requestCode == Constants.REQUESTCODE_CAPTUREACTIVITY){
			if (resultCode == Activity.RESULT_OK) {
				String capturedQrValue = data.getStringExtra("SCAN_RESULT");
				gDialog = ProgressDialog.show(gContext, "", "Applying settings...");
				
				gHelpText.setText(capturedQrValue);
				try {
					setUserSettings(new JSONObject(capturedQrValue));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(gContext, "Scan canceled", Toast.LENGTH_SHORT).show();
			}
			
			resetPreferences();
			Intent intent = new Intent(this,ContestActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void resetPreferences() {
		SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	private void setUserSettings(JSONObject jsonObject) {
		try {
			if (jsonObject.has(Constants.LANGUAGE)) {
				Constants.SETTING_LANGUAGE = Integer.valueOf(jsonObject.getString(Constants.LANGUAGE));
				
				Resources standardResources = this.getResources();
				AssetManager assets = standardResources.getAssets();
				DisplayMetrics metrics = standardResources.getDisplayMetrics();
				Configuration config = new Configuration(standardResources.getConfiguration());
//				config.locale = Locale.US;
				config.locale = new Locale("spa", "ESP");
				Resources defaultResources = new Resources(assets, metrics, config);
			}
			if (jsonObject.has(Constants.TOUCH_PRESENT)) {
				Constants.SETTING_TOUCH_PRESENT = Boolean.valueOf(jsonObject.getString(Constants.TOUCH_PRESENT));
			}
			if (jsonObject.has(Constants.FONT_SIZE)) {
				Constants.SETTING_FONT_SIZE = Integer.valueOf(jsonObject.getString(Constants.FONT_SIZE));
			}
			if (jsonObject.has(Constants.REVERSE_SCREEN)) {
				Constants.SETTING_REVERSE_SCREEN = Boolean.valueOf(jsonObject.getString(Constants.REVERSE_SCREEN));
			}
			if (jsonObject.has(Constants.TTS_VOICE)) {
				Constants.SETTING_TTS_VOICE = Integer.valueOf(jsonObject.getString(Constants.TTS_VOICE));
			}
			if (jsonObject.has(Constants.TTS_SPEED)) {
				Constants.SETTING_TTS_SPEED = Float.valueOf(jsonObject.getString(Constants.TTS_SPEED));
				gTTS.setSpeechRate(Constants.SETTING_TTS_SPEED);
			}
			if (jsonObject.has(Constants.SCAN_MODE)) {
				Constants.SETTING_SCAN_MODE = Boolean.valueOf(jsonObject.getString(Constants.SCAN_MODE));
				if(Constants.SETTING_SCAN_MODE){
					if (jsonObject.has(Constants.SCAN_MODE_SPEED)) {
						Constants.SETTING_SCAN_MODE_SPEED = Integer.valueOf(jsonObject.getString(Constants.SCAN_MODE_SPEED));
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override 
	public void onResume() {
	    IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
	    registerReceiver(gHeadsetListener, filter);
	    
	    if(gDialog != null && gDialog.isShowing()){
	    	gDialog.cancel();
	    }
	    super.onResume();
	}
	
	@Override 
	public void onPause() {
	    unregisterReceiver(gHeadsetListener);
	    super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (gTTS != null) {
			gTTS.stop();
			gTTS.shutdown();
        }
	}
	
	public void speakWord(String word, HashMap<String, String> utteranceId) {
//		if (gTTS != null && utteranceId != null) {
//			gTTS.speak(word, TextToSpeech.QUEUE_FLUSH, utteranceId);
//		}else{
//			gTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
//		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, Constants.TTS_DATA_CHECK_CODE);			

		gHeadsetListener = new HeadsetListener();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);		
		
		gQrSetting.setOnClickListener(sOnClickListener);
		gQrSetting.setOnTouchListener(gOnTouchListener);
		gNfcSetting.setOnClickListener(sOnClickListener);
		gNfcSetting.setOnTouchListener(gOnTouchListener);
		gPaperSetting.setOnClickListener(sOnClickListener);
		gPaperSetting.setOnTouchListener(gOnTouchListener);
		gManualSetting.setOnClickListener(sOnClickListener);
		gManualSetting.setOnTouchListener(gOnTouchListener);

		gHelp.setOnClickListener(sOnClickListener);
		gHelp.setOnTouchListener(gOnTouchListener);
		gNavigateRight.setOnClickListener(sOnClickListener);
		gNavigateRight.setOnTouchListener(gOnTouchListener);
//		gNavigateLeft.setOnClickListener(sOnClickListener);
//		gNavigateLeft.setOnTouchListener(gOnTouchListener);
		gFontDecrease.setOnClickListener(sOnClickListener);
		gFontDecrease.setOnTouchListener(gOnTouchListener);
		gFontIncrease.setOnClickListener(sOnClickListener);
		gFontIncrease.setOnTouchListener(gOnTouchListener);
//		gGoToStart.setOnClickListener(sOnClickListener);
//		gGoToStart.setOnTouchListener(gOnTouchListener);
		gGoToSummary.setOnClickListener(sOnClickListener);
		gGoToSummary.setOnTouchListener(gOnTouchListener);
		gVolumeIncrease.setOnClickListener(sOnClickListener);
		gVolumeIncrease.setOnTouchListener(gOnTouchListener);
		gVolumeDecrease.setOnClickListener(sOnClickListener);
		gVolumeDecrease.setOnTouchListener(gOnTouchListener);
	//	gHelpText.setText(getString(R.string.help_message));	
		gHelpText.setTextSize(Constants.SETTING_FONT_SIZE);
		gHelpText.setOnTouchListener(gOnTouchListener);
		gHelpText.setBackgroundResource(R.drawable.focused);
		
		gHelpText.requestFocus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case R.id.action_settings:
			startActivity(new Intent(HomeActivity.this, PreferencActivity.class));
			break;
		}
		return true;
	}
	
	private void writeToFile(String string) {
		// TODO Auto-generated method stub
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(new File(Environment.getExternalStoragePublicDirectory
					(Environment.DIRECTORY_DOWNLOADS) + File.separator + "preference_file.txt"), true));
			bufferedWriter.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
	
	protected void resetActivityVariable(){
		gFocusPosition = 1;
	}
	
	protected void resetDeviceVarOnStateChange() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		gFocusPosition = 0;
	}
}