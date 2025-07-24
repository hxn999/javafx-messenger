package com.db;

public class PublicUser {
    private  String name;
    private  String url;
    private  String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public PublicUser(String name, String url, String phone) {
        this.name = name;
        this.url = url;
        this.phone = phone;
    }


    public PublicUser(String dataString) {
        String[] data = dataString.split(":");
        this.name = data[0];
        this.url = data[2];
        this.phone = data[1];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(name).append(":")
                .append(phone).append(":")
                .append(url);

        return sb.toString();
    }
}