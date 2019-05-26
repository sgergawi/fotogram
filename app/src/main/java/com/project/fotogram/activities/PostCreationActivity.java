package com.project.fotogram.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.project.fotogram.R;
import com.project.fotogram.communication.RequestWithParams;
import com.project.fotogram.communication.VolleySingleton;
import com.project.fotogram.model.SessionInfo;
import com.project.fotogram.utility.Constants;
import com.project.fotogram.utility.UtilityMethods;

public class PostCreationActivity extends AppCompatActivity {
    private final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postcreation);
        Button searchPhotoButt = (Button) findViewById(R.id.action_searchPhoto);
        searchPhotoButt.setOnClickListener(new View.OnClickListener() {
            //todo perform this
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PostCreationActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent gallery = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gallery, REQUEST_CODE);
                } else {
                    UtilityMethods.checkPermissions(PostCreationActivity.this, 1052);
                }
            }
        });
        Button savePostButt = (Button) findViewById(R.id.action_savePost);
        savePostButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("fotogramLogs", "save post cliccato!");
                try {
                    ImageView loadedImage = (ImageView) findViewById(R.id.loadedImage);

                    BitmapDrawable bitmapDrawable = (BitmapDrawable) loadedImage.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    byte[] imageBytes = UtilityMethods.resizePhoto(Constants.MAX_POST_BYTES, bitmap);
                    String imageToBeLoaded = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    String postComment = ((TextView) findViewById(R.id.newPostComment)).getText().toString();
                    RequestWithParams createPostRequest = new RequestWithParams(Request.Method.POST, Constants.BASEURL + "create_post", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("fotogramLogs", "finito caricamento!");
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            UtilityMethods.manageCommunicationError(error);
                        }
                    });
                    Log.d("fotogramLogs", "imageToBeLoaded: " + imageToBeLoaded);
                    Log.d("fotogramLogs", "messaggio: " + postComment);
                    createPostRequest.addParam("session_id", SessionInfo.getInstance().getSessionId(PostCreationActivity.this));
                    createPostRequest.addParam("img", imageToBeLoaded);
                    createPostRequest.addParam("message", postComment);
                    VolleySingleton.getInstance(PostCreationActivity.this.getApplicationContext()).addToRequestQueue(createPostRequest);
                } catch (Exception e) {
                    Log.d("fotogramLogs", e.getMessage());
                }

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1052:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent gallery = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(gallery, REQUEST_CODE);
                } else {
                    //todo MOSTRARE UN MESSAGGIO DI ERRORE per negazione permessi
                }
                break;
            default:
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imageView = (ImageView) findViewById(R.id.loadedImage);
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }
    }
}