package com.example.energylens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.energylens.AddOtherDialogFragment.AddOtherDialogListener;

public class AddOtherLocDialogFragment extends DialogFragment {
	
	public interface AddOtherDialogListener {
	        public void onOtherLocSelected(String location);
	    }
	
	 AddOtherDialogListener mListener;
	 
	 public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        // Verify that the host activity implements the callback interface
	        try {
	            // Instantiate the NoticeDialogListener so we can send events to the host
	            mListener = (AddOtherDialogListener) activity;
	        } catch (ClassCastException e) {
	            // The activity doesn't implement the interface, throw exception
	            throw new ClassCastException(activity.toString()
	                    + " must implement ApplianceDialogListener");
	        }
	    }
	 
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		 // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    final View inflateView=inflater.inflate(R.layout.fragment_addother, null);
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflateView)
	    // Add action buttons
	           .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   EditText other=(EditText)inflateView.findViewById(R.id.editAddOther);
	            	   String otherString=other.getText().toString();
	                   mListener.onOtherLocSelected(otherString);
	               }
	           })
	           .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   AddOtherLocDialogFragment.this.getDialog().cancel();
	               }
	           });      
	    return builder.create();
	}
}
