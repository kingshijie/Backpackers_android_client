package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kingshijie.backpackers.map.BasicLocatorActivity;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.ServerHelper;

public class Additions extends BasicLocatorActivity {
	private Button mFinishBtn;
	private EditText mNameBtn;
	private EditText mDescriptionBtn;
	private EditText mNoticeBtn;
	private ProgressDialog mDialog;

	private String _controller;
	private final String _action = ServerHelper.addModuleAction;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.additions);

		// 继承自父类的显示当前位置信息的文本
		mWhereTextView = (TextView) findViewById(R.id.display_location);

		// 获取接受请求的controller名称
		Bundle bdl = getIntent().getExtras();
		_controller = bdl.getString("ctrl");

		mNameBtn = (EditText) findViewById(R.id.name);
		mDescriptionBtn = (EditText) findViewById(R.id.description);
		mNoticeBtn = (EditText) findViewById(R.id.notice);

		mFinishBtn = (Button) findViewById(R.id.finish);
		mFinishBtn.setOnClickListener(new finishOnClick());

		mDialog = new ProgressDialog(Additions.this);
		mDialog.setCancelable(true);
		mDialog.setMessage("正在提交，马上好哦");

	}

	private class postTask extends AsyncTask<Void, Void, String> {

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
			String name = mNameBtn.getText().toString();
			String description = mDescriptionBtn.getText().toString();
			String notice = mNoticeBtn.getText().toString();
			String checkResult = checkForm(name, description, notice);
			if (!checkResult.equals("success")) {
				return checkResult;
			}
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			// 设置服务器端对应的Action
			// toWhom[0]对应controller
			// toWhom[1]对应action
			HttpConnector.setAction(params, _controller, _action);
			if (!HttpConnector.setAuth(params, Additions.this)) {
				return "请再次登录";
			}
			// 设置参数
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("description", description));
			params.add(new BasicNameValuePair("notice", notice));
			params.add(new BasicNameValuePair("x", String.valueOf(mX)));
			params.add(new BasicNameValuePair("y", String.valueOf(mY)));
			params.add(new BasicNameValuePair("city", mCity));
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
				Toast.makeText(Additions.this, "=上传成功=", Toast.LENGTH_SHORT)
						.show();
				finish();
			} else {
				// 失败的情况
				Toast.makeText(Additions.this, result, Toast.LENGTH_SHORT)
						.show();
			}
		}

	}

	/**
	 * 检查表单是否合法
	 * 
	 * @param name
	 * @param description
	 * @param notice
	 * @return
	 */
	private String checkForm(String name, String description, String notice) {
		if (name.trim().equals("")) {
			return "名称不能为空";
		}
		if (description.trim().equals("")) {
			return "描述不能为空";
		}
		return "success";
	}

	private class finishOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			new postTask().execute();
		}

	}
}
