package com.iiitd.muc.energylens;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerDialogFragment extends DialogFragment
implements TimePickerDialog.OnTimeSetListener {
	
	public long initTime=0;

	public interface TimePickerDialogListener {
		public void onSetTime(int hourOfDay, int minute);
	}

	TimePickerDialogListener mListener;

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the TimePickerDialogListener so we can send events to the host
			mListener = (TimePickerDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement TimePickerDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
		final Calendar c = Calendar.getInstance();
		if(initTime!=0)
			c.setTimeInMillis(initTime);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hour, minute,
				DateFormat.is24HourFormat(getActivity()));
	}
	
	public void changeInitTime(long time){
		initTime=time;
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Do something with the time chosen by the user
		Intent i = new Intent();
		Bundle extras=new Bundle();       
		extras.putInt("hourOfDay",hourOfDay);
		extras.putInt("minute",minute);
		i.putExtras(extras);
		if(getTargetFragment()!=null)
			getTargetFragment().onActivityResult(getTargetRequestCode(),Activity.RESULT_OK,i);
		mListener.onSetTime(hourOfDay, minute);
		dismiss() ;
	}

}
