package com.iiitd.muc.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
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
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iiitd.muc.energylens.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class PersonalEnergyFragment extends Fragment {
	public static GraphicalView chartView;
	XYMultipleSeriesRenderer appRenderer=new XYMultipleSeriesRenderer(),mRenderer = new XYMultipleSeriesRenderer();
	XYMultipleSeriesDataset appDataset=new XYMultipleSeriesDataset(),mDataset = new XYMultipleSeriesDataset();
	String[] appliances={"TV","Microwave","Computer","AC","Fan","Washing Machine"};
	int[] distribution={30,10,40,40,5,2,2};
	static GoogleCloudMessaging gcm;
	static String SENDER_ID = "166229175411";

	long totalConsumption;
	long totalUsage;
	long[] hourlyUsage;
	JSONArray activities;
	ArrayList<String> activity_names=new ArrayList<String>();
	ArrayList<Integer> activity_usage=new ArrayList<Integer>();
	ArrayList<Long> activity_values=new ArrayList<Long>();

	ArrayList<Fragment> fragmentList=new ArrayList<Fragment>();

	String lastSyncTime;
	private Bundle latestData;
	private String LAST_SYNC_TIME="lastPEnSync";
	private String LAST_DATA="lastPEnData";
	private long lastSyncInMillis;
	private long maxY=0;
	private String PREFS_NAME="PEN_PREFS";

	View inflateView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	     
		View rootView = inflater.inflate(R.layout.fragment_personalenergy, container, false);
		gcm = GoogleCloudMessaging.getInstance(getActivity());
		inflateView=rootView;
		return rootView;

	}


	public void setupChart(){
		int[] x = { 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};

		long[] y = { 2000,3000,2800,3500,2500,2700,3000,2800,3500,3700,3800,2800,3500,3700,3800,2800,3500,3700,3800,2800,2000,2500,2700,3000};
		drawChart(y);
	}


	public void drawChart(long[] y){

		Log.v("ELSERVICES", "chart drawn");

		// Creating an  XYSeries for Income
		XYSeries mSeries = new XYSeries("Energy Consumed");

		for(int i=0;i<y.length;i++){
			mSeries.add(i+1, y[i]);
		}


		XYSeriesRenderer bar_renderer = new XYSeriesRenderer();
		bar_renderer.setLineWidth(1);
		bar_renderer.setColor(Color.rgb(0, 153, 153));
		bar_renderer.setDisplayBoundingPoints(true);
		bar_renderer.setDisplayChartValues(true);

		DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);

		//		DateFormat df=new DateFormat();
		//		String from_time=df.format("dd MMM yy HH:mm", Common.TIME_PERIOD_START).toString();
		//		String to_time=df.format("dd MMM yy HH:mm", Common.TIME_PERIOD_END).toString();

		mRenderer.setXLabels(0);

		int daySwitch=0;
		for(int i=0;i<y.length;i++){
			long currTime=lastSyncInMillis;
			long graphTime=currTime-((y.length-i)*60*60*1000);
			String text=DateFormat.format("HH:mm",graphTime).toString();
			if(daySwitch==0 && text.contains("00:")){
				text=DateFormat.format("dd/MM HH:mm",graphTime).toString();
				daySwitch=1;
			}
			mRenderer.addXTextLabel(i+1, text);
		}

		mRenderer.setXLabelsAlign(Align.RIGHT);
		mRenderer.setXLabelsAngle(-45);

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
		mRenderer.setChartTitle("Your energy usage in the last 12 hours");
		mRenderer.setChartTitleTextSize(val);
		mRenderer.setLabelsColor(Color.DKGRAY);
		mRenderer.setYLabelsColor(0, Color.DKGRAY);
		mRenderer.setXLabelsColor(Color.DKGRAY);
		mRenderer.setLabelsTextSize(val);
		mRenderer.setXTitle("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n Time");
		mRenderer.setYTitle("Energy (Wh)");
		mRenderer.setAxisTitleTextSize(val);
		mRenderer.setLegendTextSize(val);
		mRenderer.setYLabelsAlign(Align.RIGHT);
		mRenderer.setBarSpacing(1);
		mRenderer.setClickEnabled(true);
		mRenderer.setSelectableBuffer(50);
		mRenderer.setShowGrid(true);
		int[] margins={20,80,120,0};
		mRenderer.setMargins(margins);


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
			ViewParent mParent= (ViewParent)inflateView.findViewById(R.id.PEnGroup);

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



	public void setApps(ArrayList<String> apps,ArrayList<Long> vals, ArrayList<Integer> use){
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		DistributionFragment fragment = new DistributionFragment();

		int index=0;
		for(String activity:apps){
			fragment=DistributionFragment.newInstance(activity,vals.get(index),use.get(index++));
			fragmentTransaction.add(R.id.PEnDist, fragment,activity);
			fragmentList.add(fragment);
		}
		fragmentTransaction.commit();
		fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	@Override
	public void onResume() {
		super.onResume();
		LinearLayout layout = (LinearLayout) inflateView.findViewById(R.id.chartComp);
		chartView = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);
		getActivity().registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));
		Date start=new Date(Common.TIME_PERIOD_START);
		Date end=new Date(Common.TIME_PERIOD_END);
		Log.v("ELSERVICES", "From: "+start.toString()+" To: "+end.toString());
		if(Common.CURRENT_VISIBLE==1){
			Log.v("ELSERVICES", "personal");
			if(System.currentTimeMillis()-Common.PERSONAL_LAST_SENT>Common.SEND_REQUEST_INTERVAL*60*1000){
				Common.changeLastSent(1,System.currentTimeMillis());
				sendMessage();
			}
		}
	}

	public void onPause(){
		super.onResume();
		getActivity().unregisterReceiver(receiver);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

		SharedPreferences sp=getActivity().getSharedPreferences(PREFS_NAME,0);

		if(sp.contains(LAST_SYNC_TIME)){
			Log.v("ELSERVICES", "Loading PEn from saved data");
			lastSyncInMillis=sp.getLong(LAST_SYNC_TIME,System.currentTimeMillis());
			parsePref(sp.getString("JSON_RESPONSE", ""));
			setViews();
			updateChart();
			updateApps();
			updateViews(lastSyncInMillis);
		}
	}

	public void parsePref(String resp){
		try {
			JSONObject response=new JSONObject(resp);
			JSONObject options=new JSONObject(response.getString("options"));

			if(options!=null && options.length()>0){
				totalConsumption=options.getLong("total_consumption");
				totalUsage=options.getLong("total_usage");
				parseUsage(options.getString("hourly_usage"));

				Log.v("ELSERVICES", "Response Options: "+options.getLong("total_usage")+" "+options.getString("hourly_usage"));	
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


	public static void sendMessage(){
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
						if(Common.TIME_PERIOD_CHANGED){
							options.put("start_time", Common.TIME_PERIOD_START);
							options.put("end_time", Common.TIME_PERIOD_END);
						}
						else{
							options.put("start_time", "now");
							options.put("end_time", "last 12 hours");
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if(options!=null && options.length()>0)
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
				activity_values.add(activity.getLong("usage"));
				activity_usage.add((int) ((activity.getLong("usage")*100)/totalUsage));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//		Log.v("ELSERVICES", "parseactivities: "+activity_names.get(0).toString());
		//		Log.v("ELSERVICES", "parseactivities: "+activity_usage.get(0).toString());
	}

	public void parseUsage(String arr){
		Log.v("ELSERVICES", "parseusage");

		String[] items = arr.replaceAll("\\[", "").replaceAll("\\]", "").split(",");

		hourlyUsage = new long[items.length];

		for (int i = 0; i < items.length; i++) {
			try {
				hourlyUsage[i] = Long.parseLong(items[i]);
				if(hourlyUsage[i]>maxY)
					maxY=hourlyUsage[i];
			} catch (NumberFormatException nfe) {};
		}

		Log.v("ELSERVICES", "parseusage: "+Arrays.toString(hourlyUsage));
	}

	private void parseData(Bundle data){
		Log.v("ELSERVICES", "parsedata");
		String msg_type, api;

		if(data.getString("api").equals("energy/personal/")){

			LinearLayout appDist = (LinearLayout)inflateView.findViewById(R.id.PEnDist);
			Log.v("ELSERVICES","before Remove all views: " + appDist.getChildCount());
			appDist.removeAllViews();
			Log.v("ELSERVICES","Remove all views: " + appDist.getChildCount());

			activity_names.clear();
			activity_values.clear();
			activity_usage.clear();

			try {
				latestData=data;
				Set<String> keys=data.keySet();
				JSONObject response=new JSONObject();
				if(response!=null)
					Log.v("ELSERVICES", "Response not null");

				for(String key:keys){
					response.put(key, data.get(key));
				}

				SharedPreferences bundleData=getActivity().getSharedPreferences(PREFS_NAME,0);
				Editor editor=bundleData.edit();
				editor.putString("JSON_RESPONSE", response.toString());
				lastSyncInMillis=System.currentTimeMillis();
				editor.putLong(LAST_SYNC_TIME, lastSyncInMillis);
				editor.commit();

				JSONObject options=new JSONObject(response.getString("options"));

				if(options!=null && options.length()>0){
					totalConsumption=options.getLong("total_consumption");
					totalUsage=options.getLong("total_usage");
					parseUsage(options.getString("hourly_usage"));

					Log.v("ELSERVICES", "Response Options: "+options.getLong("total_usage")+" "+options.getString("hourly_usage"));	
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

		if(hourlyUsage!=null)
			drawChart(hourlyUsage);
	}

	private void updateApps(){
		setApps(activity_names,activity_values,activity_usage);
	}

	private void setViews(){
		DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);

		TextView tv=(TextView)inflateView.findViewById(R.id.segText2);
		tv.setVisibility(View.VISIBLE);
		tv=(TextView)inflateView.findViewById(R.id.totCon1);
		tv.setVisibility(View.VISIBLE);
		tv=(TextView)inflateView.findViewById(R.id.lastSyncText);
		tv.setVisibility(View.VISIBLE);
		tv=(TextView)inflateView.findViewById(R.id.distName2);
		tv.setVisibility(View.VISIBLE);

		tv=(TextView)inflateView.findViewById(R.id.sorryText2);
		tv.setVisibility(View.GONE);
		//		ImageView history=(ImageView)inflateView.findViewById(R.id.timeSelectBtn2);
		//		history.setVisibility(View.VISIBLE);
	}


	private void updateViews(long syncTime){
		long percent=(long) (((double)totalUsage/(double)totalConsumption)*100);
		TextView totalVal=(TextView)inflateView.findViewById(R.id.totalVal);
		if(totalUsage>0)
		{
			totalVal.setVisibility(View.VISIBLE);
			totalVal.setText(Long.toString(totalUsage)+" Wh"+" ("+(Long.toString(percent))+"%)");
		}
		else
			totalVal.setVisibility(View.INVISIBLE);

		TextView totalConVal=(TextView)inflateView.findViewById(R.id.totalConVal);
		if(totalConsumption>0)
		{
			totalConVal.setVisibility(View.VISIBLE);
			totalConVal.setText(Long.toString(totalConsumption)+" Wh"+" ("+(Long.toString(percent))+"%)");
		}
		else
			totalConVal.setVisibility(View.INVISIBLE);

		DateFormat df=new DateFormat();
		lastSyncTime=df.format("dd MMM yy HH:mm", syncTime).toString();
		TextView textView=(TextView)inflateView.findViewById(R.id.lastSyncText);
		textView.setText("Last synced on: "+lastSyncTime);

	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Bundle data = bundle.getBundle("Data");
				parseData(data);
				if(data.getString("api").equals("energy/personal/")){
					lastSyncInMillis=System.currentTimeMillis();
					setViews();
					updateChart();
					updateApps();
					updateViews(lastSyncInMillis);
				}
			}
		}
	};

}