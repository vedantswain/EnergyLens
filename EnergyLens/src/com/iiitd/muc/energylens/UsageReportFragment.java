package com.iiitd.muc.energylens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class UsageReportFragment extends Fragment implements OnItemSelectedListener,TimePickerDialogFragment.TimePickerDialogListener{

	View inflateView;
	private String[] locations={"New Location","Kitchen","Dining Room","Bedroom1","Bedroom2","Bedroom3","Study","Corridor"};
	private String[] labels={"New Appliance","Fan","AC","Microwave","TV","Computer","Printer","Washing Machine","Fan+AC"};
	private ArrayList<String> labelsList=new ArrayList<String>();
	private ArrayList<String> locList=new ArrayList<String>();
	private ArrayList<String> occList=new ArrayList<String>();
	private ArrayList<String> hourList,minList;
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
	TextView startTimeText, stopTimeText;
	Button startTimeBtn, stopTimeBtn;
	private String changeTimeOf="";
	private long startTime=0, stopTime=0;

	static final int FRAGMENT_ID=2512;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		inflateView=inflater.inflate(R.layout.fragment_usagereport, container, false);
		String appliance=getArguments().getString("appliance");
		long usage=getArguments().getLong("usage");
		final long from=getArguments().getLong("from")*1000;
		final long to=getArguments().getLong("to")*1000;
		id=getArguments().getLong("id");
		String loc=getArguments().getString("loc");

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		dateFormat.format(from);
		TextView activityText=(TextView)inflateView.findViewById(R.id.activityText);
		String fromTimeString=dateFormat.format(from).toString();
		String toTimeString=dateFormat.format(to).toString();
		final String activityLine="Used "+appliance+" from "+fromTimeString
				+" to "+toTimeString
				+" in "+loc+" consumed: "+Long.toString(usage)+" Wh";
		
//		Spannable sb = new SpannableString( activityLine );
//		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), activityLine.indexOf(appliance), appliance.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold
//		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), activityLine.indexOf(loc), loc.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold
//		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), activityLine.indexOf(toTimeString), toTimeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold
//		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), activityLine.indexOf(fromTimeString), toTimeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold

		activityText.setText(activityLine);

		getUpdatedPreferences();
		setAppSpinner();
		setLocSpinner();
		setOccSpinner();
		setMinsSpinner();
		setHoursSpinner();

		startTimeText=(TextView)inflateView.findViewById(R.id.startTimeText);
		startTimeText.setVisibility(View.GONE);
		startTimeBtn=(Button)inflateView.findViewById(R.id.startTimeBtn);
		startTimeBtn.setText(dateFormat.format(from).toString());
		startTimeBtn.setVisibility(View.GONE);
		startTimeBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				TimePickerDialogFragment newFragment = new TimePickerDialogFragment();
				newFragment.changeInitTime(from);
				newFragment.setTargetFragment(UsageReportFragment.this,FRAGMENT_ID);
				newFragment.show(getFragmentManager(), "Start Time Picker");
				changeTimeOf="start";
			} 
		}); 


		stopTimeText=(TextView)inflateView.findViewById(R.id.stopTimeText);
		stopTimeText.setVisibility(View.GONE);
		stopTimeBtn=(Button)inflateView.findViewById(R.id.stopTimeBtn);
		stopTimeBtn.setText(dateFormat.format(to).toString());
		stopTimeBtn.setVisibility(View.GONE);
		stopTimeBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				TimePickerDialogFragment newFragment = new TimePickerDialogFragment();
				newFragment.changeInitTime(to);
				newFragment.setTargetFragment(UsageReportFragment.this,FRAGMENT_ID);
				newFragment.show(getFragmentManager(), "Stop Time Picker");
				changeTimeOf="stop";
			} 
		}); 

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
					startTimeText.setVisibility(View.GONE);
					startTimeBtn.setVisibility(View.GONE);
					stopTimeText.setVisibility(View.GONE);
					stopTimeBtn.setVisibility(View.GONE);
					String[] pairEmpty={"",""};
					GroundReportActivity.changeCorrectionIds(id,pairEmpty,0,timeOfStay,0,0, 0);
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
					if(occList.size()>1){
						occSpinner.setVisibility(View.VISIBLE);
						occIcon.setVisibility(View.VISIBLE);
					}
					appIcon.setVisibility(View.VISIBLE);
					locIcon.setVisibility(View.VISIBLE);

					startTimeText.setVisibility(View.VISIBLE);
					startTimeBtn.setVisibility(View.VISIBLE);
					stopTimeText.setVisibility(View.VISIBLE);
					stopTimeBtn.setVisibility(View.VISIBLE);

					if(wasClicked==0)
						GroundReportActivity.changeCorrectionIds(id,pair,toOcc,timeOfStay,startTime,stopTime,2);
					else
						GroundReportActivity.changeCorrectionIds(id,pair,toOcc,timeOfStay,startTime,stopTime, 1);

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
		hourList=new ArrayList<String>();
		hourList.add("--");
		for(long i=0;i<=24;i++)
			hourList.add(Long.toString(i));

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, hourList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		hoursSpinner.setAdapter(dataAdapter);
		hoursSpinner.setOnItemSelectedListener(this);
	}

	private void setMinsSpinner(){
		minsSpinner=(Spinner) inflateView.findViewById(R.id.minSpinner);
		minList=new ArrayList<String>();
		minList.add("--");
		for(long i=0;i<=60;i++)
			minList.add(Long.toString(i));

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
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
			if(pos!=0){
				mins=Long.parseLong(minList.get(pos));
				timeOfStay=(hrs*60*60*1000)+(mins*60*1000);
				if(timeOfStay!=0){
					Log.v("ELSERVICES", timeOfStay+" will be added");
					GroundReportActivity.changeTimeOfStay(id,timeOfStay);
				}
			}
		}
		else if(parent.equals(hoursSpinner)){
			if(pos!=0){
				hrs=Long.parseLong(hourList.get(pos));
				timeOfStay=(hrs*60*60*1000)+(mins*60*1000);
				if(timeOfStay!=0){
					Log.v("ELSERVICES", timeOfStay+" will be added");
					GroundReportActivity.changeTimeOfStay(id,timeOfStay);
				}
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


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onSetTime(int hourOfDay, int minute) {
		// TODO Auto-generated method stub

	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch(requestCode) {
		case FRAGMENT_ID:

			if (resultCode == Activity.RESULT_OK) {
				Bundle bundle=data.getExtras();
				int hourOfDay=bundle.getInt("hourOfDay",0);
				int minute=bundle.getInt("minute",0);
				long time=(hourOfDay*60*60+minute*60)*1000;

				Calendar c=Calendar.getInstance();
				Calendar reportDate=Calendar.getInstance();
				reportDate.setTime(new Date(GroundReportActivity.current_date));
				c.set(reportDate.get(Calendar.YEAR),reportDate.get(Calendar.MONTH) ,reportDate.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
				String timeString=dateFormat.format(c.getTimeInMillis()).toString();
				if(changeTimeOf=="start"){
					startTime=c.getTimeInMillis();
					startTimeBtn.setText(timeString);
					GroundReportActivity.changeStartTime(id, time);
				}
				else if(changeTimeOf=="stop"){
					stopTime=c.getTimeInMillis();
					stopTimeBtn.setText(timeString);
					GroundReportActivity.changeStopTime(id, time);
				}

				//				Log.v("ELSERVICES", "Correction Time: "+c.toString());
			} else if (resultCode == Activity.RESULT_CANCELED){

			}
			break;
		}
	}
}
