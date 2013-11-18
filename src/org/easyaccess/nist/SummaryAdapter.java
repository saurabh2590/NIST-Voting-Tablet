package org.easyaccess.nist;

import java.util.ArrayList;
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

public class SummaryAdapter extends ArrayAdapter<Contest> {

	SummaryActivity gContext = null;
	List<Contest> gSummaryRowList = null;
	int gTextViewResourceId = -1;
	int gCustomResource = -1;
	// ViewHolder viewHolder = null;
	LayoutInflater inflater = null;

	public SummaryAdapter(Context context, int resource,
			int textViewResourceId, List<Contest> summaryListItemRow) {
		super(context, resource, textViewResourceId, summaryListItemRow);
		gContext = (SummaryActivity) context;
		gCustomResource = resource;
		gTextViewResourceId = textViewResourceId;
		gSummaryRowList = summaryListItemRow;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Contest contest = gSummaryRowList.get(position);

		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
			// invalidate convertView if any
			// if((contest.isReferendum && viewHolder.checkboxes.size() != 1) ||
			// (contest.candidateList != null && viewHolder.checkboxes.size() !=
			// contest.vote_per_candidate)){
			if (viewHolder.checkboxes.size() != contest.vote_per_candidate) {
				convertView = null;
			}
		}

		// prepare the view
		if (convertView == null) {
			viewHolder = new ViewHolder();
			inflater = (LayoutInflater) gContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(gCustomResource, parent, false);
			convertView.setOnTouchListener(touchListener);
			convertView.setFocusable(false);
			contest = gSummaryRowList.get(position);
			// Log.d("tushar","ballot loading = " + position +
			// ", isReferendum = " + contest.isReferendum);

			if (contest.isReferendum) {
				viewHolder = loadReferendumSummary(contest, convertView);
			} else {
				viewHolder = loadBallotSummary(contest, convertView);
			}
			convertView.setTag(viewHolder);
		}

		// loading common data to view holder
		viewHolder.position = position;
		viewHolder.tvOffice.setTextSize(TypedValue.COMPLEX_UNIT_SP,
				Constants.SETTING_FONT_SIZE);
		if (Constants.SETTING_REVERSE_SCREEN) {
			viewHolder.tvOffice.setTextColor(gContext.getResources().getColor(
					android.R.color.white));
		}

