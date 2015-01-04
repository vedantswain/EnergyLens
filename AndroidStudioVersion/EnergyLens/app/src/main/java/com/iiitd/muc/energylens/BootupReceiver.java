package com.iiitd.muc.energylens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BootupReceiver extends BroadcastReceiver {

	Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext=context;
		Log.v("ELSERVICES", "Boot-up received");
		
		SharedPreferences trainingPref = context.getSharedPreferences(Common.EL_PREFS,0);
		Common.changeTrainingStatus(trainingPref.getInt("TRAINING_STATUS", 0));
		Common.changeLabel(trainingPref.getString("LABEL","none"));
		Common.changeLocation(trainingPref.getString("LOCATION", "none"));
		Common.changePrefix(trainingPref.getString("FILE_PREFIX", ""));
		Common.changeTrainingCount(trainingPref.getInt("TRAINING_COUNT", 0));
		if(Common.TRAINING_COUNT>0)
			toggleServiceMessage("startServices from Bootup");
	}


	private void toggleServiceMessage(String message){
		Intent intent = new Intent();
		intent.setAction("EnergyLensPlus.toggleService");
		  // add data
		  intent.putExtra("message", message);

		  Log.v("ELSERVICES", "Broadcast from Wifi to Main receiver");
		 mContext.sendBroadcast(intent);
	}

}
