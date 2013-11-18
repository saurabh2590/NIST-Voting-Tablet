package org.easyaccess.nist;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Gravity;
import android.view.KeyEvent;
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

public class BallotEnd extends Activity implements OnInitListener {

	int gFocusPosition = 1;
	int gMinFocusableItem = 11;
	boolean isButtonPressed = false;
	boolean isBallotConfirmed = false;
	boolean isHeadingTTSInterupted = false;
	
	Context gContext = null;
	Thread gTTSThread = null;
	String gTTSOnStart = null;
	SpeakManager gTTS = null;
	AudioManager audioManager = null;
	HeadsetListener gHeadsetListener = null;
	UtterenceProgressHelper gTTSProgressHelper = null;

	Button gFinishBtn = null;
	ImageButton gHelp = null;
	ImageButton gFontDecrease = null, gFontIncrease = null;
	ImageButton gNavigateLeft = null, gNavigateRight = null;
	ImageButton gVolumeIncrease = null, gVolumeDecrease = null;

	TextView gBallotPage = null, gMessageText = null;

	View gBtmView = null, gTopView = null;
	View gRootView = null;
	View gAlertMsgContainer = null;
//	View gAlertFinishContainer = null;
	View gBottomBtnContainer = null;
	Runnable postBallotConfirmed = new Runnable() {
		@Override
		public void run() {
			voteAgain();
		}
	};
	
	Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_ballotend);

		gContext = BallotEnd.this;
		gFinishBtn = (Button) findViewById(R.id.btn_finish);
		gBallotPage = (TextView) findViewById(R.id.ballot_page);
		gMessageText = (TextView) findViewById(R.id.alrt_msg_one);

		gTopView = findViewById(R.id.v_scrn_top);
		gBtmView = findViewById(R.id.v_scrn_btm);
		gHelp = (ImageButton) findViewById(R.id.btn_help);
		gNavigateRight = (ImageButton) findViewById(R.id.btn_right);
		gNavigateLeft = (ImageButton) findViewById(R.id.btn_left);
		gFontDecrease = (ImageButton) findViewById(R.id.btn_font_decrease);
		gFontIncrease = (ImageButton) findViewById(R.id.btn_font_increase);
		gVolumeDecrease = (ImageButton) findViewById(R.id.btn_volume_decrease);
		gVolumeIncrease = (ImageButton) findViewById(R.id.btn_volume_increase);

		gRootView = findViewById(R.id.alrt_root_view);
		gAlertMsgContainer = findViewById(R.id.alrt_msg_container);
//		gAlertFinishContainer = findViewById(R.id.alrt_finish_container);
		gBottomBtnContainer = findViewById(R.id.bottom_btn_container);
		
		gNavigateRight.setVisibility(View.GONE);
		if (Constants.SETTING_REVERSE_SCREEN) {
			setNightMode();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		gFinishBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
//					gFinishText.clearFocus();
				}
			}
		});
		gHelp.setOnClickListener(sOnClickListener);
		gHelp.setOnTouchListener(gOnTouchListener);
		gFinishBtn.setOnClickListener(sOnClickListener);
