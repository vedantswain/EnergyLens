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
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class LightService extends Service{


	private static final int LENGTH_SHORT = 1000;
	private SensorManager lightSensorManager;
	private Sensor lightSensor;
	int rate=SensorManager.SENSOR_DELAY_NORMAL;
	Timer timer;
	String log;
	private int SAMPLE_TIME=10;
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	private final IBinder mBinder = new Binder() {
	    @Override
	    protected boolean onTransact(int code, Parcel data, Parcel reply,
	        int flags) throws RemoteException {
	      return super.onTransact(code, data, reply, flags);
	    }
	    };
	
	 public void onCreate() {  
	        super.onCreate();
	        
	    }

	    @Override
	    public void onDestroy() {        
	    	//Log.v("ELSERVICES","lightService stopped "+System.currentTimeMillis());	
	        stopSelf();
	    }

	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {  
	    	Thread thr = new Thread(null, mTask, "AlarmService_Service");
	        thr.start();
	        
	        return START_NOT_STICKY; //makes sure services don't restart on stopping
	       
	    }
	    
	    Runnable mTask = new Runnable() {
	        public void run() {
	        	
	            synchronized (mBinder) {
	              try {
	               	  Start();
	            		}

	               catch (Exception e) {
	              }
	            }
	        }
	      };
	    
	    public void Start(){

			Log.v("ELSERVICES","lightService started "+System.currentTimeMillis());
			//Toast.makeText(this, "lightService started", LENGTH_SHORT).show();
			
			lightSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			lightSensor = lightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			
			if (lightSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
				  Log.v(SENSOR_SERVICE,"light sensor found!");
				  lightSensorManager.registerListener(lightSensorListener, lightSensor, rate);
				  			  
					
					timer = new Timer();
					timer.schedule(new UnregisterTask(), SAMPLE_TIME*1000);
				  }
				else {
					Log.v("ELSERVICES","Not found!");
				}

		}
		
		class UnregisterTask extends TimerTask {
			public void run() {
		    	Log.v("ELSERVICES","lightService stopped "+System.currentTimeMillis());
				lightSensorManager.unregisterListener(lightSensorListener);
				timer.cancel();
			}
		}
		
		private final SensorEventListener lightSensorListener = new SensorEventListener(){

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSensorChanged(SensorEvent event) {
				// TODO Auto-generated method stub
				
				if (event.sensor.getType() == Sensor.TYPE_LIGHT) 
				{	

					long epoch = System.currentTimeMillis();
					log = epoch + "," + event.values[0];
//					Log.v("ELSERVICES", log);
					}
				
				synchronized(this){	    
					LogWriter.lightLogWrite(log);					
				}

			}
			
		};
		

}
