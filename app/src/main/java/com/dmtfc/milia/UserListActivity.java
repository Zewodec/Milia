package com.dmtfc.milia;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The list of all users in system
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class UserListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        /* If user not log in then go log in */
        if (ParseUser.getCurrentUser() == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setTitle("MILIA - Користувачі");

        ListView userListView = findViewById(R.id.userListView);
        ArrayList<String> usernames = new ArrayList<>();

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);

        /* If some user CLICKED than opens its profile */
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), UserFeedActivity.class);
                intent.putExtra("username", usernames.get(i));
                startActivity(intent);
            }
        });

        /* Show all users */
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.addAscendingOrder("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseUser user : objects) {
                            usernames.add(user.getUsername());
                        }
                    }
                    userListView.setAdapter(adapter);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    /* Create little menu in top bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.user_list_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /* Some options in menu */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            /* Check permission on external storage
            * And then open select image dialog
            * and send on backend */
            case R.id.share:
                CheckExternalStoragePermission();
                break;
            /* Process of logging out */
            case R.id.LogOut: {
                ParseUser.logOut();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            /* Open current user's profile */
            case R.id.MyProfile: {
                Intent intent = new Intent(this, UserProfileActivity.class);
                startActivity(intent);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check access to External Storage.
     * Than Start process of sharing photo ( Method: getPhotoToShare(); )
     */
    private void CheckExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                getPhotoToShare();
            }
        }
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> photoActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();

                        //do some operation
                        if (data != null) {

                            Uri selectedImage = data.getData();
                            try {
                                Log.i("Image", "Image Selected!");

                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, Integer.parseInt(getString(R.string.png_quality_upload)), stream);
                                byte[] byteArray = stream.toByteArray();
                                ParseFile file = new ParseFile("image.png", byteArray);
                                ParseObject object = new ParseObject("Image");
                                object.put("image", file);
                                object.put("username", ParseUser.getCurrentUser().getUsername());
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Toast.makeText(UserListActivity.this, "Зображення було додане!", Toast.LENGTH_SHORT).show();
                                            Log.i("Add Photo", "Success photo added");
                                        } else {
                                            Toast.makeText(UserListActivity.this, "Помилка: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e("Add Photo", "Error with add photo");
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                Log.e("Image", "ERROR:" + e.getMessage());
                                e.printStackTrace();
                            }

                        }
                    }
                }
            });

    /**
     * Start activity of getting image and than
     * send it on server.
     */
    private void getPhotoToShare() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoActivityResultLauncher.launch(intent);
    }
}