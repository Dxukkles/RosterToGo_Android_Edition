package com.pluszero.rostertogo;

/**
 * Created by Cyril on 03/05/2016.
 */
public interface OnSynchronisationListener {
    public void onSynchronisationProgress(String... messages);

    public void onSynchronisationCompleted(int value);

}
