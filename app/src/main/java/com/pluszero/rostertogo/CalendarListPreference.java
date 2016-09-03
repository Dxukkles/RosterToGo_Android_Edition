package com.pluszero.rostertogo;

import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.HashMap;

public class CalendarListPreference extends MultiSelectListPreference {

    private SyncPlanning syncPlanning;

    public CalendarListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        syncPlanning = new SyncPlanning(context);

        String[] items = syncPlanning.getMapCalendars().keySet().toArray(new String[0]);

        if (items.length == 0) {
            setDialogTitle("Attention");
            setDialogMessage("- Soit l'adresse mail du compte Google n'a pas été renseignée\n\n- Soit elle vient juste d'être renseignée et il est nécessaire de quitter la page de préférences pour qu'elle soit prise en compte");
        }
        String[] values = items.clone();
        for (String s : values) {
            s = s + "_value";
        }
        setEntries(items);
        setEntryValues(values);
    }
}