package com.example.energylens;

import com.crashlytics.android.Crashlytics;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final int LENGTH_SHORT = 1000;
	private AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,uploaderAlarmMgr;
	private PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,lightServicePendingIntent,magServicePendingIntent,uploaderServicePendingIntent;
	private Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,uploaderServiceIntent;
	
//	@Override
//	public onCreate(Bundle savedInstanceState) {
// 
//        View rootView = inflater.inflate(R.layout.fragment_train, container, false);
//         
//        return rootView;
//    }

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void startService(View view){
		start();
	}
	
	public void start(){
		Log.v("ELSERVICES","Service started");
		Toast.makeText(MainActivity.this, "Data collection started", LENGTH_SHORT).show();
		
		axlServiceIntent = new Intent(MainActivity.this, AxlService.class);
		axlServicePendingIntent = PendingIntent.getService(MainActivity.this,
				26194, axlServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    axlAlarmMgr= (AlarmManager)MainActivity.this.getSystemService(MainActivity.this.ALARM_SERVICE);
		setAlarm(axlServiceIntent,axlServicePendingIntent,26194,axlAlarmMgr);
		
		wifiServiceIntent = new Intent(MainActivity.this, WiFiService.class);
		wifiServicePendingIntent = PendingIntent.getService(MainActivity.this,
				12345, wifiServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    wifiAlarmMgr= (AlarmManager)MainActivity.this.getSystemService(MainActivity.this.ALARM_SERVICE);
		setAlarm(wifiServiceIntent,wifiServicePendingIntent,12345,wifiAlarmMgr);
		

		audioServiceIntent = new Intent(MainActivity.this, AudioService.class);
		audioServicePendingIntent = PendingIntent.getService(MainActivity.this,
				2512, audioServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    audioAlarmMgr= (AlarmManager)MainActivity.this.getSystemService(MainActivity.this.ALARM_SERVICE);
		setAlarm(audioServiceIntent,audioServicePendingIntent,2512,audioAlarmMgr);
		
		lightServiceIntent = new Intent(MainActivity.this, LightService.class);
		lightServicePendingIntent = PendingIntent.getService(MainActivity.this,
				11894, lightServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    lightAlarmMgr= (AlarmManager)MainActivity.this.getSystemService(MainActivity.this.ALARM_SERVICE);
		setAlarm(lightServiceIntent,lightServicePendingIntent,11894,lightAlarmMgr);
		
		magServiceIntent = new Intent(MainActivity.this, MagService.class);
		magServicePendingIntent = PendingIntent.getService(MainActivity.this,
				20591, magServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    magAlarmMgr= (AlarmManager)MainActivity.this.getSystemService(MainActivity.this.ALARM_SERVICE);
		setAlarm(magServiceIntent,magServicePendingIntent,20591,magAlarmMgr);
		
//		uploaderServiceIntent = new Intent(MainActivity.this, UploaderService.class);
//		uploaderServicePendingIntent = PendingIntent.getService(MainActivity.this,
//				4816, uploaderServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//	    uploaderAlarmMgr= (AlarmManager)MainActivity.this.getSystemService(MainActivity.this.ALARM_SERVICE);
//		setAlarm(uploaderServiceIntent,uploaderServicePendingIntent,4816,uploaderAlarmMgr);
		}

	public void setAlarm(Intent ServiceIntent,PendingIntent ServicePendingIntent,int ReqCode, AlarmManager alarmMgr){
		Log.v("ELSERVICES","Services started "+ReqCode);
				
		try{
						
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.SECOND, 10);
	
			alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis()+100, Common.INTERVAL*1000, ServicePendingIntent); 
			Log.v("ELSERVICES","Alarm Set for service "+ReqCode+" "+Common.INTERVAL);
		}
		catch(Exception e){
			Log.e("ELSERVICES",e.toString(), e.getCause());
		}
	}
	
	public void stopService(View view) throws Throwable{
		Log.v("ELSERVICES","Services stopped");
		Toast.makeText(MainActivity.this, "Data collection stopped", LENGTH_SHORT).show();
		try{
			axlAlarmMgr.cancel(axlServicePendingIntent);
			wifiAlarmMgr.cancel(wifiServicePendingIntent);
			audioAlarmMgr.cancel(audioServicePendingIntent);
			lightAlarmMgr.cancel(lightServicePendingIntent);
			magAlarmMgr.cancel(magServicePendingIntent);
//			uploaderAlarmMgr.cancel(uploaderServicePendingIntent);
		}
		catch(Exception e){
			Log.e("ELSERVICES",e.toString(), e);
			}
		}
}
