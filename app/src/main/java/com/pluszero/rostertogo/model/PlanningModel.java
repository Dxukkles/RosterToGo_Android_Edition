/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pluszero.rostertogo.model;

import com.pluszero.rostertogo.ActivityFigures;
import com.pluszero.rostertogo.DateComparator;
import com.pluszero.rostertogo.PdfManager;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

/**
 *
 * @author Cyril
 */
public class PlanningModel {

    private final static int ONE_HOURS = 3600000;
    private static final String JOUR_BLANC = "BLANC";
    private static final String TIMEZONE_PARIS = "Europe/Paris";

    public boolean modeOnline = true;
    private ArrayList<PlanningEvent> alEvents;
    private ActivityFigures activityFigures;
    private String userTrigraph;    //userTrigraph of crewmember

    public PlanningModel() {
        alEvents = new ArrayList<>();
    }

    public ArrayList<PlanningEvent> getAlEvents() {
        return alEvents;
    }

    /*
    Deal with splitted "jour off" or "malade" or "Congés" (2300Z->2359Z & 0000Z->2259Z) by removing 
    any event which has "jour off" in summary and whose duration is < 2h
     */
    public void fixSplittedActivities() {
        HashSet<PlanningEvent> hashSet = new HashSet<>();

        for (PlanningEvent pe : alEvents) {
            if (pe.isDayEvent()) {
                long diff = pe.getGcEnd().getTimeInMillis() - pe.getGcBegin().getTimeInMillis();
                if (diff > 0 && diff <= ONE_HOURS * 2) {
                    continue;
                }
                //set same begin and end time for all day events
                pe.getGcBegin().set(Calendar.HOUR_OF_DAY, 10);
                pe.getGcBegin().set(Calendar.MINUTE, 0);
                pe.getGcBegin().set(Calendar.SECOND, 0);
                pe.getGcBegin().set(Calendar.MILLISECOND, 0);

                pe.getGcEnd().set(Calendar.HOUR_OF_DAY, 10);
                pe.getGcEnd().set(Calendar.MINUTE, 0);
                pe.getGcEnd().set(Calendar.SECOND, 0);
                pe.getGcEnd().set(Calendar.MILLISECOND, 0);
            }
            hashSet.add(pe);
        }
        // ensure no duplicate and sorted by date
        alEvents = new ArrayList<>(hashSet);
        Collections.sort(alEvents, new DateComparator());
    }

    public void addJoursBlanc() {
        GregorianCalendar cal = new GregorianCalendar(); // init at today, 12h00 loc
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.setTimeZone(TimeZone.getTimeZone(TIMEZONE_PARIS));
        // planning is set each friday for 4 weeks + 3 days
        int span = findMaxPlanningDays(cal); // planning is set each friday for 4 weeks + 3 days
        //TODO: fix span when offline (presently bugs...)

        for (int i = 0; i < span; i++) {
            boolean flagJourBlanc = true;
            for (PlanningEvent pe : alEvents) {
                GregorianCalendar gce = new GregorianCalendar();
                gce.setTimeZone(TimeZone.getTimeZone(TIMEZONE_PARIS));
                gce.setTime(pe.getGcBegin().getTime());
                // test on DAY_OF_YEAR to avoid error from one month to another
                if (gce.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)) {
                    flagJourBlanc = false;
                    break;
                }
            }
            if (flagJourBlanc) {
                PlanningEvent event = new PlanningEvent();
                //set same begin and end time for all day events
                event.setGcBegin((GregorianCalendar) cal.clone());
                event.setGcEnd((GregorianCalendar) cal.clone());
                event.setLabel(JOUR_BLANC);
                event.setCategory(PlanningEvent.CAT_BLANC);
                alEvents.add(event);
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        Collections.sort(alEvents, new DateComparator());
    }

    /*
    determine number of days of visiblity, knowing that the planning is set 
    every friday for the upcoming 4 weeks + 3 days
     */
    public int findMaxPlanningDays(GregorianCalendar cal) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.FRIDAY:
                return 32;
            case Calendar.SATURDAY:
                return 31;
            case Calendar.SUNDAY:
                return 30;
            case Calendar.MONDAY:
                return 29;
            case Calendar.TUESDAY:
                return 28;
            case Calendar.WEDNESDAY:
                return 27;
            case Calendar.THURSDAY:
                return 26;
            default:
                return 32;
        }
    }

    /**
     * scan the arrayList and detect all events that are first of the day
     */
    public void factorizeDays() {
        PlanningEvent previous = null;
        for (PlanningEvent pe : alEvents) {
            // for the first item in list
            if (previous == null) {
                previous = pe;
                pe.setFirstEventOfDay(true);
            } else if (previous.getGcBegin().get(Calendar.DAY_OF_MONTH) != pe.getGcEnd().get(Calendar.DAY_OF_MONTH)) {
                pe.setFirstEventOfDay(true);
                previous = pe;
            }
        }
    }

    /**
     * copy crew from first leg to the other legs
     */
    public void copyCrew() {
        String crew = "";
        for (PlanningEvent pe : alEvents) {
            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
                if (!pe.getCrew().equals("")) {
                    crew = pe.getCrew();
                } else {
                    pe.setCrew(crew);
                }
            }
        }
    }

    // KEEP FOR OFFLINE TESTING