//		gFinishText.setOnTouchListener(gOnTouchListener);
		gNavigateRight.setOnClickListener(sOnClickListener);
		gNavigateRight.setOnTouchListener(gOnTouchListener);
		gNavigateLeft.setOnClickListener(sOnClickListener);
		gNavigateLeft.setOnTouchListener(gOnTouchListener);
		gFontDecrease.setOnClickListener(sOnClickListener);
		gFontDecrease.setOnTouchListener(gOnTouchListener);
		gFontIncrease.setOnClickListener(sOnClickListener);
		gFontIncrease.setOnTouchListener(gOnTouchListener);
		gVolumeIncrease.setOnClickListener(sOnClickListener);
		gVolumeIncrease.setOnTouchListener(gOnTouchListener);
		gVolumeDecrease.setOnClickListener(sOnClickListener);
		gVolumeDecrease.setOnTouchListener(gOnTouchListener);
		gBallotPage.setOnTouchListener(gOnTouchListener);
		gBallotPage.setOnClickListener(sOnClickListener);
		gMessageText.setOnTouchListener(gOnTouchListener);
		gMessageText.setOnClickListener(sOnClickListener);
		
		gRootView.setOnTouchListener(gOnTouchListener);

		gFinishBtn.setTextSize(Constants.SETTING_FONT_SIZE);
		gBallotPage.setTextSize(Constants.SETTING_FONT_SIZE);
		gMessageText.setTextSize(Constants.SETTING_FONT_SIZE);

		gHeadsetListener = new HeadsetListener();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		setWarningView();
		
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(SpeakManager.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, Constants.TTS_DATA_CHECK_CODE);
	}

	private void setWarningView() {
		String msg = "";
		String alertPageTitle = getString(R.string.exit_page);
		ArrayList<String> alertMessageList = new ArrayList<String>();
		alertMessageList.add(getString(R.string.vote_ready_cast));
		alertMessageList.add(getString(R.string.press) + Constants.SPACE
				+ getString(R.string.cast_ballot_and_finish).toUpperCase()
				+ Constants.SPACE + getString(R.string.below_print_vote)
				+ Constants.SPACE);
		alertMessageList.add(getString(R.string.press_back_on_mistake));

		for (int i = 0; i < alertMessageList.size(); i++) {
			if (i == alertMessageList.size() - 1) {
				msg = msg + alertMessageList.get(i);
			} else {
				msg = msg + alertMessageList.get(i) + Constants.NEXT_LINE
						+ Constants.NEXT_LINE;
			}
		}

		gTTSOnStart = alertPageTitle;
		gBallotPage.setText(alertPageTitle);
		gBallotPage.setBackgroundResource(R.drawable.focused);
		gBallotPage.requestFocus();
		gMessageText.setGravity(Gravity.CENTER);
		gMessageText.setText(msg);
		gFinishBtn.setText(getString(R.string.cast_ballot_and_finish)
				.toUpperCase());
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
					
					if(isBallotConfirmed){
						gFocusPosition = gFocusPosition == 2?3:2;
					}else{
						gFocusPosition--;
						if (gFocusPosition <= 0) {
							gFocusPosition = gMinFocusableItem;
						}
					}
					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}else{
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);

					if(isBallotConfirmed){
						gFocusPosition = gFocusPosition == 2?3:2;
					}else{
						gFocusPosition++;
						if (gFocusPosition >= gMinFocusableItem + 1) {
							gFocusPosition = 1;
						}
					}

					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_BUTTON_1:
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				
				if(isBallotConfirmed){
					gFocusPosition = gFocusPosition == 2?3:2;
				}else{
					gFocusPosition--;
					if (gFocusPosition <= 0) {
						gFocusPosition = gMinFocusableItem;
					}
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_BUTTON_2:
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				if(isBallotConfirmed){
					gFocusPosition = gFocusPosition == 2?3:2;
				}else{
					gFocusPosition++;
					if (gFocusPosition >= gMinFocusableItem + 1) {
						gFocusPosition = 1;
					}
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

	/**
	 * used by keypad/gamepad, remove focus on jumping(btn down) from old item,
	 * set focus on reaching (btn up) at current item and announce description.
	 * 
	 * @param gFocusPosition
	 * @param reach_jump
	 */
	public void navigateToOtherItem(int gFocusPosition, int reach_jump) {
		switch (reach_jump) {
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 1) {
				gBallotPage.setBackground(null);
			} else if (gFocusPosition == 2) {
				gMessageText.setBackground(null);
			} else if (gFocusPosition == 3) {
//				gFinishText.setBackground(null);
			} else if (gFocusPosition == 4) {
				gHelp.setBackground(null);
			} else if (gFocusPosition == 5) {
				gVolumeDecrease.setBackground(null);
			} else if (gFocusPosition == 6) {
				gVolumeIncrease.setBackground(null);
			} else if (gFocusPosition == 7) {
				gFontDecrease.setBackground(null);
			} else if (gFocusPosition == 8) {
				gFontIncrease.setBackground(null);
			} else if (gFocusPosition == 9) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (gFocusPosition == 10) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (gFocusPosition == 11) {
				gNavigateLeft.setBackground(null);
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
				gFinishBtn.setFocusableInTouchMode(false);
				gFinishBtn.clearFocus();
				gMessageText.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gMessageText.getText().toString().toLowerCase(), null, true);
				}
			} else if (gFocusPosition == 3) {
				gFinishBtn.setFocusableInTouchMode(true);
				gFinishBtn.requestFocus();
//				gFinishText.setBackground(getResources().getDrawable(
//						R.drawable.focused));
//				gFinishText.requestFocus();
//				
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gFinishBtn.getText().toString().toLowerCase()
							+ Constants.COMMA_SPACE + getString(R.string.button), null, true);
				}
			} else if (gFocusPosition == 4) {
				gHelp.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFinishBtn.setFocusableInTouchMode(false);
				gFinishBtn.clearFocus();
				gHelp.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_help), null, true);
				}
			} else if (gFocusPosition == 5) {
				gVolumeDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gVolumeDecrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_dec), null, true);
				}
			} else if (gFocusPosition == 6) {
				gVolumeIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gVolumeIncrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_inc), null, true);
				}
			} else if (gFocusPosition == 7) {
				gFontDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFontDecrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_dec), null, true);
				}
			} else if (gFocusPosition == 8) {
				gFontIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFontIncrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_inc), null, true);
				}
			} else if (gFocusPosition == 9) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				gBtmView.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom)
							+ Constants.DOT_SPACE + Constants.DOT_SPACE
							+ getString(R.string.scrn_bottom_press_again), null, true);
				}
			} else if (gFocusPosition == 10) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				gTopView.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			} else if (gFocusPosition == 11) {
				gNavigateLeft.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gNavigateLeft.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.previous_ballot), null, true);
				}
			}
			break;
		}
	}

	/**
	 * used by keypad/gamepad, announce on reaching(btn down) new item, perform
	 * action on jumping(btn up) from current item
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
					speakWord(gMessageText.getText().toString().toLowerCase(), null, true);
				}
			} else if (gFocusPosition == 9) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom)
							+ Constants.DOT_SPACE + Constants.DOT_SPACE
							+ getString(R.string.scrn_bottom_press_again), null, true);
				}
			} else if (gFocusPosition == 10) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			}
			break;
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 3) {
				gFinishBtn.performClick();
			} else if (gFocusPosition == 4) {
				gHelp.performClick();
			} else if (gFocusPosition == 5) {
				gVolumeDecrease.performClick();
			} else if (gFocusPosition == 6) {
				gVolumeIncrease.performClick();
			} else if (gFocusPosition == 7) {
				gFontDecrease.performClick();
			} else if (gFocusPosition == 8) {
				gFontIncrease.performClick();
			} else if (gFocusPosition == 11) {
				gNavigateLeft.performClick();
			}
			break;
		}
	}

	private OnTouchListener gOnTouchListener = new OnTouchListener() {
		/**
		 * on touch down announce the info if it represent text. on touch up perform the action
		 */
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
						speakWord(gMessageText.getText().toString().toLowerCase(), null, true);
					}
					break;
