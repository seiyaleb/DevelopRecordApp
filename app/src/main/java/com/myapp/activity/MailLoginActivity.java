package com.myapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.myapp.R;

public class MailLoginActivity extends AppCompatActivity {

    private EditText et_mail,et_password;
    public FirebaseAuth auth;
    private String TAG = "SIGNIN";
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_login);

        et_mail = findViewById(R.id.et_mail);
        et_password = findViewById(R.id.et_password);
        auth = FirebaseAuth.getInstance();
        view = findViewById(android.R.id.content);

        //ログインボタンを選択時、メアドとパスワードでログイン
        Button bt_login = findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn(et_mail.getText().toString(),et_password.getText().toString());
            }
        });
    }

    private void signIn(String email, String password) {

        //中身があるかチェック
        //ない場合はスナックバー表示
        if (!checkEmpty(email,password)) {

            Snackbar.make(view,getResources().getString(R.string.sb_no_input),Snackbar.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            //ログイン成功時
                            Log.d(TAG, "signInWithEmail:success");

                            //ログイン判定データをSharedPreferencesに保存
                            SharedPreferences dataStore = getSharedPreferences("DataStore", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = dataStore.edit();
                            editor.putBoolean("key", true);
                            editor.apply();

                            //Intent経由で記録アクティビティに遷移
                            Intent intent = new Intent(MailLoginActivity.this, RecordActivity.class);
                            startActivity(intent);


                        } else {
                            //ログインに失敗した場合は、ダイアログ表示
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            new AlertDialog.Builder(MailLoginActivity.this)
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