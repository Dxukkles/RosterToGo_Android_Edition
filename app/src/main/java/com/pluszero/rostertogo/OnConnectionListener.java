package com.pluszero.rostertogo;

/**
 * Created by Cyril on 03/05/2016.
 */
public interface OnConnectionListener {
    public void onConnectionUpdated(String... messages);

    public void onConnectionCompleted(int value);

    public void onConnectionClick(String login, String password);
}
