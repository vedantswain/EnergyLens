package com.example.energylens;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Path.FillType;
import android.os.Bundle;
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
	
	public void drawChart(){
		Log.v("ELSERVICES", "chart drawn");
		int[] x = { 1,2,3,4,5,6,7,8 };
        int[] income = { 2000,2500,2700,3000,2800,3500,3700,3800};
        
        // Creating an  XYSeries for Income
        XYSeries incomeSeries = new XYSeries("Income");
        
        for(int i=0;i<x.length;i++){
            incomeSeries.add(x[i], income[i]);
        }
        
        XYSeriesRenderer renderer = new XYSeriesRenderer();
  	  renderer.setLineWidth(2);
  	  renderer.setColor(Color.GRAY);
  	 
  	  renderer.setDisplayBoundingPoints(true);
  	  renderer.setPointStyle(PointStyle.CIRCLE);
  	  renderer.setPointStrokeWidth(3);
  	  
//  	  XYSeriesRenderer.FillOutsideLine fill=new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BELOW);
//  	  fill.setColor(Color.GRAY);
//  	  renderer.addFillOutsideLine(fill);
  	  
  	  XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
  	  mRenderer.addSeriesRenderer(renderer);
  	  
  	  mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); 
  	  
  		mRenderer.setPanEnabled(false, false);
  		mRenderer.setYAxisMax(5000);
  		mRenderer.setYAxisMin(0);
  		mRenderer.setShowGrid(true);
  		
  		
  		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
  		dataset.addSeries(incomeSeries);
  		
  		GraphicalView chartView = ChartFactory.getLineChartView(getActivity(), dataset, mRenderer); 
  		
  		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chart);
  		chart_container.addView(chartView,0);
	}
	
	public void onViewCreated(View view, Bundle savedInstanceState) {
	    // TODO Auto-generated method stub
	    super.onViewCreated(view, savedInstanceState);
	    drawChart();
	  	   		
	    }
	   

}
