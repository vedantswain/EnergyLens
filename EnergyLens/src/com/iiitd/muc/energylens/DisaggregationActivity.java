package com.iiitd.muc.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iiitd.muc.energylens.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class DisaggregationActivity extends FragmentActivity implements ApplianceDialogFragment.ApplianceDialogListener,AppLocDialogFragment.LocationDialogListener,TimePickerDialogFragment.TimePickerDialogListener{

	GraphicalView chartView;
	boolean firstPointSet=false;
	long xOfStart=0,xOfEnd=0;
	long maxY=0;
	int lastSet=1;
	boolean firstTime=true;
	String oldApp="none";
	String reassignTo="";
	int[] x = { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
	int[] y;
	int[] y1 = { 2000,3000,2800,3500,2500,2700,3000,2800,3500,3700,3800,2800,3500,3700,3800,2800,3500,3700,3800,2800,2000,2500,2700,3000};
	int[] y2 = { 1000,4000,2500,5500,3500,700,3500,2000,1500,1700,4800,2900,3000,4000,2800,2800,5500,4700,4800,800,1000,1500,4000,1000};

	ArrayList<Long> time=new ArrayList<Long>();
	ArrayList<Long> wastage_times=new ArrayList<Long>();
	ArrayList<Long> usage_times=new ArrayList<Long>();
	ArrayList<Long> value=new ArrayList<Long>();
	ArrayList<Long> wastage_value=new ArrayList<Long>();
	ArrayList<Long> usage_value=new ArrayList<Long>();
	ArrayList<Long> ids=new ArrayList<Long>();
	ArrayList<long[]> terminals=new ArrayList<long[]>();

	ArrayList<String> app_locs=new ArrayList<String>();
	ArrayList<String> apps_in_loc=new ArrayList<String>(); 
	ArrayList<String> locs=new ArrayList<String>();

	long activity_ID;
	String activityLoc;

	String[] apps={"TV","Microwave"};
	int appCounter=0;
	int[] red={0,102,153,204,0,10,71,204,0,255,255,204,0,0,102,204};
	int[] green={0,0,0,0,102,10,71,0,204,255,255,102,204,204,204,204};
	int[] blue={204,204,153,102,204,255,255,0,204,71,10,0,102,0,0,0};

	String initLoc;

	String app="none";
	int color=Color.argb(60,0, 0, 0);
	int wastageColor=Color.rgb(102, 0, 0);
	int usageColor=Color.rgb(0, 153, 153);
	String changeTimeOf;

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;
	String SENDER_ID = "166229175411";

	TextView start,end;
	JSONArray activities;
	JSONArray appliances;

	Calendar c=Calendar.getInstance();
	private boolean forCorrection=false;
	private String toLocation,toApp,request_for;
	private long lastSyncInMillis;

	long timeOfVisit,timeOfStay;
	String screenName="Disaggregated Activity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disaggregation);
		y=y1;

		start=(TextView)findViewById(R.id.setStart);
		end=(TextView) findViewById(R.id.setEnd);

		Intent intent=getIntent();
		Bundle extras=intent.getExtras();
		Random rand = new Random();
		appCounter=rand.nextInt(10)+1;

		app=extras.getString("appliance");
		//		color=extras.getInt("color");

		if(savedInstanceState!=null){
			Log.v("ELSERVICES", "Loading from savedInstance");
		}

		SharedPreferences sp=getSharedPreferences(app+"CORRECTION_PREFS",0);

		Log.v("ELSERVICES", "Shared prefs exist: "+sp.contains("LAST_SYNC"));

		if(sp.contains("LAST_SYNC")){
			Log.v("ELSERVICES", "Loading Dissag from saved data");
			lastSyncInMillis=sp.getLong("LAST_SYNC",System.currentTimeMillis());
			parsePref(sp.getString("JSON_RESPONSE", ""));
			updateViews(lastSyncInMillis);
		}

		Toast.makeText(getApplicationContext(), "Refresh if you can't see the graph", 1000);

		gcm = GoogleCloudMessaging.getInstance(this);
		setupMessage();
	}




	protected void onStart(){
		super.onStart();
		this.registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));

		if(time!=null && value!=null)
			setupChart(false);
	}


	@Override
	public void onPause() {
		super.onPause();
		timeOfStay=System.currentTimeMillis()-timeOfVisit;
		LogWriter.screenLogWrite(timeOfVisit+","+screenName+","+timeOfStay);
		this.unregisterReceiver(receiver);
	}

	public boolean acrossTwoDays(long timeStart,long timeStop){
		if(Integer.parseInt(DateFormat.format("dd", timeStop).toString())
				-Integer.parseInt(DateFormat.format("dd", timeStart).toString())
				>0){
			return true;
		}
		return false;
	}

	public void setupChart( boolean isSlice){
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

		//consumption series
		TimeSeries mSeries = new TimeSeries("Appliance usage duration");
		Date date=new Date();

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		
		Log.v("ELSERVICE","Disagg: "+Long.toString(time.size())+" "+Long.toString(value.size()));

		for (int i = 0; i < time.size(); i++) {
			c.setTimeInMillis(time.get(i).longValue()*1000);
			mSeries.add(c.getTime(), value.get(i));
			Log.v("ELSERVICES", "value: "+value.get(i)+" time: "+c.getTime().toString());
		}

		renderer.setLineWidth(3);
		renderer.setColor(color);
		// Include low and max value
		renderer.setDisplayBoundingPoints(true);

//		FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ALL);
//
//		fill.setColor(Color.argb(125, 0, 130, 130));
//		renderer.addFillOutsideLine(fill);

		//wastage series
		TimeSeries wastageSeries = new TimeSeries("Your wastage period");

		XYSeriesRenderer wastageRenderer = new XYSeriesRenderer();

		wastageRenderer.setLineWidth(2);
		wastageRenderer.setColor(wastageColor);
		// Include low and max value
		wastageRenderer.setDisplayBoundingPoints(true);

		for (int j = 0; j < wastage_times.size(); j++) {
			c.setTimeInMillis(wastage_times.get(j).longValue()*1000);
			if(j<wastage_value.size()){
				wastageSeries.add(c.getTime(), wastage_value.get(j));
				Log.v("ELSERVICES", "wastage value: "+wastage_value.get(j)+"wastage time: "+c.getTime().toString());
			}
		}

		FillOutsideLine fillWaste = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ALL);


		fillWaste.setColor(wastageColor);
		wastageRenderer.addFillOutsideLine(fillWaste);

		//usage series
		TimeSeries usageSeries = new TimeSeries("Your usage period");

		XYSeriesRenderer usageRenderer = new XYSeriesRenderer();

		usageRenderer.setLineWidth(2);
		usageRenderer.setColor(usageColor);
		// Include low and max value
		usageRenderer.setDisplayBoundingPoints(true);

		for (int j = 0; j < usage_times.size(); j++) {
			c.setTimeInMillis(usage_times.get(j).longValue()*1000);
			if(j<usage_value.size()){
				usageSeries.add(c.getTime(), usage_value.get(j));
				Log.v("ELSERVICES", "usage value: "+usage_value.get(j)+"usage time: "+c.getTime().toString());
			}
		}

		FillOutsideLine fillShared = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ALL);


		fillShared.setColor(usageColor);
		usageRenderer.addFillOutsideLine(fillShared);
		
		if(time.size()>0)
			if(acrossTwoDays(time.get(0)*1000,time.get(time.size()-1)*1000)){
				SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy");
				Date division;
				try {
					division = sdf.parse(DateFormat.format("dd/MM/yy", time.get(time.size()-1)*1000).toString());
					Log.v("ELSERVICES","Division date: "+ DateFormat.format("dd/MM/yy",division.getTime()).toString());
					mSeries.addAnnotation(DateFormat.format("dd/MM",division.getTime()).toString(),division.getTime(), 10);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		//		mRenderer.setXLabels(0);

		//		for(int i=0;i<time.size();i++){
		//			long graphTime=time.get(i).longValue()*1000;
		//			String text=DateFormat.format("dd/MM HH:mm",graphTime).toString();
		//			mRenderer.addXTextLabel(new Date(time.get(i).longValue()*1000).getTime(), text);
		//		}

		//		for(int i=0;i<wastage_times.size();i++){
		//			long graphTime=wastage_times.get(i).longValue()*1000;
		//			String text=DateFormat.format("dd/MM HH:mm",graphTime).toString();
		//			mRenderer.addXTextLabel(new Date(wastage_times.get(i).longValue()*1000).getTime(), text);
		//		}

		mRenderer.setXLabelsAlign(Align.RIGHT);
		mRenderer.setXLabelsAngle(-45);

		mRenderer.addSeriesRenderer(renderer);
		mRenderer.addSeriesRenderer(wastageRenderer);
		mRenderer.addSeriesRenderer(usageRenderer);
		
		dataset.addSeries(mSeries);
		dataset.addSeries(wastageSeries);
		dataset.addSeries(usageSeries);
		
		drawChart(mRenderer,dataset,isSlice);
	}

	public void drawChart(XYMultipleSeriesRenderer mRenderer,XYMultipleSeriesDataset dataset,boolean isSlice){
		Log.v("ELSERVICES", "chart drawn");

		//		Date date=new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

		//		if(isSlice){
		//			Log.v("ELSERVICES", "isSlice");
		//			c.setTimeInMillis(xOfStart);
		//
		//			TimeSeries sliceSeries=new TimeSeries("Time SLice");
		//			sliceSeries.add(c.getTime(), maxY*1.5);
		//			Log.v("ELSERVICES", "time-slice start: "+c.getTime());
		//			c.setTimeInMillis(xOfEnd);
		//			sliceSeries.add(c.getTime(), maxY*1.5);
		//			Log.v("ELSERVICES", "time-slice end: "+c.getTime());
		//
		//			XYSeriesRenderer sliceRenderer = new XYSeriesRenderer();
		//			sliceRenderer.setLineWidth(2);
		//			sliceRenderer.setColor(Color.argb(95, 0, 0, 0));
		//			// Include low and max value
		//			sliceRenderer.setDisplayBoundingPoints(true);
		//			FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ALL);
		//			fill.setColor(Color.argb(127, 0, 0, 0));
		//			sliceRenderer.addFillOutsideLine(fill);
		//			mRenderer.addSeriesRenderer(sliceRenderer);
		//			dataset.addSeries(sliceSeries);			
		//		}

		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);


		// We want to avoid black border
		mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
		mRenderer.setClickEnabled(true);
		mRenderer.setSelectableBuffer(20);

		mRenderer.setMarginsColor(Color.argb(0xff, 0xf0, 0xf0, 0xf0)); 
		mRenderer.setPanEnabled(true);		
		if(time.size()>0){
			mRenderer.setPanLimits(new double[] {time.get(0)*1000,time.get(time.size()-1)*1000,0,maxY*1.5});
			mRenderer.setXAxisMax((time.get(time.size()-1)*1000)+60000);
			mRenderer.setXAxisMin((time.get(0)*1000)-60000);
		}
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setZoomEnabled(true);

		mRenderer.setYAxisMax(maxY*1.5);
		mRenderer.setYAxisMin(0);
		mRenderer.setChartTitleTextSize(val);
		mRenderer.setLabelsColor(Color.DKGRAY);
		mRenderer.setYLabelsColor(0, Color.DKGRAY);
		mRenderer.setXLabelsColor(Color.DKGRAY);
		mRenderer.setLabelsTextSize(val);
		mRenderer.setLegendTextSize(val);
		mRenderer.setYLabelsAlign(Align.RIGHT);
		mRenderer.setXTitle("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n Time");
		mRenderer.setYTitle("Power (Watts)");
		mRenderer.setAxisTitleTextSize(val);
		mRenderer.setShowGrid(true); // we show the grid
		int[] margins={20,80,120,10};
		mRenderer.setMargins(margins);


		chartView = ChartFactory.getTimeChartView(this, dataset, mRenderer, "Reassign");
		//chartView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		LinearLayout chart_container=(LinearLayout)findViewById(R.id.chartComparison);
		chart_container.addView(chartView,0);

		chartView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.v("ELSERVICES", "Graph clicked");
				// handle the click event on the chart
				SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
				if (seriesSelection == null) {
					//	            Toast.makeText(getActivity(), "No chart element", Toast.LENGTH_SHORT).show();
				} else {
					// display information of the clicked point
					//					setTimeSlice((long) seriesSelection.getXValue());
				}
			}
		});

		//		Log.v("ELSERVICES", "Start slice: "+xOfStart+" Stop slice: "+xOfEnd);
	}

	public void setupMessage(){
		Bundle data = new Bundle();
		data.putString("msg_type", "request");
		data.putString("api","energy/disaggregated/");

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
			options.put("activity_name", app);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		data.putString("options", options.toString());

		sendMessage(data);
	}

	public void sendMessage(final Bundle data){
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					SecureRandom random = new SecureRandom();
					String randomId=new BigInteger(130, random).toString(32);

					gcm.send(SENDER_ID + "@gcm.googleapis.com", randomId, data);
					msg = "Correction sent message";
					Log.i("ELSERVICES", "message sent to disaggregated: "+data.toString());

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

	public void launchLocDialog(View view){
		DialogFragment newFragment = new AppLocDialogFragment();
		newFragment.show(getSupportFragmentManager(), "Locations");	    
	}

	public void correctLoc(View view){
		forCorrection=true;
		String[] currLocs=new String[app_locs.size()];
		Common.changeActivityLocs(app_locs.toArray(currLocs));
		launchLocDialog(view);
	}

	public void launchAppDialog(View view){
		DialogFragment newFragment = new ApplianceDialogFragment();
		newFragment.show(getSupportFragmentManager(), "Appliances");
	}

	public void correctApp(View view){
		String[] currLocs=new String[apps_in_loc.size()];
		Common.changeActivityApps(apps_in_loc.toArray(currLocs));
		launchAppDialog(view);
	}

	public void resetSlice(View view){
		reset();
	}

	public void reset(){
		start.setText("--:--");
		end.setText("--:--");
		xOfStart=0;
		xOfEnd=0;
		firstTime=true;
		lastSet=1;
		c=Calendar.getInstance();
		Log.v("ELSERVICES","set slice: " +c.getTime()+" get reset "+changeTimeOf);
		TextView guide=(TextView) findViewById(R.id.textGuide);
		guide.setText("touch a point to select a start time");	//indicate next edge
		setupChart(false);
	}

	public void sendReassign(View view){
		findId();
		Log.v("ELSERVICES", "id: "+activity_ID+" location: "+activityLoc+" start_time: "+xOfStart+" end_time: "+xOfEnd);
		setupMessage();
	}

	private void findId(){
		int i=0;
		for(long[] terminalPoints:terminals){
			Log.v("ELSERVICES", "Terminal points: "+terminalPoints[0]+", "+terminalPoints[1]);
			if(xOfStart>=terminalPoints[0]*1000 && xOfEnd<=terminalPoints[1]*1000){
				activity_ID=ids.get(i);
			}
			i++;
		}
	}

	public void onRadioButtonClicked(View view) {
		// Is the button now checked?
		boolean checked = ((RadioButton) view).isChecked();

		// Check which radio button was clicked
		switch(view.getId()) {
		case R.id.radio_uname1:
			if (checked)
				reassignTo=(String) ((RadioButton) view).getText();
			break;
		case R.id.radio_uname2:
			if (checked)
				reassignTo=(String) ((RadioButton) view).getText();	                
			break;
		}
	}

	public void selectStart(View view){
		TimePickerDialogFragment newFragment = new TimePickerDialogFragment();
		newFragment.show(getFragmentManager(), "Appliances");
		changeTimeOf="start";
	}

	public void selectEnd(View view){
		TimePickerDialogFragment newFragment = new TimePickerDialogFragment();
		newFragment.show(getFragmentManager(), "Time Picker");
		changeTimeOf="end";
	}

	public void onResume(){
		super.onResume();
		timeOfVisit=System.currentTimeMillis();
		setupChart(true);
		this.registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));
	}



	public void setTimeSlice(long xCoord){

		TextView guide=(TextView) findViewById(R.id.textGuide);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");

		if(lastSet==1){ //select start edge
			Log.v("ELSERVICES", "Set in start slice");
			if(firstTime==true){
				firstTime=false;
				xOfStart=xCoord;
				guide.setText("touch a point to select an end time"); //indicate next edge
				c.setTimeInMillis(xCoord);
				start.setText(dateFormat.format(c.getTime()));
				xOfStart=xCoord;
				lastSet=0;
				xOfEnd=xOfStart+1000;
			}
			else if(xCoord>xOfEnd){
				Toast.makeText(this, "illegal operation: start time is later than end time", 1500).show();
				lastSet=1;
				Log.v("ELSERVICES", "xCoord: "+xCoord+" xOfEnd: "+xOfEnd);
			}
			else{
				guide.setText("touch a point to select an end time"); //indicate next edge
				c.setTimeInMillis(xCoord);
				start.setText(dateFormat.format(c.getTime()));
				xOfStart=xCoord;

				lastSet=0;
			}				
		}
		else if(lastSet==0){ //select end edge
			Log.v("ELSERVICES", "Set in end slice");
			if(xCoord<xOfStart){
				Toast.makeText(this, "illegal operation: end time is earlier than start time", 1500).show();
				lastSet=0;
			}
			else{
				guide.setText("touch a point to select a start time");	//indicate next edge
				c.setTimeInMillis(xCoord);
				end.setText(dateFormat.format(c.getTime()));
				xOfEnd=xCoord;
				lastSet=1;
			}
		}
		setupChart(true);
	}




	@Override
	public void onSetTime(int hourOfDay, int minute) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

		if(firstTime==true){
			c=Calendar.getInstance();
		}

		Log.v("ELSERVICES","set slice: " +c.getTime()+" before set");

		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);

		long xCoord=c.getTimeInMillis();
		Log.v("ELSERVICES","set slice: " +c.getTime()+" "+changeTimeOf);

		// TODO Auto-generated method stub
		//		Log.v("ELSERVICES", "time picker works "+hourOfDay+":"+minute);	
		if(changeTimeOf=="start"){
			Log.v("ELSERVICES", "Set in end");
			if(firstTime==true){
				firstTime=false;
				start.setText(dateFormat.format(c.getTime()));
				xOfStart=xCoord;
				xOfEnd=xOfStart+1000;
			}
			else if(c.getTimeInMillis()>xOfEnd)
				Toast.makeText(this, "illegal operation: start time is later than end time", 1500).show();	
			else{
				start.setText(dateFormat.format(c.getTime()));
				xOfStart=xCoord;
			}
		}
		else if(changeTimeOf=="end"){
			Log.v("ELSERVICES", "Set in end");
			if(firstTime==true){
				firstTime=false;
				end.setText(dateFormat.format(c.getTime()));
				xOfEnd=xCoord;
				xOfStart=xOfEnd-1000;
			}
			else if(c.getTimeInMillis()<xOfStart)
				Toast.makeText(this, "illegal operation: end time is earlier than start time", 1500).show();	
			else{
				end.setText(dateFormat.format(c.getTime()));
				xOfEnd=xCoord;
			}
		}

		setupChart(true);
	}




	@Override
	public void onLocSelected(String loc,int index) {
		// TODO Auto-generated method stub
		TextView textView=(TextView) findViewById(R.id.appLocation);
		textView.setText(app+" in "+loc);
		Log.v("ELSERVICES", "New Loc: "+loc);
		//		if(forCorrection){
		//			parseApp(loc,index);
		//			forCorrection=false;
		//			TextView textView1=(TextView) findViewById(R.id.correctLoc);
		//			textView1.setText(loc);
		//			String[] currLocs=new String[locs.size()];
		//			Common.changeActivityLocs(locs.toArray(currLocs));
		//			toLocation=loc;
		//		}
		//		else{
		reset();
		if(activities!=null)
			parseActivities(loc);
		setupChart(false);
		//		}
	}

	public void toCorrect(View view){
		findId();
		Log.v("ELSERVICES", "id: "+activity_ID+" location: "+toLocation+" appliance: "+toApp+" start_time: "+xOfStart+" end_time: "+xOfEnd);

		Bundle data = new Bundle();
		data.putString("msg_type", "request");
		data.putString("api","inference/reassign/");

		JSONObject options=new JSONObject();

		try {
			options.put("start_time", xOfStart);
			options.put("end_time", xOfEnd);
			options.put("activity_id", activity_ID);
			options.put("to_appliance", toApp);
			options.put("to_location", toLocation);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		data.putString("options", options.toString());

		sendMessage(data);
	}

	private void findYMax(){
		for(long i:value){
			if(i>maxY)
				maxY=i;
		}
		Log.v("ELSERVICES", "MaxY: "+maxY);
	}

	private void parseActivities(String loc){
		Log.v("ELSERVICES", "parseactivities");
		activityLoc=loc;
		ids=new ArrayList<Long>();
		terminals=new ArrayList<long[]>();
		time=new ArrayList<Long>();
		time.clear();
		locs=new ArrayList<String>();
		value.clear();
		wastage_times.clear();
		wastage_value.clear();
		usage_times.clear();
		usage_value.clear();


		int k=0;
		for(int i=0;i<activities.length();i++){
			JSONObject activity;
			try {
				activity = activities.getJSONObject(i);
				if(locs.indexOf(activity.getString("location"))==-1){
					Log.v("ELSERVICES", "Location added: "+activity.getString("location"));
					locs.add(activity.getString("location"));
				}
				if(activity.getString("location").equals(loc)){

					long[] terminalPoints=new long[2];
					ids.add(activity.getLong("id"));
					terminalPoints[0]=activity.getLong("start_time");
					terminalPoints[1]=activity.getLong("end_time");
					Log.v("ELSERVICES", "Terminal points: "+terminalPoints[0]+", "+terminalPoints[1]);
					Log.v("ELSERVICES", "Actual Terminal points: "+activity.getLong("start_time")+", "+activity.getLong("end_time")+" id: "+activity.getLong("id"));
					terminals.add(terminalPoints);

					//render points for each activity
					time.add(terminalPoints[0]);value.add((long) 0);
					time.add(terminalPoints[0]);value.add(activity.getLong("value"));

					time.add(terminalPoints[1]);value.add(activity.getLong("value"));
					time.add(terminalPoints[1]);value.add((long) 0);

					Log.v("ELSERVICE","add Disagg: "+Long.toString(time.size())+" "+Long.toString(value.size()));

					//render wastage points
					JSONArray wastageTimeArray=activity.getJSONArray("wastage_times");
					for(int j=0;j<wastageTimeArray.length();j++){
						JSONObject wasteTime=wastageTimeArray.getJSONObject(j);
						wastage_times.add(wasteTime.getLong("start_time"));wastage_value.add((long)0);
						wastage_times.add(wasteTime.getLong("start_time"));wastage_value.add(activity.getLong("value"));

						Log.v("ELSERVICES", "Wastage points: "+wasteTime.getLong("start_time")
								+", "+wasteTime.getLong("end_time"));

						wastage_times.add(wasteTime.getLong("end_time"));wastage_value.add(activity.getLong("value"));
						wastage_times.add(wasteTime.getLong("end_time"));wastage_value.add((long)0);						
					}
					
					//render shared points
					JSONArray usageTimeArray=activity.getJSONArray("usage_times");
					for(int j=0;j<usageTimeArray.length();j++){
						JSONObject usageTime=usageTimeArray.getJSONObject(j);
						usage_times.add(usageTime.getLong("start_time"));usage_value.add((long)0);
						usage_times.add(usageTime.getLong("start_time"));usage_value.add(activity.getLong("value"));

						Log.v("ELSERVICES", "shared points: "+usageTime.getLong("start_time")
								+", "+usageTime.getLong("end_time"));

						usage_times.add(usageTime.getLong("end_time"));usage_value.add(activity.getLong("value"));
						usage_times.add(usageTime.getLong("end_time"));usage_value.add((long)0);						
					}

				}
				String[] currLocs=new String[locs.size()];
				Common.changeActivityLocs(locs.toArray(currLocs));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(String loc_name: Common.ACTIVITY_LOCS)
			Log.v("ELSERVICES","locations: "+loc_name);
		Log.v("ELSERVICES", "parseactivities complete");
		if(ids!=null){
			updateViews(System.currentTimeMillis());
			findYMax();
			setupChart(false);
		}
		else
			Toast.makeText(getApplicationContext(), "no activity in this location", 1000).show();
	}

	private void parseAppLoc(){
		for(int i=0;i<appliances.length();i++){
			JSONObject appliance;
			try {
				appliance=appliances.getJSONObject(i);
				if(!app_locs.contains(appliance.getString("location"))){
					app_locs.add(appliance.getString("location"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void parseApp(String loc, int index){
		apps_in_loc=new ArrayList<String>();
		for(int i=0;i<appliances.length();i++){
			JSONObject appliance;
			try {
				appliance=appliances.getJSONObject(i);
				if(loc.equals(appliance.getString("location"))){
					apps_in_loc.add(appliance.getString("appliance"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private void parseData(Bundle data){
		Log.v("ELSERVICES", "parsedata");

		if(data.getString("api").equals("energy/disaggregated/")){
			try {
				Set<String> keys=data.keySet();
				JSONObject response=new JSONObject();
				if(response!=null)
					Log.v("ELSERVICES", "Reassign not null");

				for(String key:keys){
					response.put(key, data.get(key));
				}

				JSONObject options=new JSONObject(response.getString("options"));

				SharedPreferences bundleData=getSharedPreferences(app+"CORRECTION_PREFS",0);
				Editor editor=bundleData.edit();
				editor.putString("JSON_RESPONSE", response.toString());
				editor.putLong("LAST_SYNC", lastSyncInMillis);
				editor.commit();

				if(options!=null){
					activities=options.getJSONArray("activities");

					if(activities!=null){
						Log.v("ELSERVICES","Reassign Activities: "+ activities.getString(0));
						TextView textView=(TextView) findViewById(R.id.appLocation);
						textView.setText(app+" at "+activities.getJSONObject(0).getString("location"));
						initLoc=activities.getJSONObject(0).getString("location");
						if(activities!=null) 
							parseActivities(initLoc);
					}

//					appliances=options.getJSONArray("appliances");

					if(appliances!=null){
						parseAppLoc();
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(data.getString("api").equals("inference/reassign/")){
			try {
				Set<String> keys=data.keySet();
				JSONObject response=new JSONObject();
				if(response!=null)
					Log.v("ELSERVICES", "Reassign not null");

				for(String key:keys){
					response.put(key, data.get(key));
				}

				JSONObject options=new JSONObject(response.getString("options"));

				if(options!=null){

					Log.v("ELSERVICES", options.getString("status"));

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
	}

	public void parsePref(String resp){
		try {
			JSONObject response=new JSONObject(resp);
			JSONObject options=new JSONObject(response.getString("options"));

			if(options!=null){
				activities=options.getJSONArray("activities");

				if(activities!=null){
					Log.v("ELSERVICES","Reassign Activities: "+ activities.getString(0));
					TextView textView=(TextView) findViewById(R.id.appLocation);
					textView.setText(app+" at "+activities.getJSONObject(0).getString("location"));
					initLoc=activities.getJSONObject(0).getString("location");
					if(activities!=null) 
						parseActivities(initLoc);
				}

				//appliances=options.getJSONArray("appliances");

				if(appliances!=null){
					parseAppLoc();
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateViews(long time){
		DateFormat df=new DateFormat();
		String lastSyncTime=df.format("dd MMM yy HH:mm", time).toString();
		TextView textView=(TextView) findViewById(R.id.lastSyncCorrect);
		textView.setText("Last synced on: "+lastSyncTime);
		lastSyncInMillis=time;
	}


	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Bundle data = bundle.getBundle("Data");
				parseData(data);
				Log.i("ELSERVICES","Correction receiver " +data.getString("api"));
			}
		}
	};

	@Override
	public void onAppSelected(String label) {
		// TODO Auto-generated method stub
		TextView textView=(TextView) findViewById(R.id.correctApp);
		textView.setText(Common.LABEL);
		toApp=label;
	}

}