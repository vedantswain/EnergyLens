package com.iiitd.muc.energylens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

public class ToggleServiceReceiver extends BroadcastReceiver {

	static public AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,battAlarmMgr,uploaderAlarmMgr;
	static public PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,
	lightServicePendingIntent,magServicePendingIntent,battServicePendingIntent,uploaderServicePendingIntent;
	static public Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,
            battServiceIntent,uploaderServiceIntent;
    Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		 // Extract data included in the Intent
		this.context=context;
	    String message = intent.getStringExtra("message");
	    Log.v("ELSERVICES", "Main receiver got message: " + message);
        SharedPreferences sharedPref = context.getSharedPreferences(Common.EL_PREFS,0);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(message.contains("startServices")) {
            editor.putBoolean("isCollecting",true);
            editor.commit();
            start();
        }
	    else if(message.contains("stopServices"))
			try {
                editor.putBoolean("isCollecting",false);
                editor.commit();
                stop();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void start(){
		Log.v("ELSERVICES","Service started");

		axlServiceIntent = new Intent(context, AxlService.class);
		axlServicePendingIntent = PendingIntent.getService(context,
				26194, axlServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		axlAlarmMgr= (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
		setAlarm(axlServiceIntent,axlServicePendingIntent,26194,axlAlarmMgr);

		wifiServiceIntent = new Intent(context, WiFiService.class);
		wifiServicePendingIntent = PendingIntent.getService(context,
				12345, wifiServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		wifiAlarmMgr= (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
		setAlarm(wifiServiceIntent,wifiServicePendingIntent,12345,wifiAlarmMgr);


		audioServiceIntent = new Intent(context, AudioService.class);
		audioServicePendingIntent = PendingIntent.getService(context,
				2512, audioServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		audioAlarmMgr= (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
		setAlarm(audioServiceIntent,audioServicePendingIntent,2512,audioAlarmMgr);

		lightServiceIntent = new Intent(context, LightService.class);
		lightServicePendingIntent = PendingIntent.getService(context,
				11894, lightServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		lightAlarmMgr= (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
		setAlarm(lightServiceIntent,lightServicePendingIntent,11894,lightAlarmMgr);

        battServiceIntent = new Intent(context, BatteryService.class);
        battServicePendingIntent = PendingIntent.getService(context,
                19994, battServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        battAlarmMgr= (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        try{
            battAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime()+100, Common.BATTERY_INTERVAL*60*1000, battServicePendingIntent);
            Log.v("ELSERVICES","Alarm Set for Battery service "+19994+" "+Common.INTERVAL);
        }
        catch(Exception e){
            Log.e("ELSERVICES",e.toString(), e.getCause());
        }

		/*magServiceIntent = new Intent(context, MagService.class);
		magServicePendingIntent = PendingIntent.getService(context,
				20591, magServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		magAlarmMgr= (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
		setAlarm(magServiceIntent,magServicePendingIntent,20591,magAlarmMgr);*/
		
		/*uploaderServiceIntent = new Intent(context, UploaderService.class);
		uploaderServicePendingIntent = PendingIntent.getService(context,
				4816, uploaderServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		uploaderAlarmMgr= (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
		uploaderAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime()+Common.UPLOAD_INTERVAL*60*1000, Common.UPLOAD_INTERVAL*60*1000, uploaderServicePendingIntent); 
		*/
		//		Log.v("ELSERVICES","Uploader alarm Set for service "+4816+" "+Common.INTERVAL);
		
		//LogWriter.debugLogWrite(System.currentTimeMillis(),"All services scheduled to start");
		
		uploaderServiceIntent = new Intent(context, UpdateAlarmReceiver.class);
		uploaderServiceIntent.setAction("EnergyLensPlus.updateAlarm");
		uploaderServiceIntent.putExtra("message", "EnergyLensPlus.updateAlarm");
		uploaderServicePendingIntent = PendingIntent.getBroadcast(context,
				4816, uploaderServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		uploaderAlarmMgr= (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        try {
            uploaderAlarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + Common.UPLOAD_INTERVAL * 60 * 1000, Common.UPLOAD_INTERVAL * 60 * 1000, uploaderServicePendingIntent);
        }
        catch(Exception e){
            Log.e("ELSERVICES",e.toString(), e.getCause());
        }
	
	}

	private void stop() throws Throwable{
		Log.v("ELSERVICES","Services stopped");
		//LogWriter.debugLogWrite(System.currentTimeMillis(),"All services stop");
		try{
			if(axlServicePendingIntent!=null && axlAlarmMgr!=null)
				axlAlarmMgr.cancel(axlServicePendingIntent);
			//			wifiAlarmMgr.cancel(wifiServicePendingIntent);

			if(audioServicePendingIntent!=null && audioAlarmMgr!=null)
				audioAlarmMgr.cancel(audioServicePendingIntent);


			if(lightServicePendingIntent!=null && lightAlarmMgr!=null)
				lightAlarmMgr.cancel(lightServicePendingIntent);

            if(battServicePendingIntent!=null && battAlarmMgr!=null)
                battAlarmMgr.cancel(battServicePendingIntent);

			/*if(magServicePendingIntent!=null && magAlarmMgr!=null)
				magAlarmMgr.cancel(magServicePendingIntent);*/

			/*if(uploaderServicePendingIntent!=null && uploaderAlarmMgr!=null)
				uploaderAlarmMgr.cancel(uploaderServicePendingIntent);*/
		}
		catch(Exception e){
			e.printStackTrace();
			//LogWriter.debugLogWrite(System.currentTimeMillis(),"Error while stopping services "+e.getMessage());
		}
	}

	private void setAlarm(Intent ServiceIntent,PendingIntent ServicePendingIntent,int ReqCode, AlarmManager alarmMgr){
		Log.v("ELSERVICES","Services started "+ReqCode);

		try{			
			alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime()+100, Common.INTERVAL*1000, ServicePendingIntent); 
			Log.v("ELSERVICES","Alarm Set for service "+ReqCode+" "+Common.INTERVAL);
		}
		catch(Exception e){
			Log.e("ELSERVICES",e.toString(), e.getCause());
		}
	}

}