package com.pluszero.rostertogo.model;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cyril
 */
public class PlanningEvent {

    public static final int NO_LAG_AVAIL = 99;

    public final static String CAT_FLIGHT = "FLT";
    public final static String CAT_DEAD_HEAD = "DHD";
    public final static String CAT_HOTEL = "HOT";
    public final static String CAT_SYND = "JDDC";
    public final static String CAT_SIMU = "SIMU";
    public final static String CAT_SIM_E2 = "E2";
    public final static String CAT_SIM_E1 = "E1";
    public final static String CAT_SIM_C1 = "C1";
    public final static String CAT_SIM_C2 = "C2";
    public final static String CAT_SIM_LOE = "LOE";
    public final static String CAT_MEDICAL = "VM";
    public final static String CAT_OFF_DDA = "OFFD";
    public final static String CAT_OFF = "OFF";
    public final static String CAT_OFF_RECUP = "OFFR";
    public final static String CAT_ILLNESS = "HS";
    public final static String CAT_VACATION = "CA";
    public final static String CAT_VACATION_OFF_CAMPAIGN = "CAHC";
    public final static String CAT_BLANC = "BLANC"; // not a Transavia code

    private GregorianCalendar gcBegin;
    private GregorianCalendar gcEnd;
    private String summary = "";
    private String category = "";
    private String description = "";
    private String iataOrig = "";
    private Airport airportOrig;
    private String iataDest = "";
    private Airport airportDest;
    private String fltNumber = "";
    private String crew = "";
    private String training = "";
    private String remark = "";
    private String hotelData = "";
    private String function = "";
    private int lagDest = 0;
    private int blockTime = 0;  // in minutes
    private boolean firstEventOfDay;


    public PlanningEvent() {
    }

    public PlanningEvent(GregorianCalendar dateStart, GregorianCalendar dateEnd, String summary, String category, String description, HashMap<String, String> trigraphs) {
        this.gcBegin = dateStart;
        this.gcEnd = dateEnd;
        this.summary = summary.trim();
        this.category = category.trim();
        this.description = description.trim();

        if (category.equals(CAT_FLIGHT) || category.equals(CAT_DEAD_HEAD)) {
            parseSummary(summary);
            parseDescription(trigraphs);
        }

        blockTime = (int) (dateEnd.getTimeInMillis() - dateStart.getTimeInMillis()) / 60000;

    }

    public GregorianCalendar getGcEnd() {
        return gcEnd;
    }

    public GregorianCalendar getGcBegin() {
        return gcBegin;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setGcEnd(GregorianCalendar gcEnd) {
        this.gcEnd = gcEnd;
    }

    public void setGcBegin(GregorianCalendar gcBegin) {
        this.gcBegin = gcBegin;
    }

    public void setLabel(String label) {
        this.summary = label;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFltNumber() {
        return fltNumber;
    }

    public Airport getAirportOrig() {
        return airportOrig;
    }

    public void setAirportOrig(Airport airportOrig) {
        this.airportOrig = airportOrig;
    }

    public Airport getAirportDest() {
        return airportDest;
    }

    public void setAirportDest(Airport airportDest) {
        this.airportDest = airportDest;
    }


    public String getIataDest() {
        return iataDest;
    }

    public String getIataOrig() {
        return iataOrig;
    }

    public String getCrew() {
        return crew;
    }

    public void setCrew(String crew) {
        this.crew = crew;
    }

    public int getLagDest() {
        return lagDest;
    }

    private void parseSummary(String summary) {
        String target;
        Pattern pattern;
        Matcher result;
        // search flight number
        fltNumber = summary.substring(0, summary.indexOf(" "));

        // origin and destination
        target = "([a-zA-Z]{3})[-\\*]([a-zA-Z]{3})";
        pattern = Pattern.compile(target);
        result = pattern.matcher(summary);

        while (result.find()) {
            iataOrig = result.group(1);
            iataDest = result.group(2);
        }

        // time shift at destination
        target = Pattern.quote("(") + "\\W\\d{4}" + Pattern.quote(")");
        pattern = Pattern.compile(target);
        result = pattern.matcher(summary);
        while (result.find()) {
            String lag = result.group(0).substring(1, 4);
            lagDest = Integer.parseInt(lag);
            return;
        }
        lagDest = NO_LAG_AVAIL;   // set lag to 99 if no lag detected ( case of deadheading )
    }

    private void parseDescription(HashMap<String, String> trigraphs) {
        StringBuilder sbCrew = new StringBuilder();
        String[] desc = description.split("\n");
        // get function
        for (String s : desc) {
            if (s.startsWith("FCT")) {
                function = s.substring(s.indexOf(":") + 1).trim();
                break;
            }
        }

        // get crew
        if ((desc.length > 4)) {
            sbCrew.append("Pilotes :\n");
            String cible2 = "[A-Z]{3}";
            Pattern regex1 = Pattern.compile(cible2);
            Matcher result1 = regex1.matcher(desc[3]);

            while (result1.find()) {
                String nom = "Inconnu";
                if (trigraphs.containsKey(result1.group(0))) {
                    nom = trigraphs.get(result1.group(0));
                }
                sbCrew.append(result1.group(0)).append(" - ").append(nom).append("\n");
            }

            sbCrew.append("\nPNC :\n");

            Matcher result2 = regex1.matcher(desc[4]);

            while (result2.find()) {
                String nom = "Inconnu";
                if (trigraphs.containsKey(result2.group(0))) {
                    nom = trigraphs.get(result2.group(0));
                }
                sbCrew.append(result2.group(0)).append(" - ").append(nom).append("\n");
            }
        }
        crew = sbCrew.toString();
    }

    public boolean isDayEvent() {
        switch (category) {
            case PlanningEvent.CAT_OFF:
                return true;

            case PlanningEvent.CAT_OFF_DDA:
                return true;

            case PlanningEvent.CAT_VACATION:
                return true;

            case PlanningEvent.CAT_VACATION_OFF_CAMPAIGN:
                return true;

            case PlanningEvent.CAT_BLANC:
                return true;

            case PlanningEvent.CAT_ILLNESS:
                return true;

            default:
                return false;
        }
    }

    public boolean isFirstEventOfDay() {
        return firstEventOfDay;
    }

    public void setFirstEventOfDay(boolean firstEventOfDay) {
        this.firstEventOfDay = firstEventOfDay;
    }

    public String getFunction() {
        return function;
    }

    public int getBlockTime() {
        return blockTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getHotelData() {
        return hotelData;
    }

    public void setHotelData(String hotelData) {
        this.hotelData = hotelData;
    }

    public String getTraining() {
        return training;
    }

    public void setTraining(String training) {
        this.training = training;
    }

    public boolean isSimActivity() {
        switch (category) {
            case CAT_SIMU:
                return true;
            case CAT_SIM_C1:
                return true;
            case CAT_SIM_C2:
                return true;
            case CAT_SIM_E1:
                return true;
            case CAT_SIM_E2:
                return true;
            case CAT_SIM_LOE:
                return true;

            default:
                return false;
        }
    }
}
