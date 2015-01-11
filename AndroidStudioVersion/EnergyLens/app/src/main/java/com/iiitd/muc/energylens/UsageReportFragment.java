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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class UsageReportFragment extends Fragment implements OnItemSelectedListener,TimePickerDialogFragment.TimePickerDialogListener{

	View inflateView;
	private String[] locations={"New Location","Dining Room","Drawing Room","Kitchen","Master Bedroom","Bedroom 2","Bedroom 3","Study","Lobby"};
	private String[] labels={"New Appliance","Fan","Light,","TV","AC","Microwave","Geyser","Grinder","Iron"};
	private ArrayList<String> labelsList=new ArrayList<String>();
	private ArrayList<String> locList=new ArrayList<String>();
	private ArrayList<String> occList=new ArrayList<String>();
	private ArrayList<String> hourList,minList;
	private Spinner appSpinner,locSpinner,hoursSpinner,minsSpinner,occSpinner;	
	private ImageView appIcon,locIcon,occIcon;
	private String toApp="none",toLoc="none";

    CheckBox ftCheckBox;

	int wasClicked=-1,toOcc=0;
	long mins=0;
	long hrs=0;
	long id;
	String[] pair={"",""};
	long timeOfStay=-1;
	int corrected_count=0;
	TextView startTimeText, endTimeText;
	Button startTimeBtn, endTimeBtn;
	private String changeTimeOf="";
	private long startTime=0, endTime=0;

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
        //startTime=from;
        //endTime=to;
		id=getArguments().getLong("id");
		String loc=getArguments().getString("loc");

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		dateFormat.format(from);
		TextView activityText=(TextView)inflateView.findViewById(R.id.activityText);
		String fromTimeString=dateFormat.format(from).toString();
		String toTimeString=dateFormat.format(to).toString();

		final String activityLine="Used <b>"+appliance+"</b> from <b>"+fromTimeString
				+"</b> to <b>"+toTimeString
				+"</b> in <b>"+loc+"</b> consumed: "+Long.toString(usage)+" Wh";

		//		Spannable sb = new SpannableString( activityLine );
		//		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), activityLine.indexOf(appliance), appliance.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold
		//		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), activityLine.indexOf(loc), loc.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold
		//		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), activityLine.indexOf(toTimeString), toTimeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold
		//		sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), activityLine.indexOf(fromTimeString), toTimeString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //bold

		activityText.setText(Html.fromHtml(activityLine));

		//getUpdatedPreferences();
		setAppSpinner();
		setLocSpinner();
		setOccSpinner();
		setMinsSpinner();
		setHoursSpinner();


		startTimeText=(TextView)inflateView.findViewById(R.id.startTimeText);
		startTimeText.setVisibility(View.GONE);
		startTimeBtn=(Button)inflateView.findViewById(R.id.startTimeBtn);
		//startTimeBtn.setText(dateFormat.format(from).toString());
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


		endTimeText=(TextView)inflateView.findViewById(R.id.stopTimeText);
		endTimeText.setVisibility(View.GONE);
		endTimeBtn=(Button)inflateView.findViewById(R.id.stopTimeBtn);
		//endTimeBtn.setText(dateFormat.format(to).toString());
		endTimeBtn.setVisibility(View.GONE);
		endTimeBtn.setOnClickListener(new OnClickListener()
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
					Log.v("ELSERVICES", "Correct clicked");
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
					endTimeText.setVisibility(View.GONE);
					endTimeBtn.setVisibility(View.GONE);
					String[] pairEmpty={"",""};
					GroundReportActivity.changeCorrectionIds(id,pairEmpty,0,timeOfStay,0,0, 0);
					wasClicked=0;
				}
			}
		});

		final RadioButton isIncorrect=(RadioButton) inflateView.findViewById(R.id.radio_incorrect);
		isIncorrect.setOnClickListener(new OnClickListener(){
			//			@Override
			public void onClick(View view) {
				// Is the button now checked?
				boolean checked = ((RadioButton) view).isChecked();

				// Check which radio button was clicked
				if (checked){
					Log.v("ELSERVICES", "Incorrect clicked");
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
					endTimeText.setVisibility(View.VISIBLE);
					endTimeBtn.setVisibility(View.VISIBLE);

					if(wasClicked==0)
						GroundReportActivity.changeCorrectionIds(id,pair,toOcc,timeOfStay,startTime,endTime,2);
					else
						GroundReportActivity.changeCorrectionIds(id,pair,toOcc,timeOfStay,startTime,endTime, 1);

					wasClicked=1;
				}
			}
		});

        ftCheckBox = (CheckBox)inflateView.findViewById(R.id.fullTimeCheckBox);
        ftCheckBox.setOnClickListener(new OnClickListener() {

                                          @Override
                                          public void onClick(View v) {
                                              Log.v("ELSERVICES","full time checkBox clicked");
                                              boolean checked = ((CheckBox) v).isChecked();
                                              if (checked) {
                                                  hoursSpinner.setVisibility(View.INVISIBLE);
                                                  TextView tv = (TextView) inflateView.findViewById(R.id.textHours);
                                                  tv.setVisibility(View.INVISIBLE);

                                                  minsSpinner.setVisibility(View.INVISIBLE);
                                                  tv = (TextView) inflateView.findViewById(R.id.textMins);
                                                  tv.setVisibility(View.INVISIBLE);

                                                  timeOfStay=to-from;
                                                  if(isIncorrect.isChecked() && startTime!=0 && endTime!=0 ){
                                                       Log.v("ELSERVICES","adding new time of stay");
                                                       timeOfStay=endTime-startTime;
                                                  }
                                                  GroundReportActivity.changeTimeOfStay(id,timeOfStay);
                                              } else {
                                                  hoursSpinner.setVisibility(View.VISIBLE);
                                                  hoursSpinner.setSelection(0);
                                                  TextView tv = (TextView) inflateView.findViewById(R.id.textHours);
                                                  tv.setVisibility(View.VISIBLE);

                                                  minsSpinner.setVisibility(View.VISIBLE);
                                                  minsSpinner.setSelection(0);
                                                  tv = (TextView) inflateView.findViewById(R.id.textMins);
                                                  tv.setVisibility(View.VISIBLE);

                                                  timeOfStay=-1;
                                                  GroundReportActivity.changeTimeOfStay(id,timeOfStay);
                                              }
                                          }
                                      }
        );


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

        ftCheckBox.setVisibility(View.VISIBLE);

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
		for(String app:GroundReportActivity.correction_apps)
			labelsList.add(app);
		//		labelsList.add("Unknown");
		//		labelsList.remove(0);
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
		for(String loc:GroundReportActivity.correction_locs)
			locList.add(loc);
		//		locList.remove(0);
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
			//			Log.v("ELSERVICES","Occupants: "+occ);
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
			String[] pair={toApp,toLoc};
			GroundReportActivity.changeCorrectionPairData(id,pair);

		}
		else if(parent.equals(locSpinner)){
			toLoc=locList.get(pos);
			Log.v("ELSERVICES", toApp+","+toLoc+" will be added");
			String[] pair={toApp,toLoc};
			GroundReportActivity.changeCorrectionPairData(id,pair);
		}
		else if(parent.equals(minsSpinner)){
			if(pos!=0){
				mins=Long.parseLong(minList.get(pos));
				timeOfStay=(hrs*60*60*1000)+(mins*60*1000);
				Log.v("ELSERVICES", timeOfStay+" will be added");
				GroundReportActivity.changeTimeOfStay(id,timeOfStay);
			}
			else
				GroundReportActivity.changeTimeOfStay(id,-1);
		}
		else if(parent.equals(hoursSpinner)){
			if(pos!=0){
				hrs=Long.parseLong(hourList.get(pos));
				timeOfStay=(hrs*60*60*1000)+(mins*60*1000);
				Log.v("ELSERVICES", timeOfStay+" will be added");
				GroundReportActivity.changeTimeOfStay(id,timeOfStay);
			}
			else
				GroundReportActivity.changeTimeOfStay(id,-1);
		}
		else if(parent.equals(occSpinner)){
			toOcc=pos;
			Log.v("ELSERVICES",toOcc+" will be added");
			GroundReportActivity.changeOccupant(id, pos);

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
					//					Log.v("ELSERVICES", "start time: "+startTime);
					GroundReportActivity.changeStartTime(id, startTime);
				}
				else if(changeTimeOf=="stop"){
					endTime=c.getTimeInMillis();
					endTimeBtn.setText(timeString);
					//					Log.v("ELSERVICES", "end time: "+endTime);
					GroundReportActivity.changeStopTime(id, endTime);
				}

                if(startTime!=0 && endTime!=0 && ftCheckBox.isChecked()){
                    //Log.v("ELSERVICES","adding new time of stay");
                    timeOfStay=endTime-startTime;
                    GroundReportActivity.changeTimeOfStay(id,timeOfStay);
                }

				Log.v("ELSERVICES",changeTimeOf+ " correction Time: "+timeString);
			} else if (resultCode == Activity.RESULT_CANCELED){

			}
			break;
		}
	}
}
