package com.example.energylens;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.energylens.AxlService.UnregisterTask;

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
import android.widget.Toast;

public class WiFiService extends Service {

	private static final int LENGTH_SHORT = 1000;
	WifiManager wifiMgr;
	WifiReceiver wifiRcvr;
	String log;
	public List<ScanResult> wifiList;
	private Timer timer;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
		Log.v("ELSERVICES", "wifiService created");
	}
	
	public void onDestroy() {
		
		stopSelf();
		mTask=null;
		unregisterReceiver(wifiRcvr);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("ELSERVICES","wifiService started "+System.currentTimeMillis());
		//Toast.makeText(this, "wifiService started", LENGTH_SHORT).show();
		Thread thr = new Thread(null, mTask, "AlarmService_Service");
		timer = new Timer();
		timer.schedule(new wifiUnregisterTask(), Constants.SAMPLE_TIME*1000);
		thr.start();
		//runWiFiTask();

		return START_NOT_STICKY;
	}
	
	class wifiUnregisterTask extends TimerTask {
		public void run() {
	    	Log.v("ELSERVICES","wifiService stopped "+System.currentTimeMillis());
		    try{	
	    		mTask=null;
				unregisterReceiver(wifiRcvr);
				timer.cancel();
		    }
		    catch(Exception e){
		    	Log.v("ELSERVICES","wifiService stopped "+System.currentTimeMillis());
		    }
		}
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
			Log.v("ELSERVICES",e.toString());
		}
		Log.i("ELSERVICES","Wifi scanning started on separate thread");
	}

	};
	

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			long epoch = System.currentTimeMillis();
			wifiList = wifiMgr.getScanResults();
			
			if (wifiList != null){
				
				for(ScanResult result:wifiList){

				try {
					log=epoch+","+result.BSSID+","+result.SSID+","+result.level;

					synchronized(this){
//							Log.v("ELSERVICES", log);
							LogWriter.wifiLogWrite(log);
						}
					}
				catch (Exception e) {
					Log.v("ELSERVICES",e.toString());
					}
				}
			}
			else{
				log=epoch+"," + "00:00:00:00:00" + ","+ "None" +","+ 1;
				synchronized(this){
					LogWriter.wifiLogWrite(log);
				}			}
		}

	}

}
