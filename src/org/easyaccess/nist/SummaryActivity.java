package org.easyaccess.nist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.easyaccess.nist.SummaryAdapter.ViewHolder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SummaryActivity extends Activity implements OnInitListener,
		OnItemSelectedListener {
	private static final String TAG = "SummaryActivity";
	// OnItemClickListener {
	// flags
	int gNumOfBallot = -1;
	int gFocusPosition = 1;
	int gMaxFocusableItem = 11;
	boolean isUpKeyPressed = false;
	boolean isButtonPressed = false;
	boolean isRightButtonVisible = false;
	boolean isHeadingTTSInterupted = false;
	boolean isKeyboardKeyPress = false;
	// int gCurrentFocusedListItem = -1;
	int gVisibleItem = 0;

	Context gContext = null;
	SpeakManager gTTS = null;
	// ArrayList<Contest> gContestList;
	AudioManager audioManager = null;
	HeadsetListener gHeadsetListener = null;
	Handler gHandler = null;
	View gFocusedView = null;
	SummaryAdapter gSumryAdapter = null;
	List<Contest> gSummaryListItemRow = null;
	UtterenceProgressHelper gTTSProgressHelper = null;

	// views
	ListView gSummaryList = null;
	ImageButton gHelp = null, gNavigateLeft = null, gNavigateRight = null,
			gGoToSummary = null, gFontDecrease = null, gFontIncrease = null,
			gVolumeIncrease = null, gVolumeDecrease = null;
	Button gScrollDownBtn = null, gScrollUpBtn = null;
	TextView gBallotPage = null, gBallotInstruction = null,
			gHeaderViewText = null, gFooterViewText = null;
	CheckBox gReferendumYes = null, gReferendumNo = null;
	View gBtmView = null, gTopView = null, gBallotInstructionContainer = null,
			gSummaryListContainer = null;

	// gesture listener
	final GestureDetector gdt = new GestureDetector(gContext,
			new GestureListener());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.summary_layout);

		gContext = this;
		gTopView = findViewById(R.id.v_scrn_top);
		gBtmView = findViewById(R.id.v_scrn_btm);
		gSummaryList = (ListView) findViewById(R.id.lv_summary);
		gHelp = (ImageButton) findViewById(R.id.btn_help);
		gNavigateRight = (ImageButton) findViewById(R.id.btn_right);
		gNavigateLeft = (ImageButton) findViewById(R.id.btn_left);
		gFontDecrease = (ImageButton) findViewById(R.id.btn_font_decrease);
		gFontIncrease = (ImageButton) findViewById(R.id.btn_font_increase);
		gGoToSummary = (ImageButton) findViewById(R.id.btn_goto_end);
		gVolumeDecrease = (ImageButton) findViewById(R.id.btn_volume_decrease);
		gVolumeIncrease = (ImageButton) findViewById(R.id.btn_volume_increase);
		gScrollDownBtn = (Button) findViewById(R.id.btn_scroll_down);
		gScrollUpBtn = (Button) findViewById(R.id.btn_scroll_up);

		gBallotPage = (TextView) findViewById(R.id.ballot_page);
		gBallotInstruction = (TextView) findViewById(R.id.second_row);
		gBallotInstructionContainer = findViewById(R.id.second_row_container);
		gSummaryListContainer = findViewById(R.id.lv_summary_container);

		gGoToSummary.setVisibility(View.GONE);
		gHandler = new Handler();

		gNavigateRight.setVisibility(View.GONE);
		// gContestList = Utils.readFile(this);

		View view = getLayoutInflater().inflate(R.layout.header_view, null);
		gFooterViewText = gHeaderViewText = (TextView) view
				.findViewById(R.id.lv_header_text);
		gSummaryList.addHeaderView(view);
		gSummaryList.addFooterView(view);

		gHeaderViewText.setText(R.string.after_confirm);
		gFooterViewText.setText(R.string.after_confirm);
		// gSummaryList.addHeaderView(view);
		
		if (Constants.SETTING_REVERSE_SCREEN) {
			setNightMode();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean success = false;
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
				success = true;
				break;
			case KeyEvent.KEYCODE_TAB:
				isKeyboardKeyPress = true;
				if(event.isShiftPressed()){
					isUpKeyPressed = true;
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);

					gFocusPosition--;
					if (gFocusPosition <= 0) {
						if (isRightButtonVisible) {
							gFocusPosition = gMaxFocusableItem
									+ gSummaryList.getAdapter().getCount();
						} else {
							gFocusPosition = gMaxFocusableItem
									+ gSummaryList.getAdapter().getCount() - 1;
						}
					}

					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}else{
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);

					gFocusPosition++;
					if(!isRightButtonVisible && gFocusPosition >= gMaxFocusableItem
							+ gSummaryList.getAdapter().getCount()){
						gFocusPosition = 1;
					}else if (gFocusPosition >= gMaxFocusableItem
							+ gSummaryList.getAdapter().getCount() + 1) {
						gFocusPosition = 1;
					}

					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				isKeyboardKeyPress = true;
			case KeyEvent.KEYCODE_BUTTON_1:
				success = true;
				isUpKeyPressed = true;
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				gFocusPosition--;
				if (gFocusPosition <= 0) {
					if (isRightButtonVisible) {
						gFocusPosition = gMaxFocusableItem
								+ gSummaryList.getAdapter().getCount();
					} else {
						gFocusPosition = gMaxFocusableItem
								+ gSummaryList.getAdapter().getCount() - 1;
					}
				}

				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				isKeyboardKeyPress = true;
			case KeyEvent.KEYCODE_BUTTON_2:
				success = true;
				
				/**
				 * uncomment this block of code for showing right key, when down key is pressed
				 * also uncomment the line of codes in the utterence progress helper
				 */
