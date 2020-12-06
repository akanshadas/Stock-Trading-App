package com.example.firstappad;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.PrivateKey;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.time.Month;

//import android.widget.SearchView;

public class HomeActivity extends AppCompatActivity {
    ArrayAdapter arrayAdapter;

    private Handler handler;
    List<String> stringList;
    String results;
    TextView netWorthID;
    float networth = 0.0f;
    DecimalFormat df = new DecimalFormat("0.00");

    //Progress Spinner
    RelativeLayout spinner_objectID;
    private ProgressBar spinner;

    // **************** PORTFOLIO ***************
    RecyclerView RV_portfolio;
    PortfolioAdapter portfAdapter;
    ArrayList<String> stringArrayList2 = new ArrayList<>();
    SharedPreferences sharedPreferencesPorfolio;
    SharedPreferences.Editor editor;
    String portfolioList;
    float liquidAmount;
    float stockAmount;

    // **************** WATCHLIST ***************
    RecyclerView RV_watchlist;
    WatchlistAdapter watchlistAdapter;
    SharedPreferences sharedPreferences;
    public static final String watchlistTickersToBeSent = "watchlistTickersToBeSent";
    SharedPreferences.Editor editorW;
    String watchList;

    TextView todayID;
    TextView footerID;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Instant nowUtc = Instant.now();
        ZoneId currDate = ZoneId.of("PST");
        ZonedDateTime nowAsiaSingapore = ZonedDateTime.ofInstant(nowUtc, currDate);

        //Spinner
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

        //Today's date
        int day = nowAsiaSingapore.getDayOfMonth();
        int year = nowAsiaSingapore.getYear();
        String month = String.valueOf(nowAsiaSingapore.getMonth());
        String m = month.substring(0, 1).toUpperCase() + month.substring(1).toLowerCase();
        todayID = (TextView) findViewById(R.id.todayID);
        todayID.setText(m+" "+String.valueOf(day)+", "+String.valueOf(year));

