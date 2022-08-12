package com.project.news_wiki;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewsWikiMain extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {
    //    LOG TAG
    public static final String LOG_TAG = NewsWikiMain.class.getName();

    /**
     * String Extra to send the keyword data to the {@link SearchActivity}
     */
    public static final String EXTRA_TEXT = "NewsWikiMain.EXTRA_TEXT";

    //    Getting the reference of ListView
    ListView newsListView;

    //    ID for the loader
    private static final int NEWS_LOADER_ID = 1;

    //    TextView that is displayed when the list is empty
    private TextView mEmptyStateTextView;

    //    Adding adView for adding ads
    private AdView adView;

    //    Search Box and related items
    private EditText searchBox;
    private static String searchContent;

    //    Progress bar
    private ProgressBar progressBar;

    //    URL for fetching latest news from the NEWS API
    private static final String NEWS_URL =
            "https://newsapi.org/v2/top-headlines?country=in&sortBy=publishedAt&language=en&apiKey=e5547a146257437d98894164ba1d1a77";

    /**
     * Adapter for the NEWS to be displayed
     */
    private NewsAdapter mAdapter;

    protected static final String ACCEPT_PROPERTY = "application/geo+json;version=1";
    protected static final String USER_AGENT_PROPERTY = "newsapi.org (anujthakur2003.anuj@gmail.com)"; //your email id for that site.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Test: SearchifyMain Activity onCreate() called...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_wiki_main);

//        Get the reference to the list view where the data is to be added
        newsListView = findViewById(R.id.list);

//        Call the function to initialize AdMob SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

//        get the reference to your FrameLayout
        //    Frame Layout for adContainer
        FrameLayout adContainerView = findViewById(R.id.adView_container);

//        Create an AdView and put it into your FrameLayout
        adView = new AdView(this);
        adContainerView.addView(adView);

        adView = new AdView(this);
        adContainerView.addView(adView);
        adView.setAdUnitId("ca-app-pub-9373632721279412/6769933651");

//        start requesting banner ads
        loadBanner();

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
            progressBar = findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);

//            Update the empty state
            mEmptyStateTextView = findViewById(R.id.empty_view);
            mEmptyStateTextView.setText(R.string.no_internet);
        }

        mEmptyStateTextView = findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        progressBar = findViewById(R.id.progress_bar);

//        Create a new adapter and set the adapter in the newsListView
        mAdapter = new NewsAdapter(this, new ArrayList<>());
        newsListView.setAdapter(mAdapter);

//        Finding a reference to the searchBox and setting up the Listener
        searchBox = findViewById(R.id.search_box);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    Store the search content in a string and send it to a new activity called
//                    SearchActivity and write all the remaining code there
                    searchContent = searchBox.getText().toString().trim();
                    Intent intent = new Intent(NewsWikiMain.this, SearchActivity.class);
                    intent.putExtra(EXTRA_TEXT, searchContent);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Find the current item that was clicked on
//                Find the current item that was clicked on
                News currentNews = mAdapter.getItem(position);
//                Custom Tabs intent
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(NewsWikiMain.this, Uri.parse(currentNews.getUrl()));
            }
        });
    }


    //    Clearing text in search box when we come back from another activity
    @Override
    protected void onResume() {
        super.onResume();
        searchBox.setText("");
    }

    /**
     * Implementing loader methods
     */

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
//        check if user has given any input
        Log.i(LOG_TAG, NEWS_URL);
        return new NewsLoader(this, NEWS_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> data) {
//        Clear the adapter
        mAdapter.clear();
//        vanishing the progress bar
        progressBar.setVisibility(View.GONE);

//        Check whether the device is connected to the internet or not
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            mEmptyStateTextView.setText(R.string.no_news);
        } else {
            mEmptyStateTextView.setText(R.string.no_internet);
        }

//        If there is a valid list of news articles available then add the data to the adapter
        if (data != null && !data.isEmpty()) {
            Log.i(LOG_TAG, "Test: Adding data to the adapter...");
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
//        Clear the adapter
        mAdapter.clear();
    }


//    Function to get the adaptive size for the ads
    private AdSize getAdSize() {
//    Determine the screen width to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

//    you can also pass your selected width here in dp
        int adWidth = (int) (widthPixels / density);

//    return the optimal size depends on your orientation (landscape or portrait)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

//    Function to request banner ads
    private void loadBanner() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        AdSize adSize = getAdSize();
//    Set the adaptive ad size to the ad view.
        adView.setAdSize(adSize);

//    Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

}