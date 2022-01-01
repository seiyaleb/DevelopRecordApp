package com.myapp.db;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.myapp.R;
import com.myapp.fragment.ListFragment;
import com.myapp.models.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//DB操作クラス
public class OperateDB {

    private DatabaseReference reference;
    private FragmentManager fragmentManager;

    public OperateDB(DatabaseReference _reference,FragmentManager _fragmentManager) {

        this.reference = _reference;
        this.fragmentManager = _fragmentManager;
    }

    //データ読み取り→表示
    public void read_data(List<Map<String,String>> list_record,Activity activity,ListView lv_record) {

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Record record = snapshot.getValue(Record.class);
                Map<String,String> item_record = new HashMap<>();
                item_record.put("key",record.getFirebaseKey());
                item_record.put("name",record.getApp_name());
                item_record.put("explanation",record.getExplanation());
                item_record.put("tech",record.getTech());
                list_record.add(item_record);

                //リストビューにデータ表示
                String[] from ={"name"};
                int [] to = {android.R.id.text1};
                SimpleAdapter adapter = new SimpleAdapter(activity, list_record,android.R.layout.simple_list_item_1,from,to);
                lv_record.setAdapter(adapter);
        }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.i("Data_Read",error.getMessage());
            }
        });
    }

    //データ追加→表示
    public void insert_data(String app, String explanation,String tech) {

        String key = reference.push().getKey();
        Record record = new Record(key,app,explanation,tech);
        reference.child(key).setValue(record).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {



                //リスト画面に切り替え
                change_to_list("add");
            }
        });
    }

    //データ更新→表示
    public void update_data(EditText et_app,EditText et_explanation,EditText et_tech,String key) {

        String app = et_app.getText().toString();
        String explanation = et_explanation.getText().toString();
        String tech = et_tech.getText().toString();
        Record record = new Record(key,app,explanation,tech);
        reference.child(key).setValue(record).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //リスト画面に切り替え
                change_to_list("update");
            }
        });
    }

    //データ削除→表示
    public void delete_data(String key) {

        reference.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //リスト画面に切り替え
                change_to_list("delete");
            }
        });
    }

    //リスト画面に切り替えメソッド
    public void change_to_list(String code) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        ListFragment listFragment = new ListFragment();
        Bundle bundle = new Bundle();

        switch (code) {

            case "add":
                bundle.putString("key","add");
                break;

            case "update":
                bundle.putString("key","update");
                break;

            case "delete":
                bundle.putString("key","delete");
                break;
        }

        listFragment.setArguments(bundle);
        transaction.replace(R.id.container,listFragment);
        transaction.commit();
    }
}
