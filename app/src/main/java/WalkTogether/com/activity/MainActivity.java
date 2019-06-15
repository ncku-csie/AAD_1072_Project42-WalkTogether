package WalkTogether.com.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


import WalkTogether.com.R;
import WalkTogether.com.fragment.CameraListFragment;
import WalkTogether.com.fragment.ChatroomFragment;
import WalkTogether.com.fragment.MapFragment;
import WalkTogether.com.fragment.MatchFragment;
import WalkTogether.com.service.StepService;
import WalkTogether.com.fragment.WalkFragment;
import WalkTogether.com.service.FCMService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private static final int JOB_ID = 0;
    private static final long ONE_INTERVAL = 1 * 1000L;

    private String mode;
    private BottomNavigationView btmnav;
    private SensorManager mSensorManager;
    private DatabaseReference mdatabase;
    private FirebaseAuth mAuth;
    private float now_step;
    private String now;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = findViewById(R.id.toobar_title);
        setSupportActionBar(toolbar);

        btmnav = findViewById(R.id.bottom_nav);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        Log.d("check_MainActivity", mAuth.getUid());

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        now = ft.format(date);




        //查詢使用者是否有配對, 0：無配對
        mdatabase = FirebaseDatabase.getInstance().getReference();
        mdatabase.child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                now_step = Float.parseFloat(dataSnapshot.child("step").child(now).getValue() + "");
                mode = String.valueOf(dataSnapshot.child("match").getValue());
                Log.d("check_MainActivity", mode);
                if(mode.equals("0")){
                    LoadFragment(new MatchFragment());
                    Log.d("check_MainActivity", "單人");
                }else{
                    LoadFragment(new WalkFragment());
                    Log.d("check_MainActivity", "雙人");
                }
             }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

      btmnav.setOnNavigationItemSelectedListener(MainActivity.this);

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
        if (id == R.id.action_info) {
            Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.Logout){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
    private Boolean LoadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment fragment = null;

        switch (menuItem.getItemId()){
            case R.id.imageButton_walk:
                if(mode.equals("0")){
                    fragment = new MatchFragment();
                }else{
                    fragment = new WalkFragment();
                }
                break;
            case R.id.imageButton_camera:
                fragment = new CameraListFragment();
                break;
            case R.id.imageButton_map:
                fragment = new MapFragment();
                break;
            case R.id.imageButton_line:
                if(mode.equals("0")){
                    fragment = new MatchFragment();
                }else{
                    fragment = new ChatroomFragment();
                }
                break;
        }
        return LoadFragment(fragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("確認視窗")
                    .setMessage("確定要結束應用程式嗎?")
                    .setPositiveButton("確定",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    finish();
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
        }
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(MainActivity.this, FCMService.class));
        startService(new Intent(MainActivity.this, StepService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
