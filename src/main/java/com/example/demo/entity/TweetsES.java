package com.example.demo.entity;

import io.searchbox.annotations.JestId;

public class TweetsES {
    private String user;
    private String text;
    @JestId
    private String id;
    private long time;
    private String[] hashtags;

    public TweetsES(String user, String text, String id, long time, String[] hashtags) {
        this.user = user;
        this.text = text;
        this.id = id;
        this.time = time;
        this.hashtags = hashtags;
        for (int i = 0; i < hashtags.length; i++) {
            hashtags[i] = hashtags[i].toLowerCase();
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String[] getHashtags() {
        return hashtags;
    }

    public void setHashtags(String[] hashtags) {
        this.hashtags = hashtags;
    }
}
