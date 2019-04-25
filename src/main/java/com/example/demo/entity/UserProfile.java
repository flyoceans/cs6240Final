package com.example.demo.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_profile")
public class UserProfile implements Comparable<UserProfile>{

    @Id
    Long id;
    String name;
    String location;
    Float pr;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Float getPr() {
        return pr;
    }

    public void setPr(Float pr) {
        this.pr = pr;
    }

    @Override
    public int compareTo(UserProfile o) {
        return this.pr > o.pr ? 1 : -1;
    }
}