//				if (!isRightButtonVisible) {
//					gNavigateRight.setVisibility(View.VISIBLE);
//					isRightButtonVisible = true;
//				}

				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				gFocusPosition++;
				if(!isRightButtonVisible && gFocusPosition >= gMaxFocusableItem
						+ gSummaryList.getAdapter().getCount()){
					gFocusPosition = 1;
				}else if (gFocusPosition >= gMaxFocusableItem
						+ gSummaryList.getAdapter().getCount() + 1) {
					gFocusPosition = 1;
				}

				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_BUTTON_3:
				success = true;
				selectCurrentFocusItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_PAGE_UP:
			case KeyEvent.KEYCODE_BUTTON_4:
				success = true;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_PAGE_DOWN:
			case KeyEvent.KEYCODE_BUTTON_5:
				success = true;
				// gNavigateRight.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
				// gNavigateRight.setBackground(null);
				// gNavigateRight.requestFocus();
				// gNavigateRight.performClick();
				break;
			case KeyEvent.KEYCODE_BUTTON_6:
				success = true;
				// gVolumeDecrease.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
				// gVolumeDecrease.setBackground(getResources().getDrawable(
				// R.drawable.focused));
				// navigateToOtherItem(gFocusPosition,
				// Constants.JUMP_FROM_CURRENT_ITEM);
				// gFocusPosition = (4 + gSummaryList.getAdapter()
				// .getCount());
				// selectCurrentFocusItem(gFocusPosition,
				// Constants.REACH_NEW_ITEM);
				// selectCurrentFocusItem(gFocusPosition,
				// Constants.JUMP_FROM_CURRENT_ITEM);

				// gVolumeDecrease.requestFocus();
				// gVolumeDecrease.performClick();
				break;
			case KeyEvent.KEYCODE_BUTTON_7:
				success = true;
				// navigateToOtherItem(gFocusPosition,
				// Constants.JUMP_FROM_CURRENT_ITEM);
				// gVolumeIncrease.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
				// gVolumeIncrease.setBackground(getResources().getDrawable(
				// R.drawable.focused));
				// gFocusPosition = (5 + gSummaryList.getAdapter()
				// .getCount());
				// selectCurrentFocusItem(gFocusPosition,
				// Constants.REACH_NEW_ITEM);
				// selectCurrentFocusItem(gFocusPosition,
				// Constants.JUMP_FROM_CURRENT_ITEM);

				// gVolumeIncrease.requestFocus();
				// gVolumeIncrease.performClick();
				break;
			}
		}
		isButtonPressed = true;
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		isButtonPressed = false;
		boolean success = false;

		int keyPressed = -1;
		if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
			keyPressed = event.getScanCode();
		} else {
			keyPressed = keyCode;
		}

		switch (keyPressed) {
		case KeyEvent.KEYCODE_F1:
		case KeyEvent.KEYCODE_APP_SWITCH:
			success = true;
			navigateToOtherItem(gFocusPosition,
					Constants.JUMP_FROM_CURRENT_ITEM);

			writeToFile("F1/APP_Switch(help) focus position = "
					+ gFocusPosition + gHelp.isFocused()
					+ System.getProperty("line.separator"));
			writeToFile(System.getProperty("line.separator"));

			gHelp.performClick();
			break;
		case KeyEvent.KEYCODE_TAB:
		case KeyEvent.KEYCODE_DPAD_UP:
			isKeyboardKeyPress = false;
		case KeyEvent.KEYCODE_BUTTON_1:
			isUpKeyPressed = false;
			success = true;
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			isKeyboardKeyPress = false;
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
			// navigateToOtherItem(gFocusPosition,
			// Constants.JUMP_FROM_CURRENT_ITEM);
			// gNavigateLeft.setBackgroundColor(getResources().getColor(
			// android.R.color.holo_orange_dark));
			// gNavigateLeft.setBackground(null);
			// gNavigateLeft.requestFocus();
			gNavigateLeft.performClick();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_PAGE_DOWN:
		case KeyEvent.KEYCODE_BUTTON_5:
			success = true;
			// navigateToOtherItem(gFocusPosition,
			// Constants.JUMP_FROM_CURRENT_ITEM);
			// gFocusPosition = (11 + gSummaryList.getAdapter()
			// .getCount());
			// selectCurrentFocusItem(gFocusPosition, Constants.REACH_NEW_ITEM);
			// selectCurrentFocusItem(gFocusPosition,
			// Constants.JUMP_FROM_CURRENT_ITEM);
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
		return success;
	}

	public void navigateToOtherItem(int focusPosition, int reach_jump) {
		switch (reach_jump) {
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (focusPosition == 1) {
				gBallotPage.setBackground(null);
			} else if (focusPosition == 2) {
				gBallotInstruction.setBackground(null);
			} else if (focusPosition > 2
					&& focusPosition < 3 + gSummaryList.getAdapter().getCount()) {
				View focusedListChild = gSummaryList.getChildAt(focusPosition
						- 3 - gSummaryList.getFirstVisiblePosition());
				focusedListChild.setBackground(null);
			} else if (focusPosition == (2 + 1 + gSummaryList.getAdapter()
					.getCount())) {
				gHelp.setBackground(null);
			} else if (focusPosition == (2 + 2 + gSummaryList.getAdapter()
					.getCount())) {
				gVolumeDecrease.setBackground(null);
			} else if (focusPosition == (2 + 3 + gSummaryList.getAdapter()
					.getCount())) {
				gVolumeIncrease.setBackground(null);
			} else if (focusPosition == (2 + 4 + gSummaryList.getAdapter()
					.getCount())) {
				gFontDecrease.setBackground(null);
			} else if (focusPosition == (2 + 5 + gSummaryList.getAdapter()
					.getCount())) {
				gFontIncrease.setBackground(null);
			} else if (focusPosition == (2 + 6 + gSummaryList.getAdapter()
					.getCount())) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (focusPosition == (2 + 7 + gSummaryList.getAdapter()
					.getCount())) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (focusPosition == (2 + 8 + gSummaryList.getAdapter()
					.getCount())) {
				gNavigateLeft.setBackground(null);
			} else if (focusPosition == (3 + 8 + gSummaryList.getAdapter()
					.getCount())) {
				gNavigateRight.setBackground(null);
			}
			break;
		case Constants.REACH_NEW_ITEM:
			View view = getWindow().getCurrentFocus();
			writeToFile("current view in focus = " + view.getId());
			if (focusPosition == 1) {
				gBallotPage.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), null, true);
				}
			} else if (focusPosition == 2) {
				gBallotInstruction.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if(gNavigateRight != null){
					gNavigateRight.setVisibility(View.VISIBLE);
					isRightButtonVisible = true;
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotInstruction.getText().toString(), null,
							true);
				}
			} else if (focusPosition > 2
					&& focusPosition <= 2 + gSummaryList.getAdapter()
							.getCount()) {
				if (focusPosition - 3 == 0) {
					/**
					 * set 1st item in the list view
					 */
					gSummaryList.setSelection(0);
					gHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// gSummaryList.smoothScrollToPosition(0);
							gSummaryList.getChildAt(0).setBackground(
									getResources().getDrawable(
											R.drawable.focused));
						}
					}, 100);
				} else if (focusPosition - 3 == gSummaryList.getAdapter()
						.getCount() - 1) {
					/**
					 * set last item in the list view
					 */
					gSummaryList.setSelection(gSummaryList.getAdapter()
							.getCount() - 1);

					gHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// gSummaryList.smoothScrollToPosition(gSummaryList
							// .getAdapter().getCount() - 1);
							gSummaryList.getChildAt(
									gSummaryList.getChildCount() - 1)
									.setBackground(
											getResources().getDrawable(
													R.drawable.focused));
						}
					}, 100);
				} else {
					if (focusPosition - 3 >= gSummaryList
							.getLastVisiblePosition()) {
						/**
						 * if focus position is greater/equals the last visible
						 * item than select that item, it move that item to top
						 * of list view
						 */
						gSummaryList.setSelection(gSummaryList
								.getLastVisiblePosition());
					} else if (gSummaryList.getFirstVisiblePosition() != 0
							&& focusPosition - 3 <= gSummaryList
									.getFirstVisiblePosition()) {
						/**
						 * if focus position is b/n the last visible item in
						 * position than select that item
						 */
						int height = gSummaryList.getHeight()
								- gSummaryList.getChildAt(0).getHeight();

						gSummaryList.setSelectionFromTop(
								gSummaryList.getFirstVisiblePosition(), height);
						// gSummaryList.smoothScrollToPositionFromTop(gSummaryList.getFirstVisiblePosition(),
						// height);
					} else {
					}

					gHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							gSummaryList.getChildAt(
									gFocusPosition
											- 3
											- gSummaryList
													.getFirstVisiblePosition())
									.setBackground(
											getResources().getDrawable(
													R.drawable.focused));
						}
					}, 100);
				}

				if (HeadsetListener.isHeadsetConnected) {
					String tts = null;

					if (focusPosition == 3) {
						tts = getString(R.string.after_confirm);
					} else if (focusPosition <= 3 + gSumryAdapter.getCount()) {
						int summaryAdapterItemIndex = focusPosition - 4;
						Contest contest = (Contest) gSumryAdapter
								.getItem(summaryAdapterItemIndex);

						if (contest.isReferendum) {
							tts = getString(R.string.for_) + Constants.SPACE
									+ contest.contest_referendum_title
									+ Constants.SPACE
									+ gContext.getString(R.string.u_hav)
									+ Constants.SPACE;

							if (contest.contest_referendum_value == null) {
								tts = tts
										+ gContext.getString(R.string.not)
										+ Constants.SPACE
										+ gContext
												.getString(R.string.made_choice)
										+ Constants.DOT_SPACE
										+ getString(R.string.if_u_want_to_vote)
										+ Constants.SPACE
										+ (isKeyboardKeyPress?getString(R.string.press_enter):getString(R.string.press_round_green_btn))
										+ Constants.DOT_SPACE;
							} else {
								tts = tts
										+ gContext.getString(R.string.chosen)
										+ Constants.COMMA_SPACE
										+ contest.contest_referendum_value
										+ Constants.COMMA_SPACE
										+ getString(R.string.want_to_change_vote)
										+ Constants.SPACE
										+ (isKeyboardKeyPress?getString(R.string.press_enter):getString(R.string.press_round_green_btn))
										+ Constants.DOT_SPACE;
							}
						} else {
							tts = getString(R.string.for_) + Constants.SPACE
									+ contest.contest_office + Constants.SPACE
									+ gContext.getString(R.string.u_hav);

							List<Candidate> selectedCandidateList = new ArrayList<Candidate>();

							for (int i = 0; i < contest.candidateList.size(); i++) {
								if (contest.candidateList.get(i).candidateCheck) {
									Candidate candidate = new Candidate();
									candidate.isWriteIn = contest.candidateList
											.get(i).isWriteIn;
									candidate.candidateCheck = contest.candidateList
											.get(i).candidateCheck;
									candidate.candidateName = contest.candidateList
											.get(i).candidateName;
									candidate.candidateParty = contest.candidateList
											.get(i).candidateParty;
									selectedCandidateList.add(candidate);
								}
							}

							if (selectedCandidateList.size() > 0) {
								tts = tts + Constants.SPACE
										+ gContext.getString(R.string.chosen)
										+ Constants.SPACE;
								for (int i = 0; i < selectedCandidateList
										.size(); i++) {
									if(selectedCandidateList.get(i).isWriteIn){
										tts = tts
												+ selectedCandidateList.get(i).candidateParty
												+ Constants.COMMA_SPACE;
									}else{
										tts = tts
												+ selectedCandidateList.get(i).candidateName
												+ Constants.COMMA_SPACE;
									}
									
									if (i != selectedCandidateList.size() - 1) {
										tts = tts + getString(R.string.and)
												+ Constants.COMMA_SPACE;
									}
								}

								if (contest.vote_per_candidate
										- selectedCandidateList.size() == 1) {
									tts = tts + gContext.getString(R.string.u_can_vote)
											+ Constants.SPACE
											+ gContext.getString(R.string.one)
											+ Constants.SPACE
											+ gContext.getString(R.string.more)
											+ Constants.SPACE
											+ gContext.getString(R.string.candidate)
											+ Constants.SPACE
											+ gContext.getString(R.string.on_this_page)
											+ Constants.DOT_SPACE;
								} else if (contest.vote_per_candidate
										- selectedCandidateList.size() > 1) {
									tts = tts + gContext.getString(R.string.u_can_vote)
											+ Constants.SPACE
											+ gContext.getString(R.string.more)
											+ Constants.SPACE
											+ gContext.getString(R.string.candidates)
											+ Constants.SPACE
											+ gContext.getString(R.string.on_this_page)
											+ Constants.DOT_SPACE;
								}
								
								tts = tts
										+ getString(R.string.want_to_change_vote)
										+ Constants.SPACE
										+ (isKeyboardKeyPress?getString(R.string.press_enter):getString(R.string.press_round_green_btn))										
										+ Constants.DOT_SPACE;
							} else {
								tts = tts
										+ Constants.SPACE
										+ gContext.getString(R.string.not)
										+ Constants.SPACE
										+ gContext
												.getString(R.string.made_choice)
										+ Constants.DOT_SPACE
										+ getString(R.string.if_u_want_to_vote)
										+ Constants.SPACE
										+ (isKeyboardKeyPress?getString(R.string.press_enter):getString(R.string.press_round_green_btn))
										+ Constants.DOT_SPACE;
							}
						}
					} else {
						tts = getString(R.string.after_confirm);
					}

					speakWord(tts, null, true);
				}
			} else if (focusPosition == 3 + gSummaryList.getAdapter()
					.getCount()) {
				gHelp.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_help), null, true);
				}
			} else if (focusPosition == 4 + gSummaryList.getAdapter()
					.getCount()) {
				gVolumeDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_dec), null, false);
				}
			} else if (focusPosition == 5 + gSummaryList.getAdapter()
					.getCount()) {
				gVolumeIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_inc), null, false);
				}
			} else if (focusPosition == 6 + gSummaryList.getAdapter()
					.getCount()) {
				gFontDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_dec), null, false);
				}
			} else if (focusPosition == 7 + gSummaryList.getAdapter()
					.getCount()) {
				gFontIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_inc), null, false);
				}
			} else if (focusPosition == 8 + gSummaryList.getAdapter()
					.getCount()) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom), null, true);
				}
			} else if (focusPosition == 9 + gSummaryList.getAdapter()
					.getCount()) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			} else if (focusPosition == 10 + gSummaryList.getAdapter()
					.getCount()) {
				gNavigateLeft.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.previous_ballot), null, true);
				}
			} else if (focusPosition == (11 + gSummaryList.getAdapter()
					.getCount())) {
				gNavigateRight.setBackground(getResources().getDrawable(
						R.drawable.focused));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.cast_ballot), null, true);
				}
			}
			break;
		}
	}

	public void selectCurrentFocusItem(int focusPosition, int pressed_released) {
		switch (pressed_released) {
		case Constants.REACH_NEW_ITEM:
			if (focusPosition == 1) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), null, true);
				}
			} else if (focusPosition == 2) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotInstruction.getText().toString(), null,
							true);
				}
			} else if (focusPosition > 2
					&& focusPosition < 3 + gSummaryList.getAdapter().getCount()) {
				((ViewHolder) gSummaryList.getChildAt(
						focusPosition - 3
								- gSummaryList.getFirstVisiblePosition())
						.getTag()).checkboxes.get(0).performClick();

				// announce handle in custom adapter onClick
			} else if (focusPosition == 3 + gSummaryList.getAdapter()
					.getCount()) {
				gHelp.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));

				if (HeadsetListener.isHeadsetConnected) {
					// add message
				}
			} else if (focusPosition == 4 + gSummaryList.getAdapter()
					.getCount()) {
				gVolumeDecrease.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));

				if (HeadsetListener.isHeadsetConnected) {
					//
				}
			} else if (focusPosition == 5 + gSummaryList.getAdapter()
					.getCount()) {
				gVolumeIncrease.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));

				if (HeadsetListener.isHeadsetConnected) {
					//
				}
			} else if (focusPosition == 6 + gSummaryList.getAdapter()
					.getCount()) {
				gFontDecrease.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));

				if (HeadsetListener.isHeadsetConnected) {
					//
				}
			} else if (focusPosition == 7 + gSummaryList.getAdapter()
					.getCount()) {
				gFontIncrease.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));

				if (HeadsetListener.isHeadsetConnected) {
					//
				}
			} else if (focusPosition == 8 + gSummaryList.getAdapter()
					.getCount()) {
			} else if (focusPosition == 9 + gSummaryList.getAdapter()
					.getCount()) {
			} else if (focusPosition == 10 + gSummaryList.getAdapter()
					.getCount()) {
				gNavigateLeft.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));

				if (HeadsetListener.isHeadsetConnected) {

				}
			} else if (focusPosition == 11 + gSummaryList.getAdapter()
					.getCount()) {
				gNavigateRight.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));

				if (HeadsetListener.isHeadsetConnected) {
				}
			}
			break;
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (focusPosition == 3 + gSummaryList.getAdapter().getCount()) {
				gHelp.setBackground(null);
				gHelp.performClick();
			} else if (focusPosition == 4 + gSummaryList.getAdapter()
					.getCount()) {
				gVolumeDecrease.performClick();
				gVolumeDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
			} else if (focusPosition == 5 + gSummaryList.getAdapter()
					.getCount()) {
				gVolumeIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gVolumeIncrease.performClick();
			} else if (focusPosition == 6 + gSummaryList.getAdapter()
					.getCount()) {
				gFontDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFontDecrease.performClick();
			} else if (focusPosition == 7 + gSummaryList.getAdapter()
					.getCount()) {
				gFontIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				gFontIncrease.performClick();
			} else if (focusPosition == 8 + gSummaryList.getAdapter()
					.getCount()) {
			} else if (focusPosition == 9 + gSummaryList.getAdapter()
					.getCount()) {
			} else if (focusPosition == 10 + gSummaryList.getAdapter()
					.getCount()) {
				gNavigateLeft.setBackground(null);
				gNavigateLeft.performClick();
			} else if (focusPosition == 11 + gSummaryList.getAdapter()
					.getCount()) {
				gNavigateRight.setBackground(null);
				gNavigateRight.performClick();
			}
			break;
		}
	}

	private OnClickListener sOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ballot_page:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(SummaryActivity.this,
							gBallotPage.getText().toString(),
							gBallotPage.getTextSize());
				}
				break;
			case R.id.lv_header_text:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(SummaryActivity.this,
							gHeaderViewText.getText().toString(),
							Constants.SETTING_FONT_SIZE);
				}
				break;
			case R.id.second_row:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(SummaryActivity.this,
							gBallotInstruction.getText().toString(),
							gBallotInstruction.getTextSize());
				}
				break;
			case R.id.btn_scroll_down:
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				gFocusPosition = 0;
				
				scroll(true);
				break;
			case R.id.btn_scroll_up:
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				gFocusPosition = 0;
				
				scroll(false);
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
			case R.id.btn_font_decrease:
				setFontSize(Constants.DECREASE);
				break;
			case R.id.btn_font_increase:
				setFontSize(Constants.INCREASE);
				break;
			case R.id.btn_left:
				navigateLeft();
				break;
			case R.id.btn_right:
				if (isRightButtonVisible) {
					navigateRight();
				}
				break;
			}
		}
	};

	protected void launchHelp() {
		resetActivity();
		Intent intent = new Intent(this, HelpScreen.class);
		startActivity(intent);
	}

	protected void scroll(boolean isScrollDown) {
		if (isScrollDown) {
			int lastPositionIndex = gSummaryList.getLastVisiblePosition();
			Log.d(TAG, "lastPositionIndex = " + lastPositionIndex);
			gSummaryList.setSelection(lastPositionIndex);
		} else {
			int height = gSummaryList.getHeight()
					- gSummaryList.getChildAt(0).getHeight();

			gSummaryList.setSelectionFromTop(
					gSummaryList.getFirstVisiblePosition(), height);
		}
	}

	private OnTouchListener gOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Utils.downX = (int) event.getRawX();
				Utils.downY = (int) event.getRawY();
				if(gNavigateRight != null){
					gNavigateRight.setVisibility(View.VISIBLE);
					isRightButtonVisible = true;
				}
				
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				gFocusPosition = 0;
				isHeadingTTSInterupted = true;
				
				switch (v.getId()) {
				// case R.id.lv_summary:
				case R.id.lv_summary_container:
					speakWord("", null, true);
					break;
				case R.id.ballot_page:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(SummaryActivity.this,
//								gBallotPage.getText().toString(),
//								gBallotPage.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gBallotPage.getText().toString(), null, true);
					}
					break;
				case R.id.lv_header_text:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(SummaryActivity.this,
//								gHeaderViewText.getText().toString(),
//								Constants.SETTING_FONT_SIZE);
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gHeaderViewText.getText().toString(), null,
								true);
					}
					break;
				case R.id.second_row:
					v.setBackgroundResource(R.drawable.focused);
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(SummaryActivity.this,
//								gBallotInstruction.getText().toString(),
//								gBallotInstruction.getTextSize());
//					}
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gBallotInstruction.getText().toString(),
								null, true);
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
				case R.id.second_row:
					v.setBackground(null);
					break;
				case R.id.lv_header_text:
					v.setBackground(null);
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
				case R.id.btn_help:
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
				}
			}
			return false;
		}
	};

	protected void navigateLeft() {
		shutUp();
		Intent intent = new Intent(gContext, ContestActivity.class);
		intent.putExtra(Constants.CONTEST_POSITION,
				gSumryAdapter.getCount() - 1);
		intent.putExtra(Constants.RETURN_TO_SUMMARY, false);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	
	protected void navigateRight() {
		shutUp();
		Intent intent = new Intent(this, BallotEnd.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}

	protected void setFontSize(int controlFlag) {
		switch (controlFlag) {
		case Constants.DECREASE:
			Constants.SETTING_FONT_SIZE = Constants.SETTING_FONT_SIZE
					- Constants.FONT_DIFFERENCE;

			if (Constants.SETTING_FONT_SIZE == Constants.MIN_FONT_SIZE) {
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_smallest), null, true);
				}
			} else if (Constants.SETTING_FONT_SIZE < Constants.MIN_FONT_SIZE) {
				Constants.SETTING_FONT_SIZE = Constants.MIN_FONT_SIZE;
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_smallest), null, true);
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
					speakWord(getString(R.string.font_largest), null, true);
				}
			} else if (Constants.SETTING_FONT_SIZE > Constants.MAX_FONT_SIZE) {
				Constants.SETTING_FONT_SIZE = Constants.FONT_SIZE_XXLARGE;
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.font_largest), null, true);
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
		gHeaderViewText.setTextSize(Constants.SETTING_FONT_SIZE);
		gFooterViewText.setTextSize(Constants.SETTING_FONT_SIZE);
		gScrollDownBtn.setTextSize(Constants.SETTING_FONT_SIZE);
		gScrollUpBtn.setTextSize(Constants.SETTING_FONT_SIZE);
		refreshSummaryList();
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
					speakWord(getString(R.string.softest), null, true);
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
					speakWord(getString(R.string.loudest), null, true);
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

	@Override
	protected void onStart() {
		super.onStart();

		gHeadsetListener = new HeadsetListener();

		gBallotPage.setTextSize(Constants.SETTING_FONT_SIZE);
		gBallotInstruction.setTextSize(Constants.SETTING_FONT_SIZE);
		gHeaderViewText.setTextSize(Constants.SETTING_FONT_SIZE);
		gFooterViewText.setTextSize(Constants.SETTING_FONT_SIZE);
		gScrollDownBtn.setTextSize(Constants.SETTING_FONT_SIZE);
		gScrollUpBtn.setTextSize(Constants.SETTING_FONT_SIZE);
		
		gScrollDownBtn.setOnClickListener(sOnClickListener);
		gScrollUpBtn.setOnClickListener(sOnClickListener);
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

		gBallotPage.setOnTouchListener(gOnTouchListener);
		gBallotPage.setOnClickListener(sOnClickListener);
		gBallotInstruction.setOnTouchListener(gOnTouchListener);
		gBallotInstruction.setOnClickListener(sOnClickListener);
		gHeaderViewText.setOnTouchListener(gOnTouchListener);
		gHeaderViewText.setOnClickListener(sOnClickListener);
		gFooterViewText.setOnTouchListener(gOnTouchListener);
		gFooterViewText.setOnClickListener(sOnClickListener);
		
		// gSummaryList.setOnTouchListener(gOnTouchListener);
		gSummaryListContainer.setOnTouchListener(gOnTouchListener);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		gBallotPage.setBackgroundResource(R.drawable.focused);

		SharedPreferences preferences = getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);

		gSummaryListItemRow = getListItemRow((Map<String, String>) preferences
				.getAll());
		gSumryAdapter = new SummaryAdapter(SummaryActivity.this,
				R.layout.summary_row, R.id.tv_ballot, gSummaryListItemRow);
		gSummaryList.setAdapter(gSumryAdapter);
		gSummaryList.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gdt.onTouchEvent(event);
			}
		});

		gSummaryList.setOnItemSelectedListener(this);
		// gBallotPage.requestFocus();
		// setfocusUpandDown(gBallotPage, gNavigateRight.getId(),
		// gBallotInstruction.getId());
		Log.d(TAG, "summary list adapter count = "
				+ gSummaryList.getAdapter().getCount());

		gScrollUpBtn.setVisibility(View.VISIBLE);
		gScrollDownBtn.setVisibility(View.VISIBLE);
		
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(SpeakManager.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, Constants.TTS_DATA_CHECK_CODE);
	}

	private List<Contest> getListItemRow(Map<String, String> ballotInfo) {
		List<Contest> summaryListItemRows = new ArrayList<Contest>();
		Log.d("tushar", "number of contest fetched from preferences = "
				+ ballotInfo.size());

		for (int j = 0; j < ballotInfo.size(); j++) {
			String stringJSON = ballotInfo.get(String.valueOf(j));
			try {
				Log.d("tushar", "string json fetched from prferences = "
						+ stringJSON);
				JSONObject jsonObject = new JSONObject(stringJSON);
				Contest contest = new Contest();

				if (jsonObject.has(Constants.CONTEST_POSITION)) {
					contest.contest_position = Integer.parseInt(jsonObject
							.getString(Constants.CONTEST_POSITION));
				}

				if (jsonObject.has(Constants.CONTEST_VOTE_PER_CANDIDATE)) {
					contest.vote_per_candidate = Integer.parseInt((jsonObject
							.getString(Constants.CONTEST_VOTE_PER_CANDIDATE)));
				}

				if (jsonObject.has(Constants.CONTEST_IS_REFERENDUM)) {
					contest.isReferendum = Boolean.parseBoolean(jsonObject
							.getString(Constants.CONTEST_IS_REFERENDUM));
				}

				if (contest.isReferendum) {
					if (jsonObject.has(Constants.CONTEST_REFERENDUM_TITLE)) {
						contest.contest_referendum_title = jsonObject
								.getString(Constants.CONTEST_REFERENDUM_TITLE);
					}

					if (jsonObject.has(Constants.CONTEST_REFERENDUM_RESPONSE)) {
						contest.contest_referendum_response = Integer
								.parseInt(jsonObject
										.getString(Constants.CONTEST_REFERENDUM_RESPONSE));
					}

					if (jsonObject.has(Constants.CONTEST_REFERENDUM_VALUE)) {
						contest.contest_referendum_value = jsonObject
								.getString(Constants.CONTEST_REFERENDUM_VALUE);
					}
				} else {
					if (jsonObject.has(Constants.CONTEST_OFFICE)) {
						contest.contest_office = jsonObject
								.getString(Constants.CONTEST_OFFICE);
					}

					if (jsonObject.has(Constants.CONTEST_ROW)) {
						JSONArray jsonArray = jsonObject
								.getJSONArray(Constants.CONTEST_ROW);
						contest.candidateList = new ArrayList<Candidate>();

						for (int i = 0; i < jsonArray.length(); i++) {
							Candidate listItemRow = new Candidate();
							jsonObject = jsonArray.getJSONObject(i);

							if (jsonObject.has(Constants.CANDIDATE_CHECK)) {
								listItemRow.candidateCheck = Boolean
										.parseBoolean(jsonObject
												.getString(Constants.CANDIDATE_CHECK));
								Log.d("tushar", "candidate check = "
										+ listItemRow.candidateCheck);
							}

							if (listItemRow.candidateCheck) {
								if (jsonObject.has(Constants.CANDIDATE_NAME)) {
									listItemRow.candidateName = jsonObject
											.getString(Constants.CANDIDATE_NAME);
									Log.d("tushar", "candidate name = "
											+ listItemRow.candidateName);
								}

								if (jsonObject.has(Constants.CANDIDATE_PARTY)) {
									listItemRow.candidateParty = jsonObject
											.getString(Constants.CANDIDATE_PARTY);
									Log.d("tushar", "candidate party = "
											+ listItemRow.candidateParty);
								}

								if (jsonObject.has(Constants.CANDIDATE_WRITEIN)) {
									listItemRow.isWriteIn = Boolean
											.parseBoolean(jsonObject
													.getString(Constants.CANDIDATE_WRITEIN));
									Log.d("tushar", "candidate writein = "
											+ listItemRow.isWriteIn);
								}

								contest.candidateList.add(listItemRow);
							}
						}
					}
				}
				summaryListItemRows.add(contest);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Log.d("tushar", "number of ballot fetched from preferences = "
				+ summaryListItemRows.size());
		return summaryListItemRows;
	}

	public void speakWord(String word, HashMap<String, String> utteranceId,
			boolean shouldRepeat) {
		if (gTTS != null) {
			gTTS.speak(word, TextToSpeech.QUEUE_FLUSH, utteranceId,
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
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
					UtterenceProgressHelper.UID_SUMMARY_ACTIVITY);
			speakWord(gBallotPage.getText().toString(), params, true);
		} else if (status == SpeakManager.ERROR) {
			Toast.makeText(gContext, getString(R.string.failed),
					Toast.LENGTH_SHORT).show();
		}
	}

	private void refreshSummaryList() {
		gSumryAdapter.notifyDataSetChanged();
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

				gTTSProgressHelper = new UtterenceProgressHelper(this);
				gTTS.setOnUtteranceProgressListener(gTTSProgressHelper);
			} else {
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent
						.setAction(SpeakManager.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		}
	}

	private void writeToFile(String string) {
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(
					new FileWriter(
							new File(
									Environment
											.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
											+ File.separator
											+ "preference_file.txt"), true));
			bufferedWriter.write(string);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
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
		super.onPause();
//		if (gTTS != null) {
//			gTTS.stop();
//			gTTS.shutdown();
//		}
//		unregisterReceiver(gHeadsetListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (gTTS != null) {
			gTTS.stop();
			gTTS.shutdown();
		}

		unregisterReceiver(gHeadsetListener);
	}

	protected void resetActivity() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		gFocusPosition = 1;
		isHeadingTTSInterupted = false;
		speakWord("", null, true);
	}

	// @Override
	// public void onFocusChange(View v, boolean hasFocus) {
	// if (!hasFocus) {
	// navigateToOtherItem(gFocusPosition,
	// Constants.JUMP_FROM_CURRENT_ITEM);
	// }
	//
	// if (hasFocus) {
	// switch (v.getId()) {
	// case R.id.ballot_page:
	// if (HeadsetListener.isHeadsetConnected) {
	// speakWord(gBallotPage.getText().toString());
	// }
	// break;
	// case R.id.second_row:
	// if (HeadsetListener.isHeadsetConnected) {
	// speakWord(gBallotInstruction.getText().toString());
	// }
	// break;
	// case R.id.lv_summary:
	// Toast.makeText(gContext, "listview summary", Toast.LENGTH_SHORT)
	// .show();
	// break;
	// case R.id.btn_help:
	// Toast.makeText(gContext, "help button", Toast.LENGTH_SHORT)
	// .show();
	// break;
	// case R.id.btn_volume_decrease:
	// Toast.makeText(gContext, "volume decrease", Toast.LENGTH_SHORT)
	// .show();
	// break;
	// case R.id.btn_volume_increase:
	// Toast.makeText(gContext, "volume increase", Toast.LENGTH_SHORT)
	// .show();
	// break;
	// case R.id.btn_font_decrease:
	// Toast.makeText(gContext, "font decrease", Toast.LENGTH_SHORT)
	// .show();
	// break;
	// case R.id.btn_font_increase:
	// Toast.makeText(gContext, "font increase", Toast.LENGTH_SHORT)
	// .show();
	// break;
	// case R.id.v_scrn_btm:
	// Toast.makeText(gContext, "scren bottom", Toast.LENGTH_SHORT)
	// .show();
	// break;
	// case R.id.v_scrn_top:
	// Toast.makeText(gContext, "scrn top", Toast.LENGTH_SHORT).show();
	// break;
	// case R.id.btn_left:
	// Toast.makeText(gContext, "btn left", Toast.LENGTH_SHORT).show();
	// break;
	// case R.id.btn_right:
	// Toast.makeText(gContext, "btn right", Toast.LENGTH_SHORT)
	// .show();
	// break;
	// }
	// }
	// }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// writeToFile("item selected function focus position = " +
		// gFocusPosition
		// + " isfocused = " + arg1.isFocused() + ", isactivated = "
		// + arg1.isActivated() + ", isselected = " + arg1.isSelected()
		// + ", is in touch mode = " + arg1.isInTouchMode()
		// + System.getProperty("line.separator"));
		// writeToFile(System.getProperty("line.separator"));
		gSummaryList
				.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		gSummaryList
				.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
	}

	// public void setfocusUpandDown(View v, int upViewID, int downViewID) {
	// v.setNextFocusUpId(upViewID);
	// v.setNextFocusDownId(downViewID);
	// }

	public void setNightMode() {
		gSummaryList.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotPage
				.setTextColor(getResources().getColor(android.R.color.white));
		gBallotPage.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotInstruction.setTextColor(getResources().getColor(
				android.R.color.white));
		gHeaderViewText.setTextColor(getResources().getColor(
				android.R.color.white));
		gFooterViewText.setTextColor(getResources().getColor(
				android.R.color.white));
		gBallotInstruction.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gSummaryListContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotInstructionContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gScrollUpBtn.setBackgroundColor(getResources().getColor(
				R.color.bg_button));
		gScrollUpBtn.setTextColor(getResources()
				.getColor(android.R.color.white));
		gScrollDownBtn.setBackgroundColor(getResources().getColor(R.color.bg_button));
		gScrollDownBtn.setTextColor(getResources().getColor(
				android.R.color.white));
	}

	private void shutUp() {
		speakWord("", null, true);
	}

	private class GestureListener extends SimpleOnGestureListener {
		private static final String TAG = "GestureListener";

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
	}
}