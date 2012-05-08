package com.kingshijie.backpackers.map;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
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
import com.kingshijie.backpackers.R;
import com.kingshijie.backpackers.util.HttpConnector;
import com.kingshijie.backpackers.util.ServerHelper;

public class BasicLocatorActivity extends Activity {
	protected BMapApiApp mApp;
	protected LocationListener mLocationListener;
	protected double mX, mY;
	protected String mCity;
	protected MKSearch mMKSearch;
	protected TextView mWhereTextView = null;
	
	private String _controller = ServerHelper.userController;
	private String _action = ServerHelper.setMyLocationAction;
	
	//上次更新位置的时间
	private static long lastUpdatedTime = 0;
	//当前的时间
	private static long currentTime = System.currentTimeMillis();
	//两次位置更新的最小间隔
	private final static long waitingTime = 10000;

	/*
	 * 初始化百度地图的定位功能
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO 检查是否开GPS
		// 获取全局APP
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
				//更新当前位置
				SharedPreferences sharedPreferences = getSharedPreferences("settings",
						Context.MODE_PRIVATE);
				//设置为旅行状态，且超过一段时间未更新
				currentTime = System.currentTimeMillis();
				if(sharedPreferences.getBoolean("on_travel", false) && (currentTime - lastUpdatedTime) > waitingTime){
					lastUpdatedTime = currentTime;
					//update transaction
					new SetMyLocationTask().execute(mX,mY);
				}
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
	protected class MySearchListener implements MKSearchListener {
		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			if (iError != 0) {
				String str = String.format("网络错误，错误号%d", iError);
				Toast.makeText(BasicLocatorActivity.this, str, Toast.LENGTH_LONG).show();
				return;
			}
			MKGeocoderAddressComponent addr = result.addressComponents;
			mCity = addr.city;
			if(mWhereTextView != null)
				mWhereTextView.setText(addr.province + addr.city);
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

	@Override
	protected void onPause() {
		super.onPause();
		mApp.mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mApp.mBMapMan.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.mBMapMan.getLocationManager().requestLocationUpdates(
				mLocationListener);
		mApp.mBMapMan.start();
	}
	
	/**
	 * 更新手机所在位置
	 * 
	 * @author aaron
	 * 
	 */
	private class SetMyLocationTask extends
			AsyncTask<Double, Void, Void> {

		/*
		 * 后台操作，包括网络传输，数据封装
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Double... arg) {
			// 发送http请求
			String uri = getResources().getString(R.string.server_address);
			// 设置服务器端对应的Action
			double x = arg[0];
			double y = arg[1];
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			params.add(new BasicNameValuePair("x",String.valueOf(x)));
			params.add(new BasicNameValuePair("y",String.valueOf(y)));
			String deviceId = Secure.getString(BasicLocatorActivity.this.getContentResolver(), Secure.ANDROID_ID);
			params.add(new BasicNameValuePair("device_id",deviceId));
			HttpConnector.setAction(params, _controller, _action);
			if (!HttpConnector.setAuth(params, BasicLocatorActivity.this)) {
				return null;
			}
			// 设置参数
			HttpConnector.doGet(uri, params);
			return null;
		}

	}

}
