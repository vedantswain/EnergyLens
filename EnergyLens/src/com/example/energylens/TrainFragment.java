package com.example.energylens;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class TrainFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_train, container, false);
         
        return rootView;
    }
	
	public void onViewCreated(View view, Bundle savedInstanceState){
		if(Common.TRAINING_COUNT>0){
			TextView changeTxt=(TextView) getActivity().findViewById(R.id.alreadyText);
			Log.i("ELSERVICES","View Id: "+Integer.toString(changeTxt.getId()));
			changeTxt.setVisibility(View.GONE);
			Button btn=(Button) getActivity().findViewById(R.id.done);
			btn.setVisibility(View.GONE);
			btn=(Button) getActivity().findViewById(R.id.notYet);
			btn.setVisibility(View.GONE);
		}		
	}

	public void start(){
		Log.v("ELSERVICES","it works");
	}
}
