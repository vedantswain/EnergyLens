package com.example.energylens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.energylens.TryAgainDialogFragment.TryAgainDialogListener;

public class TryAgainConnectionRefusedDialogFragment  extends DialogFragment {
			
		 public interface TryAgainDialogListener {
		        public void onOk();
		        public void onCancelNow();
		    }
		
		 TryAgainDialogListener mListener;
		 
		 public void onAttach(Activity activity) {
		        super.onAttach(activity);
		        // Verify that the host activity implements the callback interface
		        try {
		            // Instantiate the NoticeDialogListener so we can send events to the host
		            mListener = (TryAgainDialogListener) activity;
		        } catch (ClassCastException e) {
		            // The activity doesn't implement the interface, throw exception
		            throw new ClassCastException(activity.toString()
		                    + " must implement TryAgainDialogListener");
		        }
		    }
		 
		
		public Dialog onCreateDialog(Bundle savedInstanceState) {
	       // Use the Builder class for convenient dialog construction
	       AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	       builder.setMessage(R.string.dialog_refused_try_again)
	              .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                  public void onClick(DialogInterface dialog, int id) {
	               	   mListener.onOk();
	                      
	                  }
	              })
	              .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                  public void onClick(DialogInterface dialog, int id) {
	                      // User cancelled the dialog
	               	   mListener.onCancelNow();
	                  }
	              });
	       // Create the AlertDialog object and return it
	       return builder.create();
	   }

}
