package com.kingshijie.backpackers.util;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class SearchResultAdapter extends ArrayAdapter<Place> {
	private ArrayList<Place> items;
	private Context context;

	public SearchResultAdapter(Context context, int textViewResourceId,
			ArrayList<Place> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(android.R.layout.two_line_list_item, null);
		}

		Place item = items.get(position);
		if (item != null) {
			TextView name = (TextView) view.findViewById(android.R.id.text1);
			TextView dis = (TextView) view.findViewById(android.R.id.text2);
			name.setText(item.getName());
			double distance = item.getDistance();
			if(distance > 10){
				dis.setText(String.format("%.1f公里", distance));
			}else{
				dis.setText(String.format("%d米", (int)(distance * 1000)));
			}
		}

		return view;
	}
}
