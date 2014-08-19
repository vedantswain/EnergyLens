package com.example.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RealTimePowerFragment extends Fragment{

	private Handler mHandler = new Handler();
	GoogleCloudMessaging gcm;
    String SENDER_ID = "166229175411";
	
	private int counter=0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_realtimepower, container, false);
		gcm = GoogleCloudMessaging.getInstance(getActivity());
		
		return rootView;
	}

	public void setupChart(){
		Random rnd=new Random();
		counter++;
		Calendar c=Calendar.getInstance();
		drawChart(c.getTime(),rnd.nextInt(3000));
		}

	TimeSeries mSeries = new TimeSeries("Real-Time Power");


	public void drawChart(Date x, int y){
		mSeries.add(x, y);
		if(counter>30){
			mSeries.remove(0);
		}

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setLineWidth(2);
		renderer.setColor(Color.RED);

		renderer.setDisplayBoundingPoints(true);
//		renderer.setPointStyle(PointStyle.CIRCLE);
//		renderer.setPointStrokeWidth(3);
		renderer.setDisplayChartValues(true);

		final XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);

		mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); 
		
		mRenderer.setZoomEnabled(false);
		mRenderer.setYAxisMin(0);
		mRenderer.setChartTitleTextSize(14);
		mRenderer.setLabelsColor(Color.BLACK);
		mRenderer.setLabelsTextSize(18);
		mRenderer.setChartTitle("Real-Time Power Consumption");
		mRenderer.setShowGrid(true);

		final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(mSeries);


		GraphicalView chartView = ChartFactory.getTimeChartView(getActivity(), dataset, mRenderer,"Real Time Power");

		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chartComparison);
//		Log.i("ELSERVICES", "RTP "+chart_container.toString()+" "+System.currentTimeMillis());  

		chart_container.addView(chartView,0);
		
		TextView textView=(TextView) getView().findViewById(R.id.RealTimeText);
		textView.setText(y+"Wh");
	}	
	
	private void UpdateGUI() {
	      mHandler.post(mTask);
	   }
	
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		 Timer myTimer = new Timer();
	      myTimer.schedule(new TimerTask() {
	         @Override
	         public void run() {UpdateGUI();}
	      }, 0, 1000);
//		Thread thr = new Thread(null, mTask, "RealTime_Power");
		//        thr.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(receiver);
	}
	
	public void sendMessage(){
		 new AsyncTask<Void,String,String>() {
	         @Override
	         protected String doInBackground(Void... params) {
	             String msg = "";
	             try {
	                 Bundle data = new Bundle();
	                     data.putString("msg_type", "request");
	                     data.putString("api","power/real-time/");
	                     SecureRandom random = new SecureRandom();
	                     String randomId=new BigInteger(130, random).toString(32);
	                     
	                     gcm.send(SENDER_ID + "@gcm.googleapis.com", randomId, data);
	                     msg = "RTP sent message";
	                     Log.i("ELSERVICES", "message sent to RTP: "+randomId);
	             } catch (IOException ex) {
	                 msg = "Error :" + ex.getMessage();
	             }
	             return msg;
	         }

	         @Override
	         protected void onPostExecute(String msg) {
	            Log.i("ELSERVICES", msg);
	         }
	     }.execute(null, null, null);
	}

	Runnable mTask = new Runnable() {
		public void run() {

			synchronized (this) {
				try {
//					Log.v("ELSERVICES", "RTP ping: "+System.currentTimeMillis());
//					sendMessage();
					setupChart();
				}

				catch (Exception e) {
				}
			}
		}
	};
	
	private void parseData(Bundle data){
		String msg_type, api;
		if(data.getString("msg_type")=="response" && data.getString("api")=="power/real-time/")
				Log.v("ELSERVICES", "RTP response: "+System.currentTimeMillis());
			
		} 


	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Bundle data = bundle.getBundle("Data");
				parseData(data);
				Log.i("ELSERVICES","RealTime receiver " +data.getString("api"));
			}
		}
	};
}