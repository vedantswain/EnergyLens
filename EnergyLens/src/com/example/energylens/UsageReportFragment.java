package com.example.energylens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class UsageReportFragment extends Fragment implements OnItemSelectedListener {

	View inflateView;
	private String[] locations={"New Location","Kitchen","Dining Room","Bedroom1","Bedroom2","Bedroom3","Study","Corridor"};
	private String[] labels={"New Appliance","Fan","AC","Microwave","TV","Computer","Printer","Washing Machine","Fan+AC"};
	private ArrayList<String> labelsList=new ArrayList<String>();
	private ArrayList<String> locList=new ArrayList<String>();
	private ArrayList<String> occList=new ArrayList<String>();
	private ArrayList<Long> hourList,minList;
	private Spinner appSpinner,locSpinner,hoursSpinner,minsSpinner,occSpinner;	
	private ImageView appIcon,locIcon,occIcon;
	private String toApp="none",toLoc="none";

	int wasClicked=-1,toOcc=0;
	long mins=0;
	long hrs=0;
	long id;
	String[] pair={"",""};
	long timeOfStay=0;
	int corrected_count=0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		inflateView=inflater.inflate(R.layout.fragment_usagereport, container, false);
		String appliance=getArguments().getString("appliance");
		long usage=getArguments().getLong("usage");
		long from=getArguments().getLong("from")*1000;
		long to=getArguments().getLong("to")*1000;
		id=getArguments().getLong("id");
		String loc=getArguments().getString("loc");

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		dateFormat.format(from);
		TextView activityText=(TextView)inflateView.findViewById(R.id.activityText);
		final String activityLine="Using "+appliance+" from "+dateFormat.format(from).toString()+" to "+dateFormat.format(to).toString()
				+" in "+loc+" consumed: "+Long.toString(usage)+" Wh";
		activityText.setText(activityLine);

		getUpdatedPreferences();
		setAppSpinner();
		setLocSpinner();
		setOccSpinner();
		setMinsSpinner();
		setHoursSpinner();


		RadioButton isCorrect=(RadioButton) inflateView.findViewById(R.id.radio_correct);
		isCorrect.setOnClickListener(new OnClickListener(){
			//			@Override
			public void onClick(View view) {
				// Is the button now checked?
				boolean checked = ((RadioButton) view).isChecked();

				// Check which radio button was clicked
				if (checked){
					setTimeView();
					appSpinner.setVisibility(View.GONE);
					appSpinner.setSelection(0);
					appIcon.setVisibility(View.GONE);
					locSpinner.setVisibility(View.GONE);
					locSpinner.setSelection(0);
					locIcon.setVisibility(View.GONE);
					occSpinner.setVisibility(View.GONE);
					occSpinner.setSelection(0);
					occIcon.setVisibility(View.GONE);
					GroundReportActivity.changeCorrectionIds(id,pair,0,timeOfStay, 0);
					wasClicked=0;
				}
			}
		});

		RadioButton isIncorrect=(RadioButton) inflateView.findViewById(R.id.radio_incorrect);
		isIncorrect.setOnClickListener(new OnClickListener(){
			//			@Override
			public void onClick(View view) {
				// Is the button now checked?
				boolean checked = ((RadioButton) view).isChecked();

				// Check which radio button was clicked
				if (checked){
					setTimeView();
					appSpinner.setVisibility(View.VISIBLE);
					locSpinner.setVisibility(View.VISIBLE);
					if(occList.size()>1)
						occSpinner.setVisibility(View.VISIBLE);
					appIcon.setVisibility(View.VISIBLE);
					locIcon.setVisibility(View.VISIBLE);
					occIcon.setVisibility(View.VISIBLE);

					if(wasClicked==0)
						GroundReportActivity.changeCorrectionIds(id,pair,toOcc,timeOfStay,2);
					else
						GroundReportActivity.changeCorrectionIds(id,pair,toOcc,timeOfStay, 1);

					wasClicked=1;
				}
			}
		});

		return inflateView;
	}


	private void setTimeView(){

		hoursSpinner.setVisibility(View.VISIBLE);
		TextView tv=(TextView)inflateView.findViewById(R.id.textHours);
		tv.setVisibility(View.VISIBLE);

		minsSpinner.setVisibility(View.VISIBLE);
		tv=(TextView)inflateView.findViewById(R.id.textMins);
		tv.setVisibility(View.VISIBLE);
		ImageView iv=(ImageView)inflateView.findViewById(R.id.imageView1);
		iv.setVisibility(View.VISIBLE);
	}

	public void getUpdatedPreferences(){
		SharedPreferences sp=getActivity().getSharedPreferences(Common.EL_PREFS,0);
		String updatedLabels=sp.getString("APP_LIST", "");
		if(updatedLabels!=""){
			Common.changeActivityApps(updatedLabels.split(","));
			labels=updatedLabels.split(",");

		}
		String updatedLocs=sp.getString("LOC_LIST", "");
		if(updatedLocs!=""){
			Common.changeActivityLocs(updatedLocs.split(","));
			locations=updatedLocs.split(",");
		}
	}

	private void setHoursSpinner(){
		hoursSpinner=(Spinner) inflateView.findViewById(R.id.hourSpinner);
		hourList=new ArrayList<Long>();
		for(long i=0;i<=24;i++)
			hourList.add(i);

		ArrayAdapter<Long> dataAdapter = new ArrayAdapter<Long>(getActivity(),
				android.R.layout.simple_spinner_item, hourList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		hoursSpinner.setAdapter(dataAdapter);
		hoursSpinner.setOnItemSelectedListener(this);
	}

	private void setMinsSpinner(){
		minsSpinner=(Spinner) inflateView.findViewById(R.id.minSpinner);
		minList=new ArrayList<Long>();
		for(long i=0;i<=60;i++)
			minList.add(i);

		ArrayAdapter<Long> dataAdapter = new ArrayAdapter<Long>(getActivity(),
				android.R.layout.simple_spinner_item, minList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		minsSpinner.setAdapter(dataAdapter);
		minsSpinner.setOnItemSelectedListener(this);
	}

	private void setAppSpinner(){
		appSpinner = (Spinner) inflateView.findViewById(R.id.app_spinner);
		for(String app:labels)
			labelsList.add(app);
		labelsList.add("Unknown");
		labelsList.remove(0);
		labelsList.add(0, "none");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, labelsList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		appSpinner.setAdapter(dataAdapter);
		appSpinner.setOnItemSelectedListener(this);
		appSpinner.setVisibility(View.GONE);
		appIcon = (ImageView) inflateView.findViewById(R.id.appIcon);
		appIcon.setVisibility(View.GONE);
	}

	private void setLocSpinner(){
		locSpinner = (Spinner) inflateView.findViewById(R.id.loc_spinner);
		for(String loc:locations)
			locList.add(loc);
		locList.remove(0);
		locList.add(0, "none");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, locList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		locSpinner.setAdapter(dataAdapter);
		locSpinner.setOnItemSelectedListener(this);
		locSpinner.setVisibility(View.GONE);
		locIcon = (ImageView) inflateView.findViewById(R.id.locIcon);
		locIcon.setVisibility(View.GONE);
	}

	private void setOccSpinner(){
		occSpinner = (Spinner) inflateView.findViewById(R.id.occSpinner);
		occSpinner.setVisibility(View.GONE);
		occIcon = (ImageView) inflateView.findViewById(R.id.occIcon);
		occIcon.setVisibility(View.GONE);
		for(String occ:GroundReportActivity.occupantList){
			occList.add(occ);
			Log.v("ELSERVICES","Occupants: "+occ);
		}
		if(occList.size()>0){
			occList.add(0, "none");
			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_spinner_item, occList);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			occSpinner.setAdapter(dataAdapter);
			occSpinner.setOnItemSelectedListener(this);
		}
	}

	public static  UsageReportFragment newInstance(long id,String appliance,long use,String loc,long from, long to) {
		UsageReportFragment myFragment = new UsageReportFragment();

		Bundle args = new Bundle();
		args.putLong("id", id);
		args.putString("appliance", appliance);
		args.putLong("usage", use);
		args.putString("loc", loc);
		args.putLong("from", from);
		args.putLong("to", to);
		myFragment.setArguments(args);

		return myFragment;
	}

	public void onViewCreated(){

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long arg_id) {
		// TODO Auto-generated method stub
		if(parent.equals(appSpinner)){
			toApp=labelsList.get(pos);
			Log.v("ELSERVICES", toApp+","+toLoc+" will be added");
			if(!toApp.equals("none") && !toLoc.equals("none")){
				Log.v("ELSERVICES", toApp+","+toLoc+" will be added");
				String[] pair={toApp,toLoc};
				GroundReportActivity.changeCorrectionPairData(id,pair);
			}
		}
		else if(parent.equals(locSpinner)){
			toLoc=locList.get(pos);
			Log.v("ELSERVICES", toApp+","+toLoc+" will be added");
			if(!toApp.equals("none") && !toLoc.equals("none")){
				Log.v("ELSERVICES", toApp+","+toLoc+" will be added");
				String[] pair={toApp,toLoc};
				GroundReportActivity.changeCorrectionPairData(id,pair);
			}
		}
		else if(parent.equals(minsSpinner)){
			mins=(long)minList.get(pos);
			timeOfStay=(hrs*60*60*1000)+(mins*60*1000);
			if(timeOfStay!=0){
				Log.v("ELSERVICES", timeOfStay+" will be added");
				GroundReportActivity.changeTimeOfStay(id,timeOfStay);
			}
		}
		else if(parent.equals(hoursSpinner)){
			hrs=(long)hourList.get(pos);
			timeOfStay=(hrs*60*60*1000)+(mins*60*1000);
			if(timeOfStay!=0){
				Log.v("ELSERVICES", timeOfStay+" will be added");
				GroundReportActivity.changeTimeOfStay(id,timeOfStay);
			}
		}
		else if(parent.equals(occSpinner)){
			toOcc=pos;
			if(pos!=0){
				Log.v("ELSERVICES",toOcc+" will be added");
				GroundReportActivity.changeOccupant(id, pos);
			}
		}

	}

	public void onTimeSelect(View view){

	}

	public void offTimeSelect(View view){

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}


}
