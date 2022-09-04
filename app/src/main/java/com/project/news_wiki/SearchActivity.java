package com.project.news_wiki;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    //    LOG TAG for debugging
    private static final String LOG_TAG = SearchActivity.class.getName();

    //    URL for fetching latest news from the NEWS API
    //    don't use the proper url in place of q use random text so we can replace it
    private static String NEWS_URL =
            "https://newsapi.org/v2/everything?query&sortBy=publishedAt&language=en&apiKey=e5547a146257437d98894164ba1d1a77";

    //    ID for the loader
    private static final int NEWS_LOADER_ID = 1;

    //    TextView that is displayed when the list is empty
    private TextView mEmptyStateTextView;

    //    Progress bar
    private ProgressBar progressBar;

    //    adapter for storing data
    private NewsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Test: SearchActivity onCreate method called...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

//        Receiving the keywords sent by the main activity
        Intent intent = getIntent();
        String searchContent = intent.getStringExtra(NewsWikiMain.EXTRA_TEXT);

        Log.i(LOG_TAG, searchContent);

        searchContent = searchContent.replace(" ", "+");
        searchContent = searchContent.toLowerCase();

        Log.i(LOG_TAG, searchContent);

        NEWS_URL = NEWS_URL.replace("tech", searchContent);

        Log.i(LOG_TAG, NEWS_URL);

//        Setting the action bar according to the query
        String searchQuery = intent.getStringExtra(NewsWikiMain.EXTRA_TEXT);
        searchQuery = searchQuery.toUpperCase();

        Objects.requireNonNull(getSupportActionBar()).setTitle(searchQuery);

//        Get the reference to the list view where the data is to be added
        ListView newsListView = (ListView) findViewById(R.id.list);

//        get the reference to your FrameLayout
        //    Frame Layout for adContainer
        FrameLayout adContainerView = findViewById(R.id.adView_container);

//        Get a reference to the ConnectivityManager to check state of network
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        Get details on the currently active default data network
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

//        If there is a network connection fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
//            Get a reference to the LoaderManager in order to interact with the loader
            LoaderManager loaderManager = getLoaderManager();
//            Initialize the loader and pass the ID of our loader along with arguments and callback
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);

//            Update the empty state
            mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
            mEmptyStateTextView.setText(R.string.no_internet);
        }

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

//        Create a new adapter and set the adapter in the newsListView
        mAdapter = new NewsAdapter(this, new ArrayList<>());
        newsListView.setAdapter(mAdapter);


        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Find the current item that was clicked on
                News currentNews = mAdapter.getItem(position);
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(SearchActivity.this, Uri.parse(currentNews.getUrl()));
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mAdapter.clear();
    }

    /**
     * Implementing loader methods
     */

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
//        parse the URL using Uri and the use Builder and append the query type
        Uri baseUri = Uri.parse(NEWS_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("q", getIntent().getStringExtra(NewsWikiMain.EXTRA_TEXT));
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {
//        Clear the adapter from previous data if any and hide the progress bar
        mAdapter.clear();
        progressBar.setVisibility(View.GONE);

//        Get the network connection of the device
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = (NetworkInfo) connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            mEmptyStateTextView.setText(R.string.no_news);
        } else {
            mEmptyStateTextView.setText(R.string.no_internet);
        }

//        If there is a valid list of news articles available then add the data to the adapter
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
//        clear the adapter
        mAdapter.clear();
    }
}