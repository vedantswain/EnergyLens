package com.example.energylens;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
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

public class ComparisonFragment extends Fragment{	
	// Pie Chart Section Names
	String[] users = new String[] {
			"Vedant", "Manaswi", "Amarjeet", "P.K.",
	"Pushpendra"};

	ArrayList user_dist=new ArrayList<int []>();
	String[] titles={"Vedant","Manaswi","Amarjeet"};

	int[] user1={4,9,1,10,5,2,8,3};
	int[] user2={36,11,9,20,5,18,2,7};

	GraphicalView chartView;
	XYMultipleSeriesRenderer mRenderer=new XYMultipleSeriesRenderer();
	XYMultipleSeriesDataset	mDataset=new XYMultipleSeriesDataset();

	// Pie Chart Section Value
	double[] distribution = { 5, 13, 56, 2, 24} ;
	double[] distribution2 = {10,50,5,20,15};
	double[] distribution3={20,20,20,20,20};

	Double[] comp3={2250.0,1040.0,930.0,4240.0,1640.0};
	Double[] comp2={3040.0,10.0,2115.0,2050.0,6650.0};
	Double[] comp1={1020.0,5050.0,765.0,2810.0,1010.0};

	ArrayList<Double[]> compList=new ArrayList<Double[]>();

	int[] red={0,102,153,204,0,10,71,204,0,255,255,204,0,0,102,204};
	int[] green={0,0,0,0,102,10,71,0,204,255,255,102,204,204,204,204};
	int[] blue={204,204,153,102,204,255,255,0,204,71,10,0,102,0,0,0};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_comparison, container, false);

		compList.add(comp1);
		compList.add(comp2);
		compList.add(comp3);
		
		compList=setStack();

		return rootView;
	}

	public void setupChart(){

		int[] colors = new int[users.length];

		for(int i=0;i<users.length;i++){

			colors[i]=Color.argb(191,red[i], green[i], blue[i]);
		}

		drawChart(users,distribution,colors);

	}
	
	public ArrayList<Double[]> setStack(){
		for(int i=1;i<compList.size();i++){
			for(int j=0;j<compList.get(i).length;j++){
				compList.get(i)[j]+=compList.get(i-1)[j];
			}
		}
		
		ArrayList<Double[]> stackList=new ArrayList<Double[]>();
		for(int i=compList.size()-1;i>=0;i--){
			stackList.add(compList.get(i));
		}
		return stackList;
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

		XYSeriesRenderer bar_renderer;
		XYSeries mSeries; 	 
		
		for(int i=0;i<compList.size();i++){

			//			Log.v("ELSERVICES","Compare: "+ compList.get(i)[0].toString());

			bar_renderer = new XYSeriesRenderer();
			bar_renderer.setLineWidth(1);
			bar_renderer.setColor(colors[i]);
			bar_renderer.setDisplayBoundingPoints(true);
			//		bar_renderer.setDisplayChartValues(true);

			mSeries = new XYSeries(titles[i]); 	 
			for(int j=0;j<compList.get(i).length;j++){
				mSeries.add(j+1,compList.get(i)[j]);
			}
			mRenderer.addSeriesRenderer(bar_renderer);
			mDataset.addSeries(mSeries);


		}

		int[] margins={20,40,20,5};
		mRenderer.setMarginsColor(Color.argb(0xff, 0xf0, 0xf0, 0xf0));
				
		mRenderer.setMargins(margins);
		mRenderer.setPanEnabled(true);
		mRenderer.setPanLimits(new double[] {0,24,0,15000});
		mRenderer.setZoomButtonsVisible(true);
//		mRenderer.setYAxisMax(compList.get(0)[0]);
		mRenderer.setYAxisMin(0);
		mRenderer.setXAxisMin(0);
		mRenderer.setXAxisMax(12);
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
		
		//
		chartView = ChartFactory.getBarChartView(getActivity().getApplicationContext(), mDataset, mRenderer, Type.STACKED );
		LinearLayout chart_container=(LinearLayout)getView().findViewById(R.id.chartComp);
		chart_container.addView(chartView,0);	
		
		chartView.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v) {
	        	Log.v("ELSERVICES", "Graph clicked");
	          // handle the click event on the chart
	          SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
//	          Log.v("ELSERVICES", Float.toString(chartView.getX())+" "+Float.toString(chartView.getY()));
//	          Log.v("ELSERVICES", Float.toString(chartView.getScaleX())+" "+chartView);
	           }
	      });
		chartView.setOnTouchListener(new View.OnTouchListener() {
  			ViewPager mViewPager=CollectionTabActivity.mViewPager;
  			ViewParent mParent= (ViewParent)getActivity().findViewById(R.id.CompGroup);
  		
  			float mFirstTouchX,mFirstTouchY;

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
				if(seriesSelection!=null)
		          Log.v("ELSERVICES", Float.toString(chartView.getX())+" touch "+Float.toString(chartView.getY()));
				
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

	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
		setupChart();		    
		setUsers();
	}


}
