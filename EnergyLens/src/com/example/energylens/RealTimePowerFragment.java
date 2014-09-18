package com.example.energylens;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RealTimePowerFragment extends Fragment{

	private static Handler mHandler = new Handler();
	GoogleCloudMessaging gcm;
	String SENDER_ID = "166229175411";
	Double power;
	static Double timestamp;
	static ArrayList<Double> firstTimes=new ArrayList<Double>();
	static ArrayList<Double> firstPowers=new ArrayList<Double>();
	Timer myTimer = new Timer();

	static Context context;
	static View rootView;

	private static int counter=0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_realtimepower, container, false);
		gcm = GoogleCloudMessaging.getInstance(getActivity());

		context=getActivity();

		//		firstUpdate();

		return rootView;
	}

	public void setupChart(){
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis((long) (timestamp*1000));
		drawChart(c.getTime(),Math.round(power));
	}

//	public static void firstSetupChart(){
//		mSeries=new TimeSeries("Real-Time Power");
//		Log.v("ELSERVICES", "First chart setup");
//		Calendar c=Calendar.getInstance();
//		int index=0;
//		for(index=0;index<firstTimes.size()-1;index++){
//			c.setTimeInMillis((long) (firstTimes.get(index)*1000));
//			mSeries.add(c.getTime(), firstPowers.get(index));
//			counter++;
//			Log.v("ELSERVICES", "First data: "+c.getTime().toString()+", "+firstPowers.get(index)+" index: "+index);
//		}
//		c.setTimeInMillis((long) (firstTimes.get(index)*1000));
//		drawChart(c.getTime(),Math.round(firstPowers.get(index)));
//	}

	static TimeSeries mSeries=new TimeSeries("Real-Time Power");;


	public static void drawChart(Date x, double y){


		Log.v("ELSERVICES", "Realtime draw chart");

		counter++;
		if(counter>60){
			mSeries.remove(0);
		}
		mSeries.add(x, y);

		Log.v("ELSERVICES", "Realtime mSeries ready set");

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setLineWidth(2);
		renderer.setColor(Color.RED);

		renderer.setDisplayBoundingPoints(true);
		//		renderer.setPointStyle(PointStyle.CIRCLE);
		//		renderer.setPointStrokeWidth(3);
		renderer.setDisplayChartValues(true);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);

		final XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);

		mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); 

		mRenderer.setXLabelsAlign(Align.RIGHT);
		mRenderer.setXLabelsAngle(-45);
		
		mRenderer.setZoomEnabled(false);
		mRenderer.setPanEnabled(false);
		mRenderer.setYAxisMin(0);
		mRenderer.setYAxisMax(y+500);
		mRenderer.setChartTitleTextSize(val);
		mRenderer.setLabelsColor(Color.DKGRAY);
		mRenderer.setYLabelsColor(0, Color.DKGRAY);
		mRenderer.setXLabelsColor(Color.DKGRAY);
		mRenderer.setLabelsTextSize(val);
		mRenderer.setLegendTextSize(val);
		mRenderer.setAxisTitleTextSize(val);
		mRenderer.setYTitle("Power (Watts)");
		mRenderer.setXTitle("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n Time");
		mRenderer.setYLabelsAlign(Align.RIGHT);
		mRenderer.setChartTitle("Real-Time Power Usage");
		mRenderer.setShowGrid(true);
		int[] margins={20,80,120,0};
		mRenderer.setMargins(margins);

		Log.v("ELSERVICES", "Realtime renderer set");

		final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(mSeries);


		Log.v("ELSERVICES", "Realtime dataset ready");

		GraphicalView chartView = ChartFactory.getTimeChartView(context, dataset, mRenderer,"Real Time Power");


		Log.v("ELSERVICES", "Realtime chartview ready");

		LinearLayout chart_container=(LinearLayout)rootView.findViewById(R.id.chartComparison);
		//		Log.i("ELSERVICES", "RTP "+chart_container.toString()+" "+System.currentTimeMillis());  

		chart_container.addView(chartView,0);

		Log.v("ELSERVICES", "Realtime chartview added");

		TextView textView=(TextView) rootView.findViewById(R.id.RealTimeText);
		textView.setText("Current power consumption: "+y+" Watts");
	}	

	private void UpdateGUI() {
		mHandler.post(mTask);
	}

