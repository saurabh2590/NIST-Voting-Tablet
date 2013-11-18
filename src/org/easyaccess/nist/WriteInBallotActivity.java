package org.easyaccess.nist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.Keyboard.Key;
import android.media.AudioManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class WriteInBallotActivity extends Activity implements OnInitListener {

	Context gContext = null;
	int gFocusPosition = 1;
	int gMaxFocusableItem = 12;
	boolean isContestExplored = false;
	boolean isHeadingTTSInterupted = false;
	boolean isButtonPressed = false;

	AudioManager gAudioManager = null;
	HeadsetListener gHeadsetListener = null;
	SpeakManager gTTS = null;
	String gTTSOnStart = null;
	Thread gTTSThread = null;
	UtterenceProgressHelper gTTSProgressHelper = null;

	// widget
	CustomKeyboard gCustomKeyboard = null;
	View gBtmView = null, gTopView = null;
	ImageButton gHelp = null;
	ImageButton gNavigateLeft = null, gNavigateRight = null;
	ImageButton gFontDecrease = null, gFontIncrease = null;
	ImageButton gVolumeIncrease = null, gVolumeDecrease = null;
	TextView gBallotPage = null, gBallotInstruction = null;
	EditText gEditText = null;
	View gRootView = null;
	View gEditTextContainer = null;
	int gWriteInBallotPosition = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_writeinballot);

		gTopView = findViewById(R.id.v_scrn_top);
		gBtmView = findViewById(R.id.v_scrn_btm);
		gHelp = (ImageButton) findViewById(R.id.btn_help);
		gNavigateRight = (ImageButton) findViewById(R.id.btn_right);
		gNavigateLeft = (ImageButton) findViewById(R.id.btn_left);
		gFontDecrease = (ImageButton) findViewById(R.id.btn_font_decrease);
		gFontIncrease = (ImageButton) findViewById(R.id.btn_font_increase);
		gVolumeDecrease = (ImageButton) findViewById(R.id.btn_volume_decrease);
		gVolumeIncrease = (ImageButton) findViewById(R.id.btn_volume_increase);
		gRootView = findViewById(R.id.view_activity_root);
		gEditTextContainer = findViewById(R.id.et_container);

		gEditText = (EditText) findViewById(R.id.et_keybrdchk);

		gBallotPage = (TextView) findViewById(R.id.ballot_page);
		gBallotInstruction = (TextView) findViewById(R.id.second_row);

		gCustomKeyboard = new CustomKeyboard(this, R.id.keyboardview,
				R.xml.hexkbd);
		if (Constants.SETTING_REVERSE_SCREEN) {
			setNightMode();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(SpeakManager.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, Constants.TTS_DATA_CHECK_CODE);

		gHelp.setOnClickListener(gOnClickListener);
		gHelp.setOnTouchListener(gOnTouchListener);
		gNavigateRight.setOnClickListener(gOnClickListener);
		gNavigateRight.setOnTouchListener(gOnTouchListener);
		gNavigateLeft.setOnClickListener(gOnClickListener);
		gNavigateLeft.setOnTouchListener(gOnTouchListener);
		gFontDecrease.setOnClickListener(gOnClickListener);
		gFontDecrease.setOnTouchListener(gOnTouchListener);
		gFontIncrease.setOnClickListener(gOnClickListener);
		gFontIncrease.setOnTouchListener(gOnTouchListener);
		gVolumeIncrease.setOnClickListener(gOnClickListener);
		gVolumeIncrease.setOnTouchListener(gOnTouchListener);
		gVolumeDecrease.setOnClickListener(gOnClickListener);
		gVolumeDecrease.setOnTouchListener(gOnTouchListener);
		gEditText.setOnClickListener(gOnClickListener);
		gEditText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (event.getAction() == KeyEvent.ACTION_UP
								&& event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
							navigateRight();
							return false;
						}
						return true;
					}
				});

		gEditText.addTextChangedListener(new TextWatcher() {
			String textBeforeEdit = null;
			StringBuilder deletedWord = new StringBuilder();

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (textBeforeEdit != null) {
					if (textBeforeEdit.length() > s.length()) {
						if (start > 0) {
							if (textBeforeEdit.charAt(start) == 32) {
								if (deletedWord.length() > 0) {
									speakWord(deletedWord.reverse().toString()
											+ Constants.SPACE
											+ getString(R.string.deleted),
											null, false);
									deletedWord = new StringBuilder();
								} else {
									speakWord(getString(R.string.space)
											+ Constants.SPACE
											+ getString(R.string.deleted),
											null, false);
								}
							} else {
								deletedWord.append(textBeforeEdit.charAt(start));
								speakWord(textBeforeEdit.charAt(start)
										+ Constants.SPACE
										+ getString(R.string.deleted), null,
										false);
							}
						} else if (start == 0) {
							if (textBeforeEdit.toString().charAt(start) != 32) {
								deletedWord.append(textBeforeEdit.toString()
										.charAt(start));
								speakWord(deletedWord.reverse().toString()
										+ Constants.SPACE
										+ getString(R.string.deleted), null,
										false);
								deletedWord = new StringBuilder();
							} else {
								if (deletedWord.length() > 0) {
									Log.d("tushar", "deleted word buffer = "
											+ deletedWord.toString());
									speakWord(deletedWord.reverse().toString()
											+ Constants.SPACE
											+ getString(R.string.deleted),
											null, false);
									deletedWord = new StringBuilder();
								} else {
									speakWord(getString(R.string.space)
											+ Constants.SPACE
											+ getString(R.string.deleted),
											null, false);
									deletedWord = new StringBuilder();
								}
							}
						}
					} else if (textBeforeEdit.length() < s.length()) {
						speakWord(s.charAt(start) + "", null, false);
						if (s.charAt(start) == 32) {
							String[] stringArray = s.toString().split(
									Constants.SPACE);

							if (stringArray.length == 0) {
								speakWord(getString(R.string.space)
										+ Constants.DOT_SPACE, null, false);
							} else if (stringArray.length > 0) {
								speakWord(getString(R.string.space)
										+ Constants.DOT_SPACE
										+ stringArray[stringArray.length - 1],
										null, false);
							}
						}
					} else {
						speakWord(getString(R.string.nthing_to_dlt), null,
								false);
					}
				}
				// Toast.makeText(
				// gContext,
				// " CharSequence s = " + s + ", int start = " + start
				// + ", int before = " + before + ", int count = "
				// + count, Toast.LENGTH_LONG).show();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				textBeforeEdit = s.toString();
			}

			@Override
			public void afterTextChanged(Editable s) {
				final String text = s.toString();
				handler.removeCallbacksAndMessages(null);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						char[] charArray = text.toCharArray();

						String enteredText = "";
						for (int i = 0; i < charArray.length; i++) {
							if (charArray[i] == 32) {
								enteredText = enteredText
										+ getString(R.string.space);
							} else {
								enteredText = enteredText + charArray[i];
							}

							if (i < charArray.length) {
								enteredText = enteredText
										+ Constants.COMMA_SPACE;
							}
						}

						if (enteredText.length() > 0) {
							speakWord(getString(R.string.u_have_typed)
									+ Constants.COMMA_SPACE + enteredText,
									null, false);
						}
					}
				}, 3000);
			}
		});

		gRootView.setOnTouchListener(gOnTouchListener);

		gCustomKeyboard.registerEditText(R.id.et_keybrdchk);

		gBallotPage.setOnTouchListener(gOnTouchListener);
		gBallotPage.setOnClickListener(gOnClickListener);
		gBallotInstruction.setOnTouchListener(gOnTouchListener);
		gBallotInstruction.setOnClickListener(gOnClickListener);

		gBallotPage.setTextSize(Constants.SETTING_FONT_SIZE);
		gBallotInstruction.setTextSize(Constants.SETTING_FONT_SIZE);
		gEditText.setTextSize(Constants.SETTING_FONT_SIZE);
		gContext = WriteInBallotActivity.this;
		gHeadsetListener = new HeadsetListener();
		gAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		gBallotPage.setBackgroundResource(R.drawable.focused);
		gBallotPage.requestFocus();

		gWriteInBallotPosition = getIntent().getIntExtra(
				Constants.WRITEIN_POSITION, -1);
		ArrayList<String> writeInInstruction = getIntent()
				.getStringArrayListExtra(Constants.WRITEIN_INSTRUCTION);

		String instruction = "";
		String temp = "";

		gTTSOnStart = gBallotPage.getText().toString() + Constants.SPACE;
		for (int i = 0; i < writeInInstruction.size(); i++) {
			temp = writeInInstruction.get(i) + Constants.SPACE;
			// gTTSOnStart = gTTSOnStart + temp + Constants.COMMA_SPACE;
			instruction = instruction + temp;
			instruction = instruction + Constants.NEXT_LINE;
		}

		// gTTSOnStart = gTTSOnStart +
		// gContext.getString(R.string.hint_keyboard_chk);
		instruction = instruction
				+ gContext.getString(R.string.hint_keyboard_chk);
		gBallotInstruction.setText(instruction);
		Log.d("writeInActivity", "gTTSOnStart = " + gTTSOnStart);
		// gEditText.requestFocus();
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
				isContestExplored = true;
				break;
			case KeyEvent.KEYCODE_TAB:
				if(event.isShiftPressed()){
					isContestExplored = true;
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);

					gFocusPosition--;
					if (gFocusPosition <= 0) {
						gFocusPosition = gMaxFocusableItem;
					}
					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}else{
					isContestExplored = true;
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);

					gFocusPosition++;
					if (gFocusPosition >= gMaxFocusableItem + 1) {
						gFocusPosition = 1;
					}
					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_BUTTON_1:
				isContestExplored = true;
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
				isContestExplored = true;
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				gFocusPosition++;
				if (gFocusPosition >= gMaxFocusableItem + 1) {
					gFocusPosition = 1;
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_BUTTON_3:
				isContestExplored = true;
				selectCurrentFocusItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_PAGE_UP:
			case KeyEvent.KEYCODE_BUTTON_4:
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_PAGE_DOWN:
			case KeyEvent.KEYCODE_BUTTON_5:
				break;
			case KeyEvent.KEYCODE_BUTTON_6:
				isContestExplored = true;
				break;
			case KeyEvent.KEYCODE_BUTTON_7:
				isContestExplored = true;
				break;
			}
		}

		isButtonPressed = true;
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean success = false;
		int keyPressed = -1;

		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			keyPressed = event.getScanCode();
		} else {
			keyPressed = keyCode;
		}

		// for EZ keyboard
		switch (keyPressed) {
		case KeyEvent.KEYCODE_ESCAPE:
			gEditText.setText("");
			gNavigateRight.performClick();
			break;
		case KeyEvent.KEYCODE_F1:
		case KeyEvent.KEYCODE_APP_SWITCH:
			success = true;
			gHelp.performClick();
			break;
		case KeyEvent.KEYCODE_TAB:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_BUTTON_1:
			success = true;
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_BUTTON_2:
			success = true;
			break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_BUTTON_3:
			success = true;
			selectCurrentFocusItem(gFocusPosition,
					Constants.JUMP_FROM_CURRENT_ITEM);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_PAGE_UP:
		case KeyEvent.KEYCODE_BUTTON_4:
			success = true;
			gNavigateLeft.performClick();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_PAGE_DOWN:
		case KeyEvent.KEYCODE_BUTTON_5:
			success = true;
			gNavigateRight.performClick();
			break;
		case KeyEvent.KEYCODE_BUTTON_6:
			success = true;
			gVolumeDecrease.performClick();
			break;
		case KeyEvent.KEYCODE_BUTTON_7:
			success = true;
			gVolumeIncrease.performClick();
			break;
		}
		isButtonPressed = false;
		return success;
	}

	public void navigateToOtherItem(int gFocusPosition, int reach_jump) {
		switch (reach_jump) {
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 1) {
				gBallotPage.setBackground(null);
			} else if (gFocusPosition == 2) {
				gBallotInstruction.setBackground(null);
			} else if (gFocusPosition == 3) {
				// gEditText.setBackground(null);
				gEditText.clearFocus();
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
			} else if (gFocusPosition == 12) {
				gNavigateRight.setBackground(null);
			}
			break;
		case Constants.REACH_NEW_ITEM:
			if (gFocusPosition == 1) {
				gBallotPage.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gBallotPage.requestFocus();
				if (!gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.showCustomKeyboard(gEditText);
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), null, true);
				}
			} else if (gFocusPosition == 2) {
				gBallotInstruction.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gBallotInstruction.requestFocus();
				if (!gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.showCustomKeyboard(gEditText);
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotInstruction.getText().toString(), null,
							true);
				}
			} else if (gFocusPosition == 3) {
				// gEditText.setBackground(getResources().getDrawable(
				// R.drawable.focused));
				gEditText.requestFocus();
				if (!gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.showCustomKeyboard(gEditText);
				}

				if (HeadsetListener.isHeadsetConnected) {
					if (gEditText.getText().toString().length() > 0) {
						speakWord(getString(R.string.u_have_typed)
								+ Constants.COMMA_SPACE
								+ gEditText.getText().toString(), null, true);
					} else {
						speakWord(getString(R.string.entr_text), null, true);
					}
				}
			} else if (gFocusPosition == 4) {
				gHelp.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.hideCustomKeyboard();
				}
				// gHelp.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_help), null, true);
				}
			} else if (gFocusPosition == 5) {
				gVolumeDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.hideCustomKeyboard();
				}
				// gVolumeDecrease.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_dec), null, true);
				}
			} else if (gFocusPosition == 6) {
				gVolumeIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.hideCustomKeyboard();
				}
				// gVolumeIncrease.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_inc), null, true);
				}
			} else if (gFocusPosition == 7) {
				gFontDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gFontDecrease.requestFocus();
				if (gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.hideCustomKeyboard();
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_dec), null, true);
				}
			} else if (gFocusPosition == 8) {
				gFontIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gFontIncrease.requestFocus();
				if (gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.hideCustomKeyboard();
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_inc), null, true);
				}
			} else if (gFocusPosition == 9) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				// gBtmView.requestFocus();

				if (gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.hideCustomKeyboard();
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom), null, true);
					gTTSThread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(1000);
								speakWord(
										getString(R.string.scrn_bottom_press_again),
										null, true);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					});

					gTTSThread.start();
				}
			} else if (gFocusPosition == 10) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				// gTopView.requestFocus();
				if (!gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.showCustomKeyboard(gEditText);
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			} else if (gFocusPosition == 11) {
				gNavigateLeft.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gNavigateLeft.requestFocus();
				if (!gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.showCustomKeyboard(gEditText);
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.previous_ballot), null, true);
				}
			} else if (gFocusPosition == 12) {
				gNavigateRight.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gNavigateRight.requestFocus();
				if (!gCustomKeyboard.isCustomKeyboardVisible()) {
					gCustomKeyboard.showCustomKeyboard(gEditText);
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.next_ballot), null, true);
				}
			}
			break;
		}
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
					speakWord(gBallotInstruction.getText().toString(), null,
							true);
				}
			} else if (gFocusPosition == 3) {
				// if (gCustomKeyboard.isCustomKeyboardVisible()) {
				// gCustomKeyboard.hideCustomKeyboard();
				// } else if (!gCustomKeyboard.isCustomKeyboardVisible()) {
				// gCustomKeyboard.showCustomKeyboard(gEditText);
				// }
			} else if (gFocusPosition == 4) {
				// gHelp.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 5) {
				// gVolumeDecrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 6) {
				// gVolumeIncrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 7) {
				// gFontDecrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 8) {
				// gFontIncrease.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
			} else if (gFocusPosition == 9) {
			} else if (gFocusPosition == 10) {
			} else if (gFocusPosition == 11) {
			} else if (gFocusPosition == 12) {
			}
			break;
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (gFocusPosition == 3) {
				gNavigateRight.performClick();
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
			} else if (gFocusPosition == 9) {
			} else if (gFocusPosition == 10) {
			} else if (gFocusPosition == 11) {
				gNavigateLeft.performClick();
			} else if (gFocusPosition == 12) {
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
				case R.id.view_activity_root:
					speakWord("", null, false);
					break;
				case R.id.ballot_page:
					isContestExplored = true;

					v.setBackgroundResource(R.drawable.focused);
					// if (Constants.SETTING_TOUCH_PRESENT) {
					// Utils.showCustomDialog(WriteInBallotActivity.this,
					// gBallotPage.getText().toString(),
					// gBallotPage.getTextSize());
					// }
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gBallotPage.getText().toString(), null, true);
					}
					break;
				case R.id.second_row:
					isContestExplored = true;
					v.setBackgroundResource(R.drawable.focused);
					// if (Constants.SETTING_TOUCH_PRESENT) {
					// Utils.showCustomDialog(WriteInBallotActivity.this,
					// gBallotInstruction.getText().toString(),
					// gBallotInstruction.getTextSize());
					// }
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gBallotInstruction.getText().toString(),
								null, true);
					}
					break;
				case R.id.btn_help:
					isContestExplored = true;
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_help), null, true);
					}
					break;
				case R.id.btn_volume_decrease:
					isContestExplored = true;
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_vol_dec), null, true);
					}
					break;
				case R.id.btn_volume_increase:
					isContestExplored = true;
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_vol_inc), null, true);
					}
					break;
				case R.id.btn_font_decrease:
					isContestExplored = true;
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_font_dec), null, true);
					}
					break;
				case R.id.btn_font_increase:
					isContestExplored = true;
					v.setBackgroundColor(getResources().getColor(
							android.R.color.holo_orange_dark));
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_font_inc), null, true);
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
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				switch (v.getId()) {
				case R.id.ballot_page:
					v.setBackground(null);
					break;
				case R.id.second_row:
					v.setBackground(null);
					break;
				case R.id.btn_help:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
					// v.performClick();
					break;
				case R.id.btn_volume_decrease:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
					// v.performClick();
					break;
				case R.id.btn_volume_increase:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
					// v.performClick();
					break;
				case R.id.btn_font_decrease:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
					// v.performClick();
					break;
				case R.id.btn_font_increase:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
					// v.performClick();
					break;
				case R.id.btn_left:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
					// v.performClick();
					break;
				case R.id.btn_right:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
					// v.performClick();
					break;
				}
			}
			return false;
		}
	};

	protected void navigateRight() {
		resetActivity();
		closeActivity();
	}

	protected void navigateLeft() {
		resetActivity();
		closeActivity();
	}

	public void closeActivity() {
		Intent intent = new Intent();
		intent.putExtra(Constants.WRITEIN_POSITION, gWriteInBallotPosition);
		String enteredText = gEditText.getText().toString();

		if (enteredText == null) {
			enteredText = "";
		}
		intent.putExtra(Constants.WRITEIN_VALUE, enteredText.toUpperCase());
		setResult(Activity.RESULT_OK, intent);
		Log.d("tushar",
				"write in postion sent back to contest activity = "
						+ gWriteInBallotPosition + ", write in value = "
						+ gEditText.getText());
		finish();
	}

	private OnClickListener gOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Toast.makeText(gContext, v.getId() + ", = " + R.id.et_keybrdchk,
			// Toast.LENGTH_SHORT).show();
			switch (v.getId()) {
			case R.id.ballot_page:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(WriteInBallotActivity.this,
							gBallotPage.getText().toString(),
							gBallotPage.getTextSize());
				}
				break;
			case R.id.second_row:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(WriteInBallotActivity.this,
							gBallotInstruction.getText().toString(),
							gBallotInstruction.getTextSize());
				}
				break;
			case R.id.btn_left:
				navigateLeft();
				break;
			case R.id.et_keybrdchk:
			case R.id.btn_right:
				navigateRight();
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
			}
		}
	};

	protected void launchHelp() {
		resetActivity();
		Intent intent = new Intent(this, HelpScreen.class);
		if (HeadsetListener.isHeadsetConnected) {
			speakWord(getString(R.string.hlp_srn_launched), null, true);
		}
		startActivity(intent);
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

	protected void resetActivity() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		gFocusPosition = 1;
		isHeadingTTSInterupted = false;
		speakWord("", null, false);
		gTTSOnStart = null;
		isContestExplored = false;
		handler.removeCallbacksAndMessages(null);
	}

	private void setVolume(int controlFlag) {
		int maxVolume = gAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int curVolume = gAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);

		switch (controlFlag) {
		case Constants.DECREASE:
			curVolume = curVolume - Constants.V0LUME_DIFFERENCE;

			if (curVolume <= Constants.MIN_VOLUME) {
				gAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						Constants.MIN_VOLUME, 0);

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.softest), null, false);
				}
			} else {
				gAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						curVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.softer), null, false);
				}
			}
			break;
		case Constants.INCREASE:
			curVolume = curVolume + Constants.V0LUME_DIFFERENCE;

			if (curVolume >= maxVolume) {
				gAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						maxVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {// any number
					speakWord(getString(R.string.loudest), null, false);
				}
			} else {
				gAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						curVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.louder), null, false);
				}
			}
			break;
		}
	}

	Handler handler = new Handler();

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
		gBallotInstruction.setTextSize(Constants.SETTING_FONT_SIZE);
	}

	public void speakWord(String word, HashMap<String, String> utteranceId,
			boolean shouldRepeat) {
		Log.d("tushar", "gtts = " + gTTS);
		if (gTTS != null) {
			gTTS.speak(word, SpeakManager.QUEUE_FLUSH, utteranceId,
					shouldRepeat);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(gHeadsetListener, filter);
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
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
					UtterenceProgressHelper.UID_WRITE_IN_ACTIVITY);
			speakWord(gTTSOnStart, params, true);
		} else if (status == SpeakManager.ERROR) {
			// Toast.makeText(gContext, getString(R.string.failed),
			// Toast.LENGTH_SHORT).show();
		}
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
						WriteInBallotActivity.this);
				gTTS.setOnUtteranceProgressListener(gTTSProgressHelper);
			} else {
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent
						.setAction(SpeakManager.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		}
	}

	public void setNightMode() {
		gBallotPage
				.setTextColor(getResources().getColor(android.R.color.white));
		gBallotPage.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotInstruction.setTextColor(getResources().getColor(
				android.R.color.white));
		gBallotInstruction.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gRootView.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gEditTextContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
	}

	@Override
	public void onBackPressed() {
	}
}