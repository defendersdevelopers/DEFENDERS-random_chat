package com.kyle.random.friends;

import java.io.Serializable;

public class Message implements Serializable {
    public String key;
    public String kind ;
    public String content ;
    public String who ;
    public long date ;
    public boolean seen;
public Message(){

}
    public Message(String key, String kind , String content, String who, long date, boolean seen) {
        this.key = key;
        this.kind = kind;
        this.content=content;
        this.who = who;
        this.date = date;
        this.seen = seen;
    }
}
