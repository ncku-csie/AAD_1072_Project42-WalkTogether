package walktogether.com.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import walktogether.com.R;

public class LoginActivity extends AppCompatActivity {

    private EditText Edit_email;
    private EditText Edit_password;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Edit_email = findViewById(R.id.Edit_Account);
        Edit_password = findViewById(R.id.Edit_Password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }
    public void Btn_Register(View v){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
    public void Btn_Login(View v){
        String email = "";
        String password = "";
        if(!Edit_email.getText().toString().equals("")){
            email = Edit_email.getText().toString();
        }else{
            Toast.makeText(LoginActivity.this, "信箱未填寫", Toast.LENGTH_SHORT).show();
        }
        if(!Edit_password.getText().toString().equals("")){
            password = Edit_password.getText().toString();
        }else{
            Toast.makeText(LoginActivity.this, "密碼未填寫", Toast.LENGTH_SHORT).show();
        }
        if((!email.equals("")) && (!password.equals(""))){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        if(mAuth.getCurrentUser().isEmailVerified()){
                            Toast.makeText(LoginActivity.this, "登入成功!!!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this, "尚未驗證信箱", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(LoginActivity.this, "登入失敗!!!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 攔截返回鍵
            new AlertDialog.Builder(LoginActivity.this)
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

}
