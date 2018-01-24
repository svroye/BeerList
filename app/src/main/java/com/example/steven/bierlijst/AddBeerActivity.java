package com.example.steven.bierlijst;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.steven.bierlijst.data.BeerListContract;
import com.example.steven.bierlijst.data.BeerListDbHelper;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;

public class AddBeerActivity extends AppCompatActivity {

    // instances for the views on the screen
    Button mAddBeerButton;
    EditText mNameEditText;
    EditText mPercentageEditText;
    Button mGalleryButton;
    Button mCameraButton;
    ImageView mBeerImageView;

    // integer request code for the image capturing with an intent
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_beer);

        // instantiate the view variables
        mAddBeerButton = (Button) findViewById(R.id.addBeerConfirmButton);
        mNameEditText = (EditText) findViewById(R.id.beerNameValue);
        mPercentageEditText = (EditText) findViewById(R.id.beerPercentageValue);
        mGalleryButton = (Button) findViewById(R.id.galleryButton);
        mCameraButton = (Button) findViewById(R.id.cameraButton);
        mBeerImageView = (ImageView) findViewById(R.id.beerImage);

        // add a click listener to the confirm button, such that a new entry can be made in the DB
        mAddBeerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBeer();
            }
        });

        mGalleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

    }

    private void addBeer(){
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

        ContentResolver resolver = getContentResolver();

        resolver.insert(BeerListContract.BeerListEntry.CONTENT_URI, contentValues);

        finish();
    }

    private void takePicture(){
        Intent intentToTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intentToTakePicture.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intentToTakePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mBeerImageView.setImageBitmap(imageBitmap);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
