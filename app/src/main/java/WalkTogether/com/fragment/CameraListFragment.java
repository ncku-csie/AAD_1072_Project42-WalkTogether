package WalkTogether.com.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import WalkTogether.com.Photo;
import WalkTogether.com.R;
import WalkTogether.com.adapter.RecycleViewAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class CameraListFragment extends Fragment {

    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private static final int REQUEST_CAMERA = 50;
    private RecyclerView recyclerView;
    private ArrayList<Photo> listphotos;
    private RecycleViewAdapter myAdapter;

    private String Uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_camera_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listphotos = new ArrayList<>();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Uid = mAuth.getUid();

        recyclerView = getActivity().findViewById(R.id.camera_recyclerView);
        FloatingActionButton fab = getActivity().findViewById(R.id.btn_camera);

        download_Img_view();
//        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CAPTURE_IMAGE){
            if(resultCode == Activity.RESULT_OK){
                Bitmap bmp = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // convert byte array to Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                        byteArray.length);
                Bundle bundle = new Bundle();
                bundle.putParcelable("bitmap", bitmap);


                Fragment fragment = new CameraFragment();
                fragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        }
        else if(requestCode == REQUEST_CAMERA){
            Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
        }
    }

    private void download_Img_view(){
        DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference();
        mdatabase.child("Users").child(Uid).child("Photo").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("check_cameralist", String.valueOf(dataSnapshot));
                listphotos.add(new Photo(String.valueOf(dataSnapshot.getKey()),String.valueOf(dataSnapshot.child("mood").getValue())));
                myAdapter = new RecycleViewAdapter(getActivity(), listphotos);
                recyclerView.setAdapter(myAdapter);
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
