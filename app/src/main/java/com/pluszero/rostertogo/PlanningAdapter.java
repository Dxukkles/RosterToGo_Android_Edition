package com.pluszero.rostertogo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pluszero.rostertogo.model.PlanningEvent;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Cyril on 05/05/2016.
 */
public class PlanningAdapter extends ArrayAdapter<PlanningEvent> {

    private ArrayList<PlanningEvent> items;

    private final DateFormatSymbols dfs = new DateFormatSymbols();
    private final String[] shortDays = new String[]{"", "Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa"};
    private final String[] shortMonths = new String[]{"Jan", "Fév", "Mar", "Avr", "Mai", "Jui", "Jul", "Aou", "Sep", "Oct", "Nov", "Déc"};
    private final SimpleDateFormat sdfDate;
    private final SimpleDateFormat sdfHour;

    public PlanningAdapter(Context context, int resource, ArrayList<PlanningEvent> items) {
        super(context, resource, items);
        this.items = items;

        dfs.setShortWeekdays(shortDays);
        dfs.setShortMonths(shortMonths);
        sdfDate = new SimpleDateFormat("E dd MMM", dfs);
        sdfHour = new SimpleDateFormat("HH:mm");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this
                    .getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            v = vi.inflate(R.layout.frag_planning_item, null);
        }

        PlanningEvent event = items.get(position);
        if (event != null) {
            TextView tvDate = (TextView) v.findViewById(R.id.tvDate);
            TextView tvSummary = (TextView) v.findViewById(R.id.tvSummary);
            // cel background
            // flight activities
            if (event.getCategory().contains(PlanningEvent.CAT_FLIGHT)) {
                tvSummary.setBackgroundResource(R.color.colorFlight);
            }// dead heading
            else if (event.getCategory().contains(PlanningEvent.CAT_DEAD_HEAD)) {
                tvSummary.setBackgroundResource(R.color.colorDeadHead);
            } // ground activities
            else if (event.getCategory().contains(PlanningEvent.CAT_SYND)) {
                tvSummary.setBackgroundResource(R.color.colorSyndicat);
            } else if (event.getCategory().contains(PlanningEvent.CAT_HOTEL)) {
                tvSummary.setBackgroundResource(R.color.colorHotel);
            } else if (event.getCategory().contains(PlanningEvent.CAT_SIMU)) {
                tvSummary.setBackgroundResource(R.color.colorGroundActivity);
            } else if (event.getCategory().contains(PlanningEvent.CAT_MEDICAL)) {
                tvSummary.setBackgroundResource(R.color.colorGroundActivity);
            } else if (event.getCategory().contains(PlanningEvent.CAT_SIM_C1)) {
                tvSummary.setBackgroundResource(R.color.colorGroundActivity);
            } else if (event.getCategory().contains(PlanningEvent.CAT_SIM_C2)) {
                tvSummary.setBackgroundResource(R.color.colorGroundActivity);
            } else if (event.getCategory().contains(PlanningEvent.CAT_SIM_E1)) {
                tvSummary.setBackgroundResource(R.color.colorGroundActivity);
            } else if (event.getCategory().contains(PlanningEvent.CAT_SIM_E2)) {
                tvSummary.setBackgroundResource(R.color.colorGroundActivity);
            } else if (event.getCategory().contains(PlanningEvent.CAT_SIM_LOE)) {
                tvSummary.setBackgroundResource(R.color.colorGroundActivity);
            } // days off
            else if (event.getCategory().contains(PlanningEvent.CAT_OFF_DDA)) {
                tvSummary.setBackgroundResource(R.color.colorDayOff);
            } else if (event.getCategory().contains(PlanningEvent.CAT_OFF)) {
                tvSummary.setBackgroundResource(R.color.colorDayOff);
            } // vacations
            else if (event.getCategory().contains(PlanningEvent.CAT_VACATION)) {
                tvSummary.setBackgroundResource(R.color.colorVacation);
            } // illness
            else if (event.getCategory().contains(PlanningEvent.CAT_ILLNESS)) {
                tvSummary.setBackgroundResource(R.color.colorIllness);
            } // blanc
            else {
                tvSummary.setBackgroundResource(R.color.colorBlanc);
            }

            // color weekends in light gray
            if (event.getGcBegin().get(Calendar.DAY_OF_WEEK) == 1 || event.getGcBegin().get(Calendar.DAY_OF_WEEK) == 7) {
                tvDate.setBackgroundResource(R.color.colorWeekEnd);
            } else {
                tvDate.setBackgroundResource(R.color.colorWeek);
            }
            // display date for first event of day
            if (event.isFirstEventOfDay() || event.isDayEvent())
                tvDate.setText(sdfDate.format(event.getGcBegin().getTime()));
            else
                tvDate.setText("");

            tvSummary.setText(generateCellLabel(event));
        }

        return v;
    }

    private String generateCellLabel(PlanningEvent item) {
        StringBuilder sb = new StringBuilder();

        if (item.isDayEvent()) {
            sb.append(item.getSummary());
            return sb.toString();
        }

        if (item.getCategory().equals(PlanningEvent.CAT_FLIGHT) || item.getCategory().equals(PlanningEvent.CAT_DEAD_HEAD)) {
            sb.append(item.getFltNumber()).append(" ");
            sb.append(sdfHour.format(item.getGcBegin().getTime())).append(" ");
            if (item.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
                sb.append(item.getIataOrig()).append(" - ");
            } else {
                sb.append(item.getIataOrig()).append(" * ");
            }

            sb.append(item.getIataDest()).append(" ");
            sb.append(sdfHour.format(item.getGcEnd().getTime()));
            if (item.getLagDest() != PlanningEvent.NO_LAG_AVAIL) {
                sb.append(" (");
                if (item.getLagDest() < 0) {
                    sb.append("-").append(item.getLagDest());
                } else {
                    sb.append("+").append(item.getLagDest());
                }
                sb.append(")");
            }
            return sb.toString();
        }

        sb.append(sdfHour.format(item.getGcBegin().getTime())).append(" ");
        sb.append(item.getSummary()).append(" ");
        sb.append(sdfHour.format(item.getGcEnd().getTime()));
        return sb.toString();
    }

}
