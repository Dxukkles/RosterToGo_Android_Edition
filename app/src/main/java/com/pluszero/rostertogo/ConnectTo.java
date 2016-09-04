package com.pluszero.rostertogo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.net.ssl.HttpsURLConnection;

/**
 * @author Cyril
 */
public class ConnectTo extends AsyncTask<String, String, Integer> {

    private String body;
    public String contentIcs, contentPdf;
    private String login;
    private String password;
    private String userTrigraph;    // crewmember's userTrigraph
    private String windowId;

    private boolean changesOrSigned = false;

    private static final String URL_HOST = "https://connect.fr.transavia.com";
    private static final String URL_DASHBOARD = "https://connect.fr.transavia.com/TOConnect/pages/crewweb/dashboard.jsf?windowId=";
    private static final String URL_PLANNING = "https://connect.fr.transavia.com/TOConnect/pages/crewweb/planning.jsf?windowId=";
    private static final String URL_PLANNING_CHANGES = "https://connect.fr.transavia.com/TOConnect/pages/crewweb/changes.jsf?windowId=";

    private static final String MSG_PROCESS_FINISHED = "Processus terminé";
    private static final String MSG_LOGIN_IN_PROGRESS = "Identification en cours";
    private static final String MSG_LOGIN_OK = "Identification OK";
    private static final String MSG_CONNECTING_DASHBOARD = "Connexion au dashboard";
    private static final String MSG_CONNECTING_PLANNING = "Connexion au planning";
    private static final String MSG_FETCHING_PLANNING = "Récupération du planning";
    private static final String MSG_FETCHING_ADDITIONAL_DATA = "Récupération des données additionnelles";

    public static final String MSG_LOGIN_PASS_ERROR = "Le login ou le mot de passe saisi est incorrect";
    public static final String MSG_CHECKING_CHANGES = "Vérification des changements";
    public static final String MSG_PLANNING_VALIDATION = "Validation du planning";

    public static final int ERR_CONNECTION = 101;
    public static final int ERR_LOGIN_PASS = 102;
    public static final int ERR_MODIFICATIONS_NOT_CHECKED = 103;
    public static final int ERR_ROSTER_NOT_SIGNED = 104;
    public static final int ERR_MISC = 105;

    public static final int OK_CONNECTION = 1;

    private SharedPreferences sharedPref;

    private OnConnectionListener listener;
    private Context context;

    public InputStream pdfStream;

    public ConnectTo(Context context, String login, String password, OnConnectionListener listener) {
        this.context = context;
        this.login = login;
        this.password = password;
        this.listener = listener;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        System.setProperty("jsse.enableSNIExtension", "false");
        // Instantiate CookieManager / set CookiePolicy
        CookieManager manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
    }

    private String readStream(InputStream in) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String nextLine = "";
        try {
            while ((nextLine = reader.readLine()) != null) {
                sb.append(nextLine);
            }
        } catch (IOException e) {
        }

