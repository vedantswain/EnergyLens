package com.example.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class PersonalEnergyFragment extends Fragment{
	GraphicalView chartView;
	XYMultipleSeriesRenderer appRenderer=new XYMultipleSeriesRenderer(),mRenderer = new XYMultipleSeriesRenderer();
	XYMultipleSeriesDataset appDataset=new XYMultipleSeriesDataset(),mDataset = new XYMultipleSeriesDataset();
	String[] appliances={"TV","Microwave","Computer","AC","Fan","Washing Machine"};
	int[] distribution={30,10,40,40,5,2,2};
	GoogleCloudMessaging gcm;
	String SENDER_ID = "166229175411";

	long totalConsumption;
	long[] hourlyConsumption;
	JSONArray activities;
	ArrayList<String> activity_names=new ArrayList<String>();
	ArrayList<Integer> activity_usage=new ArrayList<Integer>();

	String lastSyncTime;
	private Bundle latestData;
	private String LAST_SYNC_TIME="lastPEnSync";
	private String LAST_DATA="lastPEnData";
	private long lastSyncInMillis;
	private long maxY=0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	     
		View rootView = inflater.inflate(R.layout.fragment_personalenergy, container, false);
		gcm = GoogleCloudMessaging.getInstance(getActivity());
		return rootView;

	}

	public void adjustTime(int[] x){

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		int curr_hour=calendar.get(Calendar.HOUR_OF_DAY);
		for(int i=0;i<x.length;i++){
			x[i]=x[i]-curr_hour;
			if(x[i]<0){
				x[i]=24+x[i];
			}
		}
	}

	public void setupChart(){
		int[] x = { 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};

		long[] y = { 2000,3000,2800,3500,2500,2700,3000,2800,3500,3700,3800,2800,3500,3700,3800,2800,3500,3700,3800,2800,2000,2500,2700,3000};
		drawChart(y);
	}


	public void drawChart(long[] y){

		Log.v("ELSERVICES", "chart drawn");

		// Creating an  XYSeries for Income
		XYSeries mSeries = new XYSeries("Power");

		for(int i=0;i<y.length;i++){
			mSeries.add(i+1, y[i]);
		}


		XYSeriesRenderer bar_renderer = new XYSeriesRenderer();
		bar_renderer.setLineWidth(1);
		bar_renderer.setColor(Color.DKGRAY);
		bar_renderer.setDisplayBoundingPoints(true);
		bar_renderer.setDisplayChartValues(true);

		mRenderer.addSeriesRenderer(bar_renderer);
		//  	  mRenderer.addSeriesRenderer(line_renderer);

		mRenderer.setMarginsColor(Color.argb(0xff, 0xf0, 0xf0, 0xf0)); 

		mRenderer.setPanEnabled(true);
		mRenderer.setPanLimits(new double[] {0,y.length+1,0,maxY*1.5});
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setYAxisMin(0);
		mRenderer.setXAxisMin(0);
		mRenderer.setYAxisMax(maxY*1.5);
		mRenderer.setXAxisMax(y.length+1);
		mRenderer.setChartTitle("Your Energy Consumption for the Last 12 hours");
		mRenderer.setChartTitleTextSize(18);
		mRenderer.setLabelsColor(Color.BLACK);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setXTitle("Hours");
		mRenderer.setYTitle("Energy");
		mRenderer.setYLabelsAlign(Align.RIGHT);
		mRenderer.setBarSpacing(1);
		mRenderer.setClickEnabled(true);
		mRenderer.setSelectableBuffer(50);
		mRenderer.setShowGrid(true);

		mDataset.addSeries(mSeries);
		chartView = ChartFactory.getBarChartView(getActivity().getApplicationContext(), mDataset, mRenderer, BarChart.Type.DEFAULT);
		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chartPEn);
		chart_container.addView(chartView,0);


		chartView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.v("ELSERVICES", "Graph clicked");
				// handle the click event on the chart
				SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
