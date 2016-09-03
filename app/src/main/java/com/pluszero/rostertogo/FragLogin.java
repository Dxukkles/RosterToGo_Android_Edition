package com.pluszero.rostertogo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnConnectionListener} interface
 * to handle interaction events.
 * Use the {@link FragLogin#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragLogin extends Fragment {

    private EditText etLogin;
    private EditText etPassword;
    private Button btnOK;
    private TextView tvConnectionStatus;
    private ProgressBar progressBar;

    private OnConnectionListener mListener;

    private SharedPreferences sharedPref;

    public FragLogin() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment
     */
    public static FragLogin newInstance() {
        FragLogin fragment = new FragLogin();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.frag_login, container, false);
        etLogin = (EditText) v.findViewById(R.id.etLogin);
        etPassword = (EditText) v.findViewById(R.id.etPassword);

        btnOK = (Button) v.findViewById(R.id.btnOk);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConnectionClick(etLogin.getText().toString(), etPassword.getText().toString());
                }
            }
        });

        tvConnectionStatus = (TextView) v.findViewById(R.id.tvStatus);
        progressBar = (ProgressBar) v.findViewById(R.id.pbStatus);
        return v;
    }

    /*
    * onAttach(Context) is not called on pre API 23 versions of Android and onAttach(Activity) is deprecated
    * Use onAttachToContext instead
    */
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConnectionListener) {
            mListener = (OnConnectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentLoginListener");
        }
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (activity instanceof OnConnectionListener) {
                mListener = (OnConnectionListener) activity;
            } else {
                throw new RuntimeException(activity.toString()
                        + " must implement OnFragmentLoginListener");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public String getLogin() {
        return etLogin.getText().toString();
    }

    public String getPasscode() {
        return etPassword.getText().toString();
    }

    public void setConnectionStatus(String status) {
        this.tvConnectionStatus.setText(status);
    }

    public Button getBtnOK() {
        return btnOK;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    @Override
    public void onPause() {
        super.onPause();
        // save login/password to preferences according to user's wish
        SharedPreferences.Editor editor = sharedPref.edit();

        boolean remember = sharedPref.getBoolean(FragSettings.KEY_PREF_LOGIN_REMEMBER, false);
        if (remember) {
            editor.putString(FragSettings.KEY_PREF_LOGIN_VALUE, etLogin.getText().toString());
            editor.putString(FragSettings.KEY_PREF_PASSWORD_VALUE, etPassword.getText().toString());
            editor.commit();
        } else {
            editor.putString(FragSettings.KEY_PREF_LOGIN_VALUE, "");
            editor.putString(FragSettings.KEY_PREF_PASSWORD_VALUE, "");
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // load login/password values if saved
        etLogin.setText(sharedPref.getString(FragSettings.KEY_PREF_LOGIN_VALUE, ""));
        etPassword.setText(sharedPref.getString(FragSettings.KEY_PREF_PASSWORD_VALUE, ""));
    }
}
