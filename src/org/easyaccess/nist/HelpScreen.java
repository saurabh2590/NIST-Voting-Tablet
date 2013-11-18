package org.easyaccess.nist;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
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

public class HelpScreen extends Activity implements OnInitListener {
	private static final String TAG = "HelpScreen";

	int gFocusPosition = 1;
	int gMinFocusableItem = 16;
	boolean isButtonPressed = false;
	boolean isHybridModeOn = true;
	boolean isHeadingTTSInterupted = false;
	
	Context gContext = null;
	SpeakManager gTTS = null;
	AudioManager audioManager = null;
	HeadsetListener gHeadsetListener = null;
	UtterenceProgressHelper gTTSProgressHelper = null;

	View gBtmView = null, gTopView = null, gMessageContainer = null;
	TextView gBallotPage = null, textView1 = null, textView2 = null,
			textView3 = null, textView4 = null, textView5 = null,
			textView6 = null;
	ImageButton gHelp = null, gNavigateLeft = null, gNavigateRight = null,
			gGoToSummary = null, gFontDecrease = null, gFontIncrease = null,
			gVolumeIncrease = null, gVolumeDecrease = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.hlp_scrn);

		gContext = HelpScreen.this;
		gHeadsetListener = new HeadsetListener();

		gTopView = findViewById(R.id.v_scrn_top);
		gBtmView = findViewById(R.id.v_scrn_btm);
		gMessageContainer = findViewById(R.id.ll_help_content);

		gBallotPage = (TextView) findViewById(R.id.ballot_page);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		textView6 = (TextView) findViewById(R.id.textView6);
		// textView7 = (TextView) findViewById(R.id.textView7);

		gHelp = (ImageButton) findViewById(R.id.btn_help);
		gNavigateRight = (ImageButton) findViewById(R.id.btn_right);
		gNavigateLeft = (ImageButton) findViewById(R.id.btn_left);
		gFontDecrease = (ImageButton) findViewById(R.id.btn_font_decrease);
		gFontIncrease = (ImageButton) findViewById(R.id.btn_font_increase);
		gVolumeDecrease = (ImageButton) findViewById(R.id.btn_volume_decrease);
		gVolumeIncrease = (ImageButton) findViewById(R.id.btn_volume_increase);
		gGoToSummary = (ImageButton) findViewById(R.id.btn_goto_end);
		gGoToSummary.setVisibility(View.GONE);

		if (Constants.SETTING_REVERSE_SCREEN) {
			setNightMode();
		}
	}

	private OnClickListener sOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ballot_page:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(HelpScreen.this, gBallotPage
							.getText().toString(), gBallotPage
							.getTextSize());
				}
				break;
			case R.id.textView1:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(HelpScreen.this, textView1
							.getText().toString(), textView1.getTextSize());
				}
				break;
			case R.id.textView2:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(HelpScreen.this, textView2
							.getText().toString(), textView2.getTextSize());
				}
				break;
			case R.id.textView3:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(HelpScreen.this, textView3
							.getText().toString(), textView3.getTextSize());
				}
				break;
			case R.id.textView4:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(HelpScreen.this, textView4
							.getText().toString(), textView4.getTextSize());
				}
				break;
			case R.id.textView5:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(HelpScreen.this, textView5
							.getText().toString(), textView5.getTextSize());
				}
				break;
			case R.id.textView6:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(HelpScreen.this, textView6
							.getText().toString(), textView6.getTextSize());
				}
				break;
			case R.id.btn_left:
				navigateLeft();
				break;
			case R.id.btn_right:
				navigateRight();
				break;
			case R.id.btn_font_decrease:
				setFontSize(Constants.DECREASE);
				break;
			case R.id.btn_font_increase:
				setFontSize(Constants.INCREASE);
				break;
			case R.id.btn_help:
				launchHelp();
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

	private void launchHelp() {
		resetActivity();
		finish();
	}

	private OnTouchListener gOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Utils.downX = (int) event.getRawX();
				Utils.downY = (int) event.getRawY();
				/**
				 * first stop the chunk reading based on current focus position
				 * and jump from the current item
				 */
				stopChunkReading();
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				gFocusPosition = 0;
				switch (v.getId()) {
				case R.id.ll_help_content:
					speakWord("", null, false);
					break;
				case R.id.ballot_page:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(HelpScreen.this, gBallotPage
//								.getText().toString(), gBallotPage
//								.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.textView1:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(HelpScreen.this, textView1
//								.getText().toString(), textView1.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.textView2:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(HelpScreen.this, textView2
//								.getText().toString(), textView2.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.textView3:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(HelpScreen.this, textView3
//								.getText().toString(), textView3.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.textView4:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(HelpScreen.this, textView4
//								.getText().toString(), textView4.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.textView5:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(HelpScreen.this, textView5
//								.getText().toString(), textView5.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.textView6:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(HelpScreen.this, textView6
//								.getText().toString(), textView6.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.textView7:
					v.setBackgroundResource(R.drawable.focused);
					// if(Constants.SETTING_TOUCH_PRESENT){
					// ContestActivity.showCustomDialog(HelpScreen.this,
					// textView7.getText().toString(), textView7.getTextSize());
					// }
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.textView8:
					v.setBackgroundResource(R.drawable.focused);
					// if(Constants.SETTING_TOUCH_PRESENT){
					// ContestActivity.showCustomDialog(HelpScreen.this,
					// textView8.getText().toString(), textView8.getTextSize());
					// }
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(((TextView) v).getText().toString(), null, true);
					}
					break;
				case R.id.btn_help:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					break;
				case R.id.btn_volume_decrease:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					break;
				case R.id.btn_volume_increase:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					break;
				case R.id.btn_font_decrease:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					break;
				case R.id.btn_font_increase:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					break;
				case R.id.btn_right:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					break;
				case R.id.btn_left:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					break;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				switch (v.getId()) {
				case R.id.ballot_page:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.textView1:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.textView2:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.textView3:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.textView4:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.textView5:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.textView6:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.textView7:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.textView8:
					resetDeviceVarOnStateChange();
					v.setBackground(null);
					break;
				case R.id.btn_help:
					resetActivityVariable();
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_volume_decrease:
					resetDeviceVarOnStateChange();
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_volume_increase:
					resetDeviceVarOnStateChange();
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_font_decrease:
					resetDeviceVarOnStateChange();
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_font_increase:
					resetDeviceVarOnStateChange();
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_left:
					resetActivityVariable();
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				case R.id.btn_right:
					resetActivityVariable();
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
//					v.performClick();
					break;
				}
			} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
				v.setBackgroundColor(0);
			}
			return false;
		}
	};

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

	protected void navigateRight() {
		resetActivity();
		finish();
	}

	private void resetActivity() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		gFocusPosition = 1;
		isHeadingTTSInterupted = false;
		speakWord("", null, false);
	}

	protected void navigateLeft() {
		resetActivity();
		finish();
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
		textView1.setTextSize(Constants.SETTING_FONT_SIZE);
		textView2.setTextSize(Constants.SETTING_FONT_SIZE);
		textView3.setTextSize(Constants.SETTING_FONT_SIZE);
		textView4.setTextSize(Constants.SETTING_FONT_SIZE);
		textView5.setTextSize(Constants.SETTING_FONT_SIZE);
		textView6.setTextSize(Constants.SETTING_FONT_SIZE);
	}

	public void navigateToOtherItem(int gFocusPosition, int reach_jump) {
		switch (reach_jump) {
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 1) {
				gBallotPage.setBackground(null);
			} else if (gFocusPosition == 2) {
				textView1.setBackground(null);
			} else if (gFocusPosition == 3) {
				textView2.setBackground(null);
			} else if (gFocusPosition == 4) {
				textView3.setBackground(null);
			} else if (gFocusPosition == 5) {
				textView4.setBackground(null);
			} else if (gFocusPosition == 6) {
				textView5.setBackground(null);
			} else if (gFocusPosition == 7) {
				textView6.setBackground(null);
			} else if (gFocusPosition == 8) {
				gHelp.setBackground(null);
			} else if (gFocusPosition == 9) {
				gVolumeDecrease.setBackground(null);
			} else if (gFocusPosition == 10) {
				gVolumeIncrease.setBackground(null);
			} else if (gFocusPosition == 11) {
				gFontDecrease.setBackground(null);
			} else if (gFocusPosition == 12) {
				gFontIncrease.setBackground(null);
			} else if (gFocusPosition == 13) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (gFocusPosition == 14) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (gFocusPosition == 15) {
				gNavigateLeft.setBackground(null);
			} else if (gFocusPosition == 16) {
				gNavigateRight.setBackground(null);
			}
			break;
		case Constants.REACH_NEW_ITEM:
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
					UtterenceProgressHelper.UID_HELP_SCREEEN);

			if (gFocusPosition == 1) {
				gBallotPage.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gBallotPage.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), params, true);
				}
			} else if (gFocusPosition == 2) {
				textView1.setBackground(getResources().getDrawable(
						R.drawable.focused));
				textView1.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView1.getText().toString(), params, true);
				}
			} else if (gFocusPosition == 3) {
				textView2.setBackground(getResources().getDrawable(
						R.drawable.focused));
				textView2.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView2.getText().toString(), params, true);
				}
			} else if (gFocusPosition == 4) {
				textView3.setBackground(getResources().getDrawable(
						R.drawable.focused));
				textView3.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView3.getText().toString(), params, true);
				}
			} else if (gFocusPosition == 5) {
				textView4.setBackground(getResources().getDrawable(
						R.drawable.focused));
				textView4.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView4.getText().toString(), params, true);
				}
			} else if (gFocusPosition == 6) {
				textView5.setBackground(getResources().getDrawable(
						R.drawable.focused));
				textView5.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView5.getText().toString(), params, true);
				}
			} else if (gFocusPosition == 7) {
				textView6.setBackground(getResources().getDrawable(
						R.drawable.focused));
				textView6.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView6.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 8) {
				gHelp.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gHelp.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_help), null, true);
				}
			} else if (gFocusPosition == 9) {
				gVolumeDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gVolumeDecrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_dec), null, true);
				}
			} else if (gFocusPosition == 10) {
				gVolumeIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gVolumeIncrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_inc), null, true);
				}
			} else if (gFocusPosition == 11) {
				gFontDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFontDecrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_dec), null, true);
				}

			} else if (gFocusPosition == 12) {
				gFontIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFontIncrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_inc), null, true);
				}
			} else if (gFocusPosition == 13) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				gBtmView.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom)
							+ Constants.DOT_SPACE + Constants.DOT_SPACE
							+ getString(R.string.scrn_bottom_press_again), null, true);
				}
			} else if (gFocusPosition == 14) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				gTopView.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			} else if (gFocusPosition == 15) {
				gNavigateLeft.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gNavigateLeft.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.previous_ballot), null, true);
				}
			} else if (gFocusPosition == 16) {
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean result = false;
		if (!isButtonPressed) {
			int keyPressed = -1;

			if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
				keyPressed = event.getScanCode();
			} else {
				keyPressed = keyCode;
			}

			/**
			 * if any key is pressed handle hybrid chunk read
			 */
			stopChunkReading();
			switch (keyPressed) {
			case KeyEvent.KEYCODE_F1:
			case KeyEvent.KEYCODE_APP_SWITCH:
				/**
				 * handle in key up, handling it here will launch the other
				 * activity and that will launch it back if the button is in
				 * pressed state
				 */
				result = true;
				break;
			case KeyEvent.KEYCODE_TAB:
				if(event.isShiftPressed()){
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					isHeadingTTSInterupted = true;
					gFocusPosition--;
					if (gFocusPosition <= 0) {
						gFocusPosition = gMinFocusableItem;
					}

					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}else{
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					isHeadingTTSInterupted = true;
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
				isHeadingTTSInterupted = true;
				gFocusPosition--;
				if (gFocusPosition <= 0) {
					gFocusPosition = gMinFocusableItem;
				}

				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				result = true;
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_BUTTON_2:
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				isHeadingTTSInterupted = true;
				gFocusPosition++;
				if (gFocusPosition >= gMinFocusableItem + 1) {
					gFocusPosition = 1;
				}

				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				result = true;
				break;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_BUTTON_3:
				selectCurrentFocusItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				result = true;
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_PAGE_UP:
			case KeyEvent.KEYCODE_BUTTON_4:
				// gNavigateLeft.setBackground(null);
				// gNavigateLeft.performClick();
				result = true;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_PAGE_DOWN:
			case KeyEvent.KEYCODE_BUTTON_5:
				// gNavigateRight.setBackground(null);
				// gNavigateRight.performClick();
				result = true;
				break;
			case KeyEvent.KEYCODE_BUTTON_6:
				// gVolumeDecrease.setBackground(null);
				// gVolumeDecrease.performClick();
				result = true;
				break;
			case KeyEvent.KEYCODE_BUTTON_7:
				// gVolumeIncrease.setBackground(null);
				// gVolumeIncrease.performClick();
				result = true;
				break;
			}
		}
		isButtonPressed = true;
		return result;
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
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_BUTTON_1:
			// gFocusPosition--;
			// if (gFocusPosition <= 0) {
			// gFocusPosition = gMinFocusableItem;
			// }
			// navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_BUTTON_2:
			// gFocusPosition++;
			// if (gFocusPosition >= gMinFocusableItem + 1) {
			// gFocusPosition = 1;
			// }
			// navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
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
			gNavigateRight.performClick();
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

	public void selectCurrentFocusItem(int gFocusPosition, int pressed_released) {
		switch (pressed_released) {
		case Constants.REACH_NEW_ITEM:
			if (gFocusPosition == 1) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 2) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView1.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 3) {
				textView2.setBackground(getResources().getDrawable(
						R.drawable.focused));

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView2.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 4) {
				textView3.setBackground(getResources().getDrawable(
						R.drawable.focused));

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView3.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 5) {
				textView4.setBackground(getResources().getDrawable(
						R.drawable.focused));

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView4.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 6) {
				textView5.setBackground(getResources().getDrawable(
						R.drawable.focused));

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView5.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 7) {
				textView6.setBackground(getResources().getDrawable(
						R.drawable.focused));

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(textView6.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 8) {
				// gHelp.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 9) {
				// gVolumeDecrease.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 10) {
				// gVolumeIncrease.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 11) {
				// gFontDecrease.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 12) {
				// gFontIncrease.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 15) {
				// gNavigateLeft.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 16) {
				// gNavigateRight.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
			}
			break;
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 1) {
				// gBallotPage.performClick();
			} else if (gFocusPosition == 2) {
				// textView1.performClick();
			} else if (gFocusPosition == 3) {
				// textView2.performClick();
			} else if (gFocusPosition == 4) {
				// textView3.performClick();
			} else if (gFocusPosition == 5) {
				// textView4.performClick();
			} else if (gFocusPosition == 6) {
				// textView5.performClick();
			} else if (gFocusPosition == 7) {
				// textView6.performClick();
			} else if (gFocusPosition == 8) {
				resetActivityVariable();
				gHelp.performClick();
			} else if (gFocusPosition == 9) {
				gVolumeDecrease.performClick();
			} else if (gFocusPosition == 10) {
				gVolumeIncrease.performClick();
			} else if (gFocusPosition == 11) {
				gFontDecrease.performClick();
			} else if (gFocusPosition == 12) {
				gFontIncrease.performClick();
			} else if (gFocusPosition == 13) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom)
							+ Constants.DOT_SPACE + Constants.DOT_SPACE
							+ getString(R.string.scrn_bottom_press_again), null, true);
				}
			} else if (gFocusPosition == 14) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			} else if (gFocusPosition == 15) {
				resetActivityVariable();
				gNavigateLeft.performClick();
			} else if (gFocusPosition == 16) {
				resetActivityVariable();
				gNavigateRight.performClick();
			}
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	public void speakWord(String word, HashMap<String, String> utteranceId, boolean shouldRepeat) {
		Log.d(TAG, "focus position in helpscreen = " + gFocusPosition);
		if (gTTS != null && utteranceId != null) {
			gTTS.speak(word, TextToSpeech.QUEUE_FLUSH, utteranceId, shouldRepeat);
		} else if (gTTS != null) {
			gTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null,shouldRepeat);
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {
			if (Constants.SETTING_LANGUAGE == Constants.DEFAULT_LANG_SETTING) {
				gTTS.setLanguage(Locale.US);
			} else {
				gTTS.setLanguage(new Locale("spa", "ESP"));
			}

			gTTS.setSpeechRate(Constants.SETTING_TTS_SPEED);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
					UtterenceProgressHelper.UID_HELP_SCREEEN);
			gTTSProgressHelper.isHybridChunkRead = true;
			speakWord(gBallotPage.getText().toString(), params, true);
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
				if (Constants.SETTING_TTS_VOICE == Constants.DEFAULT_TTS_VOICE) {
					gTTS = new SpeakManager(gContext, this, "com.svox.classic");
				} else {
					gTTS = new SpeakManager(gContext, this, "com.ivona.tts");
				}

				gTTSProgressHelper = new UtterenceProgressHelper(
						HelpScreen.this);
				gTTS.setOnUtteranceProgressListener(gTTSProgressHelper);
			} else {
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		}
	}

	@Override
	public void onResume() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(gHeadsetListener, filter);
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

	@Override
	protected void onStart() {
		super.onStart();
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, Constants.TTS_DATA_CHECK_CODE);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		gBallotPage.setBackgroundResource(R.drawable.focused);
		gBallotPage.setTextSize(Constants.SETTING_FONT_SIZE);
		
		textView1.setTextSize(Constants.SETTING_FONT_SIZE);
		textView2.setTextSize(Constants.SETTING_FONT_SIZE);
		textView3.setTextSize(Constants.SETTING_FONT_SIZE);
		textView4.setTextSize(Constants.SETTING_FONT_SIZE);
		textView5.setTextSize(Constants.SETTING_FONT_SIZE);
		textView6.setTextSize(Constants.SETTING_FONT_SIZE);
		textView1.setOnTouchListener(gOnTouchListener);
		textView1.setOnClickListener(sOnClickListener);
		textView2.setOnTouchListener(gOnTouchListener);
		textView2.setOnClickListener(sOnClickListener);
		textView3.setOnTouchListener(gOnTouchListener);
		textView3.setOnClickListener(sOnClickListener);
		textView4.setOnTouchListener(gOnTouchListener);
		textView4.setOnClickListener(sOnClickListener);
		textView5.setOnTouchListener(gOnTouchListener);
		textView5.setOnClickListener(sOnClickListener);
		textView6.setOnTouchListener(gOnTouchListener);
		textView6.setOnClickListener(sOnClickListener);
		
		gMessageContainer.setOnTouchListener(gOnTouchListener);
		gHelp.setOnClickListener(sOnClickListener);
		gHelp.setOnTouchListener(gOnTouchListener);
		gBallotPage.setOnClickListener(sOnClickListener);
		gBallotPage.setOnTouchListener(gOnTouchListener);
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

		gBallotPage.requestFocus();
	}

	protected void resetDeviceVarOnStateChange() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		gFocusPosition = 0;
	}

	protected void resetActivityVariable() {
		isHeadingTTSInterupted = false;
		gFocusPosition = 1;
	}

	public void setNightMode() {
		gBallotPage
				.setTextColor(getResources().getColor(android.R.color.white));
		gBallotPage.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gMessageContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		textView1.setTextColor(getResources().getColor(android.R.color.white));
		textView1.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		textView2.setTextColor(getResources().getColor(android.R.color.white));
		textView2.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		textView3.setTextColor(getResources().getColor(android.R.color.white));
		textView3.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		textView4.setTextColor(getResources().getColor(android.R.color.white));
		textView4.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		textView5.setTextColor(getResources().getColor(android.R.color.white));
		textView5.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		textView6.setTextColor(getResources().getColor(android.R.color.white));
		textView6.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		// textView7.setTextColor(getResources().getColor(android.R.color.white));
		// textView7.setBackgroundColor(getResources().getColor(android.R.color.black));
		// textView8.setTextColor(getResources().getColor(android.R.color.white));
		// textView8.setBackgroundColor(getResources().getColor(android.R.color.black));
	}

	public void stopChunkReading() {
		if ((1 <= gFocusPosition) && (gFocusPosition < 8)) {
			isHybridModeOn = false;
			if(gTTSProgressHelper != null){
				gTTSProgressHelper.isHybridChunkRead = false;
			}
			speakWord("", null, false);
		}
	}
}