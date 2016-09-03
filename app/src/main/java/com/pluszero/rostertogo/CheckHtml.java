package com.pluszero.rostertogo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckHtml {

    private static final int EVENT_ERROR = -1;


    public static String getViewState(String htmlContent) {
        String cible = "\"([-\\d]+:[-\\d]+)\"";
        Pattern regex1 = Pattern.compile(cible);
        Matcher result1 = regex1.matcher(htmlContent);

        if (result1.find()) {
            return result1.group(1);
        } else
            return null;
    }

    public static boolean isRosterNotSigned(String htmlContent) {
        String cible = "Please validate your planning</a>";
        Pattern regex1 = Pattern.compile(cible);
        Matcher result1 = regex1.matcher(htmlContent);

        return result1.find();
    }

    public static boolean isChangesNotSigned(String htmlPlanning) {
        String cible = "Please check your planning modifications</a>";
        Pattern regex1 = Pattern.compile(cible);
        Matcher result1 = regex1.matcher(htmlPlanning);

        return result1.find();
    }

}
