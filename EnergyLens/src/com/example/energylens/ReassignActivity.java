package com.example.energylens;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReassignActivity extends Activity implements TimePickerDialogFragment.TimePickerDialogListener{

	GraphicalView chartView;
	boolean firstPointSet=false;
	long xOfStart=0,xOfEnd=0;
	int lastSet=0;
	String oldApp="none";
	int[] x = { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
	int[] y = { 2000,3000,2800,3500,2500,2700,3000,2800,3500,3700,3800,2800,3500,3700,3800,2800,3500,3700,3800,2800,2000,2500,2700,3000};
	 String[] apps={"TV","Microwave"};
	 int appCounter=0;
	 int[] red={0,102,153,204,0,10,71,204,0,255,255,204,0,0,102,204};
	 int[] green={0,0,0,0,102,10,71,0,204,255,255,102,204,204,204,204};
	 int[] blue={204,204,153,102,204,255,255,0,204,71,10,0,102,0,0,0};
	
	 String app="none";
	 int color=Color.LTGRAY;
	 String changeTimeOf;
	 
	 Button start,end;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reassign);	
		
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
	
	public void setupChart(boolean isSlice){
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
		        Calendar c = Calendar.getInstance();
		        c.setTimeInMillis(diff);
				try {
					mSeries.add(dateFormat.parse(dateFormat.format(c.getTime())), y[i]/appCounter);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				Log.v("ELSERVICES","time: "+ dateFormat.format(c.getTime())+" value: "+y[i]/appCounter);
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
			Calendar c = Calendar.getInstance();
	        c.setTimeInMillis(xOfStart);
			
			TimeSeries sliceSeries=new TimeSeries("Time SLice");
			try {
				sliceSeries.add(dateFormat.parse(dateFormat.format(c.getTime())), 5000);
				c.setTimeInMillis(xOfEnd);
				sliceSeries.add(dateFormat.parse(dateFormat.format(c.getTime())), 5000);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
//			sliceSeries.add(xOfStart, 5000);
//			sliceSeries.add(xOfEnd, 5000);
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
  		mRenderer.setSelectableBuffer(10);
  		mRenderer.setYAxisMax(5000);
		mRenderer.setYAxisMin(0);
//		mRenderer.setXAxisMax(Calendar.getInstance().getTimeInMillis());
//		mRenderer.setXAxisMin(Calendar.getInstance().getTimeInMillis()-(24*60*60*1000));
		mRenderer.setPanEnabled(true);
  		mRenderer.setPanLimits(new double[] {0,24,0,5000});
  		mRenderer.setZoomButtonsVisible(true);
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
		
//		final EditText edittext = (EditText) findViewById(R.id.startTime);
//		edittext.setOnKeyListener(new OnKeyListener() {
//		    public boolean onKey(View v, int keyCode, KeyEvent event) {
//		        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
//		           xOfStart=Double.parseDouble(edittext.getText().toString());
//		           setupChart(true);
//		            return true;
//		        }
//		        return false;
//		        }
//		    });
//		
//		final EditText edittext2 = (EditText) findViewById(R.id.endTime);
//		edittext2.setOnKeyListener(new OnKeyListener() {
//		    public boolean onKey(View v, int keyCode, KeyEvent event) {
//		        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
//		           xOfEnd=Double.parseDouble(edittext2.getText().toString());
//		           setupChart(true);
//		            return true;
//		        }
//		        return false;
//		        }
//		    });
		
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
        Calendar c=Calendar.getInstance();
		
		if(lastSet==1){ //select start edge
			guide.setText("touch a point to select an end time"); //indicate next edge
			
				if(xCoord>xOfEnd){
					Toast.makeText(this, "illegal start edge", 1000).show();
					lastSet=1;
				}
				else{
					c.setTimeInMillis(xCoord);
					start.setText(dateFormat.format(c.getTime()));
					xOfStart=xCoord;
					lastSet=0;
				}				
		}
		else if(lastSet==0){ //select end edge
			guide.setText("touch a point to select a start time");	//indicate next edge
			
			if(xCoord<xOfStart){
				Toast.makeText(this, "illegal end edge", 1000).show();
				lastSet=0;
			}
			else{
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
		Calendar c = Calendar.getInstance(); 
		 SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
//		int mins = c.get(Calendar.MINUTE);
//		int hours=c.get(Calendar.HOUR_OF_DAY);
		
		Calendar.getInstance().getTime();
//		c.setTimeInMillis(xCoord);
		c.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_YEAR, hourOfDay, minute);
		long xCoord=c.getTimeInMillis();
				
		Log.v("ELSERVICES","set slice: " +dateFormat.format(c.getTime()));
		
		// TODO Auto-generated method stub
		//		Log.v("ELSERVICES", "time picker works "+hourOfDay+":"+minute);
		if(changeTimeOf=="start"){
			start.setText(dateFormat.format(c.getTime()));
			xOfStart=xCoord;
		}
		else if(changeTimeOf=="end"){
			end.setText(dateFormat.format(c.getTime()));
			xOfEnd=xCoord;
		}
		
		setupChart(true);
	}
	
}
