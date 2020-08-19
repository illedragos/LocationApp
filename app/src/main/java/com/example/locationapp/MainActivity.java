package com.example.locationapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int ALL_PERMISION_RESULT = 1111 ;
    private GoogleApiClient client;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ArrayList<String> permissionToRequest;
    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private TextView locationTextView;
    private LocationRequest locationRequest;
    public static final long UPDATE_INTERVAL = 5000;
    public static final long FASTEST_INTERVAL = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.location_textView);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        //Permision we need to request location from user
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionToRequest = permissionToRequest(permissions);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(permissionToRequest.size()>0){
                requestPermissions(permissionToRequest.toArray(
                        new String[permissionToRequest.size()]),
                        ALL_PERMISION_RESULT
                );
            }
        }

        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();


    }

    private ArrayList<String> permissionToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for(String perm : wantedPermissions){
            if(!hasPermission(perm)){
                result.add(perm);
            }
        }
        return result;
    }

    private void checkPlayServices(){

        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(errorCode != ConnectionResult.SUCCESS){
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, errorCode, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(MainActivity.this,"No Services",Toast.LENGTH_LONG).show();
                }
            });
            errorDialog.show();
            finish();
        }
        else{
            Toast.makeText(MainActivity.this,"All is Good",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasPermission(String perm) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(client!=null){
            client.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.disconnect();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkPlayServices();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(client!=null&&client.isConnected()){
            LocationServices.getFusedLocationProviderClient(this)
                    .removeLocationUpdates(new LocationCallback(){

                    });
            client.disconnect();
        }
    }

    private void setTextViewParam(Location location) {

        //////CONVERT TO GRADE MINUTE SECUNDE
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        int latSecondTemp = (int)Math.round(latitude * 3600);
        double latSecond = latitude * 3600;
        int latDegrees = latSecondTemp / 3600;
        latSecondTemp = Math.abs(latSecondTemp % 3600);
        latSecond =Math.abs(latSecond % 3600);
        int latMinutes = latSecondTemp / 60;
        latSecondTemp %= 60;
        latSecond %= 60;
        latSecond=(double)Math.round(latSecond * 100000d) / 100000d;

        int longSecondsTemp = (int)Math.round(longitude * 3600);
        double longSeconds = longitude * 3600;
        int longDegrees = longSecondsTemp / 3600;
        longSecondsTemp = Math.abs(longSecondsTemp % 3600);
        longSeconds = Math.abs(longSeconds % 3600);
        int longMinutes = longSecondsTemp / 60;
        longSecondsTemp %= 60;
        longSeconds %= 60;
        longSeconds=(double)Math.round(longSeconds * 100000d) / 100000d;
        ///////



        locationTextView.setText("LATITUDINE: "+location.getLatitude()+
                "\nLAT(g,m,s) :   "+latDegrees+"g "+latMinutes+"m "+latSecond+"s"+
                "\nLONGITUDINE: "+location.getLongitude()+
                "\nLONG(g,m,s):   "+longDegrees+"g "+longMinutes+"m "+longSeconds+"s"+
                "\nPrecizie: "+location.getAccuracy()+
                "\nAltitudine: " + location.getAltitude()+
                "\nPrecizie Verticala:" + location.getVerticalAccuracyMeters()+
                "\nBearing: " + location.getBearing()+
                "\nProvider: " + location.getProvider()+
                "\nTime: " + location.getTime());
    }




    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }

        fusedLocationProviderClient.getLastLocation().
                addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //Get Last location but it colud be null
                if(location!=null){
                    setTextViewParam(location);
                   /* locationTextView.setText("LAT: "+location.getLatitude()+
                            " Lon:"+location.getLongitude());*/
                }
            }
        });

        startLocationUpdates();
    }


    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this,"PERMISION DENIED BY USER",Toast.LENGTH_SHORT).show();

        }

        LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if(locationResult != null){
                    Location location = locationResult.getLastLocation();
                    setTextViewParam(location);
                   /* locationTextView.setText("Latitudine:"+location.getLatitude()+
                            "Longitudine:"+location.getLongitude());*/
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }
        },null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case ALL_PERMISION_RESULT:
                for(String perm : permissionToRequest){
                    if(hasPermission(perm)){
                        permissionsRejected.add(perm);
                    }
                }
                if(permissionsRejected.size() > 0){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Aceste permisiuni sunt obligatorii pt a folosi aplicatia")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]),ALL_PERMISION_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("CANCEL",null)
                                      .create()
                                       .show();
                        }
                    }
                }else{
                    if(client!=null){
                        client.connect();
                    }
                }
                break;

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){
            setTextViewParam(location);
           /* locationTextView.setText("Latitudine:"+location.getLatitude()+
                    "Longitudine:"+location.getLongitude());*/
        }

    }
}
