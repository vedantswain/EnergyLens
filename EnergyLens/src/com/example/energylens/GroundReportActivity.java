package com.example.energylens;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GroundReportActivity extends FragmentActivity implements TryAgainConnectionRefusedDialogFragment.TryAgainDialogListener{

	ArrayList<Fragment> fragmentList=new ArrayList<Fragment>();
	ArrayList<Long> usage=new ArrayList<Long>();
	ArrayList<Long> ids=new ArrayList<Long>();
	ArrayList<long[]> period=new ArrayList<long[]>();
	static ArrayList<Long> timeOfStay=new ArrayList<Long>();
	ArrayList<String> apps=new ArrayList<String>();
	ArrayList<String> locs=new ArrayList<String>();
	private long lastSyncInMillis;

	static ArrayList<Long> correctionIds=new ArrayList<Long>();
	static ArrayList<Boolean> correctionTF=new ArrayList<Boolean>();
	static ArrayList<String[]> correctionPairData=new ArrayList<String[]>();
	static ArrayList<String> correctOccupant=new ArrayList<String>();

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;
	String SENDER_ID = "166229175411";

	TextView start,end;
	JSONArray activities;
	JSONObject occupants;
	JSONArray appliances;

	static ArrayList<String> dev_ids=new ArrayList<String>();
	static ArrayList<String> occupantList=new ArrayList<String>();

	ArrayList<String> responses=new ArrayList<String>();
	ArrayList<String> dates=new ArrayList<String>();
	String current_response;
	int report_index;
	long current_date;

	private ProgressDialog progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ground_report);

		SharedPreferences sp=getSharedPreferences("GROUNDREPORT_PREFS",0);

		Intent intent=getIntent();
		Bundle extras=intent.getExtras();

		report_index=extras.getInt("index");
		Log.v("ELSERVICES", "Report no: "+report_index);

		if(sp.contains("JSON_RESPONSES")){
			Log.v("ELSERVICES", "Loading All responses from saved data "+sp.getString("JSON_RESPONSES", "")
					+"\t"+sp.getString("RESPONSE_DATES", ""));
			String string=sp.getString("JSON_RESPONSES", "");
			String date=sp.getString("RESPONSE_DATES", "");
			//			parsePref(sp.getString("JSON_RESPONSE", ""));
			if(!string.equals("") && !date.equals("")){
				//				Toast.makeText(GroundReportActivity.this, "Loaded last uncorrected report", 1000).show();
				parseResponses(string,date);
			}
			else{
				Toast.makeText(GroundReportActivity.this, "No new reports", 2000).show();
				finish();
			}
		}

		gcm = GoogleCloudMessaging.getInstance(this);
		//		setupMessage();
	}

	public void parseResponses(String string, String date){
		responses=new ArrayList<String>();
		dates=new ArrayList<String>();

		String[] respArray=string.split("\\|");
		for(String str:respArray){
			if(!str.equals(""))
				responses.add(str);
			Log.v("ELSERVICES","Responses: " +str);
		}

		String[] dateArray=date.split("\\|");
		for(String str:dateArray){
			Log.v("ELSERVICES", "GroundReport Date: "+str);
			if(!str.equals(""))
				dates.add(str);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy HH:mm");

		if(!dates.get(report_index).equals(""))
			current_date=Long.parseLong(dates.get(report_index));
		TextView reportDate=(TextView) findViewById(R.id.reportDate);
		reportDate.setText(dateFormat.format(current_date).toString());
		current_response=responses.get(report_index);
		Log.v("ELSERVICES","Current response: " +current_response);

		if(!current_response.equals("")){
			parsePref(current_response);	
		}
		else{
			Toast.makeText(GroundReportActivity.this, "No new reports", 2000).show();
			finish();
		}
	}

	static void changeCorrectionIds(long id,String[] pairData,int occupant,long time,int flag){
		if(flag>0){
			if(flag>1){
				int index=correctionIds.indexOf(id);
				correctionTF.remove(index);
				correctionTF.add(index, true);
			}
			else{
				correctionIds.add(id);
				correctionTF.add(true);
			}

		}
		else if(flag==0){
			int index=3676;
			if(correctionIds.contains(id)){
				index=correctionIds.indexOf(id);
				correctionTF.remove(index);
				correctionTF.add(index, false);
			}
			else{
				correctionIds.add(id);
				correctionTF.add(false);
			}
		}
		Log.v("ELSERVICES", "id added: "+correctionIds.indexOf(id));
		changeTimeOfStay(id,time);
		changeCorrectionPairData(id,pairData);
		changeOccupant(id,occupant);
	}

	static Boolean checkTimeOfStay(){
		for(long check:timeOfStay){
			if(check==0){
				Log.v("ELSERVICES", "time empty: "+timeOfStay.indexOf(check));
				return false;
			}
		}
		return true;
	}

	static void changeTimeOfStay(long id,long time){
		int index=correctionIds.indexOf(id);
		if(timeOfStay.size()>index)
			timeOfStay.remove(index);

		if(index<timeOfStay.size())	
			timeOfStay.add(index,time);
		else
			timeOfStay.add(time);
	}

	static void changeOccupant(long id, int pos){
		int index=correctionIds.indexOf(id);
		if(correctOccupant.size()>index)
			correctOccupant.remove(index);

		if(index<correctOccupant.size())	
			correctOccupant.add(index,dev_ids.get(pos));
		else
			correctOccupant.add(dev_ids.get(pos));
	}


	static void changeCorrectionPairData(long id,String[] pairData){
		int index=correctionIds.indexOf(id);
		if(correctionPairData.size()>index)
			correctionPairData.remove(index);

		if(index<correctionPairData.size())
			correctionPairData.add(index,pairData);
		else
			correctionPairData.add(pairData);
	}

	protected void onStart(){
		super.onStart();
		//		this.registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));
	}

	@Override
	public void onPause() {
		super.onPause();
		//		try{
		//			this.unregisterReceiver(receiver);
		//		}
		//		catch(Exception e){
		//			Log.e("ELSERVICES", e.getMessage());
		//		}
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

		LinearLayout ll = (LinearLayout)findViewById(R.id.groundReport);
		Log.v("ELSERVICES","before Remove all views, groundReport: " + ll.getChildCount());
		ll.removeAllViews();
		Log.v("ELSERVICES","Remove all views groundRport: " + ll.getChildCount());

		int index=0;
		for(String activity:apps){
			fragment=UsageReportFragment.newInstance(ids.get(index),activity, use.get(index),locs.get(index),period.get(index)[0],period.get(index)[1]);
			fragmentTransaction.add(R.id.groundReport, fragment,Long.toString(ids.get(index)));
			fragmentList.add(fragment);
			index++;
		}
		Log.v("ELSERVICES","GroundRport activity fragment count: " + index);
		fragmentTransaction.commit();
		fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	public void onDoneBtn(View view){
		Log.v("ELSERVICES", "Done");
		Log.v("ELSERVICES", ids.size()+" == "+correctionIds.size()+" ?");
		if((correctionIds.size()==ids.size() && ids.size()>0)){
			if(checkTimeOfStay()){
				Log.v("ELSERVICES", "going to correct");
				toCorrect();
			}
			else
				Toast.makeText(GroundReportActivity.this, "You've forgotten to fill time of stay", 1000).show();
		}
		else{
			Toast.makeText(GroundReportActivity.this, "You've not corrected all the activities", 1000).show();
		}
	}



	public void toCorrect(){
		Log.v("ELSERVICES", "correct");

		JSONObject data = new JSONObject();
		try {
			JSONObject options=new JSONObject();
			JSONArray activities=new JSONArray();

			Iterator<Long> it = ids.iterator();
			while(it.hasNext()){
				JSONObject activity=new JSONObject();
				Long id=it.next();
				try {
					if(correctionIds.contains(id)){
						int index=correctionIds.indexOf(id);
						activity.put("activity_id",id);
						activity.put("time_of_stay", timeOfStay.get(index));
						if(correctionPairData.size()>index){
							activity.put("to_appliance", correctionPairData.get(index)[0]);
							activity.put("to_location", correctionPairData.get(index)[1]);
						}
						if(correctOccupant.size()>index)
							activity.put("to_occupant", correctOccupant.get(index));
						activity.put("incorrect", correctionTF.get(index));
					}
					activities.put(activity);
					//					Log.v("ELSERVICES", activities.toString());				
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

			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			Long devid=Long.parseLong(telephonyManager.getDeviceId());

			data.put("dev_id", devid);
			data.put("options", options);

			progress = ProgressDialog.show(this, "Sending",
					"Sending corrections. Please wait...", true);
			setupHttp(data);

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void parseOccupants(){
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String devid=telephonyManager.getDeviceId();

		occupantList.clear();
		Iterator it=occupants.keys();
		while(it.hasNext()){
			String key=it.next().toString();
			if(!devid.equals(key)){
				dev_ids.add(key);
				try {
					occupantList.add(occupants.getString(key));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		dev_ids.add(0, "");
	}

	public void parsePref(String resp){
		try {
			JSONObject response=new JSONObject(resp);
			JSONObject options=new JSONObject(response.getString("options"));

			if(options!=null){
				occupants=options.getJSONObject("occupants");
				if(occupants!=null){
					parseOccupants();
				}
				activities=options.getJSONArray("activities");
				Log.v("ELSERVICES","GroundReport Activitiy count: " + activities.length());
				if(activities!=null){
					parseActivities();
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void storeRemaining(){

		correctionIds=new ArrayList<Long>();
		correctionPairData=new ArrayList<String[]>();

		SharedPreferences bundleData=getSharedPreferences("GROUNDREPORT_PREFS",0);

		StringBuilder strings=new StringBuilder("");
		for(String str:responses)
			strings.append("|"+str);

		Editor editor=bundleData.edit();
		editor.putString("JSON_RESPONSES", strings.toString());

		strings=new StringBuilder("");
		for(String str:dates)
			strings.append("|"+str);

		editor.putString("RESPONSE_DATES", strings.toString());
		editor.commit();

		//		if(responses.size()>0 && dates.size()>0){
		//			parseResponses(responses.get(responses.size()-1),dates.get(dates.size()-1));
		//		}
		//		else{
		//			Toast.makeText(GroundReportActivity.this, "No new reports", 1000).show();
		finish();
		//		}
	}

	private void parseData(String resp){
		Log.v("ELSERVICES", "parsedata");

		try {
			JSONObject response=new JSONObject(resp);
			if(response!=null)
				Log.v("ELSERVICES", "Reassign not null");


			Log.v("ELSERVICES","inference response: "+response.getString("status"));
			if(response.getString("status").equals("true")){
				if(responses.size()>0 && dates.size()>0){
					responses.remove(report_index);
					dates.remove(report_index);
					storeRemaining();
				}
			}
			else{
				Toast.makeText(GroundReportActivity.this, "There was an error. Try again", 1000).show();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 



	private void parseActivities(){
		Log.v("ELSERVICES", "parseactivities");
		ids=new ArrayList<Long>();
		period=new ArrayList<long[]>();
		usage=new ArrayList<Long>();
		apps=new ArrayList<String>();
		locs=new ArrayList<String>();

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
			Toast.makeText(GroundReportActivity.this, "no activities", 1000).show();
	}


	public void setupHttp(final JSONObject data){
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				msg = sendHttp(data);
				Log.i("ELSERVICES", "message sent to report: "+data.toString());
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				if(progress.isShowing())
					progress.dismiss();
				if(msg.contains("ERROR")){
					refusedTryAgain();
				}
					Log.i("ELSERVICES", msg);
			}
		}.execute(null, null, null);
	}

	public void refusedTryAgain(){
		DialogFragment newFragment = new TryAgainConnectionRefusedDialogFragment();
		newFragment.show(getSupportFragmentManager(), "Connection Refused");
	}
	
	public String sendHttp(JSONObject data){
		InputStream inputStream = null;

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(Common.SERVER_URL+"inference/reassign/");

		String json=data.toString();
		StringEntity se;
		try {
			se = new StringEntity(json);
			//	        se.setContentType("application/json;charset=UTF-8");
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

			httpPost.setEntity(se);

			HttpResponse httpResponse = httpclient.execute(httpPost);

			inputStream = httpResponse.getEntity().getContent();
			StatusLine sl=httpResponse.getStatusLine();


			Log.v("ELSERVICES", Integer.toString(sl.getStatusCode()));

			if(sl.getStatusCode()<200 || sl.getStatusCode()>=300)
				return "ERROR: "+sl.getStatusCode();

			StringBuffer sb=new StringBuffer();

			String message="";

			try {
				int ch;
				while ((ch = inputStream.read()) != -1) {
					sb.append((char) ch);
				}
				Log.v("ELSERVICES", "Report input stream: "+sb.toString());
				parseData(sb.toString());
				message=sb.toString();
			} catch (IOException e) {
				message=e.toString();
				throw e;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}
			return "REPORT: "+message;

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "ERROR: "+e1.toString();

		}
	}

	@Override
	public void onOk() {
		// TODO Auto-generated method stub
		toCorrect();
	}

	@Override
	public void onCancelNow() {
		// TODO Auto-generated method stub
		finish();
	}


}
