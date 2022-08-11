package com.project.news_wiki;

public class News {

    //    resource URL for image of the news
    private final String mImageResourceUrl;
    //    variable for news headline
    private final String mHeadline;
    //    variable for news content
    private final String mNews;
    //    resource URL for the news article
    private final String mUrl;


    public News(String imageUrl, String headline, String news, String url) {
        this.mImageResourceUrl = imageUrl;
        this.mHeadline = headline;
        this.mNews = news;
        this.mUrl = url;
    }

    //    getters for all the content
    public String getImageUrl() {
        return mImageResourceUrl;
    }

    public String getHeadline() {
        return mHeadline;
    }

    public String getNews() {
        return mNews;
    }

    public String getUrl() {
        return mUrl;
    }
}
