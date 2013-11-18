package org.easyaccess.nist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContestAdapter extends ArrayAdapter<Candidate> {
	private static final String TAG = "ContestAdapter";

	int gCustomResource = -1;
	int gMaxCandidateSelect = 1;
	int gTextViewResourceId = -1;

	Context gContext = null;
	ViewHolder viewHolder = null;
	ContestActivity gContextLoadContest = null;
	List<Candidate> gCandidateList = null;
	List<Candidate> gSelectedCandidateList = null;

	public ContestAdapter(Context context, int resource,
			int textViewResourceId, List<Candidate> listRow) {
		super(context, resource, textViewResourceId, listRow);
		gCustomResource = resource;
		gTextViewResourceId = textViewResourceId;
		gCandidateList = listRow;
		gContext = context;
		gContextLoadContest = (ContestActivity) context;
		gSelectedCandidateList = new ArrayList<Candidate>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) gContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(gCustomResource, parent, false);
			viewHolder = new ViewHolder();

			viewHolder.checkCandidate = (CheckBox) convertView
					.findViewById(R.id.cb_candidate_check);
			viewHolder.candidateName = (TextView) convertView
					.findViewById(gTextViewResourceId);
			viewHolder.candidateParty = (TextView) convertView
					.findViewById(R.id.tv_candidate_party);

			if (viewHolder.candidateParty.getVisibility() == View.GONE) {
				viewHolder.candidateParty.setVisibility(View.VISIBLE);
			}
			viewHolder.tvContainer = (LinearLayout) convertView
					.findViewById(R.id.tv_container);
			viewHolder.checkCandidate.setOnClickListener(clickListener);
			viewHolder.tvContainer.setOnTouchListener(touchListener);
			viewHolder.tvContainer.setOnClickListener(clickListener);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (Constants.SETTING_REVERSE_SCREEN) {
			viewHolder.candidateName.setTextColor(gContext.getResources()
					.getColor(android.R.color.white));
			viewHolder.candidateParty.setTextColor(gContext.getResources()
					.getColor(android.R.color.white));
		}

		viewHolder.position = position;
		viewHolder.isWriteIn = gCandidateList.get(position).isWriteIn;
		viewHolder.checkCandidate
				.setChecked(gCandidateList.get(position).candidateCheck);
		viewHolder.candidateName.setTextSize(TypedValue.COMPLEX_UNIT_SP,
				Constants.SETTING_FONT_SIZE);
		viewHolder.candidateParty.setTextSize(TypedValue.COMPLEX_UNIT_SP,
				Constants.SETTING_FONT_SIZE - Constants.FONT_DIFFERENCE);

		if (viewHolder.isWriteIn
				&& gCandidateList.get(position).candidateParty.length() > 0) {
			viewHolder.candidateName.setText(gContext
					.getString(R.string.writein));
			viewHolder.candidateParty
					.setText(gCandidateList.get(position).candidateParty);
		} else {
			viewHolder.candidateName
					.setText(gCandidateList.get(position).candidateName);
			viewHolder.candidateParty
					.setText(gCandidateList.get(position).candidateParty);
		}

		Log.d("tushar",
				"view holder values, gListViewRow.chkBox.isChecked() = "
						+ viewHolder.checkCandidate.isChecked()
						+ ", candidate marked = "
						+ viewHolder.candidateName.getText()
						+ ", candidate write in = " + viewHolder.isWriteIn);

		return convertView;
	}

	/**
	 * for touch
	 */
	OnTouchListener touchListener = new OnTouchListener() {
		int num_of_tap = -1;
		int last_tap_position = -1;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			changeToTouchMode();
			gContextLoadContest.isContestExplored = true;
			ViewGroup rowView = (ViewGroup) v.getParent();
			ViewHolder viewHolder = (ViewHolder) rowView.getTag();

			viewHolder.tvContainer.setBackground(gContext.getResources()
					.getDrawable(R.drawable.focused));

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Utils.downX = (int) event.getRawX();
				Utils.downY = (int) event.getRawY();
				
				gContextLoadContest.navigateToOtherItem(
						gContextLoadContest.gFocusPosition,
						Constants.JUMP_FROM_CURRENT_ITEM);
				String msg = "";

				if (last_tap_position == viewHolder.position) {
					num_of_tap++;

					if (num_of_tap == 3) {
						if (viewHolder.checkCandidate.isChecked()) {
							msg = gContext.getString(R.string.want_to_erase);
						} else {
							msg = gContext.getString(R.string.want_to_mark);
						}

						gContextLoadContest
								.saveBallot(gContextLoadContest.gBallotPosition);
						msg = msg + Constants.NEXT_LINE + Constants.NEXT_LINE
								+ gContext.getString(R.string.press)
								+ Constants.SPACE
								+ gContext.getString(R.string.close)
								+ Constants.SPACE
								+ gContext.getString(R.string.to_continue)
								+ Constants.DOT_SPACE;
//						Utils.showCustomDialog(gContextLoadContest, msg,
//								Constants.SETTING_FONT_SIZE);
					} else {
						msg = viewHolder.candidateName.getText().toString()
								+ (viewHolder.candidateParty.getText()
										.toString() != null
										&& viewHolder.candidateParty.getText()
												.length() > 0 ? Constants.LINE_SEPRATOR
										+ viewHolder.candidateParty.getText()
												.toString()
										: "");
//						if (Constants.SETTING_TOUCH_PRESENT) {
//							Utils.showCustomDialog(gContext, msg,
//									viewHolder.candidateName.getTextSize());
//						}
					}
				} else {
					num_of_tap = 1;
					last_tap_position = viewHolder.position;
					msg = viewHolder.candidateName.getText().toString()
							+ (viewHolder.candidateParty.getText().toString() != null
									&& viewHolder.candidateParty.getText()
											.length() > 0 ? (Constants.LINE_SEPRATOR + viewHolder.candidateParty
									.getText().toString()) : "");
//					if (Constants.SETTING_TOUCH_PRESENT) {
//						Utils.showCustomDialog(gContext, msg,
//								viewHolder.candidateName.getTextSize());
//					}
				}

				if (HeadsetListener.isHeadsetConnected) {
					String tts = msg + Constants.COMMA_SPACE;

					if (num_of_tap != 3) {
						if (viewHolder.candidateParty.getText().length() > 0) {
							if (viewHolder.isWriteIn) {
								tts = tts
										+ gContext
												.getString(R.string.candidate)
										+ Constants.COMMA_SPACE;
							}

							// String candidateParty = viewHolder.candidateParty
							// .getText().toString();
							tts = tts + Constants.COMMA_SPACE;
						}

						if (viewHolder.checkCandidate.isChecked()) {
							tts = tts + gContext.getString(R.string.is)
									+ Constants.SPACE
									+ gContext.getString(R.string.marked);
						} else {
							tts = tts + gContext.getString(R.string.is)
									+ Constants.SPACE
									+ gContext.getString(R.string.not_marked);
						}
					}

					gContextLoadContest.speakWord(tts, null, true);
				}

				if (num_of_tap == 3) {
					/**
					 * reset it here so that tts also know dialog appeared
					 */
					num_of_tap = 0;
				}
			} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
				gContextLoadContest.focusedListViewItemPosition = viewHolder.position;
				if (viewHolder.tvContainer != null) {
					viewHolder.tvContainer.setBackground(null);
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				if (viewHolder.tvContainer != null) {
					viewHolder.tvContainer.setBackground(null);
				}
			}
			return false;
		}
	};

	OnClickListener clickListener = new OnClickListener() {
		int num_of_tap = -1;
		int last_tap_position = -1;
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.cb_candidate_check:
				gContextLoadContest.isContestExplored = true;
				if (!gContextLoadContest.isButtonPressed) {
					gContextLoadContest.navigateToOtherItem(
							gContextLoadContest.gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gContextLoadContest.gFocusPosition = 0;
				}

				ViewGroup rowView = (ViewGroup) v.getParent();
				ViewHolder viewHolder = (ViewHolder) rowView.getTag();
				Candidate row = new Candidate();
				int position = -1;

				if (viewHolder.checkCandidate.isChecked()) {
					if (gSelectedCandidateList.size() < gMaxCandidateSelect) {

						row.candidateCheck = viewHolder.checkCandidate.isChecked();
						row.candidateName = viewHolder.candidateName.getText()
								.toString();
						row.candidateParty = viewHolder.candidateParty.getText()
								.toString();
						row.isWriteIn = viewHolder.isWriteIn;
						position = viewHolder.position;

						gCandidateList.set(position, row);
						gSelectedCandidateList.add(row);

						if (viewHolder.isWriteIn) {
							gContextLoadContest
									.saveBallot(gContextLoadContest.gBallotPosition);
							loadWriteIn(position);
						} else if (HeadsetListener.isHeadsetConnected) {
							String[] stringarry = gContextLoadContest.gBallotInstruction
									.getText().toString()
									.split(System.getProperty("line.separator"));
							String tts = gContext.getString(R.string.marked)
									+ gContext.getString(R.string.dot)
									+ Constants.SPACE
									+ row.candidateName
									+ Constants.SPACE
									+ gContext.getString(R.string.marked)
									+ gContext.getString(R.string.dot)
									+ Constants.SPACE
									+ gContext.getString(R.string.u_hav)
									+ Constants.SPACE
									+ gContext.getString(R.string.chkd)
									+ Constants.SPACE
									+ gSelectedCandidateList.size()
									+ Constants.SPACE
									+ gContext.getString(R.string.of)
									+ Constants.SPACE
									+ gMaxCandidateSelect
									+ Constants.SPACE
									+ gContext.getString(R.string.choices)
									+ Constants.SPACE
									+ gContext.getString(R.string.u_can_mak)
									+ gContext.getString(R.string.comma)
									+ Constants.SPACE
									+ stringarry[0]
									+ gContext.getString(R.string.dot)
									+ Constants.SPACE
									+ gContext.getString(R.string.u_hav)
									+ Constants.SPACE
									+ (gMaxCandidateSelect - gSelectedCandidateList
											.size()) + Constants.SPACE
									+ gContext.getString(R.string.more_choice);

							gContextLoadContest.speakWord(tts, null, true);
						}
					} else {
						viewHolder.checkCandidate.setChecked(false);

						position = viewHolder.position;
						row.isWriteIn = viewHolder.isWriteIn;
						row.candidateCheck = viewHolder.checkCandidate.isChecked();
						row.candidateName = viewHolder.candidateName.getText()
								.toString();
						row.candidateParty = viewHolder.candidateParty.getText()
								.toString();

						gCandidateList.set(position, row);

						String candidate = (gMaxCandidateSelect < 2) ? gContext
								.getString(R.string.candidate) : gContext
								.getString(R.string.candidates);
						List<String> ttsList = new ArrayList<String>();
						ttsList.add(gContext.getString(R.string.cant_choose_more)
								+ Constants.SPACE + gMaxCandidateSelect
								+ Constants.SPACE + candidate + Constants.SPACE
								+ gContext.getString(R.string.for_office)
								+ Constants.SPACE
								+ gContext.getString(R.string.want_to_change));
						ttsList.add(gContext.getString(R.string.slct_mark)
								+ Constants.SPACE
								+ gContext.getString(R.string.slct_another));
						ttsList.add(gContext.getString(R.string.go_back));

						gContextLoadContest
								.saveBallot(gContextLoadContest.gBallotPosition);
						loadAlertPage(Constants.TYPE_OVER_VOTE,
								gContext.getString(R.string.over_vote_page),
								ttsList, row.candidateName);
					}
				} else {
					position = viewHolder.position;
					row.isWriteIn = viewHolder.isWriteIn;
					row.candidateCheck = viewHolder.checkCandidate.isChecked();
					if (row.isWriteIn) {
						String writeInPost = genString(position);
						row.candidateName = gContext
								.getString(R.string.write_blank)
								+ Constants.SPACE
								+ writeInPost;
						row.candidateParty = "";
					} else {
						row.candidateParty = viewHolder.candidateParty.getText()
								.toString();
						row.candidateName = viewHolder.candidateName.getText()
								.toString();
					}

					Candidate removeCandidate = gCandidateList.get(position);
					Log.d("tushar", "remove candidate values "
							+ removeCandidate.candidateName + ", "
							+ removeCandidate.candidateParty + ", "
							+ removeCandidate.candidateCheck);

					Iterator<Candidate> it = gSelectedCandidateList.iterator();
					while (it.hasNext()) {
						Candidate candidate = it.next();
						Log.d("tushar", "fetched candidate values "
								+ candidate.candidateName + ", "
								+ candidate.candidateParty + ", "
								+ candidate.candidateCheck);
						// boolean isEquals = candidate.equals(removeCandidate);
						if (candidate.candidateCheck == removeCandidate.candidateCheck
								&& candidate.candidateName
										.equals(removeCandidate.candidateName)
								&& candidate.candidateParty
										.equals(candidate.candidateParty)) {
							boolean isEquals = true;
							Log.d("tushar", "is equals = " + isEquals);
							if (isEquals) {
								it.remove();
							}
						}
					}

					gCandidateList.set(position, row);

					ContestAdapter.this.notifyDataSetChanged();
					if (HeadsetListener.isHeadsetConnected) {
						String[] stringarry = gContextLoadContest.gBallotInstruction
								.getText().toString()
								.split(System.getProperty("line.separator"));

						String tts = gContext.getString(R.string.erase)
								+ gContext.getString(R.string.dot)
								+ Constants.SPACE
								+ gContext.getString(R.string.mrk_for)
								+ Constants.SPACE
								+ row.candidateName
								+ Constants.SPACE
								+ gContext.getString(R.string.erase_from_ballot)
								+ gContext.getString(R.string.dot)
								+ Constants.SPACE
								+ gContext.getString(R.string.u_hav)
								+ Constants.SPACE
								+ gContext.getString(R.string.chkd)
								+ Constants.SPACE
								+ gSelectedCandidateList.size()
								+ Constants.SPACE
								+ gContext.getString(R.string.of)
								+ Constants.SPACE
								+ gMaxCandidateSelect
								+ Constants.SPACE
								+ gContext.getString(R.string.choices)
								+ Constants.SPACE
								+ gContext.getString(R.string.u_can_mak)
								+ gContext.getString(R.string.comma)
								+ Constants.SPACE
								+ stringarry[0]
								+ Constants.SPACE
								+ gContext.getString(R.string.u_hav)
								+ Constants.SPACE
								+ (gMaxCandidateSelect - gSelectedCandidateList
										.size()) + Constants.SPACE
								+ gContext.getString(R.string.more_choice);

						gContextLoadContest.speakWord(tts, null, true);
					}
				}
				break;
			case R.id.tv_container:
				rowView = (ViewGroup) v.getParent();
				viewHolder = (ViewHolder) rowView.getTag();
				
				String msg = "";
				if (last_tap_position == viewHolder.position) {
					num_of_tap++;

					
					if (num_of_tap == 3) {
						if (viewHolder.checkCandidate.isChecked()) {
							msg = gContext.getString(R.string.want_to_erase);
						} else {
							msg = gContext.getString(R.string.want_to_mark);
						}

						gContextLoadContest
								.saveBallot(gContextLoadContest.gBallotPosition);
						msg = msg + Constants.NEXT_LINE + Constants.NEXT_LINE
								+ gContext.getString(R.string.press)
								+ Constants.SPACE
								+ gContext.getString(R.string.close)
								+ Constants.SPACE
								+ gContext.getString(R.string.to_continue)
								+ Constants.DOT_SPACE;
						Utils.showCustomDialog(gContextLoadContest, msg,
								Constants.SETTING_FONT_SIZE);
					} else {
						msg = viewHolder.candidateName.getText().toString()
								+ (viewHolder.candidateParty.getText()
										.toString() != null
										&& viewHolder.candidateParty.getText()
												.length() > 0 ? Constants.LINE_SEPRATOR
										+ viewHolder.candidateParty.getText()
												.toString()
										: "");
						if (Constants.SETTING_TOUCH_PRESENT) {
							Utils.showCustomDialog(gContext, msg,
									viewHolder.candidateName.getTextSize());
						}
					}
				} else {
					num_of_tap = 1;
					last_tap_position = viewHolder.position;
					msg = viewHolder.candidateName.getText().toString()
							+ (viewHolder.candidateParty.getText().toString() != null
									&& viewHolder.candidateParty.getText()
											.length() > 0 ? (Constants.LINE_SEPRATOR + viewHolder.candidateParty
									.getText().toString()) : "");
					if (Constants.SETTING_TOUCH_PRESENT) {
						Utils.showCustomDialog(gContext, msg,
								viewHolder.candidateName.getTextSize());
					}
				}

				if (num_of_tap == 3) {
					/**
					 * reset it here so that tts also know dialog appeared
					 */
					num_of_tap = 0;
				}
				break;
			}
		}

		private void loadWriteIn(int position) {
			gContextLoadContest.speakWord("", null, false);
			Log.d(TAG, "write in postion sent = " + position);
			List<String> message = new ArrayList<String>();
			message.add(gContextLoadContest.gBallotInstruction.getText()
					.toString());

			Intent intent = new Intent(gContext, WriteInBallotActivity.class);
			intent.putStringArrayListExtra(Constants.WRITEIN_INSTRUCTION,
					(ArrayList<String>) message);
			intent.putExtra(Constants.WRITEIN_POSITION, position);
			gContextLoadContest.startActivityForResult(intent,
					Constants.REQUEST_CODE_WRITEIN_ACTIVITY);
			gContextLoadContest.resetBallotVariable(false);
		}
	};

	private void loadAlertPage(int which_page, String title,
			List<String> message, String candidateName) {
		gContextLoadContest.resetBallotVariable(false);
		gContextLoadContest.speakWord("", null, false);
		Intent alertIntent = new Intent(gContext, AlertActivity.class);
		alertIntent.putExtra(Constants.ALERTPAGE_TYPE, which_page);
		alertIntent.putExtra(Constants.ALERTPAGE_TITLE, title);
		alertIntent.putStringArrayListExtra(Constants.ALERTPAGE_CONTENT,
				(ArrayList<String>) message);
		if (which_page == Constants.TYPE_OVER_VOTE) {
			alertIntent.putExtra(Constants.CANDIDATE_NAME, candidateName);
		}
		gContextLoadContest.startActivityForResult(alertIntent,
				Constants.REQUEST_CODE_ALERTACTIVITY);
	}

	protected void changeToTouchMode() {
		gContextLoadContest.navigateToOtherItem(
				gContextLoadContest.gFocusPosition,
				Constants.JUMP_FROM_CURRENT_ITEM);
		gContextLoadContest.gFocusPosition = 0;
	}

	protected String genString(int position) {
		int writeInVote = 0;
		String writeInPost = null;

		for (int i = 0; i < gCandidateList.size(); i++) {
			if (gCandidateList.get(i).isWriteIn) {
				writeInVote++;
			}
		}

		int numberOfCandidate = gCandidateList.size() - writeInVote;
		int relativePositionOfWritein = (position + 1) - numberOfCandidate;

		if (writeInVote > 1) {
			writeInPost = relativePositionOfWritein + Constants.SPACE
					+ gContext.getString(R.string.of) + Constants.SPACE
					+ writeInVote;
		} else {
			writeInPost = "";
		}

		return writeInPost;
	}

	class ViewHolder {
		TextView candidateName = null;
		CheckBox checkCandidate = null;
		TextView candidateParty = null;
		LinearLayout tvContainer = null;
		boolean isWriteIn = false;
		int position = -1;
	}
}