package com.example.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class ReassignActivity extends FragmentActivity implements AppLocDialogFragment.LocationDialogListener,TimePickerDialogFragment.TimePickerDialogListener{

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
	ArrayList<Long> usage=new ArrayList<Long>();
	ArrayList<Long> ids=new ArrayList<Long>();
	ArrayList<long[]> terminals=new ArrayList<long[]>();

	long activity_ID;
	String activityLoc;

	String[] apps={"TV","Microwave"};
	int appCounter=0;
	int[] red={0,102,153,204,0,10,71,204,0,255,255,204,0,0,102,204};
	int[] green={0,0,0,0,102,10,71,0,204,255,255,102,204,204,204,204};
	int[] blue={204,204,153,102,204,255,255,0,204,71,10,0,102,0,0,0};

	String initLoc;

	String app="none";
	int color=Color.LTGRAY;
	String changeTimeOf;

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;
	String SENDER_ID = "166229175411";

	Button start,end;
	JSONArray activities;

	Calendar c=Calendar.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reassign);
		y=y1;

		start=(Button)findViewById(R.id.setStart);
		end=(Button) findViewById(R.id.setEnd);

		Intent intent=getIntent();
		Bundle extras=intent.getExtras();
		Random rand = new Random();
		appCounter=rand.nextInt(10)+1;

		app=extras.getString("appliance");
		color=extras.getInt("color");

		gcm = GoogleCloudMessaging.getInstance(this);
		setupMessage();
	}




	protected void onStart(){
		super.onStart();
		this.registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));

		if(time!=null && usage!=null)
			setupChart(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		this.unregisterReceiver(receiver);
	}

	public void setupChart( boolean isSlice){
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();


		// Creating an  XYSeries for Income
		TimeSeries mSeries = new TimeSeries("Reassign");
		Date date=new Date();

		XYSeriesRenderer renderer = new XYSeriesRenderer();

		for (int i = 0; i < time.size(); i++) {
			c.setTimeInMillis(time.get(i).longValue()*1000);
			mSeries.add(c.getTime(), usage.get(i));
			Log.v("ELSERVICES", "usage: "+usage.get(i)+" time: "+c.getTime().toString()
					+"\n timeinmillis: "+time.get(i)+" v/s: "+c.getTimeInMillis()+" now: "+System.currentTimeMillis());
		}

		renderer.setLineWidth(2);
		renderer.setColor(color);
		// Include low and max value
		renderer.setDisplayBoundingPoints(true);

		mRenderer.addSeriesRenderer(renderer);


		dataset.addSeries(mSeries);

		drawChart(mRenderer,dataset,isSlice);
	}

	public void drawChart(XYMultipleSeriesRenderer mRenderer,XYMultipleSeriesDataset dataset,boolean isSlice){
		Log.v("ELSERVICES", "chart drawn");

		//		Date date=new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

		if(isSlice){
			Log.v("ELSERVICES", "isSlice");
			c.setTimeInMillis(xOfStart);

			TimeSeries sliceSeries=new TimeSeries("Time SLice");
			sliceSeries.add(c.getTime(), maxY*1.5);
			Log.v("ELSERVICES", "time-slice start: "+c.getTime());
			c.setTimeInMillis(xOfEnd);
			sliceSeries.add(c.getTime(), maxY*1.5);
			Log.v("ELSERVICES", "time-slice end: "+c.getTime());

			XYSeriesRenderer sliceRenderer = new XYSeriesRenderer();
			sliceRenderer.setLineWidth(2);
			sliceRenderer.setColor(Color.argb(95, 0, 0, 0));
			// Include low and max value
			sliceRenderer.setDisplayBoundingPoints(true);
			FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ALL);
			fill.setColor(Color.argb(127, 0, 0, 0));
			sliceRenderer.addFillOutsideLine(fill);
			mRenderer.addSeriesRenderer(sliceRenderer);
			dataset.addSeries(sliceSeries);			
		}


		// We want to avoid black border
		mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
		mRenderer.setClickEnabled(true);
		mRenderer.setSelectableBuffer(20);
		mRenderer.setYAxisMax(maxY*1.5);
		mRenderer.setYAxisMin(0);
		//		mRenderer.setXAxisMax(Calendar.getInstance().getTimeInMillis());
		//		mRenderer.setXAxisMin(Calendar.getInstance().getTimeInMillis()-(24*60*60*1000));
		mRenderer.setPanEnabled(false);
		mRenderer.setZoomEnabled(false);
		mRenderer.setShowGrid(true); // we show the grid


		chartView = ChartFactory.getTimeChartView(this, dataset, mRenderer, "Reassign");

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
					setTimeSlice((long) seriesSelection.getXValue());
				}
			}
		});

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
					msg = "Reassign sent message";
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
			if(xOfStart>=terminalPoints[0] && xOfEnd<=terminalPoints[1]){
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
		setupChart(true);
		this.registerReceiver(receiver, new IntentFilter(GcmIntentService.RECEIVER));
	}



	public void setTimeSlice(long xCoord){

		TextView guide=(TextView) findViewById(R.id.textGuide);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

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
		textView.setText(app+" at "+loc);
		reset();
		parseActivities(loc);
		setupChart(false);
	}

	private void findYMax(){
		for(long i:usage){
			if(i>maxY)
				maxY=i;
		}
		Log.v("ELSERVICES", "MaxY: "+maxY);
	}

	private void parseActivities(String loc){
		Log.v("ELSERVICES", "parseactivities");
		activityLoc=loc;
		long[] terminalPoints=new long[2];
		ids=new ArrayList<Long>();
		terminals=new ArrayList<long[]>();
		time=new ArrayList<Long>();
		ArrayList<String> locs=new ArrayList<String>();

		for(int i=0;i<activities.length();i++){
			JSONObject activity;
			try {
				activity = activities.getJSONObject(i);
				if(locs.indexOf(activity.getString("location"))==-1){
					Log.v("ELSERVICES", "Location added: "+activity.getString("location"));
					locs.add(activity.getString("location"));
				}
				if(activity.getString("location").equals(loc)){
					ids.add(activity.getLong("id"));
					terminalPoints[0]=activity.getLong("start_time");
					terminalPoints[1]=activity.getLong("end_time");
					terminals.add(terminalPoints);

					//render 4 points for each activity
					time.add(terminalPoints[0]);usage.add((long) 0);
					time.add(terminalPoints[0]);usage.add(activity.getLong("usage"));
					time.add(terminalPoints[1]);usage.add(activity.getLong("usage"));
					time.add(terminalPoints[1]);usage.add((long) 0);
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
			updateViews();
			findYMax();
			setupChart(false);
		}
		else
			Toast.makeText(getApplicationContext(), "no activity in this location", 1000).show();
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

				if(options!=null){
					activities=options.getJSONArray("activities");

					if(activities!=null){
						Log.v("ELSERVICES","Reassign Activities: "+ activities.getString(0));
						TextView textView=(TextView) findViewById(R.id.appLocation);
						textView.setText(app+" at "+activities.getJSONObject(0).getString("location"));
						initLoc=activities.getJSONObject(0).getString("location");
						parseActivities(initLoc);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} 

	private void updateViews(){
		DateFormat df=new DateFormat();
		String lastSyncTime=df.format("dd/MM/yy HH:mm", System.currentTimeMillis()).toString();
		TextView textView=(TextView) findViewById(R.id.lastSyncReassign);
		textView.setText("Last synced on: "+lastSyncTime);
	}


	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Bundle data = bundle.getBundle("Data");
				parseData(data);
				SharedPreferences dataPrefs;
				Log.i("ELSERVICES","Reassign receiver " +data.getString("api"));
			}
		}
	};
}
