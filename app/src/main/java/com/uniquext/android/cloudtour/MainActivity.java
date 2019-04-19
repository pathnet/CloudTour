package com.uniquext.android.cloudtour;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import com.uniquext.android.cloudtour.map.MapActivity;
import com.uniquext.android.lightpermission.LightPermission;
import com.uniquext.android.lightpermission.PermissionCallback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        LightPermission.with(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .result(new PermissionCallback() {
                    @Override
                    public void onGranted() {
                        startActivity(new Intent(MainActivity.this, MapActivity.class));
                    }

                    @Override
                    public void onDenied(String[] strings) {

                    }

                    @Override
                    public void onNoRequest(String[] strings) {

                    }
                });

    }
}
