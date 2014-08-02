package com.example.energylens;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

public class ReassignActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reassign);
	}
	
	public void setupChart(){
		int[] x = { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23};
		int[] y = { 2000,3000,2800,3500,2500,2700,3000,2800,3500,3700,3800,2800,3500,3700,3800,2800,3500,3700,3800,2800,2000,2500,2700,3000};
		drawChart(x,y, null, 0);
	}
		
	public void drawChart(int[] x, int[] y ,String app, int color){
		Log.v("ELSERVICES", "chart drawn");
		
        // Creating an  XYSeries for Income
        XYSeries mSeries = new XYSeries(app);
        
        for(int i=0;i<x.length;i++){
            mSeries.add(i, y[i]);
        }
        
        XYSeriesRenderer renderer = new XYSeriesRenderer();
  	  renderer.setLineWidth(1);
  	  renderer.setColor(color);
  	  renderer.setDisplayBoundingPoints(true);
  	  renderer.setPointStyle(PointStyle.CIRCLE);
  	  renderer.setPointStrokeWidth(2);
  	  renderer.setDisplayChartValues(true);
  	  
//  	  XYSeriesRenderer.FillOutsideLine fill=new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BELOW);
//  	  fill.setColor(Color.GRAY);
//  	  renderer.addFillOutsideLine(fill);
  	  
  	  XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
  	  mRenderer.addSeriesRenderer(renderer);
  	  
  	  mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); 
  	  
  		mRenderer.setPanEnabled(true);
  		mRenderer.setPanLimits(new double[] {0,24,0,5000});
  		mRenderer.setZoomButtonsVisible(true);
  		mRenderer.setYAxisMax(5000);
  		mRenderer.setYAxisMin(0);
  		mRenderer.setXAxisMin(0);
  		mRenderer.setChartTitle("Absolute Power Consumption");
  		mRenderer.setChartTitleTextSize(18);
  		mRenderer.setLabelsColor(Color.BLACK);
  		mRenderer.setBarSpacing(1);
  		mRenderer.setShowGrid(true);
  		
  		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
  		dataset.addSeries(mSeries);
  		
  		GraphicalView chartView = ChartFactory.getBarChartView(this, dataset, mRenderer, BarChart.Type.DEFAULT);
  		
  		LinearLayout chart_container=(LinearLayout)findViewById(R.id.chart);
  		chart_container.addView(chartView,0);
	}		
	
}
