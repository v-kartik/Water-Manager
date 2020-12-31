package com.example.watermanager.Data;

public class User {
    String uniqueId;
    String talkbackId;
    String apiKey;

    public User(){}

    public User(String uniqueId, String talkbackId , String apiKey){
        this.apiKey = apiKey;
        this.uniqueId = uniqueId;
        this.talkbackId = talkbackId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getTalkbackId() {
        return talkbackId;
    }

    public void setTalkbackId(String talkbackId) {
        this.talkbackId = talkbackId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}