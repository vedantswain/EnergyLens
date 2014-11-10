package com.iiitd.muc.energylens;

import com.iiitd.muc.energylens.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;



public class ApplianceDialogFragment extends DialogFragment {
	private String[] labels={"Fan","AC","Microwave","TV","Computer","Printer","Washing Machine","Fan+AC"};
	
	 public interface ApplianceDialogListener {
	        public void onAppSelected(String label);
	    }
	
	 ApplianceDialogListener mListener;
	 
	 public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        // Verify that the host activity implements the callback interface
	        try {
	            // Instantiate the NoticeDialogListener so we can send events to the host
	            mListener = (ApplianceDialogListener) activity;
	        } catch (ClassCastException e) {
	            // The activity doesn't implement the interface, throw exception
	            throw new ClassCastException(activity.toString()
	                    + " must implement ApplianceDialogListener");
	        }
	    }
	 
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if(Common.ACTIVITY_APPS!=null)
			labels=Common.ACTIVITY_APPS;
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.dialog_appliances)
	           .setItems(labels, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	               // The 'which' argument contains the index position
	               // of the selected item
	            	   Common.changeLabel(labels[which]);
	            	   mListener.onAppSelected(labels[which]);
	           }
	    });
	    return builder.create();
	}
	
}
