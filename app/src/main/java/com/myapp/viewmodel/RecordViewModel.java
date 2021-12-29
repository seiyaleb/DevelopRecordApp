package com.myapp.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RecordViewModel extends ViewModel {

    private MutableLiveData<String> key;
    private MutableLiveData<String> app_name;
    private MutableLiveData<String> explanation;
    private MutableLiveData<String> tech;

    public MutableLiveData<String> getKey() {

        if(key == null) {

            key = new MutableLiveData<String>();
        }

        return key;
    }

    public MutableLiveData<String> getApp_name() {

        if(app_name == null) {

            app_name = new MutableLiveData<String>();
        }

        return app_name;
    }

    public MutableLiveData<String> getExplanation() {

        if(explanation == null) {

            explanation = new MutableLiveData<String>();
        }

        return explanation;
    }

    public MutableLiveData<String> getTech() {

        if(tech == null) {

            tech = new MutableLiveData<String>();
        }

        return tech;
    }



}
