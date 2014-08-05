package com.example.energylens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootupReceiver extends BroadcastReceiver {
	
	Context mContext;
	private AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,uploaderAlarmMgr;
	private PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,lightServicePendingIntent,magServicePendingIntent,uploaderServicePendingIntent;
	private Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,uploaderServiceIntent;
	 
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext=context;
        Log.v("ELSERVICES", "Boot-up received");
        start();
    }
    
    public void start(){
		Log.v("ELSERVICES","Service started");
		
		axlServiceIntent = new Intent(mContext, AxlService.class);
		axlServicePendingIntent = PendingIntent.getService(mContext,
				26194, axlServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    axlAlarmMgr= (AlarmManager)mContext.getSystemService(mContext.ALARM_SERVICE);
		setAlarm(axlServiceIntent,axlServicePendingIntent,26194,axlAlarmMgr);
		
		wifiServiceIntent = new Intent(mContext, WiFiService.class);
		wifiServicePendingIntent = PendingIntent.getService(mContext,
				12345, wifiServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    wifiAlarmMgr= (AlarmManager)mContext.getSystemService(mContext.ALARM_SERVICE);
		setAlarm(wifiServiceIntent,wifiServicePendingIntent,12345,wifiAlarmMgr);
		

		audioServiceIntent = new Intent(mContext, AudioService.class);
		audioServicePendingIntent = PendingIntent.getService(mContext,
				2512, audioServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    audioAlarmMgr= (AlarmManager)mContext.getSystemService(mContext.ALARM_SERVICE);
		setAlarm(audioServiceIntent,audioServicePendingIntent,2512,audioAlarmMgr);
		
		lightServiceIntent = new Intent(mContext, LightService.class);
		lightServicePendingIntent = PendingIntent.getService(mContext,
				11894, lightServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    lightAlarmMgr= (AlarmManager)mContext.getSystemService(mContext.ALARM_SERVICE);
		setAlarm(lightServiceIntent,lightServicePendingIntent,11894,lightAlarmMgr);
		
		magServiceIntent = new Intent(mContext, MagService.class);
		magServicePendingIntent = PendingIntent.getService(mContext,
				20591, magServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    magAlarmMgr= (AlarmManager)mContext.getSystemService(mContext.ALARM_SERVICE);
		setAlarm(magServiceIntent,magServicePendingIntent,20591,magAlarmMgr);
		
		uploaderServiceIntent = new Intent(mContext, UploaderService.class);
		uploaderServicePendingIntent = PendingIntent.getService(mContext,
				4816, uploaderServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    uploaderAlarmMgr= (AlarmManager)mContext.getSystemService(mContext.ALARM_SERVICE);
		uploaderAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis()+Common.UPLOAD_INTERVAL*30*1000, Common.UPLOAD_INTERVAL*30*1000, uploaderServicePendingIntent); 
		Log.v("ELSERVICES","Uploader alarm Set for service "+4816+" "+Common.INTERVAL);
	}

	public void setAlarm(Intent ServiceIntent,PendingIntent ServicePendingIntent,int ReqCode, AlarmManager alarmMgr){
		Log.v("ELSERVICES","Services started "+ReqCode);
				
		try{
		
			alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis()+Common.INTERVAL, Common.INTERVAL*1000, ServicePendingIntent); 
			Log.v("ELSERVICES","Alarm Set for service "+ReqCode+" "+Common.INTERVAL);
		}
		catch(Exception e){
			Log.e("ELSERVICES",e.toString(), e.getCause());
		}
	}
	
}
