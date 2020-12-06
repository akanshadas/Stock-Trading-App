package com.example.firstappad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.MyViewHolder> implements PortfolioItemMoveCallback.ItemTouchHelperContract {

    private ArrayList<String> watchList;
    private Context context;
    //CARD DETAILS
    JSONObject Deets1Res, Deets2Res;
    TextView tickerID, compNameID, currPriceID, changePriceID;
    RequestQueue queue;
    float netWorth = 0.0f;
    DecimalFormat df = new DecimalFormat("0.00");

    //Shared Preference
    SharedPreferences sharedPreferencesPorfolio;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView ticker_cardID, currPrice_cardID, shares_cardID, changePrice_cardID;
        ImageView trendImgID;
        CardView portfolio_cardID;
        View rowView;
        public MyViewHolder(View itemView) {
            super(itemView);
            rowView = itemView;
            ticker_cardID = itemView.findViewById(R.id.ticker_cardID);
            currPrice_cardID = itemView.findViewById(R.id.currPrice_cardID);
            shares_cardID = itemView.findViewById(R.id.shares_cardID);
            changePrice_cardID = itemView.findViewById(R.id.changePrice_cardID);
            trendImgID = itemView.findViewById(R.id.trendImgID);
            portfolio_cardID = itemView.findViewById(R.id.portfolio_cardID);

            //ON CLICK LISTENER OPTION 2
        }
    }

    public PortfolioAdapter(Context context, ArrayList<String> watchList) {
        this.context = context;
        this.watchList = watchList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.portfolio_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final String ticker = watchList.get(position);
        sharedPreferencesPorfolio = context.getSharedPreferences("DataPortfolio",Context.MODE_APPEND);
        final SharedPreferences.Editor editor = sharedPreferencesPorfolio.edit();


        //GETTING VAUES FROM API CALLS
        RequestQueue queue = Volley.newRequestQueue(context);

        //2. GETTING ticker current price and change
        // IF NUMBER OF SHARES == 0, then
        if(sharedPreferencesPorfolio.contains(ticker))
        {
            float no_shares = (float) sharedPreferencesPorfolio.getInt(ticker,0);
            holder.shares_cardID.setText(String.valueOf(no_shares)+" shares");
        }
        String url2 ="https://akansha-hw9-backend.wm.r.appspot.com/details2?term=" + ticker;
        JsonObjectRequest jsonObjReq2 = new JsonObjectRequest(Request.Method.GET, url2, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Deets2Res = response;
                    String last = String.valueOf(df.format( Float.parseFloat(Deets2Res.getString("last"))));
                    holder.currPrice_cardID.setText(last);

                    float no_shares = (float) sharedPreferencesPorfolio.getInt(ticker,0);
                    netWorth = netWorth + (no_shares * Float.parseFloat(Deets2Res.getString("last")));
                    editor.putFloat("StockAmount", netWorth);
                    editor.commit();

                    float stockAmount = sharedPreferencesPorfolio.getFloat("StockAmount", 0);

                    Double change = Double.parseDouble(Deets2Res.getString("last")) - Double.parseDouble(Deets2Res.getString("prevClose"));
                    change = (float)Math.round(change * 100.0)/100.0;
                    if (change > 0) {
                        holder.changePrice_cardID.setText(String.valueOf(df.format( Float.parseFloat(change.toString()))));
                        holder.changePrice_cardID.setTextColor(Color.parseColor("#3f925b"));
                        holder.trendImgID.setImageResource(R.drawable.ic_twotone_trending_up_24);
                    }
                    else if (change < 0) {
                        change = change * -1.0;
                        holder.changePrice_cardID.setText(String.valueOf(df.format( Float.parseFloat(change.toString()))));
                        holder.changePrice_cardID.setTextColor(Color.parseColor("#9b4049"));
                        holder.trendImgID.setImageResource(R.drawable.ic_baseline_trending_down_24);
                    }
                    else {
                        holder.changePrice_cardID.setText(String.valueOf(df.format( Float.parseFloat(change.toString()))));
                        holder.changePrice_cardID.setTextColor(Color.parseColor("#cfcfcf"));}
                } catch (JSONException e) { e.printStackTrace(); }

            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.i("Watchlist:", "Didnt't get Deets1"); }
        });
        jsonObjReq2.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));   //Local host timeout issue
        queue.add(jsonObjReq2);


        holder.ticker_cardID.setText(watchList.get(position));

        //ON CLICK - OPEN STOCK DETAIL
        holder.portfolio_cardID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CompanyDetailsActivity.class);
                intent.putExtra("message_key", ticker + " - " + "TEMP");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return watchList.size();
    }

    public void removeItem(int position) { }


    public ArrayList<String> getData() {
        return watchList;
    }

    // DRAG
    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(watchList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(watchList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        Log.d("tracking SWAPPED [portfolio]:", watchList.toString());
    }

    @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
        //myViewHolder.rowView.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {
        //myViewHolder.rowView.setBackgroundColor(Color.WHITE);
    }
}

