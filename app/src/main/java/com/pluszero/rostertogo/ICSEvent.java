package com.pluszero.rostertogo;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ICSEvent {

    // Debug flag to avoid creating Log.v objects if unnecessary
    private static final String TAG = "ICSEvent";
    public static final String TAG_BEGIN = "BEGIN:VEVENT";
    private final static String TAG_REQUEST_TIME = "UID:";
    private final String TAG_START_TIME = "DTSTART;VALUE=DATE-TIME:";
    private final String TAG_END_TIME = "DTEND;VALUE=DATE-TIME:";
    private final String TAG_CATEGORIES = "CATEGORIES:";
    private final String TAG_SUMMARY = "SUMMARY:";
    private final String TAG_DESCRIPTION = "DESCRIPTION:";
    public static final String TAG_END = "END:VEVENT";

    private String ICSText = "";
    private String category = "";
    private String iataDepart = "";
    private String iataArrivee = "";

    public ICSEvent(String ICSData) {
        ICSText = ICSData;
        category = getICSCategory();
    }

    public GregorianCalendar getICSStart() {
        String cible = TAG_START_TIME + "(\\d{4}\\d{2}\\d{2}T\\d{2}\\d{2}\\d{2}Z)";

        Pattern regex = Pattern.compile(cible);
        Matcher result = regex.matcher(ICSText);
        if (result.find()) {
            String date = result.group(1);
            GregorianCalendar cal = getDateZ(date);

            return cal;
        } else {
            return null;
        }
    }

    public GregorianCalendar getICSEnd() {
        String cible = TAG_END_TIME + "(\\d{4}\\d{2}\\d{2}T\\d{2}\\d{2}\\d{2}Z)";

        Pattern regex = Pattern.compile(cible);
        Matcher result = regex.matcher(ICSText);
        if (result.find()) {
            String date = result.group(1);
            GregorianCalendar cal = getDateZ(date);
            return cal;
        } else {
            return null;
        }
    }

    public String getICSCategory() {
        String cible = TAG_CATEGORIES + "(.*)\\r";
        Pattern regex = Pattern.compile(cible);
        Matcher result = regex.matcher(ICSText);
        if (result.find()) {
            return result.group(1);
        } else {
            return "NOCAT";
        }
    }

    public String getICSDesc() {
        String cible = TAG_DESCRIPTION + "(.*)\\r";
        Pattern regex = Pattern.compile(cible);
        Matcher result = regex.matcher(ICSText);
        if (result.find()) {
            String desc = result.group(1).replace("\\n", "\n");
            StringBuilder str = new StringBuilder();
            str.append(desc);
            return str.toString();
        } else {
            return "-NIL-";
        }
    }

    public String getICSSummary() {
        String cible = TAG_SUMMARY + "(.*)\\r";
        Pattern regex = Pattern.compile(cible);
        Matcher result = regex.matcher(ICSText);
        if (result.find()) {
            String summary = result.group(1);
            if (category.equalsIgnoreCase("FLT") || category.equalsIgnoreCase("DHD")) {
                extractAirports(summary);
            }
            return summary;
        } else {
            return "No Summary";
        }
    }

    private void extractAirports(String summary) { // SUMMARY:TO3082
        // ORY-SAW(+0300) or GPM*GNT for deadheading
        String cible = "([A-Z]{3})[-\\*]([A-Z]{3})";
        Pattern regex = Pattern.compile(cible);
        Matcher result = regex.matcher(summary);
        if (result.find()) {
            iataDepart = result.group(1).toLowerCase();
            iataArrivee = result.group(2).toLowerCase();
        }
    }

    private GregorianCalendar getDateZ(String date) { // 20140605T123000Z
        GregorianCalendar tmp = new GregorianCalendar();
        tmp.setTimeZone(TimeZone.getTimeZone("UTC"));
        return getDate(date, tmp);

    }

    private GregorianCalendar getDate(String date, GregorianCalendar tmp) { // 20140605T123000Z

        String cible = "(\\d{4})(\\d{2})(\\d{2})T(\\d{2})(\\d{2})(\\d{2})Z";
        Pattern regex1 = Pattern.compile(cible);
        Matcher result1 = regex1.matcher(date);
        if (result1.find()) {
            // add -1 to group(2) cause GregorianCalendar stores months from 0 to 11
            tmp.set(Integer.valueOf(result1.group(1)), Integer.valueOf(result1.group(2)) - 1, Integer.valueOf(result1.group(3)), Integer.valueOf(result1.group(4)),
                    Integer.valueOf(result1.group(5)), Integer.valueOf(result1.group(6)));
            // set seconds and milliseconds to zero
            tmp.set(Calendar.SECOND, 0);
            tmp.set(Calendar.MILLISECOND, 0);
        }
        return tmp;
    }
}
