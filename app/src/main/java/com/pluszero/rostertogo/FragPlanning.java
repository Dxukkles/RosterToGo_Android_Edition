package com.pluszero.rostertogo;

/**
 * Created by Cyril on 05/05/2016.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pluszero.rostertogo.model.PlanningEvent;
import com.pluszero.rostertogo.model.PlanningModel;


public class FragPlanning extends Fragment {

    private ListView listView;
    private PlanningAdapter adapter;
    private PlanningModel model;

    public FragPlanning() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_planning, container, false);
        listView = (ListView) rootView.findViewById(R.id.listView);
        this.setHasOptionsMenu(true);
        updateList();
        return rootView;
    }

    public void updateList() {
        if (model.getAlEvents() != null) {
            adapter = new PlanningAdapter(getActivity(), R.layout.frag_planning_item, model.getAlEvents());
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialFragDetails dialog;
                dialog = DialFragDetails.newInstance(buildDetails(model.getAlEvents().get(position)));
                dialog.show(getFragmentManager(), "details");
            }
        });
    }

    private String buildDetails(PlanningEvent pe) {
        // construct the content
        String nl = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT) || pe.getCategory().equals(PlanningEvent.CAT_DEAD_HEAD)) {
            if (pe.getAirportOrig() != null) {
                sb.append(getString(R.string.departure)).append(nl);
                sb.append(pe.getAirportOrig().city.toUpperCase()).append(" / ");
                sb.append(pe.getAirportOrig().name).append(nl);
                sb.append(pe.getAirportOrig().country).append(nl).append(nl);
            }
            if (pe.getAirportDest() != null) {
                sb.append(getString(R.string.arrival)).append(nl);
                sb.append(pe.getAirportDest().city.toUpperCase()).append(" / ");
                sb.append(pe.getAirportDest().name).append(nl);
                sb.append(pe.getAirportDest().country).append(nl).append(nl);
            }
        }
        if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
            sb.append(getString(R.string.function)).append(pe.getFunction());
            sb.append(nl);
            sb.append(getString(R.string.flight_time));
            sb.append(Utils.convertMinutesToHoursMinutes(pe.getBlockTime()));
        } else {
            sb.append(getString(R.string.duration));
            sb.append(Utils.convertMinutesToHoursMinutes(pe.getBlockTime()));
        }

        sb.append(nl).append(nl);
        if (!pe.getCrew().equals("")) {
            sb.append(pe.getCrew()).append(nl).append(nl);
        }
        if (!pe.getTraining().equals("")) {
            sb.append(pe.getTraining()).append(nl).append(nl);
        }
        if (!pe.getRemark().equals("")) {
            sb.append(pe.getRemark()).append(nl).append(nl);
        }
        if (!pe.getHotelData().equals("")) {
            sb.append(getString(R.string.hotel)).append(nl);
            sb.append(pe.getHotelData()).append(nl).append(nl);
        }

        return sb.toString();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frag_planning_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_figures:
                DialFragDetails dialog;
                dialog = DialFragDetails.newInstance(model.getActivityFigures().buildHoursSheet());
                dialog.show(getFragmentManager(), "details");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setData(PlanningModel model) {
        this.model = model;
    }
}
