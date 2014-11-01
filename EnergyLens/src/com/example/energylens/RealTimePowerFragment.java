package com.example.energylens;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
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
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class RealTimePowerFragment extends Fragment{

	private static Handler mHandler = new Handler();
	GoogleCloudMessaging gcm;
	String SENDER_ID = "166229175411";
	Double power;
	protected static boolean firstFlag=true;
	static int minute=10;
	static Double timestamp;
	static ArrayList<Double> realTimes=new ArrayList<Double>();
	static ArrayList<Double> realPowers=new ArrayList<Double>();
	static Timer myTimer = new Timer();

	static double maxY=0;

	static Context context;
	static View rootView;

	private static int counter=0;
	static int multiplier=1;
	static ProgressDialog progress;

	static XYMultipleSeriesRenderer mRenderer;
	static LinearLayout chart_container;	
	static TimeSeries mSeries;
	static double xMinLimit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_realtimepower, container, false);
		gcm = GoogleCloudMessaging.getInstance(getActivity());

		context=getActivity();

		return rootView;
	}

	public void setupRenderer(){
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setLineWidth(2);
		renderer.setColor(Color.RED);

		renderer.setDisplayBoundingPoints(true);
		renderer.setDisplayChartValues(false);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);

		mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);

		mRenderer.setXLabels(10);
		mRenderer.setMarginsColor(Color.argb(0xff, 0xf0, 0xf0, 0xf0)); 

		mRenderer.setXLabelsAlign(Align.RIGHT);
		mRenderer.setXLabelsAngle(-45);

		mRenderer.setZoomEnabled(true);
		mRenderer.setPanEnabled(true,true);
		mRenderer.setClickEnabled(false);
		mRenderer.setInScroll(true);

		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setYAxisMin(0);
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
		int[] margins={20,80,80,0};
		mRenderer.setMargins(margins);

		mSeries=new TimeSeries("Real-Time Power");

		chart_container=(LinearLayout)rootView.findViewById(R.id.chartComparison);

	}

	public static void firstSetupChart(){
		try{
			Log.v("ELSERVICES", "First chart setup");
			Calendar c=Calendar.getInstance();

			Iterator<Double> timeIt=realTimes.iterator();
			Iterator<Double> powerIt=realPowers.iterator();


			if(!firstFlag){
				mSeries.remove(0);
				int i=(mSeries.getItemCount()-realTimes.size())+1;
				//Log.v("ELSERVICES", "index: "+i);
				int datapoints=mSeries.getItemCount();
				if(i<0)
					i=0;
				int index=i;
				while(i<datapoints){
					mSeries.remove(index);
					i++;
				}
			}

			//Log.v("ELSERVICES", "datapoints: "+mSeries.getItemCount()+" new points: "+realTimes.size());

			xMinLimit=realTimes.get(0);

			while(timeIt.hasNext() && powerIt.hasNext()){
				Double timeStamp=timeIt.next();
				multiplier=1;
				if((Math.floor(Math.log10(timeStamp))+1)==10)
					multiplier=1000;
				c.setTimeInMillis((long)(timeStamp*multiplier));
				double power=powerIt.next();
				mSeries.add(c.getTime(), power);
			}

			realTimes.clear();
			realPowers.clear();

			drawChart(mSeries);
		}
		catch(Exception e){
			Log.e("ELSERVICES", e.toString());
		}

	}

	public static void drawChart(TimeSeries mSeries){

		final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(mSeries);

		int marginY=100;
		if(mSeries.getMaxY()>1000)
			marginY=500;

		mRenderer.setYAxisMax(mSeries.getMaxY()+marginY);
//		mRenderer.setXAxisMin(mSeries.getMinX()+counter*multiplier);
		mRenderer.setXAxisMax(mSeries.getMaxX());
		mRenderer.setPanLimits(new double[] {mSeries.getMinX(),mSeries.getMaxX(),(double)0,mSeries.getMaxY()+marginY});

//		Log.v("ELSERVICES", "Realtime counter: "+counter+" multiplier: "+multiplier);
//		counter++;
		

		final GraphicalView chartView = ChartFactory.getTimeChartView(context, dataset, mRenderer,"Real Time Power");

		chartView.setOnTouchListener(new View.OnTouchListener() {
			ViewPager mViewPager=CollectionTabActivity.mViewPager;
			ViewParent mParent= (ViewParent)rootView.findViewById(R.id.RealTimeLayout);

			float mFirstTouchX,mFirstTouchY;

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
				if(seriesSelection!=null)
					Log.v("ELSERVICES", Float.toString(chartView.getX())+" touch "+Float.toString(chartView.getY()));

				// save the position of the first touch so we can determine whether the user is dragging
				// left or right
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mFirstTouchX = event.getX();
					mFirstTouchY = event.getY();
				}

				// when mViewPager.requestDisallowInterceptTouchEvent(true), the viewpager does not
				// intercept the events, and the drag events (pan, pinch) are caught by the GraphicalView

				// we want to keep the ViewPager from intercepting the event if:
				// 1- there are 2 or more touches, i.e. the pinch gesture
				// 2- the user is dragging to the left but there is no data to show to the right
				// 3- the user is dragging to the right but there is no data to show to the left
				if (event.getPointerCount() > 1
						|| (event.getX() < mFirstTouchX) 
						|| (event.getX() > mFirstTouchX)
						|| (event.getY() < mFirstTouchY)
						|| (event.getY() > mFirstTouchY)) {
					mViewPager.requestDisallowInterceptTouchEvent(true);
					mParent.requestDisallowInterceptTouchEvent(true);
				}
				else {
					mViewPager.requestDisallowInterceptTouchEvent(false);
					mParent.requestDisallowInterceptTouchEvent(true);
				}
				// TODO Auto-generated method stub
				return false;
			}

		});

		chart_container.addView(chartView,0);
		//		Log.v("ELSERVICES", "Realtime chartview added; "+mSeries.getItemCount());

		TextView textView=(TextView) rootView.findViewById(R.id.RealTimeText);
		textView.setText("Current power consumption: "+Math.round(mSeries.getY(mSeries.getItemCount()-1))+" Watts");
	}	

	private void UpdateGUI() {

		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(mTask);
			}
		}, 0, 1000);
	}

	public static void showProgress(){
		progress = ProgressDialog.show(context, "Fetching",
				"Fetching real-time data from server. Please wait...", true);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		setupRenderer();
		firstFlag=true;
		UpdateGUI();
	}

	@Override
	public void onPause() {
		super.onPause();
		myTimer.cancel();
	}


	public static String getRealTimeData(){
		InputStream inputStream = null;
		try {

			TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			Long devid=Long.parseLong(telephonyManager.getDeviceId());


			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(Common.SERVER_URL+Common.REALTIME_API+"past/");

			String json = "";

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("dev_id", devid);
			jsonObject.put("minutes", minute);

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
				//				Log.v("ELSERVICES", "first input stream: "+sb.toString());
			} catch (IOException e) {
				throw e;
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
			}

			JSONObject response=new JSONObject(sb.toString());
			Iterator it=response.keys();

			realTimes=new ArrayList<Double>();
			realPowers=new ArrayList<Double>();

			while(it.hasNext()){
				String key=it.next().toString();
				realTimes.add(Double.parseDouble(key));
				realPowers.add(Double.parseDouble(response.getString(key)));

				//				Log.v("ELSERVICES","Realtime pair"+ key+" "+response.getString(key));
			}

			Log.v("ELSERVICES", "First time received");	

			return "RTP first retrieved";


		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR: RTP response";
		}

	}

	public void sendMessage(){
		new AsyncTask<Void,String,String>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				//				chart_container.removeAllViews();
				realTimes.clear();
				realPowers.clear();
				//				Log.v("ELSERVICES", "Realtime preExecute");
				if(firstFlag)
					minute=10;
				else
					minute=1;
			}

			@Override
			protected String doInBackground(Void... params) {
				String msg = "Realtime Data retrieved";
				if(Common.CURRENT_VISIBLE==2)
					msg=getRealTimeData();

				Log.v("ELSERVICES", "Realtime: "+msg);
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				//				Log.i("ELSERVICES", msg);
				if(progress.isShowing())
					progress.dismiss();
				if(msg.equals("RTP first retrieved")){
					//					setupChart();
					firstSetupChart();				
					if(firstFlag)
						firstFlag=false;
				}
			}
		}.execute(null, null, null);
	}

	Runnable mTask = new Runnable() {
		public void run() {

			synchronized (this) { 
				try {
					if(Common.CURRENT_VISIBLE==2)
						sendMessage();
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

}