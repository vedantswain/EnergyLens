package com.iiitd.muc.energylens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ListIterator;

import com.iiitd.muc.energylens.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class GroundReportListActivity extends Activity {

	ArrayList<String> responses=new ArrayList<String>();
	ArrayList<String> dates=new ArrayList<String>();
	ArrayList<String> reportList=new ArrayList<String>();

	String top_response;
	long current_date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ground_report_list);
		setList();

	}

	protected void onResume(){
		super.onResume();
		setList();
	}


	private void setList(){

		responses=new ArrayList<String>();
		dates=new ArrayList<String>();
		reportList=new ArrayList<String>();

		SharedPreferences sp=getSharedPreferences("GROUNDREPORT_PREFS",0);

		if(sp.contains("JSON_RESPONSES")){
			Log.v("ELSERVICES", "Loading All responses from saved data "+sp.getString("JSON_RESPONSES", "")
					+"\t"+sp.getString("RESPONSE_DATES", ""));
			String string=sp.getString("JSON_RESPONSES", "");
			String date=sp.getString("RESPONSE_DATES", "");
			//			parsePref(sp.getString("JSON_RESPONSE", ""));
			if(!string.equals("") && !date.equals("")){
//				Toast.makeText(GroundReportListActivity.this, "Loaded last uncorrected report", 1000).show();
				parseResponses(string,date);
			}
			else{
				Toast.makeText(GroundReportListActivity.this, "No new reports", 2000).show();
				finish();
			}
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy HH:mm");

		ListIterator iter=dates.listIterator(dates.size());
		while(iter.hasPrevious()){
			String str=iter.previous().toString();
			if(!str.equals(""))
				reportList.add("Report "+dateFormat.format(Long.parseLong(str)));
		}

		final ListView listview = (ListView) findViewById(R.id.listview);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, reportList);
		listview.setAdapter(adapter); 

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// ListView Clicked item index
				int itemPosition     = position;

				// ListView Clicked item value
				String  itemValue    = (String) listview.getItemAtPosition(position);

				//              // Show Alert 
				//              Toast.makeText(getApplicationContext(),
				//                "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
				//                .show();

				Intent intent = new Intent(GroundReportListActivity.this,GroundReportActivity.class);
				Bundle extrasBundle=new Bundle();
				extrasBundle.putInt("index", responses.size()-position-2);
				intent.putExtras(extrasBundle);
				startActivity(intent);

			}

		}); 
	}

	public void parseResponses(String string, String date){
		responses=new ArrayList<String>();
		dates=new ArrayList<String>();

		String[] respArray=string.split("\\|");
		for(String str:respArray){
			responses.add(str);
			Log.v("ELSERVICES","Responses: " +str);
		}

		String[] dateArray=date.split("\\|");
		for(String str:dateArray){
			Log.v("ELSERVICES", "GroundReport Date: "+str);
			dates.add(str);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy HH:mm");

		if(responses.size()==0)
			top_response="";
		else
			top_response=responses.get(responses.size()-1);
		
		Log.v("ELSERVICES","Current response: " +top_response);

		if(top_response.equals("")){
			Toast.makeText(GroundReportListActivity.this, "No new reports", 1000).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.ground_report_list, menu);
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
}