//				Log.v("ELSERVICES", "Selected: "+seriesSelection.getSeriesIndex());
			}
		});

		chartView.setOnTouchListener(new View.OnTouchListener() {
			ViewPager mViewPager=CollectionTabActivity.mViewPager;
			ViewParent mParent= (ViewParent)getActivity().findViewById(R.id.PEnGroup);

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
	}



	public void setApps(ArrayList<String> apps,ArrayList<Integer> use){
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		DistributionFragment fragment = new DistributionFragment();

		int index=0;
		for(String activity:apps){
			fragment=DistributionFragment.newInstance(activity, use.get(index++));
			if(fragment.isAdded()){
				fragmentTransaction.replace(R.id.PEnGroup, fragment, activity);
				Log.v("ELSERVICES", "fragment replaced");
			}
			else
				fragmentTransaction.add(R.id.PEnGroup, fragment,activity);
		}

		fragmentTransaction.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.chartComp);
		chartView = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);
		getActivity().registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));
	}

	public void onPause(){
		super.onResume();
		getActivity().unregisterReceiver(receiver);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		if(savedInstanceState!=null){
			Log.v("ELSERVICES", "Loading from savedInstance");
			Bundle data = savedInstanceState.getBundle(LAST_DATA);
			lastSyncInMillis=savedInstanceState.getLong(LAST_SYNC_TIME);
			if(data!=null){
				parseData(data);
				updateChart();
				updateApps();
				updateViews(lastSyncInMillis);
			}
		}
		sendMessage();
	}


	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
		
		// Save the last sync time and last data received
		savedInstanceState.putLong(LAST_SYNC_TIME, lastSyncInMillis);
		if(latestData!=null)
			savedInstanceState.putBundle(LAST_DATA,latestData);
		Log.v("ELSERVICES", "Instance saved");

	}

	public class ChartZoomListener implements ZoomListener{

		@Override
		public void zoomApplied(ZoomEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void zoomReset() {
			// TODO Auto-generated method stub

		}

	}
	
	
	public void sendMessage(){
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					Bundle data = new Bundle();
					data.putString("msg_type", "request");
					data.putString("api","energy/personal/");

					JSONObject options=new JSONObject();

					try {
						options.put("start_time", "now");
						options.put("end_time", "last 12 hours");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					data.putString("options", options.toString());

					SecureRandom random = new SecureRandom();
					String randomId=new BigInteger(130, random).toString(32);

					gcm.send(SENDER_ID + "@gcm.googleapis.com", randomId, data);
					msg = "PersonalEnergy sent message";
					Log.i("ELSERVICES", "message sent to personal: "+data.toString());

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

	private void parseActivities(){
		Log.v("ELSERVICES", "parseactivities");
		for(int i=0;i<activities.length();i++){
			JSONObject activity;
			try {
				activity = activities.getJSONObject(i);
				activity_names.add(activity.getString("name"));
				activity_usage.add((int) ((activity.getLong("usage")*100)/totalConsumption));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.v("ELSERVICES", "parseactivities: "+activity_names.get(0).toString());
		Log.v("ELSERVICES", "parseactivities: "+activity_usage.get(0).toString());
	}

	public void parseConsumption(String arr){
		Log.v("ELSERVICES", "parseconsumption");

		String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

		hourlyConsumption = new long[items.length];

		for (int i = 0; i < items.length; i++) {
			try {
				hourlyConsumption[i] = Long.parseLong(items[i]);
				if(hourlyConsumption[i]>maxY)
					maxY=hourlyConsumption[i];
			} catch (NumberFormatException nfe) {};
		}

		Log.v("ELSERVICES", "parseconsumption: "+Arrays.toString(hourlyConsumption));
	}

	private void parseData(Bundle data){
		Log.v("ELSERVICES", "parsedata");
		String msg_type, api;

		if(data.getString("api").equals("energy/personal/")){
			try {
				latestData=data;
				Set<String> keys=data.keySet();
				JSONObject response=new JSONObject();
				if(response!=null)
					Log.v("ELSERVICES", "Response not null");

				for(String key:keys){
					response.put(key, data.get(key));
				}

				JSONObject options=new JSONObject(response.getString("options"));

				if(options!=null){
					totalConsumption=options.getLong("total_consumption");
					parseConsumption(options.getString("hourly_consumption"));

					Log.v("ELSERVICES", "Response Options: "+options.getLong("total_consumption")+" "+options.getString("hourly_consumption"));	
					activities=options.getJSONArray("activities");

					if(activities!=null){
						Log.v("ELSERVICES","Response Activities: "+ activities.getString(0));
						parseActivities();
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} 

	private void updateChart(){
		mRenderer = new XYMultipleSeriesRenderer();
		mDataset = new XYMultipleSeriesDataset();

		drawChart(hourlyConsumption);
	}

	private void updateApps(){
		setApps(activity_names,activity_usage);
	}

	private void updateViews(long syncTime){
		TextView totalVal=(TextView)getActivity().findViewById(R.id.totalVal);
		totalVal.setText(Long.toString(totalConsumption)+"Wh");

		DateFormat df=new DateFormat();
		lastSyncTime=df.format("dd/MM/yy HH:mm", syncTime).toString();
		TextView textView=(TextView)getActivity().findViewById(R.id.lastSyncText);
		textView.setText("Last synced on: "+lastSyncTime);

		lastSyncInMillis=System.currentTimeMillis();
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Bundle data = bundle.getBundle("Data");
				parseData(data);
				updateChart();
				updateApps();
				updateViews(System.currentTimeMillis());
			}
		}
	};

}