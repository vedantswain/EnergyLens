package com.example.energylens;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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
	int lastSet=1;
	boolean firstTime=true;
	String oldApp="none";
	String reassignTo="";
	int[] x = { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
	int[] y;
	int[] y1 = { 2000,3000,2800,3500,2500,2700,3000,2800,3500,3700,3800,2800,3500,3700,3800,2800,3500,3700,3800,2800,2000,2500,2700,3000};
	int[] y2 = { 1000,4000,2500,5500,3500,700,3500,2000,1500,1700,4800,2900,3000,4000,2800,2800,5500,4700,4800,800,1000,1500,4000,1000};

	String[] apps={"TV","Microwave"};
	int appCounter=0;
	int[] red={0,102,153,204,0,10,71,204,0,255,255,204,0,0,102,204};
	int[] green={0,0,0,0,102,10,71,0,204,255,255,102,204,204,204,204};
	int[] blue={204,204,153,102,204,255,255,0,204,71,10,0,102,0,0,0};

	String app="none";
	int color=Color.LTGRAY;
	String changeTimeOf;

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;
	String SENDER_ID = "166229175411";

	Button start,end;

	Calendar c=Calendar.getInstance();
	private boolean firstPick=true;


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
	}




	protected void onStart(){
		super.onStart();
		setupChart(false);
	}

	public void setupChart( boolean isSlice){
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();


		// Creating an  XYSeries for Income
		TimeSeries mSeries = new TimeSeries("Reassign");

		XYSeriesRenderer renderer = new XYSeriesRenderer();
		//			XYSeries mSeries = new XYSeries(app);

		//			Date date=new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

		for (int i = 0; i < y.length; i++) {
			long diff=Calendar.getInstance().getTimeInMillis()-(i*60*60*1000);
			c.setTimeInMillis(diff);
			//					mSeries.add(dateFormat.parse(dateFormat.format(c.getTime())), y[i]/appCounter);
			mSeries.add(c.getTime(), y[i]/appCounter);
		}
		//			
		//	        for(int i=0;i<x.length;i++){
		//	            mSeries.add(i, y[i]/appCounter);
		//	        }


		renderer.setLineWidth(2);
		renderer.setColor(color);
		// Include low and max value
		renderer.setDisplayBoundingPoints(true);
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setPointStrokeWidth(3);

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
			sliceSeries.add(c.getTime(), 5000);
			Log.v("ELSERVICES", "time-slice start: "+c.getTime());
			c.setTimeInMillis(xOfEnd);
			sliceSeries.add(c.getTime(), 5000);
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
		mRenderer.setYAxisMax(5000);
		mRenderer.setYAxisMin(0);
		mRenderer.setXAxisMax(Calendar.getInstance().getTimeInMillis());
		mRenderer.setXAxisMin(Calendar.getInstance().getTimeInMillis()-(24*60*60*1000));
		mRenderer.setPanEnabled(false);
		mRenderer.setZoomEnabled(false);
		mRenderer.setShowGrid(true); // we show the grid


		chartView = ChartFactory.getTimeChartView(this, dataset, mRenderer, "Reassign");

		LinearLayout chart_container=(LinearLayout)findViewById(R.id.chartComparison);
		chart_container.addView(chartView,0);

		//		mRenderer.removeAllRenderers();
		//		dataset=new XYMultipleSeriesDataset();
		//		
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

	public void sendMessage(){
		new AsyncTask<Void,String,String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					Bundle data = new Bundle();
					data.putString("my_message", "Hello World");
					data.putString("my_action",
							"com.google.android.gcm.demo.app.ECHO_NOW");
					SecureRandom random = new SecureRandom();
					String randomId=new BigInteger(130, random).toString(32);

					String id = Long.toString(System.currentTimeMillis());
					gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
					msg = "Sent message";
					Log.i("ELSERVICES", "message sent");
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
		sendMessage();
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
		newFragment.show(getFragmentManager(), "Appliances");
		changeTimeOf="end";
	}

	public void onResume(){
		super.onResume();
		setupChart(true);
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
		TextView guide=(TextView) findViewById(R.id.textGuide);
		
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
		if(index%2==0){
			y=y1;
			setupChart(false);
		}
		else{
			y=y2;
			setupChart(false);
		}
	}

}
