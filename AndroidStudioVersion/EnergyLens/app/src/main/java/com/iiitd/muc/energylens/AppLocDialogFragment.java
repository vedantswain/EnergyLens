package com.iiitd.muc.energylens;

import com.iiitd.muc.energylens.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AppLocDialogFragment extends DialogFragment {
	private String[] locations={"Kitchen","Dining Room","Bedroom1","Bedroom2","Bedroom3","Study","Corridor"};
	
	 public interface LocationDialogListener {
	        public void onLocSelected(String loc,int index);
	    }
	
	 LocationDialogListener mListener;
	 
	 public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        // Verify that the host activity implements the callback interface
	        try {
	            // Instantiate the NoticeDialogListener so we can send events to the host
	            mListener = (LocationDialogListener) activity;
	        } catch (ClassCastException e) {
	            // The activity doesn't implement the interface, throw exception
	            throw new ClassCastException(activity.toString()
	                    + " must implement LocationDialogListener");
	        }
	    }
	 
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if(Common.ACTIVITY_LOCS!=null)
			locations=Common.ACTIVITY_LOCS;
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.dialog_locations)
	           .setItems(locations, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   mListener.onLocSelected(locations[which],which);
	           }
	    });
	    return builder.create();
	}
}
