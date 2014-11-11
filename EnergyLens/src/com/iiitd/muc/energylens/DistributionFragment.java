package com.iiitd.muc.energylens;

import com.iiitd.muc.energylens.R;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DistributionFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View inflateView=inflater.inflate(R.layout.fragment_app_distribution, container, false);
		final String appliance=getArguments().getString("appliance");
		TextView appName=(TextView)inflateView.findViewById(R.id.distName);
		appName.setText(appliance);
		TextView appPercent=(TextView)inflateView.findViewById(R.id.distPercent);
		appPercent.setText(Long.toString(getArguments().getLong("value"))+"Wh ("+Integer.toString(getArguments().getInt("percent"))+"%)");

		if(appliance.equals("Unknown")){
			appName.setTextColor(Color.GRAY);
			appPercent.setTextColor(Color.GRAY);
		}

		final int color;
		final int percent=getArguments().getInt("percent");
		if(percent<10)
			color=Color.argb(195,0, 179,134);
		else if(percent<20)
			color=Color.argb(195,240, 180, 0);
		else
			color=Color.argb(195, 179, 0, 45);

		final ProgressBar progressBar=(ProgressBar)inflateView.findViewById(R.id.distBar);

		// Define a shape with rounded corners
		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));

		// Sets the progressBar color
		if(appliance.equals("Unknown"))
			pgDrawable.getPaint().setColor(Color.DKGRAY);
		else
			pgDrawable.getPaint().setColor(color);

		// Adds the drawable to your progressBar
		ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		progressBar.setProgressDrawable(progress);


		progressBar.setBackgroundColor(Color.LTGRAY);

		progressBar.setProgress(percent);

		//		ImageButton appBtn=(ImageButton)inflateView.findViewById(R.id.imageButton1);

			inflateView.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					Bundle extras=new Bundle();
					Log.v("ELSERVICES", "Distribution "+appliance);
					Intent intent=new Intent(getActivity(),DisaggregationActivity.class);
					extras.putString("appliance", appliance);
					extras.putInt("color", color);
					intent.putExtras(extras);
					if(!appliance.equals("Unknown") &&(Common.CURRENT_VISIBLE==1 ||Common.CURRENT_VISIBLE==0)){
						Log.v("ELSERVICES", "Clicked from tab: "+Common.CURRENT_VISIBLE);
						startActivity(intent);
					}
					
				}
			});			

		return inflateView;
	}

	public static DistributionFragment newInstance(String appliance,long value,int percent) {
		DistributionFragment myFragment = new DistributionFragment();

		Bundle args = new Bundle();
		args.putString("appliance", appliance);
		args.putLong("value", value);
		args.putInt("percent", percent);
		myFragment.setArguments(args);

		return myFragment;
	}

	public void onViewCreated(){

	}

}
