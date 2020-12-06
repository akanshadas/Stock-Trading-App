package com.example.firstappad;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;

import okio.Utf8;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    //Constructor
    private Context context;
    private List<NewsStruc> data;
    public  NewsAdapter(Context context, List<NewsStruc>  data){      //we have data in string array. Will have to send data to the adapter so that Adapter can adapt to the data and show in view.
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    //This creates ViewHolders for us and then stores views in them.
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //LayoutInflator returns an object for a layout.
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.news_card_view, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        final int pos = position;
        NewsStruc article = data.get(position);
        holder.news_card_title.setText(article.getTitle());
        holder.news_card_time.setText(article.getTime());
        holder.news_card_source.setText(article.getSource());
        String urlVal = article.getImageUrl();
        if (urlVal.equals("") || urlVal.equals("null")) {
            Glide.with(holder.news_card_image.getContext()).load(R.drawable.no_image).into(holder.news_card_image);
        }
        else {
            Glide.with(holder.news_card_image.getContext()).load(urlVal).into(holder.news_card_image);
        }

        Log.d("IMG URL:", article.getImageUrl());

        //On click on the article, article opens in chrome
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsStruc article = data.get(pos);
                String articleLink = article.getNewsUrl();
                Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(articleLink));
                context.startActivity(intent1);
            }
        });

        //Long click, open modal
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                NewsStruc article = data.get(pos);
                Log.d("CLICK:", "onClick: " + article.getTitle());
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.modal_news);

                ImageView dial_image, dial_twitter, dial_chrome;
                TextView dial_title;

                dial_title = dialog.findViewById(R.id.modal_news_title);
                dial_image = dialog.findViewById(R.id.modal_news_image);
                dial_twitter = dialog.findViewById(R.id.modal_news_twitter);
                dial_chrome = dialog.findViewById(R.id.modal_news_chrome);

                Glide.with(dial_image.getContext()).load(article.getImageUrl()).into(dial_image);
                dial_title.setText(article.getTitle());
                dial_chrome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NewsStruc article = data.get(pos);
                        String articleLink = article.getNewsUrl();
                        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(articleLink));
                        context.startActivity(intent1);
                    }
                });

                dial_twitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NewsStruc article = data.get(pos);
                        String hashTag = "#CSCI571StockApp";

                        String articleLink = article.getNewsUrl() ;

                        String twitterShareLink = "https://twitter.com/intent/tweet?&url=Check out this Link: " + articleLink +" "+ Uri.encode(hashTag);

                        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterShareLink));
                        context.startActivity(intent1);
                    }
                });
                //https://twitter.com/intent/tweet?&url=Check out this Link: https://hackaday.com/2020/11/12/bringing-an-ibm-model-f-into-2020/
                dialog.show();



                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    //2 components: 1) ViewHolder 2) Adapter
    public class NewsViewHolder extends RecyclerView.ViewHolder  {  //implements View.OnClickListener
        ImageView news_card_image;
        TextView news_card_title,  news_card_time,  news_card_source;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);

            //ONCLICK
            //itemView.setOnClickListener(this);
            news_card_image = itemView.findViewById(R.id.news_card_image);
            news_card_title = itemView.findViewById(R.id.news_card_title);
            news_card_time = itemView.findViewById(R.id.news_card_time);
            news_card_source = itemView.findViewById(R.id.news_card_source);
        }

//        @Override
//        public void onClick(View v) {
//            Log.d("CLICK:", "onClick: " + news_card_title.getText().toString());
//            //Toast.makeText(context, news_card_title.getText().toString() +" was clicked2", Toast.LENGTH_SHORT).show();
//            //Person person = people.get(getPosition());
//            Dialog dialog = new Dialog(context);
//            dialog.setContentView(R.layout.modal_news);
//
//            ImageView dial_image;
//            TextView dial_title, dial_url;
//
//            dial_title = dialog.findViewById(R.id.modal_news_title);
//            dial_url = dialog.findViewById(R.id.modal_news_chrome);
//
//            dial_title.setText(article);
//            dial_url.setText(news_card_url.getText().toString());
//
//            dialog.show();
//
//        }
    }

}
