package com.pluszero.rostertogo;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.pluszero.rostertogo.filenav.FolderNameDialogFragment;
import com.pluszero.rostertogo.filenav.FragFilenav;
import com.pluszero.rostertogo.filenav.OnFileNavEventListener;
import com.pluszero.rostertogo.model.PlanningEvent;
import com.pluszero.rostertogo.model.PlanningModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnConnectionListener, OnFileNavEventListener {

    public static final String FRAG_LOGIN = "frgmt_connexion";
    public static final String FRAG_LOAD = "frag_load_xml";
    public static final String FRAG_SAVE = "frag_save_xml";
    public static final String FRAG_PREFERENCES = "frag_preferences";
    public static final String FRAG_ABOUT = "frag_about";
    public static final String FRAG_PLANNING = "frag_planning"; // not linked to drawer
    public static final String FRAG_SYNC = "frag_sync";

    private PlanningModel planningModel;
    private ConnectTo connectTo;
    private HashMap<String, String> trigraphsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activ_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        planningModel = new PlanningModel();
        trigraphsList = getTrigraphsList();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displayView(R.id.nav_planning);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (planningModel.getAlEvents().size() == 0) {
            Fragment fragment = FragLogin.newInstance();
            getFragmentManager().beginTransaction().replace(
                    R.id.content_frame, fragment, FRAG_LOGIN).commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.activ_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            // Display the preferences fragment as the main content.
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.content_frame, new FragSettings())
//                    .commit();
//
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displayView(item.getItemId());
        return true;
    }

    public void displayView(int viewId) {

        switch (viewId) {
            case R.id.nav_login:
                displayFragmentLogin();
                break;

            case R.id.nav_sync_calendar:
                SyncPlanning sp = new SyncPlanning(this, planningModel);
                break;

            case R.id.nav_planning:
                displayPlanning();
                break;

            case R.id.nav_settings:
                displayFragmentSettings();
                break;

            case R.id.nav_load: // load XML
                displayFragmentLoad();
                break;

            case R.id.nav_save: // save as XML
                displayFragmentSave();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void displayFragmentSave() {
        if (planningModel.getAlEvents().size() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.no_planning_avail),
                    Toast.LENGTH_LONG).show();
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String filename = "Plng_" + planningModel.getUserTrigraph() + "_" + sdf.format(planningModel.getAlEvents().get(0).getGcBegin().getTime());
        Fragment fragment = FragFilenav.newInstance(filename, false);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, FRAG_SAVE)
                .commit();
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Sauver le planning");
    }

    private void displayFragmentLoad() {
        Fragment fragment = FragFilenav.newInstance(null, true);
        getFragmentManager().beginTransaction().replace(
                R.id.content_frame, fragment, FRAG_LOAD).commit();
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Charger un planning");
    }

    private void displayFragmentLogin() {
        Fragment fragment = FragLogin.newInstance();
        getFragmentManager().beginTransaction().replace(
                R.id.content_frame, fragment, FRAG_LOGIN).commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Connexion");
        }
    }

    private void displayFragmentSettings() {
        // Display the preferences fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new FragSettings())
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Préférences");
        }
    }


    @Override
    public void onConnectionUpdated(String... messages) {
        FragLogin fragLogin = (FragLogin) getFragmentManager().findFragmentByTag(FRAG_LOGIN);
        if (fragLogin == null)
            return;

        fragLogin.setConnectionStatus(messages[0]);
    }

    @Override
    public void onConnectionCompleted(int value) {
        FragLogin fragLogin = (FragLogin) getFragmentManager().findFragmentByTag(FRAG_LOGIN);
        if (fragLogin == null)
            return;
        fragLogin.getBtnOK().setEnabled(true);
        fragLogin.getProgressBar().setVisibility(View.INVISIBLE);
        planningModel.setUserTrigraph(connectTo.getUserTrigraph());
        addToModel(extractICS(connectTo.getBody()));
        planningModel.copyCrew();
        planningModel.fixSplittedActivities();
        planningModel.addJoursBlanc();
        planningModel.factorizeDays();
        planningModel.computeActivityFigures();

        // Insert the fragment by replacing any existing fragment
        displayPlanning();
    }

    @Override
    public void onConnectionClick(String login, String password) {
        connectTo = (ConnectTo) new ConnectTo(this, login, password, this)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

        FragLogin fragLogin = (FragLogin) getFragmentManager().findFragmentByTag(FRAG_LOGIN);
        if (fragLogin == null)
            return;
        fragLogin.getBtnOK().setEnabled(false);
        fragLogin.getProgressBar().setVisibility(View.VISIBLE);
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
                PlanningEvent planningEvent = new PlanningEvent(event.getICSStart(), event.getICSEnd(), event.getICSSummary(), event.getICSCategory(), event.getICSDesc(), trigraphsList); // Incrément pour parcourir le fichier
                alEvents.add(planningEvent);
                indexICS += source.length();
            }
            Collections.sort(alEvents, new DateComparator());
        }
        return alEvents;
    }

    private HashMap<String, String> getTrigraphsList() {
        HashMap<String, String> map = new HashMap<>();
        String[] result;
        // Load crew directory.
        InputStreamReader isr = new InputStreamReader(getResources().openRawResource(R.raw.crew_directory));
        BufferedReader br = new BufferedReader(isr, 8192);
        try {
            String line;
            while ((line = br.readLine()) != null) {
                result = line.split(";");
                map.put(result[0], result[2] + " " + result[1]);
            }
            isr.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public void onFilenavItemSelected(File file) {
        if (planningModel == null) {
            planningModel = new PlanningModel();
        }
        planningModel.modeOnline = false;
        addToModel(extractICS(readIcs(file)));
        planningModel.copyCrew();
        planningModel.fixSplittedActivities();
        planningModel.addJoursBlanc();
        planningModel.factorizeDays();
        planningModel.computeActivityFigures();
        // Insert the fragment by replacing any existing fragment
        FragPlanning fragment = new FragPlanning();
        fragment.setData(planningModel);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, FRAG_PLANNING).commit();
        if (getSupportActionBar() != null) {
            if (planningModel.getUserTrigraph() != null) {
                getSupportActionBar().setTitle("Planning (" + planningModel.getUserTrigraph() + ")");
            } else
                getSupportActionBar().setTitle("Planning");
        }
    }

    @Override
    public void onFileNavSave(String path, String name) {
        if (!Utils.isExternalStorageWritable()) {
            Toast.makeText(this, getResources().getString(R.string.external_storage_not_available),
                    Toast.LENGTH_LONG).show();
        } else {
            IcsWriter icsWriter = new IcsWriter(planningModel.getAlEvents(), planningModel.getUserTrigraph());

            try {
                File file = new File(path, name + ".ics");
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                osw.append(icsWriter.getContent());
                osw.close();
                fos.close();
                Toast.makeText(this, getResources().getString(R.string.file_saved),
                        Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Toast.makeText(this, getResources().getString(R.string.file_could_not_be_saved),
                        Toast.LENGTH_LONG).show();
                return;
            }
            // get back to previous fragment (month)
            FragPlanning fragPlanning = new FragPlanning();
            fragPlanning.setData(planningModel);
            getFragmentManager().beginTransaction().replace(R.id.content_frame, fragPlanning).commit();
        }
    }

    private String readIcs(File file) {
        String fileContent = null;
        if (!Utils.isExternalStorageReadable())
            return null;

        fileContent = readFileFromSDCard(file);
        if (fileContent == null || fileContent.equals(""))
            return null;

        return fileContent;
    }

    private String readFileFromSDCard(File file) {
        StringBuilder sb = null;
        if (!file.exists()) {
            throw new RuntimeException("File not found");
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n"); //use same separator as in online stream
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }


    private void displayPlanning() {
        FragPlanning fragment = new FragPlanning();
        fragment.setData(planningModel);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, FRAG_PLANNING).commit();
        if (getSupportActionBar() != null) {
            if (planningModel.getUserTrigraph() != null) {
                getSupportActionBar().setTitle("Planning (" + planningModel.getUserTrigraph() + ")");
            } else
                getSupportActionBar().setTitle("Planning");
        }
    }

}
