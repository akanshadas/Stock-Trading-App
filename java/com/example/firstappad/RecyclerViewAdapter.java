//package com.example.firstappad;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.ArrayList;
//import java.util.Collections;
//
//public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
//
//    private ArrayList<String> data;
//
//    public class MyViewHolder extends RecyclerView.ViewHolder {
//        private TextView mTitle;
//        View rowView;
//
//        public MyViewHolder(View itemView) {
//            super(itemView);
//            rowView = itemView;
//            //mTitle = itemView.findViewById(R.id.txtTitle);
//
//            //ON CLICK LISTENER OPTION 2
//        }
//    }
//
//    public RecyclerViewAdapter(ArrayList<String> data) {
//        this.data = data;
//    }
//
//    @Override
//    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_row, parent, false);
//        return new MyViewHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(MyViewHolder holder, int position) {
//        holder.mTitle.setText(data.get(position));
//    }
//
//    @Override
//    public int getItemCount() {
//        return data.size();
//    }
//
//    public void removeItem(int position) {
//        data.remove(position);
//        notifyItemRemoved(position);
//        Log.d("DELETED:", String.valueOf(position));
//        Log.d("tracking DELETED " , data.toString());
//    }
//
//
//    public ArrayList<String> getData() {
//        return data;
//    }
//
//    // DRAG
//    @Override
//    public void onRowMoved(int fromPosition, int toPosition) {
//        if (fromPosition < toPosition) {
//            for (int i = fromPosition; i < toPosition; i++) {
//                Collections.swap(data, i, i + 1);
//            }
//        } else {
//            for (int i = fromPosition; i > toPosition; i--) {
//                Collections.swap(data, i, i - 1);
//            }
//        }
//        notifyItemMoved(fromPosition, toPosition);
//        Log.d("tracking SWAPPED:", data.toString());
//    }
//
//    @Override
//    public void onRowSelected(MyViewHolder myViewHolder) {
//        //myViewHolder.rowView.setBackgroundColor(Color.GRAY);
//    }
//
//    @Override
//    public void onRowClear(MyViewHolder myViewHolder) {
//        //myViewHolder.rowView.setBackgroundColor(Color.WHITE);
//    }
//}
//
