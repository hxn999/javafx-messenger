//package com.client.util;
//
//
//import com.db.SignedUser;
//import com.db.User;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class MessageOfChat implements Serializable {
//
//    private String name;
//    private MessageType type;
//    private String msg;
//    private int count;
//    private ArrayList<User> list;
//    private ArrayList<User> users;
//    private String phone;
//
//    public String getPicture() {
//        return picture;
//    }
//
//    private String picture;
//
////    public Message() {
////    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public String setPhone(String phone) {
//        this.phone = phone;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getMsg() {
//
//        return msg;
//    }
//
//    public void setMsg(String msg) {
//        this.msg = msg;
//    }
//
//    public MessageType getType() {
//        return type;
//    }
//
//    public void setType(MessageType type) {
//        this.type = type;
//    }
//
//    public ArrayList<User> getUserlist() {
//        return list;
//    }
//
//    public void setUserlist(HashMap<String, User> userList) {
//        this.list = new ArrayList<>(userList.values());
//    }
//
//    public void setOnlineCount(int count){
//        this.count = count;
//    }
//
//    public int getOnlineCount(){
//        return this.count;
//    }
//
//    public void setPicture(String picture) {
//        this.picture = picture;
//    }
//
//
//    public ArrayList<User> getUsers() {
//        return users;
//    }
//
//    public void setUsers(ArrayList<User> users) {
//        this.users = users;
//    }
//}