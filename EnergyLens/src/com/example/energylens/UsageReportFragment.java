package com.example.energylens;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
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
	private Spinner appSpinner;
	private Spinner locSpinner;	
	private ImageView appIcon,locIcon;
	private String toApp="none",toLoc="none";

	int wasClicked=-1;
	
	long id;
	String[] pair={"",""};
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
				+" in "+loc+" consumed: "+Long.toString(usage)+" kWh";
		activityText.setText(activityLine);

		getUpdatedPreferences();
		setAppSpinner();
		setLocSpinner();


		RadioButton isCorrect=(RadioButton) inflateView.findViewById(R.id.radio_correct);
		isCorrect.setOnClickListener(new OnClickListener(){
//			@Override
			public void onClick(View view) {
		    // Is the button now checked?
		    boolean checked = ((RadioButton) view).isChecked();
		    
		    // Check which radio button was clicked
		            if (checked){
					appSpinner.setVisibility(View.INVISIBLE);
					appSpinner.setSelection(0);
					appIcon.setVisibility(View.INVISIBLE);
					locSpinner.setVisibility(View.INVISIBLE);
					locSpinner.setSelection(0);
					locIcon.setVisibility(View.INVISIBLE);
					GroundReportActivity.changeCorrectionIds(id,pair, 0);
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
					appSpinner.setVisibility(View.VISIBLE);
					locSpinner.setVisibility(View.VISIBLE);
					appIcon.setVisibility(View.VISIBLE);
					locIcon.setVisibility(View.VISIBLE);
					
					if(wasClicked==0)
						GroundReportActivity.changeCorrectionIds(id,pair, 2);
					else
						GroundReportActivity.changeCorrectionIds(id,pair, 1);
					
					wasClicked=1;
		    		}
			}
		});

		return inflateView;
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

	private void setAppSpinner(){
		appSpinner = (Spinner) inflateView.findViewById(R.id.app_spinner);
		for(String app:labels)
			labelsList.add(app);
		labelsList.remove(0);
		labelsList.add("Unknown");
		labelsList.add(0, "none");
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, labelsList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		appSpinner.setAdapter(dataAdapter);
		appSpinner.setOnItemSelectedListener(this);
		appSpinner.setVisibility(View.INVISIBLE);
		appIcon = (ImageView) inflateView.findViewById(R.id.appIcon);
		appIcon.setVisibility(View.INVISIBLE);
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
		locSpinner.setVisibility(View.INVISIBLE);
		locIcon = (ImageView) inflateView.findViewById(R.id.locIcon);
		locIcon.setVisibility(View.INVISIBLE);
	}

	public static UsageReportFragment newInstance(long id,String appliance,long use,String loc,long from, long to) {
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
				GroundReportActivity.changeCorrectionPairData(id,pair, 1);
			}
		}
		else if(parent.equals(locSpinner)){
			toLoc=locList.get(pos);
			Log.v("ELSERVICES", toApp+","+toLoc+" will be added");
			if(!toApp.equals("none") && !toLoc.equals("none")){
				Log.v("ELSERVICES", toApp+","+toLoc+" will be added");
				String[] pair={toApp,toLoc};
				GroundReportActivity.changeCorrectionPairData(id,pair, 1);
			}
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}


}
