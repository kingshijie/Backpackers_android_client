package com.kingshijie.backpackers.map;

import java.util.ArrayList;

import android.os.Bundle;

import com.baidu.mapapi.GeoPoint;
import com.kingshijie.backpackers.bean.GPoint;

public class Map extends BasicMapActivity {

	public final static int MY_LOCATION = 0;
	public final static int ONE_PLACE = 1;

	private int mCenterType;

	/*
	 * 获取需要显示的地点
	 * 
	 * @see com.baidu.mapapi.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// 获取需要显示的地点参数
		Bundle bdl = getIntent().getExtras();
		ArrayList<GPoint> gPoint = bdl.getParcelableArrayList("GPOINT");
		if(bdl.getInt("center_type") != 0){
			mCenterType = bdl.getInt("center_type");
		}else{
			mCenterType = MY_LOCATION;
		}

		// 添加地点图层
		mMapView.getOverlays().add(new OverItemT(mMarker, this, gPoint));

		if (gPoint.size() > 0) {
			mCenter = new GeoPoint((int) (gPoint.get(0).getX() * 1e6),
					(int) (gPoint.get(0).getY() * 1e6));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kingshijie.backpackers.BasicMapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		switch (mCenterType) {
		case ONE_PLACE:
			break;
		case MY_LOCATION:

		default:
			mApp.mBMapMan.getLocationManager().removeUpdates(mLocationListener);
			mLocationOverlay.disableMyLocation();
			mLocationOverlay.disableCompass();
			break;
		}
	}

	/*
	 * 启动时判断是否以当前位置为中心
	 * 
	 * @see com.kingshijie.backpackers.BasicMapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		switch (mCenterType) {
		case ONE_PLACE:
			mMapView.getController().animateTo(mCenter);
			break;
		case MY_LOCATION:

		default:
			mApp.mBMapMan.getLocationManager().requestLocationUpdates(
					mLocationListener);
			mLocationOverlay.enableMyLocation();
			mLocationOverlay.enableCompass();
			break;
		}
	}

}
