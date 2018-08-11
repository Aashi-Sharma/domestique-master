package com.example.tony.domestique;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import java.util.List;
import java.util.Map;


public class ProfessionalMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    Location nLastLocation;
    LocationRequest nLocationRequest;
    private Button mLogout, mSettings;

    private String customerId = "";
    private Boolean isLoggingOut = false;
    private  LatLng latLng;
    private Boolean firstTime = true;
    private String serviceType = "";
    private String pkgName = "com.example.tony.domestique";
    protected static boolean isVisible = true;
    private LinearLayout customerDetails;
    private TextView customerPhone,customerName;
    private ImageView customerImage;
    private Button cancelRequest, acceptRequest, shedules;
    private Boolean accepting = false;


    Marker YourMarker;
    private FusedLocationProviderClient mFusedLocationClient;


    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        customerDetails = findViewById(R.id.customerDetails);

        customerName = (TextView) findViewById(R.id.customerName);
        customerPhone = (TextView) findViewById(R.id.customerPhone);

        customerImage = (ImageView) findViewById(R.id.customerImage);

        cancelRequest = findViewById(R.id.cancelRequest);
        acceptRequest = findViewById(R.id.acceptRequest);
        shedules = findViewById(R.id.shedules);

        polylines = new ArrayList<>();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        mLogout = (Button) findViewById(R.id.logout);
        mSettings = (Button) findViewById(R.id.settings);

        shedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfessionalMapActivity.this, ProfessionalShedules.class);
                startActivity(intent);
                return;
            }
        });
        acceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(customerId != ""){

                    accepting = true;

                    DatabaseReference  customerReqRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId);
                    customerReqRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                final String MyuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference myref = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(MyuserId).child("Requests").child(customerId).child(snapshot.getKey());
                                if(snapshot.getKey() == "services"){
                                    DatabaseReference secondRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("services");
                                    secondRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot snap: dataSnapshot.getChildren()) {
                                                DatabaseReference myref = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(MyuserId).child("Requests").child(customerId).child("services").child(snap.getKey());
                                                myref.setValue(snap.getValue());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }else{
                                    myref.setValue(snapshot.getValue());
                                }
                            }
                            CancelRequest();
                            Toast.makeText(getApplicationContext(), "Request Accepted", Toast.LENGTH_SHORT).show();
                            accepting = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfessionalMapActivity.this, ProfessionalDetails.class);
                startActivity(intent);
                return;

            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfessionalMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        getAssignedCustomer();
    }

    private void getAssignedCustomer(){
        String professionalId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(professionalId).child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerDetail();

                }else{
                    if(!accepting){
                    customerDetails.setVisibility(View.GONE);
                    erasePolylines();
                    customerId = "";
                    if(pickupMarker != null){
                        pickupMarker.remove();
                    }
                    if(assignedCustomerPickupLocationRefListener != null){
                        assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
                    }
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private void getAssignedCustomerPickupLocation(){

         assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");

        assignedCustomerPickupLocationRefListener =  assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());

                    }
                    if(map.get(1) != null){
                        locationLong = Double.parseDouble(map.get(1).toString());

                    }
                    LatLng professionalLatLng =  new LatLng(locationLat, locationLong);
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(professionalLatLng).title("Customer Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_customer)));
                    getRouteToMarker(professionalLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getRouteToMarker(LatLng professionalLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(latLng, professionalLatLng)
                .build();
        routing.execute();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        if(firstTime){
            firstTime = false;
        }


        String MyuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference Dbref = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(MyuserId).child("service");

        Dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        serviceType = dataSnapshot.getValue().toString();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mMap = googleMap;
        nLocationRequest = new LocationRequest();
        nLocationRequest.setInterval(1000);
        nLocationRequest.setFastestInterval(1000);
        nLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mFusedLocationProviderClient.requestLocationUpdates(nLocationRequest, mLocationCalback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);


            }else{
                checkLocationPermission();
            }
        } else{
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
                    if(isVisible && serviceType != ""){
                        if(getApplicationContext() != null){
                            cancelRequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CancelRequest();

                                }
                            });


                            latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                            if(YourMarker != null){
                                YourMarker.remove();
                            }
                            YourMarker=  mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_location)));
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("professionalAvailable").child(serviceType);
                            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("professionalWorking").child(serviceType);
                            GeoFire geoFireAvailable = new GeoFire(refAvailable);
                            GeoFire geoFireWorking = new GeoFire(refWorking);

                            switch (customerId){
                                case "":
                                    geoFireWorking.removeLocation(userId);
                                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                                    break;
                                default:

                                    geoFireAvailable.removeLocation(userId);
                                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                                    break;
                            }


                        }
                    }



                }



        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){


                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("Give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(ProfessionalMapActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                .create()
                .show();
            }
            else{
                ActivityCompat.requestPermissions(ProfessionalMapActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

private void CancelRequest(){
    erasePolylines();
    customerDetails.setVisibility(View.GONE);
    customerName.setText("");
    customerPhone.setText("");
    customerImage.setImageResource(R.mipmap.ic_user);


    // requestBol = false;
    //geoQuery.removeAllListeners();
    //professionalLocationRef.removeEventListener(professionalLocationRefListener);
    if(customerId != ""){


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);


        String MeuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference professionalRef = FirebaseDatabase.getInstance().getReference().child("users").child("professionals").child(MeuserId).child("customerRideId");
        professionalRef.removeValue();
        customerId = "";
    }



    if(pickupMarker != null){
        pickupMarker.remove();
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

    @Override
    protected void onResume() {
        super.onResume();
        isVisible = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isVisible){
            try {

                disconnectDriver();
                isVisible = false;

            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Error Pause", Toast.LENGTH_LONG).show();
                isVisible = false;
            }
        }


}

    private void connectDriver(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(nLocationRequest, mLocationCalback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    private void disconnectDriver(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCalback);
        }
        if(customerId != ""){


            String MyuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference Dbref2 = FirebaseDatabase.getInstance().getReference("professionalWorking").child(serviceType).child(MyuserId);
            Dbref2.removeValue();

        }
        else{

            String MyuserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference Dbref3 = FirebaseDatabase.getInstance().getReference("professionalAvailable").child(serviceType).child(MyuserId);
            Dbref3.removeValue();

        }
    }

    public boolean isForeground(String PackageName){
        // Get the Activity Manager
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        // Get a list of running tasks, we are only interested in the last one,
        // the top most so we give a 1 as parameter so we only get the topmost.
        List< ActivityManager.RunningTaskInfo > task = manager.getRunningTasks(1);

        // Get the info we need for comparison.
        ComponentName componentInfo = task.get(0).topActivity;

        // Check if it matches our package name.
        if(componentInfo.getPackageName().equals(PackageName)) return true;

        // If not then our app is not on the foreground.
        return false;
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

    private void getAssignedCustomerDetail(){
        customerDetails.setVisibility(View.VISIBLE);
        DatabaseReference custRef = FirebaseDatabase.getInstance().getReference().child("users").child("customers").child(customerId);

        custRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){

                        customerName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){

                        customerPhone.setText(map.get("phone").toString());

                    }
                    if(map.get("profileImageUrl") != null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(customerImage);
                    }





                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
