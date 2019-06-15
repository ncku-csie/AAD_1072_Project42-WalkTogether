package WalkTogether.com.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class StepService extends Service {

    private SensorManager mSensorManager;
    private DatabaseReference mDatabse;
    private String uid;
    private float now_step;
    private String now;
    private TimerTask timerTask;
    private Timer timer;
    private ServiceConnection conn;

    @Override
    public void onCreate() {
        super.onCreate();

        uid = FirebaseAuth.getInstance().getUid();
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        now = ft.format(date);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDatabse = FirebaseDatabase.getInstance().getReference();
        mDatabse.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                now_step = Float.parseFloat(dataSnapshot.child("step").child(now).getValue() + "");

                mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

                Sensor mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                Sensor mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

                if (mSensorManager == null || mStepCounterSensor == null || mStepDetectorSensor == null) {
                    Log.d("check_Step", "STEP_COUNTER:" + "設備不支持");
                }

                mSensorManager.registerListener(mSensorEventListener, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

                conn = new ServiceConnection() {

                    @Override
                    public void onServiceConnected(ComponentName name, final IBinder service) {
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                mDatabse.child("Users").child("step").child(now).setValue(now_step);
                            }
                        };
                        timer = new Timer();
                        timer.schedule(timerTask, 0, 3000);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                    }
                };
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        private float step;
        private float pre_step = 0;

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                step = sensorEvent.values[0];
                if(step == 1){
                    now_step++;
                    Log.d("check_Step", "STEP_COUNTER:" + now_step);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

}

