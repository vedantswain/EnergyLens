package com.example.energylens;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ReassignActivity extends Activity {

	GraphicalView chartView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reassign);
	}
	
	protected void onStart(){
		super.onStart();
		setupChart();
	}
	
	public void setupChart(){
		int[] x = { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
		int[] y = { 2000,3000,2800,3500,2500,2700,3000,2800,3500,3700,3800,2800,3500,3700,3800,2800,3500,3700,3800,2800,2000,2500,2700,3000};
		drawChart(x,y,"TV", Color.RED);
	}
		
	public void drawChart(int[] x, int[] y ,String app, int color){
		Log.v("ELSERVICES", "chart drawn");
		
        // Creating an  XYSeries for Income
        XYSeries mSeries = new XYSeries(app);
        
        for(int i=0;i<x.length;i++){
            mSeries.add(i, y[i]);
        }
        
        XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setLineWidth(2);
		renderer.setColor(color);
		// Include low and max value
		renderer.setDisplayBoundingPoints(true);
		renderer.setPointStyle(PointStyle.CIRCLE);
		renderer.setPointStrokeWidth(3);
		
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);
		
		
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
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(mSeries);
		
		chartView = ChartFactory.getLineChartView(this, dataset, mRenderer);
		
		LinearLayout chart_container=(LinearLayout)findViewById(R.id.chart);
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
	            Toast.makeText(
	                ReassignActivity.this,
	                "Chart element in series index " + seriesSelection.getSeriesIndex()
	                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
	                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
	                    + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
	          }
	        }
	      });
	}		
	
}
