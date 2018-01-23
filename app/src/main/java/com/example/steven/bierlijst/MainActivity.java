package com.example.steven.bierlijst;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.steven.bierlijst.data.BeerListContract;
import com.example.steven.bierlijst.data.BeerListDbHelper;

public class MainActivity extends AppCompatActivity
        implements BeerListAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {


    public static final String TAG = "MainActivity";

    // RecyclerView for showing the list of beers
    RecyclerView mRecyclerView;

    // Adapter to bind to the RecyclerView
    BeerListAdapter mAdapter;

    // database instance for populating the recyclerview
    SQLiteDatabase mDb;

    public static final int BEER_LOADER_ID = 14589;

    public static final String[] MAIN_PROJECTION = {
            BeerListContract.BeerListEntry.COLUMN_NAME,
            BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE,
            BeerListContract.BeerListEntry.COLUMN_IMAGE_ID
    };

    public static final int INDEX_NAME = 0;
    public static final int INDEX_ALCOHOL_PERCENTAGE = 1;
    public static final int INDEX_IMAGE_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BeerListDbHelper dbHelper = new BeerListDbHelper(this);
        mDb = dbHelper.getWritableDatabase();

        // find a reference to the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewBeerList);

        // create a new adapter, passing in the cursor holding the database entries
        LinearLayoutManager manager = new LinearLayoutManager(this);

        // create layout manager and attach it to the recyclerview
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new BeerListAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Intent intentToAddBeer = new Intent(MainActivity.this, AddBeerActivity.class);
                startActivity(intentToAddBeer);
            }
        });

        getSupportLoaderManager().initLoader(BEER_LOADER_ID, null, this);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intentToDetailsActivity = new Intent(this, DetailsBeerActivity.class);
        startActivity(intentToDetailsActivity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case BEER_LOADER_ID:
                Uri uri = BeerListContract.BeerListEntry.CONTENT_URI;
                String sortOrder = BeerListContract.BeerListEntry.COLUMN_NAME + " ASC";
                return new CursorLoader(this, uri, MAIN_PROJECTION, null, null, sortOrder);
            default:
                throw new RuntimeException("Loader not implemented!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        Log.d(TAG, "Swapping cursor !!!!!");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
