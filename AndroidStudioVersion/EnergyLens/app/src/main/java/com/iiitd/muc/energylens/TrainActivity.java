package com.iiitd.muc.energylens;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


public class TrainActivity extends FragmentActivity implements ApplianceDialogFragment.ApplianceDialogListener,
LocationDialogFragment.LocationDialogListener,AddOtherDialogFragment.AddOtherDialogListener,AddOtherLocDialogFragment.AddOtherDialogListener{
	//private static final int Toast.LENGTH_SHORT = 1000;
	private AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,uploaderAlarmMgr;
	private PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,lightServicePendingIntent,magServicePendingIntent,uploaderServicePendingIntent;
	private Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,uploaderServiceIntent;
	private ViewFlipper viewFlipper;
	Double power;
	long startTime,stopTime;
	private Handler mHandler = new Handler();
	private Object lastLocation;
	private Object lastLabel;
	private String[] locations={"New Location","Dining Room","Drawing Room","Kitchen","Master Bedroom","Bedroom 2","Bedroom 3","Study","Lobby"};
	private String[] labels={"New Appliance","Fan","Light","TV","AC","Microwave","Geyser","Grinder","Iron"};
	private ArrayList<String> labelsList=new ArrayList<String>();
	private ArrayList<String> locList=new ArrayList<String>();

	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	private boolean audioBased,presenceBased;
	int audioBasedCount=0;
	int presenceBasedCount=0;

	private ArrayList<String> selectedApps=new ArrayList<String>();
	int selectedAppsCount=0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the user interface layout for this Activity
		// The layout file is defined in the project res/layout/main_activity.xml file
		setContentView(R.layout.train_activity);
		viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

		getUpdatedPreferences();

		Log.v("ELSERVICES", "Created during Status: "+Common.TRAINING_STATUS);

		if(Common.TRAINING_STATUS==1){
			viewFlipper.showNext();
		}

	}

	private void toggleServiceMessage(String message){
		Intent intent = new Intent();
		intent.setAction("EnergyLensPlus.toggleService");
		// add data
		intent.putExtra("message", message);

		Log.v("ELSERVICES", "Broadcast from Train to Main receiver");
		sendBroadcast(intent);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Log.v("ELSERVICES", "Training Status: "+Common.TRAINING_STATUS);
			if(Common.TRAINING_STATUS==0){
				if(Common.TRAINING_COUNT>0)
					toggleServiceMessage("startServices from Training");
				super.onOptionsItemSelected(item);
			}
			else if(Common.TRAINING_STATUS==2){
				Toast.makeText(TrainActivity.this, "Press the No to exit", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(TrainActivity.this, "Training in progress. Stop training to exit", Toast.LENGTH_SHORT).show();
			}

		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if(Common.TRAINING_STATUS==0){
			if(Common.TRAINING_COUNT>0)
				toggleServiceMessage("startServices from Training");
			super.onBackPressed();
		}
		else if(Common.TRAINING_STATUS==2){
			Toast.makeText(TrainActivity.this, "Press the No to exit", Toast.LENGTH_SHORT).show();
		}
		else{
			Toast.makeText(TrainActivity.this, "Training in progress. Stop training to exit", Toast.LENGTH_SHORT).show();
		}
	}

	private String getTrainingData() {
		InputStream inputStream = null;
		try {

			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			Long devid=Long.parseLong(telephonyManager.getDeviceId());


			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(Common.SERVER_URL+Common.TRAINDATA_API);

			String json = "";

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("dev_id", devid);
			jsonObject.put("start_time", startTime);
			jsonObject.put("end_time", stopTime);
			jsonObject.put("location", lastLocation);
			jsonObject.put("appliance", lastLabel);
			jsonObject.put("audio_based", audioBased);
			jsonObject.put("presence_based", presenceBased);

			json = jsonObject.toString();
			
			Log.v("ELSERVICES","Message sent to training"+json);

			StringEntity se = new StringEntity(json);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

			httpPost.setEntity(se);

			HttpResponse httpResponse = httpclient.execute(httpPost);

			inputStream = httpResponse.getEntity().getContent();
			StatusLine sl=httpResponse.getStatusLine();


			Log.v("ELSERVICES", Integer.toString(sl.getStatusCode()));


			StringBuffer sb=new StringBuffer();

			try {
				int ch;
				while ((ch = inputStream.read()) != -1) {
					sb.append((char) ch);
				}
				Log.v("ELSERVICES", "input stream: "+sb.toString());
			} catch (IOException e) {
				throw e;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}

			JSONObject response=new JSONObject(sb.toString());
			power=Double.parseDouble(response.getString("power"));

			Log.v("ELSERVICES", "power: "+power);		

			return "Training Data retrieved";

		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR: Training Data response";
		}

	}

	public void launchAppDialog(View view){
		Common.changeActivityApps(labels);
		DialogFragment newFragment = new ApplianceDialogFragment();
		newFragment.show(getSupportFragmentManager(), "Appliances");
	}

    public void stopSendMetadata(View view){
        launchTrainMoreDialog(true);
    }

    public void dontSendMetadata(View view){
        launchTrainMoreDialog(false);
    }
    
	public void launchTrainMoreDialog(boolean sendMeta){
		viewFlipper.showNext();
		lastLabel=Common.LABEL;
		lastLocation=Common.LOCATION;
        if(sendMeta)
		    mHandler.post(mTask);
		//		View prog_view=findViewById(R.id.trainingResume);
		//		prog_view.setVisibility(View.GONE);
		if(Common.TRAINING_STATUS==1){
			Toast.makeText(TrainActivity.this, "Training data collection stopped", Toast.LENGTH_SHORT).show();
		}
		Common.changeTrainingStatus(2);
		//		DialogFragment newFragment = new TrainMoreDialogFragment();
		//		newFragment.show(getSupportFragmentManager(), "TrainMore");
		Common.changeLabel("none");
		Common.changeLocation("none");
		Common.changePrefix("");
		Common.changeTrainingCount(Common.TRAINING_COUNT+1);
		updatePreferences(Common.TRAINING_STATUS);

		clearNotification();
		try {
			stopTime=System.currentTimeMillis();
			toggleServiceMessage("stopServices");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void launchLocDialog(View view){
		Common.changeActivityLocs(locations);
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
		editor.putInt("TRAINING_COUNT",Common.TRAINING_COUNT);
		// Commit the edits!
		editor.commit();
	}

	private void sendNotification(){
		String message="Training for "+Common.LABEL+" in "+Common.LOCATION+" started";

		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent=new Intent(this, CollectionTabActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);		

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("EnergyLens+")
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(message))
		.setContentText(message);

		mBuilder.setContentIntent(contentIntent);

		Notification trainingNote=mBuilder.build();
		trainingNote.flags |= Notification.FLAG_ONGOING_EVENT;

		mNotificationManager.notify(26194, trainingNote);
	}


	public void clearNotification() {
		NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(26194);
	}

	public void startService(View view){
		if(!(Common.LABEL.equals("none")) && !(Common.LOCATION.equals("none"))){
			if(audioBasedCount==0)
				Toast.makeText(TrainActivity.this, "Specify if appliance is audio based", Toast.LENGTH_SHORT).show();
			else if(presenceBasedCount==0)
				Toast.makeText(TrainActivity.this, "Specify if appliance is presence based", Toast.LENGTH_SHORT).show();
			else{
				Common.changePrefix("Training_");
				Log.v("ELSERVICES", Common.LABEL+" "+Common.LOCATION+" "+Common.FILE_PREFIX);
				viewFlipper.showNext();
				Common.changeTrainingStatus(1);
				updatePreferences(Common.TRAINING_STATUS);
				Toast.makeText(TrainActivity.this, "Training data collection started", Toast.LENGTH_SHORT).show();
				sendNotification();
				//			customNotification();
				startTime=System.currentTimeMillis();
				toggleServiceMessage("startServices from Training");
			}
		}
		else
			Toast.makeText(this, "Both appliance & location are required", Toast.LENGTH_SHORT).show();
	}


	public void sendMessage(){
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "Training Data retrieved";
				msg=getTrainingData();
				Log.v("ELSERVICES", "Training: "+msg);
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				TextView trainingPower=(TextView) findViewById(R.id.powerCon);
				if(power!=null){
					power=(double)Math.round(power * 1000) / 1000;
					trainingPower.setText(power.toString()+" W");
				}
				TextView trainingText=(TextView) findViewById(R.id.trainApp);
				trainingText.setText(lastLabel+" in "+lastLocation+"\n consumed: ");

				Log.i("ELSERVICES", msg);
			}
		}.execute(null, null, null);
	}

	Runnable mTask = new Runnable() {
		public void run() {

			synchronized (this) {
				try {
					Log.v("ELSERVICES", "Training Data ping: "+System.currentTimeMillis());
					sendMessage();
				}

				catch (Exception e) {
				}
			}
		}
	};


	@Override
	public void onAppSelected(String label) {
		// TODO Auto-generated method stub
		selectedAppsCount++;
		Log.v("ELSERVICES", "Label: "+ label);
		if(label.equals("New Appliance")){
			DialogFragment newFragment = new AddOtherDialogFragment();
			newFragment.show(getSupportFragmentManager(), "Add Other");
		}
		else if(selectedAppsCount>4){
			Common.changeLabel(selectedApps.toString());
			Toast.makeText(getApplicationContext(), "Cant add more than 4 appliances", Toast.LENGTH_SHORT).show();
		}
		else{
			if(selectedAppsCount==1)
				selectedApps.add(label);
			else if(selectedAppsCount>0){
				selectedApps.add(" + "+label);
			}
			StringBuilder sb=new StringBuilder();
			for(String apps:selectedApps)
				sb.append(apps);
			Common.changeLabel(sb.toString());
			TextView textView=(TextView) findViewById(R.id.appList);
			textView.setText(Common.LABEL);
		}
	}

	public void removeLastApp(View view){
		if(selectedAppsCount>0){
			selectedAppsCount--;
			selectedApps.remove(selectedApps.get(selectedApps.size()-1));
			StringBuilder sb=new StringBuilder();
			for(String apps:selectedApps)
				sb.append(apps);
			if(selectedAppsCount>0)
				Common.changeLabel(sb.toString());
			else
				Common.changeLabel("none");
			TextView textView=(TextView) findViewById(R.id.appList);
			textView.setText(Common.LABEL);
		}
	}

	@Override
	public void onLocSelected(String loc) {
		// TODO Auto-generated method stub
		Log.v("ELSERVICES", "Loc: "+ loc);
		if(loc.equals("New Location")){
			DialogFragment newFragment = new AddOtherLocDialogFragment();
			newFragment.show(getSupportFragmentManager(), "Add Other Loc");
		}
		else{
			TextView textView=(TextView) findViewById(R.id.setLoc);
			textView.setText(Common.LOCATION);
		}
	}

	public void onTrainMore(View view) {
		// TODO Auto-generated method stub
		Common.changeTrainingStatus(0);
		updatePreferences(Common.TRAINING_STATUS);
		viewFlipper.showPrevious();
		viewFlipper.showPrevious();
		selectedAppsCount=0;
		selectedApps=new ArrayList<String>();
		locList=new ArrayList<String>();
		audioBasedCount=0;
		presenceBasedCount=0;
		
		RadioButton rb;
		if(audioBased){
			rb=(RadioButton)findViewById(R.id.radio0);
			rb.setChecked(false);
		}
		else{
			rb=(RadioButton)findViewById(R.id.radio1);
			rb.setChecked(false);
		}
		
		if(presenceBased){
			rb=(RadioButton)findViewById(R.id.radio2);
			rb.setChecked(false);
		}
		else{
			rb=(RadioButton)findViewById(R.id.radio3);
			rb.setChecked(false);
		}
			
		TextView textView=(TextView) findViewById(R.id.appList);
		textView.setText("");
		textView=(TextView) findViewById(R.id.setLoc);
		textView.setText("Select Location");
	}

	public void onCancel(View view) {
		// TODO Auto-generated method stub
		try {
			Common.changeTrainingStatus(0);
			updatePreferences(Common.TRAINING_STATUS);
			Toast.makeText(TrainActivity.this, "Regular data collection started", Toast.LENGTH_SHORT).show();
			toggleServiceMessage("startServices from Training");
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
	}

	public void updateLabelList(){
		for(String label:labels){
			labelsList.add(label);
		}
	}

	public void updateLocList(){
		for(String loc:locations){
			locList.add(loc);
		}
	}


	public void updatePreferences(){
		SharedPreferences sp=getSharedPreferences(Common.EL_PREFS,0);
		Editor editor=sp.edit();
		StringBuilder sb=new StringBuilder();
		for(String app:labelsList){
			sb.append(app+",");
		}
		editor.putString("APP_LIST", sb.toString());

		sb=new StringBuilder();
		for(String loc:locList){
			sb.append(loc+",");
		}
		editor.putString("LOC_LIST", sb.toString());
		editor.commit();
	}

	public void getUpdatedPreferences(){
		SharedPreferences sp=getSharedPreferences(Common.EL_PREFS,0);
		String updatedLabels=sp.getString("APP_LIST", "");
		if(updatedLabels!=""){
			Common.changeActivityApps(updatedLabels.split(","));
			labels=updatedLabels.split(",");

		}
		String updatedLocs=sp.getString("LOC_LIST", "");
		if(updatedLocs!=""){
			Common.changeActivityLocs(updatedLocs.split(","));
			locations=updatedLocs.split(",");
		}
	}

	@Override
	public void onOtherSelected(String label) {
		// TODO Auto-generated method stub
		Common.changeLabel(label);
		updateLabelList();
		labelsList.add(label);
		labels=labelsList.toArray(labels);
		Common.changeActivityApps(labels);
		updatePreferences();
		if(selectedAppsCount>4){
			Common.changeLabel(selectedApps.toString());
			Toast.makeText(getApplicationContext(), "Cant add more than 4 appliances", Toast.LENGTH_SHORT).show();
		}
		else{
			if(selectedAppsCount==1)
				selectedApps.add(label);
			else if(selectedAppsCount>0){
				selectedApps.add(" + "+label);
			}
			StringBuilder sb=new StringBuilder();
			for(String apps:selectedApps)
				sb.append(apps);
			Common.changeLabel(sb.toString());
			TextView textView=(TextView) findViewById(R.id.appList);
			textView.setText(Common.LABEL);
		}
	}

	@Override
	public void onOtherLocSelected(String location) {
		// TODO Auto-generated method stub
		Common.changeLocation(location);
		updateLocList();
		locList.add(location);
		locations=locList.toArray(locations);
		Common.changeActivityLocs(locations);
		updatePreferences();
		TextView textView=(TextView) findViewById(R.id.setLoc);
		textView.setText(Common.LOCATION);
	}

	public void onAudioBasedClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		audioBasedCount=1;
		// Check which radio button was clicked
		switch(view.getId()) {
		case R.id.radio0:
			if (checked)
				audioBased=true;
			break;
		case R.id.radio1:
			if (checked)
				audioBased=false;
			break;
		}
	}

	public void onPresenceBasedClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();
		presenceBasedCount=1;
		// Check which radio button was clicked
		switch(view.getId()) {
		case R.id.radio2:
			if (checked)
				presenceBased=true;
			break;
		case R.id.radio3:
			if (checked)
				presenceBased=false;
			break;
		}
	}
}
