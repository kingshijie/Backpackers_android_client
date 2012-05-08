package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kingshijie.backpackers.bean.Place;
import com.kingshijie.backpackers.map.BasicLocatorActivity;
import com.kingshijie.backpackers.util.DistanceCalculator;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.ServerHelper;

public class Search extends BasicLocatorActivity {

	private String _controller = ServerHelper.hostelController;
	private final String _action = ServerHelper.searchNearAction;
	private final int maxDis = 29;
	private final int defaultDis = 3;

	private ProgressDialog mLoadingDialog;
	private Spinner mModule;
	private EditText mKeyWord;
	private SeekBar mDistance;
	private TextView mDisplayDistance;
	private Button searchGo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		// 继承自父类的显示当前位置信息的文本
		mWhereTextView = (TextView) findViewById(R.id.display_location);

		// 自身的控件
		mModule = (Spinner) findViewById(R.id.module);
		mKeyWord = (EditText) findViewById(R.id.key_word);
		mDistance = (SeekBar) findViewById(R.id.distance);
		mDisplayDistance = (TextView) findViewById(R.id.display_distance);
		searchGo = (Button) findViewById(R.id.search);

		mDistance.setMax(maxDis);
		mDistance.setProgress(defaultDis);
		mDisplayDistance.setText(String.valueOf(defaultDis));
		mDistance.setOnSeekBarChangeListener(new MySeekBar());

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.modules, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mModule.setAdapter(adapter);
		mModule.setOnItemSelectedListener(new changeModule());

		searchGo.setOnClickListener(new GoSearch());

		mLoadingDialog = new ProgressDialog(Search.this);
		mLoadingDialog.setCancelable(true);
		mLoadingDialog.setMessage("搜索中，请稍后...");

	}

	/**
	 * 实现拖动条的接口
	 * 
	 * @author aaron
	 * 
	 */
	private class MySeekBar implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			mDisplayDistance.setText(String.valueOf(progress + 1));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}

	private class changeModule implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			_controller = ServerHelper.getControllerByName(parent.getItemAtPosition(
					position).toString());
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}

	}

	private class GoSearch implements OnClickListener {

		/*
		 * 执行获取地点信息的操作
		 * 
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			new FetchDataTask().execute();
		}

	}

	/**
	 * 完成数据的获取
	 * 
	 * @author aaron
	 * 
	 */
	private class FetchDataTask extends AsyncTask<Void, Void, ArrayList<Place>> {

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
		protected ArrayList<Place> doInBackground(Void... arg0) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			// 设置服务器端对应的Action
			HttpConnector.setAction(params, _controller, _action);
			if (!HttpConnector.setAuth(params, Search.this)) {
				return null;
			}
			int dis = mDistance.getProgress();
			String keyWord = mKeyWord.getText().toString();
			// 设置参数
			params.add(new BasicNameValuePair("x", String.valueOf(mX)));
			params.add(new BasicNameValuePair("y", String.valueOf(mY)));
			params.add(new BasicNameValuePair("range", String
					.valueOf(DistanceCalculator.getRange(dis))));
			params.add(new BasicNameValuePair("key_word", keyWord));
			JSONObject jsonResponse = HttpConnector.doPost(uri, params);
			try {
				if (jsonResponse != null) {
					JSONArray jsonArray = jsonResponse.getJSONArray("data");
					Log.v("result num", String.valueOf(jsonArray.length()));
					ArrayList<Place> places = new ArrayList<Place>(100);
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject one = jsonArray.getJSONObject(i);
						long id = one.getLong("id");
						double x = one.getDouble("x");
						double y = one.getDouble("y");
						String name = one.getString("name");
						double distance = DistanceCalculator.GetCoarseDistance(
								mX, mY, x, y);
						if (distance <= dis) {
							Place p = new Place(id, x, y, name, distance);
							places.add(p);
						}
					}
					Collections.sort(places);
					return places;
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
		protected void onPostExecute(ArrayList<Place> result) {
			mLoadingDialog.dismiss();
			if (result != null) {
				Intent intent = new Intent(Search.this, ItemList.class);
				Bundle bdl = new Bundle();
				// Place类实现Parcelable接口
				bdl.putParcelableArrayList("GPOINT", result);
				bdl.putString("ctrl", _controller);
				intent.putExtras(bdl);
				startActivity(intent);
			} else {
				Toast.makeText(Search.this, "没有你要找的内容诶", Toast.LENGTH_LONG).show();
			}
		}

	}

}
