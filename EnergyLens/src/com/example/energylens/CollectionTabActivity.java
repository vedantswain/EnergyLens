package com.example.energylens;

import com.crashlytics.android.Crashlytics;
import java.util.Locale;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class CollectionTabActivity extends FragmentActivity {

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
	ViewPager mViewPager;
	
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
		
		getUpdatedPreferences();
		
		if(Common.TRAINING_STATUS==1){
			Intent intent = new Intent(this,TrainActivity.class);
			startActivity(intent);
		}
		else if(Common.TRAINING_COUNT>0){
			mViewPager.setCurrentItem(1);
			  Log.v("ELSERVICES", "Switched");
		}
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
		if (id == R.id.action_settings) {
			openSettings();
			return true;
		}
		return super.onOptionsItemSelected(item);
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
