package com.kingshijie.backpackers;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
import com.kingshijie.backpackers.util.HttpConnector;

public class Additions extends Activity {
	private Button mFinishBtn;
	private EditText mNameBtn;
	private EditText mDescriptionBtn;
	private EditText mNoticeBtn;
	private ProgressDialog mDialog;
	private TextView display_location;
	
	private BMapApiApp mApp;
	private LocationListener mLocationListener;
	
	private final String _controller = "module_hostel";
	private final String _action = "android_add_hostel";
	
	private double mX,mY;
	private String mCity;
	private MKSearch mMKSearch;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.additions);

		// TODO 检查是否开GPS
		mNameBtn = (EditText)findViewById(R.id.name);
		mDescriptionBtn = (EditText)findViewById(R.id.description);
		mNoticeBtn = (EditText)findViewById(R.id.notice);
		display_location = (TextView)findViewById(R.id.display_location);
		

		mFinishBtn = (Button) findViewById(R.id.finish);
		mFinishBtn.setOnClickListener(new finishOnClick());
		
		mDialog = new ProgressDialog(Additions.this);
		mDialog.setCancelable(true);
		mDialog.setMessage("正在提交，马上好哦");
		
		mApp = (BMapApiApp)this.getApplication();
		if (mApp.mBMapMan == null) {
			mApp.mBMapMan = new BMapManager(getApplication());
			mApp.mBMapMan.init(mApp.mStrKey, new BMapApiApp.MyGeneralListener());
		}
		mApp.mBMapMan.start();
        
        // 注册监听
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
            	mX = location.getLatitude();
            	mY = location.getLongitude();
                mMKSearch.reverseGeocode(new GeoPoint((int)(mX*1E6), (int)(mY*1E6)));
            	Log.v("location",String.format("(%.2f,%.2f)", mX,mY));
            }
        };
        //初始化搜索类
        mMKSearch = new MKSearch();
        mMKSearch.init(mApp.mBMapMan, new MySearchListener());//注意，MKSearchListener只支持一个，以最后一次设置为准
	}
	
	public class MySearchListener implements MKSearchListener {
	    @Override
	    public void onGetAddrResult(MKAddrInfo result, int iError) {
	    	if (iError != 0) {
				String str = String.format("网络错误，错误号%d", iError);
				Toast.makeText(Additions.this, str, Toast.LENGTH_LONG).show();
				return;
			}
	    	MKGeocoderAddressComponent addr = result.addressComponents;
	    	mCity = addr.city;
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
			String checkResult = checkForm(name, description,notice);
			if (!checkResult.equals("success")) {
				return checkResult;
			}
			List<NameValuePair> params = new ArrayList<NameValuePair>(10);
			// 设置服务器端对应的Action
			//toWhom[0]对应controller
			//toWhom[1]对应action
			HttpConnector.setAction(params, _controller, _action);
			if(!HttpConnector.setAuth(params, Additions.this)){
				return "请再次登录";
			}
			// 设置参数
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("description", description));
			params.add(new BasicNameValuePair("notice", notice));
			params.add(new BasicNameValuePair("x", String.valueOf(mX)));
			params.add(new BasicNameValuePair("y", String.valueOf(mY)));
			params.add(new BasicNameValuePair("city",mCity));
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
					return "网络返回结果为空";
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
				Toast.makeText(Additions.this, "=上传成功=", Toast.LENGTH_SHORT).show();
				finish();
			} else {
				// 失败的情况
				Toast.makeText(Additions.this, result, Toast.LENGTH_SHORT).show();
			}
		}

	}

	private String checkForm(String name, String description, String notice) {
		if(name.trim().equals("")){
			return "名称不能为空";
		}
		if(description.trim().equals("")){
			return "描述不能为空";
		}
		return "success";
	}
	
	private class finishOnClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			//TODO 需要根据所需调用
			new postTask().execute();
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
		mApp.mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		mApp.mBMapMan.start();
		super.onResume();
	}
}
