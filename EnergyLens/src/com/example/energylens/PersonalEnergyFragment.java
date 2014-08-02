package com.example.energylens;

import java.util.Calendar;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class PersonalEnergyFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {	     
        View rootView = inflater.inflate(R.layout.fragment_personalenergy, container, false);
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
		int[] y = { 2000,3000,2800,3500,2500,2700,3000,2800,3500,3700,3800,2800,3500,3700,3800,2800,3500,3700,3800,2800,2000,2500,2700,3000};
 		drawChart(x,y);
	}

	
	public void drawChart(int[] x, int[] y){
		Log.v("ELSERVICES", "chart drawn");
		
        // Creating an  XYSeries for Income
        XYSeries mSeries = new XYSeries("Power");
        
        for(int i=0;i<x.length;i++){
            mSeries.add(x[i], y[i]);
        }
        
        XYSeriesRenderer renderer = new XYSeriesRenderer();
  	  renderer.setLineWidth(1);
  	  renderer.setColor(Color.DKGRAY);
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
  		mRenderer.setChartTitle("Your Energy Consumption for the Last 24 hours");
  		mRenderer.setChartTitleTextSize(18);
  		mRenderer.setLabelsColor(Color.BLACK);
  		mRenderer.setLabelsTextSize(15);
  		mRenderer.setXTitle("Hours");
  		mRenderer.setYTitle("Energy");
  		mRenderer.setYLabelsAlign(Align.RIGHT);
  		mRenderer.setBarSpacing(1);
  		mRenderer.setShowGrid(true);
  		
  		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
  		dataset.addSeries(mSeries);
  		
  		GraphicalView chartView = ChartFactory.getBarChartView(getActivity(), dataset, mRenderer, BarChart.Type.DEFAULT);
  		
  		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chart);
  		chart_container.addView(chartView,0);
	}
	
	
	
	public void onViewCreated(View view, Bundle savedInstanceState) {
	    // TODO Auto-generated method stub
	    super.onViewCreated(view, savedInstanceState);
	    setupChart();
	  	   		
	    }
	   

}
