package com.myapp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.myapp.R;
import com.myapp.db.OperateDB;
import com.myapp.models.Record;
import com.myapp.viewmodel.RecordViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecordViewModel viewModel;

    private EditText et_app;
    private EditText et_explanation;
    private EditText et_tech;

    private FirebaseUser user;
    private String uid;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private String key;
    private String app;
    private String explanation;
    private String tech;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String param1, String param2) {
        DetailFragment fragment = new DetailFragment();
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
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        et_app = view.findViewById(R.id.et_app);
        et_explanation = view.findViewById(R.id.et_explanation);
        et_tech = view.findViewById(R.id.et_tech);

        //ViewModelインスタンス生成
        viewModel = new ViewModelProvider(requireActivity()).get(RecordViewModel.class);

        //ViewModelから値を抽出
        key = viewModel.getKey().getValue();
        app = viewModel.getApp_name().getValue();
        explanation = viewModel.getExplanation().getValue();
        tech = viewModel.getTech().getValue();

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

        //フラグメントにおけるオプションメニューの表示を有効へ
        setHasOptionsMenu(true);

        //更新ボタン選択時
        Button bt_update = view.findViewById(R.id.bt_update);
        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //データ更新→表示
                OperateDB operateDB = new OperateDB(reference,getParentFragmentManager());
                operateDB.update_data(et_app,et_explanation,et_tech,key);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //ViewModelからデータを抽出し、詳細表示
        et_app.setText(app);
        et_explanation.setText(explanation);
        et_tech.setText(tech);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_option_delete,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //削除アイコン選択時
        //削除確認のダイアログ表示
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_explanation)
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //データ削除→表示
                        OperateDB operateDB = new OperateDB(reference,getParentFragmentManager());
                        operateDB.delete_data(key);
                    }
                })
                .setNegativeButton(R.string.dialog_no,null)
                .show();

        return super.onOptionsItemSelected(item);
    }
}