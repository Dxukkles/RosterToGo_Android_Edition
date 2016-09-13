/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pluszero.rostertogo;

/**
 * @author Cyril
 */
public interface OnPlanningGeneratorListener {
    public void onPlanningGeneratorProgress(String... messages);

    public void onPlanningGeneratorCompleted(int value);
}
