package com.example.energylens;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class AboutActivity extends Activity {

	private SimpleAdapter sa;
	ArrayList<String> keys=new ArrayList<String>();
	ArrayList<String> fields=new ArrayList<String>();
	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_about);
		
		keys.add("Username");
		keys.add("Apartment Number");
		keys.add("Home AP");
		keys.add("IMEI");

		SharedPreferences sp=getSharedPreferences(Common.EL_PREFS,0);
		fields.add(sp.getString("USERNAME", ""));
		fields.add(sp.getString("APARTMENT_NO", ""));
		fields.add(sp.getString("HOME_SSID", "")+" ["+sp.getString("HOME_BSSID", "")+"]");
		
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		fields.add(telephonyManager.getDeviceId());
	
		int index=0;
		HashMap<String,String> item;
		for(String key:keys){
			item = new HashMap<String,String>();
			item.put( "line1", key);
			item.put( "line2", fields.get(index));
			list.add( item );
			index++;
		}
		sa = new SimpleAdapter(this, list,
				android.R.layout.two_line_list_item ,
				new String[] { "line1","line2" },
				new int[] {android.R.id.text1, android.R.id.text2});
		
		final ListView listview = (ListView) findViewById(R.id.listview);
		listview.setAdapter(sa);

	}
}


