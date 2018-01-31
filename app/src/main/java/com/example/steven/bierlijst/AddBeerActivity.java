package com.example.steven.bierlijst;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.steven.bierlijst.data.BeerListContract;
import com.example.steven.bierlijst.data.BeerListDbHelper;
import com.example.steven.bierlijst.data.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class AddBeerActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String TAG = "AddBeerActivity";

    // instances for the views on the screen
    Button mAddBeerButton;
    EditText mNameEditText;
    EditText mPercentageEditText;
    Button mGalleryButton;
    Button mCameraButton;
    ImageView mBeerImageView;

    private Bitmap mBitmap;

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.steven.bierlijstprovider";


    // holds the uri if the activity was started from the details activity
    Uri mUriThatStartedActivity;

    // holds the uri pointing to the image of the gallery or the one taken
    // with the camera
    private Uri mImageFromGalleryOrCamera;

    // integer request code for the image capturing with an intent
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GALLERY = 2;
    static final int MY_PERMISSIONS_REQUEST = 3;

    // id for the loader when a beer item gets modified
    public static final int MODIFY_BEER_LOADER_ID = 14582;

    public static final String[] MODIFY_BEER_PROJECTION = {
            BeerListContract.BeerListEntry._ID,
            BeerListContract.BeerListEntry.COLUMN_NAME,
            BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE,
            BeerListContract.BeerListEntry.COLUMN_IMAGE_ID
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_NAME = 1;
    public static final int INDEX_ALCOHOL_PERCENTAGE = 2;
    public static final int INDEX_IMAGE_ID = 3;

    private boolean modifyBeer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beer);

        Intent intentThatStartedThisActivity = getIntent();
        mUriThatStartedActivity = intentThatStartedThisActivity.getData();

        // instantiate the view variables
        mAddBeerButton = (Button) findViewById(R.id.addBeerConfirmButton);
        mNameEditText = (EditText) findViewById(R.id.beerNameValue);
        mPercentageEditText = (EditText) findViewById(R.id.beerPercentageValue);
        mGalleryButton = (Button) findViewById(R.id.galleryButton);
        mCameraButton = (Button) findViewById(R.id.cameraButton);
        mBeerImageView = (ImageView) findViewById(R.id.beerImage);

        if (mUriThatStartedActivity != null){
            modifyBeer = true;
            setTitle(getString(R.string.modify_beer_activity_label));
            getSupportLoaderManager().initLoader(MODIFY_BEER_LOADER_ID, null, this);
        }

        // add a click listener to the confirm button, such that a new entry can be made in the DB
        mAddBeerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (modifyBeer){
                    Log.d(TAG, "Start modify beer flow");
                    modifyBeer();
                } else {
                    Log.d(TAG, "Start add beer flow");
                    addBeer();
                }
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImageFromGallery();
            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

    }

    private void chooseImageFromGallery() {

        Intent intentToOpenGallery;

        if (Build.VERSION.SDK_INT < 19) {
            Log.d(TAG, "SDK_INT < 19");
            intentToOpenGallery = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            Log.d(TAG, "SDK_INT >= 19");
            intentToOpenGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intentToOpenGallery.addCategory(Intent.CATEGORY_OPENABLE);
        }

        // Show only images, no videos or anything else
        Log.e(TAG, "Check write to external permissions");

        intentToOpenGallery.setType("image/*");
        startActivityForResult(Intent.createChooser(intentToOpenGallery, "Select Picture"), REQUEST_GALLERY);
    }

    private void addBeer(){
        ContentValues contentValues = new ContentValues();

        // get the name from the edit text field
        String name = mNameEditText.getText().toString();
        if (name.length() == 0) return;

        // get the alcohol percentage from the edit text field
        String percentageString = mPercentageEditText.getText().toString();
        if (percentageString.length() == 0) return;

        // parse the string to a double
        Double percentage;
        try {
            percentage = Double.parseDouble(percentageString);
        } catch (NullPointerException e){
            return;
        } catch (NumberFormatException e){
            return;
        }

        contentValues.put(BeerListContract.BeerListEntry.COLUMN_NAME, name);
        contentValues.put(BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE, percentage);
        if (mImageFromGalleryOrCamera != null){
            contentValues.put(BeerListContract.BeerListEntry.COLUMN_IMAGE_ID, mImageFromGalleryOrCamera.toString());
        }

        ContentResolver resolver = getContentResolver();

        Uri returnUri = resolver.insert(BeerListContract.BeerListEntry.CONTENT_URI, contentValues);

        if(returnUri != null){
            Intent returnIntent = new Intent();
            setResult(RESULT_OK, returnIntent);
            finish();
        }

    }

    private void modifyBeer(){
        ContentValues contentValues = new ContentValues();

        // get the name from the edit text field
        String name = mNameEditText.getText().toString();
        if (name.length() == 0) return;

        // get the alcohol percentage from the edit text field
        String percentageString = mPercentageEditText.getText().toString();
        if (percentageString.length() == 0) return;

        Double percentage;

        try {
            percentage = Double.parseDouble(percentageString);
        } catch (NullPointerException e){
            return;
        } catch (NumberFormatException e){
            return;
        }


        contentValues.put(BeerListContract.BeerListEntry.COLUMN_NAME, name);
        contentValues.put(BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE, percentage);

        if (mImageFromGalleryOrCamera != null){
            contentValues.put(BeerListContract.BeerListEntry.COLUMN_ALCOHOL_PERCENTAGE, mImageFromGalleryOrCamera.toString());
        }

        ContentResolver resolver = getContentResolver();

        resolver.update(mUriThatStartedActivity, contentValues, null, null);
        finish();
    }

    private void takePicture(){
        requestPermissions();

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File f = FileUtils.createImageFile(this);

            Log.d(TAG, "File: " + f.getAbsolutePath());

            mImageFromGalleryOrCamera = FileProvider.getUriForFile(
                    this, FILE_PROVIDER_AUTHORITY, f);

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageFromGalleryOrCamera);

            // Solution taken from http://stackoverflow.com/a/18332000/3346625
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    grantUriPermission(packageName, mImageFromGalleryOrCamera, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            mBitmap = FileUtils.getBitmapFromUri(mImageFromGalleryOrCamera, this);
        } else if(requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                mImageFromGalleryOrCamera = data.getData();
                Log.i(TAG, "Uri: " + mImageFromGalleryOrCamera.toString());

                mBitmap = FileUtils.getBitmapFromUri(mImageFromGalleryOrCamera, this);
            }
        }
        mBeerImageView.setImageBitmap(mBitmap);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MODIFY_BEER_LOADER_ID:
                return new CursorLoader(this, mUriThatStartedActivity, MODIFY_BEER_PROJECTION, null, null, null);
            default:
                throw new RuntimeException("Loader not implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        mNameEditText.setText(data.getString(INDEX_NAME));
        mPercentageEditText.setText(Double.toString(data.getDouble(INDEX_ALCOHOL_PERCENTAGE)));
        String uri = data.getString(INDEX_IMAGE_ID);
        if(uri != null){
            Bitmap bitmap = FileUtils.getBitmapFromUri(Uri.parse(uri), this);
            mBeerImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            //mButtonTakePicture.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //mButtonTakePicture.setEnabled(true);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
