package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.ServerHelper;

public class Login extends Activity {

	public static final Integer LOGIN_SUCCESS = new Integer(1);
	public static final Integer LOGIN_FAIL = new Integer(0);

	private Button mLogin;
	private EditText mUsername;
	private EditText mPassword;
	private CheckBox mAutoLogin;
	private CheckBox mRmbPwd;
	private ProgressDialog mLoginDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// 映射控件
		mLogin = (Button) findViewById(R.id.login);
		mUsername = (EditText) findViewById(R.id.username);
		mPassword = (EditText) findViewById(R.id.password);
		mAutoLogin = (CheckBox) findViewById(R.id.auto_login);
		mRmbPwd = (CheckBox) findViewById(R.id.rmb_pwd);

		// 获取sharedPreferences
		SharedPreferences sharedPreferences = getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		boolean rmb_pwd = sharedPreferences.getBoolean("rmb_pwd", false);
		boolean auto_login = sharedPreferences.getBoolean("auto_login", false);
		// 如果不需要登录
		if (auto_login && sharedPreferences.getLong("user_id", 0) != 0) {
			// 调用登录activity
			Intent intent = new Intent(this, Backpackers.class);
			startActivity(intent);
			finish();
		}
		// 设置选项状态
		if (rmb_pwd) {
			mRmbPwd.setChecked(true);
		}
		if (auto_login) {
			mAutoLogin.setChecked(true);
		}
		// 记录登陆帐号
		String username = sharedPreferences.getString("username", "");
		mUsername.setText(username);
		// 如果选中记住密码选项
		if (mRmbPwd.isChecked()) {
			String password = sharedPreferences.getString("password", "");
			mPassword.setText(password);
		}

		mLoginDialog = new ProgressDialog(Login.this);
		mLoginDialog.setCancelable(true);
		mLoginDialog.setMessage("登陆中，请稍后");

		// 设置监听
		mLogin.setOnClickListener(new loginOnClick());

	}

	public String checkForm(String username, String password) {
		if (username.trim().equals("")) {
			return "用户名不能为空";
		}
		String reg = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		if (!username.matches(reg)) {
			return "邮箱格式错误";
		}
		if (password.trim().equals("")) {
			return "密码不能为空";
		}
		return "success";
	}

	// 登陆按钮事件处理
	private class loginOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			new LoginTask().execute();
		}

	}

	private class LoginTask extends AsyncTask<Void, Void, String> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(Void... arg0) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			String username = mUsername.getText().toString();
			String password = mPassword.getText().toString();
			String checkResult = checkForm(username, password);
			if (!checkResult.equals("success")) {
				return checkResult;
			}

			List<NameValuePair> params = new ArrayList<NameValuePair>(4);
			// Map<String, String> params = new HashMap<String, String>();
			// 设置服务器端对应的Action
			HttpConnector.setAction(params, ServerHelper.userController, ServerHelper.loginAction);
			// 设置参数
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("password", password));
			JSONObject jsonResponse = HttpConnector.doGet(uri, params);
			try {
				if (jsonResponse != null) {
					SharedPreferences sharedPreferences = getSharedPreferences(
							"settings", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					String stat = jsonResponse.getString("stat");
					if (stat != null && stat.equals("success")) {
						// 登录成功
						long user_id = jsonResponse.getLong("user_id");
						username = jsonResponse.getString("username");
						// 将数据存入sharedpreferences
						editor.putLong("user_id", user_id);
						editor.putString("username", username);
						if (mRmbPwd.isChecked()) {
							editor.putString("password", password);
							editor.putBoolean("rmb_pwd", true);
						} else {
							editor.putBoolean("rmb_pwd", false);
						}
						if (mAutoLogin.isChecked()) {
							editor.putBoolean("auto_login", true);
						} else {
							editor.putBoolean("auto_login", false);
						}
						// Commit the edits!
						editor.commit();
						Log.v("url response", username);
						Log.v("url response", String.valueOf(user_id));
						return "success";
					} else {
						editor.putString("username", username);
						if (mRmbPwd.isChecked()) {
							editor.putString("password", password);
							editor.putBoolean("rmb_pwd", true);
						}
						if (mAutoLogin.isChecked()) {
							editor.putBoolean("auto_login", true);
						}
						// Commit the edits!
						editor.commit();
						// Toast.makeText(Login.this,jsonResponse.getString("err_msg"),Toast.LENGTH_SHORT).show();
						return jsonResponse.getString("err_msg");
					}
				} else {
					return "网络返回结果为空";
					// Toast.makeText(Login.this, "网络返回结果为空",
					// Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				// Toast.makeText(Login.this, "登陆失败",
				// Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return "登陆失败";
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			mLoginDialog.dismiss();
			if (result.equals("success")) {
				// 登录成功的操作
				Intent intent = new Intent(Login.this, Backpackers.class);
				startActivity(intent);
				finish();
			} else {
				// 登录失败的情况
				Toast.makeText(Login.this, result, Toast.LENGTH_SHORT).show();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mLoginDialog.show();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}

}
