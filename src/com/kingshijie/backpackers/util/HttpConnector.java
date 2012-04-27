package com.kingshijie.backpackers.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class HttpConnector {
	public static void setAction(List<NameValuePair> params, String controller,
			String action) {
		params.add(new BasicNameValuePair("c", controller));
		params.add(new BasicNameValuePair("a", action));
	}
	
	public static boolean setAuth(List<NameValuePair> params,Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences("settings",
				Context.MODE_PRIVATE);
		long user_id = sharedPreferences.getLong("user_id", 0);
		String username = sharedPreferences.getString("username", null);
		if(user_id != 0 && username != null){
			params.add(new BasicNameValuePair("user_id",String.valueOf(user_id)));
			params.add(new BasicNameValuePair("username",String.valueOf(username)));
			return true;
		}else{
			return false;
		}
	}

	public static JSONObject doGet(String uri, List<NameValuePair> params) {
		// 获取访问的地址
		String con = StringOperator.mergeParams(uri, params);
		// 实例化HttpClient
		HttpClient client = new DefaultHttpClient();
		// 实例化HttpGet
		HttpGet myget = new HttpGet(con);
		//返回结果的容器
		StringBuilder builder = new StringBuilder();
		try {
			HttpResponse response = client.execute(myget);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			JSONObject jsonObject = new JSONObject(builder.toString());
			Log.v("HttpClient", "doGet success");
			return jsonObject;
		} catch (Exception e) {
			Log.v("HttpClient", "doGet fail");
			return null;
		}
	}

	public static JSONObject doPost(String uri, List<NameValuePair> params) {
		// Create a new HttpClient and Post Header
		HttpClient client = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(uri);
		//返回结果的容器
		StringBuilder builder = new StringBuilder();
		try {
			//传递参数,必须设定所传的字符集
			httppost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
			// Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			//读结果
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			JSONObject jsonObject = new JSONObject(builder.toString());
			Log.v("HttpClient", "doPost success");
			return jsonObject;

		} catch (Exception e) {
			Log.v("HttpClient", "doPost fail");
			return null;
		}

	}
}