        //FOOTER
        footerID = (TextView) findViewById(R.id.footerID);
        footerID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiingo.com/"));
                startActivity(intent);
            }
        });


        sharedPreferencesPorfolio = getSharedPreferences("DataPortfolio",Context.MODE_APPEND);
        editor = sharedPreferencesPorfolio.edit();
        if(sharedPreferencesPorfolio.contains("liquidAmount"))
        {
            liquidAmount = sharedPreferencesPorfolio.getFloat("liquidAmount", liquidAmount);
            }
        else {
            liquidAmount = 20000;
            editor.putFloat("liquidAmount",liquidAmount);
            editor.commit();

        }

        sharedPreferences = getSharedPreferences("DataStore", Context.MODE_APPEND);
        editorW = sharedPreferences.edit();



        //  ACTION BAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //placing toolbar in place of actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"black\"><b>" + getString(R.string.app_name) + "</b></font>"));


        sharedPreferences = getSharedPreferences("DataStore", Context.MODE_APPEND);
        if(sharedPreferences.contains(watchlistTickersToBeSent))
        {
            watchList = sharedPreferences.getString(watchlistTickersToBeSent,null);

        }

        //WATCHLIST
        RV_watchlist = findViewById(R.id.RV_watchlist);
        recyclerViewWatchlist();

        //PORTFOLIO
        RV_portfolio = findViewById(R.id.RV_portfolio);
        recyclerViewPortfolio();

        handler = new Handler();                      //15 SECONDS //DIDI
        handler.postDelayed(runnable, 15000);


        stockAmount = sharedPreferencesPorfolio.getFloat("StockAmount", 0);
        netWorthID = (TextView) findViewById(R.id.netWorthID);
        networth = stockAmount + liquidAmount;
        netWorthID.setText(String.valueOf(df.format(stockAmount + liquidAmount)));




    }


 //15 SECONDS
    private Runnable runnable = new Runnable() {  //15 SECONDS //DIDI
        @Override
        public void run() {
            handler.postDelayed(this, 15000);
            RV_watchlist = findViewById(R.id.RV_watchlist);
            recyclerViewWatchlist();

            //PORTFOLIO
            RV_portfolio = findViewById(R.id.RV_portfolio);
            recyclerViewPortfolio();
            Log.i("Refetching data at 15 seconds"," here");
            //This basically reruns this runnable in 5 seconds

            stockAmount = sharedPreferencesPorfolio.getFloat("StockAmount", 0);
            liquidAmount = sharedPreferencesPorfolio.getFloat("liquidAmount", 0);
            netWorthID = (TextView) findViewById(R.id.netWorthID);
            netWorthID.setText(String.valueOf(df.format(stockAmount + liquidAmount)));
        }
    };

    @Override
    public void onRestart() {  // After a pause OR at startup
        super.onRestart();

        //Refresh your stuff here
        Log.d("REFRESH", "onResume: ");
        //WATCHLIST
        RV_watchlist = findViewById(R.id.RV_watchlist);
        recyclerViewWatchlist();

        //PORTFOLIO
        RV_portfolio = findViewById(R.id.RV_portfolio);
        recyclerViewPortfolio();

        //NETWORTH
        netWorthID = (TextView) findViewById(R.id.netWorthID);
        netWorthID.setText(String.valueOf(df.format(liquidAmount+stockAmount)));

    }

    // *********************************************************************************** ACTION BAR ******************************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.top_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_icon);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_icon).getActionView();

        //final AppCompatAutoCompleteTextView autoCompleteTextView = findViewById(R.id.auto_complete_edit_text);
        final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        //final TextView selectedText = findViewById(R.id.selected_item);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(getApplicationContext(), CompanyDetailsActivity.class);
                intent.putExtra("message_key", results);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()>=3)
                {
                    if (!TextUtils.isEmpty(searchAutoComplete.getText())) {
                        stringList = new ArrayList<>();


                        //makeApiCall(searchAutoComplete.getText().toString());

                        ApiCall.make(HomeActivity.this, newText, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {

                                    JSONObject responseObject = new JSONObject(response);
                                    JSONArray array = responseObject.getJSONArray("term");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject row = array.getJSONObject(i);
                                        stringList.add(row.getString("ticker") + " - " + row.getString("name"));

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //IMPORTANT: set data here and notify
                                //autoSuggestAdapter.setData(stringList);
                                //autoSuggestAdapter.notifyDataSetChanged();
                                arrayAdapter = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_dropdown_item_1line, stringList);

                                searchAutoComplete.setAdapter(arrayAdapter);

                                arrayAdapter.notifyDataSetChanged();

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                    }
                }
                else
                {
                    return false;
                }
                return true; //call API
            }
        });

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String myItem=(String)adapterView.getItemAtPosition(i);
                searchAutoComplete.setText("" + myItem);
                results = "" + myItem;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    // *********************************************************************************** PORTFOLIO ******************************************************************************

    private void recyclerViewPortfolio()  {

        portfolioList = sharedPreferencesPorfolio.getString("portfolioTickersToBeSent", null);
        //Log.d("portfolioList:", portfolioList);
        if (portfolioList != null) {
            ArrayList<String> ticks = new ArrayList<>(Arrays.asList(portfolioList.split(",")));
            ticks.remove("null");

            portfAdapter = new PortfolioAdapter(HomeActivity.this, ticks);
            RV_portfolio.setAdapter(portfAdapter);

            ItemTouchHelper.Callback callback2 = new PortfolioItemMoveCallback((PortfolioItemMoveCallback.ItemTouchHelperContract) portfAdapter, HomeActivity.this) {
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    portfAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                    //UPDATING SHARED PREFERENCE
                    ArrayList<String> ticks = portfAdapter.getData();
                    String tempPortfolio = "null";
                    for (int i = 0; i < ticks.size(); i++) {
                        tempPortfolio += "," + ticks.get(i);
                    }
                    Log.d("tracking SWAP: ", ticks.toString() + tempPortfolio);
                    editor.putString("portfolioTickersToBeSent", tempPortfolio);
                    editor.commit();
                    Log.d("tracking SWAP NEW: ", sharedPreferencesPorfolio.getString("portfolioTickersToBeSent", null));

                    return true;
                }

                @Override
                public boolean isItemViewSwipeEnabled() {
                    return false;
                }         //PORTFOLIO: return false

                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlag = 0;               //PORTFOLIO: = 0
                    return makeMovementFlags(dragFlags, swipeFlag);
                }
            };
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback2);
            touchHelper.attachToRecyclerView(RV_portfolio);


        }
        stockAmount = sharedPreferencesPorfolio.getFloat("StockAmount", 0);
       }
    // *********************************************************************************** WATCHLIST ******************************************************************************

    private void recyclerViewWatchlist() {

        //String watchList = "null,AAPL,IBM,MSFT,AAP";
        watchList = sharedPreferences.getString(watchlistTickersToBeSent, null);
        if (watchList != null) {

            ArrayList<String> ticks = new ArrayList<>(Arrays.asList(watchList.split(",")));
            ticks.remove("null");

            watchlistAdapter = new WatchlistAdapter(HomeActivity.this, ticks);
            RV_watchlist.setAdapter(watchlistAdapter);

            ItemTouchHelper.Callback callback = new WatchlistItemMoveCallback((WatchlistItemMoveCallback.ItemTouchHelperContract) watchlistAdapter, HomeActivity.this) {
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    final int position = viewHolder.getAdapterPosition();
                    final String tk = watchlistAdapter.getData().get(position);
                    String tempWatchlist = watchlistAdapter.removeItem(position);

                    editorW.putString(watchlistTickersToBeSent, tempWatchlist);
                    editorW.remove(tk);
                    editorW.commit();
                }

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,RecyclerView.ViewHolder target) {
                    watchlistAdapter.onRowMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                    //UPDATING SHARED PREFERENCE
                    ArrayList<String> ticks = watchlistAdapter.getData();
                    String tempWatchlist = "null";
                    for (int i = 0; i < ticks.size(); i++) {
                        tempWatchlist += "," + ticks.get(i);
                    }
                    //Log.d("tracking SWAP: ", ticks.toString() + tempWatchlist);
                    editorW.putString(watchlistTickersToBeSent, tempWatchlist);
                    editorW.commit();
                    Log.d("tracking SWAP NEW: ", sharedPreferences.getString(watchlistTickersToBeSent, null));

                    return true;
                }
                @Override
                public boolean isItemViewSwipeEnabled() {
                    return true;
                }         //PORTFOLIO: return false

                @Override
                public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlag = ItemTouchHelper.LEFT;               //PORTFOLIO: = 0
                    return makeMovementFlags(dragFlags, swipeFlag);
                }
            };
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(RV_watchlist);
        }
    }

}