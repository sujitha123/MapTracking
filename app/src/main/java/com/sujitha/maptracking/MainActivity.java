package com.sujitha.maptracking;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMapGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isCheckLocationPermission()) {
            requestLocationPermission();
        } else {
            checkMapAndService();
        }
        addingListView();
    }

    void checkMapAndService() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_maps_id);
        mapFragment.getMapAsync(this);
        if (!isServiceRunning(MainActivity.this, TrackForeground.class.getCanonicalName())) {
            Intent startIntent = new Intent(MainActivity.this, TrackForeground.class);
            startIntent.setAction("Forground_starts");
            startService(startIntent);
        }

    }

    void addingListView() {
        OfflineDatabase offlineDatabase = new OfflineDatabase(MainActivity.this);
        if (offlineDatabase.getUserTrackCountFromDb() > 0) {
            CustomAdapter adapter = new CustomAdapter(offlineDatabase.getUserLocFromDb(), getApplicationContext());
            ListView listView = findViewById(R.id.activity_main_listview_id);
            listView.setAdapter(adapter);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            registerTheBroadcastForSignal();
        } catch (Exception e) {
            e.printStackTrace();
        }

        checkMapAndService();
        addingListView();
    }

    public static boolean isServiceRunning(Context context, String classPath) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (service.service.getClassName().equals(classPath)) {
                        return true;
                    }
                }
            } else {
                return true;
            }
            return false;
        } catch (Exception e) {
            return true;

        }
    }


    boolean isCheckLocationPermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                10);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMapGlobal = googleMap;

        this.googleMapGlobal.getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.googleMapGlobal.setMyLocationEnabled(true);
        this.googleMapGlobal.getUiSettings().setZoomControlsEnabled(true);

        OfflineDatabase offlineDatabase = new OfflineDatabase(MainActivity.this);
        if (offlineDatabase.getUserTrackCountFromDb() > 0) {
            ArrayList<UserLocDataModel> userLocDataModelArrayList = offlineDatabase.getUserLocFromDb();

            for (int i = 0; i < userLocDataModelArrayList.size(); i++) {
                LatLng latLng = new LatLng(Double.parseDouble(userLocDataModelArrayList.get(i).getLatitude())
                        , Double.parseDouble(userLocDataModelArrayList.get(i).getLongitude()));
                addMarkerInMap(latLng, userLocDataModelArrayList.get(i).getUnixTimeStamp());
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
                googleMapGlobal.animateCamera(cu);
            }
        }, 3000);

    }

    ArrayList<Marker> markers = new ArrayList<>();

    public void addMarkerInMap(LatLng position, String name) {
        MarkerOptions options = new MarkerOptions()
                .title(name)
                .position(position);
        Marker mark = googleMapGlobal.addMarker(options);
        markers.add(mark);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                checkMapAndService();
                addingListView();
            } else {
                Toast.makeText(MainActivity.this, "Permission denied.. Please enable Location permission", Toast.LENGTH_SHORT).show();
                requestLocationPermission();
            }
        }
    }

    private boolean isBroadcastRegistredForSignal = false;

    private void registerTheBroadcastForSignal() throws Exception {
        if (!isBroadcastRegistredForSignal)
            registerReceiver(broadcastReceiverForSignal, new IntentFilter("update_map_broad_cast"));
        isBroadcastRegistredForSignal = true;
    }

    private void unregisterTheBroadCastForSignal() throws Exception {
        if (isBroadcastRegistredForSignal) {
            try {
                unregisterReceiver(broadcastReceiverForSignal);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private BroadcastReceiver broadcastReceiverForSignal = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            googleMapGlobal.clear();
            addingListView();
            checkMapAndService();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterTheBroadCastForSignal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
