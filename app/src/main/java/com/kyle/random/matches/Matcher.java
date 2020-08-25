package com.kyle.random.matches;
import com.kyle.random.other.Country;

import java.util.ArrayList;
public class Matcher {
   public String userId,brief,profileImage,likes,love;
    public int matchRatio;
    public String age;
    boolean blocked,heIsUsingMatchPlus,heIsVIP;
    public  ArrayList<String> images;
    public String interests;


    private boolean hideAge,hideLocation,VIPFrame;
    private String fakeName ,  fakeCity,fakeCountry , fakeCountryCode , name, country , city ,   userCountryCode;
    public boolean isHideAge() {
        return hideAge && heIsVIP;
    }
    public boolean isHideLocation() {
        return hideLocation&& heIsVIP;
    }
    public boolean isVIPFrame() {
        return VIPFrame&& heIsVIP;
    }
    public String getName() {
        return  check(fakeName) ?   fakeName : name ;
    }
    public String getCity() {
        return check(fakeCity) ? fakeCity :city;
    }
    public String getCountry() {
        return check(fakeCountry) ? fakeCountry : country   ;
    }
    public String getCountryCode() {
        return   check(fakeCountryCode) ?   fakeCountryCode:userCountryCode;
    }

    private boolean check(String str){

     return  heIsVIP &&  str != null && !str.equals("null")  ;
    }


    ArrayList<Country> countriesList;
    public Matcher(String userId,ArrayList<Country> countriesList,String fakeCountry , String fakeCity, String brief, String country, String city, String name, String profileImage, String likes, String love, String userCountryCode, int matchRatio, String age, boolean blocked, boolean hideAge, boolean hideLocation, boolean heIsUsingMatchPlus, boolean heIsVIP, ArrayList<String> images, String interests, String fakeName,boolean VIPFrame , String fakeCountryCode) {
        this.userId = userId;
        this.brief = brief;
        this.country = country;
        this.fakeCountry=fakeCountry;
        this.fakeCity=fakeCity;
        this.city = city;
        this.name = name;
        this.profileImage = profileImage;
        this.likes = likes;
        this.love = love;
        this.userCountryCode = userCountryCode;
        this.matchRatio = matchRatio;
        this.age = age;
        this.countriesList=countriesList;
        this.blocked = blocked;
        this.hideAge = hideAge;
        this.hideLocation = hideLocation;
        this.heIsUsingMatchPlus = heIsUsingMatchPlus;
        this.heIsVIP = heIsVIP;
        this.images = images;
        this.interests = interests;
        this.fakeName = fakeName;
        this.VIPFrame=VIPFrame;
        this.fakeCountryCode=fakeCountryCode;
    }
}
