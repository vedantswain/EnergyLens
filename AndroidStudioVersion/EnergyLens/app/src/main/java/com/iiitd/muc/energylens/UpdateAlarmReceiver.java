package com.iiitd.muc.energylens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class UpdateAlarmReceiver extends WakefulBroadcastReceiver{

	public AlarmManager uploaderAlarmMgr;
	public PendingIntent uploaderServicePendingIntent;
	public Intent uploaderServiceIntent;

	Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.context=context;
		String message = intent.getStringExtra("message");
		Log.v("ELSERVICES", "Main receiver got message: " + message);
		if(message!=null)
			if(message.contains("EnergyLensPlus.updateAlarm")){
				Intent uploaderIntent = new Intent(context, UploaderService.class);
				startWakefulService(context,uploaderIntent);
			}
	}

}
