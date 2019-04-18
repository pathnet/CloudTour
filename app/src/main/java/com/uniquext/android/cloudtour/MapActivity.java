package com.uniquext.android.cloudtour;

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

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

    private MapView mMapView;
    private LatLng mLatLng = null;
    private Marker mMarker = null;
    private MarkerOptions mMarkerOptions = new MarkerOptions();
    private GeocodeSearch mGeocodeSearch = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activty_map;
    }

    @Override
    protected void initView() {
        mMapView = findViewById(R.id.mapView);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mMapView.onCreate(savedInstanceState);
        mGeocodeSearch = new GeocodeSearch(this);
        initSetting();
    }

    @Override
    protected void initEvent() {
        mMapView.getMap().setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (mLatLng == null) {
                    mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        });
        mMapView.getMap().setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMarkerOptions.position(latLng);
                getLocationDescription(new LatLonPoint(latLng.latitude, latLng.longitude));
            }
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
                for (GeocodeAddress address : geocodeResult.getGeocodeAddressList()) {
                    Log.e("####", "#### GeocodeAddress #######");
                    Log.e("####", "getAdcode " + address.getAdcode());
                    Log.e("####", "getBuilding " + address.getBuilding());
                    Log.e("####", "getCity " + address.getCity());
                    Log.e("####", "getDistrict " + address.getDistrict());
                    Log.e("####", "getFormatAddress " + address.getFormatAddress());
                    Log.e("####", "getLevel " + address.getLevel());
                    Log.e("####", "getNeighborhood " + address.getNeighborhood());
                    Log.e("####", "getProvince " + address.getProvince());
                    Log.e("####", "getTownship " + address.getTownship());
                    Log.e("####", "getLatLonPoint " + address.getLatLonPoint());
                }
                Log.e("####", "getGeocodeQuery().getCity() " + geocodeResult.getGeocodeQuery().getCity());
                Log.e("####", "getGeocodeQuery().getCity() " + geocodeResult.getGeocodeQuery().getLocationName());
            }
        });
    }

    private void initSetting() {
        //  开启指南针
        mMapView.getMap().getUiSettings().setCompassEnabled(true);
        //  隐藏缩放按钮
        mMapView.getMap().getUiSettings().setZoomControlsEnabled(false);
        //  初始化缩放比
        mMapView.getMap().moveCamera(CameraUpdateFactory.zoomTo(18));
        //  开启所有手势
        mMapView.getMap().getUiSettings().setAllGesturesEnabled(true);
        //  设置为true表示启动显示定位蓝点
        mMapView.getMap().setMyLocationEnabled(true);
        //  设置默认定位按钮是否显示
        mMapView.getMap().getUiSettings().setMyLocationButtonEnabled(true);
        //  设置定位蓝点的Style
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        //  定位一次，且将视角移动到地图中心点
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        mMapView.getMap().setMyLocationStyle(myLocationStyle);
    }

    private void getLocationDescription(LatLonPoint latLonPoint) {
        mGeocodeSearch.getFromLocationAsyn(new RegeocodeQuery(latLonPoint, 10, GeocodeSearch.AMAP));
    }

    private String getTitle(RegeocodeResult regeocodeResult) {
        String title = regeocodeResult.getRegeocodeAddress().getNeighborhood();
        if (!TextUtils.isEmpty(title)) return title;
        List<PoiItem> poiItems = regeocodeResult.getRegeocodeAddress().getPois();
        if (poiItems == null || poiItems.isEmpty() || poiItems.get(0) == null) return null;
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
}

/*
MyLocationStyle.LOCATION_TYPE_SHOW;//只定位一次。
(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
(MyLocationStyle.LOCATION_TYPE_FOLLOW) ;//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）
(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
 */