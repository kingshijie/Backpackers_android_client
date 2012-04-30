package com.kingshijie.backpackers;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;

import com.kingshijie.backpackers.util.Place;
import com.kingshijie.backpackers.util.SearchResultAdapter;

public class ItemList extends ListActivity {
	
	private Button show_in_map;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itemlist);
		Bundle bdl = getIntent().getExtras();
		ArrayList<Place> places = bdl.getParcelableArrayList("places");
		
		show_in_map = (Button)findViewById(R.id.show_in_map);
		show_in_map.setOnClickListener(new goMap());

		//使用自定义的adapter
		ListAdapter adapter = new SearchResultAdapter(this,
				android.R.layout.two_line_list_item, places);
		
		setListAdapter(adapter);

	}
	
	private class goMap implements OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(ItemList.this,Map.class);
			intent.putExtras(getIntent().getExtras());
			startActivity(intent);
		}
		
	}

}
