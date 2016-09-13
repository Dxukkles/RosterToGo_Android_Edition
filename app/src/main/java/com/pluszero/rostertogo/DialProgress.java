package com.pluszero.rostertogo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Cyril on 13/09/2016.
 */
public class DialProgress extends DialogFragment {

    public static final String TAG_PLANNING_GENERATION = "planning_generation";
    public static final String TAG_PLANNING_SYNCHRONISATION = "planning_synchronisation";
    private String message;


    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static DialProgress newInstance(String message) {
        DialProgress dp = new DialProgress();

        // Supply title and message strings as arguments.
        Bundle args = new Bundle();
        args.putString("message", message);
        dp.setArguments(args);
        return dp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        message = getArguments().getString("message");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.dial_progress, null);
        TextView tvMessage = (TextView) v.findViewById(R.id.tvMessage);
        tvMessage.setText(message);
        // Inflate and set the layout for the dialog
        builder.setView(v);
        return builder.create();
    }
}
