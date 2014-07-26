package com.example.energylens;

import java.util.Timer;
import java.util.TimerTask;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class AxlService extends Service {
	
	private SensorManager axlSensorManager;
	private Sensor axlSensor;
	int rate=SensorManager.SENSOR_DELAY_NORMAL;
	Timer timer;
	int SampleTime=10; //Sampling time in seconds
	String log;
	
	 @Override
	    public void onCreate() {        
	        super.onCreate();
	        
	    }

	    @Override
	    public void onDestroy() {        
	        super.onDestroy();
	        stopSelf();
	    }

	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {  
	    	 Start();
	    	 
	        return START_NOT_STICKY;
	       
	    }
	    
	public void Start(){
		axlSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		axlSensor = axlSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		if (axlSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
			  Log.v(SENSOR_SERVICE,"accelerometer found!");
			  axlSensorManager.registerListener(axlSensorListener, axlSensor, rate);
			  			  
				
				timer = new Timer();
				timer.schedule(new UnregisterTask(), SampleTime*1000);
			  }
			else {
				Log.v(SENSOR_SERVICE,"Not found!");
			}
	}
	
	class UnregisterTask extends TimerTask {
		public void run() {
			axlSensorManager.unregisterListener(axlSensorListener);
			timer.cancel(); 		
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private final SensorEventListener axlSensorListener = new SensorEventListener(){

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			float X, Y, Z;
			X = Y = Z = 0.0f;
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
			{	
				X = event.values[0];

				Y = event.values[1];
				Z = event.values[2];
				
				long epoch = System.currentTimeMillis();
				log=epoch+","+X+","+Y+","+Z;
				Log.v("ACCELEROMETER", log);
			}
			
//			synchronized(this){	    
//				Logger.acclLogger(log);	
//			}

		}
		
	};

}
