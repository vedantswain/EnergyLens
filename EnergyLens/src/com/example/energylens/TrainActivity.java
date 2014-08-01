package com.example.energylens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


public class TrainActivity extends FragmentActivity implements ApplianceDialogFragment.ApplianceDialogListener,LocationDialogFragment.LocationDialogListener,TrainMoreDialogFragment.TrainMoreDialogListener{
	private static final int LENGTH_SHORT = 1000;
	private AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,uploaderAlarmMgr;
	private PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,lightServicePendingIntent,magServicePendingIntent,uploaderServicePendingIntent;
	private Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,uploaderServiceIntent;
	  private ViewFlipper viewFlipper;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // Set the user interface layout for this Activity
	    // The layout file is defined in the project res/layout/main_activity.xml file
	    setContentView(R.layout.train_activity);
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        
        if(Common.TRAINING_STATUS==1){
			viewFlipper.showNext();
		}
        
	}
	
	@Override
	public void onBackPressed() {
		if(Common.TRAINING_STATUS==0){
			super.onBackPressed();
		}
	}
	
	public void launchAppDialog(View view){
		 DialogFragment newFragment = new ApplianceDialogFragment();
		    newFragment.show(getSupportFragmentManager(), "Appliances");
	}
	
	public void launchTrainMoreDialog(View view){
		 DialogFragment newFragment = new TrainMoreDialogFragment();
		    newFragment.show(getSupportFragmentManager(), "TrainMore");
		    Common.changeTrainingStatus(0);
		    Common.changeLabel("none");
			Common.changeLocation("none");
			Common.changePrefix("");
			updatePreferences(Common.TRAINING_STATUS);
		    try {
				stop();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void launchLocDialog(View view){
		 DialogFragment newFragment = new LocationDialogFragment();
		    newFragment.show(getSupportFragmentManager(), "Locations");
		    
	}
	
	public void updatePreferences(int update){
		SharedPreferences trainingPref = getSharedPreferences(Common.EL_PREFS,0);
		SharedPreferences.Editor editor = trainingPref.edit();
	    editor.putInt("TRAINING_STATUS", update);
	    editor.putString("LABEL",Common.LABEL);
	    editor.putString("LOCATION", Common.LOCATION);
	    editor.putString("FILE_PREFIX", Common.FILE_PREFIX);
	      // Commit the edits!
	      editor.commit();
	}
		
	public void startService(View view){
		if(Common.LABEL!="none" && Common.LOCATION!="none"){
			Common.changePrefix("Training");
//			Log.v("ELSERVICES", Common.LABEL+" "+Common.LOCATION+" "+Common.FILE_PREFIX);
			viewFlipper.showNext();
			Common.changeTrainingStatus(1);
			updatePreferences(Common.TRAINING_STATUS);
			start();
		}
		else
			Toast.makeText(this, "Both appliance & location are required", LENGTH_SHORT).show();
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
				System.currentTimeMillis()+Common.UPLOAD_INTERVAL*60*1000, Common.UPLOAD_INTERVAL*60*1000, uploaderServicePendingIntent); 
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

	@Override
	public void onAppSelected(String label) {
		// TODO Auto-generated method stub
//		Log.v("ELSERVICES", "here");
		TextView textView=(TextView) findViewById(R.id.setApp);
		textView.setText(Common.LABEL);
	}

	@Override
	public void onLocSelected(String loc) {
		// TODO Auto-generated method stub
		TextView textView=(TextView) findViewById(R.id.setLoc);
		textView.setText(Common.LOCATION);
	}

	@Override
	public void onTrainMore() {
		// TODO Auto-generated method stub
		viewFlipper.showPrevious();
		TextView textView=(TextView) findViewById(R.id.setApp);
		textView.setText("no appliance selected");
		textView=(TextView) findViewById(R.id.setLoc);
		textView.setText("no location selected");
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		try {
			start();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
	}
}
