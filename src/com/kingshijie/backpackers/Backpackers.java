package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.kingshijie.backpackers.bean.Event;
import com.kingshijie.backpackers.bean.User;
import com.kingshijie.backpackers.map.BasicLocatorActivity;
import com.kingshijie.backpackers.map.Map;
import com.kingshijie.backpackers.push.PushService;
import com.kingshijie.backpackers.util.DistanceCalculator;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.ServerHelper;

public class Backpackers extends BasicLocatorActivity {
	public static final double DEFAULT_DISTANCE = 3;

	private Button mContributeBtn;
	private Button mSearchBtn;
	private Button mUserInfoBtn;
	private Button mEventBtn;
	private Button show_in_map;
	private ToggleButton mOnTravel;
	private ToggleButton mOnListening;
	private ProgressDialog mLoadingDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 继承父类
		mWhereTextView = (TextView) findViewById(R.id.display_location);

		show_in_map = (Button) findViewById(R.id.show_in_map);
		show_in_map.setOnClickListener(new goMap());

		mUserInfoBtn = (Button) findViewById(R.id.user_info);
		mContributeBtn = (Button) findViewById(R.id.contribute);
		mSearchBtn = (Button) findViewById(R.id.search);
		mEventBtn = (Button) findViewById(R.id.event);

		mUserInfoBtn.setOnClickListener(new userInfoOnClick());
		mContributeBtn.setOnClickListener(new contributeOnClick());
		mSearchBtn.setOnClickListener(new searchOnClick());
		mEventBtn.setOnClickListener(new eventOnClick());

		mOnTravel = (ToggleButton) findViewById(R.id.onTravel);
		mOnListening = (ToggleButton) findViewById(R.id.onListening);

		SharedPreferences sharedPreferences = getSharedPreferences("settings",
				Context.MODE_PRIVATE);

		mOnTravel.setChecked(sharedPreferences.getBoolean("on_travel", false));
		mOnListening.setChecked(sharedPreferences.getBoolean("on_listening",
				true));

