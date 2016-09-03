/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pluszero.rostertogo.model;

import java.util.GregorianCalendar;

/**
 *
 * @author Cyril
 */
public class MonthActivity {

    private int blocHours;  // actually minutes
    private GregorianCalendar calStart;    // earliest event in month
    private GregorianCalendar calEnd;      // latest event in month

    public MonthActivity() {
    }

    public MonthActivity(GregorianCalendar start) {
        calStart = new GregorianCalendar();
        calStart.setTime(start.getTime());
        calEnd = new GregorianCalendar();
        calEnd.setTime(start.getTime());
    }

    public int getBlocHours() {
        return blocHours;
    }

    public void setBlocHours(int blocHours) {
        this.blocHours = blocHours;
    }

    public GregorianCalendar getCalStart() {
        return calStart;
    }

    public GregorianCalendar getCalEnd() {
        return calEnd;
    }
}
