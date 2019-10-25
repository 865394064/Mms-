package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationLauncherReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("liuzepeng", "notificationLauncherCount");
		MessagingNotification.notificationLauncherCount(context);
	}

}
