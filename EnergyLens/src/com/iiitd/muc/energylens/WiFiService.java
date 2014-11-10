package com.iiitd.muc.energylens;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.iiitd.muc.energylens.AxlService.UnregisterTask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class WiFiService extends Service {

	private static final int LENGTH_SHORT = 1000;
	static WifiManager wifiMgr;
	WifiReceiver wifiRcvr;
	static String log,homeSSID,homeBSSID;
	public static List<ScanResult> wifiList;
	private Timer timer;

//	private static AlarmManager axlAlarmMgr,wifiAlarmMgr,audioAlarmMgr,lightAlarmMgr,magAlarmMgr,uploaderAlarmMgr;
//	private  static PendingIntent axlServicePendingIntent,wifiServicePendingIntent,audioServicePendingIntent,lightServicePendingIntent,magServicePendingIntent,uploaderServicePendingIntent;
//	private static Intent axlServiceIntent,wifiServiceIntent,audioServiceIntent,lightServiceIntent,magServiceIntent,uploaderServiceIntent;

	static boolean isStarted=true;

	static Context context;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();
		Log.v("ELSERVICES", "wifiService created");

		SharedPreferences sp=getSharedPreferences(Common.EL_PREFS,0);
		homeSSID=sp.getString("HOME_SSID", "");
		homeBSSID=sp.getString("HOME_BSSID", "");

		context=this;
	}

	public void onDestroy() {

		stopSelf();
		mTask=null;
		//		unregisterReceiver(wifiRcvr);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("ELSERVICES","wifiService started "+System.currentTimeMillis());
		//Toast.makeText(this, "wifiService started", LENGTH_SHORT).show();
		Thread thr = new Thread(null, mTask, "AlarmService_Service");
		timer = new Timer();
		timer.schedule(new wifiUnregisterTask(), Common.SAMPLE_TIME*1000);
		thr.start();
		//runWiFiTask();

		return START_NOT_STICKY;
	}

	class wifiUnregisterTask extends TimerTask {
		public void run() {
			Log.v("ELSERVICES","wifiService stopped "+System.currentTimeMillis());
			try{	
				mTask=null;
				//				unregisterReceiver(wifiRcvr);
				timer.cancel();
//				LogWriter.debugLogWrite(System.currentTimeMillis(),"Mag service stopped");
				stopSelf();
			}
			catch(Exception e){
				e.printStackTrace();
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
//					LogWriter.debugLogWrite(System.currentTimeMillis(),"Wifi scan started");
					wifiMgr.startScan();
				}

			}catch(Exception e){
				Log.v("ELSERVICES",e.toString());
				LogWriter.debugLogWrite(System.currentTimeMillis(),"Error while starting wifi scan "+e.getStackTrace());
			}
			Log.i("ELSERVICES","Wifi scanning started on separate thread");
		}

	};

	
	public static class WifiReceiver extends BroadcastReceiver {
		public WifiReceiver(){
			super();
		}


		private void toggleServiceMessage(String message){
			Intent intent = new Intent();
			intent.setAction("EnergyLensPlus.toggleService");
			  // add data
			  intent.putExtra("message", message);
			  Log.v("ELSERVICES", "Broadcast from Wifi to Main receiver");
			  context.sendBroadcast(intent);
		}
		
		public void onReceive(Context c, Intent intent) {
			boolean isHome=false;
			
			try{
				long epoch = System.currentTimeMillis();
//				Log.i("ELSERVICES","Wifi received in MainActivity");
				wifiList = wifiMgr.getScanResults();

				if (wifiList != null){
					for(ScanResult result:wifiList){
						if(homeSSID.equals(result.SSID) && homeBSSID.equals(result.BSSID) && result.level>-85){
							isHome=true;
							Log.v("ELSERVICES", "AP isHome: "+result.SSID+", "+result.BSSID);
							if(!isStarted){
								isStarted=true;
								Log.v("ELSERVICES", "Is home services started");
								LogWriter.debugLogWrite(System.currentTimeMillis(),"Not home services stopped");
								toggleServiceMessage("startServices from Wifi");
							}
							
							break;
						}
					}

					Log.v("ELSERVICES", "isHome: "+isHome);

					if(isHome){
						for(ScanResult result:wifiList){
							try {
								log=epoch+","+result.BSSID+","+result.SSID+","+result.level;
								//							Log.v("ELSERVICES", homeSSID+" = "+result.SSID+", "+homeBSSID+" = "+result.BSSID+", level: "+result.level);
								
								synchronized(this){
									LogWriter.wifiLogWrite(log);
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				else{
					log=epoch+"," + "00:00:00:00:00" + ","+ "None" +","+ 1;
					synchronized(this){
						LogWriter.wifiLogWrite(log);
					}			
				}
			}
			catch(Exception e){
				e.printStackTrace();
				LogWriter.debugLogWrite(System.currentTimeMillis(),"Wifi error while home: "+e.getMessage());
			}

			if(!isHome){
				try {
						Log.v("ELSERVICES", "Not home services stopped");
						LogWriter.debugLogWrite(System.currentTimeMillis(),"Not home services stopped");
						toggleServiceMessage("stopServices");
					
					isStarted=false;
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LogWriter.debugLogWrite(System.currentTimeMillis(),"Wifi error while NOT home: "+e.getMessage());
				}
			}

		}

	}

}
