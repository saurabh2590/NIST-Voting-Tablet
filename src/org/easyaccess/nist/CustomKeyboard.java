package org.easyaccess.nist;

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class CustomKeyboard {
	// Activity which show the keyboard
	EditText edittext = null;
	Runnable runnable = null;
	private Activity gHostActivity = null;
	private KeyboardView gKeyboardView = null;
	StringBuilder wordBuilder = new StringBuilder();
	StringBuilder deletedWord = new StringBuilder();
	
	//key handler
	private OnKeyboardActionListener gOnKeyboardActionListener = new OnKeyboardActionListener() {
        public final static int CodeDelete   = -5; // Keyboard.KEYCODE_DELETE
        public final static int CodeCancel   = -3; // Keyboard.KEYCODE_CANCEL
        public final static int CodeDone   = -4; // Keyboard.KEYCODE_DONE
        
        public final static int CodePrev     = 55000;
        public final static int CodeAllLeft  = 55001;
        public final static int CodeLeft     = 55002;
        public final static int CodeRight    = 55003;
        public final static int CodeAllRight = 55004;
        public final static int CodeNext     = 55005;
        public final static int CodeClear    = 55006;
		
		@Override
		public void swipeUp() {
		}
		
		@Override
		public void swipeRight() {
		}
		
		@Override
		public void swipeLeft() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void swipeDown() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onText(CharSequence text) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onRelease(int primaryCode) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onPress(int primaryCode) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			View focusCurrent = gHostActivity.getWindow().getCurrentFocus();
            if( focusCurrent==null || focusCurrent.getClass()!=EditText.class ) return;
            edittext = (EditText) focusCurrent;
            
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            
            if( primaryCode == CodeCancel ) {
                hideCustomKeyboard();
                if(HeadsetListener.isHeadsetConnected){
                	((WriteInBallotActivity)gHostActivity).speakWord(gHostActivity.getString(R.string.cstm_kyboard_hidn), null, false);
                }
            } else if( primaryCode == CodeDelete ) {
//            	char[] character = new char[1];
//            	
//            	if(editable!=null && start>1){
//            		editable.getChars(start-1, start, character, 0);
//            		if(character[0] != 32){
//            			deletedWord.append(character[0]);
//            			Log.d("tushar","last character 1 = " + character[0]);
//            			((WriteInBallotActivity)gHostActivity).speakWord(character[0]  + Constants.SPACE + gHostActivity.getString(R.string.deleted), null, false);
//            		}else{
//            			if(deletedWord.length() > 0){
//            				((WriteInBallotActivity)gHostActivity).speakWord(deletedWord.reverse().toString()  + Constants.SPACE + gHostActivity.getString(R.string.deleted), null, false);
//            				deletedWord = new StringBuilder();
//            			}else{
//            				((WriteInBallotActivity)gHostActivity).speakWord(gHostActivity.getString(R.string.space)  + Constants.SPACE + gHostActivity.getString(R.string.deleted), null, false);
//            			}
//            		}
//            	}else if (editable!=null && start == 1){
//            		editable.getChars(start-1, start, character, 0);
//            		if(character[0] != 32){
//            			deletedWord.append(character[0]);
//            			Log.d("tushar","last character = " + character[0]);
//            			Log.d("tushar","deleted word buffer = " + deletedWord.toString());
//            			((WriteInBallotActivity)gHostActivity).speakWord(deletedWord.reverse().toString()  + Constants.SPACE + gHostActivity.getString(R.string.deleted), null, false);
//            		}else{
//                		if(deletedWord.length() > 0){
//                			Log.d("tushar","deleted word buffer = " + deletedWord.toString());
//            				((WriteInBallotActivity)gHostActivity).speakWord(deletedWord.reverse().toString()  + Constants.SPACE + gHostActivity.getString(R.string.deleted), null, false);
//            				deletedWord = new StringBuilder();
//            			}else{
//            				((WriteInBallotActivity)gHostActivity).speakWord(gHostActivity.getString(R.string.space)  + Constants.SPACE + gHostActivity.getString(R.string.deleted), null, false);
//            			}
//            		}
//            	}else if(start == 0){
//        			((WriteInBallotActivity)gHostActivity).speakWord(gHostActivity.getString(R.string.nthing_to_dlt), null, false);
//            	}
//            	
                if( editable!=null && start>0 ) {
                	editable.delete(start - 1, start);
                //	wordBuilder.delete(start-1, start);
                }
                
            } else if( primaryCode==CodeClear ) {
                if( editable!=null ) editable.clear();
            } else if( primaryCode==CodeLeft ) { //go to left
                if( start>0 ) edittext.setSelection(start - 1);
            } else if( primaryCode==CodeRight ) {//go to right
                if (start < edittext.length()) edittext.setSelection(start + 1);
            } else if( primaryCode==CodeAllLeft ) {//go to extreme left
                edittext.setSelection(0);
            } else if( primaryCode==CodeAllRight ) {//go to extreme right
                edittext.setSelection(edittext.length());
            } else if( primaryCode==CodeDone) {// Done
                Intent intent  = new Intent();
        		intent.putExtra(Constants.WRITEIN_POSITION, ((WriteInBallotActivity)gHostActivity).gWriteInBallotPosition);
        		intent.putExtra(Constants.WRITEIN_VALUE, ((WriteInBallotActivity)gHostActivity).gEditText.getText().toString());		
        		((WriteInBallotActivity)gHostActivity).setResult(Activity.RESULT_OK,intent);
        		Log.d("tushar","write in postion sent back to contest activity = " + ((WriteInBallotActivity)gHostActivity).gWriteInBallotPosition 
        				+ ", write in value = " + ((WriteInBallotActivity)gHostActivity).gEditText.getText());
        		((WriteInBallotActivity)gHostActivity).finish();
        		
//                edittext.getText();
//                hideCustomKeyboard();
//                if(HeadsetListener.isHeadsetConnected){
//                	((WriteInBallotActivity)gHostActivity).speakWord(gHostActivity.getString(R.string.cstm_kyboard_hidn));
//                }
    			/**
    			 * clear the handler message queue
    			 * ensure that only one runnable is in the handler message queue
    			 */
    			((WriteInBallotActivity)gHostActivity).handler.removeCallbacksAndMessages(null);
            } else if( primaryCode==CodePrev ) {
                View focusNew= edittext.focusSearch(View.FOCUS_BACKWARD);
                if( focusNew!=null ) focusNew.requestFocus(); 
            } else if( primaryCode==CodeNext ) {
                View focusNew= edittext.focusSearch(View.FOCUS_FORWARD);
                if( focusNew!=null ) focusNew.requestFocus(); 
            } else { // insert character
                
            	editable.insert(start, Character.toString((char) primaryCode));
//            	if(!((WriteInBallotActivity)gHostActivity).isContestExplored){
//            		((WriteInBallotActivity)gHostActivity).isContestExplored = true;
//        		}
//        		
//            	((WriteInBallotActivity)gHostActivity).speakWord(String.valueOf((char) primaryCode), null, false);
//            //	wordBuilder.append((char) primaryCode);
//            	
//                if(primaryCode == 32){
//                	String[] stringArray = editable.toString().split(Constants.SPACE);
//                	if(stringArray.length > 0){
//                		((WriteInBallotActivity)gHostActivity).speakWord(gHostActivity.getString(R.string.space) 
//                				+ gHostActivity.getString(R.string.dot) + Constants.SPACE + stringArray[stringArray.length-1], null, false);
//                	}
//                }
//                deletedWord = new StringBuilder();
//              
//                if(edittext.getText().toString().length() > 0){
//                    runnable = new Runnable() {
//        				@Override
//        				public void run() {
//        					char[] charArray = edittext.getText().toString().toCharArray();
//        					
//        					String enteredText = "";
//        					for(int i = 0; i<charArray.length; i++){
//        						if(charArray[i] == ' '){
//        							enteredText = enteredText + gHostActivity.getString(R.string.space);
//        						}else{
//        							enteredText = enteredText + charArray[i];
//        						}
//            					
//        						
//        						if(i < charArray.length){
//        							enteredText = enteredText + Constants.COMMA_SPACE; 
//        						}
//        					}
//        					
//        					((WriteInBallotActivity)gHostActivity).speakWord(gHostActivity.getString(R.string.u_have_typed)
//        							+ 	Constants.COMMA_SPACE + enteredText, null
//        							, false);
//        				}
//        			};
//        			
//        			/**
//        			 * clear handler message queue
//        			 * ensure that only one runnable is in the handler message queue
//        			 */
//        			((WriteInBallotActivity)gHostActivity).handler.removeCallbacksAndMessages(null);
//                }
//                
//    			/**
//    			 * clear the handler message queue
//    			 */
//    			((WriteInBallotActivity)gHostActivity).handler.postDelayed(runnable, 3000);
            }
		}
	};

    public CustomKeyboard(Activity host, int viewid, int layoutid) {
        gHostActivity= host;
        gKeyboardView= (KeyboardView)gHostActivity.findViewById(viewid);
        gKeyboardView.setKeyboard(new Keyboard(gHostActivity, layoutid));
        gKeyboardView.setPreviewEnabled(false); // NOTE Do not show the preview balloons
        gKeyboardView.setOnKeyboardActionListener(gOnKeyboardActionListener);
        // Hide the standard keyboard initially
        gHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    
    /** Returns whether the CustomKeyboard is visible. */
    public boolean isCustomKeyboardVisible() {
        return gKeyboardView.getVisibility() == View.VISIBLE;
    }
    
    /** Make the CustomKeyboard visible, and hide the system keyboard for view v. */
    public void showCustomKeyboard( View v ) {
        gKeyboardView.setVisibility(View.VISIBLE);
        gKeyboardView.setEnabled(true);
        if( v!=null ) ((InputMethodManager)gHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    
    /** Make the CustomKeyboard invisible. */
    public void hideCustomKeyboard() {
        gKeyboardView.setVisibility(View.GONE);
        gKeyboardView.setEnabled(false);
    }

    public void registerEditText(int resid) {
        // Find the EditText 'resid'
        EditText edittext= (EditText)gHostActivity.findViewById(resid);
        
        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override 
            public void onFocusChange(View v, boolean hasFocus) {
                if( hasFocus ) showCustomKeyboard(v); else hideCustomKeyboard(); 
            }
        });
        
        edittext.setOnClickListener(new OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override 
            public void onClick(View v) {
                showCustomKeyboard(v);
            }
        });
        
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        edittext.setOnTouchListener(new OnTouchListener() {
            @Override 
            public boolean onTouch(View v, MotionEvent event) {
        		((WriteInBallotActivity)gHostActivity).isHeadingTTSInterupted = true;
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                
                //added
                ((WriteInBallotActivity)gHostActivity).navigateToOtherItem(((WriteInBallotActivity)gHostActivity).gFocusPosition, Constants.JUMP_FROM_CURRENT_ITEM);
                ((WriteInBallotActivity)gHostActivity).gFocusPosition = 3;
                
                // make the edittext focusable
                edittext.requestFocus();
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }
}