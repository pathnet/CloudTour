package com.uniquext.android.cloudtour.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.uniquext.android.cloudtour.Constant;
import com.uniquext.android.cloudtour.EventModel;
import com.uniquext.android.cloudtour.R;
import com.uniquext.android.cloudtour.base.BaseActivity;
import com.uniquext.android.cloudtour.service.FakeService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * 　 　　   へ　　　 　／|
 * 　　    /＼7　　　 ∠＿/
 * 　     /　│　　 ／　／
 * 　    │　Z ＿,＜　／　　   /`ヽ
 * 　    │　　　 　　ヽ　    /　　〉
 * 　     Y　　　　　   `　  /　　/
 * 　    ｲ●　､　●　　⊂⊃〈　　/
 * 　    ()　 へ　　　　|　＼〈
 * 　　    >ｰ ､_　 ィ　 │ ／／      去吧！
 * 　     / へ　　 /　ﾉ＜| ＼＼        比卡丘~
 * 　     ヽ_ﾉ　　(_／　 │／／           消灭代码BUG
 * 　　    7　　　　　　　|／
 * 　　    ＞―r￣￣`ｰ―＿
 * ━━━━━━感觉萌萌哒━━━━━━
 *
 * @author UniqueXT
 * @version 1.0
 * @date 2019/4/18  10:32
 */
public class MapActivity extends BaseActivity {

    LocationManager locationManager;
    private MapView mMapView;

    private Marker mMarker = null;
    private GeocodeSearch mGeocodeSearch = null;
    private MarkerOptions mMarkerOptions = new MarkerOptions();

    private Marker mLocationMarker = null;
    private MarkerOptions mLocationMarkerOptions = null;

    private FloatingActionButton mFabSave;
    private FloatingActionButton mFabLocation;

//    private void test(LatLng latLng) {
//        float distance = AMapUtils.calculateLineDistance(location, latLng);
//        Log.e("####", "distance " + distance);
//
//        Point locationP = mMapView.getMap().getProjection().toScreenLocation(location);
//        Point latLngP = mMapView.getMap().getProjection().toScreenLocation(latLng);
//        double degrees = Math.toDegrees(Math.atan2(latLngP.y - locationP.y, latLngP.x - locationP.x)) + 90;  //  90为方位角
//        Log.e("####", "degrees " + degrees);
//
//        LatLng temp = LatLngUtil.computerLatLng(location.latitude, location.longitude, degrees, distance);
//        float tempDistance = AMapUtils.calculateLineDistance(location, temp);
//        Log.e("####", "tempDistance " + tempDistance);
//
//        Point tempP = mMapView.getMap().getProjection().toScreenLocation(temp);
//        double tempDegrees = Math.toDegrees(Math.atan2(tempP.y - locationP.y, tempP.x - locationP.x)) + 90;  //  90为方位角
//        Log.e("####", "tempDegrees " + tempDegrees);
//
//        Marker marker = mMapView.getMap().addMarker(new MarkerOptions().position(temp).title("B"));
//    }

    @Override
    protected int getLayoutId() {
        return R.layout.activty_map;
    }

    @Override
    protected void initView() {
        mMapView = findViewById(R.id.mapView);
        mFabSave = findViewById(R.id.fabSave);
        mFabLocation = findViewById(R.id.fabLocation);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        mGeocodeSearch = new GeocodeSearch(this);
        initSetting();
        initLocation();

        findViewById(R.id.fabLocation).setOnClickListener(v -> initLocation());
    }

    @Override
    protected void initEvent() {
        mFabLocation.setOnClickListener(v -> initLocation());
        mMapView.getMap().setOnMapClickListener(latLng -> {
            mMarkerOptions.position(latLng);
            getLocationDescription(new LatLonPoint(latLng.latitude, latLng.longitude));
        });
        mGeocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                String title = getTitle(regeocodeResult);
                String content = regeocodeResult.getRegeocodeAddress().getFormatAddress();
                mMarkerOptions.title(title).snippet(content);
                refreshMarker();
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
            }
        });
        mFabSave.setOnClickListener(v -> {
            Log.e("####", "getTitle " + mMarkerOptions.getTitle());
            Log.e("####", "getSnippet " + mMarkerOptions.getSnippet());
//            Log.e("####", "getPosition " + mMarkerOptions.getPosition().toString());
            startService(new Intent(this, FakeService.class));
            EventBus.getDefault().postSticky(new EventModel(Constant.FAKE_LOCATION, mMarkerOptions.getPosition()));
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventModel eventModel) {
        switch (eventModel.getCode()) {
            case Constant.NOT_ALLOWED_MOCK:
                Log.e("####", "请开启模拟定位");
                // TODO: 2019/4/19 跳转到开发者模式开启模拟定位
                Snackbar.make(mFabSave, "请开启模拟定位", Snackbar.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void initSetting() {
        //  开启指南针
        mMapView.getMap().getUiSettings().setCompassEnabled(true);
        //  隐藏缩放按钮
        mMapView.getMap().getUiSettings().setZoomControlsEnabled(false);
        //  开启所有手势
        mMapView.getMap().getUiSettings().setAllGesturesEnabled(true);
    }

    private void getLocationDescription(LatLonPoint latLonPoint) {
        mGeocodeSearch.getFromLocationAsyn(new RegeocodeQuery(latLonPoint, 10, GeocodeSearch.AMAP));
    }

    private String getTitle(RegeocodeResult regeocodeResult) {
        String title = regeocodeResult.getRegeocodeAddress().getNeighborhood();
        if (!TextUtils.isEmpty(title)) return title;
        List<PoiItem> poiItems = regeocodeResult.getRegeocodeAddress().getPois();
        if (poiItems == null || poiItems.isEmpty() || poiItems.get(0) == null)
            return regeocodeResult.getRegeocodeAddress().getCity();
        PoiItem poiItem = poiItems.get(0);
        if (!TextUtils.isEmpty(poiItem.getTitle())) return poiItem.getTitle();
        if (!TextUtils.isEmpty(poiItem.getSnippet())) return poiItem.getSnippet();
        return regeocodeResult.getRegeocodeAddress().getCity();
    }

    private void refreshMarker() {
        if (mMarker == null) {
            mMarker = mMapView.getMap().addMarker(mMarkerOptions);
        } else {
            mMarker.setMarkerOptions(mMarkerOptions);
        }
    }

    //  region 生命周期
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    //  endregion

    //  region 原生定位
    private void initLocation() {
        mLocationMarkerOptions = new MarkerOptions().icon(
                BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_location))
        );
        //获取定位服务
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startLocation();
    }

    @SuppressLint("MissingPermission")
    private void startLocation() {
        String bestProvider = getProvider();
        Location location = locationManager.getLastKnownLocation(bestProvider);
        Log.e("####", location.getBearing() + "");
        Log.e("####", location.toString());

        mLocationMarkerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
        if (mLocationMarker == null) {
            mLocationMarker = mMapView.getMap().addMarker(mLocationMarkerOptions);
        } else {
            mLocationMarker.setMarkerOptions(mLocationMarkerOptions);
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(mLocationMarkerOptions.getPosition(), 12, 0, 0));
        mMapView.getMap().moveCamera(cameraUpdate);
    }

    private String getProvider() {
        // 构建位置查询条件
        Criteria criteria = new Criteria();
        // 查询精度：高
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 电量要求：低
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // 返回最合适的符合条件的provider，第2个参数为true说明 , 如果只有一个provider是有效的,则返回当前provider
        return locationManager.getBestProvider(criteria, true);
    }
    //  endregion


}

/*
MyLocationStyle.LOCATION_TYPE_SHOW;//只定位一次。
(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
(MyLocationStyle.LOCATION_TYPE_FOLLOW) ;//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）
(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
 */