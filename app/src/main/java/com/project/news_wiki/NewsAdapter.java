package com.project.news_wiki;

import android.app.Activity;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsAdapter extends ArrayAdapter<News> {

    public NewsAdapter(Activity context, ArrayList<News> news) {
        super(context, 0, news);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.news_layout, parent, false);
        }

        News currentNews = getItem(position);

        ImageView newsImage = (ImageView) listItemView.findViewById(R.id.news_image);
        Picasso.with(getContext()).load(Uri.parse(currentNews.getImageUrl())).into(newsImage);

        TextView heading = (TextView) listItemView.findViewById(R.id.news_headline);
        heading.setText(currentNews.getHeadline());

        TextView news = (TextView) listItemView.findViewById(R.id.news_content);
        news.setText(currentNews.getNews());

        return listItemView;
    }
}
