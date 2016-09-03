package com.pluszero.rostertogo.filenav;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pluszero.rostertogo.ActivMain;
import com.pluszero.rostertogo.R;

import java.io.File;


public class FragFilenav extends Fragment {

    public static final int FILE_PICKED = 1;
    public FileNavigator fileNav;
    private FileNavAdapter adapter;
    private TextView tvPath;
    private ListView listView;
    private OnFileNavEventListener mListener;
    private EditText etFilename;
    private boolean loadMode = true;    // to discriminate between load and save

    public FragFilenav() {
        fileNav = new FileNavigator();
    }

    public static FragFilenav newInstance(String filename, boolean loadMode) {
        FragFilenav f = new FragFilenav();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("filename", filename);
        args.putBoolean("load_mode", loadMode);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // to override activity's action bar menu
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String filename = null;
        if (getArguments() != null) {
            filename = getArguments().getString("filename");
            loadMode = getArguments().getBoolean("load_mode");
        }
        View v = inflater.inflate(R.layout.frag_filenav, container, false);
        LinearLayout layout = (LinearLayout) v
                .findViewById(R.id.linlayFileName);
        if (this.getTag().equals(ActivMain.FRAG_LOAD)) {
            layout.setVisibility(View.GONE);
        } else {
            etFilename = (EditText) v.findViewById(R.id.etFileName);
            etFilename.setText(filename);
        }

        setupListView(v);

        updateList();
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFileNavEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFilenavItemListener");
        }
    }

    private void setupListView(View v) {
        tvPath = (TextView) v.findViewById(R.id.tvPath);
        listView = (ListView) v.findViewById(R.id.listview);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int result = fileNav.navigateDown(position);
                if (result == FileNavigator.MSG_NOT_DIRECTORY) {
                    File file = fileNav.getListFiles().get(position);
                    if (loadMode) {
                        mListener.onFilenavItemSelected(file);
                    }
                }
                updateList();
            }
        });
    }

    public void updateList() {
        adapter = new FileNavAdapter(getActivity(), R.layout.filenav_list_row, fileNav.getListFiles());
        if (fileNav.getActualFile() != null)
            tvPath.setText(fileNav.getActualFile().getAbsolutePath());
        listView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (this.getTag().equals(ActivMain.FRAG_SAVE))
            inflater.inflate(R.menu.filenav_actions_save, menu);
        else
            inflater.inflate(R.menu.filenav_actions_load, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_back:
                fileNav.navigateUp();
                updateList();
                return true;

            case R.id.action_save:
                // write the file
                mListener.onFileNavSave(fileNav.getActualFile()
                        .getAbsolutePath(), etFilename.getText().toString());

                return true;

            case R.id.action_new_folder:
                showFolderNameDialogFragment();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Dialogs handling
    public void showFolderNameDialogFragment() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new FolderNameDialogFragment();
        dialog.show(getFragmentManager(), "FolderNameDialogFragment");
    }
}
