package com.iiitd.muc.energylens;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

public class BatteryService extends Service {
    Context c;
    int collectstate;

    static int level;
    static boolean isCharging;

    public static int lvl;
    public static boolean state;
    int status;
    public static	ArrayList<String> wifi = new ArrayList<String>();
    @Override

    public void onCreate() {

        Thread thr = new Thread(null, mTask, "AlarmService_Service");
        thr.start();

    }

    public BatteryService(){

    }

    @Override
    public void onDestroy() {

        stopSelf();
        mTask=null;
        Log.i("ELSERVICES", "stopped Battery service");
        // Tell the user we stopped.

    }

    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
        public void run() {

            synchronized (mBinder) {
                try {

                    Log.v("ELSERVICES","Battery Service started");
                    Battery();


                } catch (Exception e) {
                }
            }


            // Done with our work... stop the service!
            BatteryService.this.stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    /**
     * Show a notification while this service is running.
     */



    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                                     int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //  Logger.logInfo("Service Started onStartCommand");
        return Service.START_NOT_STICKY;
    }

    private void Battery()
    {

        registerReceiver(mBatInfoReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));

    }

    class UnregisterTask {
        public void run() {
            try {
                getApplicationContext().unregisterReceiver(mBatInfoReceiver);
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }



    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            level = i.getIntExtra("level", 0);

            status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            long epoch = System.currentTimeMillis();


            float batteryPct = level / (float)status;

            String stat=epoch+","+level+","+isCharging+","+batteryPct;

            //    log_battery(stat);
            synchronized (this){
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Common.EL_PREFS,0);
                boolean isCollecting=sharedPref.getBoolean("isCollecting",false);

                if (isCollecting) {
                    LogWriter.battLogWrite(stat);
                }
            }

            try {
                unregisterReceiver(this);
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }
        }

    };

    public static int battery_level(){

        lvl=level;
        return lvl;

    }

    public static boolean battery_state(){


        state=isCharging;
        return state;

    }



}

