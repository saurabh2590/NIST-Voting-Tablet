package org.easyaccess.nist;

import org.easyaccess.nist.R;
import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

//Nist crash form key
@ReportsCrashes(formKey = "dFJ1VUppM0ZUdlVMSlZlamxqZGRRYmc6MQ", socketTimeout = 30000, mode = ReportingInteractionMode.NOTIFICATION, resToastText = R.string.crash_toast_text, resNotifTickerText = R.string.crash_notif_ticker_text, resNotifTitle = R.string.crash_notif_title, resNotifText = R.string.crash_notif_text, resNotifIcon = android.R.drawable.stat_notify_error, resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, resDialogTitle = R.string.crash_dialog_title, resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, resDialogOkToast = R.string.crash_dialog_ok_toast)

public class MyApplication extends Application {
	@Override
	public void onCreate() {
		ACRA.init(this);
		super.onCreate();
	}
}