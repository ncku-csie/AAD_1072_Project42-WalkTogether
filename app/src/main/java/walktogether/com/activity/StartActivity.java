package walktogether.com.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import walktogether.com.R;


public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 4000);



        mAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    if(user.isEmailVerified()){
//                        Toast.makeText(StartActivity.this, "信箱已驗證", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(StartActivity.this, MainActivity.class));
                                finish();
                            }
                        }, 2000);

                    }else{
                        Toast.makeText(StartActivity.this, "尚未驗證帳號，請檢查信箱。", Toast.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(StartActivity.this, LoginActivity.class));
                                finish();
                            }
                        }, 2000);
                    }
                }else{
                    // 登入失敗, 導向登入註冊頁面
//                    Toast.makeText(StartActivity.this, "尚未註冊帳號。", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(StartActivity.this, LoginActivity.class));
                            finish();
                        }
                    }, 2000);
                }
            }
        };

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        if (authStateListener != null){
            mAuth.removeAuthStateListener(authStateListener);
        }
        super.onStop();
    }
}
