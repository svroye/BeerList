package com.example.steven.bierlijst.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Steven on 22/01/2018.
 */

public class BeerProvider extends ContentProvider {

    public static final String TAG = "BeerProvider";

    // helper instance for getting a reference to a database instance
    BeerListDbHelper mDbHelper;

    // int code for the content provider for the entire table of beers
    public static final int BEERS = 200;
    // int code for the content provider for a single beer entry
    public static final int BEERS_WITH_ID = 201;

    // uri matcher for checking which data is requested
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /*
    returns a UriMatcher, with the URIs for the beerlist table and a single entry in this table
     */
    public static UriMatcher buildUriMatcher(){
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(BeerListContract.AUTHORITY, BeerListContract.PATH_BEERS, BEERS);
        matcher.addURI(BeerListContract.AUTHORITY, BeerListContract.PATH_BEERS + "/#", BEERS_WITH_ID);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new BeerListDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);

        // return cursor containing the result from the query
        Cursor returnCursor;

        switch (match){
            // return all the beers
            case BEERS:
                returnCursor = db.query(BeerListContract.BeerListEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            // return the beer with a certain id
            case BEERS_WITH_ID:
                // retrieve the id from the uri
                String id = uri.getPathSegments().get(1);

                // set up the selection and selectionArg for the SQL query
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[] {id};

                returnCursor = db.query(BeerListContract.BeerListEntry.TABLE_NAME,
                        projection, mSelection, mSelectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //Log.d(TAG, "Cursor count " + returnCursor.getCount());
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        // get a database instance to write to
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // get the match for the URI
        int match = sUriMatcher.match(uri);
        // this is the URI that will be returned by the function
        Uri returnUri;

        // check for the values of the match
        switch (match){
            case BEERS:
                long id = db.insert(BeerListContract.BeerListEntry.TABLE_NAME, null, contentValues);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(BeerListContract.BeerListEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into: " + uri);
                }
                break;
            case BEERS_WITH_ID:
            default:
                throw new UnsupportedOperationException("Invalid Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "Return Uri is: " + returnUri);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);

        int numberOfRowsDeleted = 0;

        switch (match){
            case BEERS_WITH_ID:
                String id = uri.getPathSegments().get(1);
                numberOfRowsDeleted = db.delete(BeerListContract.BeerListEntry.TABLE_NAME, "_id=?", new String[] {id} );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        if (numberOfRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numberOfRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
                      @Nullable String[] strings) {
        return 0;
    }

}
