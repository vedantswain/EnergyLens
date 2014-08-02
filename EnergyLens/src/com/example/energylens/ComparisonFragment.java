package com.example.energylens;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ComparisonFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_comparison, container, false);
         
        return rootView;
    }
	
	public void setupChart(){
	    // Pie Chart Section Names
        String[] code = new String[] {
            "Eclair & Older", "Froyo", "Gingerbread", "Honeycomb",
            "IceCream Sandwich", "Jelly Bean"
        };
 
        // Pie Chart Section Value
        double[] distribution = { 3.9, 12.9, 55.8, 1.9, 23.7, 1.8 } ;
 
        // Color of each Pie Chart Sections
        int red=5,blue=5,green=5;
        double color_var=0.75;
        
        int[] colors = new int[code.length];
        		
        for(int i=0;i<code.length;i++){
        		red=(int) ((255-red)*color_var);
        		blue=(int) ((255-blue)*color_var);
        		green=(int) ((255-green)*color_var);
        	
        	colors[i]=Color.argb(255, red, green, blue);
        }
        
        drawChart(code,distribution,colors);
 
	}
	
	 private void drawChart(String[] users, double[] portion, int[] colors){
		 
	    
	        // Instantiating CategorySeries to plot Pie Chart
	        MultipleCategorySeries mSeries = new MultipleCategorySeries("Occupant Power Distribution");
	       mSeries.add(users, portion);
	       
	 
	        // Instantiating a renderer for the Pie Chart
	        DefaultRenderer defaultRenderer  = new DefaultRenderer();
	        for(int i = 0 ;i<portion.length;i++){
	            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
	            seriesRenderer.setColor(colors[i]);
	            seriesRenderer.setDisplayChartValues(true);
	            // Adding a renderer for a slice
	            defaultRenderer.addSeriesRenderer(seriesRenderer);
	        }
	 
	        defaultRenderer.setChartTitle("Android version distribution as on October 1, 2012 ");
	        defaultRenderer.setChartTitleTextSize(20);
	        defaultRenderer.setZoomEnabled(true);
	        defaultRenderer.setChartTitle("Occupant Power Distribution");
	        defaultRenderer.setDisplayValues(true);
	        defaultRenderer.setLabelsColor(Color.BLACK);
	       
	         		
	  		GraphicalView chartView = ChartFactory.getDoughnutChartView(getActivity(), mSeries, defaultRenderer);
	  		
	  		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chart);
	  		chart_container.addView(chartView,0);

	    }
	 
	 public void onViewCreated(View view, Bundle savedInstanceState) {
		    // TODO Auto-generated method stub
		    super.onViewCreated(view, savedInstanceState);
		    setupChart();
		  	   		
		    }
		   
	
}
