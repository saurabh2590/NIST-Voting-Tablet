package org.easyaccess.nist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AlertActivity extends Activity implements OnInitListener {

	private static final String TAG = "AlertActivity";
	int gResultCode = -1;
	int gFocusPosition = 1;
	int gMinFocusableItem = 10;
	boolean isButtonPressed = false;
	/**
	 * flag for checking the auto advance of speech
	 */
	boolean isHeadingTTSInterupted = false;

	Context gContext = null;
	String gTTSOnStart = null;
	SpeakManager gTTS = null;
	AudioManager audioManager = null;
	HeadsetListener gHeadsetListener = null;
	UtterenceProgressHelper gTTSProgressHelper = null;

	TextView gBallotPage = null, gMessageText = null;
	View gBtmView = null, gTopView = null, gRootView = null,
			gAlertMsgContainer = null;
	ImageButton gHelp = null, gNavigateLeft = null, gNavigateRight = null,
			gGoToSummary = null, gFontDecrease = null, gFontIncrease = null,
			gVolumeIncrease = null, gVolumeDecrease = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.alrt_page_layout);

		gContext = AlertActivity.this;

		gBallotPage = (TextView) findViewById(R.id.ballot_page);
		gMessageText = (TextView) findViewById(R.id.alrt_msg_one);

		gTopView = findViewById(R.id.v_scrn_top);
		gBtmView = findViewById(R.id.v_scrn_btm);
		gRootView = findViewById(R.id.alrt_root_view);
		gAlertMsgContainer = findViewById(R.id.alrt_msg_container);
		
		gHelp = (ImageButton) findViewById(R.id.btn_help);
		gNavigateLeft = (ImageButton) findViewById(R.id.btn_left);
		gFontDecrease = (ImageButton) findViewById(R.id.btn_font_decrease);
		gFontIncrease = (ImageButton) findViewById(R.id.btn_font_increase);
		gVolumeDecrease = (ImageButton) findViewById(R.id.btn_volume_decrease);
		gVolumeIncrease = (ImageButton) findViewById(R.id.btn_volume_increase);
		gNavigateRight = (ImageButton) findViewById(R.id.btn_right);
		gGoToSummary = (ImageButton) findViewById(R.id.btn_goto_end);

		gNavigateRight.setVisibility(View.GONE);
		gGoToSummary.setVisibility(View.GONE);

		if (Constants.SETTING_REVERSE_SCREEN) {
			setNightMode();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!isButtonPressed) {
			int keyPressed = -1;
			if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
				keyPressed = event.getScanCode();
			} else {
				keyPressed = keyCode;
			}

			isHeadingTTSInterupted = true;
			switch (keyPressed) {
			case KeyEvent.KEYCODE_F1:
			case KeyEvent.KEYCODE_APP_SWITCH:
				break;
			case KeyEvent.KEYCODE_TAB:
				if(event.isShiftPressed()){
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gFocusPosition--;
					if (gFocusPosition <= 0) {
						gFocusPosition = gMinFocusableItem;
					}
					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}else{
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gFocusPosition++;
					if (gFocusPosition >= gMinFocusableItem + 1) {
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
					gFocusPosition = gMinFocusableItem;
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_BUTTON_2:
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				gFocusPosition++;
				if (gFocusPosition >= gMinFocusableItem + 1) {
					gFocusPosition = 1;
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_BUTTON_3:
				selectCurrentFocusItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_PAGE_UP:
			case KeyEvent.KEYCODE_BUTTON_4:
				// navigateToOtherItem(gFocusPosition,
				// Constants.JUMP_FROM_CURRENT_ITEM);
				// gNavigateLeft.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
				// gNavigateLeft.setBackground(null);
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_PAGE_DOWN:
			case KeyEvent.KEYCODE_BUTTON_5:
				// navigateToOtherItem(gFocusPosition,
				// Constants.JUMP_FROM_CURRENT_ITEM);
				// gNavigateRight.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
				// gNavigateRight.setBackground(null);
				break;
			case KeyEvent.KEYCODE_BUTTON_6:
				// navigateToOtherItem(gFocusPosition,
				// Constants.JUMP_FROM_CURRENT_ITEM);
				// gVolumeDecrease.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
				// gVolumeDecrease.setBackground(null);
				break;
			case KeyEvent.KEYCODE_BUTTON_7:
				// navigateToOtherItem(gFocusPosition,
				// Constants.JUMP_FROM_CURRENT_ITEM);
				// gVolumeIncrease.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
				// gVolumeIncrease.setBackground(null);
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
		case KeyEvent.KEYCODE_F1:
		case KeyEvent.KEYCODE_APP_SWITCH:
			gHelp.performClick();
			break;
		case KeyEvent.KEYCODE_TAB:
			if(event.isShiftPressed()){
				
			}else{
				
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_BUTTON_1:
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_BUTTON_2:
			break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_BUTTON_3:
			selectCurrentFocusItem(gFocusPosition,
					Constants.JUMP_FROM_CURRENT_ITEM);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_PAGE_UP:
		case KeyEvent.KEYCODE_BUTTON_4:
			gNavigateLeft.performClick();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_PAGE_DOWN:
		case KeyEvent.KEYCODE_BUTTON_5:
			if (gNavigateRight.getVisibility() == View.VISIBLE) {
				gNavigateRight.performClick();
			}
			break;
		case KeyEvent.KEYCODE_BUTTON_6:
			gVolumeDecrease.performClick();
			break;
		case KeyEvent.KEYCODE_BUTTON_7:
			gVolumeIncrease.performClick();
			break;
		}
		return true;
	}

	public void navigateToOtherItem(int gFocusPosition, int reach_jump) {
		switch (reach_jump) {
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 1) {
				gBallotPage.setBackground(null);
			} else if (gFocusPosition == 2) {
				gMessageText.setBackground(null);
			} else if (gFocusPosition == 3) {
				gHelp.setBackground(null);
			} else if (gFocusPosition == 4) {
				gVolumeDecrease.setBackground(null);
			} else if (gFocusPosition == 5) {
				gVolumeIncrease.setBackground(null);
			} else if (gFocusPosition == 6) {
				gFontDecrease.setBackground(null);
			} else if (gFocusPosition == 7) {
				gFontIncrease.setBackground(null);
			} else if (gFocusPosition == 8) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (gFocusPosition == 9) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (gFocusPosition == 10) {
				gNavigateLeft.setBackground(null);
			} else if (gFocusPosition == 11) {
				gNavigateRight.setBackground(null);
			}
			break;
		case Constants.REACH_NEW_ITEM:
			if (gFocusPosition == 1) {
				gBallotPage.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gBallotPage.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 2) {
				gMessageText.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gMessageText.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gMessageText.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 3) {
				gHelp.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gHelp.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_help), null, true);
				}
			} else if (gFocusPosition == 4) {
				gVolumeDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gVolumeDecrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_dec), null, true);
				}
			} else if (gFocusPosition == 5) {
				gVolumeIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gVolumeIncrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_inc), null, true);
				}
			} else if (gFocusPosition == 6) {
				gFontDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFontDecrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_dec), null, true);
				}
			} else if (gFocusPosition == 7) {
				gFontIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFontIncrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_inc), null, true);
				}
			} else if (gFocusPosition == 8) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				gBtmView.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom)
							+ Constants.DOT_SPACE + Constants.DOT_SPACE
							+ getString(R.string.scrn_bottom_press_again),
							null, true);
				}
			} else if (gFocusPosition == 9) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				gTopView.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			} else if (gFocusPosition == 10) {
				gNavigateLeft.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gNavigateLeft.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.previous_ballot), null, true);
				}
			} else if (gFocusPosition == 11) {
				gNavigateRight.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gNavigateRight.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.next_ballot), null, true);
				}
			}
			break;
		}
	}

	/**
	 * used by keypad/gamepad
	 * 
	 * @param gFocusPosition
	 * @param pressed_released
	 */
	public void selectCurrentFocusItem(int gFocusPosition, int pressed_released) {
		switch (pressed_released) {
		case Constants.REACH_NEW_ITEM:
			if (gFocusPosition == 1) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 2) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gMessageText.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 3) {
				// gHelp.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 4) {
				// gVolumeDecrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 5) {
				// gVolumeIncrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 6) {
				// gFontDecrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 7) {
				// gFontIncrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 8) {
			} else if (gFocusPosition == 9) {
			} else if (gFocusPosition == 10) {
				// gNavigateLeft.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 11) {
				// gNavigateRight.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			}
			break;
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 3) {
				gHelp.performClick();
			} else if (gFocusPosition == 4) {
				gVolumeDecrease.performClick();
			} else if (gFocusPosition == 5) {
				gVolumeIncrease.performClick();
			} else if (gFocusPosition == 6) {
				gFontDecrease.performClick();
			} else if (gFocusPosition == 7) {
				gFontIncrease.performClick();
			} else if (gFocusPosition == 8) {
			} else if (gFocusPosition == 9) {
			} else if (gFocusPosition == 10) {
				gNavigateLeft.performClick();
			} else if (gFocusPosition == 11) {
				gNavigateRight.performClick();
			}
			break;
		}
	}

	private OnTouchListener gOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Utils.downX = (int) event.getRawX();
				Utils.downY = (int) event.getRawY();
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				gFocusPosition = 0;
				isHeadingTTSInterupted = true;
				
				switch (v.getId()) {
				case R.id.alrt_root_view:
					speakWord("", null, false);
					break;
				case R.id.ballot_page:
					v.setBackgroundResource(R.drawable.focused);
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gBallotPage.getText().toString(), null, true);
					}
					break;
				case R.id.alrt_msg_one:
					v.setBackground(getResources().getDrawable(
							R.drawable.focused));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gMessageText.getText().toString(), null, true);
					}
					break;
				case R.id.btn_font_decrease:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_font_dec), null, true);
					}
					break;
				case R.id.btn_font_increase:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_font_inc), null, true);
					}
					break;
				case R.id.btn_goto_end:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					break;
				case R.id.btn_help:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_help), null, true);
					}
					break;
				case R.id.btn_left:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.previous_ballot), null,
								true);
					}
					break;
				case R.id.btn_right:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.next_ballot), null, true);
					}
					break;
				case R.id.btn_volume_decrease:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_vol_dec), null, true);
					}
					break;
				case R.id.btn_volume_increase:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_vol_inc), null, true);
					}
					break;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				switch (v.getId()) {
				case R.id.ballot_page:
					v.setBackground(null);
					break;
				case R.id.alrt_msg_one:
					v.setBackground(null);
//					v.performClick();
					break;
				case R.id.btn_help:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_volume_decrease:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_volume_increase:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_font_decrease:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_font_increase:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_left:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_right:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				}
			}
			return false;
		}
	};

	private OnClickListener sOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ballot_page:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(AlertActivity.this, gBallotPage
							.getText().toString(), gBallotPage
							.getTextSize());
				}
			break;
			case R.id.alrt_msg_one:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(AlertActivity.this, gMessageText
							.getText().toString(), gMessageText
							.getTextSize());
				}
				break;
			case R.id.btn_help:
				launchHelp();
				break;
			case R.id.btn_font_decrease:
				setFontSize(Constants.DECREASE);
				break;
			case R.id.btn_font_increase:
				setFontSize(Constants.INCREASE);
				break;
			case R.id.btn_volume_decrease:
				setVolume(Constants.DECREASE);
				break;
			case R.id.btn_volume_increase:
				setVolume(Constants.INCREASE);
				break;
			case R.id.btn_left:
				navigateLeft();
				break;
			case R.id.btn_right:
				navigateRight();
				break;
			}
		}
	};

	private void navigateRight() {
		Log.d(TAG, "isHeadingTTSInterupted = " + isHeadingTTSInterupted);
		resetActivity();
		Intent resultIntent = new Intent();
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		/**
		 * send the intent when navigate forward which is null in navigating
		 * backward
		 */
		setResult(gResultCode, resultIntent);
		finish();
	}

	protected void navigateLeft() {
		resetActivity();
		Intent resultIntent = null;
		switch (gResultCode) {
		case Constants.TYPE_NOTICE_PAGE:
			setResult(gResultCode);
			finish();
			break;
		case Constants.TYPE_UNDER_VOTE:
			setResult(gResultCode);
			finish();
			break;
		case Constants.TYPE_OVER_VOTE:
			String candidateName = getIntent().getStringExtra(
					Constants.CANDIDATE_NAME);
			String tts = null;

			if (candidateName != null) {
				tts = getString(R.string.not_marked) + Constants.SPACE
						+ candidateName + Constants.SPACE
						+ getString(R.string.is) + Constants.SPACE
						+ getString(R.string.not_marked);
			} else {
				tts = getString(R.string.not_marked) + Constants.SPACE
						+ candidateName + Constants.SPACE
						+ getString(R.string.is) + Constants.SPACE
						+ getString(R.string.not_marked);
			}

			resultIntent = new Intent();
			if (tts != null) {
				resultIntent.putExtra(Constants.ANNOUNCE, tts);
			}
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			setResult(gResultCode, resultIntent);
			finish();
			break;
		case Constants.TYPE_LOAD_SUMMARY:
			setResult(gResultCode);
			finish();
			break;
		}
	}

	private void setVolume(int controlFlag) {
		int maxVolume = audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		switch (controlFlag) {
		case Constants.DECREASE:
			curVolume = curVolume - Constants.V0LUME_DIFFERENCE;

			if (curVolume <= Constants.MIN_VOLUME) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						Constants.MIN_VOLUME, 0);

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.softest), null, false);
				}
			} else {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						curVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.softer), null, false);
				}
			}
			break;
		case Constants.INCREASE:
			curVolume = curVolume + Constants.V0LUME_DIFFERENCE;

			if (curVolume >= maxVolume) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						maxVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {// any number
					speakWord(getString(R.string.loudest), null, false);
				}
			} else {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						curVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.louder), null, false);
				}
			}
			break;
		}
	}

	protected void setFontSize(int controlFlag) {
		switch (controlFlag) {
		case Constants.DECREASE:
			Constants.SETTING_FONT_SIZE = Constants.SETTING_FONT_SIZE
					- Constants.FONT_DIFFERENCE;

			if (Constants.SETTING_FONT_SIZE == Constants.MIN_FONT_SIZE) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_smallest), null, false);
				}
			} else if (Constants.SETTING_FONT_SIZE < Constants.MIN_FONT_SIZE) {
				Constants.SETTING_FONT_SIZE = Constants.MIN_FONT_SIZE;
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_smallest), null, false);
				}
			} else {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_smaller), null, false);
				}
			}
			break;
		case Constants.INCREASE:
			Constants.SETTING_FONT_SIZE = Constants.SETTING_FONT_SIZE
					+ Constants.FONT_DIFFERENCE;

			if (Constants.SETTING_FONT_SIZE == Constants.MAX_FONT_SIZE) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_largest), null, false);
				}
			} else if (Constants.SETTING_FONT_SIZE > Constants.MAX_FONT_SIZE) {
				Constants.SETTING_FONT_SIZE = Constants.FONT_SIZE_XXLARGE;
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_largest), null, false);
				}
			} else {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_larger), null, false);
				}
			}
			break;
		}
		gBallotPage.setTextSize(Constants.SETTING_FONT_SIZE);
		gMessageText.setTextSize(Constants.SETTING_FONT_SIZE);
	}

	protected void launchHelp() {
		resetActivity();
		Intent intent = new Intent(this, HelpScreen.class);
		startActivity(intent);
	}

	public void speakWord(String word, HashMap<String, String> utteranceId,
			boolean shouldRepeat) {
		if (gTTS != null) {
			gTTS.speak(word, SpeakManager.QUEUE_FLUSH, utteranceId,
					shouldRepeat);
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
			if (gTTSOnStart != null) {
				Log.d("AlertActivity", "gttsOnStart = " + gTTSOnStart);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
						UtterenceProgressHelper.UID_ALERT_ACTIVITY);
				gTTSProgressHelper.isHybridChunkRead = true;
				 speakWord(gBallotPage.getText().toString(), params, true);
//				speakWord(gTTSOnStart, null, true);
			}
		} else if (status == SpeakManager.ERROR) {
			Toast.makeText(gContext, getString(R.string.tts_failed),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		gHelp.setOnClickListener(sOnClickListener);
		gHelp.setOnTouchListener(gOnTouchListener);
		gNavigateRight.setOnClickListener(sOnClickListener);
		gNavigateRight.setOnTouchListener(gOnTouchListener);
		gNavigateLeft.setOnClickListener(sOnClickListener);
		gNavigateLeft.setOnTouchListener(gOnTouchListener);
		gFontDecrease.setOnClickListener(sOnClickListener);
		gFontDecrease.setOnTouchListener(gOnTouchListener);
		gFontIncrease.setOnClickListener(sOnClickListener);
		gFontIncrease.setOnTouchListener(gOnTouchListener);
		gGoToSummary.setOnClickListener(sOnClickListener);
		gGoToSummary.setOnTouchListener(gOnTouchListener);
		gVolumeIncrease.setOnClickListener(sOnClickListener);
		gVolumeIncrease.setOnTouchListener(gOnTouchListener);
		gVolumeDecrease.setOnClickListener(sOnClickListener);
		gVolumeDecrease.setOnTouchListener(gOnTouchListener);

		gRootView.setOnTouchListener(gOnTouchListener);
		gBallotPage.setOnTouchListener(gOnTouchListener);
		gBallotPage.setOnClickListener(sOnClickListener);
		gMessageText.setOnTouchListener(gOnTouchListener);
		gMessageText.setOnClickListener(sOnClickListener);
		
		gHeadsetListener = new HeadsetListener();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		gResultCode = getIntent().getIntExtra(Constants.ALERTPAGE_TYPE, -1);
		String alertPageTitle = getIntent().getStringExtra(
				Constants.ALERTPAGE_TITLE);
		ArrayList<String> gAlertMessage = getIntent().getStringArrayListExtra(
				Constants.ALERTPAGE_CONTENT);

		String msg = "";

		for (int i = 0; i < gAlertMessage.size(); i++) {
			if (i == gAlertMessage.size() - 1) {
				msg = msg + gAlertMessage.get(i);
			} else {
				msg = msg + gAlertMessage.get(i) + Constants.NEXT_LINE
						+ Constants.NEXT_LINE;
			}
		}

		if (gResultCode == Constants.TYPE_UNDER_VOTE
				|| gResultCode == Constants.TYPE_LOAD_SUMMARY) {
			gNavigateRight.setVisibility(View.VISIBLE);
			gMinFocusableItem++;
		}

		if (gAlertMessage.size() == 1) {
			gMessageText.setGravity(Gravity.CENTER);
		}

		gTTSOnStart = alertPageTitle;
		gMessageText.setText(msg);
		gMessageText.setTextSize(Constants.SETTING_FONT_SIZE);

		gBallotPage.setText(alertPageTitle);
		gBallotPage.setTextSize(Constants.SETTING_FONT_SIZE);
		gBallotPage.setBackgroundResource(R.drawable.focused);
		gBallotPage.requestFocus();
		
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(SpeakManager.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, Constants.TTS_DATA_CHECK_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.TTS_DATA_CHECK_CODE) {
			if (resultCode == SpeakManager.Engine.CHECK_VOICE_DATA_PASS) {
				if(!isFinishing()){
					if (Constants.SETTING_TTS_VOICE == Constants.DEFAULT_TTS_VOICE) {
						gTTS = new SpeakManager(gContext, this, "com.svox.classic");
					} else {
						gTTS = new SpeakManager(gContext, this, "com.ivona.tts");
					}

					gTTS.setSpeechRate(Constants.TTS_SPEED_STD);
					gTTSProgressHelper = new UtterenceProgressHelper(
							AlertActivity.this);
					gTTS.setOnUtteranceProgressListener(gTTSProgressHelper);
				}
			} else {
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent
						.setAction(SpeakManager.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		}
	}

	protected void resetActivity() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		gFocusPosition = 1;
		speakWord("", null, false);
	}

	public void setNightMode() {
		gRootView.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gAlertMsgContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotPage
				.setTextColor(getResources().getColor(android.R.color.white));
		gBallotPage.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gMessageText.setTextColor(getResources()
				.getColor(android.R.color.white));
		gMessageText.setBackgroundColor(getResources().getColor(
				android.R.color.black));
	}

	@Override
	protected void onPause() {
		super.onPause();
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
	protected void onDestroy() {
		super.onDestroy();
//		if (gTTS != null) {
//			gTTS.stop();
//			gTTS.shutdown();
//		}
	}
}