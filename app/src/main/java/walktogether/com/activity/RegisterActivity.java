package walktogether.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import walktogether.com.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText Edit_name, Edit_email, Edit_password, Edit_age;
    private RadioGroup radioGroup;
    private RadioButton radioButton_male, radioButton_female;
    private String sex;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Edit_name = findViewById(R.id.Register_name);
        Edit_email = findViewById(R.id.Register_email);
        Edit_password = findViewById(R.id.Register_password);
        Edit_age = findViewById(R.id.Register_age);
        radioGroup = findViewById(R.id.Register_sex);
        radioButton_male = findViewById(R.id.sex_male);
        radioButton_female = findViewById(R.id.sex_female);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Log.d("check_Register", String.valueOf(mAuth.getCurrentUser()));
        monitoringRadioGrop();

    }
    public void Register(View v){
        final String name = Edit_name.getText().toString();
        final String email = Edit_email.getText().toString();
        final String password = Edit_password.getText().toString();
        final String age = Edit_age.getText().toString();


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //將資料寫進資料庫
                                    DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference();
                                    mdatabase = mdatabase.child("Users").child(user.getUid());
                                    mdatabase.child("name").setValue(name);
                                    mdatabase.child("email").setValue(email);
                                    mdatabase.child("password").setValue(password);
                                    mdatabase.child("age").setValue(age);
                                    mdatabase.child("match").setValue(0);
                                    mdatabase.child("sex").setValue(sex);
                                    //寄驗證信
                                    sendVerificationEmail();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                }else{
                                    Log.d("check_register", "createUserWithEmail:failure", task.getException());
                                }

                            }
                        }
                );
    }
    private void sendVerificationEmail()
    {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "驗證信已經寄出, 請查看", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "驗證信寄出失敗", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void monitoringRadioGrop(){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.sex_male:
                        sex = "male";
                        break;
                    case R.id.sex_female:
                        sex = "female";
                        break;
                    default:
                        sex = "male";
                        break;
                }
            }
        });
    }
}
