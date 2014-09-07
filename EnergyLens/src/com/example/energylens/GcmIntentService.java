package com.example.energylens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private String TAG="GCMDemo";
	public static String RECEIVER="com.example.energylens";
	
	ArrayList<String> groundReportResponses=new ArrayList<String>();
	ArrayList<String> groundReportDates=new ArrayList<String>();


	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		Log.i(TAG, "Handling intent "+messageType+" "+extras.isEmpty());

		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM
			 * will be extended in the future with new message types, just ignore
			 * any message types you're not interested in, or that you don't
			 * recognize.
			 */
			if (GoogleCloudMessaging.
					MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				Toast.makeText(getApplicationContext(),"Send error: " + extras.toString(),1000).show();
			} else if (GoogleCloudMessaging.
					MESSAGE_TYPE_DELETED.equals(messageType)) {
				Toast.makeText(getApplicationContext(),"Send error: " + extras.toString(),1000).show();
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.
					MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				// This loop represents the service doing some work.

				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				// Post notification of received message.
				if(extras.getString("api","none").contains("energy/"))
					sendNotification(extras);
				Log.i(TAG, "Received: " + extras.toString());
				publishData(extras); 
			}
			else{
				Log.i(TAG, "NOA");
			}

			Log.i("ELSERVICES", "publish data: "+extras.toString());
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}


	//Publish data for other activities to receive
	public void publishData(Bundle data){
		Intent intent = new Intent(RECEIVER);
		intent.putExtra("Data", data);
		sendBroadcast(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(Bundle data) {
		Log.i(TAG, "Received: " + data.toString());
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent=new Intent(this, CollectionTabActivity.class);
		Bundle extras=new Bundle();
		extras.putString("started_from", "notification");
		extras.putString("notif_id", Integer.toString(26194));
		intent.putExtras(extras);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);			

		String msg_type=data.getString("msg_type");
		String api=data.getString("api");

		String message="Msg_type: "+msg_type+" API: "+api;

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("GCM Notification")
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(message))
		.setContentText(message);

		if(api.equals("energy/report/notification/")){
			getGroundReportPreferences();
			parseGroundReportData(data);
		}

		SharedPreferences sp=getSharedPreferences(Common.EL_PREFS,0);
		long timeRcvd=sp.getLong("LAST_NOTIF_ARRIVAL", 0);
		boolean lastNotifClicked=sp.getBoolean("LAST_NOTIF_CLICKED", true);
		if(timeRcvd!=0 && !lastNotifClicked)
			LogWriter.notifLogWrite(timeRcvd+","+sp.getLong("LAST_NOTIF_ID",0)+","+"never");

		//store notification data
		Editor editor=sp.edit();
		editor.putLong("LAST_NOTIF_ARRIVAL",System.currentTimeMillis());
		editor.putLong("LAST_NOTIF_ID", NOTIFICATION_ID);
		editor.putBoolean("LAST_NOTIF_CLICKED", false);
		editor.commit();

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

	}

	private void getGroundReportPreferences(){
		SharedPreferences sp=getSharedPreferences("GROUNDREPORT_PREFS",0);

		if(sp.contains("JSON_RESPONSES")){
			Log.v("ELSERVICES", "Loading GroundReport from saved data");
			String string=sp.getString("JSON_RESPONSES", "");
			String date=sp.getString("RESPONSE_DATES", "");
			//			parsePref(sp.getString("JSON_RESPONSE", ""));
			if(!string.equals("") && !date.equals(""))
				parseResponses(string,date);
		}

	}	

	private void parseGroundReportData(Bundle data){
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

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void parseResponses(String string, String date){
		String[] respArray=string.split("|");
		for(String str:respArray){
			groundReportResponses.add(str);
		}

		String[] dateArray=date.split("|");
		for(String str:dateArray){
			groundReportDates.add(str);
		}

	}

	private void storePreferences(String response){
		SharedPreferences bundleData=getSharedPreferences("GROUNDREPORT_PREFS",0);

		groundReportResponses.add("|"+response);
		StringBuilder strings=new StringBuilder();
		for(String str:groundReportResponses)
			strings.append(str);

		Editor editor=bundleData.edit();
		editor.putString("JSON_RESPONSES", strings.toString());

		groundReportDates.add("|"+Long.toString(System.currentTimeMillis()));
		for(String str:groundReportDates)
			strings.append(str);

		editor.putString("RESPONSE_DATES", strings.toString());
		editor.commit();

	}

}