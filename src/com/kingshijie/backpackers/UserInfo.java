package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.kingshijie.backpackers.bean.User;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.LevelCalculator;
import com.kingshijie.backpackers.util.ServerHelper;

public class UserInfo extends Activity {
	private TextView name;
	private TextView credit;
	private TextView level;
	private TextView addition_num;
	private TextView report_num;
	private ProgressDialog mLoadingDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userinfo);

		name = (TextView) findViewById(R.id.userinfo_name);
		credit = (TextView) findViewById(R.id.credit);
		level = (TextView) findViewById(R.id.level);
		addition_num = (TextView) findViewById(R.id.addition_num);
		report_num = (TextView) findViewById(R.id.report_num);
		
		mLoadingDialog = new ProgressDialog(UserInfo.this);
		mLoadingDialog.setCancelable(true);
		mLoadingDialog.setMessage("正在获取数据，请稍后...");
		
		//获取用户信息
		new FetchUserTask().execute();
	}
	
	/**
	 * 完成数据的获取
	 * 
	 * @author aaron
	 * 
	 */
	private class FetchUserTask extends AsyncTask<Void, Void, User> {

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
		protected User doInBackground(Void... arg) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			// 设置服务器端对应的Action
			HttpConnector.setAction(params, ServerHelper.userController, ServerHelper.fetchUserInfoAction);
			if (!HttpConnector.setAuth(params, UserInfo.this)) {
				return null;
			}
			JSONObject jsonResponse = HttpConnector.doGet(uri, params);
			try {
				if (jsonResponse != null) {
					User p = new User();
					p.setName(jsonResponse.getString("username"));
					p.setCredit(jsonResponse.getInt("credit"));
					p.setAdditionNum(jsonResponse.getInt("addition_num"));
					p.setReportNum(jsonResponse.getInt("report_num"));
					return p;
				} else {
					return null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		protected void onPostExecute(User result) {
			mLoadingDialog.dismiss();
			if (result != null) {
				// 给文本框赋值
				name.setText(result.getName());
				credit.setText(String.valueOf(result.getCredit()));
				level.setText(LevelCalculator.getLevelName(result.getCredit()));
				addition_num.setText(String.valueOf(result.getAdditionNum()));
				report_num.setText(String.valueOf(result.getReportNum()));
			} else {
				Toast.makeText(UserInfo.this, "没有你要找的内容诶", Toast.LENGTH_LONG).show();
			}
		}

	}

}
