package com.example.steven.bierlijst;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.steven.bierlijst.data.BeerListContract;
import com.example.steven.bierlijst.data.BeerListDbHelper;

public class MainActivity extends AppCompatActivity
        implements BeerListAdapter.ListItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String TAG = "MainActivity";

    // RecyclerView for showing the list of beers
    RecyclerView mRecyclerView;

    // Adapter to bind to the RecyclerView
    BeerListAdapter mAdapter;

    // database instance for populating the recyclerview
    SQLiteDatabase mDb;

    TextView emptyStateTextView;

    public static final int BEER_LOADER_ID = 14589;

    public static final String[] MAIN_PROJECTION = {
            BeerListContract.BeerListEntry._ID,
            BeerListContract.BeerListEntry.COLUMN_NAME,
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_NAME = 1;

    public static final int REQUEST_CODE_ADD_BEER = 9756;

    SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emptyStateTextView = (TextView) findViewById(R.id.recyclerViewEmptyState);

        // get reference to a database instance
        BeerListDbHelper dbHelper = new BeerListDbHelper(this);
        mDb = dbHelper.getWritableDatabase();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

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
                Intent intentToAddBeer = new Intent(MainActivity.this, AddBeerActivity.class);
                startActivityForResult(intentToAddBeer, REQUEST_CODE_ADD_BEER);
            }
        });

        getSupportLoaderManager().initLoader(BEER_LOADER_ID, null, this);
    }


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
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onListItemClick(int clickedItemId) {
        Intent intentToDetailsActivity = new Intent(this, DetailsBeerActivity.class);
        Uri uriWithId = BeerListContract.BeerListEntry.getUriWithAppendedId(clickedItemId);
        intentToDetailsActivity.setData(uriWithId);
        startActivity(intentToDetailsActivity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case BEER_LOADER_ID:
                Uri uri = BeerListContract.BeerListEntry.CONTENT_URI;
                String sortPreference = getSortOrderColumnName(mSharedPreferences.getString(
                        getString(R.string.pref_key_order), getString(R.string.pref_order_by_name_value)));
                Log.d(TAG, "Sorting according to: " + sortPreference);
                String sortOrder = sortPreference + " ASC";
                return new CursorLoader(this, uri, MAIN_PROJECTION, null, null, sortOrder);
            default:
                throw new RuntimeException("Loader not implemented: " + BEER_LOADER_ID);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public String getSortOrderColumnName(String sort){
        String columnName = null;
        if(sort.equals(getString(R.string.pref_order_by_name_value))){
            columnName = BeerListContract.BeerListEntry.COLUMN_NAME;
        } else if(sort.equals(getString(R.string.pref_order_by_percentage_value))){
            columnName = BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE;
        }
        return columnName;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case RESULT_OK:
                showItemAddedSnackBar();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showItemAddedSnackBar(){
        final Snackbar snackBar = Snackbar.make(mRecyclerView, getString(R.string.snackbar_add_beer_successful),
                Snackbar.LENGTH_LONG);
        snackBar.setAction(getString(R.string.snackbar_ok_button), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackBar.dismiss();
                        Log.d(TAG, "Snackbar closed!!");
                    }
                });
        snackBar.show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged is triggered!!!");
        if(key.equals(getString(R.string.pref_key_order))){
            Log.d(TAG, "Ordering method is changed !");
            getSupportLoaderManager().restartLoader(BEER_LOADER_ID, null, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

}
