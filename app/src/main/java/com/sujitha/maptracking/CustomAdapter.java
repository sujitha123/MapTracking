package com.sujitha.maptracking;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;


public class CustomAdapter extends ArrayAdapter<UserLocDataModel> {

    private ArrayList<UserLocDataModel> dataSet;
    Context mContext;

    private static class ViewHolder {
        AppCompatTextView latitude;
        AppCompatTextView longitude;
        AppCompatTextView timeStamp;
    }

    public CustomAdapter(ArrayList<UserLocDataModel> data, Context context) {
        super(context, R.layout.location_list_item, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserLocDataModel dataModel = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.location_list_item, parent, false);
            viewHolder.latitude = (AppCompatTextView) convertView.findViewById(R.id.location_list_lati_id);
            viewHolder.longitude = (AppCompatTextView) convertView.findViewById(R.id.location_list_longi_id);
            viewHolder.timeStamp = (AppCompatTextView) convertView.findViewById(R.id.location_list_time_stamp_id);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.latitude.setText("Latitude: " + dataModel.getLatitude());
        viewHolder.longitude.setText("Longitude: " + dataModel.getLongitude());
        viewHolder.timeStamp.setText("~" + dataModel.getUnixTimeStamp());
        return convertView;
    }
}


