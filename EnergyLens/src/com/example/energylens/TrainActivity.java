package com.example.energylens;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class TrainActivity extends Activity{
	private static final int LENGTH_SHORT = 1000;
	private AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,uploaderAlarmMgr;
	private PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,lightServicePendingIntent,magServicePendingIntent,uploaderServicePendingIntent;
	private Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,uploaderServiceIntent;
	private String[] labels={"Fan","AC","Microwave","TV","Computer","Printer","Washing Machine","Fan+AC"};
	private String[] locations={"Kitchen","Dining Room","Bedroom1","Bedroom2","Bedroom3","Study","Corridor","Inside","Outside","None"};
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Set the user interface layout for this Activity
	    // The layout file is defined in the project res/layout/main_activity.xml file
	    setContentView(R.layout.train_activity);
	}
	
	public void startService(View view){
		start();
	}
	
	
	public void stopService(View view){
		try {
			stop();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void start(){
		Log.v("ELSERVICES","Service started");
		Toast.makeText(TrainActivity.this, "Data collection started", LENGTH_SHORT).show();
		
		axlServiceIntent = new Intent(TrainActivity.this, AxlService.class);
		axlServicePendingIntent = PendingIntent.getService(TrainActivity.this,
				26194, axlServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    axlAlarmMgr= (AlarmManager)TrainActivity.this.getSystemService(TrainActivity.this.ALARM_SERVICE);
		setAlarm(axlServiceIntent,axlServicePendingIntent,26194,axlAlarmMgr);
		
		wifiServiceIntent = new Intent(TrainActivity.this, WiFiService.class);
		wifiServicePendingIntent = PendingIntent.getService(TrainActivity.this,
				12345, wifiServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    wifiAlarmMgr= (AlarmManager)TrainActivity.this.getSystemService(TrainActivity.this.ALARM_SERVICE);
		setAlarm(wifiServiceIntent,wifiServicePendingIntent,12345,wifiAlarmMgr);
		

		audioServiceIntent = new Intent(TrainActivity.this, AudioService.class);
		audioServicePendingIntent = PendingIntent.getService(TrainActivity.this,
				2512, audioServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    audioAlarmMgr= (AlarmManager)TrainActivity.this.getSystemService(TrainActivity.this.ALARM_SERVICE);
		setAlarm(audioServiceIntent,audioServicePendingIntent,2512,audioAlarmMgr);
		
		lightServiceIntent = new Intent(TrainActivity.this, LightService.class);
		lightServicePendingIntent = PendingIntent.getService(TrainActivity.this,
				11894, lightServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    lightAlarmMgr= (AlarmManager)TrainActivity.this.getSystemService(TrainActivity.this.ALARM_SERVICE);
		setAlarm(lightServiceIntent,lightServicePendingIntent,11894,lightAlarmMgr);
		
		magServiceIntent = new Intent(TrainActivity.this, MagService.class);
		magServicePendingIntent = PendingIntent.getService(TrainActivity.this,
				20591, magServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    magAlarmMgr= (AlarmManager)TrainActivity.this.getSystemService(TrainActivity.this.ALARM_SERVICE);
		setAlarm(magServiceIntent,magServicePendingIntent,20591,magAlarmMgr);
		
		uploaderServiceIntent = new Intent(TrainActivity.this, UploaderService.class);
		uploaderServicePendingIntent = PendingIntent.getService(TrainActivity.this,
				4816, uploaderServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    uploaderAlarmMgr= (AlarmManager)TrainActivity.this.getSystemService(TrainActivity.this.ALARM_SERVICE);
		uploaderAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis()+100, Common.UPLOAD_INTERVAL*30*1000, uploaderServicePendingIntent); 
		Log.v("ELSERVICES","Uploader alarm Set for service "+4816+" "+Common.INTERVAL);
	}

	public void setAlarm(Intent ServiceIntent,PendingIntent ServicePendingIntent,int ReqCode, AlarmManager alarmMgr){
		Log.v("ELSERVICES","Services started "+ReqCode);
				
		try{
		
			alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis()+100, Common.INTERVAL*1000, ServicePendingIntent); 
			Log.v("ELSERVICES","Alarm Set for service "+ReqCode+" "+Common.INTERVAL);
		}
		catch(Exception e){
			Log.e("ELSERVICES",e.toString(), e.getCause());
		}
	}
	
	
	public void stop() throws Throwable{
		Log.v("ELSERVICES","Services stopped");
		Toast.makeText(TrainActivity.this, "Data collection stopped", LENGTH_SHORT).show();
		try{
			axlAlarmMgr.cancel(axlServicePendingIntent);
			wifiAlarmMgr.cancel(wifiServicePendingIntent);
			audioAlarmMgr.cancel(audioServicePendingIntent);
			lightAlarmMgr.cancel(lightServicePendingIntent);
			magAlarmMgr.cancel(magServicePendingIntent);
			uploaderAlarmMgr.cancel(uploaderServicePendingIntent);
		}
		catch(Exception e){
			e.printStackTrace();
			}
		}
}
