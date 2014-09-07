package com.example.energylens;

import java.io.IOException;
import java.io.InputStream;
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
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class RealTimePowerFragment extends Fragment{

	private Handler mHandler = new Handler();
	GoogleCloudMessaging gcm;
	String SENDER_ID = "166229175411";
	Double power,timestamp;
	Timer myTimer = new Timer();

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
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis((long) (timestamp*1000));
		drawChart(c.getTime(),Math.round(power));
	}

	TimeSeries mSeries = new TimeSeries("Real-Time Power");


	public void drawChart(Date x, double y){
		mSeries.add(x, y);
		counter++;
		if(counter>60){
			mSeries.remove(0);
		}

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setLineWidth(2);
		renderer.setColor(Color.RED);

		renderer.setDisplayBoundingPoints(true);
		//		renderer.setPointStyle(PointStyle.CIRCLE);
		//		renderer.setPointStrokeWidth(3);
		renderer.setDisplayChartValues(true);

		DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);
		
		final XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);

		mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); 

		mRenderer.setZoomEnabled(false);
		mRenderer.setPanEnabled(false);
		mRenderer.setYAxisMin(0);
		mRenderer.setYAxisMax(12000);
		mRenderer.setChartTitleTextSize(val);
		mRenderer.setLabelsColor(Color.DKGRAY);
		mRenderer.setYLabelsColor(0, Color.DKGRAY);
		mRenderer.setXLabelsColor(Color.DKGRAY);
		mRenderer.setLabelsTextSize(val);
		mRenderer.setLegendTextSize(val);
		mRenderer.setYTitle("Power");
		mRenderer.setXTitle("Time");
		mRenderer.setYLabelsAlign(Align.RIGHT);
		mRenderer.setChartTitle("Real-Time Power Consumption");
		mRenderer.setShowGrid(true);
		int[] margins={20,80,10,0};
		mRenderer.setMargins(margins);

		final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(mSeries);


		GraphicalView chartView = ChartFactory.getTimeChartView(getActivity(), dataset, mRenderer,"Real Time Power");

		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chartComparison);
		//		Log.i("ELSERVICES", "RTP "+chart_container.toString()+" "+System.currentTimeMillis());  

		chart_container.addView(chartView,0);

		TextView textView=(TextView) getView().findViewById(R.id.RealTimeText);
		textView.setText("Current power consumption: "+y+" W");
	}	

	private void UpdateGUI() {
		mHandler.post(mTask);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(Common.CURRENT_VISIBLE==2)
					UpdateGUI();
			}
		}, 0, 1000);
		//		Thread thr = new Thread(null, mTask, "RealTime_Power");
		//        thr.start();
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


			Log.v("ELSERVICES", Integer.toString(sl.getStatusCode()));


			StringBuffer sb=new StringBuffer();

			try {
				int ch;
				while ((ch = inputStream.read()) != -1) {
					sb.append((char) ch);
				}
				Log.v("ELSERVICES", "input stream: "+sb.toString());
			} catch (IOException e) {
				throw e;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}

			JSONObject response=new JSONObject(sb.toString());
			timestamp=Double.parseDouble(response.getString("timestamp"));
			power=Double.parseDouble(response.getString("power"));

			Log.v("ELSERVICES", "timestamp: "+timestamp+", code: "+power);		
			return "RTP retrieved";

		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR: RTP response";
		}

	}

	public void sendMessage(){
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "Realtime Data retrieved";
				msg=getRealTimeData();
				Log.v("ELSERVICES", "Realtime: "+msg);
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
					sendMessage();
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