package com.jojastream.shuffallery.object;

public class User {
    private String userName;
    private String coinNumber;
    private String emailAddress;
    private String likeStatus;
    private String wallpaper;

    public User(String emailAddress, String userName, String coinNumber, String likeStatus, String wallpaper) {
        this.coinNumber = coinNumber;
        this.userName = userName;
        this.emailAddress = emailAddress;
        this.likeStatus = likeStatus;
        this.wallpaper = wallpaper;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCoinNumber() {
        return coinNumber;
    }

    public void setCoinNumber(String coinNumber) {
        this.coinNumber = coinNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getLikeStatus() {
        return likeStatus;
    }

    public void setLikeStatus(String likeStatus) {
        this.likeStatus = likeStatus;
    }

    public String getWallpaper() {
        return wallpaper;
    }

    public void setWallpaper(String wallpaper) {
        this.wallpaper = wallpaper;
    }
}
