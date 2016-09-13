package com.pluszero.rostertogo.model;

import android.content.Context;
import android.os.AsyncTask;

import com.pluszero.rostertogo.ConnectTo;
import com.pluszero.rostertogo.DateComparator;
import com.pluszero.rostertogo.ICSEvent;
import com.pluszero.rostertogo.OnPlanningGeneratorListener;
import com.pluszero.rostertogo.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Cyril on 13/09/2016.
 * <p/>
 * A class that parse .ics and .pdf files and constructs fill the planning model with appropriate
 * data
 */
public class PlanningGenerator extends AsyncTask<String, String, Integer> {


    public static final int PLANNING_OK = 1;
    private OnPlanningGeneratorListener listener;
    private Context context;
    private PlanningModel planningModel;
    private ConnectTo connectTo;
    private HashMap<String, String> trigraphs;
    private String icsContent; // for offline mode only

    public PlanningGenerator(Context context, OnPlanningGeneratorListener listener) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected Integer doInBackground(String... params) {

        publishProgress("Génération du planning en cours...");
        if (planningModel.modeOnline) {
            planningModel.setUserTrigraph(connectTo.getUserTrigraph());
            addToModel(extractICS(connectTo.contentIcs));
        } else {
            addToModel(extractICS(icsContent));
        }


        // additional work
        planningModel.findAirportDetails();
        planningModel.copyCrew();
        planningModel.fixSplittedActivities();
        planningModel.addJoursBlanc();
        planningModel.factorizeDays();
        planningModel.computeActivityFigures();

        // get data from PDF
        // IMPORTANT : deal with PDF after previous additional work
        File privateDir = context.getDir("pdf", Context.MODE_PRIVATE); //Creating an internal dir;
        File pdfFile = new File(privateDir, "planning.pdf"); //Getting a file within the dir.
        planningModel.addDataFromPDF(pdfFile, trigraphs);

        return PLANNING_OK;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        listener.onPlanningGeneratorProgress(values);
    }

    @Override
    protected void onPostExecute(Integer value) {
        super.onPostExecute(value);
        listener.onPlanningGeneratorCompleted(value.intValue());
    }

    private void addToModel(ArrayList<PlanningEvent> list) {
        ArrayList<PlanningEvent> events = new ArrayList<>();
        // get the date of first event, set it at 00h00
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(list.get(0).getGcBegin().getTime());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        //add events of actual model that are before, in a new arraylist
        for (PlanningEvent pe : planningModel.getAlEvents()) {
            if (pe.getGcBegin().getTimeInMillis() < c.getTimeInMillis()) {
                events.add(pe);
            }
        }
        events.addAll(list);
        // add content of new list to model
        planningModel.getAlEvents().clear();
        planningModel.getAlEvents().addAll(events);
        planningModel.sortByAscendingDate();
    }

    private ArrayList<PlanningEvent> extractICS(String content) {
        ArrayList<PlanningEvent> alEvents = new ArrayList();

        // Extraction planning
        if (!content.equals("")) {
            // get user trigraph
            int idxBegin = content.indexOf("UID:");
            int idxEnd = content.indexOf(System.getProperty("line.separator"), idxBegin);
            String range = content.substring(idxBegin, idxEnd);
            String target = "-[A-Z]{3}";
            Pattern regex = Pattern.compile(target);
            Matcher result = regex.matcher(range);

            if (result.find()) {
                planningModel.setUserTrigraph(result.group(0).replaceAll("-", ""));
            } else {
                // use lowercaser to differantiate with normal trigraph
                planningModel.setUserTrigraph("nil");
            }

            int indexICS = 0;

            while ((indexICS = content.indexOf(ICSEvent.TAG_BEGIN, indexICS)) != -1) {

                String source = Utils.extractString(content, ICSEvent.TAG_BEGIN, ICSEvent.TAG_END, indexICS);
                ICSEvent event = new ICSEvent(source);
                PlanningEvent planningEvent = new PlanningEvent(event.getICSStart(), event.getICSEnd(), event.getICSSummary(), event.getICSCategory(), event.getICSDesc(), trigraphs); // Incrément pour parcourir le fichier
                alEvents.add(planningEvent);
                indexICS += source.length();
            }
            Collections.sort(alEvents, new DateComparator());
        }
        return alEvents;
    }

    public void setPlanningModel(PlanningModel planningModel) {
        this.planningModel = planningModel;
    }

    public void setTrigraphs(HashMap<String, String> trigraphs) {
        this.trigraphs = trigraphs;
    }

    public void setConnectTo(ConnectTo connectTo) {
        this.connectTo = connectTo;
    }

    public void setIcsContent(String icsContent) {
        this.icsContent = icsContent;
    }
}
