package com.dongholab.ticketbot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import java.util.List;

public class TicketAdapter extends ArrayAdapter<String> {

    List<String> chartContents;
    Context context;
    int resource;

    public TicketAdapter(Context context, int resource, List<String> chartContents) {
        super(context, resource, chartContents);
        this.chartContents = chartContents;
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(resource, parent, false);
        return view;
    }
}