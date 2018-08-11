package com.example.tony.domestique;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    Location nLastLocation;
    LocationRequest nLocationRequest;
    private Button mLogout, mRequest;
    private LatLng pickupLocation;
    private Boolean requestBol = false;
    private Marker pickupMarker;
    private Boolean isLoggingOut = false;
    private LinearLayout mProfessionalInfo;
    private ImageView mProfessionalProfileImage;
    private TextView mProfessionalName, mProfessionalPhone;
    private  ArrayList<String> mySerivce = new ArrayList<>();
    private  String category, totalCost, date;
    private Button cancelButton;
    private Boolean seekProfessional = true;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        polylines = new ArrayList<>();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mLogout = (Button) findViewById(R.id.logout);
        mRequest = (Button) findViewById(R.id.request);

        mProfessionalInfo = (LinearLayout) findViewById(R.id.professionalDetails);

        mProfessionalName = (TextView) findViewById(R.id.professionalName);
        mProfessionalPhone = (TextView) findViewById(R.id.professionalPhone);

        mProfessionalProfileImage = (ImageView) findViewById(R.id.professionalImage);

        cancelButton = findViewById(R.id.cancelRequest);

        //Getting Values from Previous Activity

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mySerivce = getIntent().getStringArrayListExtra("services");
        category = (String) getIntent().getSerializableExtra("category");
        totalCost = (String) getIntent().getSerializableExtra("total");
        date = (String) getIntent().getSerializableExtra("date");

        for(String svc : mySerivce){

            Toast.makeText(CustomerMapActivity.this,svc, Toast.LENGTH_SHORT).show();

        }



        mMap = googleMap;
        nLocationRequest = new LocationRequest();
        nLocationRequest.setInterval(1000);
        nLocationRequest.setFastestInterval(1000);
        nLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mFusedLocationProviderClient.requestLocationUpdates(nLocationRequest, mLocationCalback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }else{
                checkLocationPermission();
            }
        }
        else{
            final LocationManager manager = (LocationManager) getSystemService( getApplicationContext().LOCATION_SERVICE );

            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                buildAlertMessageNoGps();
            }

            mFusedLocationProviderClient.requestLocationUpdates(nLocationRequest, mLocationCalback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);


        }


    }

    LocationCallback mLocationCalback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(final Location location : locationResult.getLocations()){

                if(getApplicationContext() != null){
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                    if(!seekProfessional){
                        seekProfessional = true;
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(userId);
                        ref2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    seekProfessional = true;
                                    erasePolylines();
                                    mProfessionalInfo.setVisibility(View.GONE);
                                    mProfessionalName.setText("");
                                    mProfessionalPhone.setText("");
                                    mProfessionalProfileImage.setImageResource(R.mipmap.ic_user);


                                    // requestBol = false;
                                    geoQuery.removeAllListeners();
                                    professionalLocationRef.removeEventListener(professionalLocationRefListener);
                                    if(driverFoundID != null){
                                        DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(driverFoundID).child("customerRideId");
                                        professionalRef.removeValue();
                                        driverFoundID = null;
                                    }
                                    driverFound = false;
                                    radius = 1;

                                    if(mProfessionalMarker != null){
                                        mProfessionalMarker.remove();
                                    }
                                    mRequest.setText("Call a service professional");

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            seekProfessional = true;
                            erasePolylines();
                            mProfessionalInfo.setVisibility(View.GONE);
                            mProfessionalName.setText("");
                            mProfessionalPhone.setText("");
                            mProfessionalProfileImage.setImageResource(R.mipmap.ic_user);


                           // requestBol = false;
                            geoQuery.removeAllListeners();
                            professionalLocationRef.removeEventListener(professionalLocationRefListener);
                            if(driverFoundID != null){
                                DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(driverFoundID).child("customerRideId");
                                professionalRef.removeValue();
                                driverFoundID = null;
                            }
                            driverFound = false;
                            radius = 1;


                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                            GeoFire geoFire = new GeoFire(ref);
                            geoFire.removeLocation(userId);

                            if(mProfessionalMarker != null){
                                mProfessionalMarker.remove();
                            }
                            mRequest.setText("Call a service professional");
                        }
                    });


                    mRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(seekProfessional){
                                if(pickupMarker != null){
                                    pickupMarker.remove();
                                }
                                seekProfessional = false;
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));



                                DatabaseReference myref = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(userId);
                                myref.child("category").setValue(category);
                                for(int i = 0 ; i < mySerivce.size(); i++){
                                    myref.child("services").child(mySerivce.get(i)).setValue(true);
                                }
                                myref.child("totalcost").setValue(totalCost);
                                myref.child("date").setValue(date);


                                pickupLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                pickupMarker =  mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Your Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location)));
                                mRequest.setText("Getting you a Service Professional");

                                getClosestProfessional();
                            }



                        }
                    });



                }

            }
        }
    };

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;
    GeoQuery geoQuery;
    private void getClosestProfessional(){

        DatabaseReference professionalLocation =FirebaseDatabase.getInstance().getReference().child("professionalAvailable").child(category);
        GeoFire geoFire = new GeoFire(professionalLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
               // if(!driverFound && requestBol){
                    if(!driverFound){
                    driverFound = true;
                    driverFoundID = key;
                    DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(driverFoundID);
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);
                    professionalRef.updateChildren(map);
                    getProfessionalLocation();
                    mRequest.setText("Looking For Service Professionals.....");


                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius= radius + 10;
                    getClosestProfessional();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mProfessionalMarker;
    private DatabaseReference professionalLocationRef;
    private ValueEventListener professionalLocationRefListener;

    public void getProfessionalLocation(){

        professionalLocationRef = FirebaseDatabase.getInstance().getReference().child("professionalWorking").child(category).child(driverFoundID).child("l");
        professionalLocationRefListener = professionalLocationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                   // if(dataSnapshot.exists() && requestBol){
                        if(dataSnapshot.exists()){
                        List<Object> map = (List <Object>) dataSnapshot.getValue();
                        double locationLat = 0;
                        double locationLong = 0;
                        getAssignedProfessionsalDetail();
                        mRequest.setText("Service Professional Found");
                        if(map.get(0) != null){
                            locationLat = Double.parseDouble(map.get(0).toString());

                        }
                        if(map.get(1) != null){
                            locationLong = Double.parseDouble(map.get(1).toString());

                        }
                        LatLng professionalLatLng =  new LatLng(locationLat, locationLong);
                        if(mProfessionalMarker != null){
                            mProfessionalMarker.remove();

                        }
                        Location loc1 = new Location("");
                        loc1.setLatitude(pickupLocation.latitude);
                        loc1.setLongitude(pickupLocation.longitude);

                        Location loc2 = new Location("");
                        loc2.setLatitude(professionalLatLng.latitude);
                        loc2.setLongitude(professionalLatLng.longitude);

                        float distance = loc1.distanceTo(loc2);
                        mRequest.setText("Professional Found" + String.valueOf(distance));

                        mProfessionalMarker = mMap.addMarker(new MarkerOptions().position(professionalLatLng).title("Service Provider").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_professional)));
                        getRouteToMakrer(professionalLatLng);



                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


    }

    private void getRouteToMakrer(LatLng professionalLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLocation, professionalLatLng)
                .build();
        routing.execute();


    }

    private void getAssignedProfessionsalDetail(){
        mProfessionalInfo.setVisibility(View.VISIBLE);
        DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(driverFoundID);

        professionalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){

                        mProfessionalName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){

                        mProfessionalPhone.setText(map.get("phone").toString());

                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mProfessionalProfileImage);
                    }





                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){


                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("Give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1 : {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationProviderClient.requestLocationUpdates(nLocationRequest, mLocationCalback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Pleae Provide permission", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {
 }

    private void erasePolylines(){

        for(Polyline line: polylines){
            line.remove();
        }
        polylines.clear();

    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }




}
