package com.pluszero.rostertogo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class AvailCalendarsAdapter extends ArrayAdapter<String> {

    private Context c;

    public AvailCalendarsAdapter(Context c, int resource, String[] objects) {
        super(c, resource, objects);
        this.c = c;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        String name = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) c
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.avail_calendars_row, null);
        }

        CheckBox cbxName = (CheckBox) convertView.findViewById(R.id.cbxCalendar);
        cbxName.setText(name);

        return convertView;
    }
}
