package com.uniquext.android.cloudtour.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;

import com.uniquext.android.cloudtour.Constant;
import com.uniquext.android.cloudtour.EventModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;

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
 * @date 2019/4/19  14:44
 */
public class FakeService extends Service {

    private final String PROVIDER = LocationManager.GPS_PROVIDER;
    private volatile Location mLocation = new Location(PROVIDER);
    private LocationManager locationManager;
    private Thread mFakeThread = new FakeThread();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.initManager();
        this.initLocation();
        EventBus.getDefault().register(this);

        mFakeThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void onMessageEvent(EventModel eventModel) {
        switch (eventModel.getCode()) {
            case Constant.FAKE_LOCATION:
                mLocation.setLatitude(eventModel.getLatLng().latitude);
                mLocation.setLongitude(eventModel.getLatLng().longitude);
                break;
            case Constant.CLOSE_FAKE:
                stopSelf();
                break;
        }
    }

    private void initLocation() {
        mLocation.setAccuracy(5.0f);
    }

    private void initManager() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.addTestProvider(PROVIDER,
                    true,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true,
                    Criteria.POWER_MEDIUM,
                    Criteria.ACCURACY_FINE
            );
            locationManager.setTestProviderEnabled(PROVIDER, true);
        } catch (SecurityException e) {
            EventBus.getDefault().post(new EventModel(Constant.NOT_ALLOWED_MOCK));
            stopSelf();
        }
    }

    private class FakeThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    if (mLocation.getLatitude() == 0 && mLocation.getLongitude() == 0)
                        continue;
                    mLocation.setTime(System.currentTimeMillis());
                    mLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    locationManager.setTestProviderLocation(PROVIDER, mLocation);
                } catch (SecurityException e) {
                    EventBus.getDefault().post(new EventModel(Constant.NOT_ALLOWED_MOCK));
                    stopSelf();
                    return;
                }
            }
        }
    }
}
