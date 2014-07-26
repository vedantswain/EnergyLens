package com.example.energylens;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	
	private AlarmManager alarmMgr;
	private PendingIntent axlServicePendingIntent;
	private Intent axlServiceIntent;
	long interval=20000; //milliseconds between each scheduling of service

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
		axlServiceIntent = new Intent(MainActivity.this, AxlService.class);
		
		axlServicePendingIntent = PendingIntent.getService(this,
				12345, axlServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		     
		alarmMgr= (AlarmManager)this.getSystemService(this.ALARM_SERVICE);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 10);

		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(), interval, axlServicePendingIntent); 
	}
	
	public void stopService(View view){
		Intent i = new Intent(MainActivity.this, AxlService.class);
        MainActivity.this.stopService(i);
        alarmMgr.cancel(axlServicePendingIntent);
	}
}
