package com.pluszero.rostertogo;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class Utils {
    // Debug flag to avoid creating Log.v objects if unnecessary

    private static final String TAG = "Utils";
    public static final String SAVE_DIRECTORY = "CrewTO";

    public static String getDateTimeExtension() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Calendar cal = Calendar.getInstance();
        String tmp = dateFormat.format(cal.getTime());
        return tmp;
    }

    public static String getConnexionDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        Date date = Calendar.getInstance().getTime();
        String jour = dateFormat.format(date);
        String heure = hourFormat.format(date);

        String tmp = " le " + jour + " � " + heure;
        return tmp;
    }

    public static boolean isWellFormed(InputStream is) {
        DefaultHandler handler = new DefaultHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(is, handler);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Parse a string formatted as "HH.MM" and returns a Calendar object set
     * with the time.
     *
     * @param time The string to parse
     * @return The newly created Calendar object with the parsed time.
     */
    public static Calendar getCalendar(String time, TimeZone timeZone) {
        Calendar calendar = new GregorianCalendar(timeZone);

        String[] timeComponents = time.split(":");
        setTime(calendar, Integer.parseInt(timeComponents[0]), Integer.parseInt(timeComponents[1]), 0, 0);
        return calendar;
    }

    /**
     * Set the time of the {@code calendar}.
     *
     * @param calendar    The calendar to which to set the time
     * @param hour        The hour to set
     * @param minute      The minute to set
     * @param second      The second to set
     * @param millisecond The millisecond to set
     */
    public static void setTime(Calendar calendar, int hour, int minute, int second, int millisecond) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
    }

    private static class FilesCompare implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return (int) (((File) lhs).lastModified() - ((File) rhs).lastModified());
        }

    }

    public static String msToHours(long ms) {
        final long MILLIS_PER_SECOND = 1000;
        final long SECONDS_PER_HOUR = 3600;
        final long SECONDS_PER_MINUTE = 60;

        long deltaSeconds = ms / MILLIS_PER_SECOND;
        long deltaHours = deltaSeconds / SECONDS_PER_HOUR;
        long leftoverSeconds = deltaSeconds % SECONDS_PER_HOUR;
        long deltaMinutes = leftoverSeconds / SECONDS_PER_MINUTE;

        return deltaHours + ":" + deltaMinutes;
    }

    // Extrait une sous chaine de string
    public static String extractString(String src, String debut, String fin) {
        return extractString(src, debut, fin, 0);
    }

    public static String extractString(String src, String debut, String fin, int startIndex) {
        int i = 0;
        int j = 0;

        i = src.indexOf(debut, startIndex);
        if (i == -1) {
            return null;
        }
        j = src.indexOf(fin, i);
        if (j == -1) {
            return null;
        }

        String tmp = null;
        if (i < j + fin.length()) // tmp = src.substring(i, j - 1);
        {
            tmp = src.substring(i, j + fin.length());
        }

        return tmp;
    }

    public static String getWindowId(String location) {
        // Extrait le code 3 chiffre window id de l'url
        String cible = "windowId=(\\w{3})";
        Pattern regex1 = Pattern.compile(cible);
        Matcher result1 = regex1.matcher(location);

        if (result1.find()) {
            return result1.group(1);
        } else {
            return null;
        }
    }

    public static int countEvents(String ics) {
        // Extrait le code 3 chiffre window id de l'url
        String cible = "BEGIN:VEVENT";
        Pattern regex1 = Pattern.compile(cible);
        Matcher result1 = regex1.matcher(ics);
        int i = 0;
        while (result1.find()) {
            i++;
        }
        return i;
    }

    public static String convertMinutesToHoursMinutes(int timeInMinutes) {
        int h = timeInMinutes / 60;
        int m = timeInMinutes % 60;

        return h + "h" + (m < 10 ? "0" + m : m);
    }

    public static boolean deleteDirectory(File path) {
        boolean resultat = true;

        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    resultat &= deleteDirectory(files[i]);
                } else {
                    resultat &= files[i].delete();
                }
            }
        }
        resultat &= path.delete();
        return resultat;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    static public int saveToFile(String source, String path, String encoding) {
        Writer out;
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(path);
            if (encoding.equals("")) {
                out = new OutputStreamWriter(fos);
            } else {
                out = new OutputStreamWriter(fos, encoding);
            }
            out.write(source);
            out.close();
            fos.close();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
