package com.example.steven.bierlijst.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Steven on 20/01/2018.
 */

public class BeerListContract {

    public static final String SCHEME = "content://";
    public static final String AUTHORITY = "com.example.steven.bierlijst";
    public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY);

    public static final String PATH_BEERS = "beers";

    public static class BeerListEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_BEERS).build();

        public static final String TABLE_NAME = "beerList";
        public static final String COLUMN_NAME = "beerName";
        public static final String COLUMN_ALCOHOL_PERCENTAGE = "percentage";
        public static final String COLUMN_IMAGE_ID = "picture";
        public static final String COLUMN_RATING = "rating";

        public static Uri getUriWithAppendedId(int id){
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
        }
    }

}
