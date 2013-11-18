package org.easyaccess.nist;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

public class ScannerActivity extends Activity implements OnInitListener{

	Context gContext = null;
	String gTTSOnStart = null;
	SpeakManager gTTS = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gContext = this;
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.setPackage("org.easyaccess.nist");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, Constants.REQUESTCODE_CAPTUREACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.TTS_DATA_CHECK_CODE) {
			if (resultCode == SpeakManager.Engine.CHECK_VOICE_DATA_PASS) {

				if (Constants.SETTING_TTS_VOICE == Constants.DEFAULT_TTS_VOICE) {
					gTTS = new SpeakManager(gContext, this, "com.svox.classic");
				} else {
					gTTS = new SpeakManager(gContext, this, "com.ivona.tts");
				}

				gTTSOnStart = getString(R.string.start_vote);
//				gTTSProgressHelper = new UtterenceProgressHelper(
//						ContestActivity.this);
//				gTTS.setOnUtteranceProgressListener(gTTSProgressHelper);
			} else {
				Intent ttsInstallIntent = new Intent();
				ttsInstallIntent
						.setAction(SpeakManager.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(ttsInstallIntent);
			}
		} else if (requestCode == Constants.REQUESTCODE_CAPTUREACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				String capturedQrValue = data.getStringExtra("SCAN_RESULT");
				// try {
				// setUserSettings(new JSONObject(capturedQrValue));
				setUserSettings(capturedQrValue);
				// } catch (JSONException e) {
				// e.printStackTrace();
				// }
			} else if (resultCode == RESULT_CANCELED) {
				if (data != null) {
					String capturedQrValue = data
							.getStringExtra("RESULT_CANCELED");
					Log.d("tushar", capturedQrValue);
					setUserSettings(capturedQrValue);
				} else {
				}
			}

			Intent intent = new Intent(this, ContestActivity.class);
			startActivity(intent);
			finish();
		}
	}

	private void setUserSettings(String capturedSetting) {
		String[] settings = capturedSetting.split(";");

		for (int i = 0; i < settings.length; i++) {
			switch (i) {
			case 0:
				Constants.SETTING_LANGUAGE = Integer.valueOf(settings[i]);
				if (Constants.SETTING_LANGUAGE != Constants.DEFAULT_LANG_SETTING) {
					Resources standardResources = this.getResources();
					AssetManager assets = standardResources.getAssets();
					DisplayMetrics metrics = standardResources
							.getDisplayMetrics();
					Configuration config = new Configuration(
							standardResources.getConfiguration());
					// config.locale = Locale.US;
					config.locale = new Locale("es", "ES");
					Resources defaultResources = new Resources(assets, metrics,
							config);
				}
				break;
			case 1:
				Constants.SETTING_TOUCH_PRESENT = Boolean.valueOf(settings[i]);
				break;
			case 2:
				Constants.SETTING_FONT_SIZE = Integer.valueOf(settings[i]);
				break;
			case 3:
				Constants.SETTING_TTS_VOICE = Integer.valueOf(settings[i]);
				break;
			case 4:
				Constants.SETTING_TTS_SPEED = Float.valueOf(settings[i]);
				break;
			case 5:
				Constants.SETTING_REVERSE_SCREEN = Boolean.valueOf(settings[i]);
				break;
			case 6:
				Constants.SETTING_SCAN_MODE = Boolean.valueOf(settings[i]);
				break;
			case 7:
				if (Constants.SETTING_SCAN_MODE) {
					Constants.SETTING_SCAN_MODE_SPEED = Integer
							.valueOf(settings[i]);
				}
				break;
			}
		}

		// try {
		// if (jsonObject.has(Constants.LANGUAGE)) {
		// Constants.SETTING_LANGUAGE =
		// Integer.valueOf(jsonObject.getString(Constants.LANGUAGE));
		// if(Constants.SETTING_LANGUAGE != Constants.DEFAULT_SETTING){
		// Resources standardResources = this.getResources();
		// AssetManager assets = standardResources.getAssets();
		// DisplayMetrics metrics = standardResources.getDisplayMetrics();
		// Configuration config = new
		// Configuration(standardResources.getConfiguration());
		// // config.locale = Locale.US;
		// config.locale = new Locale("es", "ES");
		// Resources defaultResources = new Resources(assets, metrics, config);
		// }
		// }
		// if (jsonObject.has(Constants.TOUCH_PRESENT)) {
		// Constants.SETTING_TOUCH_PRESENT =
		// Boolean.valueOf(jsonObject.getString(Constants.TOUCH_PRESENT));
		// }
		// if (jsonObject.has(Constants.FONT_SIZE)) {
		// Constants.SETTING_FONT_SIZE =
		// Integer.valueOf(jsonObject.getString(Constants.FONT_SIZE));
		// }
		// if (jsonObject.has(Constants.REVERSE_SCREEN)) {
		// Constants.SETTING_REVERESE_SCREEN =
		// Boolean.valueOf(jsonObject.getString(Constants.REVERSE_SCREEN));
		// }
		// if (jsonObject.has(Constants.TTS_VOICE)) {
		// Constants.SETTING_TTS_VOICE =
		// Integer.valueOf(jsonObject.getString(Constants.TTS_VOICE));
		// }
		// if (jsonObject.has(Constants.TTS_SPEED)) {
		// Constants.SETTING_TTS_SPEED =
		// Float.valueOf(jsonObject.getString(Constants.TTS_SPEED));
		// }
		// if (jsonObject.has(Constants.SCAN_MODE)) {
		// Constants.SETTING_SCAN_MODE =
		// Boolean.valueOf(jsonObject.getString(Constants.SCAN_MODE));
		// if(Constants.SETTING_SCAN_MODE){
		// if (jsonObject.has(Constants.SCAN_MODE_SPEED)) {
		// Constants.SETTING_SCAN_MODE_SPEED =
		// Integer.valueOf(jsonObject.getString(Constants.SCAN_MODE_SPEED));
		// }
		// }
		// }
		// } catch (JSONException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

//	private void resetPreferences() {
//		SharedPreferences preferences = getSharedPreferences(
//				Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
//		Editor editor = preferences.edit();
//		editor.clear();
//		editor.commit();
//	}

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
				speakWord(gTTSOnStart, null, true);
				gTTSOnStart = null;
			}
		} else if (status == SpeakManager.ERROR) {
			Toast.makeText(gContext, getString(R.string.failed),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public void speakWord(String word, HashMap<String, String> utteranceId, boolean shouldRepeating) {
		if (gTTS != null) {
			if (utteranceId != null) {
				gTTS.speak(word, SpeakManager.QUEUE_FLUSH, utteranceId, shouldRepeating);
			} else {
				gTTS.speak(word, SpeakManager.QUEUE_FLUSH, null, shouldRepeating);
			}
		}
	}

	@Override
	public void onBackPressed() {
	}
}