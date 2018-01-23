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
        implements BeerListAdapter.ListItemClickListener {



    public static final String TAG = "MainActivity";

    // RecyclerView for showing the list of beers
    RecyclerView mRecyclerView;

    // Adapter to bind to the RecyclerView
    BeerListAdapter mAdapter;

    // database instance for populating the recyclerview
    SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BeerListDbHelper dbHelper = new BeerListDbHelper(this);
        mDb = dbHelper.getWritableDatabase();



        // find a reference to the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerViewBeerList);

        Cursor cursor = getBeerList();


        // create a new adapter, passing in the cursor holding the database entries
        LinearLayoutManager manager = new LinearLayoutManager(this);


        // create layout manager and attach it to the recyclerview
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new BeerListAdapter(this, this, cursor);
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

    private Cursor getBeerList(){
        return mDb.query(BeerListContract.BeerListEntry.TABLE_NAME,
                null, null, null, null, null, BeerListContract.BeerListEntry.COLUMN_NAME);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        mAdapter.swapCursor(getBeerList());
//    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intentToDetailsActivity = new Intent(this, DetailsBeerActivity.class);
        startActivity(intentToDetailsActivity);
    }

}
