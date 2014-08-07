package com.example.energylens;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CompDistributionFragment extends Fragment {
	
	 int[] red={0,102,153,204,0,10,71,204,0,255,255,204,0,0,102,204};
	 int[] green={0,0,0,0,102,10,71,0,204,255,255,102,204,204,204,204};
	 int[] blue={204,204,153,102,204,255,255,0,204,71,10,0,102,0,0,0};
	 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflateView=inflater.inflate(R.layout.fragment_distribution, container, false);
        TextView appName=(TextView)inflateView.findViewById(R.id.distName);
    	appName.setText(getArguments().getString("user"));
    	TextView appPercent=(TextView)inflateView.findViewById(R.id.distPercent);
    	appPercent.setText(Integer.toString(getArguments().getInt("percent"))+"%");
    	
    	int color;
    	int percent=getArguments().getInt("percent");
    	int i=getArguments().getInt("index");
    	color=Color.rgb(red[i],green[i],blue[i]);
    	
//    	appPercent.setTextColor(color);
    	
    	ProgressBar progressBar=(ProgressBar)inflateView.findViewById(R.id.distBar);

    	// Define a shape with rounded corners
        final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
        ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners,     null, null));

        // Sets the progressBar color
        pgDrawable.getPaint().setColor(color);

        // Adds the drawable to your progressBar
        ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        progressBar.setProgressDrawable(progress);

        
        progressBar.setBackgroundColor(Color.LTGRAY);
    	
        progressBar.setProgress(percent);
    	
    	return inflateView;
    }
    
    public static CompDistributionFragment newInstance(String appliance,int percent,int index) {
        CompDistributionFragment myFragment = new CompDistributionFragment();

        Bundle args = new Bundle();
        args.putString("user", appliance);
        args.putInt("percent", percent);
        args.putInt("index",index);
        myFragment.setArguments(args);

        return myFragment;
    }
    
    public void onViewCreated(){
    	
    }
    
}

