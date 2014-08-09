package com.example.energylens;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ComparisonFragment extends Fragment{
	// Pie Chart Section Names
    String[] users = new String[] {
        "Vedant", "Manaswi", "Amarjeet", "P.K.",
        "Pushpendra"};
    
    ArrayList user_dist=new ArrayList<int []>();
    
    int[] user1={4,9,1,10,5,2,8,3};
    int[] user2={36,11,9,20,5,18,2,7};
    
    XYMultipleSeriesRenderer mRenderer=new XYMultipleSeriesRenderer();

    // Pie Chart Section Value
    double[] distribution = { 5, 13, 56, 2, 24} ;
    double[] distribution2 = {10,50,5,20,15};
    double[] distribution3={20,20,20,20,20};
	
	int[] red={0,102,153,204,0,10,71,204,0,255,255,204,0,0,102,204};
	 int[] green={0,0,0,0,102,10,71,0,204,255,255,102,204,204,204,204};
	 int[] blue={204,204,153,102,204,255,255,0,204,71,10,0,102,0,0,0};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_comparison, container, false);
        
        
        return rootView;
    }
	
	public void setupChart(){
	     
        int[] colors = new int[users.length];
        		
        for(int i=0;i<users.length;i++){
        	
        	colors[i]=Color.argb(191,red[i], green[i], blue[i]);
        }
        
        drawChart(users,distribution,colors);
 
	}
	
	public void setUsers(){
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		CompDistributionFragment fragment = new CompDistributionFragment();
		
		float[] distribution_f=new float[distribution.length];
		
		for(int i=0;i<distribution.length;i++){
			distribution_f[i]=(float)distribution[i];
		}
				
		fragment=CompDistributionFragment.newInstance("TV",distribution_f);
		fragmentTransaction.add(R.id.CompGroup, fragment,"TV");
		
		distribution_f=new float[distribution2.length];
		
		for(int i=0;i<distribution2.length;i++){
			distribution_f[i]=(float)distribution2[i];
		}
				
		fragment=CompDistributionFragment.newInstance("AC",distribution_f);
		fragmentTransaction.add(R.id.CompGroup, fragment,"AC");
				
		distribution_f=new float[distribution3.length];
		
		for(int i=0;i<distribution2.length;i++){
			distribution_f[i]=(float)distribution3[i];
		}
				
		fragment=CompDistributionFragment.newInstance("Fan",distribution_f);
		fragmentTransaction.add(R.id.CompGroup, fragment,"Fan");
				
		fragmentTransaction.commit();
	
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
	        
	        defaultRenderer.setScale(0.75f);
	        defaultRenderer.setChartTitleTextSize(20);
	        defaultRenderer.setPanEnabled(false);
	        defaultRenderer.setZoomButtonsVisible(true);
	        defaultRenderer.setChartTitle("You v/s Other Occupants");
	        defaultRenderer.setDisplayValues(true);
	        defaultRenderer.setLabelsColor(Color.BLACK);
	    	defaultRenderer.setChartTitleTextSize(18);
	  		defaultRenderer.setLabelsTextSize(18);
	  		defaultRenderer.setShowLegend(false);	  			       
	         		
	  		GraphicalView chartView = ChartFactory.getDoughnutChartView(getActivity(), mSeries, defaultRenderer);
	  		
	  		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chartComp);
	  		chart_container.addView(chartView,0);

	    }
	 
	 public void onViewCreated(View view, Bundle savedInstanceState) {
		    // TODO Auto-generated method stub
		    super.onViewCreated(view, savedInstanceState);
		    setupChart();		    
		  	setUsers();
		    }
		   
	
}
