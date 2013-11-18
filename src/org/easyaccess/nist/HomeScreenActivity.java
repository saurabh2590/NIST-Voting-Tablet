package org.easyaccess.nist;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class HomeScreenActivity extends Activity implements OnInitListener {
	private static final String TAG = "HomeScreenActivity";
	
	boolean isButtonPressed = false;
	int gFocusPosition = 1;
	int gMaxFocusableItem = 2;

	Context gContext = null;
	String gTTsOnStart = null;
	SpeakManager gTTS = null;
	Button gBtnScan = null, gBtnSkipScan = null;
	View rootView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_ballotstart);

		gContext = this;
		rootView = findViewById(R.id.alrt_root_view);
		gBtnScan = (Button) findViewById(R.id.btn_scan);
		gBtnSkipScan = (Button) findViewById(R.id.btn_skip_scan);
		
		gBtnScan.setBackgroundColor(getResources().getColor(R.color.bg_button));
		gBtnScan.setTextColor(getResources().getColor(android.R.color.white));
		gBtnSkipScan.setBackgroundColor(getResources().getColor(R.color.bg_button));
		gBtnSkipScan.setTextColor(getResources().getColor(android.R.color.white));

		gBtnScan.isInTouchMode();
		
		File directory = new File(Constants.NIST_VOTING_PROTOTYPE_DIRECTORY);

		if (!directory.exists()) {
			directory.mkdir();
		}

		File sampleFile = new File(directory,
				Constants.NIST_VOTING_PROTOTYPE_FILE_SP);
		if (!sampleFile.exists()) {
			StringBuilder builder = Utils.readFile(gContext, this.getResources().openRawResource(R.raw.election_info_sp));
			Utils.writeToFile(builder.toString(), directory
					+ File.separator + Constants.NIST_VOTING_PROTOTYPE_FILE_SP, false);
		}
		
		sampleFile = new File(directory,
				Constants.NIST_VOTING_PROTOTYPE_FILE_EN);
		if (!sampleFile.exists()) {
			StringBuilder builder = Utils.readFile(gContext, this.getResources().openRawResource(R.raw.election_info_en));
			Utils.writeToFile(builder.toString(), directory
					+ File.separator + Constants.NIST_VOTING_PROTOTYPE_FILE_EN, false);
		}
	}

	private OnClickListener sOnClickListener = new OnClickListener() {
		Intent intent = null;

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_scan:
				launchScan();
				break;
			case R.id.btn_skip_scan:
				skipScan();
				break;
			case R.id.alrt_root_view:
				speakWord("", null, false);
				break;
			}
		}

		private void skipScan() {
			resetPreferences();
			intent = new Intent(gContext, ContestActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}

		private void launchScan() {
			resetPreferences();
			intent = new Intent(gContext, ScannerActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}
	};
	
	private void resetPreferences() {
		Constants.SETTING_LANGUAGE = Constants.DEFAULT_LANG_SETTING;
		Constants.SETTING_TOUCH_PRESENT = Constants.DEFAULT_TOUCH_PRESENT_SETTING;
		Constants.SETTING_FONT_SIZE = Constants.FONT_SIZE_STD;
		Constants.SETTING_REVERSE_SCREEN = Constants.DEFAULT_REVERSE_SCREEN_SETTING;
		Constants.SETTING_TTS_VOICE = Constants.DEFAULT_TTS_VOICE;
		Constants.SETTING_TTS_SPEED = Constants.TTS_SPEED_STD;
		Constants.SETTING_SCAN_MODE = Constants.DEFAULT_SCAN_MODE_SETTING; 
		Constants.SETTING_SCAN_MODE_SPEED = Constants.DEFAULT_SCAN_SPEED_SETTING;
		
		SharedPreferences preferences = getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
	}

	private OnTouchListener gOnTouchListener = new OnTouchListener() {
		/**
		 * on touch down announce the info if it represent text. on touch up
		 * perform the action
		 */
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				gFocusPosition = 0;

				switch (v.getId()) {
				case R.id.btn_scan:
					v.setBackground(getResources().getDrawable(
							R.drawable.focused));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gBtnScan.getText().toString(), null, true);
					}
					break;
				case R.id.btn_skip_scan:
					v.setBackground(getResources().getDrawable(
							R.drawable.focused));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gBtnSkipScan.getText().toString(), null, true);
					}
					break;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				switch (v.getId()) {
				case R.id.btn_scan:
					v.setBackground(null);
//					v.performClick();
					break;
				case R.id.btn_skip_scan:
					v.setBackground(null);
//					v.performClick();
					break;
				}
			}
			return false;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!isButtonPressed) {
			int keyPressed = -1;
			if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
				keyPressed = event.getScanCode();
			} else {
				keyPressed = keyCode;
			}

			switch (keyPressed) {
			case KeyEvent.KEYCODE_TAB:
				if(event.isShiftPressed()){
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);

					gFocusPosition--;
					if (gFocusPosition <= 0) {
						gFocusPosition = gMaxFocusableItem;
					}

					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}else{
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);

					gFocusPosition++;
					if (gFocusPosition > gMaxFocusableItem) {
						gFocusPosition = 1;
					}

					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_BUTTON_1:
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				gFocusPosition--;
				if (gFocusPosition <= 0) {
					gFocusPosition = gMaxFocusableItem;
				}

				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_BUTTON_2:
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				gFocusPosition++;
				if (gFocusPosition > gMaxFocusableItem) {
					gFocusPosition = 1;
				}

				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_BUTTON_3:
				selectCurrentFocusItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				break;
			}
		}
		isButtonPressed = true;
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		isButtonPressed = false;
		int keyPressed = -1;
		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			keyPressed = event.getScanCode();
		} else {
			keyPressed = keyCode;
		}

		switch (keyPressed) {
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_BUTTON_3:
			// selectCurrentFocusItem(gFocusPosition,
			// Constants.JUMP_FROM_CURRENT_ITEM);
			break;
		}
		return true;
	}

	public void selectCurrentFocusItem(int focusPosition, int pressed_released) {
		switch (pressed_released) {
		case Constants.REACH_NEW_ITEM:
			break;
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 1) {
				gBtnScan.performClick();
			} else if (gFocusPosition == 2) {
				gBtnSkipScan.performClick();
			}
			break;
		}
	}

	public void navigateToOtherItem(int focusPosition, int reach_jump) {
		View view = getCurrentFocus();
		Log.d(TAG, " from navigation method focus position = " + focusPosition 
				+ ", current focus = " + view );
//				+ ", next focus down id = " 
//				+ view.getNextFocusDownId()
//				+ ", next focus forward id = " + view.getNextFocusForwardId()
//				+ ", next focus left id = " + view.getNextFocusLeftId()
//				+ ", next focus right id = " + view.getNextFocusRightId()
//				+ ", next focus up id = " + view.getNextFocusUpId());
		
		switch (reach_jump) {
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (focusPosition == 1) {
//				gBtnScan.setBackground(null);
			} else if (focusPosition == 2) {
//				gBtnSkipScan.setBackground(null);
			}
			break;
		case Constants.REACH_NEW_ITEM:
			if (focusPosition == 1) {
				gBtnScan.setFocusableInTouchMode(true);// for handling the ez-keypad focus
				gBtnSkipScan.setFocusableInTouchMode(false);// for handling the ez-keypad focus
				gBtnScan.requestFocus();// for handling the focus when come out of touch mode
//				gBtnScan.setBackground(getResources().getDrawable(
//						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBtnScan.getText().toString() + Constants.COMMA_SPACE + getString(R.string.button), null, true);
				}
			} else if (focusPosition == 2) {
				gBtnScan.setFocusableInTouchMode(false);// for handling the ez-keypad focus
				gBtnSkipScan.setFocusableInTouchMode(true);// for handling the ez-keypad focus
				gBtnSkipScan.requestFocus();// for handling the focus when come out of touch mode
//				gBtnSkipScan.setBackground(getResources().getDrawable(
//						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBtnSkipScan.getText().toString() + Constants.COMMA_SPACE + getString(R.string.button), null, true);
				}
			}
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG,"resultCode = " + resultCode);
		if (requestCode == Constants.TTS_DATA_CHECK_CODE) {
			if (resultCode == SpeakManager.Engine.CHECK_VOICE_DATA_PASS) {
				Log.d(TAG,"Setting speak manager");
				if (Constants.SETTING_TTS_VOICE == Constants.DEFAULT_TTS_VOICE) {
					gTTS = new SpeakManager(gContext, this, "com.svox.classic");
				} else {
					gTTS = new SpeakManager(gContext, this, "com.ivona.tts");
				}
			} else {
				Log.d(TAG,"launching intent for installing tts");
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent
						.setAction(SpeakManager.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		}
	}

	@Override
	public void onInit(int status) {
		if (status == SpeakManager.SUCCESS) {
			if (Constants.SETTING_LANGUAGE == Constants.DEFAULT_LANG_SETTING) {
				gTTS.setLanguage(Locale.US);
			} else {
				gTTS.setLanguage(new Locale("spa", "ESP"));
			}

			gTTS.setSpeechRate(Constants.SETTING_TTS_SPEED);
			speakWord(gTTsOnStart, null, true);
		} else if (status == SpeakManager.ERROR) {
			Toast.makeText(gContext, getString(R.string.failed),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void speakWord(String word, HashMap<String, String> utteranceId,
			boolean shouldRepeat) {
		if (gTTS != null) {
			gTTS.speak(word, SpeakManager.QUEUE_FLUSH, utteranceId,
					shouldRepeat);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (gTTS != null) {
			gTTS.stop();
			gTTS.shutdown();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(SpeakManager.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, Constants.TTS_DATA_CHECK_CODE);
		
		gBtnScan.setText(R.string.scan_barcode);
		gBtnSkipScan.setText(R.string.skip_barcode);

		gBtnScan.setTextSize(Constants.SETTING_FONT_SIZE);
		gBtnSkipScan.setTextSize(Constants.SETTING_FONT_SIZE);
		gBtnScan.setOnClickListener(sOnClickListener);
//		gBtnScan.setOnTouchListener(gOnTouchListener);
		gBtnSkipScan.setOnClickListener(sOnClickListener);
//		gBtnSkipScan.setOnTouchListener(gOnTouchListener);
		rootView.setOnClickListener(sOnClickListener);
		
		gBtnSkipScan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
//					gBtnSkipScan.setFocusableInTouchMode(true);
				}else{
//					gBtnSkipScan.setFocusableInTouchMode(false);					
				}
			}
		});
		
		gBtnScan.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
//					gBtnScan.setFocusableInTouchMode(true);
				}else{
//					gBtnScan.setFocusableInTouchMode(false);					
				}
			}
		});
		
		gTTsOnStart = getString(R.string.scan_barcode) + Constants.COMMA_SPACE + getString(R.string.button);

//		View view = getCurrentFocus();
//		Log.d(TAG, "current focus = " + view 
//				+ ", next focus down id = " 
//				+ view.getNextFocusDownId()
//				+ ", next focus forward id = " + view.getNextFocusForwardId()
//				+ ", next focus left id = " + view.getNextFocusLeftId()
//				+ ", next focus right id = " + view.getNextFocusRightId()
//				+ ", next focus up id = " + view.getNextFocusUpId());
		
//		gBtnScan.setBackgroundResource(R.drawable.focused);
		gBtnScan.setFocusableInTouchMode(true);// for showing focus initially
		gBtnScan.requestFocus();
		
//		view = getCurrentFocus();
//		Log.d(TAG, "current focus = " + view 
//				+ ", next focus down id = " 
//				+ view.getNextFocusDownId()
//				+ ", next focus forward id = " + view.getNextFocusForwardId()
//				+ ", next focus left id = " + view.getNextFocusLeftId()
//				+ ", next focus right id = " + view.getNextFocusRightId()
//				+ ", next focus up id = " + view.getNextFocusUpId());
	}
}