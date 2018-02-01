package com.example.steven.bierlijst;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.steven.bierlijst.data.BeerListContract;

/**
 * Created by Steven on 20/01/2018.
 */

public class BeerListAdapter extends RecyclerView.Adapter<BeerListAdapter.BeerViewHolder> {

    // tag for logging
    public static final String TAG = "BeerListAdapter";

    // Cursor holding the database instances
    private Cursor mCursor;

    Context mContext;

    ListItemClickListener mListItemClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemId);
    }

    /*
    Constructor for the BeerListAdapter
     */
    public BeerListAdapter(Context context, ListItemClickListener listItemClickListener){
        mContext = context;
        mListItemClickListener = listItemClickListener;
    }

    @Override
    public BeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "Entering onCreateViewHolder");
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        BeerViewHolder viewHolder = new BeerViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BeerViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) return;
        // getting the name from the current row
        String name = mCursor.getString(mCursor.getColumnIndex(BeerListContract.BeerListEntry.COLUMN_NAME));
        double percentage = mCursor.getDouble(mCursor.getColumnIndex(BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE));
        holder.bind(name, percentage);
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) return mCursor.getCount();
        return 0;
    }

    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (mCursor != null) mCursor.close();
        mCursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    public class BeerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        TextView mBeerNameTextView;
        TextView mAlcoholPercentageTextView;
        int id;

        public BeerViewHolder(View itemView) {
            super(itemView);
            mBeerNameTextView = itemView.findViewById(R.id.textViewBeerName);
            mAlcoholPercentageTextView = itemView.findViewById(R.id.textViewAlcoholPercentage);
            itemView.setOnClickListener(this);
        }

        public void bind(String name, double percentage){
            mBeerNameTextView.setText(name);
            mAlcoholPercentageTextView.setText(Double.toString(percentage) + "%");
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            int clickedItemId = mCursor.getInt(mCursor.getColumnIndex(BeerListContract.BeerListEntry._ID));
            mListItemClickListener.onListItemClick(clickedItemId);
        }
    }

}
