package org.easyaccess.nist;

import android.app.Activity;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

public class UtterenceProgressHelper extends UtteranceProgressListener {
	private static final String TAG = "UtterenceProgressHelper";

	public static final String UID_NEXT_BALLOT = "utteranceIdNextBallot";
	public static final String UID_PREVIOUS_BALLOT = "utteranceIdPreviousBallot";

	public static final String UID_HELP_SCREEEN = "utteranceIdHelpScreen";
	public static final String UID_BALLOT_END = "utteranceIdBallotActivity";
	public static final String UID_ALERT_ACTIVITY = "utteranceIdAlertActivity";
	public static final String UID_WRITE_IN_ACTIVITY = "utteranceIdWriteInActivity";
	public static final String UID_CONTEST_ACTIVITY = "utteranceIdContestActivity";
	public static final String UID_SUMMARY_ACTIVITY = "utteranceIdSummaryActivity";
	public static final String UID_CONTEST_ACTIVITY_CHUNK_READ = "utteranceIdContestActivityChunkRead";

	boolean isHybridChunkRead = false;// help to toggle the hybrid mode on/off
	boolean isChunkReadComplete = false;// help to repeat when up key pressed

	BallotEnd gBallotEnd = null;
	AlertActivity gAlertActivity = null;
	HelpScreen gHelpScreenActivity = null;
	ContestActivity gContestActivity = null;
	SummaryActivity gSummaryActivity = null;
	WriteInBallotActivity gWriteInBallotActivity = null;

	public UtterenceProgressHelper(Activity activity) {
		if (activity instanceof ContestActivity) {
			this.gContestActivity = (ContestActivity) activity;
		} else if (activity instanceof HelpScreen) {
			this.gHelpScreenActivity = (HelpScreen) activity;
		} else if (activity instanceof SummaryActivity) {
			this.gSummaryActivity = (SummaryActivity) activity;
		} else if (activity instanceof AlertActivity) {
			this.gAlertActivity = (AlertActivity) activity;
		} else if (activity instanceof BallotEnd) {
			this.gBallotEnd = (BallotEnd) activity;
		} else if (activity instanceof WriteInBallotActivity) {
			this.gWriteInBallotActivity = (WriteInBallotActivity) activity;
		}
	}

