package com.pluszero.rostertogo;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.util.Log;

import com.pluszero.rostertogo.model.PlanningEvent;
import com.pluszero.rostertogo.model.PlanningModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

/**
 * Created by Cyril on 09/05/2016.
 */
public class SyncPlanning extends AsyncTask<String, String, Integer> {

    public static final String CALENDAR_FLIGHT = "calendar_flight";
    public static final String CALENDAR_GROUND = "calendar_ground";
    public static final String CALENDAR_OFF = "calendar_off";
    public static final String CALENDAR_VACATION = "calendar_vacation";
    public static final String CALENDAR_BLANC = "calendar_blanc";
    public static final String CALENDAR_EVENTS_DELETE = "calendar_events_delete";

    private Context context;
    private HashMap<String, String> mapCalendars;
    private String accountName;
    private PlanningModel model;
    private OnSynchronisationListener listener;

    public int events_ok, events_err;

    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[]{
            Calendars._ID,                           // 0
            Calendars.ACCOUNT_NAME,                  // 1
            Calendars.CALENDAR_DISPLAY_NAME,         // 2
            Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;


    /**
     * This constructor is used by the application to get the list of available calendars
     *
     * @param context
     */
    public SyncPlanning(Context context) {
        this.context = context;
        queryUserCalendars();
    }

    public SyncPlanning(Context context, PlanningModel model, OnSynchronisationListener listener) {
        this.context = context;
        this.model = model;
        this.listener = listener;

    }

    @Override
    protected Integer doInBackground(String... params) {
        publishProgress("Récupération de la liste des calendriers");
        queryUserCalendars();
        publishProgress("Effacement des anciens évenements");
        deleteOldEvents();
        publishProgress("Ajout des nouveaux évenements");
        addEvents(this.model);
        return 1;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        listener.onSynchronisationProgress(values);
    }

    @Override
    protected void onPostExecute(Integer value) {
        super.onPostExecute(value);
        listener.onSynchronisationCompleted(value.intValue());
    }

    private void queryUserCalendars() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        accountName = sharedPref.getString("pref_google_email", "");
        // Run query
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?))";
        String[] selectionArgs = new String[]{accountName, "com.google"};
        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
        // Use the cursor to step through the returned records
        mapCalendars = new HashMap<>();
        while (cur.moveToNext()) {
            // Get the field values
            long calID = cur.getLong(PROJECTION_ID_INDEX);
            String name = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            mapCalendars.put(name, String.valueOf(calID));
        }
    }


    private void addEvents(PlanningModel model) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        // dateformatter for UID, STAMP...
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        String timezone = TimeZone.getDefault().getID();
        // calendar for ics time generation id
        GregorianCalendar cal = new GregorianCalendar();
        long calID = 1;
        int n = 0;
        ContentResolver cr = context.getContentResolver();

        for (PlanningEvent pe : model.getAlEvents()) {

            //TODO: deal with allday events !

            if (getExportType(pe).equals(CALENDAR_FLIGHT)){
                HashSet calsFlight = (HashSet) sharedPref.getStringSet("pref_google_calendar_flight", new HashSet<String>());
                for (Object obj : calsFlight) {
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.UID_2445, cal.getTimeInMillis() + "_ROSTERTOGO_" + model.getUserTrigraph() + n);
                    values.put(CalendarContract.Events.DTSTART, pe.getGcBegin().getTimeInMillis());
                    values.put(CalendarContract.Events.DTEND, pe.getGcEnd().getTimeInMillis());
                    values.put(CalendarContract.Events.TITLE, buildSummary(pe));
                    values.put(CalendarContract.Events.DESCRIPTION, buildDescription(pe));
                    values.put(CalendarContract.Events.CALENDAR_ID, mapCalendars.get(obj.toString()));
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    long eventID = Long.parseLong(uri.getLastPathSegment());

                    break;
                }
            }

            if (getExportType(pe).equals(CALENDAR_GROUND)){
                HashSet calsGround = (HashSet) sharedPref.getStringSet("pref_google_calendar_ground", new HashSet<String>());
                for (Object obj : calsGround) {
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.UID_2445, cal.getTimeInMillis() + "_ROSTERTOGO_" + model.getUserTrigraph() + n);
                    values.put(CalendarContract.Events.DTSTART, pe.getGcBegin().getTimeInMillis());
                    values.put(CalendarContract.Events.DTEND, pe.getGcEnd().getTimeInMillis());
                    values.put(CalendarContract.Events.TITLE, buildSummary(pe));
                    values.put(CalendarContract.Events.DESCRIPTION, buildDescription(pe));
                    values.put(CalendarContract.Events.CALENDAR_ID, mapCalendars.get(obj.toString()));
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    long eventID = Long.parseLong(uri.getLastPathSegment());

                    break;
                }
            }

            if (getExportType(pe).equals(CALENDAR_OFF)){
                HashSet calsOff = (HashSet) sharedPref.getStringSet("pref_google_calendar_daysoff", new HashSet<String>());
                for (Object obj : calsOff) {
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.UID_2445, cal.getTimeInMillis() + "_ROSTERTOGO_" + model.getUserTrigraph() + n);
                    values.put(CalendarContract.Events.DTSTART, pe.getGcBegin().getTimeInMillis());
                    values.put(CalendarContract.Events.DTEND, pe.getGcEnd().getTimeInMillis());
                    values.put(CalendarContract.Events.TITLE, buildSummary(pe));
                    values.put(CalendarContract.Events.DESCRIPTION, buildDescription(pe));
                    values.put(CalendarContract.Events.CALENDAR_ID, mapCalendars.get(obj.toString()));
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    long eventID = Long.parseLong(uri.getLastPathSegment());

                    break;
                }
            }

            if (getExportType(pe).equals(CALENDAR_VACATION)){
                HashSet calsVacation = (HashSet) sharedPref.getStringSet("pref_google_calendar_vacation", new HashSet<String>());
                for (Object obj : calsVacation) {
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.UID_2445, cal.getTimeInMillis() + "_ROSTERTOGO_" + model.getUserTrigraph() + n);
                    values.put(CalendarContract.Events.DTSTART, pe.getGcBegin().getTimeInMillis());
                    values.put(CalendarContract.Events.DTEND, pe.getGcEnd().getTimeInMillis());
                    values.put(CalendarContract.Events.TITLE, buildSummary(pe));
                    values.put(CalendarContract.Events.DESCRIPTION, buildDescription(pe));
                    values.put(CalendarContract.Events.CALENDAR_ID, mapCalendars.get(obj.toString()));
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    long eventID = Long.parseLong(uri.getLastPathSegment());

                    break;
                }
            }

            if (getExportType(pe).equals(CALENDAR_BLANC)){
                HashSet calsBlanc = (HashSet) sharedPref.getStringSet("pref_google_calendar_dayswhite", new HashSet<String>());
                for (Object obj : calsBlanc) {
                    ContentValues values = new ContentValues();
                    values.put(CalendarContract.Events.UID_2445, cal.getTimeInMillis() + "_ROSTERTOGO_" + model.getUserTrigraph() + n);
                    values.put(CalendarContract.Events.DTSTART, pe.getGcBegin().getTimeInMillis());
                    values.put(CalendarContract.Events.DTEND, pe.getGcEnd().getTimeInMillis());
                    values.put(CalendarContract.Events.TITLE, buildSummary(pe));
                    values.put(CalendarContract.Events.DESCRIPTION, buildDescription(pe));
                    values.put(CalendarContract.Events.CALENDAR_ID, mapCalendars.get(obj.toString()));
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    long eventID = Long.parseLong(uri.getLastPathSegment());

                    break;
                }
            }
        }
    }

    private void deleteOldEvents() {
        //list all events
        long begin = model.getAlEvents().get(0).getGcBegin().getTimeInMillis();
        long end = model.getAlEvents().get(model.getAlEvents().size() - 1).getGcEnd().getTimeInMillis();

        String[] proj = new String[]{
                CalendarContract.Instances._ID,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.UID_2445};
        Cursor cursor = CalendarContract.Instances.query(
                context.getContentResolver(), proj, begin, end);

        // filter events whose uid2445 contains "_ROSTERTOGO_" + user's trigraph
        ArrayList<Long> rosterEvents = new ArrayList<>();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    long eventId = cursor.getLong(4);
                    String title = cursor.getString(3);
                    String uid2445 = cursor.getString(5);
                    if (uid2445 == null) {
                        continue;
                    } else if (uid2445.contains("_ROSTERTOGO_" + model.getUserTrigraph())) {
                        rosterEvents.add(eventId);
                    }
                }
            }
        }
        // delete the filtered events
        for (Long eventId : rosterEvents) {
            Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId.longValue());
            int rows = context.getContentResolver().delete(deleteUri, null, null);
            // do something with rows, if necessary...
            Log.i("ROSTERTOGO", "Rows deleted: " + rows);
        }
    }

    private String buildSummary(PlanningEvent pe) {
        StringBuilder sb = new StringBuilder();
        if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT) || pe.getCategory().equals(PlanningEvent.CAT_DEAD_HEAD)) {
            sb.append(pe.getFltNumber()).append(" ")
                    .append(pe.getIataOrig()).append(" - ")
                    .append(pe.getIataDest());
            if (pe.getLagDest() != PlanningEvent.NO_LAG_AVAIL) {
                sb.append(" (TU ");
                if (pe.getLagDest() < 0) {
                    sb.append("-").append(pe.getLagDest()).append(")");
                } else {
                    sb.append("+").append(pe.getLagDest()).append(")");
                }
            }
            return sb.toString();
        } else {
            return pe.getSummary();
        }
    }

    private String buildDescription(PlanningEvent pe) {
        String nl = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT) || pe.getCategory().equals(PlanningEvent.CAT_DEAD_HEAD)) {
            sb = new StringBuilder();
            if (pe.getAirportOrig() != null) {
                sb.append("Départ:").append(nl);
                sb.append(pe.getAirportOrig().city.toUpperCase()).append(" / ");
                sb.append(pe.getAirportOrig().name).append(nl);
                sb.append(pe.getAirportOrig().country).append(nl).append(nl);
            }
            if (pe.getAirportDest() != null) {
                sb.append("Arrivée:").append(nl);
                sb.append(pe.getAirportDest().city.toUpperCase()).append(" / ");
                sb.append(pe.getAirportDest().name).append(nl);
                sb.append(pe.getAirportDest().country).append(nl).append(nl);
            }

            if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
                sb.append("Fonction : ").append(pe.getFunction());
                sb.append(nl);
                sb.append("Temps de vol : ");
                sb.append(Utils.convertMinutesToHoursMinutes(pe.getBlockTime()));
            } else {
                sb.append("Durée : ");
                sb.append(Utils.convertMinutesToHoursMinutes(pe.getBlockTime()));
            }

            sb.append(nl).append(nl).append(pe.getCrew());

            if (!pe.getTraining().equals("")) {
                sb.append(nl).append(nl).append(pe.getTraining());
            }
            if (!pe.getRemark().equals("")) {
                sb.append(nl).append(nl).append(pe.getRemark());
            }
            if (!pe.getHotelData().equals("")) {
                sb.append(nl).append(nl).append("Hôtel :").append(nl);
                sb.append(pe.getHotelData());
            }
        } else {
            sb.append("Durée : ");
            sb.append(Utils.convertMinutesToHoursMinutes(pe.getBlockTime()));
            if (!pe.getTraining().equals("")) {
                sb.append(nl);
                sb.append(nl);
                sb.append(pe.getTraining());
            }
            if (!pe.getRemark().equals("")) {
                sb.append(nl);
                sb.append(nl);
                sb.append(pe.getRemark());
            }
        }
        return sb.toString();
    }

    public HashMap<String, String> getMapCalendars() {
        return mapCalendars;
    }

    private String getExportType(PlanningEvent event) {
        switch (event.getCategory()) {
            case PlanningEvent.CAT_FLIGHT:
                return CALENDAR_FLIGHT;
            case PlanningEvent.CAT_DEAD_HEAD:
                return CALENDAR_FLIGHT;
            case PlanningEvent.CAT_HOTEL:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_SYND:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_ILLNESS:
                return CALENDAR_OFF;
            case PlanningEvent.CAT_MEDICAL:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_OFF:
                return CALENDAR_OFF;
            case PlanningEvent.CAT_OFF_DDA:
                return CALENDAR_OFF;
            case PlanningEvent.CAT_OFF_RECUP:
                return CALENDAR_OFF;
            case PlanningEvent.CAT_SIMU:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_SIM_C1:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_SIM_C2:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_SIM_E1:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_SIM_E2:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_SIM_LOE:
                return CALENDAR_GROUND;
            case PlanningEvent.CAT_VACATION:
                return CALENDAR_VACATION;
            case PlanningEvent.CAT_BLANC:
                return CALENDAR_BLANC;

            default:
                return CALENDAR_FLIGHT;
        }
    }
}
