package com.iiitd.muc.energylens;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by vedantdasswain on 11/01/15.
 */
public class ManualOverrideDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.action_manual_override)
                .setItems(R.array.string_array_manual_override, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                toggleServiceMessage("Force startServices");
                                break;
                            case 1:
                                toggleServiceMessage("Force stopServices");
                                break;
                            default:
                                break;
                        }
                    }
                });
        return builder.create();
    }

    private void toggleServiceMessage(String message){
        Intent intent = new Intent();
        intent.setAction("EnergyLensPlus.toggleService");
        // add data
        intent.putExtra("message", message);

        Log.v("ELSERVICES", "Broadcast from Manual to Main receiver");
        getActivity().sendBroadcast(intent);
    }
}