        return sb.toString();
    }

    private String extractWindowId(String location) {
        // get the 3 figures "windowId" from the url
        String target = "windowId=(\\w{3})";
        Pattern regex1 = Pattern.compile(target);
        Matcher result1 = regex1.matcher(location);

        if (result1.find()) {
            return result1.group(1);
        } else {
            return null;
        }
    }

    /*
    return the userTrigraph of the loggued crew member
     */
    private String extractUserTrigraph(String source) {
        int idxBegin = source.indexOf("<div class=\"identite\">");
        int idxEnd = source.indexOf("</div>", idxBegin);
        String range = source.substring(idxBegin, idxEnd);
        String target = "\\([A-Z]{3}\\)";
        Pattern regex = Pattern.compile(target);
        Matcher result = regex.matcher(range);

        if (result.find()) {
            // remove braquets before returning value
            return result.group(0).replaceAll("[\\(\\)]", "");
        } else {
            return null;
        }
    }

    public String getUserTrigraph() {
        return userTrigraph;
    }

    @Override
    protected Integer doInBackground(String... params) {
        DataOutputStream wr;
        try {
            publishProgress(MSG_LOGIN_IN_PROGRESS);

            HttpsURLConnection conn;
            URL url;
            int responseCode;
            String location;
            // Connection to accueil
            url = new URL(URL_HOST + "/TOConnect/accueil.jsf");
            conn = (HttpsURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            location = conn.getHeaderField("Location");
            // split url from the embedded cookie
            location = location.substring(0, location.indexOf(";"));

            // connection to login
            url = new URL(location);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            location = conn.getHeaderField("Location");
            windowId = extractWindowId(location);

            // connection to login with updated windowId
            url = new URL(location);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            String viewStateValue = CheckHtml.getViewState(readStream(conn.getInputStream()));

            // post login credentials
            url = new URL(location);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            String urlParam = "&formLogin=formLogin"
                    + "&" + "formLogin_username=" + login
                    + "&" + "formLogin_password=" + password
                    + "&" + "formLogin_actionLogin="
                    + "&" + "javax.faces.ViewState=" + viewStateValue;

            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.10 (.NET CLR 3.5.30729)");

            DataOutputStream dosLogin = new DataOutputStream(conn.getOutputStream());
            dosLogin.writeBytes(urlParam);
            dosLogin.flush();

            if (readStream(conn.getInputStream()).contains(MSG_LOGIN_PASS_ERROR)) {
                return ERR_LOGIN_PASS;
            }

            location = conn.getHeaderField("Location");

            // connection to accueil
            url = new URL(location);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            responseCode = conn.getResponseCode();

            publishProgress(MSG_LOGIN_OK);

            body = readStream(conn.getInputStream());

            // connection to dashboard
            url = new URL(URL_DASHBOARD + windowId);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            publishProgress(MSG_CONNECTING_DASHBOARD);

            conn.setInstanceFollowRedirects(false);
            responseCode = conn.getResponseCode();
            body = readStream(conn.getInputStream());
            userTrigraph = extractUserTrigraph(body);
            viewStateValue = CheckHtml.getViewState(body);
            if (viewStateValue == null) {
                return ERR_CONNECTION;
            }

            // in case of planning modifications
            if (body.contains("Please check your planning modifications</a>")) {

                if (sharedPref.getBoolean(context.getString(R.string.keypref_planning_autosign), false) == false) {
                    return ERR_MODIFICATIONS_NOT_CHECKED;
                }
                url = new URL(URL_PLANNING_CHANGES + windowId);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setInstanceFollowRedirects(false);
                urlParam = "&j_idt73=j_idt73"
                        + "&" + "j_idt73_j_idt194="
                        + "&" + "javax.faces.ViewState=" + viewStateValue;

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);

                publishProgress(MSG_CHECKING_CHANGES);

                DataOutputStream dosModifs = new DataOutputStream(conn.getOutputStream());
                dosModifs.writeBytes(urlParam);
                dosModifs.flush();

                body = readStream(conn.getInputStream());
                //TODO: detect if check is ok
                if (body.contains(MSG_LOGIN_PASS_ERROR)) {
                    return ERR_LOGIN_PASS;
                }
                changesOrSigned = true;
            }

            // in case of planning signature required
            if (body.contains("Please validate your planning</a>")) {
                if (sharedPref.getBoolean(context.getString(R.string.keypref_planning_autosign), false) == false) {
                    return ERR_ROSTER_NOT_SIGNED;
                }
                url = new URL(URL_PLANNING + windowId);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setInstanceFollowRedirects(false);
                urlParam = "formPlanning=formPlanning"
                        + "&" + "formPlanning_tabListeActivites_selection="
                        + "&" + "formPlanning_j_idt143"
                        + "&" + "javax.faces.ViewState=" + viewStateValue;

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);

                publishProgress(MSG_PLANNING_VALIDATION);
                DataOutputStream dosSign = new DataOutputStream(conn.getOutputStream());
                dosSign.writeBytes(urlParam);
                dosSign.flush();

                body = readStream(conn.getInputStream());
                //TODO: detect if check is ok
                if (body.contains(MSG_LOGIN_PASS_ERROR)) {
                    return ERR_LOGIN_PASS;
                }
                changesOrSigned = true;
            }


            // connection to planning
            url = new URL(URL_PLANNING + windowId);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            publishProgress(MSG_CONNECTING_PLANNING);

            conn.setInstanceFollowRedirects(false);
            responseCode = conn.getResponseCode();
            body = readStream(conn.getInputStream());

            // get the ics file
            url = new URL(URL_PLANNING + windowId);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            urlParam = "&formPlanning=formPlanning"
                    + "&" + "formPlanning_tabListeActivites_selection="
                    + "&" + "formPlanning_j_idt145="
                    + "&" + "formLogin_actionLogin="
                    + "&" + "javax.faces.ViewState=" + viewStateValue;

            publishProgress(MSG_FETCHING_PLANNING);

            DataOutputStream dosIcs = new DataOutputStream(conn.getOutputStream());
            dosIcs.writeBytes(urlParam);
            dosIcs.flush();

            int BUFFER = 1024;
            int count;
            byte data[] = new byte[BUFFER];
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos, BUFFER);

            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                bos.write(data, 0, count);
            }
            bos.close();
            baos.close();

            bis.close();
            bis.close();

            contentIcs = baos.toString("UTF-8");

//            // get the pdf file
            url = new URL(URL_PLANNING + windowId);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setInstanceFollowRedirects(false);
            urlParam = "&formPlanning=formPlanning"
                    + "&" + "formPlanning_tabListeActivites_selection="
                    + "&" + "formPlanning_j_idt144="
                    + "&" + "formLogin_actionLogin="
                    + "&" + "javax.faces.ViewState=" + viewStateValue;

            publishProgress(MSG_FETCHING_ADDITIONAL_DATA);

            DataOutputStream dosPdf = new DataOutputStream(conn.getOutputStream());
            dosPdf.writeBytes(urlParam);
            dosPdf.flush();

            pdfStream = conn.getInputStream();

            publishProgress(MSG_PROCESS_FINISHED);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ERR_MISC;
        } catch (ProtocolException e) {
            e.printStackTrace();
            return ERR_MISC;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ERR_MISC;

        } catch (IOException e) {
            e.printStackTrace();
            return ERR_MISC;

        }
        return OK_CONNECTION;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("preexec OK");
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        listener.onConnectionUpdated(values);
    }

    @Override
    protected void onPostExecute(Integer value) {
        super.onPostExecute(value);
        listener.onConnectionCompleted(value.intValue());
    }
}