		if (sharedPreferences.getBoolean("on_listening", true)) {
			//打开接受push的service
			String deviceID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID); 
			Editor editor = getSharedPreferences(PushService.TAG, MODE_PRIVATE).edit();
	    	editor.putString(PushService.PREF_DEVICE_ID, deviceID);
	    	editor.commit();
			PushService.actionStart(getApplicationContext());
		}
		mLoadingDialog = new ProgressDialog(Backpackers.this);
		mLoadingDialog.setCancelable(true);
		mLoadingDialog.setMessage("搜索中，请稍后...");

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
			// 显示当前的周围的背包客
			new FetchNearUserTask().execute();
		}

	}

	private class eventOnClick implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			new FetchEventsTask().execute();
		}

	}

	private class userInfoOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Backpackers.this, UserInfo.class);
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

	/**
	 * [正在旅行]按键的事件
	 * 
	 * @param v
	 */
	@SuppressWarnings("unchecked")
	public void onTravelClicked(View v) {
		Toast.makeText(Backpackers.this, "正在设置...", Toast.LENGTH_SHORT).show();
		// Perform action on clicks
		boolean onTravel = ((ToggleButton) v).isChecked();
		// 写入preference
		SharedPreferences sharedPreferences = getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("on_travel", onTravel);
		editor.commit();
		// 传给网络
		List<NameValuePair> params = new ArrayList<NameValuePair>(10);
		params.add(new BasicNameValuePair("on_travel", (onTravel ? "1" : "0")));
		new SetParamTask().execute(params);
	}

	/**
	 * 接受事件按键的事件
	 * 
	 * @param v
	 */
	@SuppressWarnings("unchecked")
	public void onListeningClicked(View v) {
		// Perform action on clicks
		boolean onListening = ((ToggleButton) v).isChecked();
		//根据用户希望打开或关闭service
		if(onListening){
			//打开service
			PushService.actionStart(getApplicationContext());
		}else{
			//关闭service
			PushService.actionStop(getApplicationContext());
		}
		// 写入preference
		SharedPreferences sharedPreferences = getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("on_listening", onListening);
		editor.commit();
		// 传给网络
		List<NameValuePair> params = new ArrayList<NameValuePair>(10);
		params.add(new BasicNameValuePair("on_listening", (onListening ? "1"
				: "0")));
		new SetParamTask().execute(params);
	}

	/**
	 * 获取周围的事件
	 * 
	 * @author aaron
	 * 
	 */
	private class FetchEventsTask extends
			AsyncTask<Void, Void, ArrayList<Event>> {

		/*
		 * 显示等待的dialog
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mLoadingDialog.show();
		}

		/*
		 * 后台操作，包括网络传输，数据封装
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected ArrayList<Event> doInBackground(Void... arg0) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			// 设置服务器端对应的Action
			HttpConnector.setAction(params, ServerHelper.eventController,
					ServerHelper.fetchNearEventAction);
			if (!HttpConnector.setAuth(params, Backpackers.this)) {
				return null;
			}
			// 默认范围距离
			double dis = DEFAULT_DISTANCE;
			// 设置参数
			params.add(new BasicNameValuePair("x", String.valueOf(mX)));
			params.add(new BasicNameValuePair("y", String.valueOf(mY)));
			params.add(new BasicNameValuePair("range", String
					.valueOf(DistanceCalculator.getRange(dis))));
			JSONObject jsonResponse = HttpConnector.doPost(uri, params);
			try {
				if (jsonResponse != null) {
					JSONArray jsonArray = jsonResponse.getJSONArray("data");
					Log.v("result num", String.valueOf(jsonArray.length()));
					ArrayList<Event> events = new ArrayList<Event>(100);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject one = jsonArray.getJSONObject(i);
						long id = one.getLong("event_id");
						String name = one.getString("name");
						double x = one.getDouble("x");
						double y = one.getDouble("y");
						double distance = DistanceCalculator.GetCoarseDistance(
								mX, mY, x, y);
						if (distance <= dis) {
							Event event = new Event();
							event.setId(id);
							event.setX(x);
							event.setY(y);
							event.setName(name);
							events.add(event);
						}
					}
					return events;
				} else {
					return null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		/*
		 * 是否跳转到结果list
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(ArrayList<Event> result) {
			mLoadingDialog.dismiss();
			if (result != null) {
				Intent intent = new Intent(Backpackers.this, EventList.class);
				Bundle bdl = new Bundle();
				// Place类实现Parcelable接口
				bdl.putParcelableArrayList("GPOINT", result);
				intent.putExtras(bdl);
				startActivity(intent);
			} else {
				Toast.makeText(Backpackers.this, "没有你要找的内容诶", Toast.LENGTH_LONG)
						.show();
			}
		}

	}

	/**
	 * 设置用户的参数
	 * 
	 * @author aaron
	 * 
	 */
	private class SetParamTask extends
			AsyncTask<List<NameValuePair>, Void, String> {

		/*
		 * 后台操作，包括网络传输，数据封装
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(List<NameValuePair>... arg) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			// 设置服务器端对应的Action
			List<NameValuePair> params = arg[0];
			HttpConnector.setAction(params, ServerHelper.userController,
					ServerHelper.setParamsAction);
			if (!HttpConnector.setAuth(params, Backpackers.this)) {
				return null;
			}
			// 设置参数
			JSONObject jsonResponse = HttpConnector.doGet(uri, params);
			try {
				if (jsonResponse != null
						&& jsonResponse.getString("stat").equals("success")) {

					return "success";
				} else {
					return null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		/*
		 * 显示结果
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(String result) {
			if (result != null) {
				Toast.makeText(Backpackers.this, "设置成功", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(Backpackers.this, "设置失败", Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	/**
	 * 获取周围的背包客
	 * 
	 * @author aaron
	 * 
	 */
	private class FetchNearUserTask extends
			AsyncTask<Void, Void, ArrayList<User>> {

		/*
		 * 显示等待的dialog
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mLoadingDialog.show();
		}

		/*
		 * 后台操作，包括网络传输，数据封装
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected ArrayList<User> doInBackground(Void... arg0) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			// 设置服务器端对应的Action
			HttpConnector.setAction(params, ServerHelper.userController,
					ServerHelper.fetchNearUserAction);
			if (!HttpConnector.setAuth(params, Backpackers.this)) {
				return null;
			}
			// 默认范围距离
			double dis = DEFAULT_DISTANCE;
			// 设置参数
			params.add(new BasicNameValuePair("x", String.valueOf(mX)));
			params.add(new BasicNameValuePair("y", String.valueOf(mY)));
			params.add(new BasicNameValuePair("range", String
					.valueOf(DistanceCalculator.getRange(dis))));
			JSONObject jsonResponse = HttpConnector.doPost(uri, params);
			try {
				if (jsonResponse != null) {
					JSONArray jsonArray = jsonResponse.getJSONArray("data");
					Log.v("result num", String.valueOf(jsonArray.length()));
					ArrayList<User> users = new ArrayList<User>(100);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject one = jsonArray.getJSONObject(i);
						long id = one.getLong("user_id");
						String username = one.getString("username");
						int sex = one.getInt("sex");
						double x = one.getDouble("x");
						double y = one.getDouble("y");
						double distance = DistanceCalculator.GetCoarseDistance(
								mX, mY, x, y);
						if (distance <= dis) {
							User user = new User();
							user.setId(id);
							user.setX(x);
							user.setY(y);
							user.setName(username);
							user.setSex(sex);
							users.add(user);
						}
					}
					return users;
				} else {
					return null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		/*
		 * 是否跳转到结果list
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(ArrayList<User> result) {
			mLoadingDialog.dismiss();
			if (result != null) {
				Intent intent = new Intent(Backpackers.this, Map.class);
				Bundle bdl = new Bundle();
				// Place类实现Parcelable接口
				bdl.putParcelableArrayList("GPOINT", result);
				intent.putExtras(bdl);
				startActivity(intent);
			} else {
				Toast.makeText(Backpackers.this, "没有你要找的内容诶", Toast.LENGTH_LONG)
						.show();
			}
		}

	}

}