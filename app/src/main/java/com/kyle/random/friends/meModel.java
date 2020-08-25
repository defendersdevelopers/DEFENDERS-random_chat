package com.kyle.random.friends;
import java.util.ArrayList;
public class meModel {
  public   String name;
    public  String age;
    public  String profileImage;
    public  String brief;
    public  String country;
    public  String interests;
    public String likes;
    public String love;
public  ArrayList<String >images;
    public meModel(String name, String age, String profileImage, String brief, String country, String interests, String likes, String love,ArrayList<String> images) {
        this.name = name;
        this.age = age;
        this.profileImage = profileImage;
        this.brief = brief;
        this.country = country;
        this.interests = interests;
        this.likes = likes;
        this.love = love;
        this.images=images;
    }
}
