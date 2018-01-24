package com.example.steven.bierlijst;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.steven.bierlijst.data.BeerListContract;

public class DetailsBeerActivity extends AppCompatActivity
                implements LoaderManager.LoaderCallbacks<Cursor>{

    // stores the uri coming from the clicked list item in the MainActivity
    Uri uriFromIntent;

    // id for the loader used for quering the data
    public static final int DETAIL_LOADER_ID = 23657;

    public static final String[] DETAILS_PROJECTION = {
            BeerListContract.BeerListEntry._ID,
            BeerListContract.BeerListEntry.COLUMN_NAME,
            BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE,
            BeerListContract.BeerListEntry.COLUMN_IMAGE_ID
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_NAME = 1;
    public static final int INDEX_ALCOHOL_PERCENTAGE = 2;
    public static final int INDEX_IMAGE_ID = 3;

    private int rowId;

    private TextView beerNameTextView;
    private TextView beerAlcoholPercentage;
    private ImageView beerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_beer);

        beerNameTextView = (TextView) findViewById(R.id.detailsBeerNameValue);
        beerAlcoholPercentage = (TextView) findViewById(R.id.detailsBeerPercentageValue);
        beerImage = (ImageView) findViewById(R.id.detailsBeerImage);

        uriFromIntent = getIntent().getData();
        if (uriFromIntent == null) throw new NullPointerException("Uri cannot be null!");
        getSupportLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case DETAIL_LOADER_ID:
                return new CursorLoader(this, uriFromIntent, DETAILS_PROJECTION, null, null, null);
            default:
                throw new RuntimeException("Loader not implemented: " + DETAIL_LOADER_ID);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        boolean isValidCursor = false;

        if(data != null && data.moveToFirst()) isValidCursor = true;
        if(!isValidCursor) return;

        // get the name of the beer and display it on the TextView
        String name = data.getString(INDEX_NAME);
        beerNameTextView.setText(name);

        // get the alcohol percentage from the beer and display it on the TextView
        double alcoholPercentage = data.getInt(INDEX_ALCOHOL_PERCENTAGE);
        beerAlcoholPercentage.setText(Double.toString(alcoholPercentage));

        rowId = data.getInt(INDEX_ID);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_delete_beer){
            Uri uri = BeerListContract.BeerListEntry.getUriWithAppendedId(rowId);
            getContentResolver().delete(uri, null, null);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
