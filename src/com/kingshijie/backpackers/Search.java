package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKGeocoderAddressComponent;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.kingshijie.backpackers.util.DistanceCalculator;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.Place;

public class Search extends Activity {

	private final String _controller = "module_scenery";
	private final String _action = "android_search_near";
	private final int maxDis = 29;
	private final int defaultDis = 3;

	private ProgressDialog mLoadingDialog;
	private Spinner mModule;
	private EditText mKeyWord;
	private SeekBar mDistance;
	private TextView mDisplayDistance;
	private Button searchGo;
	private TextView display_location;

	private BMapApiApp mApp;
	private LocationListener mLocationListener;
	private double mX, mY;
	private MKSearch mMKSearch;

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

		mModule = (Spinner) findViewById(R.id.module);
		mKeyWord = (EditText) findViewById(R.id.key_word);
		mDistance = (SeekBar) findViewById(R.id.distance);
		mDisplayDistance = (TextView) findViewById(R.id.display_distance);
		searchGo = (Button) findViewById(R.id.search);
		display_location = (TextView) findViewById(R.id.display_location);

		mDistance.setMax(maxDis);
		mDistance.setProgress(defaultDis);
		mDisplayDistance.setText(String.valueOf(defaultDis));
		mDistance.setOnSeekBarChangeListener(new MySeekBar());

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.modules, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mModule.setAdapter(adapter);

		searchGo.setOnClickListener(new GoSearch());

		mLoadingDialog = new ProgressDialog(Search.this);
		mLoadingDialog.setCancelable(true);
		mLoadingDialog.setMessage("搜索中，请稍后...");

		// 获取当前位置
		mApp = (BMapApiApp) this.getApplication();
		if (mApp.mBMapMan == null) {
			mApp.mBMapMan = new BMapManager(getApplication());
			mApp.mBMapMan
					.init(mApp.mStrKey, new BMapApiApp.MyGeneralListener());
		}
		mApp.mBMapMan.start();

		// 注册监听
		mLocationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				mX = location.getLatitude();
				mY = location.getLongitude();
				mMKSearch.reverseGeocode(new GeoPoint((int) (mX * 1E6),
						(int) (mY * 1E6)));
				Log.v("location", String.format("(%.2f,%.2f)", mX, mY));
			}
		};
		// 初始化搜索类
		mMKSearch = new MKSearch();
		mMKSearch.init(mApp.mBMapMan, new MySearchListener());// 注意，MKSearchListener只支持一个，以最后一次设置为准

	}

	/**
	 * 百度地图搜索接口的实现
	 * 
	 * @author aaron
	 * 
	 */
	public class MySearchListener implements MKSearchListener {
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			if (iError != 0) {
				String str = String.format("网络错误，错误号%d", iError);
				Toast.makeText(Search.this, str, Toast.LENGTH_LONG).show();
				return;
			}
			MKGeocoderAddressComponent addr = result.addressComponents;
			display_location.setText(addr.province + addr.city + addr.street);
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
		}

		@Override
		public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
		}
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

	private class GoSearch implements OnClickListener {

		/*
		 * (non-Javadoc)
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
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			mLoadingDialog.show();
		}

		/*
		 * (non-Javadoc)
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
			params.add(new BasicNameValuePair("key_word",keyWord));
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
						double distance = DistanceCalculator.GetCoarseDistance(mX, mY, x, y);
						if(distance <= dis){
							Place p = new Place(id,x,y,name,distance);
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
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(ArrayList<Place> result) {
			mLoadingDialog.dismiss();
			if (result != null) {
				mLoadingDialog.dismiss();
				Intent intent = new Intent(Search.this, ItemList.class);
				Bundle bdl = new Bundle();
				//Place类实现Parcelable接口
				bdl.putParcelableArrayList("places", result);
				intent.putExtras(bdl);
				startActivity(intent);
			} else {
			}
		}

	}

	@Override
	protected void onPause() {
		mApp.mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mApp.mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mApp.mBMapMan.getLocationManager().requestLocationUpdates(
				mLocationListener);
		mApp.mBMapMan.start();
		super.onResume();
	}
}
