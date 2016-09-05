/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pluszero.rostertogo;

/**
 * @author Cyril
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfManager {

    private PDFParser parser;
    private PDFTextStripper pdfStripper;
    private PDDocument pdDoc;
    private COSDocument cosDoc;

    private String text;
    private ArrayList<String> alEvents;
    private ArrayList<String> alHotels;
    private HashMap<String, String> trigraphs;
    private DateFormatSymbols dfs = new DateFormatSymbols();
    private String[] shortDays = new String[]{"", "dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam."};

    SimpleDateFormat sdf;

    private String newline = System.getProperty("line.separator");

    // KEEP FOR OFFLINE TESTING
    public PdfManager(File file, HashMap<String, String> trigraphs) {
        sdf = new SimpleDateFormat("E dd/MM/yyyy", dfs);
        dfs.setShortWeekdays(shortDays);
        this.trigraphs = trigraphs;

        try {
            ToText(file);
            splitPdf();
            alHotels = buildHotelDetailsList(text);
        } catch (IOException ex) {
        }

    }

    public PdfManager(InputStream is, HashMap<String, String> trigraphs) {
        sdf = new SimpleDateFormat("E dd/MM/yyyy", dfs);
        dfs.setShortWeekdays(shortDays);
        this.trigraphs = trigraphs;

        try {
            ToText(is);
            splitPdf();
            alHotels = buildHotelDetailsList(text);
        } catch (IOException ex) {
        }
    }

    private void ToText(File file) throws IOException {
        this.pdfStripper = null;
        this.pdDoc = null;
        this.cosDoc = null;

        parser = new PDFParser(file); // for pfdBox 1.8, as 2.0 not yet supported in Android

        parser.parse();
        cosDoc = parser.getDocument();
        pdfStripper = new PDFTextStripper();
        pdDoc = new PDDocument(cosDoc);
        pdDoc.getNumberOfPages();
        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(pdDoc.getNumberOfPages());
        text = pdfStripper.getText(pdDoc);
        pdDoc.close();
    }

    private void ToText(InputStream is) throws IOException {
        this.pdfStripper = null;
        this.pdDoc = null;
        this.cosDoc = null;

        parser = new PDFParser(is); // for PdfBox 1.8 as 2.0 not yet supported in Android

        parser.parse();
        cosDoc = parser.getDocument();
        pdfStripper = new PDFTextStripper();
        pdDoc = new PDDocument(cosDoc);
        pdDoc.getNumberOfPages();
        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(pdDoc.getNumberOfPages());
        text = pdfStripper.getText(pdDoc);
        pdDoc.close();
    }

    /**
     * extract each part of the pdft text between two date patterns and add it
     * to an array of strings
     */
    private void splitPdf() {
        // first build an array containing indices of each date pattern
        ArrayList<Integer> alIndices = new ArrayList<>();
        String regex = "[a-z]{3}\\. [0-9]{2}/[0-9]{2}/[0-9]{4}";
        // search date
        Pattern pattern = Pattern.compile(regex);
        Matcher result = pattern.matcher(text);
        // create an array with all occurrences
        while (result.find()) {
            alIndices.add(result.start());
        }
        // then build an array of string, each string is the part between two date patterns
        String part;
        alEvents = new ArrayList<>();

        for (int i = 0; i < alIndices.size(); i++) {
            int begin = alIndices.get(i);
            int end;
            if (i < alIndices.size() - 1) {
                end = alIndices.get(i + 1);
            } else {
                end = text.length() - 1;
            }

            part = text.substring(begin, end);
            alEvents.add(part);
        }
    }

    /**
     * Scan the array of strings for the matching date, and get the crew
     *
     * @param cal the date of the activity
     * @return the crew of the flight
     */
    public String findCrew(GregorianCalendar cal) {
        for (int i = 0; i < alEvents.size(); i++) {
            String s = alEvents.get(i);
            // detect if part is the matching date
            if (s.contains(sdf.format(cal.getTime()))) {
                // search for crew info
                int idxStart = s.indexOf("Pilot");
                int idxEnd = s.indexOf("Check In");
                if (idxStart > -1 && idxEnd > -1) {
                    return s.substring(idxStart, idxEnd).trim();
                }
            }
        }
        return null;
    }

    /**
     * Scan the array of strings for the matching date, and get the sim crew
     *
     * @param cal the date of the activity
     * @return the crew/participant of the activity
     */
    public String findTraining(GregorianCalendar cal) {
        // get each part of pdf between indices and parse it
        for (int i = 0; i < alEvents.size(); i++) {
            String s = alEvents.get(i);
            // detect if part is the matching date
            if (s.contains(sdf.format(cal.getTime()))) {
                if (s.contains("Ground Act.") && s.toLowerCase().contains("simu")) {
                    return extractTraining(s);
                }
            }
        }
        return null;
    }

    /**
     * Scan the array of strings for the matching date, departure and
     * destination, and get the remarks
     *
     * @param cal the date of the activity
     * @param dep the airport of departure
     * @param arr the airport of arrival
     * @return the remarks of the activity
     */
    public String findRemarks(GregorianCalendar cal, String dep, String arr) {
        // get each part of pdf between indices and parse it
        for (int i = 0; i < alEvents.size(); i++) {
            String s = alEvents.get(i);
            // detect if part is the matching date
            if (s.contains(sdf.format(cal.getTime()))) {
                if (s.contains("Duty Flight") && s.contains(dep + "-" + arr)) {
                    return extractRemarks(s);
                }
            }
        }
        return null;
    }

    /**
     * Scan the array of strings for the matching date, and get the remarks
     *
     * @param cal the date of the activity
     * @return the remarks of the activity
     */
    public String findRemarks(GregorianCalendar cal) {
        // get each part of pdf between indices and parse it
        for (int i = 0; i < alEvents.size(); i++) {
            String s = alEvents.get(i);
            // detect if part is the matching date
            if (s.contains(sdf.format(cal.getTime()))) {
                return extractRemarks(s);
            }
        }
        return null;
    }

    public String findHotelDetails(GregorianCalendar cal) {
        if (alHotels == null || alHotels.isEmpty()) {
            return null;
        }
        for (int i = 0; i < alEvents.size(); i++) {
            String s = alEvents.get(i);
            // detect if part is the matching date
            if (s.contains(sdf.format(cal.getTime()))) {
                // search for hotel info
                for (String hotel : alHotels) {
                    // search for first 10 chars cause hotel details
                    //  include telephone number and adress
                    if (s.contains(hotel.substring(0, 10))) {
                        return hotel;
                    }
                }
            }
        }
        return null;
    }

    private String extractTraining(String s) {

        // split the source in lines
        String[] array = s.split(newline);
        // find line number of "Ins."
        int begin = 0;
        int end = 0;

        if (s.contains("Ins.")) {
            for (int i = 0; i < array.length; i++) {
                if (array[i].contains("Ins.")) {
                    begin = i;
                    continue;
                }

                if (array[i].contains("Check In")) {
                    end = i;
                    break;
                }
            }
        } else if (s.contains("Tr.")) {
            for (int i = 0; i < array.length; i++) {
                if (array[i].contains("Tr.")) {
                    begin = i;
                    continue;
                }

                if (array[i].contains("Check In")) {
                    end = i;
                    break;
                }
            }
        } else {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        // add the line juste above "Ins."
        sb.append("Training : ").append(array[begin - 1].trim()).append(newline);
        // add the rest
        for (int i = begin; i < end; i++) {
            String decoded = decodeTrigraphInLine(array[i].trim());
            if (!decoded.equals("")) {
                sb.append(decoded).append(newline);
            }
        }
        return sb.toString();
    }

    private String extractRemarks(String s) {
        int begin = s.indexOf("Check In");

        if (begin != -1) {
            String target = "DUTY=[0-9]{1,2}:[0-9]{2}";
            Pattern regex = Pattern.compile(target);
            Matcher result = regex.matcher(s);
            if (result.find()) {
                int idx = s.indexOf(result.group(0)) + result.group(0).length();
                return Utils.splitTrim(s.substring(begin, idx), newline);
            }
        }
        return null;
    }

    private String decodeTrigraphInLine(String line) {
        String target = "[A-Z]{3}";
        Pattern regex = Pattern.compile(target);
        Matcher result = regex.matcher(line);

        if (result.find()) {
            String name = "Inconnu";
            if (trigraphs.containsKey(result.group(0))) {
                name = trigraphs.get(result.group(0));
            }
            StringBuilder sb = new StringBuilder(line);
            // if there is a descriptor ("Xxx :"), insert a new line after
            if (line.contains(":")) {
                sb.insert(line.indexOf(result.group(0)), newline);
            }
            sb.append(" - ").append(name);
            return sb.toString();
        }
        return "";
    }

    private ArrayList<String> buildHotelDetailsList(String src) {
        ArrayList<String> list = new ArrayList<>();
        String target = "Hotel Telephone Address";
        int idx = src.indexOf(target) + target.length() + newline.length();

        String[] lines = src.substring(idx).split(newline);
        for (int i = 0; i < lines.length; i++) {
            if (i == 0 && (lines[i].equals(" ") || lines[i].equals(""))) {
                continue;
            }
            // deal with page change
            if (lines[i].contains("Crew Roster")) {
                continue;
            }
            if (lines[i].contains("Schedule in")) {
                continue;
            }
            if (lines[i].contains("Licenced to")) {
                continue;
            }
            if (lines[i].contains("Printed on")) {
                continue;
            }
            if (lines[i].contains(" / Box ")) {
                continue;
            }
            // end of hotel details area
            if (i > 0 && (lines[i].equals(" ") || lines[i].equals(""))) {
                break;
            }
            if (lines[i].contains("Remarks")) {
                break;
            }
            if (lines[i].contains("Applicability Remark")) {
                break;
            }
            list.add(lines[i]);
        }

        return list;
    }
}
