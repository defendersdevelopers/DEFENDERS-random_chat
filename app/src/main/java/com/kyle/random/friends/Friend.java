package com.kyle.random.friends;

public class Friend {
   public String Uid,name,age,brief,city,country,profileImage,addWay;
    public boolean blocked , heIsVIP,applied=false;

public String randomChatDate="";
public  String userCountryCode="";
    public Friend(String uid, String name, String age, String brief, String city, String country, String profileImage,boolean blocked,String addWay,boolean heIsVIP) {
        Uid = uid;
        this.name = name;
        this.age = age;
        this.brief = brief;
        this.city = city;
        this.country = country;
        this.profileImage = profileImage;
        this.blocked=blocked;
        this.addWay=addWay;
        this.heIsVIP=heIsVIP;
    }
}
