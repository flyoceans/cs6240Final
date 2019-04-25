package com.example.demo.entity;

public class Tweet {
    UserProfile user;
    String Tweet;
    String[] hashtags;
    long time;

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public String getTweet() {
        return Tweet;
    }

    public void setTweet(String tweet) {
        Tweet = tweet;
    }

    public String[] getHashtags() {
        return hashtags;
    }

    public void setHashtags(String[] hashtags) {
        this.hashtags = hashtags;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
