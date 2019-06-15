package WalkTogether.com.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import WalkTogether.com.R;

public class CameraFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final int REQUEST_PERMISSION_LOCATION = 0;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    private Double latitude, longitude;
    private LatLng latlng;

    private RadioGroup mood_group;
    private RadioButton mood_smile, mood_normal, mood_wow, mood_sad, mood_angry;
    private ImageView upload_image;
    private Bitmap bitmap;
    private TextView upload_date;
    private Button upload_btn;
    private String Uid;

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
        return inflater.inflate(R.layout.content_camera, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        upload_image = getActivity().findViewById(R.id.upload_image);
        upload_date = getActivity().findViewById(R.id.upload_date);
        upload_btn = getActivity().findViewById(R.id.upload_button);
        mood_group = getActivity().findViewById(R.id.radioGroup);
        mood_smile = getActivity().findViewById(R.id.mood_smile);
        mood_normal = getActivity().findViewById(R.id.mood_normal);
        mood_wow = getActivity().findViewById(R.id.mood_wow);
        mood_sad = getActivity().findViewById(R.id.mood_sad);
        mood_angry = getActivity().findViewById(R.id.mood_angry);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Log.d("check_WalkFragment", mAuth.getUid());
        Uid = mAuth.getUid();

        String time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Date());
        upload_date.setText(time);

        Bundle bundle = getArguments();
        bitmap = bundle.getParcelable("bitmap");
        upload_image.setImageBitmap(bitmap);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION);
        }

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                latitude = 0.0;
                longitude = 0.0;
                if(latlng != null){
                    latitude = latlng.latitude;
                    longitude = latlng.longitude;
                }
                final String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                StorageReference mstorage = FirebaseStorage.getInstance().getReference();
                Bitmap bitmap = ((BitmapDrawable) upload_image.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = mstorage.child(time).putBytes(data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("check_camera", String.valueOf(taskSnapshot));
                        DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference();
                        mdatabase.child("Users").child(Uid).child("Photo").child(time).child("lat").setValue(latitude);
                        mdatabase.child("Users").child(Uid).child("Photo").child(time).child("lng").setValue(longitude);
                        mdatabase.child("Users").child(Uid).child("Photo").child(time).child("mood").setValue(0);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                });
            }
        });

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_LOCATION){
            buildGoogleApiClient();
        }
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
            Toast.makeText(getContext(), latlng.latitude + "," + latlng.longitude, Toast.LENGTH_LONG).show();
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

}
