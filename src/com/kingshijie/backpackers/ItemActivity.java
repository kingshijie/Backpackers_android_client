package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kingshijie.backpackers.bean.Place;
import com.kingshijie.backpackers.map.Map;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.ServerHelper;

public class ItemActivity extends Activity {
	private TextView name;
	private TextView description;
	private TextView notice;
	private Button showInMap;
	private Button fetchRoute;
	private Button report;
	private RatingBar ratingBar;
	private Place mItem;
	private String _controller;
	private String _action;

	static final int DIALOG_RATING_ID = 0;
	static final int DIALOG_REPORT_ID = 1;
	static final int DIALOG_ROUTE_ID = 2;

	private ProgressDialog mLoadingDialog;
	


	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item);

		name = (TextView) findViewById(R.id.name);
		description = (TextView) findViewById(R.id.description);
		notice = (TextView) findViewById(R.id.notice);
		showInMap = (Button) findViewById(R.id.show_in_map);
		fetchRoute = (Button) findViewById(R.id.fetch_route);
		report = (Button) findViewById(R.id.report);
		ratingBar = (RatingBar) findViewById(R.id.ratingBar);

		// 获取intent的数据
		Bundle bdl = getIntent().getExtras();
		_controller = bdl.getString("ctrl");
		long itemId = bdl.getLong("item_id");

		// 打分条触发事件
		ratingBar.setOnTouchListener(new scoreIt());

		// 触发举报事件
		report.setOnClickListener(new doReport());
		
		showInMap.setOnClickListener(new showMap());

		mLoadingDialog = new ProgressDialog(ItemActivity.this);
		mLoadingDialog.setCancelable(true);
		mLoadingDialog.setMessage("正在获取数据，请稍后...");

		new FetchItemTask().execute(itemId);

	}

	private class scoreIt implements OnTouchListener {

		/*
		 * 显示打分的dialog
		 * 
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			showDialog(DIALOG_RATING_ID);
			return true;
		}

	}

	private class doReport implements OnClickListener {

		@Override
		public void onClick(View v) {
			showDialog(DIALOG_REPORT_ID);
		}

	}
	
	private class showMap implements OnClickListener{

		@Override
		public void onClick(View v) {
			ArrayList<Place> places = new ArrayList<Place>(1);
			places.add(mItem);
			Intent intent = new Intent(ItemActivity.this,Map.class);
			Bundle bdl = new Bundle();
			bdl.putParcelableArrayList("GPOINT", places);
			bdl.putInt("center_type",Map.ONE_PLACE);
			intent.putExtras(bdl);
			startActivity(intent);
		}
		
	}

	/*
	 * 每次启动重置分数
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;
		switch (id) {
		case DIALOG_RATING_ID:
			builder = new AlertDialog.Builder(ItemActivity.this);
			LayoutInflater inflater = getLayoutInflater();
			final View layout = inflater.inflate(R.layout.dialog_scoring,
					(ViewGroup) findViewById(R.id.dialog_scoring));
			builder.setTitle("打个分吧")
					.setView(layout)
					.setCancelable(true)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@SuppressWarnings("unchecked")
								public void onClick(DialogInterface dialog,
										int id) {
									RatingBar rating = (RatingBar) layout
											.findViewById(R.id.score);
									int score = (int)rating.getRating();
									List<NameValuePair> par = new ArrayList<NameValuePair>(10);
									par.add(new BasicNameValuePair("id",String.valueOf(mItem.getId())));
									par.add(new BasicNameValuePair("score",String.valueOf(score)));
									_action = ServerHelper.scoringAction;
									new HttpManager().execute(par);
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
		case DIALOG_REPORT_ID:
			Resources res = getResources();
			final String[] items = res.getStringArray(R.array.report_choices);
			builder = new AlertDialog.Builder(this);
			builder.setTitle("选择举报内容");
			builder.setSingleChoiceItems(items, -1,
					new DialogInterface.OnClickListener() {
						@SuppressWarnings("unchecked")
						public void onClick(DialogInterface dialog, int item) {
							String contents = items[item];
							List<NameValuePair> par = new ArrayList<NameValuePair>(10);
							par.add(new BasicNameValuePair("id",String.valueOf(mItem.getId())));
							par.add(new BasicNameValuePair("contents",contents));
							_action = ServerHelper.reportAction;
							new HttpManager().execute(par);
							dialog.dismiss();
						}
					});
			dialog = builder.create();
			break;
		case DIALOG_ROUTE_ID:
			//TODO 查询路线
			dialog = null;
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
	
	/**
	 * 获取item信息并给view赋值
	 * 
	 * @author aaron
	 * 
	 */
	private class FetchItemTask extends AsyncTask<Long, Void, Place> {

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
		protected Place doInBackground(Long... arg) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			// 设置服务器端对应的Action
			HttpConnector.setAction(params, _controller, ServerHelper.fetchItemAction);
			if (!HttpConnector.setAuth(params, ItemActivity.this)) {
				return null;
			}
			// 设置参数
			params.add(new BasicNameValuePair("id", String.valueOf(arg[0])));
			JSONObject jsonResponse = HttpConnector.doGet(uri, params);
			try {
				if (jsonResponse != null) {
					Place p = new Place();
					p.setId(jsonResponse.getLong("id"));
					p.setName(jsonResponse.getString("name"));
					p.setScore(jsonResponse.getInt("score"));
					p.setVoted(jsonResponse.getInt("voted"));
					p.setX(jsonResponse.getDouble("x"));
					p.setY(jsonResponse.getDouble("y"));
					p.setCity(jsonResponse.getString("city"));
					p.setDescription(jsonResponse.getString("description"));
					p.setNotice(jsonResponse.getString("notice"));
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

		/*
		 * 是否跳转到结果list
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(Place result) {
			mLoadingDialog.dismiss();
			if (result != null) {
				mItem = result;
				// 给文本框赋值
				name.setText(mItem.getName());
				description.setText(mItem.getDescription());
				notice.setText(mItem.getNotice().equals("") ? "无" : mItem.getNotice());
				// 设置分数
				float rating;
				if (mItem.getVoted() != 0) {
					rating = ((float)mItem.getScore()) / mItem.getVoted();
				} else {
					rating = 0;
				}
				Log.v("rating", String.valueOf(rating));
				ratingBar.setRating(rating);
			} else {
				Toast.makeText(ItemActivity.this, "没有你要找的内容诶", Toast.LENGTH_LONG).show();
			}
		}

	}
	
	/**
	 * 完成数据的获取
	 * 
	 * @author aaron
	 * 
	 */
	private class HttpManager extends AsyncTask<List<NameValuePair>, Void, String> {

		/*
		 * 显示等待的dialog
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			Toast.makeText(ItemActivity.this, "正在发送...", Toast.LENGTH_LONG).show();
		}

		/*
		 * 后台操作，包括网络传输，数据封装
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(List<NameValuePair>... arg) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			List<NameValuePair> params = arg[0];
			// 设置服务器端对应的Action
			HttpConnector.setAction(params, _controller, _action);
			if (!HttpConnector.setAuth(params, ItemActivity.this)) {
				return null;
			}
			JSONObject jsonResponse = HttpConnector.doGet(uri, params);
			try {
				if (jsonResponse != null) {
					String stat = jsonResponse.getString("stat");
					if(stat != null && stat.equals("success")){
						return "success";
					}else{
						return jsonResponse.getString("err_msg");
					}
				} else {
					return "操作失败";
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return "网络返回结果错误";
			}
		}

		/*
		 * 是否跳转到结果list
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				Toast.makeText(ItemActivity.this, "操作成功", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(ItemActivity.this, result, Toast.LENGTH_LONG).show();
			}
		}

	}
}
