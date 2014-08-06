package com.example.energylens;

import java.util.Calendar;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PersonalEnergyFragment extends Fragment{
	GraphicalView chartView;
	XYMultipleSeriesRenderer appRenderer=new XYMultipleSeriesRenderer(),mRenderer = new XYMultipleSeriesRenderer();
	XYMultipleSeriesDataset appDataset=new XYMultipleSeriesDataset(),mDataset = new XYMultipleSeriesDataset();
	String[] appliances={"TV","Microwave","Computer","AC","Fan","Washing Machine"};
	int[] distribution={30,10,40,40,5,2,2};
	private String EXTRA_TITLE;
	private String message;
	
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
        
        XYSeriesRenderer bar_renderer = new XYSeriesRenderer();
  	  bar_renderer.setLineWidth(1);
  	  bar_renderer.setColor(Color.DKGRAY);
  	  bar_renderer.setDisplayBoundingPoints(true);
  	  
  	  XYSeriesRenderer line_renderer=new XYSeriesRenderer();
  	  line_renderer.setPointStyle(PointStyle.CIRCLE);
  	  line_renderer.setColor(Color.LTGRAY);
  	  line_renderer.setLineWidth(0);
	  line_renderer.setPointStrokeWidth(10);
//  	  renderer.setDisplayChartValues(true);
  	  
//  	  XYSeriesRenderer.FillOutsideLine fill=new XYSeriesRenderer.FillOutsideLine(XYSeriesRenderer.FillOutsideLine.Type.BELOW);
//  	  fill.setColor(Color.GRAY);
//  	  renderer.addFillOutsideLine(fill);
  	  
  	  
  	  mRenderer.addSeriesRenderer(bar_renderer);
  	  mRenderer.addSeriesRenderer(line_renderer);
  	  
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
  		mRenderer.setClickEnabled(true);
  		mRenderer.setSelectableBuffer(50);
  		mRenderer.setShowGrid(true);
  		
  		
  		mDataset.addSeries(mSeries);
  		mDataset.addSeries(mSeries);
  		
//  		XYCombinedChartDef[] types = new XYCombinedChartDef[] {new XYCombinedChartDef(BarChart.TYPE, 0, 1), new XYCombinedChartDef(LineChart.TYPE, 2)};
  		
//  		chartView = ChartFactory.getBarChartView(getActivity(), mDataset, mRenderer, BarChart.Type.DEFAULT);
  		
  		String[] types = new String[] { BarChart.TYPE,LineChart.TYPE };
  		 
		chartView = ChartFactory.getCombinedXYChartView(getActivity().getBaseContext(), mDataset, mRenderer, types);
  		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chart);
  		chart_container.addView(chartView,0);
  		
  		chartView.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	Log.v("ELSERVICES", "Graph clicked");
	          // handle the click event on the chart
	          SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
	          Log.v("ELSERVICES", Float.toString(chartView.getX())+" "+Float.toString(chartView.getY()));
	          if (seriesSelection == null) {
//	            Toast.makeText(getActivity(), "No chart element", Toast.LENGTH_SHORT).show();
	          } else {
	            // display information of the clicked point
	            Toast.makeText(
	                getActivity(),
	                "Chart element in series index " + seriesSelection.getSeriesIndex()
	                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
	                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
	                    + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
	          }
	        }
	      });
  		
  		chartView.setOnTouchListener(new View.OnTouchListener() {
  			ViewPager mViewPager=CollectionTabActivity.mViewPager;
  			ViewParent mParent= (ViewParent)getActivity().findViewById(R.id.PEnGroup);
  		
  			float mFirstTouchX,mFirstTouchY;

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// save the position of the first touch so we can determine whether the user is dragging
  			    // left or right
  			    if (event.getAction() == MotionEvent.ACTION_DOWN) {
  			        mFirstTouchX = event.getX();
  			        mFirstTouchY = event.getY();
  			    }

  			    // when mViewPager.requestDisallowInterceptTouchEvent(true), the viewpager does not
  			    // intercept the events, and the drag events (pan, pinch) are caught by the GraphicalView

  			    // we want to keep the ViewPager from intercepting the event if:
  			    // 1- there are 2 or more touches, i.e. the pinch gesture
  			    // 2- the user is dragging to the left but there is no data to show to the right
  			    // 3- the user is dragging to the right but there is no data to show to the left
  			    if (event.getPointerCount() > 1
  			            || (event.getX() < mFirstTouchX) 
  			            || (event.getX() > mFirstTouchX)
  			            || (event.getY() < mFirstTouchY)
  			            || (event.getY() > mFirstTouchY)) {
  			        mViewPager.requestDisallowInterceptTouchEvent(true);
  			        mParent.requestDisallowInterceptTouchEvent(true);
  			    }
  			    else {
  			        mViewPager.requestDisallowInterceptTouchEvent(false);
  			        mParent.requestDisallowInterceptTouchEvent(true);
  			    }
				// TODO Auto-generated method stub
				return false;
			}
  			
  		});
	}
	
	
	
	
	public void setApps(){
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		DistributionFragment fragment = new DistributionFragment();
		fragment=DistributionFragment.newInstance("TV", 15);
		fragmentTransaction.add(R.id.PEnGroup, fragment);
		
		fragment=DistributionFragment.newInstance("AC", 45);
		fragmentTransaction.add(R.id.PEnGroup, fragment);

		fragment=DistributionFragment.newInstance("Fan", 2);
		fragmentTransaction.add(R.id.PEnGroup, fragment);
		
		fragmentTransaction.commit();
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	      LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.chart);
	      chartView = ChartFactory.getLineChartView(getActivity(), mDataset, mRenderer);
	      
	    
	  }
	
	
	public void onViewCreated(View view, Bundle savedInstanceState) {
	    // TODO Auto-generated method stub
	    super.onViewCreated(view, savedInstanceState);
	    setupChart();
	    setApps();
	}


}
