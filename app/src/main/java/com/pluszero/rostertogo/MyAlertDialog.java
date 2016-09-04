package com.pluszero.rostertogo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Cyril on 04/09/2016.
 */
public class MyAlertDialog extends DialogFragment {

    private String title, message;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static MyAlertDialog newInstance(String title, String message) {
        MyAlertDialog f = new MyAlertDialog();

        // Supply title and message strings as arguments.
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString("title");
        message = getArguments().getString("message");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}