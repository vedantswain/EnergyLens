package com.example.energylens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.energylens.AppLocDialogFragment.LocationDialogListener;

public class WifiListDialogFragment extends DialogFragment {
	private String[] wifiList={"No Wifi available"};

	public interface WifiListDialogListener {
		public void onWifiSelected(String ssid,int index);
	}

	WifiListDialogListener mListener;

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (WifiListDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement WifiListDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if(GCMActivity.apList!=null)
			wifiList=GCMActivity.apList.toArray(wifiList);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_locations)
		.setItems(wifiList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// The 'which' argument contains the index position
				// of the selected item
				mListener.onWifiSelected(wifiList[which],which);
			}
		}
				);
		return builder.create();
	}
}