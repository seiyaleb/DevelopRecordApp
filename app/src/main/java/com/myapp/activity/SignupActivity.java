package com.myapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.R;

public class SignupActivity extends AppCompatActivity {

    private EditText et_mail,et_password;
    public FirebaseAuth auth;
    private String TAG = "SIGNUP";
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        et_mail = findViewById(R.id.et_mail);
        et_password = findViewById(R.id.et_password);
        auth = FirebaseAuth.getInstance();
        view = findViewById(android.R.id.content);

        //登録ボタン選択時、メアドとパスワードでユーザー登録
        Button bt_signup = findViewById(R.id.bt_signup);
        bt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createAccount(et_mail.getText().toString(),et_password.getText().toString());
            }
        });
    }

    //ユーザー登録
    private void createAccount(String email, String password) {

        //中身があるかチェック
        //ない場合はスナックバー表示
        if(!checkEmpty(email,password)) {
            Snackbar.make(view,getResources().getString(R.string.sb_no_input),Snackbar.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //登録に成功した場合、ログイントップアクティビティに遷移
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Intent intent = new Intent(SignupActivity.this,LoginTopActivity.class);
                            intent.putExtra("key","signup");
                            startActivity(intent);

                        } else {

                            //登録に失敗した場合、ダイアログ表示
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            new AlertDialog.Builder(SignupActivity.this)
                                    .setTitle("Error!")
                                    .setMessage(task.getException().getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                });
    }

    //中身があるか判定
    public boolean checkEmpty(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Log.d(TAG, "何も記入されていません");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Log.d(TAG, "何も記入されていません");
            return false;
        }
        return true;
    }
}