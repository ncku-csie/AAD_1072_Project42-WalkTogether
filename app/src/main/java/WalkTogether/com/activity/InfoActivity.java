package WalkTogether.com.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import WalkTogether.com.R;

import static com.prolificinteractive.materialcalendarview.MaterialCalendarView.SELECTION_MODE_MULTIPLE;

public class InfoActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView info_name, info_age, info_sex;
    private String state;
    private MaterialCalendarView calendarview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView title = findViewById(R.id.toobar_title);
        title.setText("個人資訊");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        info_name = findViewById(R.id.info_name);
        info_age = findViewById(R.id.info_age);
        info_sex = findViewById(R.id.info_sex);
        calendarview = findViewById(R.id.calendarView);
        calendarview.setSelectionMode(SELECTION_MODE_MULTIPLE);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth!=null){
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("Users").child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("check_info", String.valueOf(dataSnapshot));
                    info_name.setText(String.valueOf(dataSnapshot.child("name").getValue()));
                    info_age.setText("年齡 : " + dataSnapshot.child("age").getValue());
                    info_sex.setText("性別 : " + dataSnapshot.child("sex").getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mDatabase.child("Users").child(mAuth.getUid()).child("state").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d("check_info", String.valueOf(dataSnapshot));
                    if(String.valueOf(dataSnapshot.getValue()).equals("1")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date date = sdf.parse(String.valueOf(dataSnapshot.getKey()));
                            calendarview.setDateSelected(date, true);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
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
}
