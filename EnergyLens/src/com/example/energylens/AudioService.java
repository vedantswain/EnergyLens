package com.example.energylens;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class AudioService extends Service {

	AudioData audio;

	int collectstate;
	static String Position="position";
	short[] recordedAudioBuffer,buffer,bufferval;
	int bufferRead;
	int sample;
	Context c=this;
	@Override

	public void onCreate() {



	}

	@Override
	public void onDestroy() {

		stopSelf();
		audio.stopReader();

		Log.i("ELSERVICES","Audio service stopped");

	}


	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};




	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.v("ELSERVICES","AudioService started "+System.currentTimeMillis());
		recordAudio();

		return Service.START_NOT_STICKY;
	}



	private void recordAudio(){

		synchronized(this){

			audio=new AudioData();
			audio.start();
		}
	}
}

