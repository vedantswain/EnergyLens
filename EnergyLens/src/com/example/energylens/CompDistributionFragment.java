package com.example.energylens;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CompDistributionFragment extends Fragment {
	
	 int[] red={0,102,153,204,0,10,71,204,0,255,255,204,0,0,102,204};
	 int[] green={0,0,0,0,102,10,71,0,204,255,255,102,204,204,204,204};
	 int[] blue={204,204,153,102,204,255,255,0,204,71,10,0,102,0,0,0};
	 
	 int[]	textId={R.id.segText1,R.id.segText2,R.id.segText3,R.id.segText4,R.id.segText5};
	 int[]	segId={R.id.seg1,R.id.seg2,R.id.seg3,R.id.seg4,R.id.seg5};
	 int fullWidth;	
 	
	 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View inflateView=inflater.inflate(R.layout.fragment_distribution, container, false);
       
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        
        float fullWidth = displayMetrics.widthPixels / displayMetrics.density;
        
        fullWidth-=40;
        
    	float[] distribution=getArguments().getFloatArray("distribution");
    	String applianceName=getArguments().getString("appliance");
    	TextView appName=(TextView)inflateView.findViewById(R.id.appName);
    	appName.setText(applianceName);
    	
    	
    	for(int i=0;i<distribution.length;i++){
    		View segShape=inflateView.findViewById(segId[i]);
    		float percentWidth=(float) (fullWidth*distribution[i]*0.01);
    		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) segShape.getLayoutParams();
    		params.width=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, percentWidth, getResources().getDisplayMetrics());
    		params.height=18;
    		
    		Drawable segShapeBG=(Drawable)segShape.getBackground();
    		segShapeBG.setColorFilter(Color.argb(191, red[i], green[i], blue[i]),Mode.MULTIPLY);
    		
    		
    		TextView percentage=(TextView)inflateView.findViewById(textId[i]);
    		int textSize=(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, percentage.getTextSize(), getResources().getDisplayMetrics());
    		
    		if((int)percentWidth<textSize)
     			percentage.setVisibility(View.INVISIBLE);
     		else
     			percentage.setText(Double.toString(distribution[i])+"%");
    		
     		
//    		Log.v("ELSERVICES", "Dimensions: "+params.width+"x"+params.height);
//    		Log.v("ELSERVICES", "new width"+percentWidth+" full width"+fullWidth);
    		segShape.setLayoutParams(params);
    	}
    	
    	for(int i=distribution.length;i<segId.length;i++){
    		View segShape=inflateView.findViewById(segId[i]);
    		segShape.setVisibility(View.GONE);
    		TextView percentage=(TextView)inflateView.findViewById(textId[i]);
    		percentage.setVisibility(View.GONE);
    	}

    	return inflateView;
    }
    
    public static CompDistributionFragment newInstance(String appliance,float [] distribution) {
        CompDistributionFragment myFragment = new CompDistributionFragment();

        Bundle args = new Bundle();
        args.putString("appliance", appliance);
        args.putFloatArray("distribution", distribution);
        myFragment.setArguments(args);

        return myFragment;
    }
    
    public void onViewCreated(){
    	
    }
    
}

