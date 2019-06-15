package WalkTogether.com.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import WalkTogether.com.R;
import WalkTogether.com.activity.MainActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();
        initFCM();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("check_fcm_data", "Message data payload: " + remoteMessage.getData().get("title"));
            Log.d("check_fcm_data", "Message data payload: " + remoteMessage.getData().get("body"));
            ShowNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("check_fcm", "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void initFCM(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                Log.d("check_fcm", "initFCM: token: " + task.getResult().getToken());
                sendRegistrationToServer(task.getResult().getToken());
            }
        });
    }

    private void sendRegistrationToServer(String token) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth!=null){
            Log.d("check_fcm", "sendRegistrationToServer: sending token to server: " + token);
            Log.d("check_fcm", mAuth.getUid());
            DatabaseReference mdatabse = FirebaseDatabase.getInstance().getReference();
            mdatabse.child("Users").child(mAuth.getUid()).child("fcm_token").setValue(token);
        }
    }

    private void ShowNotification(String title, String body){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_ID = "LET'S_WALK";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_ID, "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Let's walk channel");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationbuilder = new NotificationCompat.Builder(this, NOTIFICATION_ID);
        Intent ResultIntent = new Intent(this, MainActivity.class);
        PendingIntent ResultPendingIntent = PendingIntent.getActivity(this, 1, ResultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationbuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(ResultPendingIntent);
        notificationManager.notify(1, notificationbuilder.build());
    }

}