	@Override
	public void onDone(String utteranceId) {
		Log.d(TAG, "utteranceId = " + utteranceId);

		/**
		 * check for cancelation of hybrid mode when reading
		 */
		if (utteranceId.equals(UID_HELP_SCREEEN) && isHybridChunkRead) {
			gHelpScreenActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!gHelpScreenActivity.isHeadingTTSInterupted) {
						gHelpScreenActivity.navigateToOtherItem(
								gHelpScreenActivity.gFocusPosition,
								Constants.JUMP_FROM_CURRENT_ITEM);
						gHelpScreenActivity.gFocusPosition++;
						gHelpScreenActivity.navigateToOtherItem(
								gHelpScreenActivity.gFocusPosition,
								Constants.REACH_NEW_ITEM);
					}
				}
			});
		} else if (utteranceId.equals(UID_ALERT_ACTIVITY)) {
			gAlertActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "gAlertActivity.isHeadingTTSInterupted = "
							+ gAlertActivity.isHeadingTTSInterupted);
					if (!gAlertActivity.isHeadingTTSInterupted) {
						gAlertActivity.navigateToOtherItem(
								gAlertActivity.gFocusPosition,
								Constants.JUMP_FROM_CURRENT_ITEM);
						gAlertActivity.gFocusPosition++;
						gAlertActivity.navigateToOtherItem(
								gAlertActivity.gFocusPosition,
								Constants.REACH_NEW_ITEM);
					}
				}
			});
		} else if (utteranceId.equals(UID_BALLOT_END)) {
			gBallotEnd.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!gBallotEnd.isHeadingTTSInterupted) {
						gBallotEnd.navigateToOtherItem(
								gBallotEnd.gFocusPosition,
								Constants.JUMP_FROM_CURRENT_ITEM);
						gBallotEnd.gFocusPosition++;
						gBallotEnd.navigateToOtherItem(
								gBallotEnd.gFocusPosition,
								Constants.REACH_NEW_ITEM);
					}
				}
			});
		} else if (utteranceId.equals(UID_WRITE_IN_ACTIVITY)) {
			gWriteInBallotActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG,
							"gWriteInBallotActivity.isHeadingTTSInterupted = "
									+ gWriteInBallotActivity.isHeadingTTSInterupted);
					if (!gWriteInBallotActivity.isHeadingTTSInterupted) {
						gWriteInBallotActivity.navigateToOtherItem(
								gWriteInBallotActivity.gFocusPosition,
								Constants.JUMP_FROM_CURRENT_ITEM);
						gWriteInBallotActivity.gFocusPosition++;
						gWriteInBallotActivity.navigateToOtherItem(
								gWriteInBallotActivity.gFocusPosition,
								Constants.REACH_NEW_ITEM);
					}
				}
			});
		} else if (utteranceId.equals(UID_SUMMARY_ACTIVITY)) {
			gSummaryActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!gSummaryActivity.isHeadingTTSInterupted) {
						gSummaryActivity.navigateToOtherItem(
								gSummaryActivity.gFocusPosition,
								Constants.JUMP_FROM_CURRENT_ITEM);
						gSummaryActivity.gFocusPosition++;
						gSummaryActivity.navigateToOtherItem(
								gSummaryActivity.gFocusPosition,
								Constants.REACH_NEW_ITEM);
						/**
						 * uncomment this block of code for showing right key
						 * when subtitle of the summary screen is reading also
						 * uncomment the block of code in the summary activity,
						 * onkey down,
						 */
						// if(!gSummaryActivity.isUpKeyPressed &&
						// !gSummaryActivity.isRightButtonVisible){
						// gSummaryActivity.gNavigateRight.setVisibility(View.VISIBLE);
						// gSummaryActivity.isRightButtonVisible = true;
						// }
					}
				}
			});
		} else if (utteranceId.equals(UID_CONTEST_ACTIVITY)) {
			gContestActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "gContestActivity.gFocusPosition = "
							+ gContestActivity.gFocusPosition);
					if (gContestActivity.gFocusPosition == 1
							&& !gContestActivity.isHeadingTTSInterupted) {
						gContestActivity.isReadBallotDetail = true;
						gContestActivity.navigateToOtherItem(
								gContestActivity.gFocusPosition,
								Constants.JUMP_FROM_CURRENT_ITEM);
						gContestActivity.gFocusPosition++;
						gContestActivity.navigateToOtherItem(
								gContestActivity.gFocusPosition,
								Constants.REACH_NEW_ITEM);
					}
				}
			});
		} else if (utteranceId.equals(UID_NEXT_BALLOT)) {
			gContestActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "loading next ballot from progress helper");
					gContestActivity.loadNextBallot();
				}
			});
		} else if (utteranceId.equals(UID_PREVIOUS_BALLOT)) {
			gContestActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "loading previous ballot from progress helper");
					gContestActivity.loadPreviousBallot();
				}
			});
		} else if (utteranceId.equals(UID_CONTEST_ACTIVITY_CHUNK_READ)) {
			gContestActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						isChunkReadComplete = true;
						Log.d(TAG, "gContestActivity.gFocusPosition = "
								+ gContestActivity.gFocusPosition
								+ "gContestActivity.isHeadingTTSInterupted"
								+ gContestActivity.isHeadingTTSInterupted);
						if (isHybridChunkRead) {
							gContestActivity.navigateToOtherItem(
									gContestActivity.gFocusPosition,
									Constants.JUMP_FROM_CURRENT_ITEM);

							if (gContestActivity.gContestAdapter == null) {
								gContestActivity.bufferNextChunk = true;
							}

							Thread.sleep(500);
							gContestActivity.gFocusPosition++;
							if (gContestActivity.gFocusPosition == ((gContestActivity.gContestAdapter == null) ? 13 + gContestActivity.gChunkList
									.size()
									: 13 + gContestActivity.gContestAdapter
											.getCount())) {
								gContestActivity.gFocusPosition = 1;
							}

							gContestActivity.navigateToOtherItem(
									gContestActivity.gFocusPosition,
									Constants.REACH_NEW_ITEM);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (Exception e) {
					}
				}
			});
		}
	}

	@Override
	public void onError(String utteranceId) {
	}

	@Override
	public void onStart(String utteranceId) {
		if (utteranceId.equals(UID_CONTEST_ACTIVITY_CHUNK_READ)) {
			isChunkReadComplete = false;
		}
	}
}