//	public static void firstUpdate(){
//		mHandler.post(firstTask);
//	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(Common.CURRENT_VISIBLE==2)
					UpdateGUI();
			}
		}, 0, 1000);
	}

	@Override
	public void onPause() {
		super.onPause();
		myTimer.cancel();
	}

	private String getRealTimeData() {
		InputStream inputStream = null;
		try {

			TelephonyManager telephonyManager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
			Long devid=Long.parseLong(telephonyManager.getDeviceId());


			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(Common.SERVER_URL+Common.REALTIME_API);

			String json = "";

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("dev_id", devid);

			json = jsonObject.toString();

			StringEntity se = new StringEntity(json);
			//	        se.setContentType("application/json;charset=UTF-8");
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

			httpPost.setEntity(se);

			HttpResponse httpResponse = httpclient.execute(httpPost);

			inputStream = httpResponse.getEntity().getContent();
			StatusLine sl=httpResponse.getStatusLine();


			//			Log.v("ELSERVICES", Integer.toString(sl.getStatusCode()));


			StringBuffer sb=new StringBuffer();

			try {
				int ch;
				while ((ch = inputStream.read()) != -1) {
					sb.append((char) ch);
				}
				//				Log.v("ELSERVICES", "input stream: "+sb.toString());
			} catch (IOException e) {
				throw e;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}

			JSONObject response=new JSONObject(sb.toString());
			Iterator it=response.keys();
			String key=it.next().toString();
			timestamp=Double.parseDouble(key);
			power=Double.parseDouble(response.getString(key));

			//			Log.v("ELSERVICES", "timestamp: "+timestamp+", code: "+power);		
			return "RTP retrieved";

		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR: RTP response";
		}

	}

//	public static String getFirstTimeData(){
//		InputStream inputStream = null;
//		try {
//
//			TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//			Long devid=Long.parseLong(telephonyManager.getDeviceId());
//
//
//			DefaultHttpClient httpclient = new DefaultHttpClient();
//			HttpPost httpPost = new HttpPost(Common.SERVER_URL+Common.REALTIME_API+"past/");
//
//			String json = "";
//
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("dev_id", devid);
//
//			json = jsonObject.toString();
//
//			StringEntity se = new StringEntity(json);
//			//	        se.setContentType("application/json;charset=UTF-8");
//			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
//
//			httpPost.setEntity(se);
//
//			HttpResponse httpResponse = httpclient.execute(httpPost);
//
//			inputStream = httpResponse.getEntity().getContent();
//			StatusLine sl=httpResponse.getStatusLine();
//
//
//			Log.v("ELSERVICES", Integer.toString(sl.getStatusCode()));
//
//
//			StringBuffer sb=new StringBuffer();
//
//			try {
//				int ch;
//				while ((ch = inputStream.read()) != -1) {
//					sb.append((char) ch);
//				}
//				Log.v("ELSERVICES", "first input stream: "+sb.toString());
//			} catch (IOException e) {
//				throw e;
//			} finally {
//				if (inputStream != null) {
//					inputStream.close();
//				}
//			}
//
//			JSONObject response=new JSONObject(sb.toString());
//			Iterator it=response.keys();
//
//			firstTimes=new ArrayList<Double>();
//			firstPowers=new ArrayList<Double>();
//
//			while(it.hasNext()){
//				String key=it.next().toString();
//				firstTimes.add(Double.parseDouble(key));
//				firstPowers.add(Double.parseDouble(response.getString(key)));
//
//				//				Log.v("ELSERVICES","Realtime pair"+ key+" "+response.getString(key));
//			}
//
//			Log.v("ELSERVICES", "First time received");	
//
//			return "RTP first retrieved";
//
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "ERROR: RTP response";
//		}
//
//	}
//
//	public static void sendFirst(){
//		new AsyncTask<Void,String,String>() {
//			@Override
//			protected String doInBackground(Void... params) {
//				String msg = "Realtime First Data retrieved";
//				msg=getFirstTimeData();
//				Log.v("ELSERVICES", "Realtime First: "+msg);
//				return msg;
//			}
//
//			@Override
//			protected void onPostExecute(String msg) {
//				//				Log.i("ELSERVICES", msg);
//				firstSetupChart();
//			}
//		}.execute(null, null, null);
//	}

	public void sendMessage(){
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "Realtime Data retrieved";
				msg=getRealTimeData();
				//				Log.v("ELSERVICES", "Realtime: "+msg);
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.i("ELSERVICES", msg);
				if(msg.equals("RTP retrieved"))
					setupChart();
			}
		}.execute(null, null, null);
	}

	Runnable mTask = new Runnable() {
		public void run() {

			synchronized (this) {
				try {
					//					Log.v("ELSERVICES", "RTP ping: "+System.currentTimeMillis());
					sendMessage();

				}

				catch (Exception e) {
				}
			}
		}
	};

//	static Runnable firstTask = new Runnable() {
//		public void run() {
//
//			synchronized (this) {
//				try {
//					//					Log.v("ELSERVICES", "RTP ping: "+System.currentTimeMillis());
//					sendFirst();
//
//				}
//
//				catch (Exception e) {
//				}
//			}
//		}
//	};

	private void parseData(Bundle data){
		String msg_type, api;
		if(data.getString("msg_type")=="response" && data.getString("api")=="power/real-time/")
			Log.v("ELSERVICES", "RTP response: "+System.currentTimeMillis());

	} 


	//	private BroadcastReceiver receiver = new BroadcastReceiver() {
	//
	//		@Override
	//		public void onReceive(Context context, Intent intent) {
	//			Bundle bundle = intent.getExtras();
	//			if (bundle != null) {
	//				Bundle data = bundle.getBundle("Data");
	//				parseData(data);
	//				Log.i("ELSERVICES","RealTime receiver " +data.getString("api"));
	//			}
	//		}
	//	};
}