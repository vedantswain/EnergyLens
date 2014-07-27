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
	private AlarmManager alarmMgr;
	private PendingIntent axlServicePendingIntent;
	private Intent axlServiceIntent;
	
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
		     
		alarmMgr= (AlarmManager)this.getSystemService(this.ALARM_SERVICE);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 10);

		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(), INTERVAL*1000, axlServicePendingIntent); 
	}
	
	public void stopService(View view){
		Intent i = new Intent(MainActivity.this, AxlService.class);
		Log.v("ELSERVICES","Service stopped");
		Toast.makeText(this, "Data collection stopped", LENGTH_SHORT).show();
		try{
			alarmMgr.cancel(axlServicePendingIntent);
		}
		catch(Exception e){
				Log.v("EL+SERVICES",e.toString());
			}
		}
}
