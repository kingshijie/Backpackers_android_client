package com.kingshijie.backpackers;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.kingshijie.backpackers.bean.Event;
import com.kingshijie.backpackers.map.Map;
import com.kingshijie.backpackers.util.SearchResultAdapter;

public class EventList extends ListActivity {
	private Button show_in_map;
	private Button publishevent;
	private ArrayList<Event> events;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventlist);

		Bundle bdl = getIntent().getExtras();
		events = bdl.getParcelableArrayList("GPOINT");

		show_in_map = (Button) findViewById(R.id.show_in_map);
		show_in_map.setOnClickListener(new goMap());

		publishevent = (Button)findViewById(R.id.publishevent);
		publishevent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EventList.this, PublishEvent.class);
				startActivity(intent);
			}
		});

		// 使用自定义的adapter
		ListAdapter adapter = new SearchResultAdapter(this,
				android.R.layout.two_line_list_item, events);

		setListAdapter(adapter);

		// 设置单击监听
		ListView lv = getListView();

		lv.setOnItemClickListener(new clickItem());

	}

	/**
	 * 单击某个条目的事件
	 * 
	 * @author aaron
	 * 
	 */
	private class clickItem implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			long eventId = events.get(position).getId();
			Intent intent = new Intent(EventList.this, EventActivity.class);
			Bundle bdl = new Bundle();
			bdl.putLong("event_id", eventId);
			intent.putExtras(bdl);
			startActivity(intent);
		}

	}

	/**
	 * 单击进入地图察看
	 * 
	 * @author aaron
	 * 
	 */
	private class goMap implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(EventList.this, Map.class);
			intent.putExtras(getIntent().getExtras());
			startActivity(intent);
		}

	}

}
