package com.myapp.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.myapp.R;
import com.myapp.db.OperateDB;
import com.myapp.models.Record;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText et_app;
    private EditText et_explanation;
    private EditText et_tech;

    private FirebaseUser user;
    private String uid;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private String app;
    private String explanation;
    private String tech;

    public AddFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddFragment newInstance(String param1, String param2) {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        et_app = view.findViewById(R.id.et_app);
        et_explanation = view.findViewById(R.id.et_explanation);
        et_tech = view.findViewById(R.id.et_tech);

        //db関連
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("users").child(uid);

        //端末の戻るボタンを押した時
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {

                    //リスト画面に切り替え
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    ListFragment listFragment = new ListFragment();
                    transaction.replace(R.id.container,listFragment);
                    transaction.commit();

                    return true;
                }
                return false;
            }
        });

        //追加ボタン選択時
        Button bt_add = view.findViewById(R.id.bt_add);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //入力値があるかチェック
                //ない場合はスナックバーを表示し、return
                app = et_app.getText().toString();
                explanation = et_explanation.getText().toString();
                tech = et_tech.getText().toString();
                if (!checkEmpty(app,explanation,tech)) {

                    Snackbar.make(view,getResources().getString(R.string.sb_no_input),Snackbar.LENGTH_SHORT).show();
                    return;
                }

                //データ追加→表示
                OperateDB operateDB = new OperateDB(reference,getParentFragmentManager());
                operateDB.insert_data(app,explanation,tech);
            }
        });
    }

    //中身があるか判定
    public boolean checkEmpty(String app, String explanation,String tech) {

        if (TextUtils.isEmpty(app) || TextUtils.isEmpty(explanation) || TextUtils.isEmpty(tech)) {
            Log.d("DATA_ADD", "記入されていません");
            return false;
        }

        return true;
    }
}