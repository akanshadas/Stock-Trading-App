package com.example.firstappad;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//Volley
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;

public class CompanyDetailsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferencesPortfolio;
    public static final String watchlistTickers = "watchlistTickers";
    public static final String watchlistTickersToBeSent = "watchlistTickersToBeSent";
    public static final String portfolioTickersToBeSent = "portfolioTickersToBeSent";

    DecimalFormat df4 = new DecimalFormat("0.0000");

    private boolean isChecked = false;

    String TAG = "CompanyDetailsActivity";
    String ticker;
    TextView tickerID, compNameID, currPriceID, changePriceID, sharesOwnedID, marketValueID, currPrice2ID, lowID, bidPriceID, openPriceID, midPriceID, highPriceID, volumeID, compDescripID;
    TextView footerID;
    Button tradeBtnID;
    RequestQueue queue;
    String Deets1Str = "", Deets2Str = "", newsStr = "", chartStr = "";
    JSONObject Deets1Res, Deets2Res, newsRes, chartRes;
    //Webview
    public WebView webView;
    //WatchList
    String watchList;
    String portfolioList;
    Float liquidAmount;
    String currentStockPrice;
    //News
    RecyclerView newsList;
    private List<NewsStruc> allNewsList = new ArrayList<NewsStruc>();

    //Progress Spinner
    private ProgressBar spinner;
    RelativeLayout spinner_objectID;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_details);

        // FOR WATCHLIST + PORFOLIO
        sharedPreferences = getSharedPreferences("DataStore", Context.MODE_APPEND);
        sharedPreferencesPortfolio = getSharedPreferences("DataPortfolio", Context.MODE_APPEND);

        if(sharedPreferences.contains(watchlistTickersToBeSent))
        {
            watchList = sharedPreferences.getString(watchlistTickersToBeSent,null);
            Log.i("Watchlist Initial: ", watchList);

        }

        if(sharedPreferencesPortfolio.contains("liquidAmount"))
        {
            Log.i("current price:" , "true");

            liquidAmount = sharedPreferencesPortfolio.getFloat("liquidAmount", (float) 20000.0);

        }


        if(sharedPreferencesPortfolio.contains(portfolioTickersToBeSent))
        {
            portfolioList = sharedPreferencesPortfolio.getString(portfolioTickersToBeSent,null);
            Log.i("Portfolio Initial: ", portfolioList);
        }


        // *******************************************************************************  NECESSARY STUFF *******************************************************************************
        //ACTION BAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);

        //placing toolbar in place of actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\"><b>" + getString(R.string.app_name) + "</b></font>"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);      //Back button

        //PROGRESS SPINNER
        spinner = (ProgressBar)findViewById(R.id.progressBarID);
        spinner_objectID = (RelativeLayout) findViewById(R.id.spinner_objectID);
        spinner.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                spinner.setVisibility(View.GONE);
                spinner_objectID.setVisibility(View.GONE);
            }
        }, 3000 );

        // *******************************************************************************  API CALLING FUNCTIONS *******************************************************************************
        Intent intent = getIntent();
        String str = intent.getStringExtra("message_key");
        Log.d("message_key value:", str);
        String[] temp = str.split(" ");
        ticker = temp[0];

        final TextView tickerID, compNameID, currPriceID, changePriceID, sharesOwnedID, marketValueID, currPrice2ID, lowID, bidPriceID, openPriceID, midPriceID, highPriceID, volumeID, compDescripID;

        queue = Volley.newRequestQueue(this);
        CompanyDeets1GET();
        CompanyDeets2GET();
        NewsGET();
        ChartGET();

        //FOOTER
        footerID = (TextView) findViewById(R.id.footerID);
        footerID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiingo.com/"));
                startActivity(intent);
            }
        });

        //See more, see less
        compDescripID = (TextView) findViewById(R.id.compDescripID);
        final TextView plus = (TextView) findViewById(R.id.plus);
        final TextView minus = (TextView) findViewById(R.id.minus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plus.setVisibility(View.GONE);
                minus.setVisibility(View.VISIBLE);
                compDescripID.setMaxLines(Integer.MAX_VALUE);
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minus.setVisibility(View.GONE);
                plus.setVisibility(View.VISIBLE);
                compDescripID.setMaxLines(2);
            }
        });

    }

    // ******************************************************************************* API CALLS *******************************************************************************

    public void CompanyDeets1GET() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.0.4:3000/details1?term=" + ticker;

        // Request a string response from the provided URL.

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                        Deets1Res = response;
                        //STRING -> JSON
                        tickerID = (TextView) findViewById(R.id.tickerID);
                        compNameID = (TextView) findViewById(R.id.compNameID);
                        compDescripID = (TextView) findViewById(R.id.compDescripID);

                        tickerID.setText(Deets1Res.getString("ticker"));
                        compNameID.setText(Deets1Res.getString("name"));
                        compDescripID.setText(Deets1Res.getString("description"));
                } catch (JSONException e) { e.printStackTrace(); }


                    }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.i(TAG, "Error is:"+error);
            }
        });
        // Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));   //Local host timeout issue
        queue.add(jsonObjReq);

    }

    public void CompanyDeets2GET() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.0.4:3000/details2?term=" + ticker;

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Deets2Res = response;
                    //STRING -> JSON



                    currPriceID = (TextView) findViewById(R.id.currPriceID);
                    changePriceID = (TextView) findViewById(R.id.changePriceID);
                    sharesOwnedID = (TextView) findViewById(R.id.sharesOwnedID);
                    marketValueID = (TextView) findViewById(R.id.marketValueID);
                    currPrice2ID = (TextView) findViewById(R.id.currPrice2ID);
                    lowID = (TextView) findViewById(R.id.lowID);
                    bidPriceID = (TextView) findViewById(R.id.bidPriceID);
                    openPriceID = (TextView) findViewById(R.id.openPriceID);
                    midPriceID = (TextView) findViewById(R.id.midPriceID);
                    highPriceID = (TextView) findViewById(R.id.highPriceID);
                    volumeID = (TextView) findViewById(R.id.volumeID);

                    String last = Deets2Res.getString("last");              if (last == "null") {last = "0.0";}
                    String low = Deets2Res.getString("low");                if (low == "null") {low = "0.0";}
                    String bidPrice = Deets2Res.getString("bidPrice");      if (bidPrice == "null") {bidPrice = "0.0";}
                    String open = Deets2Res.getString("open");              if (open == "null") {open = "0.0";}
                    String mid = Deets2Res.getString("mid");                if (mid == "null") {mid = "0.0";}
                    String high = Deets2Res.getString("high");              if (high == "null") {high = "0.0";}
                    String volume = Deets2Res.getString("volume");          if (volume == "null") {volume = "0.0";}
                    //Change calculation
                    Double change = Double.parseDouble(Deets2Res.getString("last")) - Double.parseDouble(Deets2Res.getString("prevClose"));
                    change = (float)Math.round(change * 100.0)/100.0;
                    if (change > 0) { changePriceID.setText("$" + change.toString());       changePriceID.setTextColor(Color.parseColor("#3f925b"));}
                    else if (change < 0) {change = change * -1.0; changePriceID.setText("-$" + change.toString());    changePriceID.setTextColor(Color.parseColor("#9b4049"));}
                    else {changePriceID.setText("$" + change.toString()); changePriceID.setTextColor(Color.parseColor("#000000"));}

                    currPriceID.setText("$" + last);
                    currPrice2ID.setText("Current Price: " +last);
                    lowID.setText("Low: " + low);
                    bidPriceID.setText("Bid Price: " + bidPrice);
                    openPriceID.setText("OpenPrice: " + open);
                    midPriceID.setText("Mid: " + mid);
                    highPriceID.setText("High: " + high);
                    volumeID.setText("Volume: " + volume);
                    currentStockPrice = last;
                    if(sharedPreferencesPortfolio.contains(ticker))
                    {
                        int z = sharedPreferencesPortfolio.getInt(ticker,0);

                        sharesOwnedID.setText("You have " + String.valueOf(z) + " shares of " + ticker + ".");
                        float x = z * Float.valueOf(last).floatValue();
                        marketValueID.setText("Market Value: $"+String.valueOf(x));
                    }
                    else {
                        sharesOwnedID.setText("You have 0 shares of " + ticker + ".");
                        marketValueID.setText("Start trading!");
                    }

                } catch (JSONException e) { e.printStackTrace(); }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.i(TAG, "error is"+error);
            }
        });
        // Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));   //Local host timeout issue
        queue.add(jsonObjReq);

    }

    public void NewsGET() {
        Log.i(TAG, "CompanyDeets2GET-----------------------");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.0.4:3000/news?term=" + ticker;

        // Request a string response from the provided URL.



        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                newsRes = response;

                //STRING -> JSON
                try {

                    //RECYCLERVIEW
                    newsList = (RecyclerView) findViewById(R.id.newsList);
                    newsList.setNestedScrollingEnabled(false);
                    newsList.setLayoutManager(new LinearLayoutManager(CompanyDetailsActivity.this));
                    newsList.addItemDecoration(new DividerItemDecoration(newsList.getContext(),DividerItemDecoration.VERTICAL));
                    JSONArray array = (JSONArray) newsRes.get("articles");
                    Log.d("NEWZ:LENGTH", String.valueOf(array.length()));

                    //ERROR HANDLING
                    if (array.length() > 0 ){

                        //ITERATING
                        for (int i = 0; i < array.length(); i++) {

                            if (i == 0) {
                                JSONObject art = array.getJSONObject(i);
                                JSONObject art_child = art.getJSONObject("source");
                                final String title = art.getString("title"); final String source = art_child.getString("name"); final String image = art.getString("urlToImage");
                                final String newsUrl = art.getString("url"); final String time = calculateTime(art.getString("publishedAt"));

                                ImageView news_card_image1 = (ImageView) findViewById(R.id.news_card_image1);
                                TextView news_card_title1 = (TextView) findViewById(R.id.news_card_title1);
                                TextView news_card_time1 = (TextView) findViewById(R.id.news_card_time1);
                                TextView news_card_source1 = (TextView) findViewById(R.id.news_card_source1);

                                news_card_title1.setText(title);
                                news_card_source1.setText(source);
                                String urlVal = image;
                                if (urlVal.equals("") || urlVal.equals("null")) {
                                    Glide.with(news_card_image1.getContext()).load(R.drawable.no_image).into(news_card_image1);
                                }
                                else {
                                    Glide.with(news_card_image1.getContext()).load(urlVal).into(news_card_image1);
                                }
                                news_card_time1.setText(calculateTime(time));

                                final CardView news1Card = (CardView) findViewById(R.id.news1Card);
                                news1Card.setVisibility(View.VISIBLE);
                                //onCLick - open in browser
                                news1Card.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsUrl));
                                        startActivity(intent);
                                    }
                                });

                                //Long click, open modal
                                news1Card.setOnLongClickListener(new View.OnLongClickListener() {

                                    @Override
                                    public boolean onLongClick(View v) {
                                        Log.d("CLICK:", "onClick: " + title);
                                        Dialog dialog = new Dialog(news1Card.getContext());
                                        dialog.setContentView(R.layout.modal_news);

                                        ImageView dial_image, dial_twitter, dial_chrome;
                                        TextView dial_title;

                                        dial_title = dialog.findViewById(R.id.modal_news_title);
                                        dial_image = dialog.findViewById(R.id.modal_news_image);
                                        dial_twitter = dialog.findViewById(R.id.modal_news_twitter);
                                        dial_chrome = dialog.findViewById(R.id.modal_news_chrome);

                                        Glide.with(dial_image.getContext()).load(image).into(dial_image);
                                        dial_title.setText(title);
                                        dial_chrome.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsUrl));
                                                startActivity(intent);
                                            }
                                        });
                                        dial_twitter.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                String hashTag = "#CSCI571StockApp";
                                                String twitterShareLink = "https://twitter.com/intent/tweet?&url=Check out this Link: " + newsUrl +" "+ Uri.encode(hashTag);
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterShareLink));
                                                startActivity(intent);
                                            }
                                        });
                                        dialog.show();
                                        return true;
                                    }
                                });

                            }
                            else {
                                JSONObject art = array.getJSONObject(i);
                                JSONObject art_child = art.getJSONObject("source");
                                String title = art.getString("title"); String source = art_child.getString("name"); String image = art.getString("urlToImage"); String newsUrl = art.getString("url");
                                String time = calculateTime(art.getString("publishedAt"));

                                NewsStruc newz = new NewsStruc(title, source, image, newsUrl, time);
                                allNewsList.add(newz);
                            }
                        }

                        newsList.setAdapter(new NewsAdapter(CompanyDetailsActivity.this, allNewsList));
                    }


                } catch (JSONException e) { e.printStackTrace(); }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.i(TAG, "error is:"+error);
            }
        });
        // Add the request to the RequestQueue.
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));   //Local host timeout issue
        queue.add(jsonObjReq);

    }

    public void ChartGET() {
        Log.i(TAG, "Getting chart");
        //Webview
        webView =  (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new CustomWebViewClient());                //UNCOMMENT - KINI
        webView.getSettings().setJavaScriptEnabled(true);                   //UNCOMMENT - KINI
        webView.setVerticalScrollBarEnabled(false);
        webView.loadUrl("file:///android_asset/chart.html");              //UNCOMMENT - KINI
    }


    // ******************************************************************************* FUNCTIONS FOR FUNCTIONALITIES *******************************************************************************

    //CHARTS
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:(function setText() {myFunction('"+ticker+"');})()");
            //String newUrl = "javascript:JavaFunct('"+ticker+"')";
            //view.loadUrl(newUrl);
        }
    }


    //NEWS
    public String calculateTime(String date) {
        String periodDiffStr = "";

        try{
            LocalDateTime ldt = LocalDateTime.now();
            ZoneId zoneId = ZoneId.of("America/Los_Angeles");
            ZonedDateTime zonedCurrent = ldt.atZone(zoneId);

            Instant timestamp = Instant.parse(date);
            ZonedDateTime zonedArticle = timestamp.atZone(zoneId);

            Duration d = Duration.between(zonedArticle,zonedCurrent);
            Long diff = d.getSeconds();
            if(diff > 3600 * 24)
            {
                Long numdays = diff/(3600 * 24);
                periodDiffStr = numdays + "d ago";
            }
            else if(diff > 3600)
            {
                Long numHours = diff/3600;
                periodDiffStr = numHours + " h ago";

            }
            else if(diff > 60)
            {
                Long numMin = diff/60;
                periodDiffStr = numMin + " m ago";
            }
            else if(diff >= 0){
                periodDiffStr = diff + " s ago";
            }
            else{
                periodDiffStr = "1 h ago";
            }
        }
        catch (Exception e){
            //Do nothing
            //periodDiffStr = e.toString() + "\n " + date;
            periodDiffStr = "1 h ago";
        }
        return periodDiffStr;
    }

    // ******************************************************************************* FUNCTIONS FOR ACTION BAR *******************************************************************************
    //Menu Bar
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.action_favorite);
        if(sharedPreferences.contains(ticker))
        {
            Drawable myDrawable = getResources().getDrawable(R.drawable.ic_baseline_star_24); // The ID of your drawable.
            checkable.setIcon(myDrawable);
            checkable.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // ******************************************************************************* SHARED PREFERENCE *******************************************************************************

        int id = item.getItemId();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            isChecked = item.isChecked();
            if(!isChecked)
            {

                //item.setChecked(!isChecked);
                Drawable myDrawable = getResources().getDrawable(R.drawable.ic_baseline_star_24); // The ID of your drawable.
                item.setIcon(myDrawable);
                Toast.makeText(CompanyDetailsActivity.this, '"'+ticker+'"'+ " was added to favorites", Toast.LENGTH_SHORT).show();
                editor.putBoolean(ticker,true);
                watchList = watchList + "," + ticker;
            }
            else {

                //item.setChecked(isChecked);
                Drawable myDrawable = getResources().getDrawable(R.drawable.ic_baseline_star_border_24); // The ID of your drawable.
                item.setIcon(myDrawable);
                Toast.makeText(CompanyDetailsActivity.this, '"'+ticker+'"'+ " was removed from favorites", Toast.LENGTH_SHORT).show();
                editor.remove(ticker);
                ArrayList<String> ticks = new ArrayList<>(Arrays.asList(watchList.split(",")));
                ticks.remove(ticker);
                String result = "";
                int len = ticks.size();
                for (int i =0;i<len;i++)
                {
                    if((len-1) ==i)
                    {
                        result = result + ticks.get(i);
                    }
                    else {
                        result = result + ticks.get(i) + ",";
                    }

                }
                watchList = result;
            }
            isChecked = !item.isChecked();

            item.setChecked(isChecked);

           editor.putString(watchlistTickersToBeSent,watchList);

            editor.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

//    @Override
//    public void onBackPressed() {
//        startActivity(new Intent(CompanyDetailsActivity.this,HomeActivity.class));
//        super.onBackPressed();
//    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fav_icon_menu, menu);

        return true;
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }

    // ******************************************************************************* FUNCTIONS FOR PORTFOLIO *******************************************************************************
    public void tradingFunct(View view) {
        // ***************************************************************************** PORTFOLIO - start *****************************************************************************
        tradeBtnID = (Button) findViewById(R.id.tradeBtnID);
        final SharedPreferences.Editor editor = sharedPreferencesPortfolio.edit();


        final Dialog dialog = new Dialog(tradeBtnID.getContext());
        dialog.setContentView(R.layout.trade_modal);
        dialog.show();
        final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE );

        final TextView tradeCompID, calcID, liquidAmtID;
        final EditText noOfSharesID;
        Button buyBtnID, sellBtnID;


        tradeCompID = dialog.findViewById(R.id.tradeCompID);
        calcID = dialog.findViewById(R.id.calcID);
        noOfSharesID = dialog.findViewById(R.id.noOfSharesID);
        liquidAmtID = dialog.findViewById(R.id.liquidAmtID);
        buyBtnID = dialog.findViewById(R.id.buyBtnID);
        sellBtnID = dialog.findViewById(R.id.sellBtnID);

        //Information displayed inside the modal
        final Double currPrice =  Double.parseDouble( currentStockPrice);
        Log.i("current price:" , String.valueOf(currPrice));
        Log.i("liquid amount:" , String.valueOf(liquidAmount));

        tradeCompID.setText("Trade " + compNameID.getText() + " shares");
        calcID.setText("0 x $" + currPrice.toString() + "/share = $" + "0.0");
        liquidAmtID.setText("$" + liquidAmount  + " available to buy " + tickerID.getText());

        noOfSharesID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = noOfSharesID.getText().toString();

                //start
                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {    /* do nothing */

                    Pattern pattern1 = Pattern.compile("-*[0-9][0-9]*\\.");
                    Matcher matcher1 = pattern1.matcher(input);
                    //Condition: User tries to buy zero or negative shares
                    if ( input.isEmpty() || matcher1.matches() || input.equals("0") || input.startsWith("-")) {
                        Double no_shares = 0.0;

                        Double total = 0.0;
                        calcID.setText(no_shares +" x $" + currPrice.toString() + "/share = $" + df4.format(total).toString());
                    }
                }
                //BUYING
                else {
                    Double no_shares = Double.parseDouble(input);

                    Double total = 0.0;
                    total = no_shares * currPrice;
                    calcID.setText(no_shares.toString() +" x $" + currPrice.toString() + "/share = $" + df4.format(total).toString());
                }
                //end
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //BUYING A STOCK
        buyBtnID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = noOfSharesID.getText().toString();
                //Input conditions
                //Condition: User enters invalid input like text or punctuations
                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {

                    Pattern pattern1 = Pattern.compile("^-[0-9][0-9]*$");
                    Matcher matcher1 = pattern1.matcher(input);
                    //Condition: User tries to buy zero or negative shares
                    if ( input.isEmpty() || matcher1.matches() || input.equals("0") || input.startsWith("-")) {
                        Toast.makeText(CompanyDetailsActivity.this, "Cannot buy less than 0 shares", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(CompanyDetailsActivity.this, "‘Please enter valid amount", Toast.LENGTH_SHORT).show();
                    }
                }

                //BUYING
                else {
                    Double no_shares = Double.parseDouble(noOfSharesID.getText().toString());

                    Double total = 0.0;
                    total = no_shares * currPrice;
                    //Condition: User tries to buy more shares than money available
                    if (total > liquidAmount) {
                        Toast.makeText(CompanyDetailsActivity.this, "Not enough money to buy", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //BUY SHARES -- WRITE CODE      - UPDATE THIS
                        //shared preference
                        if(sharedPreferencesPortfolio.contains(ticker))
                        {
                            int a;
                            a = sharedPreferencesPortfolio.getInt(ticker,-1);
                            a = a + (int) Math.round(no_shares);
                            editor.putInt(ticker, a);
                        }
                        else
                        {
                            int b = (int) Math.round(no_shares);
                            editor.putInt(ticker,b);
                            portfolioList = portfolioList + "," + ticker;
                        }

                        liquidAmount = liquidAmount - total.floatValue();

                        editor.putString(portfolioTickersToBeSent,portfolioList);

                        editor.putFloat("liquidAmount",liquidAmount);


                        editor.commit();

                        if(sharedPreferencesPortfolio.contains(ticker))
                        {
                            int z = sharedPreferencesPortfolio.getInt(ticker,0);

                            sharesOwnedID.setText("You have " + String.valueOf(z) + " shares of " + ticker + ".");
                            float x = z * currPrice.floatValue();
                            marketValueID.setText("Market Value: $"+String.valueOf(x));
                        }
                        else {
                            sharesOwnedID.setText("You have 0 shares of " + ticker + ".");
                            marketValueID.setText("Start trading!");
                        }

                        //END: CONGRATS DIALOG
                        dialog.hide();
                        dialog.dismiss();
                        final Dialog dialog_congrats = new Dialog(tradeBtnID.getContext());
                        dialog_congrats.setContentView(R.layout.congrats_modal);
                        TextView congrats_msgID;
                        Button close_congratsID;
                        congrats_msgID = dialog_congrats.findViewById(R.id.congrats_msgID);
                        close_congratsID = dialog_congrats.findViewById(R.id.close_congratsID);
                        //Integer int_no_shares = Integer.parseInt(Double.parseDouble(no_shares));
                        int int_no_shares = (int) Math.round(no_shares);

                        congrats_msgID.setText("You have successfully bought " + int_no_shares + " shares of "+ tickerID.getText().toString());     //Setting text
                        close_congratsID.setOnClickListener(new View.OnClickListener() {                                                        //click DONE
                            @Override
                            public void onClick(View v) {
                                dialog_congrats.hide();
                                dialog_congrats.dismiss();
                            }
                        });
                        dialog_congrats.show();
                    }
                }
            }
        });

        //SELLING A STOCK
        sellBtnID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = noOfSharesID.getText().toString();
                //Input conditions
                //Condition: User enters invalid input like text or punctuations
                Pattern pattern = Pattern.compile("^[1-9][0-9]*$");
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {

                    Pattern pattern1 = Pattern.compile("^-[0-9][0-9]*$");
                    Matcher matcher1 = pattern1.matcher(input);
                    //Condition: User tries to buy zero or negative shares
                    if ( input.isEmpty() || matcher1.matches() || input.equals("0") || input.startsWith("-")) {
                        Toast.makeText(CompanyDetailsActivity.this, "Cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(CompanyDetailsActivity.this, "‘Please enter valid amount", Toast.LENGTH_SHORT).show();
                    }
                }
                //BUYING
                else {
                    Double no_shares = Double.parseDouble(noOfSharesID.getText().toString());

                    Double total = 0.0;
                    total = no_shares * currPrice;

                    int current_stocks;
                    if(sharedPreferencesPortfolio.contains(ticker))
                    {
                        current_stocks = sharedPreferencesPortfolio.getInt(ticker,0);
                    }
                    else {
                        current_stocks = 0;
                    }


                    //Condition: Users try to sell more shares than they own
                    if (no_shares.floatValue() > current_stocks) {
                        Toast.makeText(CompanyDetailsActivity.this, "Not enough shares to sell", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //SELL SHARES -- WRITE CODE - UPDATE THIS
                        //shared preference
                        int total_stocks;
                        int curStocks = (int)Math.round(no_shares);
                        total_stocks = current_stocks - curStocks;
                        if(total_stocks>0)
                        {
                            editor.putInt(ticker,total_stocks);
                        }
                        else {
                            editor.remove(ticker);

                            ArrayList<String> Pticks = new ArrayList<>(Arrays.asList(portfolioList.split(",")));
                            Pticks.remove(ticker);
                            String result = "";
                            int len = Pticks.size();
                            for (int i =0;i<len;i++)
                            {
                                if((len-1) ==i)
                                {
                                    result = result + Pticks.get(i);
                                }
                                else {
                                    result = result + Pticks.get(i) + ",";
                                }

                            }
                            portfolioList = result;
                        }


                        liquidAmount = liquidAmount + total.floatValue();
                        editor.putString(portfolioTickersToBeSent,portfolioList);

                        editor.putFloat("liquidAmount",liquidAmount);


                        editor.commit();
                       if(sharedPreferencesPortfolio.contains(ticker))
                        {
                            int z = sharedPreferencesPortfolio.getInt(ticker,0);

                            sharesOwnedID.setText("You have " + String.valueOf(z) + " shares of " + ticker + ".");
                            float x = z * currPrice.floatValue();
                            marketValueID.setText("Market Value: $"+String.valueOf(x));
                        }
                        else {
                            sharesOwnedID.setText("You have 0 shares of " + ticker + ".");
                            marketValueID.setText("Start trading!");
                        }

                        //END: CONGRATS DIALOG
                        dialog.hide();
                        dialog.dismiss();
                        final Dialog dialog_congrats = new Dialog(tradeBtnID.getContext());
                        dialog_congrats.setContentView(R.layout.congrats_modal);
                        TextView congrats_msgID;
                        Button close_congratsID;
                        congrats_msgID = dialog_congrats.findViewById(R.id.congrats_msgID);
                        close_congratsID = dialog_congrats.findViewById(R.id.close_congratsID);
                        int int_no_shares = (int) Math.round(no_shares);

                        congrats_msgID.setText("You have successfully sold " + int_no_shares + " shares of "+ tickerID.getText().toString());     //Setting text
                        close_congratsID.setOnClickListener(new View.OnClickListener() {                                                        //click DONE
                            @Override
                            public void onClick(View v) {
                                dialog_congrats.hide();
                                dialog_congrats.dismiss();
                            }
                        });
                        dialog_congrats.show();
                    }
                }
            }
        });


        // ***************************************************************************** PORTFOLIO - end *****************************************************************************


    }


}
