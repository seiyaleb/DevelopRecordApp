package com.myapp.activity;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.myapp.R;

public class LoginTopActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private String TAG = "LOGIN";
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_top);

        view = findViewById(android.R.id.content);

        //FirebaseAuthインスタンスを取得
        mAuth = FirebaseAuth.getInstance();

        //Googleサインインを設定
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Googleサインインボタンを選択時
        SignInButton signInButton = findViewById(R.id.googleLoginButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        //メールログインボタンを選択時
        Button bt_login_mail = findViewById(R.id.bt_login_mail);
        bt_login_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //メールログインアクティビティに遷移
                Intent intent = new Intent(LoginTopActivity.this,MailLoginActivity.class);
                startActivity(intent);
            }
        });

        //ユーザー登録のリンクを選択時
        TextView tv_link_signin = findViewById(R.id.tv_link_signup);
        tv_link_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //サインアップアクティビティに遷移
                Intent intent = new Intent(LoginTopActivity.this,SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    //Googleサインイン画面に遷移
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activityResultLauncher.launch(signInIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //ユーザーが現在ログインしているかどうかを確認
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            //Intent経由で記録アクティビティに遷移
            Intent intent = new Intent(LoginTopActivity.this, RecordActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //ユーザー登録完了時、スナックバー表示
        String key = getIntent().getStringExtra("key");
        if(key != null && key.equals("signup")) {

            Snackbar.make(view,getResources().getString(R.string.sb_signin),Snackbar.LENGTH_SHORT).show();
        }

        //ログアウトのスナックバーを表示
        SharedPreferences dataStore = getSharedPreferences("DataStore", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = dataStore.edit();
        if(dataStore.getBoolean("key",false)) {
            editor.clear();
            editor.apply();
            Snackbar.make(view,getResources().getString(R.string.sb_logout),Snackbar.LENGTH_SHORT).show();
        }
    }

    //Googleサインイン画面から戻ってきた時に実行
    private ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {

                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {

                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {

                            //Googleサインインに成功した場合、Firebaseで認証する
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d("LOGIN", "firebaseAuthWithGoogle:" + account.getId());
                            firebaseAuthWithGoogle(account.getIdToken());

                        } catch (ApiException e) {

                            //Googleサインインに失敗した場合、スナックバー表示
                            Log.w("LOGIN", "Google sign in failed", e);

                            Snackbar.make(view,getResources().getString(R.string.sb_failure_google_signin),Snackbar.LENGTH_SHORT).show();
                        }

                    }
                }

            });

    ///Googleアカウント情報を元に、firebaseで認証
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //認証成功時
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //ログイン判定データをSharedPreferencesに保存
                            SharedPreferences dataStore = getSharedPreferences("DataStore", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = dataStore.edit();
                            editor.putBoolean("key", true);
                            editor.apply();

                            //Intent経由で記録アクティビティに遷移
                            Intent intent = new Intent(LoginTopActivity.this, RecordActivity.class);
                            startActivity(intent);

                        } else {
                            //認証失敗時、ダイアログ表示
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            new AlertDialog.Builder(LoginTopActivity.this)
                                    .setTitle("Error!")
                                    .setMessage(task.getException().getMessage())
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    }
                });

    }

    //戻るボタンを無効化
    @Override
    public void onBackPressed() {

    }
}