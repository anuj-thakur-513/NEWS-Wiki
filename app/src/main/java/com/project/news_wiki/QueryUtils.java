package com.project.news_wiki;

import static com.project.news_wiki.NewsWikiMain.ACCEPT_PROPERTY;
import static com.project.news_wiki.NewsWikiMain.USER_AGENT_PROPERTY;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    //    Log Tag
    private static final String LOG_TAG = QueryUtils.class.getName();

    //    Create a private constructor so nobody can make an object of this class
    private QueryUtils() {
    }

    //    Create a URL using the given string object
    private static URL createUrl(String stringUrl) {
        if (stringUrl == null) {
            return null;
        }
        URL url = null;
//        Try creating the url
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creating the URL", e.getCause());
        }
        return url;
    }

    //    Make HTTP request using the above created URL
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestProperty("Accept", ACCEPT_PROPERTY);
            urlConnection.setRequestProperty("User-Agent", USER_AGENT_PROPERTY);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error in makeHttpRequest method", e.getCause());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = bufferedReader.readLine();

            while (line != null) {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    //    Parse the JSON response from the string
    private static List<News> extractDataFromJson(String jsonResponse) {
        List<News> news = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray articlesArray = baseJsonResponse.getJSONArray("articles");

            for (int i = 0; i < articlesArray.length(); i++) {
                JSONObject currentArticle = articlesArray.getJSONObject(i);

                String title = currentArticle.getString("title");
                String url = currentArticle.getString("url");

                if (url.contains("com")) {
                    String[] separated = url.split(".com");
                    url = separated[0] + ".com";
                } else {
                    String[] separated = url.split(".in/");
                    url = separated[0] + ".in";
                }

                String urlToImage = currentArticle.getString("urlToImage");
                String content = currentArticle.getString("content");
                if (content == "null") {
                    content = currentArticle.getString("description");
                }

                content = content.replaceAll("<.*>", "");

                News news1 = new News(urlToImage, title, content, url);
                news.add(news1);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing JSON", e.getCause());
        }
        return news;
    }

    public static List<News> fetchNews(String stringUrl) {
        URL url = createUrl(stringUrl);
        String jsonResponse = null;

        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request from fetchNews method", e.getCause());
        }

        List<News> news = extractDataFromJson(jsonResponse);
        return news;
    }

}
