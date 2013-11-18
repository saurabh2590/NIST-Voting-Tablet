package org.easyaccess.nist;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class CustomDialog extends DialogFragment{
	Context gContext = null;
	
	static CustomDialog newInstance(Context gContext, ArrayList<String> msgList){
		CustomDialog cstmDialog = new CustomDialog();
		Bundle bundle = new Bundle();
		bundle.putStringArrayList(Constants.ALERTDIALOG_MESSAGE, msgList);
		cstmDialog.setArguments(bundle);
		cstmDialog.gContext = gContext;
		return cstmDialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		List<String> dialogMessage = this.getArguments().getStringArrayList(Constants.ALERTDIALOG_MESSAGE);
		String msg = null;
		for (int i = 0; i < dialogMessage.size(); i++) {
			if (i == dialogMessage.size() - 1) {
				msg = msg + dialogMessage.get(i);
			} else {
				msg = msg + dialogMessage.get(i) + Constants.NEXT_LINE
						+ Constants.NEXT_LINE;
			}
		}
		
		AlertDialog dialog = new AlertDialog.Builder(gContext)
			.setMessage(msg)
			.setPositiveButton(getString(R.string.dialog_ok), null).create();

		return dialog;
	}
}