//    public void addDataFromPDF(HashMap<String, String> trigraphs) {
//        File pdfFile = new java.io.File(System.getProperty("user.home"), ".RosterToGo/planning.pdf");
//        PdfManager manager = new PdfManager(pdfFile, trigraphs);
//
//        for (PlanningEvent pe : alEvents) {
//            // Crew (when crew not present in ics file)
//            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT) && pe.getCrew().equals("")) {
//                String data = manager.findCrew(pe.getGcBegin());
//                if (data != null) {
//                    pe.setCrew(data);
//                }
//            }
//            // hotel data
//            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT) || pe.getCategory().equals(PlanningEvent.CAT_DEAD_HEAD)) {
//                String data = manager.findHotelDetails(pe.getGcBegin());
//                if (data != null) {
//                    pe.setHotelData(data);
//                }
//            }
//
//            // flight remarks
//            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
//                String data = manager.findRemarks(
//                        pe.getGcBegin(),
//                        pe.getIataOrig(),
//                        pe.getIataDest());
//                if (data != null) {
//                    pe.setRemark(data);
//                }
//            }
//
//            if (pe.isSimActivity()) {
//                String data = manager.findTraining(pe.getGcBegin());
//                if (data != null) {
//                    pe.setCrew(data);
//                }
//
//                data = manager.findRemarks(pe.getGcBegin());
//                if (data != null) {
//                    pe.setRemark(data);
//                }
//            }
//        }
//
//    }

    public void addDataFromPDF(InputStream is, HashMap<String, String> trigraphs) {
        PdfManager manager = new PdfManager(is, trigraphs);

        for (PlanningEvent pe : alEvents) {
            // Crew (when crew not present in ics file)
            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT) && pe.getCrew().equals("")) {
                String data = manager.findCrew(pe.getGcBegin());
                if (data != null) {
                    pe.setCrew(data);
                }
            }

            // hotel data
            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT) || pe.getCategory().equals(PlanningEvent.CAT_DEAD_HEAD)) {
                String data = manager.findHotelDetails(pe.getGcBegin());
                if (data != null) {
                    pe.setHotelData(data);
                }
            }
            // remarks
            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
                String data = manager.findRemarks(
                        pe.getGcBegin(),
                        pe.getIataOrig(),
                        pe.getIataDest());
                if (data != null) {
                    pe.setRemark(data);
                }
            }

            if (pe.isSimActivity()) {
                String data = manager.findTraining(pe.getGcBegin());
                if (data != null) {
                    pe.setTraining(data);
                }

                data = manager.findRemarks(pe.getGcBegin());
                if (data != null) {
                    pe.setRemark(data);
                }
            }
        }
    }

    /**
     * determine the first and last dates of planning (dd/MM/yyyy)
     *
     * @return a string containing first and last days
     */
    public String getPlanningFirstAndLastDatesAsString() {
        Date d1 = alEvents.get(0).getGcBegin().getTime();
        Date d2 = alEvents.get(alEvents.size() - 1).getGcEnd().getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(d1) + " au " + sdf.format(d2);
    }

    public String getUserTrigraph() {
        return userTrigraph;
    }

    public void setUserTrigraph(String userTrigraph) {
        this.userTrigraph = userTrigraph;
    }

    public void computeActivityFigures() {
        activityFigures = new ActivityFigures(this);
    }

    public void sortByAscendingDate() {
        Collections.sort(alEvents, new DateComparator());
    }

    public ActivityFigures getActivityFigures() {
        return activityFigures;
    }
}
