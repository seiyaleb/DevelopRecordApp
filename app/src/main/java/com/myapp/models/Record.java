package com.myapp.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Record {

    public String firebaseKey;
    public String app_name;
    public String explanation;
    public String tech;

    public Record() {
    }

    public Record(String key, String title, String comment, String skill) {

        this.firebaseKey = key;
        this.app_name = title;
        this.explanation = comment;
        this.tech = skill;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public String getApp_name() {
        return app_name;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getTech() {
        return tech;
    }
}
