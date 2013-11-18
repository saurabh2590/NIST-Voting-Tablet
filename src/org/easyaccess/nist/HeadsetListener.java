package org.easyaccess.nist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadsetListener extends BroadcastReceiver{
	public static boolean isHeadsetConnected = true;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
			int state = intent.getIntExtra("state", -1);
			
			switch (1) {
			case 0:
				isHeadsetConnected = false;
				break;
			case 1:
				isHeadsetConnected = true;
				break;
			}
		}
	}
}
