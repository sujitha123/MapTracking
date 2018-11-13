package com.sujitha.maptracking;


public class UserLocDataModel {

    private String id;
    private String longitude;
    private String latitude;
    private String unixTimeStamp;

    public UserLocDataModel(String longitude, String latitude, String unixTimeStamp) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.unixTimeStamp = unixTimeStamp;
    }

    public UserLocDataModel(String id, String longitude, String latitude, String unixTimeStamp) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.unixTimeStamp = unixTimeStamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getUnixTimeStamp() {
        return unixTimeStamp;
    }

    public void setUnixTimeStamp(String unixTimeStamp) {
        this.unixTimeStamp = unixTimeStamp;
    }
}
