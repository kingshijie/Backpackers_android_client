package com.kingshijie.backpackers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
		// 获取sharedPreferences
		SharedPreferences sharedPreferences = getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		//如果需要登录
		if(!(sharedPreferences.getBoolean("auto_login", false) && sharedPreferences.getLong("user_id", 0) != 0)){
			//调用登录activity
			Intent intent = new Intent(this, Login.class);
			startActivity(intent);
		}
		
		mUserInfoBtn = (Button)findViewById(R.id.user_info);
		mContributeBtn = (Button)findViewById(R.id.contribute);
		mSearchBtn = (Button)findViewById(R.id.search);
		
		mUserInfoBtn.setOnClickListener(new userInfoOnClick());
		mContributeBtn.setOnClickListener(new contributeOnClick());
		mSearchBtn.setOnClickListener(new searchOnClick());
	}
	
	private class userInfoOnClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class contributeOnClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Backpackers.this,Contribute.class);
			startActivity(intent);
		}
		
	}
	
	private class searchOnClick implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}

}