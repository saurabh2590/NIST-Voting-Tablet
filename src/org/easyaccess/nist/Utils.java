package org.easyaccess.nist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Utils {
	private final static String TAG = "Utils";
	public static int downX = -1;
	public static int downY = -1;

	static ArrayList<Contest> readContestFile(Context context, String filePath) {
		BufferedReader readingStream = null;
		ArrayList<Contest> contestLists = null;

		try {
			File file = new File(filePath);
			readingStream = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			StringBuilder builder = new StringBuilder();
			String line = null;

			while ((line = readingStream.readLine()) != null) {
				builder.append(line);
			}

			contestLists = parseContest(builder.toString(), context);
		} catch (IOException e) {
		} finally {
			if (readingStream != null) {
				try {
					readingStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return contestLists;
	}

	static StringBuilder readFile(Context context, String filePath) {
		BufferedReader readingStream = null;
		StringBuilder builder = new StringBuilder();

		try {
			File file = new File(filePath);
			readingStream = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line = null;

			while ((line = readingStream.readLine()) != null) {
				builder.append(line);
			}

		} catch (IOException e) {
		} finally {
			if (readingStream != null) {
				try {
					readingStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return builder;
	}

	static StringBuilder readFile(Context context, InputStream inputStream) {
		BufferedReader readingStream = null;
		StringBuilder builder = new StringBuilder();

		try {
			readingStream = new BufferedReader(new InputStreamReader(
					inputStream));
			String line = null;

			while ((line = readingStream.readLine()) != null) {
				builder.append(line);
			}

		} catch (IOException e) {
		} finally {
			if (readingStream != null) {
				try {
					readingStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return builder;
	}

	static void writeToFile(String string, String filePath, boolean isAppend) {
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(new File(
					filePath), isAppend));
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

	static ArrayList<Contest> parseContest(String string, Context context) {
		JSONObject jsonObject = null;
		ArrayList<Contest> contestList = null;

		try {
			jsonObject = new JSONObject(string);

			if (jsonObject.has(Constants.CONTESTS)) {
				JSONArray contestArray = jsonObject
						.getJSONArray(Constants.CONTESTS);
				contestList = new ArrayList<Contest>();

				Contest contest = null;

				for (int i = 0; i < contestArray.length(); i++) {
					jsonObject = contestArray.getJSONObject(i);
					contest = new Contest();

					contest.contest_position = i;

					if (jsonObject.has(Constants.TYPE)) {
						contest.contest_type = jsonObject
								.getString(Constants.TYPE);
					}

					if (jsonObject.has(Constants.ELECTORATE_SPECS)) {
						contest.contest_electorate_specs = jsonObject
								.getString(Constants.ELECTORATE_SPECS);
					}

					if (jsonObject.has(Constants.OFFICE)) {
						contest.contest_office = jsonObject
								.getString(Constants.OFFICE);
					}

					if (jsonObject.has(Constants.NUMBER_VOTING_FOR)) {
						contest.vote_per_candidate = (int) jsonObject
								.getLong(Constants.NUMBER_VOTING_FOR);
					}

					if (jsonObject.has(Constants.REFERENDUM_TITLE)) {
						contest.contest_referendum_title = jsonObject
								.getString(Constants.REFERENDUM_TITLE);
						contest.isReferendum = true;
					}

					if (jsonObject.has(Constants.REFERENDUM_INSTRUCTION)) {
						contest.contest_referendum_instruction = jsonObject
								.getString(Constants.REFERENDUM_INSTRUCTION);
					}

					if (jsonObject.has(Constants.REFERENDUM_SUBTITLE)) {
						contest.contest_referendum_subs = jsonObject
								.getString(Constants.REFERENDUM_SUBTITLE);
					}

					if (jsonObject.has(Constants.CANDIDATES)) {
						JSONArray candidateArray = jsonObject
								.getJSONArray(Constants.CANDIDATES);
						contest.candidateList = getCandidateList(
								candidateArray, contest.vote_per_candidate,
								context);
					}

					contestList.add(contest);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return contestList;
	}

	static ArrayList<Candidate> getCandidateList(JSONArray candidateArray,
			int vote_per_candidate, Context context) {
		ArrayList<Candidate> candidateList = new ArrayList<Candidate>();

		Candidate candidate = null;
		JSONObject candidateObject = null;

		for (int j = 0; j < candidateArray.length(); j++) {
			try {
				candidateObject = candidateArray.getJSONObject(j);
				candidate = new Candidate();

				if (candidateObject.has(Constants.NAME)) {
					candidate.candidateName = candidateObject
							.getString(Constants.NAME);
				}

				if (candidateObject.has(Constants.PARTY)) {
					candidate.candidateParty = candidateObject
							.getString(Constants.PARTY);
				}

				if (candidateObject.has(Constants.WRITEIN)) {
					candidate.isWriteIn = Boolean.valueOf(candidateObject
							.getString(Constants.WRITEIN));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			candidateList.add(candidate);
			// candidateList.add(candidate);
		}

		for (int j = 0; j < vote_per_candidate; j++) {
			candidate = new Candidate();

			if (vote_per_candidate == 1) {
				candidate.candidateName = context
						.getString(R.string.write_blank);
			} else {
				candidate.candidateName = context
						.getString(R.string.write_blank)
						+ Constants.SPACE
						+ (j + 1)
						+ Constants.SPACE
						+ context.getString(R.string.of)
						+ Constants.SPACE
						+ vote_per_candidate;
			}

			candidate.candidateParty = "";

			candidate.isWriteIn = true;

			candidateList.add(candidate);
			// candidateList.add(candidate);
		}
		return candidateList;
	}

//	public static void showCustomAlertDialog(
//			final ContestActivity contestActivity, String message,
//			float textSize) {
//		WindowManager windowManager = (WindowManager) contestActivity
//				.getSystemService(Context.WINDOW_SERVICE);
//		Display display = windowManager.getDefaultDisplay();
//		Point size = new Point();
//		display.getSize(size);
//
//		final Dialog alrtDialog = new Dialog(contestActivity);
//		alrtDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		alrtDialog.setContentView(R.layout.view_cstm_dialog);
//
//		// AlertDialog.Builder dialogBuilder = new
//		// AlertDialog.Builder(contestActivity);
//		// dialogBuilder.setPositiveButton(contestActivity.getString(R.string.dialog_ok),
//		// null);
//		// dialogBuilder.setView(LayoutInflater.from(contestActivity).inflate(R.layout.view_cstm_dialog,
//		// null, true));
//		//
//		// final Dialog alrtDialog = dialogBuilder.create();
//		alrtDialog.show();
//		contestActivity.gFocusPosition = 0;
//		contestActivity.isAlertDialogVisible = true;
//
//		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//		lp.copyFrom(alrtDialog.getWindow().getAttributes());
//		lp.width = size.x;
//		lp.height = size.y * 50 / 100;
//
//		alrtDialog.getWindow().setAttributes(lp);
//
//		Log.d("tushar", "dialog message = " + message);
//
//		final Button btn = (Button) alrtDialog.findViewById(R.id.btn_finish);
//		btn.setTextSize(Constants.FONT_SIZE_STD);
//		btn.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				alrtDialog.dismiss();
//			}
//		});
//
//		final TextView text = (TextView) alrtDialog
//				.findViewById(R.id.tv_dialog_msg);
//		if (!Constants.SETTING_REVERSE_SCREEN) {
//			alrtDialog.findViewById(R.id.view_root_relativelayout)
//					.setBackground(
//							contestActivity.getResources().getDrawable(
//									R.drawable.cstm_dialog_background));
//			text.setTextColor(contestActivity.getResources().getColor(
//					android.R.color.black));
//		}
//
//		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
//		text.setText(message);
//
//		text.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				contestActivity
//						.speakWord(text.getText().toString(), null, true);
//				return true;
//			}
//		});
//
//		text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) {
//				if (hasFocus) {
//					text.setBackgroundResource(R.drawable.focused);
//					contestActivity.speakWord(text.getText().toString(), null,
//							true);
//				} else {
//					text.setBackground(null);
//					contestActivity.speakWord(
//							contestActivity.getString(R.string.close)
//									+ Constants.COMMA_SPACE
//									+ contestActivity
//											.getString(R.string.button), null,
//							true);
//				}
//			}
//		});
//
//		alrtDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				contestActivity.isAlertDialogVisible = false;
//				// contestActivity.speakWord("warning prompt closed", null,
//				// true);
//			}
//		});
//
//		alrtDialog
//				.setOnDismissListener(new DialogInterface.OnDismissListener() {
//					@Override
//					public void onDismiss(DialogInterface dialog) {
//						contestActivity.isAlertDialogVisible = false;
//						// contestActivity.speakWord("warning prompt closed",
//						// null, true);
//					}
//				});
//
//		alrtDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//			@Override
//			public boolean onKey(DialogInterface dialog, int keyCode,
//					KeyEvent event) {
//				// if(event.getAction() == KeyEvent.ACTION_MULTIPLE){
//				// return false;
//				// }
//
//				boolean result = false;
//				View view = alrtDialog.getCurrentFocus();
//
//				Log.d(TAG, "key action = " + event.getAction());
//				if (event.getAction() == KeyEvent.ACTION_DOWN) {
//
//					contestActivity.isButtonPressed = true;
//					int keyPressed = -1;
//					if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
//						keyPressed = event.getScanCode();
//					} else {
//						keyPressed = keyCode;
//					}
//
//					Log.d(TAG, "key pressed = " + keyPressed);
//					Log.d(TAG, "button event = " + event);
//					Log.d(TAG,
//							"current focus = " + alrtDialog.getCurrentFocus());
//					/**
//					 * for making the button focusable
//					 */
//					btn.setFocusable(true);
//					btn.setFocusableInTouchMode(true);
//
//					switch (keyPressed) {
//					case KeyEvent.KEYCODE_DPAD_UP:
//					case KeyEvent.KEYCODE_BUTTON_1:
//						// button.clearFocus();
//						text.requestFocus();
//
//						result = true;
//						break;
//					case KeyEvent.KEYCODE_DPAD_DOWN:
//					case KeyEvent.KEYCODE_BUTTON_2:
//						btn.requestFocus();
//
//						result = true;
//						break;
//					case KeyEvent.KEYCODE_ENTER:
//					case KeyEvent.KEYCODE_BUTTON_3:
//						if (view instanceof Button) {
//							dialog.dismiss();
//						} else if (view instanceof TextView) {
//							contestActivity.speakWord(((TextView) view)
//									.getText().toString(), null, true);
//						}
//
//						result = true;
//						break;
//					}
//				} else if (event.getAction() == KeyEvent.ACTION_UP) {
//				}
//				return result;
//			}
//		});
//	}

	public static void showCustomDialog(Context context, String message,
			float textSize) {
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.view_cstm_dialog);
		// dialog.setPositiveButton(context.getString(R.string.dialog_ok),
		// null);
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		// Display display= getWindow().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		LinearLayout rootViewGroup = (LinearLayout) dialog
				.findViewById(R.id.view_root_relativelayout);
		LayoutParams params = (LayoutParams) rootViewGroup.getLayoutParams();
		params.height = size.y * 40 / 100;
		params.width = size.x;

		WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
		Log.d("tushar", "size.y/2 = " + size.y / 2 + ", downY = " + downY
				+ ", wmlp.y = " + wmlp.y + ", wmlp.y + params.height = "
				+ wmlp.y + params.height);
		if (downY < size.y / 2 && downY > (size.y / 2 - params.height)) {
			wmlp.gravity = Gravity.BOTTOM;
		} else if (downY > size.y / 2 && downY < (size.y / 2 + params.height)) {
			wmlp.gravity = Gravity.TOP;
		}

		final TextView text = (TextView) dialog
				.findViewById(R.id.tv_dialog_msg);
		text.setMovementMethod(new ScrollingMovementMethod());
		Log.d("Utils", "text.getScrollBarSize() = " + text.getScrollBarSize());
		final Button scrollUp = (Button) dialog
				.findViewById(R.id.btn_scroll_up);
		final Button scrollDown = (Button) dialog
				.findViewById(R.id.btn_scroll_down);
		scrollUp.setTextSize(Constants.FONT_SIZE_STD);
		scrollDown.setTextSize(Constants.FONT_SIZE_STD);

		scrollUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollText(text, false);
			}
		});

		scrollDown.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scrollText(text, true);
			}
		});

		text.post(new Runnable() {
			@Override
			public void run() {
				if (!isScrollable(text)) {
					scrollUp.setVisibility(View.GONE);
					scrollDown.setVisibility(View.GONE);
				}
			}
		});

		Button btn = (Button) dialog.findViewById(R.id.btn_finish);
		btn.setTextSize(Constants.FONT_SIZE_STD);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		btn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					text.setBackgroundResource(R.drawable.focused);
				} else {
					text.setBackground(null);
				}
			}
		});
		
		if (Constants.SETTING_REVERSE_SCREEN) {
			dialog.findViewById(R.id.view_root_relativelayout).setBackground(
					context.getResources().getDrawable(
							android.R.color.black));
			text.setTextColor(context.getResources().getColor(
					android.R.color.white));
//			scrollUp.setBackgroundColor(context.getResources().getColor(
//					android.R.color.darker_gray));
//			scrollUp.setTextColor(context.getResources().getColor(
//					android.R.color.white));
//			scrollDown.setBackgroundColor(context.getResources().getColor(
//					android.R.color.darker_gray));
//			scrollDown.setTextColor(context.getResources().getColor(
//					android.R.color.white));
			
			scrollUp.setBackgroundColor(context.getResources().getColor(
					R.color.bg_button));
			scrollUp.setTextColor(context.getResources().getColor(
					android.R.color.white));
			scrollDown.setBackgroundColor(context.getResources().getColor(
					R.color.bg_button));
			scrollDown.setTextColor(context.getResources().getColor(
					android.R.color.white));
		}else{
			dialog.findViewById(R.id.view_root_relativelayout).setBackground(
					context.getResources().getDrawable(
							android.R.color.white));
			text.setTextColor(context.getResources().getColor(
					android.R.color.black));
		}

		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize * 11 / 10);
		text.setText(message);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	private static boolean isScrollable(TextView textView) {
		int height = textView.getHeight();
		int scrollY = textView.getScrollY();
		int totalNumberOfLine = textView.getLineCount();
		int firstVisibleLineNumber = textView.getLayout().getLineForVertical(
				scrollY);
		int lastVisibleLineNumber = textView.getLayout().getLineForVertical(
				scrollY + height);
		if (lastVisibleLineNumber < totalNumberOfLine - 1) {
			return true;
		}
		return false;
	}

	protected static void scrollText(TextView textView, boolean isScrollDown) {
		Log.d("Utils", "textView isScrollDown = " + isScrollDown);
		int scrollTo = -1;
		int firstVisiblelineTop = -1;
		int lastVisiblelineTop = -1;
		int height = textView.getHeight();
		int scrollY = textView.getScrollY();
		int totalNumberOfLine = textView.getLineCount();
		int firstVisibleLineNumber = textView.getLayout().getLineForVertical(
				scrollY);
		int lastVisibleLineNumber = textView.getLayout().getLineForVertical(
				scrollY + height);

		int remainingLine = -1;

		firstVisiblelineTop = textView.getLayout().getLineTop(
				firstVisibleLineNumber);
		lastVisiblelineTop = textView.getLayout().getLineTop(
				lastVisibleLineNumber);

		if (isScrollDown) {
			Log.d("Utils", "totalNumberOfLine = " + totalNumberOfLine
					+ ", lastVisibleLineNumber = " + lastVisibleLineNumber);
			if (lastVisibleLineNumber < totalNumberOfLine - 1) {
				remainingLine = totalNumberOfLine - lastVisibleLineNumber;
				int tempScrollAmount = textView.getLineHeight() * remainingLine
						+ textView.getPaddingTop()
						+ textView.getPaddingBottom();
				if (tempScrollAmount < lastVisiblelineTop - firstVisiblelineTop) {
					/**
					 * added extra line height if there is some half line while
					 * scrolling
					 */
					scrollTo = firstVisiblelineTop + tempScrollAmount
							+ textView.getLineHeight();
				} else {
					scrollTo = lastVisiblelineTop;
				}

				textView.scrollTo(0, scrollTo);
			}
		} else {
			remainingLine = firstVisibleLineNumber;
			int tempScrollAmount = textView.getLineHeight() * remainingLine;
			if (tempScrollAmount < lastVisiblelineTop - firstVisiblelineTop) {
				scrollTo = 0;
			} else {
				scrollTo = firstVisiblelineTop
						- (lastVisiblelineTop - firstVisiblelineTop);
			}

			textView.scrollTo(0, scrollTo);
		}
	}

	public static String changeToLowerCase(String speechString) {
		if (speechString != null) {
			speechString.toLowerCase();
		}
		return speechString;
	}
}