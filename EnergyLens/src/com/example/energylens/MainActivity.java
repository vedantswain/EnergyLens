package com.example.energylens;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final int LENGTH_SHORT = 1000;
	private static final long INTERVAL = 20; //milliseconds between each scheduling of service
	private AlarmManager axlAlarmMgr,wifiAlarmMgr;
	private PendingIntent axlServicePendingIntent,wifiServicePendingIntent;
	private Intent axlServiceIntent,wifiServiceIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

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
		Log.v("ELSERVICES","Service started");
		Toast.makeText(this, "Data collection started", LENGTH_SHORT).show();
		
		axlServiceIntent = new Intent(MainActivity.this, AxlService.class);
		axlServicePendingIntent = PendingIntent.getService(this,
				26194, axlServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    axlAlarmMgr= (AlarmManager)this.getSystemService(this.ALARM_SERVICE);
		setAlarm(axlServiceIntent,axlServicePendingIntent,26194,axlAlarmMgr);
		
		wifiServiceIntent = new Intent(MainActivity.this, WiFiService.class);
		wifiServicePendingIntent = PendingIntent.getService(this,
				12345, wifiServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    wifiAlarmMgr= (AlarmManager)this.getSystemService(this.ALARM_SERVICE);
		setAlarm(wifiServiceIntent,wifiServicePendingIntent,12345,wifiAlarmMgr);
	}
	
	public void setAlarm(Intent ServiceIntent,PendingIntent ServicePendingIntent,int ReqCode, AlarmManager alarmMgr){
		Log.v("ELSERVICES","Service started "+ReqCode);
				
		try{
						
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.SECOND, 10);
	
			alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), INTERVAL*1000, ServicePendingIntent); 
			Log.v("ELSERVICES","Alarm Set "+ReqCode);
		}
		catch(Exception e){
			Log.e("ELSERVICES",e.toString(), e.getCause());
		}
	}
	
	public void stopService(View view){
		Intent i = new Intent(MainActivity.this, AxlService.class);
		Log.v("ELSERVICES","Service stopped");
		Toast.makeText(this, "Data collection stopped", LENGTH_SHORT).show();
		try{
			axlAlarmMgr.cancel(axlServicePendingIntent);
			wifiAlarmMgr.cancel(wifiServicePendingIntent);
		}
		catch(Exception e){
			Log.e("ELSERVICES",e.toString(), e.getCause());
			}
		}
}
