package org.easyaccess.nist;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class PreferencActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	Context gContext = null;
	NfcAdapter nfcAdapter = null;
	Tag detectedTag = null;
	IntentFilter[] readTagFilters;
	PendingIntent pendingIntent;
	ListPreference languageSelect = null, fontSize = null, synthesizerVoice = null, synthesizerSpeed = null, scanSpeed = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLsSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		gContext = PreferencActivity.this;
		
		// inflate the preference screen
		addPreferencesFromResource(R.xml.prefs);
	
		languageSelect = (ListPreference) findPreference(getString(R.string.key_lang));
		fontSize = (ListPreference) findPreference(getString(R.string.key_font_size));
		synthesizerVoice = (ListPreference) findPreference(getString(R.string.key_synthesizer_voice));
		synthesizerSpeed = (ListPreference) findPreference(getString(R.string.key_synthesizer_speed));
		scanSpeed = (ListPreference) findPreference(getString(R.string.key_scan_mode_speed));
		
		languageSelect.setSummary(languageSelect.getEntry());
		fontSize.setSummary(fontSize.getEntry());
		synthesizerVoice.setSummary(synthesizerVoice.getEntry());
		synthesizerSpeed.setSummary(synthesizerSpeed.getEntry());
		scanSpeed.setSummary(scanSpeed.getEntry());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if(key.equals(R.string.key_lang)){
			languageSelect.setSummary(languageSelect.getEntry());
		}else if (key.equals(R.string.key_font_size)) {
			fontSize.setSummary(fontSize.getEntry());
		}else if (key.equals(R.string.key_synthesizer_voice)) {
			synthesizerVoice.setSummary(synthesizerVoice.getEntry());
		}else if (key.equals(R.string.key_synthesizer_speed)) {
			synthesizerSpeed.setSummary(synthesizerSpeed.getEntry());
		}else if (key.equals(R.string.key_scan_mode_speed)) {
			scanSpeed.setSummary(scanSpeed.getEntry());
		}
	}
}