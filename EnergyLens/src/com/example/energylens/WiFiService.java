package com.example.energylens;

import java.util.List;

import com.example.sensormanagement.CommonFunctions;
import com.example.sensormanagement.Logger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

public class WiFiService extends Service {

	WifiManager wifiMgr;
	WifiReceiver wifiRcvr;
	public List<ScanResult> wifiList;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Thread thr = new Thread(null, mTask, "AlarmService_Service");
		thr.start();
		//runWiFiTask();

		return START_NOT_STICKY;
	}
	
	Runnable mTask = new Runnable() {
		public void run() {
			
			try{
				synchronized(this){
			wifiMgr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			if (wifiMgr.isWifiEnabled()==false) 
				wifiMgr.setWifiEnabled(true);
				wifiRcvr = new WifiReceiver();
			getApplicationContext().registerReceiver(wifiRcvr, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				wifiMgr.startScan();
				}
			
		}catch(Exception e){

			System.out.println(e.toString());
		}
		Log.i("","Registered broadcast WiFi service and starting WiFi scan");
	}

	};
	

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {

			wifiList = wifiMgr.getScanResults();
			//Log.i("AlarmService1", Boolean.toString(wifiList.isEmpty()));

			if (wifiList != null){
				
			
			for(ScanResult result:wifiList){


				try {

					long epoch = System.currentTimeMillis();

					wifi.add(result.SSID);


					SharedPreferences app_preferences1 = getSharedPreferences("CollectState",MODE_PRIVATE); 
					collectstate=app_preferences1.getInt("state", 0);

					
					log=epoch+","+result.BSSID+","+result.SSID+","+result.level+","+label;

					SharedPreferences app_preferences =getSharedPreferences("settings_data",MODE_PRIVATE); 

					CommonFunctions.setType(app_preferences.getString("type", "none"));

					CommonFunctions.setUniqueNo(app_preferences.getString("uniqueno", ""));

					if(collectstate==1){   

						synchronized(this){



							Logger.wifiLogger(log);


						}
					}

				} catch (Exception e) {

					e.printStackTrace();
				}

			}
			
			}
			else{
				long epoch = System.currentTimeMillis();
				log=epoch+"," + "00:00:00:00:00" + ","+ "None" +","+ 1 +","+label;
				Logger.wifiLogger(log);
			}
		}

	}

}