		// loading specific data to the view holder
		if (contest.isReferendum) {
			viewHolder.tvOffice
					.setText(gSummaryRowList.get(position).contest_referendum_title);

			for (int i = 0; i < viewHolder.checkboxes.size(); i++) {
				viewHolder.tvCandidateName.get(i)
						.setTextSize(TypedValue.COMPLEX_UNIT_SP,
								Constants.SETTING_FONT_SIZE);

				if (Constants.SETTING_REVERSE_SCREEN) {
					viewHolder.tvCandidateName.get(i).setTextColor(
							gContext.getResources().getColor(
									android.R.color.white));
				}

				if (contest.contest_referendum_value == null) {
					setContestSkipped(viewHolder, i);
				} else {
					viewHolder.checkboxes.get(i).setChecked(true);
					viewHolder.tvCandidateName.get(i).setText(
							contest.contest_referendum_value);
				}
			}
		} else {
			viewHolder.tvOffice
					.setText(gSummaryRowList.get(position).contest_office);

			for (int i = 0; i < viewHolder.checkboxes.size(); i++) {
				viewHolder.tvCandidateName.get(i)
						.setTextSize(TypedValue.COMPLEX_UNIT_SP,
								Constants.SETTING_FONT_SIZE);

				if (Constants.SETTING_REVERSE_SCREEN) {
					viewHolder.tvCandidateName.get(i).setTextColor(
							gContext.getResources().getColor(
									android.R.color.white));
				}

				if (contest.candidateList.size() > i) {
					Log.d("tushar",
							"candidate name = "
									+ contest.candidateList.get(i).candidateName
									+ ", candidate checked = "
									+ contest.candidateList.get(i).candidateCheck
									+ ", is write in = "
									+ contest.candidateList.get(i).isWriteIn);
					if (contest.candidateList.get(i).candidateCheck) {
						viewHolder.checkboxes.get(i).setChecked(
								contest.candidateList.get(i).candidateCheck);
						if (contest.candidateList.get(i).isWriteIn) {
							viewHolder.tvCandidateName
									.get(i)
									.setText(
											contest.candidateList.get(i).candidateParty);
							// viewHolder.tvCandidateName.get(i).setText(contest.candidateList.get(i).candidateName);
						} else {
							viewHolder.tvCandidateName.get(i).setText(
									contest.candidateList.get(i).candidateName);
						}
					}
				} else {
					viewHolder = setContestSkipped(viewHolder, i);
				}
			}
		}
		return convertView;
	}

	private ViewHolder setContestSkipped(ViewHolder viewHolder, int i) {
		viewHolder.checkboxes.get(i).setChecked(false);
		viewHolder.tvCandidateName.get(i).setText(
				gContext.getString(R.string.contest_skipped));
		return viewHolder;
	}

	private ViewHolder loadBallotSummary(Contest contest, View convertView) {
		CheckBox chkBox = null;
		TextView tvCandidateName = null;
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.isReferendum = false;

		LinearLayout summaryViewRoot = (LinearLayout) convertView
				.findViewById(R.id.summary_row_root);

		viewHolder.tvOffice = (TextView) convertView
				.findViewById(gTextViewResourceId);
		viewHolder.tvOffice.setOnClickListener(clickListener);
		viewHolder.tvOffice.setOnTouchListener(touchListener);

		viewHolder.checkboxes = new ArrayList<CheckBox>();
		viewHolder.tvCandidateName = new ArrayList<TextView>();

		// Log.d("tushar","generating candidate list of ballot = " +
		// contest.contest_office + ", having candidatelist = " +
		// contest.candidateList.size());

		for (int i = 0; i < contest.vote_per_candidate; i++) {
			View candidateView = inflater.inflate(R.layout.ballot_choice_row,
					summaryViewRoot, false);
			chkBox = (CheckBox) candidateView
					.findViewById(R.id.cb_candidate_check);
			tvCandidateName = (TextView) candidateView
					.findViewById(R.id.tv_candidate_name);

			// adding listener
			chkBox.setOnClickListener(clickListener);
			tvCandidateName.setOnClickListener(clickListener);
			tvCandidateName.setOnTouchListener(touchListener);

			// adding item to list
			viewHolder.checkboxes.add(chkBox);
			viewHolder.tvCandidateName.add(tvCandidateName);

			// adding the candidate view below title
			summaryViewRoot.addView(candidateView, i + 1);
		}
		return viewHolder;
	}

	private ViewHolder loadReferendumSummary(Contest contest, View convertView) {
		CheckBox chkBox = null;
		TextView tvCandidateName = null;
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.isReferendum = true;
		LinearLayout summaryViewRoot = (LinearLayout) convertView
				.findViewById(R.id.summary_row_root);
		viewHolder.tvOffice = (TextView) convertView
				.findViewById(gTextViewResourceId);
		viewHolder.tvOffice.setOnClickListener(clickListener);
		viewHolder.tvOffice.setOnTouchListener(touchListener);
		viewHolder.checkboxes = new ArrayList<CheckBox>();
		viewHolder.tvCandidateName = new ArrayList<TextView>();

		for (int i = 0; i < contest.vote_per_candidate; i++) {
			View candidateView = inflater.inflate(R.layout.ballot_choice_row,
					summaryViewRoot, false);
			chkBox = (CheckBox) candidateView
					.findViewById(R.id.cb_candidate_check);
			tvCandidateName = (TextView) candidateView
					.findViewById(R.id.tv_candidate_name);

			// adding listener
			chkBox.setOnClickListener(clickListener);
			tvCandidateName.setOnClickListener(clickListener);
			tvCandidateName.setOnTouchListener(touchListener);

			// adding item to list
			viewHolder.checkboxes.add(chkBox);
			viewHolder.tvCandidateName.add(tvCandidateName);

			// adding the candidate view below title
			summaryViewRoot.addView(candidateView, i + 1);
		}
		return viewHolder;
	}

	OnTouchListener touchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.d("SummaryAdapter", "touchevent = " + event);
			boolean result = false;
			
			switch (v.getId()) {
			case R.id.tv_ballot:
				ViewHolder viewHolder = (ViewHolder) ((ViewGroup)v.getParent()).getTag();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Utils.downX = (int) event.getRawX();
					Utils.downY = (int) event.getRawY();
					
					if (gContext.gNavigateRight != null) {
						gContext.gNavigateRight.setVisibility(View.VISIBLE);
						gContext.isRightButtonVisible = true;
					}

					gContext.isHeadingTTSInterupted = true;
					gContext.navigateToOtherItem(gContext.gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gContext.gFocusPosition = 0;

					viewHolder.tvOffice.setBackground(gContext.getResources()
							.getDrawable(R.drawable.focused));

					String tts = gContext.getString(R.string.for_)
							+ Constants.SPACE
							+ viewHolder.tvOffice.getText().toString()
							+ Constants.SPACE + gContext.getString(R.string.u_hav)
							+ Constants.SPACE;

					List<Candidate> selectedCandidateList = new ArrayList<Candidate>();
					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(
								gContext.getResources().getDrawable(
										R.drawable.focused));

						Candidate candidate = new Candidate();
						candidate.candidateCheck = viewHolder.checkboxes.get(i)
								.isChecked();
						candidate.candidateName = viewHolder.tvCandidateName.get(i)
								.getText().toString();

						if (viewHolder.checkboxes.get(i).isChecked()) {
							selectedCandidateList.add(candidate);
						}
					}

					if (HeadsetListener.isHeadsetConnected) {
						if (selectedCandidateList.size() > 0) {
							tts = tts + gContext.getString(R.string.chosen)
									+ Constants.SPACE;

							for (int i = 0; i < selectedCandidateList.size(); i++) {
								tts = tts
										+ selectedCandidateList.get(i).candidateName
										+ Constants.COMMA_SPACE;
								if (i != selectedCandidateList.size() - 1) {
									tts = tts + gContext.getString(R.string.and)
											+ Constants.COMMA_SPACE;
								}
							}

							if (viewHolder.tvCandidateName.size()
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
							} else if (viewHolder.tvCandidateName.size()
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
									+ gContext
											.getString(R.string.want_to_change_vote)
									+ Constants.SPACE
									+ gContext
											.getString(R.string.touch_circle_left)
									+ Constants.DOT_SPACE;
						} else {
							if (viewHolder.isReferendum) {
								tts = getContext()
										.getString(
												R.string.you_have_not_cast_a_vote_on_the_referendum_titled)
										+ Constants.SPACE
										+ viewHolder.tvOffice.getText().toString()
										+ Constants.DOT_SPACE
										+ getContext()
												.getString(
														R.string.if_you_want_to_vote_on_this_referendum);
							} else {
								tts = tts
										+ Constants.SPACE
										+ gContext.getString(R.string.not)
										+ Constants.SPACE
										+ gContext.getString(R.string.made_choice)
										+ Constants.DOT_SPACE
										+ gContext
												.getString(R.string.if_u_want_to_vote);
							}
							tts = tts
									+ Constants.COMMA_SPACE
									+ gContext
											.getString(R.string.touch_circle_left);
						}
						gContext.speakWord(tts, null, true);
					}
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					viewHolder.tvOffice.setBackground(null);

					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(null);
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					viewHolder.tvOffice.setBackground(null);

					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(null);
					}
				}
				break;
			case R.id.tv_candidate_name:
				viewHolder = (ViewHolder) ((ViewGroup)v.getParent().getParent().getParent()).getTag();
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (gContext.gNavigateRight != null) {
						gContext.gNavigateRight.setVisibility(View.VISIBLE);
						gContext.isRightButtonVisible = true;
					}

					gContext.isHeadingTTSInterupted = true;
					gContext.navigateToOtherItem(gContext.gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gContext.gFocusPosition = 0;
					viewHolder.tvOffice.setBackground(gContext.getResources()
							.getDrawable(R.drawable.focused));

					String tts = gContext.getString(R.string.for_)
							+ Constants.SPACE
							+ viewHolder.tvOffice.getText().toString()
							+ Constants.SPACE + gContext.getString(R.string.u_hav)
							+ Constants.SPACE;

					List<Candidate> selectedCandidateList = new ArrayList<Candidate>();
					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(
								gContext.getResources().getDrawable(
										R.drawable.focused));

						Candidate candidate = new Candidate();
						candidate.candidateCheck = viewHolder.checkboxes.get(i)
								.isChecked();
						candidate.candidateName = viewHolder.tvCandidateName.get(i)
								.getText().toString();

						if (viewHolder.checkboxes.get(i).isChecked()) {
							selectedCandidateList.add(candidate);
						}
					}

					if (HeadsetListener.isHeadsetConnected) {
						if (selectedCandidateList.size() > 0) {
							tts = tts + gContext.getString(R.string.chosen)
									+ Constants.SPACE;

							for (int i = 0; i < selectedCandidateList.size(); i++) {
								tts = tts
										+ selectedCandidateList.get(i).candidateName
										+ Constants.COMMA_SPACE;
								if (i != selectedCandidateList.size() - 1) {
									tts = tts + gContext.getString(R.string.and)
											+ Constants.COMMA_SPACE;
								}
							}

							if (viewHolder.tvCandidateName.size()
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
							} else if (viewHolder.tvCandidateName.size()
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
									+ gContext
											.getString(R.string.want_to_change_vote)
									+ Constants.SPACE
									+ gContext
											.getString(R.string.touch_circle_left)
									+ Constants.DOT_SPACE;
						} else {
							if (viewHolder.isReferendum) {
								tts = getContext()
										.getString(
												R.string.you_have_not_cast_a_vote_on_the_referendum_titled)
										+ Constants.SPACE
										+ viewHolder.tvOffice.getText().toString()
										+ Constants.DOT_SPACE
										+ getContext()
												.getString(
														R.string.if_you_want_to_vote_on_this_referendum);
							} else {
								tts = tts
										+ Constants.SPACE
										+ gContext.getString(R.string.not)
										+ Constants.SPACE
										+ gContext.getString(R.string.made_choice)
										+ Constants.DOT_SPACE
										+ gContext
												.getString(R.string.if_u_want_to_vote);
							}
							tts = tts
									+ Constants.COMMA_SPACE
									+ gContext
											.getString(R.string.touch_circle_left);
						}
						gContext.speakWord(tts, null, true);
					}
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					viewHolder.tvOffice.setBackground(null);

					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(null);
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					viewHolder.tvOffice.setBackground(null);

					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(null);
					}
				}
				break;
			default:
				result = true;
				viewHolder = (ViewHolder) v.getTag();
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (gContext.gNavigateRight != null) {
						gContext.gNavigateRight.setVisibility(View.VISIBLE);
						gContext.isRightButtonVisible = true;
					}

					gContext.isHeadingTTSInterupted = true;
					gContext.navigateToOtherItem(gContext.gFocusPosition,
							Constants.JUMP_FROM_CURRENT_ITEM);
					gContext.gFocusPosition = 0;

					viewHolder.tvOffice.setBackground(gContext.getResources()
							.getDrawable(R.drawable.focused));

					String tts = gContext.getString(R.string.for_)
							+ Constants.SPACE
							+ viewHolder.tvOffice.getText().toString()
							+ Constants.SPACE + gContext.getString(R.string.u_hav)
							+ Constants.SPACE;

					List<Candidate> selectedCandidateList = new ArrayList<Candidate>();
					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(
								gContext.getResources().getDrawable(
										R.drawable.focused));

						Candidate candidate = new Candidate();
						candidate.candidateCheck = viewHolder.checkboxes.get(i)
								.isChecked();
						candidate.candidateName = viewHolder.tvCandidateName.get(i)
								.getText().toString();

						if (viewHolder.checkboxes.get(i).isChecked()) {
							selectedCandidateList.add(candidate);
						}
					}

					if (HeadsetListener.isHeadsetConnected) {
						if (selectedCandidateList.size() > 0) {
							tts = tts + gContext.getString(R.string.chosen)
									+ Constants.SPACE;

							for (int i = 0; i < selectedCandidateList.size(); i++) {
								tts = tts
										+ selectedCandidateList.get(i).candidateName
										+ Constants.COMMA_SPACE;
								if (i != selectedCandidateList.size() - 1) {
									tts = tts + gContext.getString(R.string.and)
											+ Constants.COMMA_SPACE;
								}
							}

							if (viewHolder.tvCandidateName.size()
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
							} else if (viewHolder.tvCandidateName.size()
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
									+ gContext
											.getString(R.string.want_to_change_vote)
									+ Constants.SPACE
									+ gContext
											.getString(R.string.touch_circle_left)
									+ Constants.DOT_SPACE;
						} else {
							if (viewHolder.isReferendum) {
								tts = getContext()
										.getString(
												R.string.you_have_not_cast_a_vote_on_the_referendum_titled)
										+ Constants.SPACE
										+ viewHolder.tvOffice.getText().toString()
										+ Constants.DOT_SPACE
										+ getContext()
												.getString(
														R.string.if_you_want_to_vote_on_this_referendum);
							} else {
								tts = tts
										+ Constants.SPACE
										+ gContext.getString(R.string.not)
										+ Constants.SPACE
										+ gContext.getString(R.string.made_choice)
										+ Constants.DOT_SPACE
										+ gContext
												.getString(R.string.if_u_want_to_vote);
							}
							tts = tts
									+ Constants.COMMA_SPACE
									+ gContext
											.getString(R.string.touch_circle_left);
						}
						gContext.speakWord(tts, null, true);
					}
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					viewHolder = (ViewHolder) v.getTag();
					viewHolder.tvOffice.setBackground(null);

					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(null);
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					viewHolder = (ViewHolder) v.getTag();
					viewHolder.tvOffice.setBackground(null);

					for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
						viewHolder.tvCandidateName.get(i).setBackground(null);
					}
				}
				break;
			}
			return result;
		}
	};

	OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.cb_candidate_check:
				CheckBox checkBox = (CheckBox) v;
				checkBox.toggle();

				ViewGroup rowView = (ViewGroup) v.getParent().getParent();
				ViewHolder viewHolder = (ViewHolder) rowView.getTag();
				String contestOffice = viewHolder.tvOffice.getText().toString();
				// Log.d("tushar","contest office fetched from the tag = " +
				// contestOffice);

				if (gSummaryRowList != null) {
					gContext.speakWord("", null, false);
					for (int i = 0; i < gSummaryRowList.size(); i++) {

						if (contestOffice.equalsIgnoreCase(gSummaryRowList
								.get(i).contest_office)
								|| contestOffice
										.equalsIgnoreCase(gSummaryRowList
												.get(i).contest_referendum_title)) {
							Intent intent = new Intent(gContext,
									ContestActivity.class);
							intent.putExtra(Constants.CONTEST_POSITION, i);
							intent.putExtra(Constants.RETURN_TO_SUMMARY, true);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							gContext.startActivity(intent);
							gContext.finish();
						}
					}
				}
				break;
			case R.id.tv_ballot:
				rowView = (ViewGroup) v.getParent();
				viewHolder = (ViewHolder) rowView.getTag();
				showTouchPresentDialog(viewHolder);
				break;
			case R.id.tv_candidate_name:
				rowView = (ViewGroup) v.getParent().getParent().getParent();
				viewHolder = (ViewHolder) rowView.getTag();
				showTouchPresentDialog(viewHolder);
				break;
			}
		}
	};

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	protected void showTouchPresentDialog(ViewHolder viewHolder) {
		String dialogText = viewHolder.tvOffice.getText().toString();

		List<Candidate> selectedCandidateList = new ArrayList<Candidate>();
		for (int i = 0; i < viewHolder.tvCandidateName.size(); i++) {
			Candidate candidate = new Candidate();
			candidate.candidateCheck = viewHolder.checkboxes.get(i).isChecked();
			candidate.candidateName = viewHolder.tvCandidateName.get(i)
					.getText().toString();
			dialogText = dialogText + Constants.LINE_SEPRATOR
					+ candidate.candidateName;

			if (viewHolder.checkboxes.get(i).isChecked()) {
				selectedCandidateList.add(candidate);
			}
		}

		if (Constants.SETTING_TOUCH_PRESENT) {
			Utils.showCustomDialog(gContext, dialogText,
					viewHolder.tvOffice.getTextSize());
		}
	}

	class ViewHolder {
		int position = -1;
		boolean isReferendum = false;
		TextView tvOffice = null;
		List<CheckBox> checkboxes = null;
		List<TextView> tvCandidateName = null;
	}
}