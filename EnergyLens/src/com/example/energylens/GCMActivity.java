package com.example.energylens;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.energylens.TryAgainDialogFragment.TryAgainDialogListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMActivity extends FragmentActivity implements TryAgainDialogListener,WifiListDialogFragment.WifiListDialogListener{

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "EnergyLens";
	private static final String PROPERTY_APP_VERSION = "1.0.0";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	static WifiManager wifiMgr;
	private static regWifiReceiver wifiRcvr;
	public static List<ScanResult> wifiList;
	public static ArrayList<String> SSIDs=new ArrayList<String>();
	public static ArrayList<String> BSSIDs=new ArrayList<String>();

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "166229175411";

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "GCMDemo";

	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;
	long devid;
	int reg_success=0;
	String regid="",regName,regEmail,serverUrl,regAppt="";
	private String wifiSSID="",wifiBSSID="";

	private static final String[] SERVER_URLS = new String[] {
		"http://192.168.20.217:9010/", "http://172.20.10.2:9010/"
	};

	@Override
	public void onCreate(Bundle savedInstanceState) { 	
		super.onCreate(savedInstanceState);

		//        Intent intent1 = new Intent(this,CollectionTabActivity.class);
		//		startActivity(intent1);

		if(Common.DOUBLE_BACK){
			finish();
		}

		setContentView(R.layout.activity_gcm);
		context = getApplicationContext();


		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, SERVER_URLS);
		AutoCompleteTextView textView = (AutoCompleteTextView)findViewById(R.id.serverUrl);
		textView.setAdapter(adapter);

		final CheckBox checkBox = (CheckBox) findViewById(R.id.firstInstall);
		checkBox.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(checkBox.isChecked()){
					Log.v("ELSERVICES", "Checked");
					EditText apptNo=(EditText) findViewById(R.id.apptNo);
					apptNo.setVisibility(View.VISIBLE);
					Button selectWifi=(Button) findViewById(R.id.selectWifi);
					selectWifi.setVisibility(View.VISIBLE);
				}else{
					Log.v("ELSERVICES", "unChecked");
					EditText apptNo=(EditText) findViewById(R.id.apptNo);
					apptNo.setVisibility(View.INVISIBLE);
					Button selectWifi=(Button) findViewById(R.id.selectWifi);
					selectWifi.setVisibility(View.INVISIBLE);
				}
			}
		});

		getUpdatedPreferences();

		// Check device for Play Services APK. If check succeeds, proceed with
		//  GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

			if (!regid.isEmpty()) {
				if(wifiRcvr!=null)
					unregisterReceiver(wifiRcvr);
				Intent intent = new Intent(this,CollectionTabActivity.class);
				startActivity(intent);
			}

		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

		WifiScan();

	}

	// You need to do the Play Services APK check here too.
	@Override
	protected void onResume() {

		if(Common.DOUBLE_BACK){
			Common.changeDoubleBack(false);
			finish();
		}
		super.onResume();
		checkPlayServices();
	}

	public void getUpdatedPreferences(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		Common.changeServerUrl(sharedPref.getString("SERVER_URL",Common.SERVER_URL));

		SharedPreferences trainingPref = getSharedPreferences(Common.EL_PREFS,0);
		Common.changeTrainingStatus(trainingPref.getInt("TRAINING_STATUS", 0));
		Common.changeLabel(trainingPref.getString("LABEL","none"));
		Common.changeLocation(trainingPref.getString("LOCATION", "none"));
		Common.changePrefix(trainingPref.getString("FILE_PREFIX", ""));
		Common.changeTrainingCount(trainingPref.getInt("TRAINING_COUNT", 0));

		Log.v("ELSERVICES", "Training onresume "+Common.TRAINING_STATUS+"\n Label "+Common.LABEL+"\n Location "+Common.LOCATION);

	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(GCMActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					sendRegistrationIdToBackend();

					// Persist the regID - no need to register again.
					//					storeRegistrationId(context, regid);


				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
					reg_success=0;
					tryAgain();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.i(TAG, msg);

				if(reg_success==1)
					toMain();

			}
		}.execute(null, null, null);

	}

	public void toMain(){
		if(wifiRcvr!=null)
			unregisterReceiver(wifiRcvr);
		Intent intent = new Intent(this,CollectionTabActivity.class);
		startActivity(intent);
	}

	public void onRegister(View view){
		EditText name=(EditText)findViewById(R.id.gcmName);
		EditText email=(EditText)findViewById(R.id.gcmEmail);
		EditText server_URL=(EditText)findViewById(R.id.serverUrl);
		EditText apptNo=(EditText)findViewById(R.id.apptNo);
		CheckBox checkBox=(CheckBox) findViewById(R.id.firstInstall);

		if(name.getText().toString().matches("")){
			Toast.makeText(context, "we need to call you something", 1000).show();
		}
		else if(checkBox.isChecked() && apptNo.getText().toString().matches("")){
			Toast.makeText(context, "you forgot to give your appartment number", 1000).show();
		}
		else{
			regName=name.getText().toString();
			regEmail=email.getText().toString();
			if(checkBox.isChecked()){
				regAppt=apptNo.getText().toString();
			}

			if(server_URL.getText().toString().matches(""))
				serverUrl=Common.SERVER_URL;
			else
				serverUrl=server_URL.getText().toString();
			Common.changeServerUrl(serverUrl);
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("SERVER_URL", serverUrl);
			editor.commit();

			Log.i("ELSERVICES", "Name: "+regName+'\n'+"Server: "+serverUrl);

			registerInBackground();

		}

	}

	public void tryAgain(){
		DialogFragment newFragment = new TryAgainDialogFragment();
		newFragment.show(getSupportFragmentManager(), "Appliances");
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		InputStream inputStream = null;
		String result = "";
		Log.i(TAG,"Server URL: "+ Common.SERVER_URL);
		try {
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			devid=Long.parseLong(telephonyManager.getDeviceId());

			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(Common.SERVER_URL+Common.REG_API);

			String json = "";

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("registration_id", regid);
			jsonObject.put("dev_id", devid);
			jsonObject.put("user_name", regName);
			jsonObject.put("email_id", regEmail);
			jsonObject.put("apt_no", regAppt);
			
			JSONObject jsonHomeAp=new JSONObject();
			jsonHomeAp.put("ssid", wifiSSID);
			SSIDs.remove(wifiSSID);
			jsonHomeAp.put("macid", wifiBSSID);
			BSSIDs.remove(wifiBSSID);
			jsonObject.put("home_ap", jsonHomeAp);
			
			int index=0;
			JSONArray jsonOtherAps=new JSONArray();
			for(String ssid:SSIDs){
				JSONObject jsonOtherAp=new JSONObject();
				jsonOtherAp.put("ssid", ssid);
				jsonOtherAp.put("bssid", BSSIDs.get(index));
				index++;
				jsonOtherAps.put(jsonOtherAp);
			}
			jsonObject.put("other_ap", jsonOtherAps);

			json = jsonObject.toString();

			Log.v(TAG, "registration_id: "+regid+" dev_id: "+devid);

			StringEntity se = new StringEntity(json);
			//        se.setContentType("application/json;charset=UTF-8");
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

			httpPost.setEntity(se);

			//        httpPost.addHeader("host", serverUrl);
			//        httpPost.addHeader("Accept", "application/json");
			//        httpPost.addHeader("Content-type", "application/json");

			HttpResponse httpResponse = httpclient.execute(httpPost);

			inputStream = httpResponse.getEntity().getContent();
			StatusLine sl=httpResponse.getStatusLine();


			Log.v(TAG,"Status code: " + Integer.toString(sl.getStatusCode()));

			if(sl.getStatusCode()!=200){
				reg_success=0;
				regid="";
				tryAgain();
			}

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
			String serverResponseType=response.getString("type");
			int serverResponseCode=response.getInt("code");
			String serverResponseMessage=response.getString("message");
			Log.v("ELSERVICES", "type: "+serverResponseType+'\n'+"code: "+serverResponseCode+'\n'+"message+: "+serverResponseMessage);

			if(serverResponseCode==4 ){
				reg_success=0;
				regid="";
				tryAgain();
			}
			else if(serverResponseCode==3){
				reg_success=1;
				storeRegistrationId(context, regid);
			}
			else
				tryAgain();

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}

		Log.i(TAG, result);
	}


	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId "+regId+" on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}


	@Override
	public void onTryAgain() {
		// TODO Auto-generated method stub
		registerInBackground();	
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		finish();	
	}

	public void WifiScan(){
		try{
			synchronized(this){
				wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
				if (wifiMgr.isWifiEnabled()==false) 
					wifiMgr.setWifiEnabled(true);
				wifiRcvr = new regWifiReceiver();
				getApplicationContext().registerReceiver(wifiRcvr, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				wifiMgr.startScan();
			}

		}catch(Exception e){
			Log.v("ELSERVICES",e.toString());
		}
	}

	public static class regWifiReceiver extends BroadcastReceiver {
		public regWifiReceiver(){
			super();
		}
		public void onReceive(Context c, Intent intent) {
			try{
				wifiList = wifiMgr.getScanResults();
				Log.i("ELSERVICES","Wifi: "+wifiList.get(0).toString());
				for(ScanResult result:wifiList){
					if(!SSIDs.contains(result.SSID)){
						SSIDs.add(result.SSID);
						BSSIDs.add(result.BSSID);
						Log.v("ELSERVICES", "Wifi SSID: "+result.SSID);
					}
				}

			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

	}

	public void launchWifiDialog(View view){
		DialogFragment newFragment = new WifiListDialogFragment();
		newFragment.show(getSupportFragmentManager(), "Wifi SSIDs");
	}


	@Override
	public void onWifiSelected(String ssid, int index) {
		// TODO Auto-generated method stub
		Button selectWifi=(Button) findViewById(R.id.selectWifi);
		if(ssid!="No Wifi available"){
			wifiSSID=ssid;
			wifiBSSID=BSSIDs.get(index);
			selectWifi.setText(ssid);
		}
		else{
			wifiSSID="";
			wifiBSSID="";
		}
	}

}