package com.sujitha.maptracking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;

public class TrackForeground extends Service {

    private Context context;

    OfflineDatabase offlineDatabase;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.context = this;
        offlineDatabase = new OfflineDatabase(this);
        if (intent.getAction().equals("Forground_starts")) {
            connectToGoogleApiClientAndGetTheAddress();
            showTheForegroundNotification();
        } else if (intent.getAction().equals("Forground_stops")) {
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    private GoogleApiClient googleApiClient = null;
    private LocationRequest locationRequest = null;
    private FusedLocationProviderClient mFusedLocationClient = null;

    @SuppressWarnings("MissingPermission")
    private void connectToGoogleApiClientAndGetTheAddress() {
        connectGoogleApiClient();
    }

    private void connectGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        setRequestTime();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(TrackForeground.this, "Google api client not updated!", Toast.LENGTH_SHORT).show();
                        googleApiClient = null;
                    }
                }).build();
        googleApiClient.connect();
    }


    private void setRequestTime() {
        ////////////////////////////////////////////////////////
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setSmallestDisplacement(1);
        startLocationReq();
    }


    @SuppressWarnings("MissingPermission")
    private void startLocationReq() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    protected void stopLocationUpdatesAndRemoveGoogleApiClient() {
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        if (googleApiClient != null) {
            if (googleApiClient.isConnected())
                googleApiClient.disconnect();
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                addDataToDB(location);
            }
        }
    };

    private void addDataToDB(Location location) {
        if (offlineDatabase != null) {
            int checkValue = offlineDatabase.addUserLocToDbWhenUserisOffline(new
                    UserLocDataModel(location.getLongitude() + ""
                    , location.getLatitude() + "", getCurrentTimeDate()));
            if (checkValue != -1) {
                Toast.makeText(context, "New Location Added", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent("update_map_broad_cast");
                sendBroadcast(intent);
            }
        }

    }


    public static String getCurrentTimeDate() {
        Date date = new Date();
        String strDateFormat = "yyyy-MM-dd hh:mm a";
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        return dateFormat.format(date);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Tracking", "In onDestroy");
        stopLocationUpdatesAndRemoveGoogleApiClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }

    private void showTheForegroundNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("NOTIFICATION_CHANNEL_ID_OF_TRACK_NOTIFICATION",
                    "OREO_TRACK_NOTIFICATION_CHANNEL_NAME",
                    importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Bitmap logoBitmapIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL_ID_OF_TRACK_NOTIFICATION");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("" + getString(R.string.app_name));
        builder.setLargeIcon(logoBitmapIcon);
        builder.setContentIntent(pendingIntent);
        builder.setTicker("Tracking Started");
        builder.setContentText("Tracking Started . . .");
        builder.setPriority(PRIORITY_HIGH);
        final Notification notification = builder.build();
        startForeground(546, notification);
    }

}
