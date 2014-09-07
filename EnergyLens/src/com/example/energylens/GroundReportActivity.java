package com.example.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GroundReportActivity extends Activity {

	ArrayList<Fragment> fragmentList=new ArrayList<Fragment>();
	ArrayList<Long> usage=new ArrayList<Long>();
	ArrayList<Long> ids=new ArrayList<Long>();
	ArrayList<long[]> period=new ArrayList<long[]>();
	ArrayList<String> apps=new ArrayList<String>();
	ArrayList<String> locs=new ArrayList<String>();
	private long lastSyncInMillis;

	static ArrayList<Long> correctionIds=new ArrayList<Long>();
	static ArrayList<String[]> correctionPairData=new ArrayList<String[]>();

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;
	String SENDER_ID = "166229175411";

	TextView start,end;
	JSONArray activities;
	JSONArray appliances;

	ArrayList<String> responses=new ArrayList<String>();
	ArrayList<String> dates=new ArrayList<String>();
	String current_response;
	long current_date;
	static int corrected_count=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ground_report);

		SharedPreferences sp=getSharedPreferences("GROUNDREPORT_PREFS",0);

		if(sp.contains("JSON_RESPONSES")){
			Log.v("ELSERVICES", "Loading GroundReport from saved data");
			String string=sp.getString("JSON_RESPONSES", "");
			String date=sp.getString("RESPONSE_DATES", "");
//			parsePref(sp.getString("JSON_RESPONSE", ""));
			if(!string.equals("") && !date.equals(""))
				parseResponses(string,date);
			if(ids!=null){
				setApps(ids,apps,usage,locs,period);
			}
			else
				Toast.makeText(getApplicationContext(), "no activity in this location", 1000).show();
		}

		gcm = GoogleCloudMessaging.getInstance(this);
		setupMessage();
	}

	public void parseResponses(String string, String date){
		String[] respArray=string.split("|");
		for(String str:respArray){
			responses.add(str);
		}

		String[] dateArray=date.split("|");
		for(String str:dateArray){
			dates.add(str);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy HH:mm");
		current_date=Long.parseLong(dateArray[dateArray.length-1]);
		TextView reportDate=(TextView) findViewById(R.id.reportDate);
		reportDate.setText(dateFormat.format(current_date).toString());
		current_response=respArray[respArray.length-1];
		parsePref(current_response);		
	}

	static void changeCorrectionIds(long id,String[] pairData,int flag){
		if(flag==1){
			correctionIds.add(id);
			if(corrected_count>0)
				corrected_count--;
		}
		else if(flag==0 && correctionIds.contains(id)){
			int index=correctionIds.indexOf(id);
			correctionIds.remove(index);
			corrected_count++;
			if(correctionPairData.contains(pairData)){
				int pdIndex=correctionPairData.indexOf(pairData);
				correctionPairData.remove(pdIndex);
				Log.v("ELSERVICES", "removed");
			}
		}
	}

	static void changeCorrectionPairData(long id,String[] pairData,int flag){
		int index=correctionIds.indexOf(id);
		if(flag==1){
			if(correctionPairData.contains(pairData)){
				int pdIndex=correctionPairData.indexOf(pairData);
				correctionPairData.remove(pdIndex);
				correctionPairData.add(pdIndex,pairData);
			}
			

		}else if(flag==0){
			if(correctionPairData.contains(pairData)){
				int pdIndex=correctionPairData.indexOf(pairData);
				correctionPairData.remove(pdIndex);
			}
		}
	}

	protected void onStart(){
		super.onStart();
		this.registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));
	}

	@Override
	public void onPause() {
		super.onPause();
		this.unregisterReceiver(receiver);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//		getMenuInflater().inflate(R.menu.ground_report, menu);
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

	public void setApps(ArrayList<Long> ids,ArrayList<String> apps,ArrayList<Long> use,ArrayList<String> locs,ArrayList<long[]> period){
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		UsageReportFragment fragment = new UsageReportFragment();

		int index=0;
		for(String activity:apps){
			fragment=UsageReportFragment.newInstance(ids.get(index),activity, use.get(index),locs.get(index),period.get(index)[0],period.get(index)[1]);
			fragmentTransaction.add(R.id.groundReport, fragment,Long.toString(ids.get(index)));
			fragmentList.add(fragment);
			index++;
		}
		fragmentTransaction.commit();
		fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	public void onDoneBtn(View view){
		Log.v("ELSERVICES", "Done");
		if(correctionIds.size()+corrected_count==ids.size() && ids.size()>0){
			//					Log.v("ELSERVICES", correctionIds.get(0)+" "+correctionIds.get(1)+" "+correctionIds.get(2)+" ");
			Log.v("ELSERVICES", correctionPairData.get(0)[0]+" "+correctionPairData.get(0)[1]);
			responses.remove(responses.size()-1);
			dates.remove(responses.size()-1);
			if(responses.size()>0 && dates.size()>0)
				storeRemaining();
			toCorrect();
		}
		else{
			Toast.makeText(this, "You've not corrected all the activities", 1000);
		}
	}



	public void toCorrect(){
		Log.v("ELSERVICES", "correct");

		Bundle data = new Bundle();
		data.putString("msg_type", "request");
		data.putString("api","inference/reassign/");

		JSONObject options=new JSONObject();
		JSONArray activities=new JSONArray();

		Iterator<Long> it = ids.iterator();
		while(it.hasNext()){
			JSONObject activity=new JSONObject();
			Long id=it.next();
			try {
				activity.put("activity_id",id);
				if(correctionIds.contains(id)){
					int index=correctionIds.indexOf(id);
					activity.put("id",id);
					activity.put("to_appliance", correctionPairData.get(index)[0]);
					activity.put("to_location", correctionPairData.get(index)[1]);
					activity.put("incorrect", "true");
				}
				else{
					activity.put("to_appliance", "");
					activity.put("to_location", "");
					activity.put("incorrect", "false");
				}
				activities.put(activity);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			options.put("activities", activities);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data.putString("options", options.toString());

		sendMessage(data);
	}

	public void parsePref(String resp){
		try {
			JSONObject response=new JSONObject(resp);
			JSONObject options=new JSONObject(response.getString("options"));

			if(options!=null){
				activities=options.getJSONArray("activities");

				if(activities!=null){
					parseActivities();
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void storePreferences(String response){
		SharedPreferences bundleData=getSharedPreferences("GROUNDREPORT_PREFS",0);

		responses.add("|"+response);
		StringBuilder strings=new StringBuilder();
		for(String str:responses)
			strings.append(str);

		Editor editor=bundleData.edit();
		editor.putString("JSON_RESPONSES", strings.toString());

		dates.add("|"+Long.toString(System.currentTimeMillis()));
		for(String str:dates)
			strings.append(str);

		editor.putString("RESPONSE_DATES", strings.toString());
		editor.commit();

	}

	private void storeRemaining(){
		SharedPreferences bundleData=getSharedPreferences("GROUNDREPORT_PREFS",0);

		StringBuilder strings=new StringBuilder();
		for(String str:responses)
			strings.append(str);

		Editor editor=bundleData.edit();
		editor.putString("JSON_RESPONSES", strings.toString());

		for(String str:dates)
			strings.append(str);

		editor.putString("RESPONSE_DATES", strings.toString());
		editor.commit();
		
		if(responses.size()>0 && dates.size()>0)
			parseResponses(responses.get(responses.size()-1),dates.get(dates.size()-1));
	}

	private void parseData(Bundle data){
		Log.v("ELSERVICES", "parsedata");

		if(data.getString("api").equals("energy/report/notification/")){
			try {
				Set<String> keys=data.keySet();
				JSONObject response=new JSONObject();
				if(response!=null)
					Log.v("ELSERVICES", "GroundReport not null");

				for(String key:keys){
					response.put(key, data.get(key));
				}

				JSONObject options=new JSONObject(response.getString("options"));

				storePreferences(response.toString());

				if(options!=null){
					activities=options.getJSONArray("activities");

					if(activities!=null){
						parseActivities();
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(data.getString("api").equals("inference/reassign/")){
			try {
				Set<String> keys=data.keySet();
				JSONObject response=new JSONObject();
				if(response!=null)
					Log.v("ELSERVICES", "Reassign not null");

				for(String key:keys){
					response.put(key, data.get(key));
				}

				JSONObject options=new JSONObject(response.getString("options"));

				if(options!=null){

					Log.v("ELSERVICES", options.getString("status"));

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	}


	private void parseActivities(){
		Log.v("ELSERVICES", "parseactivities");
		ids=new ArrayList<Long>();
		period=new ArrayList<long[]>();
		usage=new ArrayList<Long>();
		apps=new ArrayList<String>();
		locs=new ArrayList<String>();

		LinearLayout ll = (LinearLayout) findViewById(R.id.groundReport);
		ll.removeAllViews();

		int k=0;
		for(int i=0;i<activities.length();i++){
			JSONObject activity;
			try {
				activity = activities.getJSONObject(i);
				locs.add(activity.getString("location"));
				apps.add(activity.getString("name"));
				usage.add(activity.getLong("usage"));
				ids.add(activity.getLong("id"));
				long[] periodPoints=new long[2];
				periodPoints[0]=activity.getLong("start_time");
				periodPoints[1]=activity.getLong("end_time");
				Log.v("ELSERVICES", "Period points: "+periodPoints[0]+", "+periodPoints[1]);
				period.add(periodPoints);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(ids!=null){
			setApps(ids,apps,usage,locs,period);
		}
		else
			Toast.makeText(getApplicationContext(), "no activities", 1000).show();
	}


	public void setupMessage(){
		Bundle data = new Bundle();
		data.putString("msg_type", "request");
		data.putString("api","energy/report/notification/");

		JSONObject options=new JSONObject();

		data.putString("options", options.toString());

		sendMessage(data);
	}

	public void sendMessage(final Bundle data){
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					SecureRandom random = new SecureRandom();
					String randomId=new BigInteger(130, random).toString(32);

					gcm.send(SENDER_ID + "@gcm.googleapis.com", randomId, data);
					msg = "GroundReport sent message";
					Log.i("ELSERVICES", "message sent to report: "+data.toString());

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

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Bundle data = bundle.getBundle("Data");
				parseData(data);
				Log.i("ELSERVICES","GroundReport receiver " +data.getString("api"));
			}
		}
	};

}
