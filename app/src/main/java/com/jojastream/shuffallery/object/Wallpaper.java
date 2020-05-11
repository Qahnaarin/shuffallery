package com.jojastream.shuffallery.object;

public class Wallpaper {
    private String likeCount;
    private String baseURL;
    private String dislikeCount;
    private String usedCount;

    public Wallpaper( String likeCount, String dislikeCount, String usedCount, String baseURL) {
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.baseURL = baseURL;
        this.usedCount = usedCount;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(String dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public String getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(String usedCount) {
        this.usedCount = usedCount;
    }
}
