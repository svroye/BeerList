package com.example.steven.bierlijst.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by Steven on 20/01/2018.
 */

public class BeerListDbHelper extends SQLiteOpenHelper {

    public static final String TAG = "BeerListDbHelper";

    static final String DATABASE_NAME = "beerlist.db";
    static final int DATABASE_VERSION = 2;

    public BeerListDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_BEERLIST_TABLE = "CREATE TABLE "
                + BeerListContract.BeerListEntry.TABLE_NAME + " ("
                + BeerListContract.BeerListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BeerListContract.BeerListEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE + " REAL NOT NULL, "
                + BeerListContract.BeerListEntry.COLUMN_IMAGE_ID + " TEXT "
                + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_BEERLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(TAG, "Upgrading Database version");
        String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS "
                + BeerListContract.BeerListEntry.TABLE_NAME + ";";

        sqLiteDatabase.execSQL(SQL_DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
