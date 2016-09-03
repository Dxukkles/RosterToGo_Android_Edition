package com.pluszero.rostertogo.filenav;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.pluszero.rostertogo.R;


public class FolderNameDialogFragment extends DialogFragment {

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
//	public interface FolderNameDialogListener {
//		public void onDialogPositiveClick(DialogFragment dialog);
//
//		public void onDialogNegativeClick(DialogFragment dialog);
//	}

	// Use this instance of the interface to deliver action events
	OnFileNavEventListener mListener;

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the FileNavEventListener so we can send events to the
			// host
			mListener = (OnFileNavEventListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement FolderNameDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage(R.string.enter_new_folder_name)
				.setView(
						inflater.inflate(R.layout.dialogfrag_new_folder_name,
								null))
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Send the positive button event back to the
								// host activity
								mListener
										.onNewFolderDialogPositiveClick(FolderNameDialogFragment.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Send the negative button event back to the
								// host activity
								mListener
										.onNewFolderDialogNegativeClick(FolderNameDialogFragment.this);
							}
						});
		return builder.create();
	}
}