package com.kingshijie.backpackers.map;

import android.app.Application;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;

public class BMapApiApp extends Application {
	static BMapApiApp mDemoApp;

	BMapManager mBMapMan = null;

	String mStrKey = "A9F7AE82C0161B24C6D925657596B6DF25753484";
	boolean m_bKeyRight = true;

	static class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(int iError) {
			Toast.makeText(BMapApiApp.mDemoApp.getApplicationContext(),
					"已连接上百度服务器", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {
				Toast.makeText(BMapApiApp.mDemoApp.getApplicationContext(),
						"未获得授百度权", Toast.LENGTH_LONG).show();
				BMapApiApp.mDemoApp.m_bKeyRight = false;
			}
		}

	}

	@Override
	public void onCreate() {
		mDemoApp = this;
		setmBMapMan(new BMapManager(this));
		getmBMapMan().init(this.getmStrKey(), new MyGeneralListener());
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		if (getmBMapMan() != null) {
			getmBMapMan().destroy();
			setmBMapMan(null);
		}
		super.onTerminate();
	}

	public BMapManager getmBMapMan() {
		return mBMapMan;
	}

	public void setmBMapMan(BMapManager mBMapMan) {
		this.mBMapMan = mBMapMan;
	}

	public String getmStrKey() {
		return mStrKey;
	}

	public void setmStrKey(String mStrKey) {
		this.mStrKey = mStrKey;
	}
}
