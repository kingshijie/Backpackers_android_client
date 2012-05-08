package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.kingshijie.backpackers.bean.Event;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.ServerHelper;

public class EventActivity extends Activity {
	private TextView name;
	private TextView destination;
	private TextView user;
	private TextView time;
	private TextView spot;
	private TextView contact;
	private TextView content;
	private ProgressDialog mLoadingDialog;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event);
		
		name = (TextView)findViewById(R.id.name);
		destination = (TextView)findViewById(R.id.destination);
		user = (TextView)findViewById(R.id.user);
		time = (TextView)findViewById(R.id.time);
		spot = (TextView)findViewById(R.id.spot);
		contact = (TextView)findViewById(R.id.contact);
		content = (TextView)findViewById(R.id.content);
		
		mLoadingDialog = new ProgressDialog(EventActivity.this);
		mLoadingDialog.setCancelable(true);
		mLoadingDialog.setMessage("正在获取数据，请稍后...");
		
		Bundle bdl = getIntent().getExtras();
		long eventId = bdl.getLong("event_id");
		
		new FetchEventTask().execute(eventId);
		
	}
	
	/**
	 * 完成数据的获取
	 * 
	 * @author aaron
	 * 
	 */
	private class FetchEventTask extends AsyncTask<Long, Void, Event> {

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
		protected Event doInBackground(Long... arg) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			// 设置服务器端对应的Action
			HttpConnector.setAction(params, ServerHelper.eventController, ServerHelper.fetchEventAction);
			if (!HttpConnector.setAuth(params, EventActivity.this)) {
				return null;
			}
			// 设置参数
			params.add(new BasicNameValuePair("id", String.valueOf(arg[0])));
			JSONObject jsonResponse = HttpConnector.doGet(uri, params);
			try {
				if (jsonResponse != null) {
					Event p = new Event();
					p.setId(jsonResponse.getLong("event_id"));
					p.setName(jsonResponse.getString("name"));
					p.setX(jsonResponse.getDouble("x"));
					p.setY(jsonResponse.getDouble("y"));
					p.setContact(jsonResponse.getString("contact"));
					p.setContent(jsonResponse.getString("content"));
					p.setDestination(jsonResponse.getString("destination"));
					p.setSpot(jsonResponse.getString("spot"));
					p.setTime(jsonResponse.getString("time"));
					p.setUser_id(jsonResponse.getLong("user_id"));
					p.setUsername(jsonResponse.getString("username"));
					return p;
				} else {
					return null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		protected void onPostExecute(Event result) {
			mLoadingDialog.dismiss();
			if (result != null) {
				// 给文本框赋值
				name.setText(result.getName());
				destination.setText(result.getDestination());
				user.setText(result.getUsername());
				time.setText(result.getTime());
				spot.setText(result.getSpot());
				contact.setText(result.getContact());
				content.setText(result.getContent());
			} else {
				Toast.makeText(EventActivity.this, "没有你要找的内容诶", Toast.LENGTH_LONG).show();
			}
		}

	}
	
}
