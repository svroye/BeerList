package com.example.steven.bierlijst;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.steven.bierlijst.data.BeerListContract;
import com.example.steven.bierlijst.data.FileUtils;

public class DetailsBeerActivity extends AppCompatActivity
                implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "DetailsBeerActivity";
    // stores the uri coming from the clicked list item in the MainActivity
    Uri uriFromIntent;

    // id for the loader used for quering the data
    public static final int DETAIL_LOADER_ID = 23657;

    public static final String[] DETAILS_PROJECTION = {
            BeerListContract.BeerListEntry._ID,
            BeerListContract.BeerListEntry.COLUMN_NAME,
            BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE,
            BeerListContract.BeerListEntry.COLUMN_IMAGE_ID,
            BeerListContract.BeerListEntry.COLUMN_RATING
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_NAME = 1;
    public static final int INDEX_ALCOHOL_PERCENTAGE = 2;
    public static final int INDEX_IMAGE_ID = 3;
    public static final int INDEX_RATING = 4;

    public static final int MODIFY_BEER_INTENT_CODE = 4721;

    private int rowId;

    private TextView beerNameTextView;
    private TextView beerAlcoholPercentage;
    private ImageView beerImage;
    private RatingBar beerRatingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_beer);

        beerNameTextView = (TextView) findViewById(R.id.detailsBeerNameValue);
        beerAlcoholPercentage = (TextView) findViewById(R.id.detailsBeerPercentageValue);
        beerImage = (ImageView) findViewById(R.id.detailsBeerImage);
        beerRatingBar = (RatingBar) findViewById(R.id.detailsRatingBar);

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
        double alcoholPercentage = data.getDouble(INDEX_ALCOHOL_PERCENTAGE);
        beerAlcoholPercentage.setText(Double.toString(alcoholPercentage));

        // get the uri from the image. If it's null, no image was added and nothing needs to be done
        String stringUriFromImage = data.getString(INDEX_IMAGE_ID);
        if(stringUriFromImage != null){
            Bitmap bitmap = FileUtils.getBitmapFromUri(Uri.parse(stringUriFromImage), this);
            beerImage.setImageBitmap(bitmap);
        }

        float beerRating = data.getFloat(INDEX_RATING);
        if(beerRating != 0.0) {
            beerRatingBar.setRating(beerRating);
        } else {
            Log.e(TAG, "No rating available!");
            beerRatingBar.setRating((float) 0.0);
        }

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
        switch (id){
            case R.id.menu_item_delete_beer:
                showDialog();
                break;
            case R.id.menu_item_modify_beer:
                Uri uri = BeerListContract.BeerListEntry.getUriWithAppendedId(rowId);
                Intent intentToStartModifyBeer = new Intent(this, AddBeerActivity.class);
                intentToStartModifyBeer.setData(uri);
                startActivityForResult(intentToStartModifyBeer, MODIFY_BEER_INTENT_CODE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    creates a Dialog message for the user to make sure that he wants to delete this entry
    */
    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_delete_title))
                .setMessage(getString(R.string.dialog_delete_content));
        builder.setPositiveButton(getString(R.string.dialog_delete_confirm_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = BeerListContract.BeerListEntry.getUriWithAppendedId(rowId);
                getContentResolver().delete(uri, null, null);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(getString(R.string.flag_delete_beer), true);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_delete_ignore_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing, the system dismisses the dialog for us
            }
        });

        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == MODIFY_BEER_INTENT_CODE){
            if(resultCode == RESULT_OK) showSnackBar(getString(R.string.snackbar_modify_beer_successful));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    showing a Snackbar at the bottom of the page
     */
    public void showSnackBar(String action){
        final Snackbar snackBar = Snackbar.make(beerNameTextView, action,
                Snackbar.LENGTH_LONG);
        snackBar.setAction(getString(R.string.snackbar_ok_button), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackBar.dismiss();
            }
        });
        snackBar.show();
    }
}
