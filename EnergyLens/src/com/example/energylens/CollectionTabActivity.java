package com.example.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class CollectionTabActivity extends FragmentActivity implements TrainMoreDialogFragment.TrainMoreDialogListener{

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	static Context context;
	String SENDER_ID = "166229175411";
	Boolean doubleBackToExitPressedOnce=false;
	String groundReportDates;

	static public AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,uploaderAlarmMgr;
	static public PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,lightServicePendingIntent,magServicePendingIntent,uploaderServicePendingIntent;
	static public Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,uploaderServiceIntent;

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

	int prevTabNo=0;
	String screenName="EnergyWastage";

	long timeOfVisit,timeOfStay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(R.layout.activity_collection_tab);

		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		context=this;
		gcm = GoogleCloudMessaging.getInstance(this);

		getUpdatedPreferences();
//		addShortcut();

		if(Common.TRAINING_COUNT==0){
			DialogFragment newFragment = new TrainMoreDialogFragment();
			newFragment.show(getSupportFragmentManager(), "Training");
		}

		if(Common.TRAINING_STATUS==1){
			Intent intent = new Intent(this,TrainActivity.class);
			startActivity(intent);
		}
		else if(Common.TRAINING_COUNT>0){
			mViewPager.setCurrentItem(0);
			Log.v("ELSERVICES", "Switched");
			Common.changeCurrentVisible(0);
			toggleServiceMessage("startServices from Main");
		}

		timeOfVisit=System.currentTimeMillis();

		if(savedInstanceState!=null){
			//			Common.changePEnSIS(savedInstanceState.getBundle("PEN_SIS"));
			Log.v("ELSERVICES", "loaded from main");
			Log.v("ELSERVICES", savedInstanceState.getString("Message"));
		}

		Common.changeLastSent(0,System.currentTimeMillis()-(Common.SEND_REQUEST_INTERVAL+1)*60*1000);
		Common.changeLastSent(1,System.currentTimeMillis()-(Common.SEND_REQUEST_INTERVAL+1)*60*1000);

		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int tabNo) {
				// TODO Auto-generated method stub
				switch(prevTabNo){
				case 0:screenName="EnergyWastage";
				break;
				case 1:screenName="PersonalEnergy";
				break;
				case 2:screenName="RealTimePower";
				break;
				}
				if(prevTabNo!=-1){
					Log.v("ELSERVICES", "Writing into screen log");
					timeOfStay=System.currentTimeMillis()-timeOfVisit;
					LogWriter.screenLogWrite(timeOfVisit+","+screenName+","+timeOfStay);
				}
				
				prevTabNo=tabNo;
				Common.changeCurrentVisible(tabNo);
				Log.v("ELSERVICES", "Current visible: "+tabNo+" time since: "
						+Long.toString((System.currentTimeMillis()-Common.WASTAGE_LAST_SENT)/1000)
						+" interval: "+Common.SEND_REQUEST_INTERVAL);
				switch(tabNo){
				case 0:
					if(System.currentTimeMillis()-Common.WASTAGE_LAST_SENT>Common.SEND_REQUEST_INTERVAL*60*1000){
						Common.changeLastSent(0,System.currentTimeMillis());
						EnergyWastageFragment.sendMessage();
					}
					break;
				case 1:
					if(System.currentTimeMillis()-Common.PERSONAL_LAST_SENT>Common.SEND_REQUEST_INTERVAL*60*1000){
						Common.changeLastSent(1,System.currentTimeMillis());
						PersonalEnergyFragment.sendMessage();
					}
					break;

				}
				timeOfVisit=System.currentTimeMillis();
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

	}
	
	@Override
	public void onPause() {
		super.onPause();
		switch(Common.CURRENT_VISIBLE){
		case 0:screenName="EnergyWastage";
		break;
		case 1:screenName="PersonalEnergy";
		break;
		case 2:screenName="RealTimePower";
		break;
		}		
		timeOfStay=System.currentTimeMillis()-timeOfVisit;
		LogWriter.screenLogWrite(timeOfVisit+","+screenName+","+timeOfStay);
	}
	
	private void toggleServiceMessage(String message){
		Intent intent = new Intent();
		intent.setAction("EnergyLensPlus.toggleService");
		  // add data
		  intent.putExtra("message", message);

		  Log.v("ELSERVICES", "Broadcast from Train to Main receiver");
		  sendBroadcast(intent);
	}

	public void addShortcut(){

		SharedPreferences shortcutPref = getSharedPreferences(Common.EL_PREFS,0);

		if(!shortcutPref.getBoolean("SHORTCUT_INSTALLED", false)){
			Editor editor=shortcutPref.edit();
			editor.putBoolean("SHORTCUT_INSTALLED", true);
			editor.commit();
			Intent shortcutintent=new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

			shortcutintent.putExtra("duplicate", false);
			shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.title_activity_main));
			Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher);
			shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
			shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext(), CollectionTabActivity.class));

			sendBroadcast(shortcutintent);
		}
	}


	protected void onResume(){
		super.onResume();
		timeOfVisit=System.currentTimeMillis();
		getUpdatedPreferences();
		
//		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
//			      new IntentFilter("toggleService"));
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

	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			Common.apiToSend="energy/wastage/";
			Common.chartTitle="Your Energy Wastage";
			sendMessage();
		} else {
			Common.apiToSend="energy/personal/";
			Common.chartTitle="Your Energy Consumption";
			sendMessage();
		}
	}

	public void getUpdatedPreferences(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Common.changeServerUrl(sharedPref.getString("SERVER_URL", Common.SERVER_URL));

		SharedPreferences trainingPref = getSharedPreferences(Common.EL_PREFS,0);
		Common.changeTrainingStatus(trainingPref.getInt("TRAINING_STATUS", 0));
		Common.changeLabel(trainingPref.getString("LABEL","none"));
		Common.changeLocation(trainingPref.getString("LOCATION", "none"));
		Common.changePrefix(trainingPref.getString("FILE_PREFIX", ""));
		Common.changeTrainingCount(trainingPref.getInt("TRAINING_COUNT", 0));

		Log.v("ELSERVICES", "Training onresume "+Common.TRAINING_STATUS+"\n Label "+Common.LABEL+"\n Location "+Common.LOCATION);

		SharedPreferences sp=getSharedPreferences("GROUNDREPORT_PREFS",0);

		if(sp.contains("JSON_RESPONSES")){
			Log.v("ELSERVICES", "Loading GroundReport from saved data");
			groundReportDates=sp.getString("RESPONSE_DATES", "");
		}
	}

	public void toTimeSelect(View view){
		Intent intent=new Intent(this,TimeSelectActivity.class);
		startActivity(intent);
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
		toggleServiceMessage("startServices from Main");
		Log.i("ELSERVICES", "Training count: "+Common.TRAINING_COUNT);
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
	
	public void openAbout(){
		Intent intent = new Intent(this,AboutActivity.class);
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
		else if(id==R.id.appAbout){
			openAbout();
			return true;
		}
		else if(id == R.id.groundReport){
			getUpdatedPreferences();
			Intent intent = new Intent(this,GroundReportListActivity.class);
			if(groundReportDates!=null){
				if(groundReportDates.length()>2)
					startActivity(intent);
				else
					Toast.makeText(getApplicationContext(), "No new reports", 2000).show();
			}
			else
				Toast.makeText(getApplicationContext(), "No new reports", 2000).show();
			return true;
		}
		else if(id == R.id.training){
			getUpdatedPreferences();
			Intent intent = new Intent(this,TrainActivity.class);
			startActivity(intent);
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

	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);

		// Save the last sync time and last data received
		//		savedInstanceState.putBundle("PEN_SIS", Common.PEN_SIS);
		savedInstanceState.putString("Message", "main message restored");
		Log.v("ELSERVICES", "Main Instance saved");

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
	
	//BroadcastManager for getting calls from running services
	// handler for received Intents for the "my-event" event 
//	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//	  @Override
//	  public void onReceive(Context context, Intent intent) {
//	    // Extract data included in the Intent
//	    String message = intent.getStringExtra("message");
//	    Log.v("ELSERVICES", "Main receiver got message: " + message);
//	    if(message.contains("startServices"))
//	    	toggleServiceMessage("startServices from Main");
//	    else if(message.equals("stopServices"))
//			try {
//				stop();
//			} catch (Throwable e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	  }
//	};
	

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
				fragment=new EnergyWastageFragment();
				break;
			case 1:
				fragment=new PersonalEnergyFragment();
				break;
			case 2:
				fragment=new RealTimePowerFragment();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			String title="";


			Drawable mDrawable=getResources().getDrawable(R.drawable.ic_energy);

			switch (position) {
			case 0:
				title= getString(R.string.title_section2).toUpperCase(l);
				mDrawable=getResources().getDrawable(R.drawable.ic_wastage);
				break;
			case 1:
				title= getString(R.string.title_section3).toUpperCase(l);
				mDrawable=getResources().getDrawable(R.drawable.ic_energy);
				break;
			case 2:
				title= getString(R.string.title_section4).toUpperCase(l);
				mDrawable=getResources().getDrawable(R.drawable.ic_realtime);
				break;
			}
			mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight()); 

			SpannableStringBuilder sb = new SpannableStringBuilder(" "+title);
			ImageSpan span = new ImageSpan(mDrawable, ImageSpan.ALIGN_BOTTOM); 
			sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			return sb;

			//			return null;
		}
	}

	@Override
	public void onTrainMore() {
		// TODO Auto-generated method stub
		Common.changeTrainingCount(Common.TRAINING_COUNT+1);
		updatePreferences();
		toggleServiceMessage("startServices from Main");
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		Intent intent=new Intent(this,TrainActivity.class);
		startActivity(intent);
	}
}
