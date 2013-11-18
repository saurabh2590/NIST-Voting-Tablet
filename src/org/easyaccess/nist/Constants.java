package org.easyaccess.nist;

import java.io.File;

import android.os.Environment;

public class Constants {
	
	protected static final float TTS_SPEED_SLOW = 0.5f;
	protected static final float TTS_SPEED_STD = 1.0f;
	protected static final float TTS_SPEED_FAST = 1.5f;
	
	protected static final int FONT_SIZE_STD = 30;
	protected static final int FONT_SIZE_LARGE = 37;
	protected static final int FONT_SIZE_XLARGE = 44;
	protected static final int FONT_SIZE_XXLARGE = 51;
	
	protected static int DEFAULT_LANG_SETTING = 0;
	protected static int DEFAULT_SCAN_SPEED_SETTING = 0;
	protected static int DEFAULT_TTS_VOICE = 1;
	protected static boolean DEFAULT_TOUCH_PRESENT_SETTING = false;
	protected static boolean DEFAULT_REVERSE_SCREEN_SETTING = false;
	protected static boolean DEFAULT_SCAN_MODE_SETTING = false;
	
	// assigning the default value to the settings
	protected static int SETTING_LANGUAGE = DEFAULT_LANG_SETTING;
	protected static boolean SETTING_TOUCH_PRESENT = DEFAULT_TOUCH_PRESENT_SETTING;
	protected static int SETTING_FONT_SIZE = Constants.FONT_SIZE_STD;
	protected static boolean SETTING_REVERSE_SCREEN = DEFAULT_REVERSE_SCREEN_SETTING;
	protected static int SETTING_TTS_VOICE = DEFAULT_TTS_VOICE;
	protected static float SETTING_TTS_SPEED = Constants.TTS_SPEED_STD;
	protected static boolean SETTING_SCAN_MODE = DEFAULT_SCAN_MODE_SETTING; 
	protected static int SETTING_SCAN_MODE_SPEED = DEFAULT_SCAN_SPEED_SETTING;
	
	protected static final int MAX_FONT_SIZE = Constants.FONT_SIZE_XXLARGE;
	protected static final int MIN_FONT_SIZE = Constants.FONT_SIZE_STD;
	protected static final int FONT_DIFFERENCE = 7;
	protected static final int IMAGE_DIFFERENCE = 7;
	protected static final int MIN_VOLUME = 6;
	protected static final int V0LUME_DIFFERENCE = 3;
	
	protected static final int TTS_DATA_CHECK_CODE = 0;
	protected static final int QR_REQUEST_CODE = 1;
	public static final int REMAINING_BALLOT_SAVED = 2;
	
	protected static final String LINE_SEPRATOR = System.getProperty("line.separator");

	protected static final String SPACE = " ";
	protected static final String DOT_SPACE = ". ";
	protected static final String COMMA_SPACE = ", ";
	
	//constants to read the settings from qr code and nfc tags
	protected static final String LANGUAGE = "Language";
	protected static final String TOUCH_PRESENT = "TouchPresent";
	protected static final String FONT_SIZE = "FontSize";
	protected static final String TTS_VOICE = "TTS_Voice";
	protected static final String TTS_SPEED = "TTS_Speed";
	protected static final String SCAN_MODE = "Scan_Mode";
	protected static final String SCAN_MODE_SPEED = "Scan_Mode_Speed";
	protected static final String REVERSE_SCREEN = "Reverse_Screen";

	//Constants for reading Contest file
	protected static final String CONTESTS = "contests";
	protected static final String NAME = "name";
	protected static final String TYPE = "type";
	protected static final String ELECTORATE_SPECS = "electorateSpecifications";
	protected static final String OFFICE = "office";
	protected static final String WRITEIN = "writeIn";
	protected static final String NUMBER_VOTING_FOR = "numberVotingFor";
	protected static final String REFERENDUM_TITLE = "referendumTitle";
	protected static final String REFERENDUM_SUBTITLE = "referendumSubtitle";
	protected static final String REFERENDUM_INSTRUCTION = "referendumInstruction";
	protected static final String CANDIDATES = "candidates";
	protected static final String PARTY = "party";
	protected static final int ROW_TWO_LINE = 2;
	
	protected static final int NEXT = 0;
	protected static final int PREVIOUS = 1;
	protected static final int START = 2;
	protected static final int SUMMARY = 3;
	protected static final int DECREASE = 200;
	protected static final int INCREASE = 201;
	protected static final int DEFAULT = 202;
	protected static final int JUMP_FROM_CURRENT_ITEM = 300;
	protected static final int REACH_NEW_ITEM = 301;
	protected static final String ROW_INFO = "row";
	
	//request code for different activity
	protected static final int REQUEST_CODE_ALERTACTIVITY = 1003;
	protected static final int REQUESTCODE_CAPTUREACTIVITY = 1004;
	protected static final int REQUEST_CODE_WRITEIN_ACTIVITY = 1005;

