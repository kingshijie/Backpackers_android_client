package com.kingshijie.backpackers.map;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.kingshijie.backpackers.R;
import com.kingshijie.backpackers.bean.GPoint;

public class BasicMapActivity extends MapActivity {
	protected BMapApiApp mApp;
	static MapView mMapView = null;
	static View mPopView = null;
	LocationListener mLocationListener = null;
	MyLocationOverlay mLocationOverlay = null;
	GeoPoint mCenter;
	Drawable mMarker;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.mapapi.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.map);

		mApp = (BMapApiApp) this.getApplication();
		if (mApp.mBMapMan == null) {
			mApp.mBMapMan = new BMapManager(getApplication());
			mApp.mBMapMan
					.init(mApp.mStrKey, new BMapApiApp.MyGeneralListener());
		}
		mApp.mBMapMan.start();

		// 初始化mapView
		super.initMapActivity(mApp.mBMapMan);

		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setDrawOverlayWhenZooming(true);

		// 初始化标志物
		mMarker = getResources().getDrawable(R.drawable.marker_item);
		mMarker.setBounds(0, 0, mMarker.getIntrinsicWidth(),
				mMarker.getIntrinsicHeight());

		// 初始化弹出框标志物
		mPopView = super.getLayoutInflater().inflate(R.layout.popview, null);
		mMapView.addView(mPopView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
				MapView.LayoutParams.TOP_LEFT));
		mPopView.setVisibility(View.GONE);
		
		//当前位置
		mLocationOverlay = new MyLocationOverlay(this, mMapView);
		mMapView.getOverlays().add(mLocationOverlay);

		//位置监听
		mLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					mCenter = new GeoPoint(
							(int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					mMapView.getController().animateTo(mCenter);
				}
			}
		};
	}

	protected void onPause() {
		super.onPause();
		mApp.mBMapMan.stop();
	}

	protected void onResume() {
		super.onResume();
		mApp.mBMapMan.start();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}

class OverItemT extends ItemizedOverlay<OverlayItem> {

	private List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
	private Drawable marker;
	private Context mContext;

	public OverItemT(Drawable marker, Context context, ArrayList<GPoint> places) {
		super(boundCenterBottom(marker));

		this.marker = marker;
		this.mContext = context;

		for (int i = 0; i < places.size(); i++) {
			// 用给定的经纬度构造GeoPoint，单位是微度 (度 * 1E6)
			GeoPoint p = new GeoPoint((int) (places.get(i).getX() * 1E6),
					(int) (places.get(i).getY() * 1E6));
			mGeoList.add(new OverlayItem(p, places.get(i).getName(), places
					.get(i).getName()));
		}

		populate(); // createItem(int)方法构造item。一旦有了数据，在调用其它方法前，首先调用这个方法
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

		Projection projection = mapView.getProjection();
		for (int index = size() - 1; index >= 0; index--) {
			OverlayItem overLayItem = getItem(index);

			String title = overLayItem.getTitle();
			// 将经纬度转化为像素
			Point point = projection.toPixels(overLayItem.getPoint(), null);

			//TODO 地图标示
			// 画名称
			Paint paintText = new Paint();
			paintText.setColor(Color.BLUE);
			paintText.setTextSize(15);
			canvas.drawText(title, point.x - 30, point.y, paintText);
		}

		super.draw(canvas, mapView, shadow);
		// 调整一个drawable边界，使得（0，0）是这个drawable底部最后一行中心的一个像素。
		boundCenterBottom(marker);
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return mGeoList.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mGeoList.size();
	}

	@Override
	protected boolean onTap(int i) {
		setFocus(mGeoList.get(i));
		GeoPoint pt = mGeoList.get(i).getPoint();
		Map.mMapView.updateViewLayout(Map.mPopView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, pt,
				MapView.LayoutParams.BOTTOM_CENTER));
		TextView text = (TextView) Map.mPopView.findViewById(R.id.text);
		text.setText(mGeoList.get(i).getTitle());
		Map.mPopView.setVisibility(View.VISIBLE);
		//TODO 改变点击事件
		Toast.makeText(this.mContext, mGeoList.get(i).getSnippet(),
				Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1) {
		// TODO Auto-generated method stub
		Map.mPopView.setVisibility(View.GONE);
		return super.onTap(arg0, arg1);
	}

}
