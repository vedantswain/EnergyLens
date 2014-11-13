package com.iiitd.muc.energylens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
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

import com.iiitd.muc.energylens.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static int WASTAGE_NOTIFICATION_ID = 1;
	public static final int REPORT_NOTIFICATION_ID=1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	private String TAG="GCMDemo";
	public static String RECEIVER="com.iiitd.muc.energylens";

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
				if(extras.getString("api","none").contains("energy/report/notification/"))
					sendNotification(extras);
				else if(extras.getString("api","none").contains("energy/wastage/notification/"))
					sendWastageNotification(extras);

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

	private void sendWastageNotification(Bundle data){
		Log.i(TAG, "Received report: " + data.toString());
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		String msg_type=data.getString("msg_type");
		String api=data.getString("api");

		if(api.equals("energy/wastage/notification/")){

			Set<String> keys=data.keySet();
			JSONObject response=new JSONObject();

			String message="";
			
			try {
				for(String key:keys){
					response.put(key, data.get(key));
				}
				JSONObject options=new JSONObject(response.getString("options"));
				message=options.getString("message");
				WASTAGE_NOTIFICATION_ID=options.getInt("id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

			NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("Energy Wastage Detected")
			.setStyle(new NotificationCompat.BigTextStyle()
			.bigText(message))
			.setAutoCancel(true)
			.setContentText(message);

			Notification wasteReportNote=mBuilder.build();
			wasteReportNote.flags |= Notification.FLAG_AUTO_CANCEL;
			wasteReportNote.defaults |= Notification.DEFAULT_VIBRATE;
			wasteReportNote.defaults |= Notification.DEFAULT_LIGHTS;
			wasteReportNote.flags |= Notification.FLAG_SHOW_LIGHTS;
			wasteReportNote.ledARGB = 0xff00ff00;
			wasteReportNote.ledOnMS = 300;
			wasteReportNote.ledOffMS = 1000;

			mNotificationManager.notify(WASTAGE_NOTIFICATION_ID, wasteReportNote);
		}
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(Bundle data) {
		Log.i(TAG, "Received report: " + data.toString());
		mNotificationManager = (NotificationManager)
				this.getSystemService(Context.NOTIFICATION_SERVICE);

		String msg_type=data.getString("msg_type");
		String api=data.getString("api");

		if(api.equals("energy/report/notification/")){
			getGroundReportPreferences();
			parseGroundReportData(data);
		}

		String message;

		Intent intent=new Intent(this, GroundReportListActivity.class);

		if(groundReportResponses.size()>1){
			message=Integer.toString(groundReportResponses.size())+" new verification reports received";
		}
		else{
			message=" New verification reports received";
		}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("EnergyLens+")
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(message))
		.setContentIntent(contentIntent)
		.setAutoCancel(true)
		.setContentText(message);

		Notification groundReportNote=mBuilder.build();
		groundReportNote.flags |= Notification.FLAG_AUTO_CANCEL;
		groundReportNote.defaults |= Notification.DEFAULT_VIBRATE;
		groundReportNote.defaults |= Notification.DEFAULT_LIGHTS;
		groundReportNote.flags |= Notification.FLAG_SHOW_LIGHTS;
		groundReportNote.ledARGB = 0xff00ff00;
		groundReportNote.ledOnMS = 300;
		groundReportNote.ledOffMS = 1000;

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(REPORT_NOTIFICATION_ID, groundReportNote);

	}

	private void getGroundReportPreferences(){
		SharedPreferences sp=getSharedPreferences("GROUNDREPORT_PREFS",0);
		Log.v("ELSERVICES", "Loading GroundReport from saved data");

		if(sp.contains("JSON_RESPONSES")){
			String string=sp.getString("JSON_RESPONSES", "");
			String date=sp.getString("RESPONSE_DATES", "");
			//			parsePref(sp.getString("JSON_RESPONSE", ""));
			if(!string.equals("") && !date.equals(""))
				parseResponses(string,date);
		}

	}	

	private void parseGroundReportData(Bundle data){
		Log.v("ELSERVICES", "parse groundreport");

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
		groundReportResponses=new ArrayList<String>();
		groundReportDates=new ArrayList<String>();

		int responseCount=0;

		String[] respArray=string.split("\\|");
		for(String str:respArray){
			if(!str.equals(""))
				groundReportResponses.add(str);
			responseCount++;
		}

		int dateCount=0;

		String[] dateArray=date.split("\\|");
		for(String str:dateArray){
			Log.v("ELSERVICES", "GroundReport Date: "+str);
			if(!str.equals(""))
				groundReportDates.add(str);
			dateCount++;
		}

		Log.v("ELSERVICES", "GroundReport Responses: "+Integer.toString(responseCount)+" v/s "+groundReportResponses.size());
		Log.v("ELSERVICES", "GroundReport Dates: "+Integer.toString(dateCount)+" v/s "+groundReportDates.size());

	}

	private void storePreferences(String response){
		SharedPreferences bundleData=getSharedPreferences("GROUNDREPORT_PREFS",0);

		groundReportResponses.add(response);
		StringBuilder strings=new StringBuilder("");
		for(String str:groundReportResponses)
			strings.append("|"+str);

		Editor editor=bundleData.edit();
		editor.putString("JSON_RESPONSES", strings.toString());
		editor.putString("JSON_RESPONSE", response);

		strings=new StringBuilder("");
		groundReportDates.add(Long.toString(System.currentTimeMillis()));
		for(String str:groundReportDates)
			strings.append("|"+str);

		editor.putString("RESPONSE_DATES", strings.toString());
		editor.commit();

		Log.v("ELSERVICES", "store groundreport");
	}

}