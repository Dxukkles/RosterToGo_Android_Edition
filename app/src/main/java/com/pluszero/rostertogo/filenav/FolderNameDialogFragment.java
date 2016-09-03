package com.pluszero.rostertogo.filenav;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.pluszero.rostertogo.R;


public class FolderNameDialogFragment extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());

        builder.setMessage(R.string.enter_new_folder_name)
                .setView(
                        inflater.inflate(R.layout.dialogfrag_new_folder_name,
                                null))
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent i = getActivity().getIntent();
                                i.putExtra("folder_name", input.getText().toString());
                                getTargetFragment().onActivityResult(getTargetRequestCode(), FragFilenav.RESULT_OK, i);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Send the negative button event back to the
                                // host activity
                                dismiss();
                            }
                        });
        return builder.create();
    }
}