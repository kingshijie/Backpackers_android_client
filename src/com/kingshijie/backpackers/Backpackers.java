package com.kingshijie.backpackers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.kingshijie.backpackers.util.DistanceCalculator;

public class Backpackers extends Activity {

	private Button mContributeBtn;
	private Button mSearchBtn;
	private Button mUserInfoBtn;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mUserInfoBtn = (Button)findViewById(R.id.user_info);
		mContributeBtn = (Button)findViewById(R.id.contribute);
		mSearchBtn = (Button)findViewById(R.id.search);
		
		mUserInfoBtn.setOnClickListener(new userInfoOnClick());
		mContributeBtn.setOnClickListener(new contributeOnClick());
		mSearchBtn.setOnClickListener(new searchOnClick());
		
 	}

	private class userInfoOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Backpackers.this, Map.class);
			startActivity(intent);
		}

	}

	private class contributeOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Backpackers.this, Contribute.class);
			startActivity(intent);
		}

	}

	private class searchOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Backpackers.this, Search.class);
			startActivity(intent);
		}

	}

}