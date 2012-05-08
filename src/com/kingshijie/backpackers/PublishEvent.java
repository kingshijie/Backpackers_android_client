package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kingshijie.backpackers.map.BasicLocatorActivity;
import com.kingshijie.backpackers.util.DistanceCalculator;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.ServerHelper;

public class PublishEvent extends BasicLocatorActivity {
	private Button time;
	private TextView name;
	private TextView destination;
	private TextView spot;
	private TextView contact;
	private TextView content;
	private TextView displayDatetime;
	private int mDay;
	private int mMonth;
	private int mYear;
	private int mHour;
	private int mMinute;
	private String mDatetime;
	private Button publishBtn;
	private ProgressDialog mDialog;

	private final int PICK_DATETIME_DIALOG = 0;

	/*
	 * 初始化按键
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.publishevent);

		name = (TextView) findViewById(R.id.name);
		destination = (TextView) findViewById(R.id.destination);
		spot = (TextView) findViewById(R.id.spot);
		contact = (TextView) findViewById(R.id.contact);
		content = (TextView) findViewById(R.id.content);
		time = (Button) findViewById(R.id.time);
		publishBtn = (Button) findViewById(R.id.do_publish);
		displayDatetime = (TextView) findViewById(R.id.display_datetime);

		time.setOnClickListener(new showTimer());
		publishBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//启动task异步添加事件
				new publishTask().execute();
			}

		});

		mDialog = new ProgressDialog(PublishEvent.this);
		mDialog.setCancelable(true);
		mDialog.setMessage("正在提交，马上好哦");

	}

	private class showTimer implements OnClickListener {

		@Override
		public void onClick(View v) {
			showDialog(PICK_DATETIME_DIALOG);
		}

	}

	/*
	 * 打开dialog
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		AlertDialog.Builder builder;
		switch (id) {
		case PICK_DATETIME_DIALOG:
			builder = new AlertDialog.Builder(PublishEvent.this);
			LayoutInflater inflater = getLayoutInflater();
			final View layout = inflater
					.inflate(R.layout.dialog_datetime, null);
			builder.setTitle("设置日期与时间")
					.setView(layout)
					.setCancelable(true)
					.setPositiveButton("设置",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									DatePicker datePicker = (DatePicker) layout
											.findViewById(R.id.datePicker);
									TimePicker timePicker = (TimePicker) layout
											.findViewById(R.id.timePicker);
									mDay = datePicker.getDayOfMonth();
									mMonth = datePicker.getMonth() + 1;
									mYear = datePicker.getYear();
									mHour = timePicker.getCurrentHour();
									mMinute = timePicker.getCurrentMinute();
									mDatetime = String
											.format("%02d-%02d-%02d %02d:%02d:00",
													mYear, mMonth, mDay, mHour,
													mMinute);
									displayDatetime.setText(mDatetime);
									dialog.dismiss();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			dialog = builder.create();
			break;
		default:
			break;
		}
		return dialog;
	}

	private class publishTask extends AsyncTask<Void, Void, String> {

		/*
		 * 任务执行前显示dialog
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mDialog.show();
		}

		/*
		 * 发送POST请求并处理结果
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(Void... toWhom) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);

			String checkResult = checkForm(name, destination, displayDatetime,
					spot, contact, content);
			if (!checkResult.equals("success")) {
				return checkResult;
			}
			List<NameValuePair> params = new ArrayList<NameValuePair>(12);
			// 设置服务器端对应的Action
			// toWhom[0]对应controller
			// toWhom[1]对应action
			HttpConnector.setAction(params, ServerHelper.eventController,
					ServerHelper.publishEventAction);
			if (!HttpConnector.setAuth(params, PublishEvent.this)) {
				return "请再次登录";
			}
			// 设置参数
			params.add(new BasicNameValuePair("name", name.getText().toString()));
			params.add(new BasicNameValuePair("destination", destination
					.getText().toString()));
			params.add(new BasicNameValuePair("time", mDatetime));
			params.add(new BasicNameValuePair("spot", spot.getText().toString()));
			params.add(new BasicNameValuePair("contact", contact.getText()
					.toString()));
			params.add(new BasicNameValuePair("content", content.getText()
					.toString()));
			params.add(new BasicNameValuePair("x",String.valueOf(mX)));
			params.add(new BasicNameValuePair("y",String.valueOf(mY)));
			params.add(new BasicNameValuePair("range",String.valueOf(DistanceCalculator.getRange(Backpackers.DEFAULT_DISTANCE))));
			JSONObject jsonResponse = HttpConnector.doPost(uri, params);
			try {	
				if (jsonResponse != null) {
					String stat = jsonResponse.getString("stat");
					if (stat.equals("success")) {
						// 上传
						return "success";
					} else {
						return jsonResponse.getString("err_msg");
					}
				} else {
					return "网络返回结果错误";
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return "登陆失败";
			}
		}

		/*
		 * 任务完成后的
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			mDialog.dismiss();
			if (result.equals("success")) {
				// 上传成功的操作
				Toast.makeText(PublishEvent.this, "=上传成功=", Toast.LENGTH_SHORT)
						.show();
				finish();
			} else {
				// 失败的情况
				Toast.makeText(PublishEvent.this, result, Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	private String checkForm(TextView name, TextView destination,
			TextView time, TextView spot, TextView contact, TextView content) {
		if (name.getText().toString().trim().length() == 0) {
			return "名称不能为空";
		}
		if (destination.getText().toString().trim().length() == 0) {
			return "目的地不能为空";
		}
		if (time.getText().toString().trim().length() == 0) {
			return "未设置事件开始时间";
		}
		if (spot.getText().toString().trim().length() == 0) {
			return "集合地点不能为空";
		}
		if (contact.getText().toString().trim().length() == 0) {
			return "联系方式不能为空";
		}
		if (content.getText().toString().trim().length() == 0) {
			return "事件描述不能为空";
		}
		return "success";
	}

}
