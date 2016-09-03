/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pluszero.rostertogo;

import com.pluszero.rostertogo.model.MonthActivity;
import com.pluszero.rostertogo.model.PlanningEvent;
import com.pluszero.rostertogo.model.PlanningModel;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author Cyril
 */
public class ActivityFigures {

    private final ArrayList<MonthActivity> alMonths;
    private final PlanningModel model;

    public ActivityFigures(PlanningModel model) {
        this.model = model;
        alMonths = buildMonthsList();
        computeMonthHours();
    }

    /**
     * scann list of event and determine the different months involved
     */
    private ArrayList<MonthActivity> buildMonthsList() {
        boolean firstEvent = true;   // for the first event
        MonthActivity previous;
        ArrayList<MonthActivity> arrayList = new ArrayList<>();

        for (PlanningEvent event : model.getAlEvents()) {
            if (firstEvent) {
                arrayList.add(new MonthActivity(event.getGcBegin()));
                firstEvent = false;
                continue;
            }

            previous = arrayList.get(arrayList.size() - 1);
            if (event.getGcBegin().get(Calendar.MONTH) != previous.getCalStart().get(Calendar.MONTH)) {
                // set end of previous month (a day before new month)
                previous.getCalEnd().setTime(event.getGcBegin().getTime());
                previous.getCalEnd().add(Calendar.DAY_OF_MONTH, -1);
                arrayList.add(new MonthActivity(event.getGcBegin()));
            }

            // update the current monthactivity last day
            arrayList.get(arrayList.size() - 1).getCalEnd().setTime(event.getGcBegin().getTime());

        }
        return arrayList;
    }

    private void computeMonthHours() {
        // loop through all active months
        for (MonthActivity ma : alMonths) {
            int minutes = 0;
            //loop through events, deal with corresponding months only
            for (PlanningEvent pe : model.getAlEvents()) {
                if (pe.getGcBegin().get(Calendar.MONTH) == ma.getCalStart().get(Calendar.MONTH)) {
                    if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
                        minutes += pe.getBlockTime();
                    }
                }
            }
            ma.setBlocHours(minutes);
        }
    }

    private int computeTotalHours() {
        int total = 0; // in minutes
        for (PlanningEvent pe : model.getAlEvents()) {
            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
                long time = pe.getGcEnd().getTimeInMillis() - pe.getGcBegin().getTimeInMillis();
                total += time / 60000;
            }
        }
        return total;
    }

    public ArrayList<MonthActivity> getAlMonths() {
        return alMonths;
    }

    public String buildHoursSheet() {
        DateFormatSymbols dfs = new DateFormatSymbols();
        final String[] shortMonths = new String[]{"Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jul", "Aou", "Sep", "Nov", "Déc"};
        dfs.setShortMonths(shortMonths);
        final SimpleDateFormat sdfDay = new SimpleDateFormat("dd", dfs);
        final SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM yyyy", dfs);

        StringBuilder sb = new StringBuilder();
        // total hours

        sb.append(model.getPlanningFirstAndLastDatesAsString());
        sb.append(System.getProperty("line.separator"));
        sb.append("Heures bloc : ");
        sb.append(Utils.convertMinutesToHoursMinutes(computeTotalHours()));
        sb.append(System.getProperty("line.separator"));
        sb.append(System.getProperty("line.separator"));

        // for each month
        for (MonthActivity ma : model.getActivityFigures().getAlMonths()) {
            sb.append(sdfMonth.format(ma.getCalStart().getTime()));
            sb.append(" (du ").append(sdfDay.format(ma.getCalStart().getTime()));
            sb.append(" au ").append(sdfDay.format(ma.getCalEnd().getTime()));
            sb.append(")");
            sb.append(System.getProperty("line.separator"));
            sb.append("Heures bloc : ");
            sb.append(Utils.convertMinutesToHoursMinutes(ma.getBlocHours()));
            sb.append(System.getProperty("line.separator"));
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

}
