/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pluszero.rostertogo;

/**
 * @author Cyril
 */
public interface OnPdfManagerListener {
    public void onPdfManagerProgress(String... messages);

    public void onPdfManagerCompleted(int value);
}
