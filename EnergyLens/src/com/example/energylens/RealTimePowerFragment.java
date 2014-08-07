package com.example.energylens;

import java.util.Random;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class RealTimePowerFragment extends Fragment{
	
	private Handler mHandler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_realtimepower, container, false);
         
        return rootView;
    }
	
	public void setupChart(){
		Random rnd=new Random();
		int counter=0;
		while(true){
			drawChart(counter,rnd.nextInt(3000));
			counter++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	 XYSeries mSeries = new XYSeries("Power");
     
	
	public void drawChart(int x, int y){
		Log.i("ELSERVICES", "RTP");
        mSeries.add(x, y);
        if(x>60){
        	mSeries.remove(0);
        }
        
  	  XYSeriesRenderer renderer = new XYSeriesRenderer();
	  renderer.setLineWidth(2);
	  renderer.setColor(Color.RED);
	 
	  renderer.setDisplayBoundingPoints(true);
	  renderer.setPointStyle(PointStyle.CIRCLE);
	  renderer.setPointStrokeWidth(3);
	  renderer.setDisplayChartValues(true);
	  
	  final XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	  mRenderer.addSeriesRenderer(renderer);
	  
	  mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); 
	  
		mRenderer.setPanEnabled(false, false);
		mRenderer.setPanEnabled(true);
  		mRenderer.setPanLimits(new double[] {0,24,0,5000});
		mRenderer.setYAxisMax(5000);
		mRenderer.setYAxisMin(0);
		mRenderer.setChartTitleTextSize(14);
  		mRenderer.setLabelsColor(Color.BLACK);
  		mRenderer.setLabelsTextSize(18);
  		mRenderer.setChartTitle("Real-Time Power Consumption");
		mRenderer.setShowGrid(true);
	
  		final XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
  		dataset.addSeries(mSeries);
  		
  		
  		mHandler.post(new Runnable() {
            @Override
            public void run() {
            	GraphicalView chartView = ChartFactory.getLineChartView(getActivity(), dataset, mRenderer);
          		Log.i("ELSERVICES", "RTP "+chartView.toString());  
          		
          		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chartComp);
          		
          		chart_container.addView(chartView,0);

            }
        });
  			}
	
	
	
	public void onViewCreated(View view, Bundle savedInstanceState) {
	    // TODO Auto-generated method stub
	    super.onViewCreated(view, savedInstanceState);
	    Thread thr = new Thread(null, mTask, "RealTime_Power");
//        thr.start();
	    }
	
	 Runnable mTask = new Runnable() {
	        public void run() {
	        	
	            synchronized (this) {
	              try {
	            	  setupChart();
	               	  }

	               catch (Exception e) {
	              }
	            }
	        }
	      };
}
