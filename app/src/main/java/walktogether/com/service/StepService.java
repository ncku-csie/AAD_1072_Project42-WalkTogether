package walktogether.com.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

import walktogether.com.R;
import walktogether.com.database.StepDataDao;
import walktogether.com.database.StepEntity;
import walktogether.com.activity.MainActivity;
import walktogether.com.constant.Constants;
import walktogether.com.utils.TimeUtil;


public class StepService extends Service implements SensorEventListener {

    public static final String TAG = "StepService";
    private static String CURRENT_DATE;
    private int CURRENT_STEP;
    private static int saveDuration = 3000;
    private SensorManager sensorManager;
    private StepDataDao stepDataDao;
    private static int stepSensor = -1;
    private BroadcastReceiver mInfoReceiver;
    private TimeCount timeCount;
    private Messenger messenger = new Messenger(new MessengerHandler());
    private boolean hasRecord;
    private int hasStepCount;
    private int previousStepCount;
    private Notification.Builder builder;

    private NotificationManager notificationManager;
    private Intent nfIntent;


    @Override
    public void onCreate() {
        super.onCreate();
        initBroadcastReceiver();
        new Thread(new Runnable() {
            public void run() {
                getStepDetector();
            }
        }).start();
        startTimeCount();
        initTodayData();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        builder = new Notification.Builder(this.getApplicationContext());

        nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("今日步数"+CURRENT_STEP+"步")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("加油，記得要多走路");
        Notification stepNotification = builder.build();

        notificationManager.notify(110,stepNotification);

        startForeground(110, stepNotification);

        return START_STICKY;
    }

    private class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FROM_CLIENT:
                    try {
                        //將當前數據發送出去
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain(null, Constants.MSG_FROM_SERVER);
                        Bundle bundle = new Bundle();
                        bundle.putInt("steps", CURRENT_STEP);
                        replyMsg.setData(bundle);
                        messenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);

        mInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case Intent.ACTION_SCREEN_OFF:
                        saveDuration = 10000;
                        break;
                    case Intent.ACTION_SHUTDOWN:
                        saveStepData();
                        break;
                    case Intent.ACTION_USER_PRESENT:
                        saveDuration = 3000;
                        break;
                    case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                        saveStepData();
                        break;
                    case Intent.ACTION_DATE_CHANGED:
                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_TIME_TICK:
                        saveStepData();
                        isNewDay();
                        break;
                    default:
                        break;
                }
            }
        };

        registerReceiver(mInfoReceiver, filter);
    }


    private void initTodayData() {
        CURRENT_DATE = TimeUtil.getCurrentDate();
        stepDataDao = new StepDataDao(getApplicationContext());
        StepEntity entity = stepDataDao.getCurDataByDate(CURRENT_DATE);
        if (entity == null) {
            CURRENT_STEP = 0;
        } else {
            CURRENT_STEP = Integer.parseInt(entity.getSteps());
        }
    }

    private void isNewDay() {
        String time = "00:00";
        if (time.equals(new SimpleDateFormat("HH:mm").format(new Date())) ||
                !CURRENT_DATE.equals(TimeUtil.getCurrentDate())) {
            initTodayData();
        }
    }

    private void getStepDetector() {
        if (sensorManager != null) {
            sensorManager = null;
        }
        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        int VERSION_CODES = Build.VERSION.SDK_INT;
        if (VERSION_CODES >= 19) {
            addCountStepListener();
        }
    }

    private void addCountStepListener() {
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            stepSensor = 0;
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (detectorSensor != null) {
            stepSensor = 1;
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (stepSensor == 0) {
            int tempStep = (int) event.values[0];
            if (!hasRecord) {
                hasRecord = true;
                hasStepCount = tempStep;
            } else {
                int thisStepCount = tempStep - hasStepCount;
                CURRENT_STEP += (thisStepCount - previousStepCount);
                previousStepCount = thisStepCount;
            }
        } else if (stepSensor == 1) {
            if (event.values[0] == 1.0) {
                CURRENT_STEP++;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void startTimeCount() {
        timeCount = new TimeCount(saveDuration, 1000);
        timeCount.start();
    }


    private class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            timeCount.cancel();
            saveStepData();
            startTimeCount();
        }
    }

    private void saveStepData() {
        StepEntity entity = stepDataDao.getCurDataByDate(CURRENT_DATE);
        if (entity == null) {
            entity = new StepEntity();
            entity.setCurDate(CURRENT_DATE);
            entity.setSteps(String.valueOf(CURRENT_STEP));

            stepDataDao.addNewData(entity);
        } else {
            entity.setSteps(String.valueOf(CURRENT_STEP));

            stepDataDao.updateCurData(entity);
        }

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.welcome))
                .setContentTitle("今日步數"+CURRENT_STEP+"步")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("加油，要記得勤加運動");

        Notification stepNotification = builder.build();
        notificationManager.notify(110,stepNotification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        unregisterReceiver(mInfoReceiver);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}