//				case R.id.btn_finish:
//					v.setBackground(getResources().getDrawable(
//							R.drawable.focused));
//					break;
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
//				case R.id.btn_finish:
//					v.setBackground(null);
//					v.performClick();
//					break;
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
					Utils.showCustomDialog(BallotEnd.this, gBallotPage
							.getText().toString(), gBallotPage
							.getTextSize());
				}
				break;
			case R.id.alrt_msg_one:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(BallotEnd.this, gMessageText
							.getText().toString(), gMessageText
							.getTextSize());
				}
				break;
			case R.id.btn_finish:
				ballotConfirmed();
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
				break;
			}
		}
	};

	private void ballotConfirmed() {
		
		if(!isBallotConfirmed){
			navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
			speakWord("", null, false);
			gFocusPosition = 2;
			gMinFocusableItem = 2;
			isBallotConfirmed = true;

			/**
			 * remove finish textview 
			 */
			gNavigateLeft.setVisibility(View.GONE);
			gBallotPage.setVisibility(View.GONE);
			gBottomBtnContainer.setVisibility(View.GONE);
			
			/**
			 * change the text of message 
			 */
			gMessageText.setText(getString(R.string.ballot_finish));
			gFinishBtn.setText(getString(R.string.dialog_ok));
			
			navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
			
			/**
			 * cancel the screen after 15 sec
			 */
			handler.postDelayed(postBallotConfirmed, 15000);
		}else{			
			/**
			 * remove the delayed runnable when canceling manually.
			 * very important
			 */
			handler.removeCallbacks(postBallotConfirmed);
			Utils.writeToFile(getString(R.string.ballot_finish), Constants.NIST_VOTING_PROTOTYPE_DIRECTORY
					+ File.separator + Constants.NIST_VOTING_PROTOTYPE_VOTE_FILE, false);
			voteAgain();
		}
	}
	
	private void voteAgain() {
		ContestActivity.isSummaryGenerated = false;
		speakWord("", null, false);
		finish();
		Intent intent = new Intent(gContext, HomeScreenActivity.class);
		startActivity(intent);
	}

	private void navigateLeft() {
		if(!isBallotConfirmed){
			resetActivity();
//			Intent resultIntent = new Intent();
//			resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			finish();
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
		gFinishBtn.setTextSize(Constants.SETTING_FONT_SIZE);
	}

	protected void launchHelp() {
		resetActivity();
		Intent intent = new Intent(this, HelpScreen.class);
		startActivity(intent);
	}

	public void speakWord(String word, HashMap<String, String> utteranceId, boolean shouldRepeat) {
		if (gTTS != null) {
			gTTS.speak(word, SpeakManager.QUEUE_FLUSH, utteranceId, shouldRepeat);
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
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
						UtterenceProgressHelper.UID_BALLOT_END);
				speakWord(gTTSOnStart, params, true);
			}
		} else if (status == SpeakManager.ERROR) {
			Toast.makeText(gContext, getString(R.string.tts_failed),
					Toast.LENGTH_SHORT).show();
		}
	}

	protected void resetActivity() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		gFocusPosition = 1;
		isHeadingTTSInterupted = false;
		speakWord("", null, false);
	}

	public void setNightMode() {
		gRootView.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gAlertMsgContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
//		gAlertFinishContainer.setBackgroundColor(getResources().getColor(
//				android.R.color.black));
		gBallotPage
				.setTextColor(getResources().getColor(android.R.color.white));
		gBallotPage.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gMessageText.setTextColor(getResources()
				.getColor(android.R.color.white));
		gMessageText.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gFinishBtn
				.setTextColor(getResources().getColor(android.R.color.white));
//		gFinishBtn.setBackgroundColor(getResources().getColor(
//				android.R.color.darker_gray));
		gFinishBtn.setBackgroundColor(getResources().getColor(R.color.bg_button));
	}

	@Override
	protected void onPause() {
		super.onPause();
//		if (gTTS != null) {
//			gTTS.stop();
//			gTTS.shutdown();
//		}
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.TTS_DATA_CHECK_CODE) {
			if (resultCode == SpeakManager.Engine.CHECK_VOICE_DATA_PASS) {
				if (Constants.SETTING_TTS_VOICE == Constants.DEFAULT_TTS_VOICE) {
					gTTS = new SpeakManager(gContext, this, "com.svox.classic");
				} else {
					gTTS = new SpeakManager(gContext, this, "com.ivona.tts");
				}
				
				gTTSProgressHelper = new UtterenceProgressHelper(
						BallotEnd.this);
				gTTS.setOnUtteranceProgressListener(gTTSProgressHelper);
			} else {
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent
						.setAction(SpeakManager.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		}
	}
}