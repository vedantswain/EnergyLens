package com.example.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class CollectionTabActivity extends FragmentActivity {
	
	GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;
    String SENDER_ID = "166229175411";
    Boolean doubleBackToExitPressedOnce=false;
    GraphicalView chartView;

    private AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,uploaderAlarmMgr;
	private PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,lightServicePendingIntent,magServicePendingIntent,uploaderServicePendingIntent;
	private Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,uploaderServiceIntent;
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	static ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(R.layout.activity_collection_tab);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		gcm = GoogleCloudMessaging.getInstance(this);
		
		getUpdatedPreferences();
		
		if(Common.TRAINING_STATUS==1){
			Intent intent = new Intent(this,TrainActivity.class);
			startActivity(intent);
		}
		else if(Common.TRAINING_COUNT>0){
				mViewPager.setCurrentItem(1);
			  Log.v("ELSERVICES", "Switched");
			  start();
		}
	}

	
	
	protected void onResume(){
		super.onResume();
		getUpdatedPreferences();
	}
	
	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			Common.changeDoubleBack(true);
	        finish();
	        return;
	    }

	    this.doubleBackToExitPressedOnce = true;
	    Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

	    new Handler().postDelayed(new Runnable() {

	        @Override
	        public void run() {
	            doubleBackToExitPressedOnce=false;                       
	        }
	    }, 2000);
	}
	
	public void getUpdatedPreferences(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Common.changeServerUrl(sharedPref.getString("SERVER_URL", "http://192.168.20.217:9010/"));
		
		SharedPreferences trainingPref = getSharedPreferences(Common.EL_PREFS,0);
		Common.changeTrainingStatus(trainingPref.getInt("TRAINING_STATUS", 0));
		Common.changeLabel(trainingPref.getString("LABEL","none"));
	    Common.changeLocation(trainingPref.getString("LOCATION", "none"));
	    Common.changePrefix(trainingPref.getString("FILE_PREFIX", ""));
	    Common.changeTrainingCount(trainingPref.getInt("TRAINING_COUNT", 0));
		
		Log.v("ELSERVICES", "Training onresume "+Common.TRAINING_STATUS+"\n Label "+Common.LABEL+"\n Location "+Common.LOCATION);
		
	}
	
	public void toReassign(View view){
		
//		Log.v("ELSERVICES", Boolean.toString(checkBox.isChecked()));
		Intent intent=new Intent(this,ReassignActivity.class);
		startActivity(intent);
	}
	
	public void onNotYet(View view){
		TextView changeTxt=(TextView) findViewById(R.id.alreadyText);
		changeTxt.setText("Welcome to EnergyLens+,\n just press the button below to get started ");
		Button btn=(Button) findViewById(R.id.done);
		btn.setVisibility(View.GONE);
		btn=(Button) findViewById(R.id.notYet);
		btn.setVisibility(View.GONE);
	}
	
	public void onDoneThat(View view){
		Common.changeTrainingCount(Common.TRAINING_COUNT+1);
		TextView changeTxt=(TextView) findViewById(R.id.alreadyText);
		changeTxt.setText("Great! Just checking.");
		Button btn=(Button) findViewById(R.id.done);
		btn.setVisibility(View.GONE);
		btn=(Button) findViewById(R.id.notYet);
		btn.setVisibility(View.GONE);
		updatePreferences();
		start();
		Log.i("ELSERVICES", "Training count: "+Common.TRAINING_COUNT);
	}
	
	public void start(){
		Log.v("ELSERVICES","Service started");
		
		axlServiceIntent = new Intent(CollectionTabActivity.this, AxlService.class);
		axlServicePendingIntent = PendingIntent.getService(CollectionTabActivity.this,
				26194, axlServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    axlAlarmMgr= (AlarmManager)CollectionTabActivity.this.getSystemService(CollectionTabActivity.this.ALARM_SERVICE);
		setAlarm(axlServiceIntent,axlServicePendingIntent,26194,axlAlarmMgr);
		
		wifiServiceIntent = new Intent(CollectionTabActivity.this, WiFiService.class);
		wifiServicePendingIntent = PendingIntent.getService(CollectionTabActivity.this,
				12345, wifiServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    wifiAlarmMgr= (AlarmManager)CollectionTabActivity.this.getSystemService(CollectionTabActivity.this.ALARM_SERVICE);
		setAlarm(wifiServiceIntent,wifiServicePendingIntent,12345,wifiAlarmMgr);
		

		audioServiceIntent = new Intent(CollectionTabActivity.this, AudioService.class);
		audioServicePendingIntent = PendingIntent.getService(CollectionTabActivity.this,
				2512, audioServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    audioAlarmMgr= (AlarmManager)CollectionTabActivity.this.getSystemService(CollectionTabActivity.this.ALARM_SERVICE);
		setAlarm(audioServiceIntent,audioServicePendingIntent,2512,audioAlarmMgr);
		
		lightServiceIntent = new Intent(CollectionTabActivity.this, LightService.class);
		lightServicePendingIntent = PendingIntent.getService(CollectionTabActivity.this,
				11894, lightServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    lightAlarmMgr= (AlarmManager)CollectionTabActivity.this.getSystemService(CollectionTabActivity.this.ALARM_SERVICE);
		setAlarm(lightServiceIntent,lightServicePendingIntent,11894,lightAlarmMgr);
		
		magServiceIntent = new Intent(CollectionTabActivity.this, MagService.class);
		magServicePendingIntent = PendingIntent.getService(CollectionTabActivity.this,
				20591, magServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    magAlarmMgr= (AlarmManager)CollectionTabActivity.this.getSystemService(CollectionTabActivity.this.ALARM_SERVICE);
		setAlarm(magServiceIntent,magServicePendingIntent,20591,magAlarmMgr);
		
		uploaderServiceIntent = new Intent(CollectionTabActivity.this, UploaderService.class);
		uploaderServicePendingIntent = PendingIntent.getService(CollectionTabActivity.this,
				4816, uploaderServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    uploaderAlarmMgr= (AlarmManager)CollectionTabActivity.this.getSystemService(CollectionTabActivity.this.ALARM_SERVICE);
		uploaderAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis()+Common.UPLOAD_INTERVAL*30*1000, Common.UPLOAD_INTERVAL*30*1000, uploaderServicePendingIntent); 
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
	
	
	
	public void updatePreferences(){
		SharedPreferences trainingPref = getSharedPreferences(Common.EL_PREFS,0);
		SharedPreferences.Editor editor = trainingPref.edit();
	    editor.putInt("TRAINING_COUNT",Common.TRAINING_COUNT);
	      // Commit the edits!
	      editor.commit();
	}
		
	
	public void openSettings(){
		Intent intent = new Intent(this,SettingsActivity.class);
		startActivity(intent);
	}
	
	public void startTraining(View view){
		getUpdatedPreferences();
		Intent intent = new Intent(this,TrainActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.collection_tab, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.appGroup) {
			openSettings();
			return true;
		}
		else if(id == android.R.id.home){
			if (doubleBackToExitPressedOnce) {
				Common.changeDoubleBack(true);
		        finish();
		        return true;
		    }

		    this.doubleBackToExitPressedOnce = true;
		    Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

		    new Handler().postDelayed(new Runnable() {

		        @Override
		        public void run() {
		            doubleBackToExitPressedOnce=false;                       
		        }
		    }, 2000);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void sendMessage(){
		 new AsyncTask<Void,String,String>() {
	         @Override
	         protected String doInBackground(Void... params) {
	             String msg = "";
	             try {
	                 Bundle data = new Bundle();
	                     data.putString("my_message", "Hello World");
	                     data.putString("my_action",
	                             "com.google.android.gcm.demo.app.ECHO_NOW");
	                     SecureRandom random = new SecureRandom();
	                     String randomId=new BigInteger(130, random).toString(32);
	                     
	                     String id = Long.toString(System.currentTimeMillis());
	                     gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
	                     msg = "Sent message";
	                     Log.i("ELSERVICES", "message sent");
	             } catch (IOException ex) {
	                 msg = "Error :" + ex.getMessage();
	             }
	             return msg;
	         }

	         @Override
	         protected void onPostExecute(String msg) {
	            Log.i("ELSERVICES", msg);
	         }
	     }.execute(null, null, null);
	}

	public void onSend(View view){
		sendMessage();
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			Fragment fragment=null;
		switch(position){	
		case 0:
			fragment=new TrainFragment();
			break;
		case 1:
			fragment=new PersonalEnergyFragment();
			break;
		case 2:
			fragment=new RealTimePowerFragment();
			break;
		case 3:
			fragment=new ComparisonFragment();
			break;
		}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 4 total pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section4).toUpperCase(l);
			}
			return null;
		}
	}
}
