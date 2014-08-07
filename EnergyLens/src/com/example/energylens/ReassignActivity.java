package com.example.energylens;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReassignActivity extends Activity {

	GraphicalView chartView;
	boolean firstPointSet=false;
	double xOfStart=0,xOfEnd=0;
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
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reassign);	
		
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
			
			XYSeriesRenderer renderer = new XYSeriesRenderer();
			XYSeries mSeries = new XYSeries(app);
	        
	        for(int i=0;i<x.length;i++){
	            mSeries.add(i, y[i]/appCounter);
	        }
	        
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
		
        // Creating an  XYSeries for Income
      
		
		if(isSlice){
			XYSeries sliceSeries=new XYSeries("Time SLice");
			sliceSeries.add(xOfStart, 5000);
			sliceSeries.add(xOfEnd, 5000);
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
  		mRenderer.setSelectableBuffer(30);
  		mRenderer.setYAxisMax(5000);
		mRenderer.setYAxisMin(0);
		mRenderer.setPanEnabled(true);
  		mRenderer.setPanLimits(new double[] {0,24,0,5000});
  		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setShowGrid(true); // we show the grid
			
		
		chartView = ChartFactory.getLineChartView(this, dataset, mRenderer);
		
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
	        	  setTimeSlice(seriesSelection.getXValue());
	          }
	        }
	      });
		
		final EditText edittext = (EditText) findViewById(R.id.startTime);
		edittext.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
		           xOfStart=Double.parseDouble(edittext.getText().toString());
		           setupChart(true);
		            return true;
		        }
		        return false;
		        }
		    });
		
		final EditText edittext2 = (EditText) findViewById(R.id.endTime);
		edittext2.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
		           xOfEnd=Double.parseDouble(edittext2.getText().toString());
		           setupChart(true);
		            return true;
		        }
		        return false;
		        }
		    });
		
	}
	
	public void onResume(){
		super.onResume();
		setupChart(true);
	}
	
	
	
	public void setTimeSlice(double xCoord){
		EditText start=(EditText) findViewById(R.id.startTime);
		EditText end=(EditText) findViewById(R.id.endTime);
		TextView guide=(TextView) findViewById(R.id.textGuide);		
		
		if(lastSet==1){ //select start edge
			guide.setText("touch a point to select an end edge"); //indicate next edge
			
				if(xCoord>xOfEnd){
					Toast.makeText(this, "illegal start edge", 1000).show();
					lastSet=1;
				}
				else{
					start.setText(Double.toString(xCoord));
					xOfStart=xCoord;
					lastSet=0;
				}
				
				
		}
		else if(lastSet==0){ //select end edge
			guide.setText("touch a point to select a start edge");	//indicate next edge
			
			if(xCoord<xOfStart){
				Toast.makeText(this, "illegal end edge", 1000).show();
				lastSet=0;
			}
			else{
				end.setText(Double.toString(xCoord));
				xOfEnd=xCoord;
				lastSet=1;
			}
		}
		setupChart(true);
	}
	
}
