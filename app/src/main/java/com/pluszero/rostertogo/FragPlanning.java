package com.pluszero.rostertogo;

/**
 * Created by Cyril on 05/05/2016.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pluszero.rostertogo.model.PlanningEvent;
import com.pluszero.rostertogo.model.PlanningModel;

import java.util.List;


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

        updateList();
        return rootView;
    }

    public void updateList() {
        if (model.getAlEvents() != null)
            adapter = new PlanningAdapter(getActivity(), R.layout.frag_planning_item, model.getAlEvents());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DialFragActivityDetails dialog;
                dialog = DialFragActivityDetails.newInstance(buildDetails(model.getAlEvents().get(position)));
                dialog.show(getFragmentManager(), "details");
            }
        });
    }

    private String buildDetails(PlanningEvent pe) {
        // construct the content
        String nl = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();

        if (pe.getCategory().equals(PlanningEvent.CAT_FLIGHT)) {
            sb.append("Fonction : ").append(pe.getFunction());
            sb.append(nl);
            sb.append("Temps de vol : ");
            sb.append(Utils.convertMinutesToHoursMinutes(pe.getBlockTime()));
        } else {
            sb.append("Durée : ");
            sb.append(Utils.convertMinutesToHoursMinutes(pe.getBlockTime()));
        }
        sb.append(nl).append(nl);
        sb.append(pe.getCrew());

        return sb.toString();
    }

    public void setData(PlanningModel model) {
        this.model = model;
    }
}
