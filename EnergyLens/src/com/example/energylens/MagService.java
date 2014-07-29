package com.example.energylens;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class MagService extends Service{

	private static final int LENGTH_SHORT = 1000;
	private SensorManager magSensorManager;
	private Sensor magSensor;
	int rate=SensorManager.SENSOR_DELAY_NORMAL;
	Timer timer;
	String log;
	private int SAMPLE_TIME=10;
	
	@Override
	public void onCreate() {


	}

	public MagService(){

	}

	@Override
	public void onDestroy() {

		//isMyServiceRunning();
		stopSelf();
	}


	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("ELSERVICES","MagService started "+System.currentTimeMillis());
		Start();

		//  Logger.logInfo("Service Started onStartCommand");
		return START_NOT_STICKY;
	}
	
	public void Start(){
		
		try{     
			magSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
			magSensor = magSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

			magSensorManager.registerListener( MagSensorListener , magSensor , rate);

			timer = new Timer();
			timer.schedule(new UnregisterTask(), SAMPLE_TIME*1000);

		}catch(Exception e){
			Log.i("ELSERVICES",e.toString());
		}
	}

	class UnregisterTask extends TimerTask {
		public void run() {
			Log.i("ELSERVICES","MagService stopped "+System.currentTimeMillis());
			magSensorManager.unregisterListener(MagSensorListener);
			timer.cancel(); 		


		}
	}


	private final SensorEventListener MagSensorListener = new SensorEventListener() {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
		}

		public void onSensorChanged(SensorEvent event) {
			float X, Y, Z;
			X = Y = Z = 0.0f;
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) 
			{	
				X = event.values[0];
				Y = event.values[1];
				Z = event.values[2];
				try {

					long epoch = System.currentTimeMillis();	
					log=epoch+","+X+","+Y+","+Z;
//					Log.v("ELSERVICES",log);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
				synchronized(this){	    
					LogWriter.magLogWrite(log);	
			
			}
		}
	};

}
