package com.kyle.random.other;
public class Hobby {
    public  int imageId;
    public String name;
    public boolean check;
    public String key;

    public Hobby(String key, String name, int imageId) {
        this.key = key;
        this.imageId = imageId;
        this.name = name;
        this.check = false;
    }
}