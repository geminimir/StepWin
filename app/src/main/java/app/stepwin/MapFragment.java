package app.stepwin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback{

    MapView map;
    View rootview;
    GoogleMap mMap;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.activity_map, container, false);
        return rootview;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        map = (MapView) rootview.findViewById(R.id.map);
        if(map != null) {
            map.onCreate(null);
            map.onResume();
            map.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        if(isNetworkConnected()) {
            ArrayList<Double> lati = new ArrayList<Double>();
            ArrayList<Double> longi = new ArrayList<Double>();
            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mMap.setMyLocationEnabled(false);
            //Initialize Google Play Services
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    DatabaseHandler db = new DatabaseHandler(getActivity());
                    //db.DeleteTable();
                    Cursor c = db.RetrieveLocations();
                    if (c.moveToFirst()) {
                        do {
                            longi.add(c.getDouble(0));
                            lati.add(c.getDouble(1));

                        } while (c.moveToNext());
                    }
                    Double[] longitude = new Double[longi.size()];
                    Double[] latitude = new Double[lati.size()];
                    longi.toArray(longitude);
                    lati.toArray(latitude);
                    for (int i = 0; i < longitude.length - 1; i++) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(longitude[i], latitude[i]), 12.0f));
                        DrawLine(new LatLng(longitude[i], latitude[i]), new LatLng(longitude[i + 1], latitude[i + 1]));
                    }
                    if (longitude.length > 1) {
                        Marker starting = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(longitude[0], latitude[0]))
                                .title("Starting Point"));
                        Marker ending = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(longitude[longitude.length - 1], latitude[latitude.length - 1]))
                                .title("Current Point"));
                    }

                }
            } else {
                mMap.setMyLocationEnabled(true);
            }
        }
        else
            NetworkAlert();
    }
    public  void DrawLine(LatLng location1, LatLng location2) {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(location1)
                .add(location2);
        mMap.addPolyline(polylineOptions);
    }
    private void NetworkAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Connection Failed");
        alertDialogBuilder.setMessage("Please check your internet connection");
        alertDialogBuilder.setPositiveButton("TRY AGAIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //getActivity().finish();
                //startActivity(getIntent());
            }
        });
        alertDialogBuilder.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertD = alertDialogBuilder.create();
        alertD.show();
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
