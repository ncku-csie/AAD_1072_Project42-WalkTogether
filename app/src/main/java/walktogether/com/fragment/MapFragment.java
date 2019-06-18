package walktogether.com.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import walktogether.com.CustomInfoWindowGoogleMap;
import walktogether.com.InfoWindowData;
import walktogether.com.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private ArrayList<String> pic_name, partner_pic_name;
    private ArrayList<String> content, partner_content;
    private ArrayList<Double> lat, partner_lat;
    private ArrayList<Double> lng, partner_lng;
    private LatLng latlng;
    private String Uid, partner_uid;

    private final int REQUEST_PERMISSION_LOCATION = 0;

    protected synchronized void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_map, container, false);
    }



    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getUid();

        pic_name = new ArrayList<>();
        lat = new ArrayList<>();
        lng = new ArrayList<>();
        content = new ArrayList<>();
        partner_pic_name = new ArrayList<>();
        partner_lat = new ArrayList<>();
        partner_lng = new ArrayList<>();
        partner_content = new ArrayList<>();

        buildGoogleApiClient();

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        final DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference();
        mdatabase.child("Users").child(Uid).child("Photo").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("check_Map", String.valueOf(dataSnapshot));
                double latitude = Double.parseDouble(String.valueOf(dataSnapshot.child("lat").getValue()));
                double longitude = Double.parseDouble(String.valueOf(dataSnapshot.child("lng").getValue()));
                if(latitude != 0.0 && longitude != 0.0){
                    pic_name.add(String.valueOf(dataSnapshot.getKey()));
                    content.add((String.valueOf(dataSnapshot.child("sentence").getValue())));
                    lat.add(latitude);
                    lng.add(longitude);
                }
                DrawMarker();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mdatabase.child("Users").child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                partner_uid = String.valueOf(dataSnapshot.child("partner").getValue());
                Log.d("check_Map", partner_uid);
                if(!partner_uid.equals('0') && !partner_uid.equals('1')){
                    mdatabase.child("Users").child(partner_uid).child("Photo").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            Log.d("check_Map", String.valueOf(dataSnapshot));
                            double latitude = Double.parseDouble(String.valueOf(dataSnapshot.child("lat").getValue()));
                            double longitude = Double.parseDouble(String.valueOf(dataSnapshot.child("lng").getValue()));
                            if(latitude != 0.0 && longitude != 0.0){
                                partner_pic_name.add(String.valueOf(dataSnapshot.getKey()));
                                partner_content.add((String.valueOf(dataSnapshot.child("sentence").getValue())));
                                partner_lat.add(latitude);
                                partner_lng.add(longitude);
                            }
                            DrawMarker_partner();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // 這行指令在 IDE 會出現紅線，不過仍可正常執行，可不予理會
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null)
        {
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();
            latlng = new LatLng(lat, lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18));
        }
        else
        {
            Toast.makeText(getContext(), "偵測不到定位，請確認定位功能已開啟。", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void DrawMarker(){

        for(int i=0;i<lat.size();i++){

            InfoWindowData info = new InfoWindowData(pic_name.get(i), content.get(i));

            CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(getContext());
            mMap.setInfoWindowAdapter(customInfoWindow);

            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            m.setTag(info);
            m.showInfoWindow();
        }
    }
    private void DrawMarker_partner(){

        for(int i=0;i<lat.size();i++){

            InfoWindowData info = new InfoWindowData(partner_pic_name.get(i), partner_content.get(i));

            CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(getContext());
            mMap.setInfoWindowAdapter(customInfoWindow);

            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(partner_lat.get(i), partner_lng.get(i))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
            m.setTag(info);
            m.showInfoWindow();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_LOCATION){
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(getContext(),"沒開GPS",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        } else {
            Toast.makeText(getContext(),"沒開GPS",Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        }
    }
}