	//constants for writein activity
	protected static final String WRITEIN_VALUE = "writein_value";
	protected static final String WRITEIN_POSITION = "writein_position";

	// Constants for reading Contest from shared preferences
	protected static final int DEFAULT_VOTE_PER_CANDIDATE = 1;
	protected static final String PREFERENCE_NAME = "ballot_info";
//	protected static final String PREFERENCE_BALLOT_OBJECT_KEY = "ballot_";
	protected static final String CONTEST_ROW = "contest_row";
	protected static final String CONTEST_POSITION = "contest_position";
	protected static final String CONTEST_OFFICE = "contest_office";
	protected static final String CONTEST_VOTE_PER_CANDIDATE = "vote_per_candidate";
	protected static final String CONTEST_IS_REFERENDUM = "is_referendum";
	protected static final String CONTEST_REFERENDUM_TITLE = "contest_referendum_title";
	protected static final String CONTEST_ELECTORATE_SPECS = "contest_electorate_specs";
	protected static final String CONTEST_REFERENDUM_SUBS = "contest_referendum_subs";
	protected static final String CANDIDATE_CHECK = "candidate_check";
	protected static final String CANDIDATE_NAME = "candidate_name";
	protected static final String CANDIDATE_PARTY = "candidate_party";
	protected static final String CANDIDATE_WRITEIN = "candidate_writein";
	protected static final String CONTEST_REFERENDUM_INSTRUCTION = "referendumInstruction";
	protected static final String CONTEST_REFERENDUM_RESPONSE = "referendumResponse";
	protected static final String CONTEST_REFERENDUM_VALUE = "referendumValue";

	//Constants for alertpage
	protected static final int TYPE_OVER_VOTE = 401;
	protected static final int TYPE_NOTICE_PAGE = 402;
	protected static final int TYPE_UNDER_VOTE = 403;
	protected static final int TYPE_LOAD_SUMMARY = 404;
	protected static final String ANNOUNCE = "announce_txt";
	protected static final String BALLOT_POSITION = "ballot_position";
	protected static final String ALERTPAGE_TITLE = "alert_page_title";
	protected static final String ALERTPAGE_TYPE = "alert_page_type";
	protected static final String ALERTPAGE_CONTENT = "alert_page_content";
	protected static final String ALERTPAGE_MESSAGE = "alert_page_message";
	protected static final String ALERTDIALOG_MESSAGE = "alert_dialog_message";
	protected static final String EXTRA_LOADCONTEST = "extra_for_loadcontest";
	
	//constants for saving
	protected static final int SAVE_BALLOT = 500;
	protected static final int SAVE_REFERENDUM = 501;
	protected static final int SAVE_WRITEIN = 502;

	//constants for referendum
	protected static final int REFERENDUM_NOT_ATTEMPT = 600;
	protected static final int REFERENDUM_ACCEPTED = 601;
	protected static final int REFERENDUM_DISCARDED = 602;

	//constants for boundary's
	protected static final int CHECK_PARAGRAPH_BOUNDARY = 700;
	protected static final int CHECK_SENTENCE_BOUNDARY = 701;
	protected static final int CHECK_CLAUSE_BOUNDARY = 702;
	protected static final int CHECK_WORD_BOUNDARY = 703;

	//constants for navigating forward/backward
	protected static final int REACH_BACKWARD = 801;
	protected static final int REACH_FORWARD = 802;

	//Constatns for chunk algo
	public static final int HYBRID_INITIALIZE = 900;
	protected static final int HYBRID_READ_START = 901;
	protected static final int HYBRID_READ_END = 902;

	protected static final long THREAD_SLEEP_TIME = 2000;

	protected static final String MESSAGE = "ttsMessage";

	protected static final String NUM_OF_BALLOT = "num_of_ballots";
	protected static final String LOAD_CONTEST = "load_contest";

	protected static final int MAX_CHARACTER_OFFSET = 249;

	protected static final String RETURN_TO_SUMMARY = "return_to_summary";

	protected static final String WRITEIN_INSTRUCTION = "writein_instruction";

	public static final String NEXT_LINE = System.getProperty("line.separator");
	
	public static final String TAB_SPACE = "/t";
	
	public static final String NIST_VOTING_PROTOTYPE_DIRECTORY = Environment.getExternalStorageDirectory() + 
			File.separator + "NIST_VOTING_PROTOTYPE";
	public static final String NIST_VOTING_PROTOTYPE_FILE_SP = "election_info_sp.txt";
	public static final String NIST_VOTING_PROTOTYPE_FILE_EN = "election_info_en.txt";
	public static final String NIST_VOTING_PROTOTYPE_VOTE_FILE = "ballot.txt";
}