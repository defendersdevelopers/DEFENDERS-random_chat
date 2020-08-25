package com.kyle.random.chat;
public class FriendChatItem {
    public String Uid, name, addWay, profileImage, lastMessage, colorHex;
    boolean heIsVIP, lastMessageSeen, blocked;
    boolean isISentFriendRequest, isHeSentFriendRequest;
    Long lastMessageDate;
    String flagImage;
    boolean others;
    public FriendChatItem(String uid, String name, String addWay, String profileImage, String lastMessage, Long lastMessageDate, boolean lastMessageSeen, boolean heIsVIP, String flagImage, boolean blocked, String colorHex, boolean others, boolean isISentFriendRequest, boolean isHeSentFriendRequest) {
        Uid = uid;
        this.name = name;
        this.profileImage = profileImage;
        this.lastMessageDate = lastMessageDate;
        this.lastMessageSeen = lastMessageSeen;
        this.lastMessage = lastMessage;
        this.heIsVIP = heIsVIP;
        this.flagImage = flagImage;
        this.addWay = addWay;
        this.blocked = blocked;
        this.colorHex = colorHex;
        this.others = others;
        this.isISentFriendRequest = isISentFriendRequest;
        this.isHeSentFriendRequest = isHeSentFriendRequest;
    }
}
