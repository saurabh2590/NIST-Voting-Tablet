package org.easyaccess.nist;

import java.util.ArrayList;

public class Contest {
	int contest_position = -1;
	int vote_per_candidate = Constants.DEFAULT_VOTE_PER_CANDIDATE;
	boolean isReferendum = false;
	
	String contest_type = null;
	String contest_office = null;
	String contest_electorate_specs = null;
	ArrayList<Candidate> candidateList = null;
	
	String contest_referendum_title = null;
	String contest_referendum_subs = null;
	String contest_referendum_value = null;
	String contest_referendum_instruction = null;
	int contest_referendum_response = Constants.REFERENDUM_NOT_ATTEMPT;
}