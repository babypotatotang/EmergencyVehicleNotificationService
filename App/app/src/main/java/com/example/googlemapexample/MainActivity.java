package com.example.googlemapexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private GoogleMap mMap;
    private Marker[] currentMarker = new Marker[100];
    private List<LatLng>Locations= new ArrayList<LatLng>();

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static boolean ID=true;
    //gps??? ?????? ?????? ????????? ??????????????? ????????? ??????????????? ?????? ??????
    private static final int UPDATE_INTERVAL_MS = 3000;// 3???
    private static final int FASTEST_UPDATE_INTERVAL_MS = 2000; // 2???

    // onRequestPermissionsResult?????? ????????? ???????????? ActivityCompat.requestPermissions??? ????????? ????????? ????????? ???????????? ?????? ???????????????.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;
    private int wait = 0;

    private ToggleButton tb;
    private int emg_button=1;
    private static double min=1000;


    //private  String PhoneNum = "";
    private String PhoneNum = "010-9271-3205";

    String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,Manifest.permission.READ_PHONE_NUMBERS
    };  // ?????? ?????????

    Location mCurrentLocatiion;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;

    private View mLayout2;
    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mLayout = findViewById(R.id.layout_main);
        mLayout2=findViewById(R.id.layout_main);
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS) //????????? update ?????? ??????(3000ms=3???)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS); //?????? ?????? ??? update ?????? ?????? (2000ms=2???)

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        tb=(ToggleButton)this.findViewById(R.id.togglebutton);
    }

    private void buttonactivate() {
        Log.d(TAG,"Emergency button: "+ emg_button);
        Log.d(TAG,"ID : "+ ID);

        if(ID){
            Toast.makeText(getApplicationContext(),"?????? ?????????" , Toast.LENGTH_SHORT).show();
            Log.d(TAG,"??????: ?????? ??????");

            tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                    if(on){
                        tb.setText("???????????????");
                        emg_button=0;
                        Log.d(TAG,"Emergency button: "+ emg_button);
                    }
                    else{
                        tb.setText("???????????????");
                        emg_button=1;
                        Log.d(TAG,"Emergency button: "+emg_button);
                    }
                }
            });
        }
        else{
            emg_button=0;
            Toast.makeText(getApplicationContext(), "?????? ?????????",Toast.LENGTH_SHORT).show();
            tb.setText("????????? ???????????? ??? ????????????.");
            tb.setClickable(false);
            Log.d(TAG,"??????: ?????? ??????");
        }

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;

        setDefaultLocation();
        //getPermission();


        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. ?????? ???????????? ????????? ?????????
            startLocationUpdates(); // 3. ?????? ???????????? ??????
        } else {
            //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.
            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Snackbar.make(mLayout, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                        ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }

        }
        


        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                wait=1;
                Log.d(TAG, "wait1 = "+wait);
            }
        });

        Response.Listener<String> checkEmergency=new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject2=new JSONObject(response);
                    ID=jsonObject2.getBoolean("ID");
                    Log.d(TAG,"ID ??????:"+ID);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        IDRequest idRequest=new IDRequest(PhoneNum, checkEmergency);
        RequestQueue IDqueue=Volley.newRequestQueue(MainActivity.this);
        IDqueue.add(idRequest);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {

                Location location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                String markerSnippet = "??????:" + location.getLatitude() + " ??????:" + location.getLongitude();

                /* ?????? ???????????? DB?????? */
                String Latitude = String.valueOf(location.getLatitude()); // ???????????? ????????? string ????????? ??????
                String Longitude = String.valueOf(location.getLongitude());
                Log.d(TAG, "PhoneNum : "+PhoneNum);

                Response.Listener<String> responseListener = new Response.Listener<String>() { // php ?????? ?????? ??????
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int number = jsonObject.getInt("number");
                            Log.d(TAG,"number : " + number);
                            String test=jsonObject.getString("test");
                            Log.d(TAG,"test : " + test);

                            String[] lat = new String[number+2];
                            String[] lng = new String[number+2];
                            Log.d(TAG, "String success !!");


                            while(number>=0) {
                                lat[number] = jsonObject.getString("Latitude"+number);
                                lng[number] = jsonObject.getString("Longitude"+number);
                                Log.d(TAG, "Latitude : "+lat[number]);
                                Log.d(TAG, "Longitude : "+lng[number]);
                                number--;
                            }
                            setCurrentLocation(lat, lng); //?????? ????????? ?????? ??????
                            buttonactivate();
                        }

                        catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                };

                // ????????? Volley??? ???????????? ??????
                AddressRequest addressRequest = new AddressRequest(Latitude, Longitude, PhoneNum, emg_button,ID,responseListener);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(addressRequest);

                Log.d(TAG, "onLocationResult : " + markerSnippet);


                if(wait!=1) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentPosition);
                    mMap.moveCamera(cameraUpdate);
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                }
                wait=0;

                mCurrentLocatiion = location;

            }
        }


    };

    @SuppressLint({"MissionPermission", "HardwareIds"})
    private void getPermission(){
        Log.d(TAG, "getPermission()");
        int chkper_phonestate = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int chkper_phonenum = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        String PhoneNumber_Temp = "";
        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (chkper_phonenum == PackageManager.PERMISSION_GRANTED && chkper_phonestate == PackageManager.PERMISSION_GRANTED &&hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            try {
                if (telephony.getLine1Number() != null) {
                    PhoneNumber_Temp = telephony.getLine1Number();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (PhoneNumber_Temp.startsWith("+82")) {
                PhoneNumber_Temp = PhoneNumber_Temp.replace("+82", "0");
                PhoneNum = PhoneNumberUtils.formatNumber(PhoneNumber_Temp);
                startLocationUpdates(); // 3. ?????? ???????????? ??????
            }
        }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout2, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }

        }

    };
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setCurrentLocation(String[] lat, String[] lng) {
        int i=0;
        double distance;
        int bool_warning = 0;

        while(currentMarker[i]!=null){
            currentMarker[i].remove();
            i++;
        }

        i=0;
        while(lat[i]!=null)
        {
            double latitude = Double.parseDouble(lat[i]);
            double longitude = Double.parseDouble(lng[i]);

            LatLng currentLatLng = new LatLng(latitude, longitude); // maker ?????? ( 0.001 = ??? 100m )
            Log.d(TAG, "currentLatLng : "+latitude + ", "+longitude);

            if (getDistance(currentPosition, currentLatLng) < 500 && getDistance(currentPosition, currentLatLng)!=0) { // ?????? ??? ????????? ?????? ???,
                Log.d(TAG, "WARRING !! :" + getDistance(currentPosition, currentLatLng));
                distance = getDistance(currentPosition, currentLatLng);
                if (distance<min){
                    min = distance;
                }
                bool_warning = 1;
            }

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLatLng);
            markerOptions.draggable(true);
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.redcircle); // maker icon ??????
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 70, 50, false); // maker ??????
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

            currentMarker[i] = mMap.addMarker(markerOptions);
            i++;

        }

        if (bool_warning == 1) { // ?????? ??? ????????? ?????? ???,
            warning(min);
            min=1000;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void warning(double distance){
        soundwarning(distance);
        viewwarning();
    }

    public void soundwarning(double distance){
        Vibrator vibrator; //?????? ?????? ??????
        MediaPlayer player; //?????? ?????? ??????
        vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long []{500,1000,500,1000},-1);

        if(distance<=500 && distance>=200)
        {
            player=MediaPlayer.create(this,R.raw.emergency_500m);
            player.start();
        }
        else if(distance<200&&distance>=100)
        {
            player=MediaPlayer.create(this,R.raw.emergency_200m);
            Log.d(TAG, "distance100??????: "+distance);
            player.start();
        }
        else if(distance<100&&distance>=50)
        {
            player=MediaPlayer.create(this,R.raw.emergency_nearby);
            Log.d(TAG, "distance50??????: "+distance);
            player.start();
        }
        else if(distance<50&&distance>=10)
        {
            player=MediaPlayer.create(this,R.raw.little_urgent);
            Log.d(TAG, "distance10??????: "+distance);
            player.start();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void viewwarning(){
        Animation mAnimation = new AlphaAnimation(1.0f, 0.3f);
        mAnimation.setDuration(1000);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(3);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mLayout.startAnimation(mAnimation);
    }

    public double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);

        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);

        distance = locationA.distanceTo(locationB);
        return distance; // m ??????
    }

    private void startLocationUpdates() //????????? ??????????????? ?????? ?????????????????? ??????
    {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : ????????? ???????????? ??????");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());


            if (checkPermission())
                mMap.setMyLocationEnabled(true); // ???????????? ????????? ??????????????? ??????
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap != null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setDefaultLocation()
    {
        //????????? ??????, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(35.832303, 128.757473);
        String markerTitle = "???????????? ????????? ??? ??????";
        String markerSnippet = "?????? ???????????? GPS ?????? ?????? ???????????????";

        int i=0;

        if (currentMarker[i] != null) currentMarker[i].remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker[i] = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }

    //??????????????? ????????? ????????? ????????? ?????? ????????????
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   )
        {
            return true;
        }

        return false;
    }

    /*
     * ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ??????????????????.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????

            boolean check_result = true;
            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults)
            {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result )
            {
                // ???????????? ??????????????? ?????? ??????????????? ???????????????.
                startLocationUpdates();
            }
            else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1]))
                {
                    // ???????????? ????????? ????????? ???????????? ?????? ?????? ???????????? ????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????. ", Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                }else {
                    // "?????? ?????? ??????"??? ???????????? ???????????? ????????? ????????? ???????????? ??????(??? ??????)?????? ???????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }

        }
    }

    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n" + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS ????????? ?????????");
                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        Response.Listener<String> responseListener = new Response.Listener<String>() { // php ?????? ?????? ??????
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");

                    if(success){
                        Log.d(TAG, "Success !");
                    }
                    else{
                        Log.d(TAG, "Fail..");
                    }
                }

                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        // ????????? Volley??? ???????????? ??????
        DestroyRequest destroyRequest = new DestroyRequest(PhoneNum, responseListener);
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(destroyRequest);

        Log.d(TAG, "Destroy !!");
    }
}