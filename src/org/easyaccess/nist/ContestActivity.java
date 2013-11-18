package org.easyaccess.nist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.easyaccess.nist.ContestAdapter.ViewHolder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ContestActivity extends Activity implements OnInitListener,
		OnCheckedChangeListener {

	private static final String TAG = "ContestActivity";

	// flags
	int gBallotPosition = 0;// for loading ballot
	int gFocusPosition = 1;
	int gLastIndexOfChunk = -1;// for keeping track of last index of chunk
	int gMinFocusableItem = 12;
	int focusedListViewItemPosition = -1;

	// boolean isSetTTSParam = true;
	boolean isContestExplored = false;
	boolean isHeadingTTSInterupted = false;
	boolean isLoadingToMemory = true;
	boolean isLeavingCurrentScreen = false;
	boolean isReadBallotDetail = false;
	boolean isKeyboardKeyPress = false;
	private boolean isTTSInit = false;
	
	/**
	 * for toggling from hybrid to basic reading mode
	 */
	boolean isHybridModeOn = true;
	/**
	 * for checking is referendum all chunks is stored in chunk list
	 */
	boolean isChunkListFull = false;
	/**
	 * for checking referendum is reading forward/backward
	 */
	boolean bufferNextChunk = false;
	/**
	 * for checking summary to goto summary or next ballot
	 */
	boolean isCameFromSummary = false;
	/**
	 * for checking summary ballot position is consumed once
	 */
	boolean isSummaryBallotLoaded = false;
	/**
	 * for checking is SummaryGenerated once in each ballot set it to false
	 * again in ballot end.
	 */
	static boolean isSummaryGenerated = false;
	/**
	 * to check that setting is scanned
	 */
	boolean isAlertDialogVisible = false;
	boolean isSettingScanned = false;
	boolean isButtonPressed = false;

	/**
	 * for speaking different tts on start while returning from different entry
	 * points
	 */
	String gTTSOnStart = null;
	/**
	 * for loading all contest
	 */
	ArrayList<Contest> gContestList = null;
	/**
	 * for loading one contest in list
	 */
	ContestAdapter gContestAdapter = null;
	/**
	 * for storing chunks first and last index
	 */
	List<SimpleEntry<Integer, Integer>> gChunkList = null;
	Context gContext = null;
	Thread saveThread = null;
	Thread chunkThread = null;
	SpeakManager gTTS = null;
	AudioManager gAudioManager = null;
	HeadsetListener gHeadsetListener = null;
	UtterenceProgressHelper gTTSProgressHelper = null;

	ListView gCandidateListView = null;
	RelativeLayout gBallotReferendumLayout = null;
	View gBtmView = null, gTopView = null, gReferendumContainer = null,
			gYesCheckBoxView = null, gNoCheckBoxView = null,
			gScrollUpContainer = null, gScrollDownContainer = null,
			gBallotInstructionContainer = null;
	ImageButton gHelp = null, gNavigateLeft = null, gNavigateRight = null,
			gGoToSummary = null, gFontDecrease = null, gFontIncrease = null,
			gVolumeIncrease = null, gVolumeDecrease = null;
	Button gScrollDownBtn = null, gScrollUpBtn = null;
	TextView gBallotPage = null, gBallotInstruction = null,
			gReferendumText = null, gReferendumYesTitle = null,
			gReferendumNoTitle = null;
	CheckBox gReferendumYes = null, gReferendumNo = null;

	Runnable runnable = null;
	final GestureDetector gdt = new GestureDetector(gContext,
			new GestureListener());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.contest_view);
		gCandidateListView = (ListView) findViewById(R.id.lv_candidates);

		gTopView = findViewById(R.id.v_scrn_top);
		gBtmView = findViewById(R.id.v_scrn_btm);
		gReferendumContainer = findViewById(R.id.cb_layout);
		gYesCheckBoxView = findViewById(R.id.cb_yes);
		gNoCheckBoxView = findViewById(R.id.cb_no);
		gBallotInstructionContainer = findViewById(R.id.second_row_container);
		gScrollUpContainer = findViewById(R.id.frame_scroll_up);
		gScrollDownContainer = findViewById(R.id.frame_scroll_down);

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
		gReferendumText = (TextView) findViewById(R.id.tv_referendum_subtitle);
		gReferendumYesTitle = (TextView) gYesCheckBoxView
				.findViewById(R.id.tv_candidate_name);
		gReferendumNoTitle = (TextView) gNoCheckBoxView
				.findViewById(R.id.tv_candidate_name);

		gReferendumYes = (CheckBox) gYesCheckBoxView
				.findViewById(R.id.cb_candidate_check);
		gReferendumNo = (CheckBox) gNoCheckBoxView
				.findViewById(R.id.cb_candidate_check);

		gBallotReferendumLayout = (RelativeLayout) findViewById(R.id.ballot_referendum_container);

		if (Constants.SETTING_REVERSE_SCREEN) {
			setNightMode();
		}
	}

	private void loadContestUI(Contest contest, int ballotPosition) {
		toggleReferendumViews(false);
		toggleContestViews(true);

		gContestAdapter = new ContestAdapter(this, R.layout.ballot_choice_row,
				R.id.tv_candidate_name, contest.candidateList);
		gCandidateListView.setAdapter(gContestAdapter);
		gCandidateListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int currentListViewItemPosition = gCandidateListView
						.pointToPosition((int) event.getX(), (int) event.getY());

				View rowView = null;
				ViewHolder viewHolder = null;

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gFocusPosition = 0;

					if (currentListViewItemPosition == AdapterView.INVALID_POSITION) {
						speakWord("", null, false);
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (currentListViewItemPosition == AdapterView.INVALID_POSITION) {
						rowView = gCandidateListView
								.getChildAt(focusedListViewItemPosition);

						if (rowView != null) {
							viewHolder = (ViewHolder) rowView.getTag();
							viewHolder.tvContainer.setBackground(null);
						}
						/**
						 * to handle the boundary condition
						 */
						focusedListViewItemPosition = AdapterView.INVALID_POSITION;
					} else if (focusedListViewItemPosition != currentListViewItemPosition) {
						Log.d("", "gCandidateListView.point to Position ="
								+ currentListViewItemPosition);

						if (focusedListViewItemPosition != AdapterView.INVALID_POSITION) {
							rowView = gCandidateListView
									.getChildAt(focusedListViewItemPosition);
							if (rowView != null) {
								viewHolder = (ViewHolder) rowView.getTag();
								viewHolder.tvContainer.setBackground(null);
							}
						}

						focusedListViewItemPosition = currentListViewItemPosition;
						rowView = gCandidateListView
								.getChildAt(focusedListViewItemPosition);
						viewHolder = (ViewHolder) rowView.getTag();
						viewHolder.tvContainer
								.setBackground(gContext.getResources()
										.getDrawable(R.drawable.focused));

						if (HeadsetListener.isHeadsetConnected) {
							if (runnable != null) {
								gHandler.removeCallbacks(runnable);
							}

							String tts = viewHolder.candidateName.getText()
									.toString() + Constants.SPACE;

							if (viewHolder.candidateParty.getText().length() > 0) {
								if (viewHolder.isWriteIn) {
									tts = tts
											+ gContext
													.getString(R.string.candidate)
											+ Constants.COMMA_SPACE;
								}

								String candidateParty = viewHolder.candidateParty
										.getText().toString();
								tts = tts + candidateParty
										+ Constants.COMMA_SPACE;
							}

							if (viewHolder.checkCandidate.isChecked()) {
								tts = tts + gContext.getString(R.string.is)
										+ Constants.SPACE
										+ gContext.getString(R.string.marked);
							} else {
								tts = tts
										+ gContext.getString(R.string.is)
										+ Constants.SPACE
										+ gContext
												.getString(R.string.not_marked);
							}

							final String textToSpeak = tts;
							runnable = new Runnable() {
								@Override
								public void run() {
									speakWord(textToSpeak, null, true);
								}
							};
							gHandler.postDelayed(runnable, 500);
							isContestExplored = true;
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					rowView = gCandidateListView
							.getChildAt(focusedListViewItemPosition);
					if (rowView != null) {
						viewHolder = (ViewHolder) rowView.getTag();
						viewHolder.tvContainer.setBackground(null);
					}
					break;
				}

				return false;
			}
		});

		loadFromPreferences(gContestAdapter, ballotPosition);

		gBallotInstruction.setText(contest.contest_office);
		gBallotInstruction
				.append(System.getProperty("line.separator")
						+ getString(R.string.vote_for)
						+ Constants.SPACE
						+ getResources().getStringArray(
								R.array.vote_per_candidate)[contest.vote_per_candidate]);
		gContestAdapter.gMaxCandidateSelect = contest.vote_per_candidate;

		if (HeadsetListener.isHeadsetConnected) {
			String tts = gBallotPage.getText().toString() + Constants.DOT_SPACE;

			// if (gContestAdapter.gSelectedCandidateList.size() > 0) {
			// params = null;
			// tts = tts + getString(R.string.voted_for) + Constants.SPACE;
			//
			// /**
			// * add the selected candidate name for tts
			// */
			// boolean addAnd = false;
			// for (int i = 0; i < gContestAdapter.gCandidateList.size(); i++) {
			// Candidate candidate = gContestAdapter.gCandidateList.get(i);
			// if (candidate.candidateCheck) {
			// if (addAnd) {
			// tts = tts + Constants.COMMA_SPACE
			// + getString(R.string.and)
			// + Constants.COMMA_SPACE;
			// }
			//
			// if (candidate.isWriteIn) {
			// tts = tts + candidate.candidateName
			// + Constants.SPACE
			// + getString(R.string.candidate)
			// + Constants.COMMA_SPACE
			// + candidate.candidateParty
			// + Constants.SPACE;
			// } else {
			// tts = tts + candidate.candidateName
			// + Constants.COMMA_SPACE;
			// }
			// addAnd = true;
			// }
			// }
			//
			// tts = tts + Constants.DOT_SPACE;
			// // for (int i = 0; i < gContestAdapter.gSelectedCandidateList
			// // .size(); i++) {
			// // tts = tts
			// // + gContestAdapter.gSelectedCandidateList.get(i).candidateName
			// // + Constants.COMMA_SPACE;
			// //
			// // if (i != gContestAdapter.gSelectedCandidateList.size() - 1) {
			// // tts = tts + getString(R.string.and)
			// // + Constants.COMMA_SPACE;
			// // }
			// // }
			//
			// /**
			// * add the remaining candidate name for tts
			// */
			// if (contest.vote_per_candidate
			// - gContestAdapter.gSelectedCandidateList.size() == 0) {
			// tts = tts + getString(R.string.u_hav) + Constants.SPACE
			// + getString(R.string.zero) + Constants.SPACE
			// + getString(R.string.more_choice);
			// } else if (contest.vote_per_candidate
			// - gContestAdapter.gSelectedCandidateList.size() == 1) {
			// tts = tts + getString(R.string.u_can_vote)
			// + Constants.SPACE + getString(R.string.one)
			// + Constants.SPACE + getString(R.string.more)
			// + Constants.SPACE + getString(R.string.candidate)
			// + Constants.SPACE + getString(R.string.for_office);
			// } else if (contest.vote_per_candidate
			// - gContestAdapter.gSelectedCandidateList.size() > 1) {
			// tts = tts + getString(R.string.u_can_vote)
			// + Constants.SPACE + getString(R.string.more)
			// + Constants.SPACE + getString(R.string.candidates)
			// + Constants.SPACE + getString(R.string.for_office);
			// }
			// }

			Log.d(TAG, "tts of contest loading = " + tts + ", gTTS = " + gTTS + ", isTTSInit = " + isTTSInit);
			if (gTTS == null || !isTTSInit) {
				Log.d(TAG, "gTTS null or not speaking");
				/**
				 * if tts is null or is not speaking(initializing) than make
				 * sure it will speak
				 */
				gTTSOnStart = tts;
			} else {
				Log.d(TAG, "gTTS is not null and speaking");
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
						UtterenceProgressHelper.UID_CONTEST_ACTIVITY);
				speakWord(tts, params, true);
			}
		}

		updateScrollButton();
	}

	private boolean isCandidateListScrollable() {
		boolean result = false;
		int first = gCandidateListView.getFirstVisiblePosition();
		int last = gCandidateListView.getLastVisiblePosition();
		if (first == 0
				&& gCandidateListView.getChildAt(first).getTop() >= 0
				&& last == gCandidateListView.getCount() - 1
				&& gCandidateListView.getChildAt(last).getBottom() <= gCandidateListView
						.getHeight()) {
			/**
			 * fits in list view
			 */
			result = false;
		} else {
			result = true;
		}
		Log.d(TAG, "isList scrollable = " + result);
		return result;
	}

	private void addRemoveLeftArrow(int ballotPosition) {
		if (ballotPosition == 0 && !isCameFromSummary) {
			gNavigateLeft.setVisibility(View.GONE);
			gMinFocusableItem = 11;
		} else if (gNavigateLeft.getVisibility() == View.GONE) {
			gNavigateLeft.setVisibility(View.VISIBLE);
			gMinFocusableItem = 12;
		}
	}

	private void toggleContestViews(boolean isVisible) {
		if (isVisible) {
			gCandidateListView.setVisibility(View.VISIBLE);
		} else {
			gCandidateListView.setVisibility(View.GONE);
		}
	}

	private void toggleReferendumViews(boolean isVisible) {
		if (isVisible) {
			gReferendumText.setVisibility(View.VISIBLE);
			gReferendumText.setTextSize(Constants.SETTING_FONT_SIZE);
			LinearLayout ll = (LinearLayout) findViewById(R.id.cb_layout);
			ll.setVisibility(View.VISIBLE);
		} else {
			gReferendumText.setVisibility(View.GONE);
			LinearLayout ll = (LinearLayout) findViewById(R.id.cb_layout);
			ll.setVisibility(View.GONE);
		}
	}

	protected void loadReferendumUI(Contest contest, int ballotPosition) {
		toggleContestViews(false);
		toggleReferendumViews(true);
		if (!Constants.SETTING_REVERSE_SCREEN) {
			gYesCheckBoxView.setBackground(getResources().getDrawable(
					R.drawable.list_view_boundary));
			gNoCheckBoxView.setBackground(getResources().getDrawable(
					R.drawable.list_view_boundary));
		}

		gReferendumYes.setOnCheckedChangeListener(this);
		gReferendumYesTitle.setText(getString(R.string.accept));
		gReferendumNo.setOnCheckedChangeListener(this);
		gReferendumNoTitle.setText(getString(R.string.reject));

		loadFromPreferences(gContestAdapter, ballotPosition);

		gBallotInstruction.setText(contest.contest_referendum_title);
		if (contest.contest_referendum_instruction != null) {
			// gBallotInstruction.setLines(Constants.ROW_TWO_LINE);
			gBallotInstruction.append(Constants.LINE_SEPRATOR
					+ contest.contest_referendum_instruction);
		}

		gReferendumText.setText(contest.contest_referendum_subs);
		gChunkList = new ArrayList<SimpleEntry<Integer, Integer>>();

		if (HeadsetListener.isHeadsetConnected) {
			String tts = gBallotPage.getText().toString() + Constants.DOT_SPACE;

			// if (gReferendumYes.isChecked()) {
			// tts = tts + Constants.DOT_SPACE
			// + contest.contest_referendum_title
			// + Constants.DOT_SPACE
			// + getString(R.string.referendum_accepted)
			// + Constants.DOT_SPACE;
			// } else if (gReferendumNo.isChecked()) {
			// tts = tts + Constants.DOT_SPACE
			// + contest.contest_referendum_title
			// + Constants.DOT_SPACE
			// + getString(R.string.referendum_not_accepted)
			// + Constants.DOT_SPACE;
			// } else {
			// // tts = tts + contest.contest_referendum_instruction
			// // + Constants.DOT_SPACE;
			// }
			
			Log.d(TAG, "tts of contest loading = " + tts + ", gTTS = " + gTTS + ", isTTSInit = " + isTTSInit);
			if (gTTS == null || !isTTSInit) {
				Log.d(TAG, "gTTS is null or not speaking");
				
				/**
				 * if tts is null or is not speaking(initializing) than make
				 * sure it will speak
				 */
				gTTSOnStart = tts;
			} else {
				Log.d(TAG, "gTTS is not null and speaking");
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
						UtterenceProgressHelper.UID_CONTEST_ACTIVITY);
				speakWord(tts, params, true);
			}
			Log.d(TAG, "tts = " + tts);
		}

		startHybridChunkAlgo();
		updateScrollButton();
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
			} else if (gFocusPosition > 2
					&& gFocusPosition < ((gContestAdapter == null) ? (3 + gChunkList
							.size() + 2) : (3 + gContestAdapter.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 1)
					+ gChunkList.size() + 2) : ((2 + 1) + gContestAdapter
					.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 2)
					+ gChunkList.size() + 2) : ((2 + 2) + gContestAdapter
					.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 3)
					+ gChunkList.size() + 2) : ((2 + 3) + gContestAdapter
					.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 4)
					+ gChunkList.size() + 2) : ((2 + 4) + gContestAdapter
					.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 5)
					+ gChunkList.size() + 2) : ((2 + 5) + gContestAdapter
					.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 6)
					+ gChunkList.size() + 2) : ((2 + 6) + gContestAdapter
					.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 7)
					+ gChunkList.size() + 2) : ((2 + 7) + gContestAdapter
					.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 8)
					+ gChunkList.size() + 2) : ((2 + 8) + gContestAdapter
					.getCount()))) {
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 9)
					+ gChunkList.size() + 2) : ((2 + 9) + gContestAdapter
					.getCount()))) {
				// gNavigateLeft.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), null, true);
				}
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 10)
					+ gChunkList.size() + 2) : ((2 + 10) + gContestAdapter
					.getCount()))) {
				// gNavigateRight.setBackgroundColor(getResources().getColor(
				// android.R.color.holo_orange_dark));
			}
			break;
		case Constants.JUMP_FROM_CURRENT_ITEM:
			// try{
			if (gFocusPosition > 2
					&& gFocusPosition < ((gContestAdapter == null) ? (3 + gChunkList
							.size() + 2) : (3 + gContestAdapter.getCount()))) {
				if (gContestAdapter == null) {
					if (gFocusPosition < 3 + gChunkList.size()) {
						Entry<Integer, Integer> entry = gChunkList
								.get(gFocusPosition - 3);
						speakWord(gReferendumText.getText().toString()
								.subSequence(entry.getKey(), entry.getValue())
								.toString(), null, true);
					} else if (gFocusPosition == 3 + gChunkList.size()) {
						gReferendumYes.performClick();
					} else if (gFocusPosition == 3 + gChunkList.size() + 1) {
						gReferendumNo.performClick();
					}
				} else {
					CheckBox cb = (CheckBox) gCandidateListView.getChildAt(
							gFocusPosition - 3).findViewById(
							R.id.cb_candidate_check);
					cb.performClick();
				}
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 1)
					+ gChunkList.size() + 2) : ((2 + 1) + gContestAdapter
					.getCount()))) {
				gHelp.performClick();
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 2)
					+ gChunkList.size() + 2) : ((2 + 2) + gContestAdapter
					.getCount()))) {
				gVolumeDecrease.performClick();
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 3)
					+ gChunkList.size() + 2) : ((2 + 3) + gContestAdapter
					.getCount()))) {
				gVolumeIncrease.performClick();
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 4)
					+ gChunkList.size() + 2) : ((2 + 4) + gContestAdapter
					.getCount()))) {
				gFontDecrease.performClick();
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 5)
					+ gChunkList.size() + 2) : ((2 + 5) + gContestAdapter
					.getCount()))) {
				gFontIncrease.performClick();
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 6)
					+ gChunkList.size() + 2) : ((2 + 6) + gContestAdapter
					.getCount()))) {
				gGoToSummary.performClick();
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 7)
					+ gChunkList.size() + 2) : ((2 + 7) + gContestAdapter
					.getCount()))) {
				// gBtmView.setBackgroundColor(getResources().getColor(
				// android.R.color.black));

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom)
							+ Constants.DOT_SPACE + Constants.DOT_SPACE
							+ getString(R.string.scrn_bottom_press_again),
							null, true);
				}
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 8)
					+ gChunkList.size() + 2) : ((2 + 8) + gContestAdapter
					.getCount()))) {
				// gTopView.setBackgroundColor(getResources().getColor(
				// android.R.color.black));
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 9)
					+ gChunkList.size() + 2) : ((2 + 9) + gContestAdapter
					.getCount()))) {
				if (gNavigateLeft.getVisibility() == View.VISIBLE) {
					gNavigateLeft.performClick();
				} else {
					gNavigateRight.performClick();
				}
			} else if (gFocusPosition == ((gContestAdapter == null) ? ((2 + 10)
					+ gChunkList.size() + 2) : ((2 + 10) + gContestAdapter
					.getCount()))) {
				gNavigateRight.performClick();
			}

			break;
		}
	}

	public void navigateToOtherItem(int focusPosition, int reach_jump) {
		switch (reach_jump) {
		case Constants.JUMP_FROM_CURRENT_ITEM:
			if (focusPosition == 1) {
				gBallotPage.setBackground(null);
			} else if (focusPosition == 2) {
				gBallotInstruction.setBackground(null);

				if (gContestAdapter == null
						&& focusPosition < 3 + gChunkList.size()) {
					gReferendumText
							.setText(gContestList.get(gBallotPosition).contest_referendum_subs);
				}
			} else if (focusPosition > 2
					&& focusPosition < ((gContestAdapter == null) ? (3 + (gChunkList
							.size() + 2)) : (3 + gContestAdapter.getCount()))) {
				if (gContestAdapter != null) {
					View focusedListChild = gCandidateListView
							.getChildAt(focusPosition
									- 3
									- gCandidateListView
											.getFirstVisiblePosition());
					focusedListChild.setBackground(null);
				} else {
					if (focusPosition < 3 + gChunkList.size()) {
						gReferendumText.setText(gContestList
								.get(gBallotPosition).contest_referendum_subs);
					} else if (focusPosition == 3 + gChunkList.size()) {
						if (!Constants.SETTING_REVERSE_SCREEN) {
							gYesCheckBoxView
									.setBackground(getResources().getDrawable(
											R.drawable.list_view_boundary));
						} else {
							gYesCheckBoxView.setBackground(null);
						}
					} else if (focusPosition == 3 + gChunkList.size() + 1) {
						if (!Constants.SETTING_REVERSE_SCREEN) {
							gNoCheckBoxView
									.setBackground(getResources().getDrawable(
											R.drawable.list_view_boundary));
						} else {
							gNoCheckBoxView.setBackground(null);
						}
					}
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 1) + (gChunkList
					.size() + 2)) : ((2 + 1) + gContestAdapter.getCount()))) {
				gHelp.setBackground(null);
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 2) + (gChunkList
					.size() + 2)) : ((2 + 2) + gContestAdapter.getCount()))) {
				gVolumeDecrease.setBackground(null);
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 3) + (gChunkList
					.size() + 2)) : ((2 + 3) + gContestAdapter.getCount()))) {
				gVolumeIncrease.setBackground(null);
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 4) + (gChunkList
					.size() + 2)) : ((2 + 4) + gContestAdapter.getCount()))) {
				gFontDecrease.setBackground(null);
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 5) + (gChunkList
					.size() + 2)) : ((2 + 5) + gContestAdapter.getCount()))) {
				gFontIncrease.setBackground(null);
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 6) + (gChunkList
					.size() + 2)) : ((2 + 6) + gContestAdapter.getCount()))) {
				gGoToSummary.setBackground(null);
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 7) + (gChunkList
					.size() + 2)) : ((2 + 7) + gContestAdapter.getCount()))) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 8) + (gChunkList
					.size() + 2)) : ((2 + 8) + gContestAdapter.getCount()))) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.black));
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 9) + (gChunkList
					.size() + 2)) : ((2 + 9) + gContestAdapter.getCount()))) {
				if (gNavigateLeft.getVisibility() == View.VISIBLE) {
					gNavigateLeft.setBackground(null);
				} else {
					gNavigateRight.setBackground(null);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 10) + (gChunkList
					.size() + 2)) : ((2 + 10) + gContestAdapter.getCount()))) {
				gNavigateRight.setBackground(null);
			}
			break;
		case Constants.REACH_NEW_ITEM:
			if (focusPosition == 1) {
				gBallotPage.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gBallotPage.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(gBallotPage.getText().toString(), null, true);
				}
			} else if (focusPosition == 2) {
				gBallotInstruction.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gBallotInstruction.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					if (isReadBallotDetail) {
						readBallotInfo(gBallotInstruction.getText().toString());
					} else {
						speakWord(gBallotInstruction.getText().toString(),
								null, true);
					}
				}
			} else if (focusPosition > 2
					&& focusPosition < ((gContestAdapter == null) ? (3 + (gChunkList
							.size() + 2)) : (3 + gContestAdapter.getCount()))) {

				if (gContestAdapter == null) {
					if (focusPosition < 3 + gChunkList.size()) {
						setReferendumFocus(gChunkList.get(focusPosition - 3));
						// gReferendumSubtitle.requestFocus();
					} else if (focusPosition == 3 + gChunkList.size()) {
						/**
						 * stop the hybrid chunk read without canceling the
						 * hybrid mode
						 */
						gYesCheckBoxView.setBackground(getResources()
								.getDrawable(R.drawable.focused));

						if (HeadsetListener.isHeadsetConnected) {
							String tts = null;

							String referendumName = gBallotInstruction
									.getText().toString();
							referendumName = referendumName
									.split(Constants.LINE_SEPRATOR)[0];

							if (!gReferendumYes.isChecked()
									&& !gReferendumNo.isChecked()) {
								/**
								 * "Accept is not marked for [referred measure
								 * number 3A] . Press the round green button on
								 * the keypad now to accept the referendum."
								 */
								tts = getString(R.string.accept)
										+ Constants.COMMA_SPACE
										+ getString(R.string.is)
										+ Constants.SPACE
										+ getString(R.string.not)
										+ Constants.SPACE
										+ getString(R.string.marked)
										+ Constants.SPACE
										+ getString(R.string.for_)
										+ Constants.SPACE
										+ referendumName
										+ Constants.DOT_SPACE
										+ (isKeyboardKeyPress ? getString(R.string.press_enter)
												: getString(R.string.press_round_green_btn))
										+ Constants.SPACE
										+ getString(R.string.to_acpt_referendum);
							} else if (gReferendumNo.isChecked()) {
								/**
								 * Accept is not marked for [referred measure
								 * number 3A] . You will need to first erase the
								 * other mark in order accept the referendum.
								 */
								tts = getString(R.string.accept)
										+ Constants.COMMA_SPACE
										+ getString(R.string.is)
										+ Constants.SPACE
										+ getString(R.string.not)
										+ Constants.SPACE
										+ getString(R.string.marked)
										+ Constants.SPACE
										+ getString(R.string.for_)
										+ Constants.SPACE
										+ referendumName
										+ Constants.COMMA_SPACE
										+ String.format(
												getString(R.string.erase_other_mark_to_),
												getString(R.string.accept));
							} else if (gReferendumYes.isChecked()) {
								/**
								 * Accept is marked for [referred measure number
								 * 3A]. Press the round green button on the
								 * keypad now to erase the mark.
								 */
								tts = getString(R.string.accept)
										+ Constants.COMMA_SPACE
										+ getString(R.string.is)
										+ Constants.SPACE
										+ getString(R.string.marked)
										+ Constants.SPACE
										+ getString(R.string.for_)
										+ Constants.SPACE
										+ referendumName
										+ Constants.COMMA_SPACE
										+ (isKeyboardKeyPress ? getString(R.string.press_enter)
												: getString(R.string.press_round_green_btn))
										+ Constants.SPACE
										+ getString(R.string.to_erase_mark);
							}
							speakWord(tts, null, true);
						}
					} else if (focusPosition == 3 + gChunkList.size() + 1) {
						gNoCheckBoxView.setBackground(getResources()
								.getDrawable(R.drawable.focused));
						if (HeadsetListener.isHeadsetConnected) {
							String tts = null;

							String referendumName = gBallotInstruction
									.getText().toString();
							referendumName = referendumName
									.split(Constants.LINE_SEPRATOR)[0];

							if (!gReferendumYes.isChecked()
									&& !gReferendumNo.isChecked()) {
								/**
								 * Reject is not marked for [referred measure
								 * number 3A] . Press the round green button on
								 * the keypad now to reject the referendum
								 */
								tts = getString(R.string.reject)
										+ Constants.COMMA_SPACE
										+ getString(R.string.is)
										+ Constants.SPACE
										+ getString(R.string.not)
										+ Constants.SPACE
										+ getString(R.string.marked)
										+ Constants.SPACE
										+ getString(R.string.for_)
										+ Constants.SPACE
										+ referendumName
										+ Constants.COMMA_SPACE
										+ (isKeyboardKeyPress ? getString(R.string.press_enter)
												: getString(R.string.press_round_green_btn))
										+ Constants.SPACE
										+ getString(R.string.to_reject_referendum);
							} else if (gReferendumYes.isChecked()) {
								/**
								 * Reject is not marked for [referred measure
								 * number 3A] . You will need to first erase the
								 * other mark in order reject the referendum.
								 */
								tts = getString(R.string.reject)
										+ Constants.COMMA_SPACE
										+ getString(R.string.is)
										+ Constants.SPACE
										+ getString(R.string.not)
										+ Constants.SPACE
										+ getString(R.string.marked)
										+ Constants.SPACE
										+ getString(R.string.for_)
										+ Constants.SPACE
										+ referendumName
										+ Constants.COMMA_SPACE
										+ String.format(
												getString(R.string.erase_other_mark_to_),
												getString(R.string.reject));
							} else if (gReferendumNo.isChecked()) {
								/**
								 * Reject is marked for [referred measure number
								 * 3A]. Press the round green button on the
								 * keypad now to erase the mark.
								 */
								tts = getString(R.string.accept)
										+ Constants.COMMA_SPACE
										+ getString(R.string.is)
										+ Constants.SPACE
										+ getString(R.string.marked)
										+ Constants.SPACE
										+ getString(R.string.for_)
										+ Constants.SPACE
										+ referendumName
										+ Constants.COMMA_SPACE
										+ (isKeyboardKeyPress ? getString(R.string.press_enter)
												: getString(R.string.press_round_green_btn))
										+ Constants.SPACE
										+ getString(R.string.to_erase_mark);
							}
							speakWord(tts, null, true);
						}
					}
				} else {
					if (focusPosition - 3 == 0) {
						/**
						 * set 1st item in the list view
						 */
						gCandidateListView.setSelection(0);
						gHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								// gSummaryList.smoothScrollToPosition(0);
								gCandidateListView.getChildAt(0).setBackground(
										getResources().getDrawable(
												R.drawable.focused));
							}
						}, 100);
					} else if (focusPosition - 3 == gCandidateListView
							.getAdapter().getCount() - 1) {
						/**
						 * set last item in the list view
						 */
						gCandidateListView.setSelection(gCandidateListView
								.getAdapter().getCount() - 1);

						gHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								gCandidateListView.getChildAt(
										gCandidateListView.getChildCount() - 1)
										.setBackground(
												getResources().getDrawable(
														R.drawable.focused));
							}
						}, 100);
					} else {
						if (focusPosition - 3 >= gCandidateListView
								.getLastVisiblePosition()) {
							/**
							 * if focus position is greater/equals the last
							 * visible item than select that item, it move that
							 * item to top of list view
							 */
							gCandidateListView.setSelection(gCandidateListView
									.getLastVisiblePosition());
						} else if (gCandidateListView.getFirstVisiblePosition() != 0
								&& focusPosition - 3 <= gCandidateListView
										.getFirstVisiblePosition()) {
							/**
							 * if focus position is b/n the last visible item in
							 * position than select that item
							 */
							int height = gCandidateListView.getHeight()
									- gCandidateListView.getChildAt(0)
											.getHeight();

							gCandidateListView.setSelectionFromTop(
									gCandidateListView
											.getFirstVisiblePosition(), height);
						}

						gHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								gCandidateListView
										.getChildAt(
												gFocusPosition
														- 3
														- gCandidateListView
																.getFirstVisiblePosition())
										.setBackground(
												getResources().getDrawable(
														R.drawable.focused));
							}
						}, 100);
					}

					// View focusedListChild = gCandidateListView
					// .getChildAt(focusPosition - 3);
					// focusedListChild.setBackground(getResources().getDrawable(
					// R.drawable.focused));

					String tts = ((Candidate) gContestAdapter
							.getItem(focusPosition - 3)).candidateName
							+ Constants.COMMA_SPACE;

					if (((Candidate) gContestAdapter.getItem(focusPosition - 3)).isWriteIn
							&& ((Candidate) gContestAdapter
									.getItem(focusPosition - 3)).candidateCheck) {
						tts = tts + getString(R.string.candidate)
								+ Constants.SPACE;
					}

					/**
					 * announce the party name if candidate
					 */
					String candidateParty = ((Candidate) gContestAdapter
							.getItem(focusPosition - 3)).candidateParty;
					if (candidateParty != null) {
						tts = tts + candidateParty + Constants.COMMA_SPACE;
					}

					if (HeadsetListener.isHeadsetConnected) {
						if (gContestAdapter.getItem(focusPosition - 3).candidateCheck) {
							tts = tts + getString(R.string.is)
									+ Constants.SPACE
									+ getString(R.string.marked)
									+ Constants.DOT_SPACE;
						} else {
							if (gContestAdapter != null) {
								int noOfCheckedCandidate = 0;
								for (int i = 0; i < gContestAdapter.getCount(); i++) {
									if (gContestAdapter.getItem(i).candidateCheck) {
										noOfCheckedCandidate++;
									}
								}

								if (noOfCheckedCandidate >= gContestAdapter.gMaxCandidateSelect) {
									tts = tts
											+ getString(R.string.is)
											+ Constants.SPACE
											+ getString(R.string.not_marked)
											+ Constants.DOT_SPACE
											+ getString(R.string.want_to_vote)
											+ Constants.COMMA_SPACE
											+ getString(R.string.election_full_first_erase_mark);
								} else {
									tts = tts
											+ getString(R.string.is)
											+ Constants.SPACE
											+ getString(R.string.not_marked)
											+ Constants.DOT_SPACE
											+ getString(R.string.want_to_vote)
											+ Constants.COMMA_SPACE
											+ (isKeyboardKeyPress ? getString(R.string.press_enter)
													: getString(R.string.press_round_green_btn));
								}
							}
						}
						speakWord(tts, null, true);
					}
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 1) + (gChunkList
					.size() + 2)) : ((2 + 1) + gContestAdapter.getCount()))) {
				gHelp.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gHelp.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_help), null, true);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 2) + (gChunkList
					.size() + 2)) : ((2 + 2) + gContestAdapter.getCount()))) {
				gVolumeDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gVolumeDecrease.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_dec), null, true);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 3) + (gChunkList
					.size() + 2)) : ((2 + 3) + gContestAdapter.getCount()))) {
				// gGoToStart.setBackground(getResources().getDrawable(
				// R.drawable.focused));
				gVolumeIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gVolumeIncrease.requestFocus();
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_vol_inc), null, true);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 4) + (gChunkList
					.size() + 2)) : ((2 + 4) + gContestAdapter.getCount()))) {
				gFontDecrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gFontDecrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_dec), null, true);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 5) + (gChunkList
					.size() + 2)) : ((2 + 5) + gContestAdapter.getCount()))) {
				gFontIncrease.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gFontIncrease.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_font_inc), null, true);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 6) + (gChunkList
					.size() + 2)) : ((2 + 6) + gContestAdapter.getCount()))) {
				gGoToSummary.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gGoToSummary.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.btn_sumry), null, true);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 7) + (gChunkList
					.size() + 2)) : ((2 + 7) + gContestAdapter.getCount()))) {
				gBtmView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_bottom)
							+ Constants.DOT_SPACE + Constants.DOT_SPACE
							+ getString(R.string.scrn_bottom_press_again),
							null, true);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? ((2 + 8) + (gChunkList
					.size() + 2)) : ((2 + 8) + gContestAdapter.getCount()))) {
				gTopView.setBackgroundColor(getResources().getColor(
						android.R.color.holo_orange_dark));
				// gTopView.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.scrn_top), null, true);
				}
			} else if (focusPosition == ((gContestAdapter == null) ? (gMinFocusableItem + (gChunkList
					.size() + 2)) : (2 + 9) + gContestAdapter.getCount())) {
				if (gNavigateLeft.getVisibility() == View.VISIBLE) {
					gNavigateLeft.setBackground(getResources().getDrawable(
							R.drawable.focused));
					// gNavigateLeft.requestFocus();

					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.previous_ballot), null,
								true);
					}
				} else {
					gNavigateRight.setBackground(getResources().getDrawable(
							R.drawable.focused));
					// gNavigateRight.requestFocus();

					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.next_ballot), null, true);
					}
				}
			} else if (focusPosition == ((gContestAdapter == null) ? (gMinFocusableItem + (gChunkList
					.size() + 2)) : ((2 + 10) + gContestAdapter.getCount()))) {
				gNavigateRight.setBackground(getResources().getDrawable(
						R.drawable.focused));
				// gNavigateRight.requestFocus();

				if (HeadsetListener.isHeadsetConnected) {
					speakWord(getString(R.string.next_ballot), null, true);
				}
			}
			break;
		}
	}

	private void readBallotInfo(String initialString) {
		String tts = initialString;
		Contest contest = gContestList.get(gBallotPosition);

		if (gContestAdapter != null) {
			if (gContestAdapter.gSelectedCandidateList.size() > 0) {
				// params = null;
				tts = tts + Constants.DOT_SPACE + getString(R.string.voted_for)
						+ Constants.SPACE;

				/**
				 * add the selected candidate name for tts
				 */
				boolean addAnd = false;
				for (int i = 0; i < gContestAdapter.gCandidateList.size(); i++) {
					Candidate candidate = gContestAdapter.gCandidateList.get(i);
					if (candidate.candidateCheck) {
						if (addAnd) {
							tts = tts + Constants.COMMA_SPACE
									+ getString(R.string.and)
									+ Constants.COMMA_SPACE;
						}

						if (candidate.isWriteIn) {
							tts = tts + candidate.candidateName
									+ Constants.SPACE
									+ getString(R.string.candidate)
									+ Constants.COMMA_SPACE
									+ candidate.candidateParty
									+ Constants.SPACE;
						} else {
							tts = tts + candidate.candidateName
									+ Constants.COMMA_SPACE;
						}
						addAnd = true;
					}
				}

				tts = tts + Constants.DOT_SPACE;
				/**
				 * add the remaining candidate name for tts
				 */
				if (contest.vote_per_candidate
						- gContestAdapter.gSelectedCandidateList.size() == 0) {
					tts = tts + getString(R.string.u_hav) + Constants.SPACE
							+ getString(R.string.zero) + Constants.SPACE
							+ getString(R.string.more_choice);
				} else if (contest.vote_per_candidate
						- gContestAdapter.gSelectedCandidateList.size() == 1) {
					tts = tts + getString(R.string.u_can_vote)
							+ Constants.SPACE + getString(R.string.one)
							+ Constants.SPACE + getString(R.string.more)
							+ Constants.SPACE + getString(R.string.candidate)
							+ Constants.SPACE + getString(R.string.for_office);
				} else if (contest.vote_per_candidate
						- gContestAdapter.gSelectedCandidateList.size() > 1) {
					tts = tts + getString(R.string.u_can_vote)
							+ Constants.SPACE + getString(R.string.more)
							+ Constants.SPACE + getString(R.string.candidates)
							+ Constants.SPACE + getString(R.string.for_office);
				}
			}
		} else if (gChunkList != null) {
			if (gReferendumYes.isChecked()) {
				tts = tts
						+ Constants.DOT_SPACE
						// + contest.contest_referendum_title
						// + Constants.DOT_SPACE
						+ getString(R.string.referendum_accepted)
						+ Constants.DOT_SPACE;
			} else if (gReferendumNo.isChecked()) {
				tts = tts
						+ Constants.DOT_SPACE
						// + contest.contest_referendum_title
						// + Constants.DOT_SPACE
						+ getString(R.string.referendum_not_accepted)
						+ Constants.DOT_SPACE;
			} else {
				// tts = tts + contest.contest_referendum_instruction
				// + Constants.DOT_SPACE;
			}
		}

		speakWord(tts, null, false);
		isReadBallotDetail = false;
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
				isKeyboardKeyPress = true;
				isContestExplored = true;

				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				
				if (event.isShiftPressed()) {
					if (gChunkList != null
							&& gTTSProgressHelper.isChunkReadComplete
							&& gFocusPosition > 2
							&& gFocusPosition < 2 + gChunkList.size() + 1) {
						/**
						 * repeat the chunk
						 */
					} else {
						gFocusPosition--;
					}

					if (gFocusPosition <= 0) {
						gFocusPosition = ((gContestAdapter == null) ? gMinFocusableItem
								+ gChunkList.size() + 2
								: gMinFocusableItem + gContestAdapter.getCount());
					} else if (gContestAdapter == null) {
						bufferNextChunk = false;
						if ((gFocusPosition >= 2 && gFocusPosition <= 2 + gChunkList
								.size())) {
							/**
							 * stop the hybrid read and cancel the hybrid mode
							 */
							toggleHybridChunkReadAndHybridModeDependency();
						}
					}
					navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				} else {
					gFocusPosition++;
					if (gFocusPosition >= ((gContestAdapter == null) ? gMinFocusableItem
							+ 1 + gChunkList.size() + 2
							: gMinFocusableItem + 1
									+ gContestAdapter.getCount())) {
						gFocusPosition = 1;
					} else if (gContestAdapter == null
							&& (gFocusPosition > 2 && gFocusPosition <= 2 + gChunkList
									.size())) {
						/**
						 * start the hydrid read if focus position (2, 2 +
						 * gChunkList.size()], can stop the hydrid read and
						 * cancel the hybrid mode if button press between (2, 2
						 * + gChunkList.size()]
						 */
						toggleHybridChunkReadAndHybridModeDependency();
					}

					bufferNextChunk = true;
					navigateToOtherItem(gFocusPosition,
							Constants.REACH_NEW_ITEM);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				isKeyboardKeyPress = true;
			case KeyEvent.KEYCODE_BUTTON_1:
				isContestExplored = true;

				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				if (gChunkList != null
						&& gTTSProgressHelper.isChunkReadComplete
						&& gFocusPosition > 2
						&& gFocusPosition < 2 + gChunkList.size() + 1) {
					/**
					 * repeat the chunk
					 */
				} else {
					gFocusPosition--;
				}

				if (gFocusPosition <= 0) {
					gFocusPosition = ((gContestAdapter == null) ? gMinFocusableItem
							+ gChunkList.size() + 2
							: gMinFocusableItem + gContestAdapter.getCount());
				} else if (gContestAdapter == null) {
					bufferNextChunk = false;
					if ((gFocusPosition >= 2 && gFocusPosition <= 2 + gChunkList
							.size())) {
						/**
						 * stop the hybrid read and cancel the hybrid mode
						 */
						toggleHybridChunkReadAndHybridModeDependency();
					}
				}
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				isKeyboardKeyPress = true;
			case KeyEvent.KEYCODE_BUTTON_2:
				isContestExplored = true;

				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);

				gFocusPosition++;
				if (gFocusPosition >= ((gContestAdapter == null) ? gMinFocusableItem
						+ 1 + gChunkList.size() + 2
						: gMinFocusableItem + 1 + gContestAdapter.getCount())) {
					gFocusPosition = 1;
				} else if (gContestAdapter == null
						&& (gFocusPosition > 2 && gFocusPosition <= 2 + gChunkList
								.size())) {
					/**
					 * start the hydrid read if focus position (2, 2 +
					 * gChunkList.size()], can stop the hydrid read and cancel
					 * the hybrid mode if button press between (2, 2 +
					 * gChunkList.size()]
					 */
					toggleHybridChunkReadAndHybridModeDependency();
				}

				bufferNextChunk = true;
				navigateToOtherItem(gFocusPosition, Constants.REACH_NEW_ITEM);
				break;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_BUTTON_3:
				isContestExplored = true;

				if (gContestAdapter == null && gFocusPosition > 2
						&& gFocusPosition < (3 + gChunkList.size())) {
					/**
					 * if chunk is re-read in hybrid mode cancel the hybrid mode
					 */
					toggleHybridChunkReadAndHybridModeDependency();
				}
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
			case KeyEvent.KEYCODE_MOVE_END:
				break;
			}
		}

		isButtonPressed = true;
		return true;
	}

	/**
	 * hybrid mode is canceled only when it is interepted
	 */
	private void toggleHybridChunkReadAndHybridModeDependency() {
		if (isHybridModeOn) {
			gTTSProgressHelper.isHybridChunkRead = gTTSProgressHelper.isHybridChunkRead ? false
					: true;
			if (!gTTSProgressHelper.isHybridChunkRead) {
				isHybridModeOn = false;
				stopChunkReading();
			}
		}
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

		switch (keyPressed) {
		case KeyEvent.KEYCODE_F1:
		case KeyEvent.KEYCODE_APP_SWITCH:
			success = true;
			gHelp.performClick();
			break;
		case KeyEvent.KEYCODE_TAB:
		case KeyEvent.KEYCODE_DPAD_UP:
			isKeyboardKeyPress = false;
		case KeyEvent.KEYCODE_BUTTON_1:
			success = true;
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			isKeyboardKeyPress = false;
		case KeyEvent.KEYCODE_BUTTON_2:
			success = true;
			break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_BUTTON_3:
			selectCurrentFocusItem(gFocusPosition,
					Constants.JUMP_FROM_CURRENT_ITEM);
			success = true;
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_PAGE_UP:
		case KeyEvent.KEYCODE_BUTTON_4:
			success = true;
			if (gNavigateLeft.getVisibility() == View.VISIBLE) {
				gNavigateLeft.performClick();
			}
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
		case KeyEvent.KEYCODE_MOVE_END:
			success = true;
			gGoToSummary.performClick();
			break;
		}
		isButtonPressed = false;
		return success;
	}

	// private ArrayList<Candidate> getCandidateList(JSONArray candidateArray) {
	// ArrayList<Candidate> candidateList = new ArrayList<Candidate>();
	//
	// Candidate candidate = null;
	// JSONObject candidateObject = null;
	//
	// for (int j = 0; j < candidateArray.length(); j++) {
	// try {
	// candidateObject = candidateArray.getJSONObject(j);
	// candidate = new Candidate();
	//
	// if (candidateObject.has(Constants.NAME)) {
	// candidate.candidateName = candidateObject
	// .getString(Constants.NAME);
	// }
	//
	// if (candidateObject.has(Constants.PARTY)) {
	// candidate.candidateParty = candidateObject
	// .getString(Constants.PARTY);
	// }
	//
	// if (candidateObject.has(Constants.WRITEIN)) {
	// candidate.isWriteIn = Boolean.valueOf(candidateObject
	// .getString(Constants.WRITEIN));
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	//
	// candidateList.add(candidate);
	// }
	// return candidateList;
	// }

	private OnClickListener gOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
			switch (v.getId()) {
			case R.id.tv_candidate_name:
				if (((LinearLayout) v.getParent().getParent()).getId() == gYesCheckBoxView
						.getId()) {
					if (Constants.SETTING_TOUCH_PRESENT) {
						Utils.showCustomDialog(ContestActivity.this,
								gReferendumYesTitle.getText().toString(),
								gReferendumYesTitle.getTextSize());
					}
				} else if (((LinearLayout) v.getParent().getParent()).getId() == gNoCheckBoxView
						.getId()) {
					if (Constants.SETTING_TOUCH_PRESENT) {
						Utils.showCustomDialog(ContestActivity.this,
								gReferendumNoTitle.getText().toString(),
								gReferendumNoTitle.getTextSize());
					}
				}
				break;
			case R.id.ballot_page:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(ContestActivity.this, gBallotPage
							.getText().toString(), gBallotPage.getTextSize());
				}
				break;
			case R.id.second_row:
				if (Constants.SETTING_TOUCH_PRESENT) {
					Utils.showCustomDialog(ContestActivity.this,
							gBallotInstruction.getText().toString(),
							gBallotInstruction.getTextSize());
				}
				break;
			case R.id.btn_scroll_down:
				if (gContestAdapter == null) {
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gFocusPosition = 0;
					scrollReferendum(true);
				} else {
					navigateToOtherItem(gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gFocusPosition = 0;
					scrollList(true);
				}
				break;
			case R.id.btn_scroll_up:
				if (gContestAdapter == null) {
					scrollReferendum(false);
				} else {
					scrollList(false);
				}
				break;
			case R.id.btn_left:
				navigateLeft();
				break;
			case R.id.btn_right:
				navigateRight();
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
				launchHelp();
				break;
			case R.id.btn_volume_decrease:
				setVolume(Constants.DECREASE);
				break;
			case R.id.btn_volume_increase:
				setVolume(Constants.INCREASE);
				break;
			default:
				break;
			}
		}
	};

	protected void navigateLeft() {
		Contest contest = gContestList.get(gBallotPosition);

		// if (gContestAdapter != null) {
		// refreshCandidateList();
		// }

		saveCurrentBallot(contest, gBallotPosition);

		if (isCameFromSummary) {
			/**
			 * if came from summary go back to summary page
			 */
			loadSummary();
		} else {
			/**
			 * if moving back no need to check for exploring the election
			 */
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
					UtterenceProgressHelper.UID_PREVIOUS_BALLOT);
			speakWord(getString(R.string.previous), params, false);
		}
	}

	protected void navigateRight() {
		Contest contest = gContestList.get(gBallotPosition);
		saveCurrentBallot(contest, gBallotPosition);

		if (isCameFromSummary) {
			loadSummary();
		} else if (!isContestExplored
				|| (contest.isReferendum && (gReferendumYes.isChecked() || gReferendumNo
						.isChecked()))
				|| (gContestAdapter != null && gContestAdapter.gSelectedCandidateList
						.size() == gContestAdapter.gMaxCandidateSelect)) {
			Log.d(TAG,
					"contest not explored or not under voted, load another ballot");

			HashMap<String, String> params = new HashMap<String, String>();
			params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
					UtterenceProgressHelper.UID_NEXT_BALLOT);
			speakWord(getString(R.string.next), params, false);
		} else if (gContestAdapter == null
				|| gContestAdapter.gSelectedCandidateList.size() < gContestAdapter.gMaxCandidateSelect) {
			Log.d("tushar", "contest explored && under voted");
			List<String> message = new ArrayList<String>();
			String tts = "";

			if (gContestAdapter == null) {

				/**
				 * contest is referendum
				 */
				tts = tts + getString(R.string.not_voted_referendum)
						+ Constants.SPACE
						+ getString(R.string.want_vote_referendum);
			} else {

				/**
				 * contest is ballot
				 */
				if (gContestAdapter.gSelectedCandidateList.size() == 0) {
					tts = tts + getString(R.string.not_voted_ofc)
							+ Constants.SPACE
							+ getString(R.string.want_vote_ofc);
				} else if (gContestAdapter.gMaxCandidateSelect
						- gContestAdapter.gSelectedCandidateList.size() == 1) {
					tts = tts + getString(R.string.alwd_to_choose)
							+ Constants.SPACE + getString(R.string.one)
							+ Constants.SPACE + getString(R.string.more)
							+ Constants.SPACE + getString(R.string.candidate)
							+ Constants.SPACE + getString(R.string.for_office)
							+ Constants.SPACE
							+ getString(R.string.slct_an_additional)
							+ Constants.SPACE + getString(R.string.candidate)
							+ Constants.COMMA_SPACE
							+ getString(R.string.press_page_back_btn);

				} else {
					tts = tts
							+ getString(R.string.alwd_to_choose)
							+ Constants.SPACE
							+ (gContestAdapter.gMaxCandidateSelect - gContestAdapter.gSelectedCandidateList
									.size()) + Constants.SPACE
							+ getString(R.string.more) + Constants.SPACE
							+ getString(R.string.candidates) + Constants.SPACE
							+ getString(R.string.for_office) + Constants.SPACE
							+ getString(R.string.slct_additional)
							+ Constants.SPACE + getString(R.string.candidates)
							+ Constants.COMMA_SPACE
							+ getString(R.string.press_page_back_btn);
				}
			}

			tts = tts + Constants.SPACE
					+ getString(R.string.prcd_nxt_ballot_page);
			message.add(tts);

			resetBallotVariable(true);
			loadAlertPage(Constants.TYPE_UNDER_VOTE,
					gContext.getString(R.string.undr_vote_page), message, null);
		}
	}

	private boolean loadFromPreferences(ContestAdapter contestAdapter,
			int ballotPosition) {
		boolean result = false;
		SharedPreferences preferences = getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		String ballotInfo = preferences.getString(
				String.valueOf(ballotPosition), null);

		if (ballotInfo != null) {
			try {
				JSONObject savedBallotInfo = new JSONObject(ballotInfo);

				if (contestAdapter == null) {
					if (savedBallotInfo
							.has(Constants.CONTEST_REFERENDUM_RESPONSE)) {
						int referendumValue = savedBallotInfo
								.getInt(Constants.CONTEST_REFERENDUM_RESPONSE);
						if (referendumValue == Constants.REFERENDUM_ACCEPTED) {
							gReferendumYes.setChecked(true);
						} else if (referendumValue == Constants.REFERENDUM_DISCARDED) {
							gReferendumNo.setChecked(true);
						}
					}
				} else {
					JSONArray savedCandidateList = null;
					if (savedBallotInfo.has(Constants.CONTEST_ROW)) {
						savedCandidateList = savedBallotInfo
								.getJSONArray(Constants.CONTEST_ROW);
					}

					if (savedCandidateList != null) {
						for (int i = 0; i < savedCandidateList.length(); i++) {
							Log.d("tushar",
									"saved candidate list values = "
											+ savedCandidateList.getJSONObject(
													i).getBoolean(
													Constants.CANDIDATE_CHECK));

							String candidateName = savedCandidateList
									.getJSONObject(i).getString(
											Constants.CANDIDATE_NAME);
							(contestAdapter.getItem(i)).candidateName = candidateName;
							boolean status = savedCandidateList
									.getJSONObject(i).getBoolean(
											Constants.CANDIDATE_CHECK);
							(contestAdapter.getItem(i)).candidateCheck = status;
							String candidateParty = savedCandidateList
									.getJSONObject(i).getString(
											Constants.CANDIDATE_PARTY);
							(contestAdapter.getItem(i)).candidateParty = candidateParty;
							boolean isWriteIn = savedCandidateList
									.getJSONObject(i).getBoolean(
											Constants.CANDIDATE_WRITEIN);
							(contestAdapter.getItem(i)).isWriteIn = isWriteIn;

							// contest adapter is already loaded
							if (status) {
								gContestAdapter.gSelectedCandidateList
										.add(contestAdapter.getItem(i));
							}

							result = true;
							refreshCandidateList();
						}
						Log.d("tushar", "no. of selected candidate = "
								+ gContestAdapter.gSelectedCandidateList.size());
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	protected void launchHelp() {
		Contest contest = gContestList.get(gBallotPosition);
		saveCurrentBallot(contest, gBallotPosition);
		isLeavingCurrentScreen = true;
		resetBallotVariable(true);
		Intent intent = new Intent(this, HelpScreen.class);
		startActivity(intent);
	}

	protected void loadSummary() {
		saveRemainingBallot(gBallotPosition, gContestList);
	}

	private void loadAlertPage(int which_page, String page_title,
			List<String> message, String candidateName) {
		resetBallotVariable(true);
		Intent intent = new Intent(this, AlertActivity.class);
		intent.putExtra(Constants.ALERTPAGE_TYPE, which_page);
		intent.putExtra(Constants.ALERTPAGE_TITLE, page_title);
		intent.putStringArrayListExtra(Constants.ALERTPAGE_CONTENT,
				(ArrayList<String>) message);
		if (candidateName != null) {
			intent.putExtra(Constants.CANDIDATE_NAME, candidateName);
		}
		startActivityForResult(intent, Constants.REQUEST_CODE_ALERTACTIVITY);
	}

	private OnTouchListener gOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			boolean success = false;
			Utils.downX = (int) event.getRawX();
			Utils.downY = (int) event.getRawY();
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				success = true;
				isHeadingTTSInterupted = true;
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				String tts = null;
				gFocusPosition = 0;

				if (gTTSProgressHelper != null
						&& gTTSProgressHelper.isHybridChunkRead) {
					stopChunkReading();
				}

				switch (v.getId()) {
				case R.id.lv_candidates:
				case R.id.ballot_referendum_container:
					speakWord("", null, false);
					break;
				case R.id.tv_candidate_name:
					/**
					 * handling the touch event for referendum candidate touch
					 * event is handled in the contest adapter
					 */
					isContestExplored = true;

					v.setBackgroundResource(R.drawable.focused);

					if (((LinearLayout) v.getParent().getParent()).getId() == gYesCheckBoxView
							.getId()) {
						if (gReferendumYes.isChecked()) {
							tts = getString(R.string.accept)
									+ Constants.COMMA_SPACE
									+ getString(R.string.is) + Constants.SPACE
									+ getString(R.string.marked);
						} else if (gReferendumNo.isChecked()) {
							tts = getString(R.string.accept)
									+ Constants.COMMA_SPACE
									+ getString(R.string.is_not_marked)
									+ Constants.SPACE
									+ String.format(
											getString(R.string.erase_other_mark_to_),
											getString(R.string.accept));
						} else {
							tts = getString(R.string.accept)
									+ Constants.COMMA_SPACE
									+ getString(R.string.is_not_marked)
									+ Constants.SPACE
									+ getString(R.string.touch_circle_nxt)
									+ Constants.SPACE
									+ getString(R.string.accept)
									+ Constants.COMMA_SPACE
									+ getString(R.string.mark_this)
									+ Constants.SPACE
									+ getString(R.string.referendum)
									+ Constants.SPACE + getString(R.string.as)
									+ Constants.SPACE
									+ getString(R.string.accepted);
						}

						// if (Constants.SETTING_TOUCH_PRESENT) {
						// Utils.showCustomDialog(ContestActivity.this,
						// gReferendumYesTitle.getText().toString(),
						// gReferendumYesTitle.getTextSize());
						// }
					} else if (((LinearLayout) v.getParent().getParent())
							.getId() == gNoCheckBoxView.getId()) {
						if (gReferendumNo.isChecked()) {
							tts = getString(R.string.reject)
									+ Constants.COMMA_SPACE
									+ getString(R.string.is) + Constants.SPACE
									+ getString(R.string.marked);
						} else if (gReferendumYes.isChecked()) {
							tts = getString(R.string.reject)
									+ Constants.COMMA_SPACE
									+ getString(R.string.is_not_marked)
									+ Constants.SPACE
									+ String.format(
											getString(R.string.erase_other_mark_to_),
											getString(R.string.reject));
						} else {
							tts = getString(R.string.reject)
									+ Constants.COMMA_SPACE
									+ getString(R.string.is_not_marked)
									+ Constants.SPACE
									+ getString(R.string.touch_circle_nxt)
									+ Constants.SPACE
									+ getString(R.string.reject)
									+ Constants.COMMA_SPACE
									+ getString(R.string.mark_this)
									+ Constants.SPACE
									+ getString(R.string.referendum)
									+ Constants.SPACE + getString(R.string.as)
									+ Constants.SPACE
									+ getString(R.string.rejected);
						}
						// if (Constants.SETTING_TOUCH_PRESENT) {
						// Utils.showCustomDialog(ContestActivity.this,
						// gReferendumNoTitle.getText().toString(),
						// gReferendumNoTitle.getTextSize());
						// }
					}

					if (HeadsetListener.isHeadsetConnected) {
						speakWord(tts, null, true);
					}
					Log.d(TAG, "tts of the check box = " + tts);
					break;
				case R.id.ballot_page:
					isContestExplored = true;

					v.setBackgroundResource(R.drawable.focused);
					// if (Constants.SETTING_TOUCH_PRESENT) {
					// Utils.showCustomDialog(ContestActivity.this,
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
					// Utils.showCustomDialog(ContestActivity.this,
					// gBallotInstruction.getText().toString(),
					// gBallotInstruction.getTextSize());
					// }
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(gBallotInstruction.getText().toString(),
								null, true);
					}
					break;
				case R.id.tv_referendum_subtitle:
					// gdt.onTouchEvent(event);
					// isContestExplored = true;
					//
					// gTTSProgressHelper.isHybridChunkRead =
					// gTTSProgressHelper.isHybridChunkRead ? false
					// : true;
					// if (gTTSProgressHelper.isHybridChunkRead) {
					// // set the flag to read the hybrid mode
					// gFocusPosition = 3;
					// gTTSProgressHelper.isHybridChunkRead = true;
					// isReferendumReadForward = true;
					//
					// if (gFocusPosition < 3 + gChunkList.size()) {
					// setReferendumFocus(gChunkList
					// .get(gFocusPosition - 3));
					// // gReferendumSubtitle.requestFocus();
					//
					// // buffer nxt chunk to chunk list if chunklist is
					// // not full and reading is forward.
					// if (!isChunkListFull && isReferendumReadForward) {
					// gLastIndexOfChunk = chunkAlgorithm(gLastIndexOfChunk);
					// }
					// Log.d("tushar", " focusposition in referendum = "
					// + gFocusPosition);
					// }
					// } else if (!gTTSProgressHelper.isHybridChunkRead) {
					// speakWord("", null); // stop the tts forcefully
					// }
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
					if (HeadsetListener.isHeadsetConnected) {
						speakWord(getString(R.string.btn_sumry), null, true);
					}
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
				success = false;
				switch (v.getId()) {
				case R.id.tv_candidate_name: // for referendum checkbox title
					v.setBackground(null);
					break;
				case R.id.ballot_page:
					v.setBackground(null);
					break;
				case R.id.second_row:
					v.setBackground(null);
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
				case R.id.btn_goto_end:
					v.setBackgroundColor(getResources().getColor(
							android.R.color.black));
					// v.performClick();
					break;
				case R.id.btn_help:
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
				}
			}
			return false;
		}
	};

	protected void saveBallot(int ballotPosition) {
		Contest contest = gContestList.get(ballotPosition);
		saveCurrentBallot(contest, ballotPosition);
	}

	protected void saveCurrentBallot(final Contest contest,
			final int ballotPosition) {
		JSONObject ballotObject = null;

		if (contest.isReferendum) {
			ballotObject = gnratReferendumItemToBeSaved(ballotPosition);
		} else {
			ballotObject = gnratBallotItemToBeSaved(ballotPosition);
		}

		SharedPreferences preferences = getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		// getListItemRow((Map<String, String>) gPreferences.getAll());
		editor.putString(String.valueOf(ballotPosition),
				ballotObject.toString());
		editor.apply();
		editor.commit();
		// getListItemRow((Map<String, String>) gPreferences.getAll());
		Log.d("tushar", "ballot of position = " + ballotPosition + " saved");
	}

	private JSONObject gnratReferendumItemToBeSaved(int ballotPosition) {
		JSONObject balletObject = new JSONObject();

		try {
			balletObject.put(Constants.CONTEST_POSITION, ballotPosition);
			balletObject.put(Constants.CONTEST_OFFICE,
					gContestList.get(ballotPosition).contest_office);
			balletObject.put(Constants.CONTEST_VOTE_PER_CANDIDATE,
					gContestList.get(ballotPosition).vote_per_candidate);
			balletObject.put(Constants.CONTEST_IS_REFERENDUM,
					gContestList.get(ballotPosition).isReferendum);
			balletObject.put(Constants.CONTEST_REFERENDUM_TITLE,
					gContestList.get(ballotPosition).contest_referendum_title);
			balletObject.put(Constants.CONTEST_ELECTORATE_SPECS,
					gContestList.get(ballotPosition).contest_electorate_specs);
			balletObject.put(Constants.CONTEST_REFERENDUM_SUBS,
					gContestList.get(ballotPosition).contest_referendum_subs);

			if (gReferendumYes.isChecked()) {
				balletObject.put(Constants.CONTEST_REFERENDUM_RESPONSE,
						Constants.REFERENDUM_ACCEPTED);
				balletObject.put(Constants.CONTEST_REFERENDUM_VALUE,
						gReferendumYesTitle.getText());
			} else if (gReferendumNo.isChecked()) {
				balletObject.put(Constants.CONTEST_REFERENDUM_RESPONSE,
						Constants.REFERENDUM_DISCARDED);
				balletObject.put(Constants.CONTEST_REFERENDUM_VALUE,
						gReferendumNoTitle.getText());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// try {
		// balletObject.put(Constants.CONTEST_IS_REFERENDUM,
		// gContestList.get(ballotPosition).isReferendum);
		// balletObject.put(Constants.CONTEST_POSITION, ballotPosition);
		// balletObject.put(Constants.CONTEST_VOTE_PER_CANDIDATE,
		// gContestList.get(ballotPosition).vote_per_candidate);
		// balletObject.put(Constants.CONTEST_REFERENDUM_TITLE,
		// gContestList.get(ballotPosition).contest_referendum_title);
		// balletObject.put(Constants.CONTEST_ELECTORATE_SPECS,
		// gContestList.get(ballotPosition).contest_electorate_specs);
		//
		// if (gReferendumYes.isChecked()) {
		// balletObject.put(Constants.CONTEST_REFERENDUM_RESPONSE,
		// Constants.REFERENDUM_ACCEPTED);
		// } else if (gReferendumNo.isChecked()) {
		// balletObject.put(Constants.CONTEST_REFERENDUM_RESPONSE,
		// Constants.REFERENDUM_DISCARDED);
		// }
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		return balletObject;
	}

	private JSONObject gnratBallotItemToBeSaved(int ballotPosition) {
		JSONObject balletObject = new JSONObject();
		JSONArray jsonCandidateList = new JSONArray();
		JSONObject candidateRow = null;
		List<Candidate> candidateList = null;

		candidateList = gContestList.get(ballotPosition).candidateList;
		Candidate row = null;

		try {
			balletObject.put(Constants.CONTEST_POSITION, ballotPosition);
			balletObject.put(Constants.CONTEST_OFFICE,
					gContestList.get(ballotPosition).contest_office);
			balletObject.put(Constants.CONTEST_VOTE_PER_CANDIDATE,
					gContestList.get(ballotPosition).vote_per_candidate);
			balletObject.put(Constants.CONTEST_IS_REFERENDUM,
					gContestList.get(ballotPosition).isReferendum);
			balletObject.put(Constants.CONTEST_REFERENDUM_TITLE,
					gContestList.get(ballotPosition).contest_referendum_title);
			balletObject.put(Constants.CONTEST_ELECTORATE_SPECS,
					gContestList.get(ballotPosition).contest_electorate_specs);
			balletObject.put(Constants.CONTEST_REFERENDUM_SUBS,
					gContestList.get(ballotPosition).contest_referendum_subs);
			balletObject.accumulate(Constants.CONTEST_ROW, jsonCandidateList);

			for (int i = 0; i < candidateList.size(); i++) {
				row = candidateList.get(i);
				candidateRow = new JSONObject();

				candidateRow.put(Constants.CANDIDATE_WRITEIN, row.isWriteIn);
				candidateRow.put(Constants.CANDIDATE_CHECK, row.candidateCheck);
				candidateRow.put(Constants.CANDIDATE_NAME, row.candidateName);
				candidateRow.put(Constants.CANDIDATE_PARTY, row.candidateParty);
				jsonCandidateList.put(candidateRow);

				Log.d("tushar", "candidate saved = " + row.candidateName);
				Log.d("tushar", "candidate writein = " + row.isWriteIn);
			}

			Log.d("tushar",
					"number of candidates save for "
							+ balletObject.getString(Constants.CONTEST_OFFICE)
							+ " is " + jsonCandidateList.length());
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// try {
		// balletObject.put(Constants.CONTEST_IS_REFERENDUM,
		// gContestList.get(ballotPosition).isReferendum);
		// balletObject.put(Constants.CONTEST_POSITION, ballotPosition);
		// balletObject.put(Constants.CONTEST_OFFICE,
		// gContestList.get(ballotPosition).contest_office);
		// balletObject.put(Constants.CONTEST_VOTE_PER_CANDIDATE,
		// gContestList.get(ballotPosition).vote_per_candidate);
		// balletObject.accumulate(Constants.CONTEST_ROW, jsonCandidateList);
		//
		// // save the checked candidate only
		// for (int i = 0; i < candidateList.size(); i++) {
		// row = candidateList.get(i);
		//
		// if(row.candidateCheck){
		// candidateRow = new JSONObject();
		//
		// candidateRow.put(Constants.CANDIDATE_WRITEIN, row.isWriteIn);
		// candidateRow.put(Constants.CANDIDATE_CHECK, row.candidateCheck);
		// candidateRow.put(Constants.CANDIDATE_NAME, row.candidateName);
		// candidateRow.put(Constants.CANDIDATE_PARTY, row.candidateParty);
		// jsonCandidateList.put(candidateRow);
		//
		// Log.d("tushar", "candidate saved = " + row.candidateName);
		// }
		// }
		//
		// // balletObject.accumulate(Constants.CONTEST_ROW, jsonCandidateList);
		//
		// Log.d("tushar","number of candidates save for "
		// + balletObject.getString(Constants.CONTEST_OFFICE)
		// + " is " + jsonCandidateList.length());
		// } catch (JSONException e) {
		// e.printStackTrace();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return balletObject;
	}

	private void getListItemRow(Map<String, String> ballotInfo) {
		for (int j = 0; j < ballotInfo.size(); j++) {
			String stringJSON = ballotInfo.get(String.valueOf(j));
			// Log.d("tushar", "candidate list = " + stringJSON);
			writeToFile(stringJSON);
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
			gReferendumText.scrollTo(0, 0);
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

		// calc new chunk according to new font size
		if (gContestAdapter == null) {
			startHybridChunkAlgo();
		}

		if (gContestAdapter == null) {
			gReferendumText
					.setText(gContestList.get(gBallotPosition).contest_referendum_subs);
		}

		if (gContestAdapter != null) {
			refreshCandidateList();
		} else if (gReferendumText != null) {
			gReferendumText.setTextSize(Constants.SETTING_FONT_SIZE);
		}

		updateScrollButton();
		// resizeImage(controlFlag);
		gBallotPage.setTextSize(Constants.SETTING_FONT_SIZE);
		gBallotInstruction.setTextSize(Constants.SETTING_FONT_SIZE);
		gReferendumYesTitle.setTextSize(Constants.SETTING_FONT_SIZE);
		gReferendumNoTitle.setTextSize(Constants.SETTING_FONT_SIZE);
		gScrollDownBtn.setTextSize(Constants.SETTING_FONT_SIZE);
		gScrollUpBtn.setTextSize(Constants.SETTING_FONT_SIZE);
	}

	private void updateScrollButton() {
		if (gContestAdapter != null) {
			gCandidateListView.post(new Runnable() {
				@Override
				public void run() {
					boolean isListScrollable = isCandidateListScrollable();
					if (isListScrollable) {
						gScrollDownBtn.setVisibility(View.VISIBLE);
						gScrollUpBtn.setVisibility(View.VISIBLE);
					} else {
						gScrollDownBtn.setVisibility(View.GONE);
						gScrollUpBtn.setVisibility(View.GONE);
					}
				}
			});
		} else {
			/**
			 * update according to referendum
			 */
			gReferendumText.post(new Runnable() {
				@Override
				public void run() {
					if (gReferendumText.getHeight() >= (gReferendumText
							.getLineCount() * gReferendumText.getLineHeight())) {
						/**
						 * text inside text view
						 */
						gScrollDownBtn.setVisibility(View.GONE);
						gScrollUpBtn.setVisibility(View.GONE);
					} else {
						gScrollDownBtn.setVisibility(View.VISIBLE);
						gScrollUpBtn.setVisibility(View.VISIBLE);
					}
				}
			});
		}
	}

	private void startHybridChunkAlgo() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					gChunkList.clear();
					isChunkListFull = false;
					// if (gChunkList.size() == 0) {
					// gLastIndexOfChunk = chunkAlgorithm(0);
					// } else {
					// gLastIndexOfChunk = chunkAlgorithm(gChunkList.get(
					// gChunkList.size() - 1).getValue());
					// }

					gLastIndexOfChunk = chunkAlgorithm(0);
					Log.d(TAG, "gLastIndexOfChunk = " + gLastIndexOfChunk
							+ ", going into the loop");

					while (gLastIndexOfChunk != gReferendumText.getText()
							.toString().length()) {
						Log.d(TAG, "gLastIndexOfChunk = " + gLastIndexOfChunk);
						gLastIndexOfChunk = chunkAlgorithm(gLastIndexOfChunk);
					}
					// isThreadStop = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
					// isThreadStop = true;
				} catch (Exception e) {

				}
			}
		}).start();
	}

	public int chunkAlgorithm(int startIndexOfChunk) {
		String textToHighlight = null;
		int endIndexOfChunk = -1;
		int lastCharacterOffset = gReferendumText.getText().toString().length();
		int numOfLineForMaxChar = gReferendumText.getLayout().getLineForOffset(
				Constants.MAX_CHARACTER_OFFSET);
		int numOfVisibleLine = (gReferendumText.getHeight()
				- gReferendumText.getTotalPaddingTop() - gReferendumText
					.getTotalPaddingBottom()) / gReferendumText.getLineHeight();

		Log.d(TAG, "number of line for max char = " + numOfLineForMaxChar);
		// if (numOfLineForMaxChar > numOfVisibleLine) {
		// boder the text that will fit the window
		// textToHighlight = gReferendumText
		// .getText()
		// .toString()
		// .substring(
		// startIndexOfChunk,
		// gReferendumText.getLayout().getLineEnd(
		// numOfVisibleLine - 1));
		// endIndexOfChunk = getChunkLastIndex(textToHighlight,
		// startIndexOfChunk);
		// } else
		if (lastCharacterOffset < startIndexOfChunk
				+ Constants.MAX_CHARACTER_OFFSET) {
			isChunkListFull = true;
			textToHighlight = gReferendumText.getText().toString()
					.substring(startIndexOfChunk, lastCharacterOffset);
			endIndexOfChunk = lastCharacterOffset;
		} else {
			textToHighlight = gReferendumText
					.getText()
					.toString()
					.substring(startIndexOfChunk,
							startIndexOfChunk + Constants.MAX_CHARACTER_OFFSET);

			endIndexOfChunk = getChunkLastIndex(textToHighlight,
					startIndexOfChunk);
		}
		gChunkList.add(new SimpleEntry<Integer, Integer>(startIndexOfChunk,
				endIndexOfChunk));

		return endIndexOfChunk;
	}

	public int getChunkLastIndex(String textToHighlight, int startIndexOfChunk) {
		int endIndexOfChunk = -1;
		if (textToHighlight != null) {
			endIndexOfChunk = checkBoundary(textToHighlight, startIndexOfChunk,
					Constants.CHECK_PARAGRAPH_BOUNDARY);

			if (endIndexOfChunk == -1) {
				endIndexOfChunk = checkBoundary(textToHighlight,
						startIndexOfChunk, Constants.CHECK_SENTENCE_BOUNDARY);
			}

			if (endIndexOfChunk == -1) {
				endIndexOfChunk = checkBoundary(textToHighlight,
						startIndexOfChunk, Constants.CHECK_CLAUSE_BOUNDARY);
			}

			if (endIndexOfChunk == -1) {
				endIndexOfChunk = checkBoundary(textToHighlight,
						startIndexOfChunk, Constants.CHECK_WORD_BOUNDARY);
			}
		}
		return endIndexOfChunk;
	}

	public void setReferendumFocus(Entry<Integer, Integer> entry) {
		// int height = gReferendumSubtitle.getLineHeight();
		int scrollY = gReferendumText.getScrollY();
		int firstVisibleLineNumber = gReferendumText.getLayout()
				.getLineForVertical(scrollY);
		int lastVisibleLineNumber = firstVisibleLineNumber
				+ (gReferendumText.getHeight()
						- gReferendumText.getTotalPaddingTop() - gReferendumText
							.getTotalPaddingBottom())
				/ gReferendumText.getLineHeight();

		int startline = gReferendumText.getLayout().getLineForOffset(
				entry.getKey());
		int lastline = gReferendumText.getLayout().getLineForOffset(
				entry.getValue());

		Log.d(TAG, "startline of index = " + startline);
		Log.d(TAG, "firstVisibleLineNumber = " + firstVisibleLineNumber);
		Log.d(TAG, "lastVisibleLineNumber = " + lastVisibleLineNumber);
		Log.d(TAG, "lastline of index = " + lastline);

		if (startline < firstVisibleLineNumber
				|| lastline > lastVisibleLineNumber) {
			Log.d(TAG, " line out of visiblity should scroll");
			int scrollAmount = gReferendumText.getLayout()
					.getLineTop(startline);
			// if(scrollAmount>0)
			gReferendumText.scrollTo(0, scrollAmount);
		}

		SpannableString s = new SpannableString(gReferendumText.getText());
		ForegroundColorSpan fcs = new ForegroundColorSpan(getResources()
				.getColor(android.R.color.holo_orange_dark));
		s.setSpan(fcs, entry.getKey(), entry.getValue(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		gReferendumText.setText(s);

		String chunk = s.subSequence(entry.getKey(), entry.getValue())
				.toString().toLowerCase();

		if (2 + gChunkList.size() == gFocusPosition) {
			stopChunkReading();
		}

		// if (gTTSProgressHelper.isHybridChunkRead) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
				UtterenceProgressHelper.UID_CONTEST_ACTIVITY_CHUNK_READ);
		speakWord(chunk, params, true);
		// } else {
		// speakWord(chunk, null, true);
		// }
	}

	private int checkBoundary(String textToHighlight, int startIndexOfCheck,
			int checkBoundary) {
		int indexOfBoundary = -1;
		StringBuilder sb = new StringBuilder();
		sb.append((char) 13);
		sb.append((char) 10);
		String secondChar[] = { " ", sb.toString() };

		switch (checkBoundary) {
		case Constants.CHECK_PARAGRAPH_BOUNDARY:
			sb.append((char) 10);
			sb.append((char) 13);
			String paraEnd = sb.toString();
			indexOfBoundary = textToHighlight.lastIndexOf(paraEnd);

			if (indexOfBoundary != -1) {
				indexOfBoundary += 4;
				// Log.d("tushar", "text boundary = " +
				// textToHighlight.substring(0, indexOfBoundary));
			}
			Log.d("tushar", "index of Boundary = " + indexOfBoundary + ", "
					+ paraEnd);

			break;
		case Constants.CHECK_SENTENCE_BOUNDARY:
			String firstChar[] = { ".", "?", "!" };

			break_loop: for (String compareChar1 : firstChar) {
				for (String compareChar2 : secondChar) {
					String compareString = compareChar1 + compareChar2;
					// Log.d("tushar", "compare string = " + compareString);
					indexOfBoundary = textToHighlight
							.lastIndexOf(compareString);
					if (indexOfBoundary != -1)
						break break_loop;
				}
			}

			if (indexOfBoundary != -1) {
				indexOfBoundary += 2;
				// Log.d("tushar", "text boundary = " +
				// textToHighlight.substring(0, indexOfBoundary));
			}
			// Log.d("tushar", "index of Boundary = " + indexOfBoundary );
			break;
		case Constants.CHECK_CLAUSE_BOUNDARY:
			String firstChar1[] = { ",", ";" };

			break_loop: for (String compareChar1 : firstChar1) {
				for (String compareChar2 : secondChar) {
					String compareString = compareChar1 + compareChar2;
					// Log.d("tushar", "compare string = " + compareString);
					indexOfBoundary = textToHighlight
							.lastIndexOf(compareString);
					if (indexOfBoundary != -1)
						break break_loop;
				}
			}

			if (indexOfBoundary != -1) {
				indexOfBoundary += 2;
				// Log.d("tushar", "text boundary = " +
				// textToHighlight.substring(0, indexOfBoundary));
			}
			// Log.d("tushar", "index of Boundary = " + indexOfBoundary );
			break;
		case Constants.CHECK_WORD_BOUNDARY:
			indexOfBoundary = textToHighlight.lastIndexOf(" ");
			if (indexOfBoundary != -1) {
				indexOfBoundary += 1;
				// Log.d("tushar","text boundary = " +
				// textToHighlight.substring(0, indexOfBoundary).replaceAll(" ",
				// "?"));
			}
			break;
		}

		if (indexOfBoundary != -1) {
			indexOfBoundary += startIndexOfCheck;
		}
		return indexOfBoundary;
	}

	private void saveRemainingBallot(final int ballotPosition,
			final ArrayList<Contest> contestList) {
		if ((saveThread == null)
				|| (!saveThread.isAlive() && saveThread.getState() == Thread.State.TERMINATED)) {
			saveThread = new Thread(new Runnable() {
				@Override
				public void run() {
					int numberOfBallotSaved = 0;
					// save current ballot
					saveBallot(ballotPosition);
					// Log.d("tushar","current ballot saved");

					for (int i = (ballotPosition + 1); i < contestList.size(); i++) {
						Log.d("tushar", "checking ballot = " + i);
						if (!isAlreadySaved(i)) {
							// Log.d("tushar","ballot saving = " + i);
							saveBallot(i);
							numberOfBallotSaved++;
						}
					}
					gHandler.sendEmptyMessage(Constants.REMAINING_BALLOT_SAVED);
				}
			});
			saveThread.start();
		}
	}

	private boolean isAlreadySaved(int ballotPosition) {
		boolean result = false;
		SharedPreferences preferences = getSharedPreferences(
				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		if (preferences.getString(String.valueOf(ballotPosition), null) != null) {
			Log.d("tushar", "isAlreadySaved? true");
			result = true;
		}
		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("tushar", "request code = " + requestCode + ", resultCode = "
				+ resultCode);

		if (requestCode == Constants.TTS_DATA_CHECK_CODE) {
			if (resultCode == SpeakManager.Engine.CHECK_VOICE_DATA_PASS) {

				if (Constants.SETTING_TTS_VOICE == Constants.DEFAULT_TTS_VOICE) {
					gTTS = new SpeakManager(gContext, this, "com.svox.classic");
				} else {
					gTTS = new SpeakManager(gContext, this, "com.ivona.tts");
				}

				gTTSProgressHelper = new UtterenceProgressHelper(
						ContestActivity.this);
				gTTS.setOnUtteranceProgressListener(gTTSProgressHelper);
			} else {
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent
						.setAction(SpeakManager.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		} else if (requestCode == Constants.REQUEST_CODE_ALERTACTIVITY) {
			if (resultCode == Constants.TYPE_NOTICE_PAGE) {
			} else if (resultCode == Constants.TYPE_OVER_VOTE) {
				gTTSOnStart = data.getStringExtra(Constants.ANNOUNCE);
			} else if (resultCode == Constants.TYPE_UNDER_VOTE) {
				if (data != null) {
					/**
					 * if data is not null than right btn pressed tts has to
					 * initialize again hence removing old refernce
					 */
					gBallotPosition++;
					if (gBallotPosition == gContestList.size()) {
						gBallotPosition = gContestList.size() - 1;
						resetBallotVariable(true);
						Intent intent = new Intent(this, SummaryActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					Log.d("tushar", "under vote right ballot position = "
							+ gBallotPosition);
				}
			} else if (resultCode == Constants.TYPE_LOAD_SUMMARY) {
				if (data != null) {
					isSummaryGenerated = true;
					speakWord("", null, false);
					startActivity(new Intent(this, SummaryActivity.class));
					finish();
				}
			} else {
				isContestExplored = true;
			}
		} else if (requestCode == Constants.REQUEST_CODE_WRITEIN_ACTIVITY
				&& resultCode == Activity.RESULT_OK) {
			String writeInValue = data.getStringExtra(Constants.WRITEIN_VALUE);
			int writeInPosition = data.getIntExtra(Constants.WRITEIN_POSITION,
					-1);

			if (writeInPosition != -1 && writeInValue.trim().length() > 0) {
				gContestList.get(gBallotPosition).candidateList
						.get(writeInPosition).candidateParty = writeInValue;
				gContestList.get(gBallotPosition).candidateList
						.get(writeInPosition).candidateName = getString(R.string.writein);

				// gBallotPage.getText().toString() + Constants.COMMA_SPACE
				// + Constants.DOT_SPACE
				gTTSOnStart = gContext.getString(R.string.marked)
						+ gContext.getString(R.string.dot) + Constants.SPACE
						+ gContext.getString(R.string.writein)
						+ Constants.SPACE
						+ gContext.getString(R.string.candidate)
						+ Constants.SPACE + writeInValue + Constants.SPACE
						+ gContext.getString(R.string.mrk_on_ballot);

				Log.d(TAG, "write in activity tts = " + gTTSOnStart);
				gContestAdapter.notifyDataSetChanged();
			} else {
				gTTSOnStart = gContestList.get(gBallotPosition).candidateList
						.get(writeInPosition).candidateName
						+ gContext.getString(R.string.dot)
						+ Constants.SPACE
						+ gContext.getString(R.string.is_empty);
				gContestAdapter.getItem(writeInPosition).candidateCheck = false;
			}

			gContestAdapter.notifyDataSetChanged();
			saveBallot(gBallotPosition);
		}
	}

	private void getInstalledEngineInfo(SpeakManager gTTS) {
		// List<EngineInfo> installedEnginePckg = gTTS.getEngines();

		// for(int i = 0; i < installedEnginePckg.size(); i++){
		// Log.d("tushar", "engine pckg name = " +
		// installedEnginePckg.get(i).name);
		// }

		// String defaultPckgName = gTTS.getDefaultEngine();

		// if(defaultPckgName.equalsIgnoreCase("com.ivona.tts")){
		// Log.d("tushar", "default engine ivona");
		gTTS = new SpeakManager(gContext, this, "com.svox.classic");

		// }
	}

	private void unregisterAllReceiver() {
		if (isSettingScanned) {
			unregisterReceiver(gHeadsetListener);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		gScrollUpBtn.setOnClickListener(gOnClickListener);
		gScrollDownBtn.setOnClickListener(gOnClickListener);
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
		gGoToSummary.setOnClickListener(gOnClickListener);
		gGoToSummary.setOnTouchListener(gOnTouchListener);
		gVolumeIncrease.setOnClickListener(gOnClickListener);
		gVolumeIncrease.setOnTouchListener(gOnTouchListener);
		gVolumeDecrease.setOnClickListener(gOnClickListener);
		gVolumeDecrease.setOnTouchListener(gOnTouchListener);
		gBallotPage.setOnClickListener(gOnClickListener);
		gBallotPage.setOnTouchListener(gOnTouchListener);
		gBallotInstruction.setOnClickListener(gOnClickListener);
		gBallotInstruction.setOnTouchListener(gOnTouchListener);
		gReferendumYesTitle.setOnTouchListener(gOnTouchListener);
		gReferendumYesTitle.setOnClickListener(gOnClickListener);
		gReferendumNoTitle.setOnTouchListener(gOnTouchListener);
		gReferendumNoTitle.setOnClickListener(gOnClickListener);

		gBallotReferendumLayout.setOnTouchListener(gOnTouchListener);
		gCandidateListView.setOnTouchListener(gOnTouchListener);
		gReferendumText.setOnTouchListener(gOnTouchListener);
		// gReferendumSubtitle.setOnTouchListener(gOnTouchListener);

		gReferendumText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.d("", "textview touch event = " + event);
				gdt.onTouchEvent(event);
				if (gTTSProgressHelper != null
						&& gTTSProgressHelper.isHybridChunkRead) {
					return true;
				} else {
					return false;
				}
			}
		});

		// gBallotPage.sets
		gBallotPage.setTextSize(Constants.SETTING_FONT_SIZE);
		gBallotInstruction.setTextSize(Constants.SETTING_FONT_SIZE);
		gReferendumText.setTextSize(Constants.SETTING_FONT_SIZE);
		gReferendumText.setMovementMethod(new ScrollingMovementMethod());
		gReferendumYesTitle.setTextSize(Constants.SETTING_FONT_SIZE);
		gReferendumNoTitle.setTextSize(Constants.SETTING_FONT_SIZE);
		gScrollDownBtn.setTextSize(Constants.SETTING_FONT_SIZE);
		gScrollUpBtn.setTextSize(Constants.SETTING_FONT_SIZE);

		gContext = ContestActivity.this;
		gHeadsetListener = new HeadsetListener();
		gAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		if (Constants.SETTING_LANGUAGE == Constants.DEFAULT_LANG_SETTING) {
			gContestList = Utils.readContestFile(this,
					Constants.NIST_VOTING_PROTOTYPE_DIRECTORY + File.separator
							+ Constants.NIST_VOTING_PROTOTYPE_FILE_EN);
		} else {
			gContestList = Utils.readContestFile(this,
					Constants.NIST_VOTING_PROTOTYPE_DIRECTORY + File.separator
							+ Constants.NIST_VOTING_PROTOTYPE_FILE_SP);
		}

		if (gContestList != null) {
			Log.d("tushar", "is summary ballot loaded = "
					+ isSummaryBallotLoaded);

			/**
			 * load ballot page from summary
			 */
			if (!isSummaryBallotLoaded) {
				int ballotPosition = getIntent().getIntExtra(
						Constants.CONTEST_POSITION, -1);
				if (ballotPosition != -1) {
					gBallotPosition = ballotPosition;
				}

				isCameFromSummary = getIntent().getBooleanExtra(
						Constants.RETURN_TO_SUMMARY, false);
				isSummaryBallotLoaded = true;
				Log.d("tushar", "is came from summary = " + isCameFromSummary);
			}
			loadBallot(gBallotPosition);
		}

		isTTSInit = false;
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(SpeakManager.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, Constants.TTS_DATA_CHECK_CODE);
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
					if (gContestAdapter == null
							&& !gTTSProgressHelper.isHybridChunkRead) {
						/**
						 * for not interupting the reading of hybrid mode
						 */
						speakWord(getString(R.string.softest), null, false);
					} else if (gContestAdapter != null) {
						speakWord(getString(R.string.softest), null, false);
					}
				}
			} else {
				gAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						curVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {
					if (gContestAdapter == null
							&& !gTTSProgressHelper.isHybridChunkRead) {
						/**
						 * for not interupting the reading of hybrid mode
						 */
						speakWord(getString(R.string.softer), null, false);
					} else if (gContestAdapter != null) {
						speakWord(getString(R.string.softer), null, false);
					}
				}
			}
			break;
		case Constants.INCREASE:
			curVolume = curVolume + Constants.V0LUME_DIFFERENCE;

			if (curVolume >= maxVolume) {
				gAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						maxVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {
					if (gContestAdapter == null
							&& !gTTSProgressHelper.isHybridChunkRead) {
						/**
						 * for not interupting the reading of hybrid mode
						 */
						speakWord(getString(R.string.loudest), null, false);
					} else if (gContestAdapter != null) {
						speakWord(getString(R.string.loudest), null, false);
					}
				}
			} else {
				gAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						curVolume, 0);
				if (HeadsetListener.isHeadsetConnected) {
					if (gContestAdapter == null
							&& !gTTSProgressHelper.isHybridChunkRead) {
						/**
						 * for not interupting the reading of hybrid mode
						 */
						speakWord(getString(R.string.louder), null, false);
					} else if (gContestAdapter != null) {
						speakWord(getString(R.string.louder), null, false);
					}
				}
			}
			break;
		}
	}

	/**
	 * varable changed when ballot changed
	 * 
	 * @param resetAdapter
	 */
	protected void resetBallotVariable(boolean resetAdapter) {
		speakWord("", null, false);

		if (gChunkList != null || gContestAdapter != null) {
			navigateToOtherItem(gFocusPosition,
					Constants.JUMP_FROM_CURRENT_ITEM);
			Log.d(TAG, "gFocusPosition = " + gFocusPosition);
		}

		/**
		 * reseting focus position to read the title
		 */
		gFocusPosition = 1;
		if (resetAdapter) {
			gContestAdapter = null;
		}
		gChunkList = null;
		// isSetTTSParam = true;
		isChunkListFull = false;
		isHybridModeOn = true;
		isContestExplored = false;
		isHeadingTTSInterupted = false;
		if (gTTSProgressHelper != null) {
			stopChunkReading();
		}
	}

	private void refreshCandidateList() {
		gContestAdapter.notifyDataSetChanged();
	}

	public void speakWord(String word, HashMap<String, String> utteranceId,
			boolean shouldRepeat) {
		if (gTTS != null) {
			if (utteranceId != null) {
				gTTS.speak(word, SpeakManager.QUEUE_FLUSH, utteranceId,
						shouldRepeat);
			} else {
				gTTS.speak(word, SpeakManager.QUEUE_FLUSH, null, shouldRepeat);
			}
			Log.d(TAG, " gfocus position = " + gFocusPosition);
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
				// Log.d(TAG, "isSetTTSParam = " + isSetTTSParam);
				HashMap<String, String> params = null;
				// if (isSetTTSParam) {
				params = new HashMap<String, String>();
				params.put(SpeakManager.Engine.KEY_PARAM_UTTERANCE_ID,
						UtterenceProgressHelper.UID_CONTEST_ACTIVITY);
				// } else {
				// isSetTTSParam = true;
				// }

				speakWord(gTTSOnStart, params, true);
				gTTSOnStart = null;
			}
			isTTSInit = true;
		} else if (status == SpeakManager.ERROR) {
			Toast.makeText(gContext, getString(R.string.failed),
					Toast.LENGTH_SHORT).show();
		}
	}

	public JSONObject getJSONObject(String filePath) {
		StringBuilder builder;
		BufferedReader reader = null;
		JSONObject jObject = null;

		try {
			reader = new BufferedReader(new FileReader(new File(filePath)));
			builder = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			jObject = new JSONObject(builder.toString());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return jObject;
	}

	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(gHeadsetListener, filter);
	}

	@Override
	public void onPause() {
		super.onPause();
		// if (gTTS != null) {
		// gTTS.stop();
		// gTTS.shutdown();
		// }
		// unregisterReceiver(gHeadsetListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (gTTS != null) {
			gTTS.stop();
			gTTS.shutdown();
			isTTSInit = false;
		}
		unregisterReceiver(gHeadsetListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// if (gTTS != null) {
		// gTTS.stop();
		// gTTS.shutdown();
		// }
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!isButtonPressed) {
			navigateToOtherItem(gFocusPosition,
					Constants.JUMP_FROM_CURRENT_ITEM);
			gFocusPosition = 0;
		}

		if (((LinearLayout) buttonView.getParent()).getId() == gYesCheckBoxView
				.getId()) {
			if (gReferendumNo.isChecked()) {
				gReferendumYes.toggle();
				saveBallot(gBallotPosition);
				List<String> msgList = new ArrayList<String>();
				msgList.add(getString(R.string.overvote_no_referendum));
				msgList.add(getString(R.string.go_back));
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				loadAlertPage(Constants.TYPE_OVER_VOTE,
						gContext.getString(R.string.over_vote_page), msgList,
						getString(R.string.accept));
			} else {
				String tts = null;
				if (gReferendumYes.isChecked()) {
					tts = getString(R.string.marked) + Constants.COMMA_SPACE
							+ getString(R.string.accept) + Constants.SPACE
							+ getString(R.string.mrk_on_ballot);
				} else {
					tts = getString(R.string.erase) + Constants.SPACE
							+ getString(R.string.mrk_for) + Constants.SPACE
							+ getString(R.string.accept) + Constants.SPACE
							+ getString(R.string.erase_from_ballot);
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(tts, null, true);
				}
			}
		} else if (((LinearLayout) buttonView.getParent()).getId() == gNoCheckBoxView
				.getId()) {
			if (gReferendumYes.isChecked()) {
				gReferendumNo.toggle();
				saveBallot(gBallotPosition);
				List<String> msgList = new ArrayList<String>();
				msgList.add(getString(R.string.overvote_yes_referendum));
				msgList.add(getString(R.string.go_back));
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				loadAlertPage(Constants.TYPE_OVER_VOTE,
						gContext.getString(R.string.over_vote_page), msgList,
						getString(R.string.reject));
			} else {
				String tts = null;
				if (gReferendumNo.isChecked()) {
					tts = getString(R.string.marked) + Constants.COMMA_SPACE
							+ getString(R.string.reject) + Constants.SPACE
							+ getString(R.string.mrk_on_ballot);
				} else {
					tts = getString(R.string.erase) + Constants.SPACE
							+ getString(R.string.mrk_for) + Constants.SPACE
							+ getString(R.string.reject) + Constants.SPACE
							+ getString(R.string.erase_from_ballot);
				}
				if (HeadsetListener.isHeadsetConnected) {
					speakWord(tts, null, true);
				}
			}
		}
	}

	public void setNightMode() {
		gCandidateListView.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotPage
				.setTextColor(getResources().getColor(android.R.color.white));
		gBallotPage.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotInstruction.setTextColor(getResources().getColor(
				android.R.color.white));
		gBallotInstruction.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotInstructionContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gReferendumText.setTextColor(getResources().getColor(
				android.R.color.white));
		gReferendumText.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gReferendumContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gYesCheckBoxView.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gNoCheckBoxView.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gReferendumYesTitle.setTextColor(getResources().getColor(
				android.R.color.white));
		gReferendumNoTitle.setTextColor(getResources().getColor(
				android.R.color.white));
		gScrollUpBtn.setBackgroundColor(getResources().getColor(
				R.color.bg_button));
		gScrollUpBtn.setTextColor(getResources()
				.getColor(android.R.color.white));
		gScrollDownBtn.setBackgroundColor(getResources().getColor(R.color.bg_button));
		gScrollDownBtn.setTextColor(getResources().getColor(
				android.R.color.white));

		gScrollUpContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gScrollDownContainer.setBackgroundColor(getResources().getColor(
				android.R.color.black));
		gBallotReferendumLayout.setBackgroundColor(getResources().getColor(
				android.R.color.black));

		// ((LinearLayout)gReferendumYesTitle.getParent().getParent()).setBackgroundColor(getResources().getColor(
		// android.R.color.black));
		// ((ViewGroup) gReferendumYesTitle.getParent())
		// .setBackgroundColor(getResources().getColor(
		// android.R.color.black));
		// ((ViewGroup) gReferendumNoTitle.getParent().getParent())
		// .setBackgroundColor(getResources().getColor(
		// android.R.color.black));
	}

	public void loadBallot(int ballotPositionToLoad) {
		resetBallotVariable(true);
		addRemoveLeftArrow(ballotPositionToLoad);
		setBallotPageTitle(ballotPositionToLoad);

		Contest contest = gContestList.get(ballotPositionToLoad);
		if (contest.isReferendum) {
			loadReferendumUI(contest, ballotPositionToLoad);
		} else {
			loadContestUI(contest, ballotPositionToLoad);
		}
	}

	private void setBallotPageTitle(int ballotPositionToLoad) {
		int ballotPage = ballotPositionToLoad + 1;

		if (isCameFromSummary) {
			/**
			 * if came from summary change the title
			 */
			gBallotPage.setText(getString(R.string.change_vote));
		} else {
			gBallotPage.setText(getString(R.string.ballot_page)
					+ Constants.SPACE + ballotPage + Constants.SPACE
					+ getString(R.string.of) + Constants.SPACE
					+ (gContestList.size()));
		}

		gBallotPage.setBackgroundResource(R.drawable.focused);
		// gBallotPage.requestFocus();
	}

	private class GestureListener extends SimpleOnGestureListener {
		private static final String TAG = "GestureListener";

		// @Override
		// public boolean onDown(MotionEvent e) {
		// Log.d(TAG, "touch down, hybrid chunk read = "
		// + gTTSProgressHelper.isHybridChunkRead);
		// return false;
		// }

		// @Override
		// public boolean onScroll(MotionEvent e1, MotionEvent e2,
		// float distanceX, float distanceY) {
		// return false;
		// }

		// @Override
		// public boolean onSingleTapUp(MotionEvent e) {
		//
		// isContestExplored = true;
		// startChunkReading();
		// return false;
		// }

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			isContestExplored = true;
			isHeadingTTSInterupted = true;
			speakWord("", null, false);
			/**
			 * if referendum is currently reading in hybrid mode than stop it
			 * and cancel the hybrid mode else preserve it for keyboard
			 */
			toggleHybridChunkReadAndHybridModeDependency();

			if (gTTSProgressHelper.isHybridChunkRead) {
				startChunkReading();
			} else {
				navigateToOtherItem(gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				gFocusPosition = 0;

				// /**
				// * if referendum is currently reading in hybrid mode than stop
				// * it and cancel the hybrid mode else preserve it for keyboard
				// */
				// toggleHybridChunkReadAndHybridModeDependency();

				/**
				 * set the text to black again
				 */
				gReferendumText
						.setText(gContestList.get(gBallotPosition).contest_referendum_subs);

				int offsetposition = gReferendumText.getOffsetForPosition(
						e.getX(), e.getY());

				// startChunkReading();
				int chunkIndex = -1;
				Entry<Integer, Integer> entry = null;
				for (int i = 0; i < gChunkList.size(); i++) {
					entry = gChunkList.get(i);
					if (offsetposition > entry.getKey()
							&& offsetposition < entry.getValue()) {
						chunkIndex = i;
						break;
					}
				}

				if (chunkIndex > AdapterView.INVALID_POSITION
						&& chunkIndex < gChunkList.size()) {
					gFocusPosition = 2 + chunkIndex + 1;
					setReferendumFocus(gChunkList.get(chunkIndex));
				}
			}
			return false;
		}
	}

	public void startChunkReading() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		// gTTSProgressHelper.isHybridChunkRead =
		// gTTSProgressHelper.isHybridChunkRead ? false
		// : true;

		if (gTTSProgressHelper.isHybridChunkRead) {
			gFocusPosition = 3;
			bufferNextChunk = true;

			if (gFocusPosition < 3 + gChunkList.size()) {
				setReferendumFocus(gChunkList.get(gFocusPosition - 3));

				/**
				 * buffer nxt chunk to chunk list if chunklist is not full.
				 */
				// if (!isChunkListFull && bufferNextChunk) {
				// gLastIndexOfChunk = chunkAlgorithm(gLastIndexOfChunk);
				// }
				Log.d("tushar", " focusposition in referendum = "
						+ gFocusPosition);
			}
		} else if (!gTTSProgressHelper.isHybridChunkRead) {
			speakWord("", null, false);
		}
	}

	public void stopChunkReading() {
		gTTSProgressHelper.isHybridChunkRead = false;
	}

	Handler gHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "message what = " + msg.what);

			switch (msg.what) {
			case Constants.REMAINING_BALLOT_SAVED:
				if (isSummaryGenerated) {
					speakWord("", null, true);
					unregisterAllReceiver();
					startActivity(new Intent(gContext, SummaryActivity.class));
				} else {
					List<String> message = new ArrayList<String>();
					message.add(getString(R.string.goto_summary_page));
					message.add(getString(R.string.go_back) + Constants.SPACE
							+ getString(R.string.prcd_summary_page));
					loadAlertPage(Constants.TYPE_LOAD_SUMMARY,
							gContext.getString(R.string.notice_page), message,
							null);
				}
				break;
			}
		}
	};

	protected void scrollList(boolean isScrollDown) {
		if (isScrollDown) {
			int lastPositionIndex = gCandidateListView.getLastVisiblePosition();
			Log.d(TAG, "lastPositionIndex = " + lastPositionIndex);
			gCandidateListView.setSelection(lastPositionIndex);
		} else {
			int height = gCandidateListView.getHeight()
					- gCandidateListView.getChildAt(0).getHeight();

			gCandidateListView.setSelectionFromTop(
					gCandidateListView.getFirstVisiblePosition(), height);
		}
	}

	protected void scrollReferendum(boolean isScrollDown) {
		int scrollTo = -1;
		int firstVisiblelineTop = -1;
		int lastVisiblelineTop = -1;
		int height = gReferendumText.getHeight();
		int scrollY = gReferendumText.getScrollY();
		int totalNumberOfLine = gReferendumText.getLineCount();
		int firstVisibleLineNumber = gReferendumText.getLayout()
				.getLineForVertical(scrollY);
		int lastVisibleLineNumber = gReferendumText.getLayout()
				.getLineForVertical(scrollY + height);

		// int numOfVisibleLine = (gReferendumSubtitle.getHeight()
		// - gReferendumSubtitle.getTotalPaddingTop() - gReferendumSubtitle
		// .getTotalPaddingBottom())
		// / gReferendumSubtitle.getLineHeight();
		int remainingLine = -1;

		firstVisiblelineTop = gReferendumText.getLayout().getLineTop(
				firstVisibleLineNumber);
		lastVisiblelineTop = gReferendumText.getLayout().getLineTop(
				lastVisibleLineNumber);

		if (isScrollDown) {
			if (lastVisibleLineNumber < totalNumberOfLine - 1) {
				remainingLine = totalNumberOfLine - lastVisibleLineNumber;
				int tempScrollAmount = gReferendumText.getLineHeight()
						* remainingLine;
				if (tempScrollAmount < lastVisiblelineTop - firstVisiblelineTop) {
					/**
					 * added extra line height if there is some half line while
					 * scrolling
					 */
					scrollTo = firstVisiblelineTop + tempScrollAmount
							+ gReferendumText.getLineHeight();
				} else {
					scrollTo = lastVisiblelineTop;
				}

				gReferendumText.scrollTo(0, scrollTo);
			}
		} else {
			remainingLine = firstVisibleLineNumber;
			int tempScrollAmount = gReferendumText.getLineHeight()
					* remainingLine;
			if (tempScrollAmount < lastVisiblelineTop - firstVisiblelineTop) {
				scrollTo = 0;
			} else {
				scrollTo = firstVisiblelineTop
						- (lastVisiblelineTop - firstVisiblelineTop);
			}

			gReferendumText.scrollTo(0, scrollTo);
		}
	}

	public void loadNextBallot() {
		gBallotPosition++;

		if (gBallotPosition == gContestList.size()) {
			resetBallotVariable(true);
			gBallotPosition = gContestList.size() - 1;
			Intent intent = new Intent(this, SummaryActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else {
			loadBallot(gBallotPosition);
		}
	}

	public void loadPreviousBallot() {
		gBallotPosition--;

		if (gBallotPosition < 0) {
			/**
			 * if gBallotposition is less than 0 than it is on 1st page or 0
			 * ballot
			 */
			// resetBallotVariable(true);
			gBallotPosition = 0;
			if (HeadsetListener.isHeadsetConnected) {
				speakWord(getString(R.string.on_first_page), null, true);
			}
		} else {
			/**
			 * if gBallotposition is not less than 0 than it is not on 1st page
			 * so load ballot
			 */
			loadBallot(gBallotPosition);
		}
	}

	protected void changeToTouchMode() {
		navigateToOtherItem(gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
		gFocusPosition = 0;
	}
}