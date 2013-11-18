package org.easyaccess.nist;

import java.util.HashMap;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;


public class SpeakManager extends TextToSpeech{
	private static final String TAG = "SpeakManager";
	String previousString = null;
	Context gContext = null;
	
	public SpeakManager(Context context, OnInitListener listener) {
		super(context, listener);
		gContext = context;
	}
	
	public SpeakManager(Context context, OnInitListener listener, String engine) {
		super(context, listener, engine);
		gContext = context;
	}

	public int speak(String text, int queueMode, HashMap<String, String> params, boolean shouldRepeat) {
//		Log.d(TAG, "text to speak = " + text);
		if(text.length() > 0 && shouldRepeat){
			if(previousString != null && previousString.equalsIgnoreCase(text)){
				Log.d(TAG, "repeating text = " + text);
				previousString = text;
				text = gContext.getString(R.string.repeating) + Constants.COMMA_SPACE + text;
			}else {
				previousString = text;
			}
		}
//		speak("", queueMode, params);
		return speak(text, queueMode, params);
	}
	
//	@Override
//	public int speak(String text, int queueMode, HashMap<String, String> params) {
//		Log.d(TAG, "text to speak = " + text);
//		if(text.length() > 0){
//			if(previousString != null && text.length() > 0 && previousString.equalsIgnoreCase(text)){
//				Log.d(TAG, "repeating text = " + text);
//				previousString = text;
//				text = gContext.getString(R.string.repeating) + Constants.COMMA_SPACE + text;
//			}else {
//				previousString = text;
//			}
//		}
//		return super.speak(text, queueMode, params);
//	}
}