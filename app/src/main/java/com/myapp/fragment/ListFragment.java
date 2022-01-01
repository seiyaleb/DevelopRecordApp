package com.myapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myapp.R;
import com.myapp.activity.LoginTopActivity;
import com.myapp.db.OperateDB;
import com.myapp.models.Record;
import com.myapp.viewmodel.RecordViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SharedPreferences dataStore;
    private SharedPreferences.Editor editor;
    private View _view;
    private ListView lv_record;
    private List<Map<String,String>> list_record;
    private RecordViewModel viewModel;

    private FirebaseUser user;
    private String uid;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    public ListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
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
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _view = view;
        lv_record = view.findViewById(R.id.lv_record);
        list_record = new ArrayList<>();

        //ViewModelインスタンス生成
        viewModel = new ViewModelProvider(requireActivity()).get(RecordViewModel.class);

        //db関連
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("users").child(uid);

        //データ読み取り→表示
        OperateDB operateDB = new OperateDB(reference,getParentFragmentManager());
        operateDB.read_data(list_record,getActivity(),lv_record);

        //リストビューの各アイテムを選択時
        lv_record.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //値を抽出し、ViewModelに格納
                Map<String,String> item_record = (Map<String,String>)parent.getItemAtPosition(position);
                viewModel.getKey().setValue(item_record.get("key"));
                viewModel.getApp_name().setValue(item_record.get("name"));
                viewModel.getExplanation().setValue(item_record.get("explanation"));
                viewModel.getTech().setValue(item_record.get("tech"));

                //追加画面に切り替え
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                DetailFragment detailFragment = new DetailFragment();
                transaction.replace(R.id.container,detailFragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        //端末の戻るボタンを無効化
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    return true;
                }
                return false;
            }
        });

        //フラグメントにおけるオプションメニューの表示を有効へ
        setHasOptionsMenu(true);

        //FABを選択時
        FloatingActionButton fab_add = view.findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //追加画面に切り替え
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                AddFragment addFragment = new AddFragment();
                transaction.replace(R.id.container,addFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //ログインのスナックバーを表示
        display_sb_login();

        //データ処理のスナックバー表示
        display_sb_data();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_option_main,menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //ログイン判定データをSharedPreferencesに保存
        editor.putBoolean("key", true);
        editor.apply();

        //ログアウト処理
        FirebaseAuth.getInstance().signOut();

        //ログイントップアクティビティに遷移
        Intent intent = new Intent(getActivity(), LoginTopActivity.class);
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    //ログインのスナックバー表示
    public void display_sb_login() {

        dataStore = getActivity().getSharedPreferences("DataStore", Context.MODE_PRIVATE);
        editor = dataStore.edit();
        if(dataStore.getBoolean("key",false)) {
            editor.clear();
            editor.apply();
            Snackbar.make(_view,getResources().getString(R.string.sb_login),Snackbar.LENGTH_SHORT).show();
        }
    }

    //データ処理のスナックバー表示
    public void display_sb_data() {

        Bundle bundle = getArguments();
        if(bundle != null) {

            String key = bundle.getString("key");
            if (key != null) {

                switch (key) {

                    case "add":
                        Snackbar.make(_view,getResources().getString(R.string.sb_add),Snackbar.LENGTH_SHORT).show();
                        break;

                    case "update":
                        Snackbar.make(_view,getResources().getString(R.string.sb_update),Snackbar.LENGTH_SHORT).show();
                        break;

                    case "delete":
                        Snackbar.make(_view,getResources().getString(R.string.sb_delete),Snackbar.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }
}