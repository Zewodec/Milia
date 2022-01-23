package com.dmtfc.milia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * My Profile Activity for authorized user
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class UserProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        loadUserProfileImage();

        String username = ParseUser.getCurrentUser().getUsername();

        ParseQuery<ParseObject> query = new ParseQuery<>("Image");

        query.whereEqualTo("username", username);
        query.orderByDescending("createdAt");

        GridLayout photoGridLayout = findViewById(R.id.PhotoGridLayout);

        SetUserFeedInfo(username);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    for (ParseObject object : objects) {
                        ParseFile file = (ParseFile) object.get("image");

                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null && data != null) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                    ImageView imageView = new ImageView(getApplicationContext());

                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                            photoGridLayout.getWidth() / 3,
                                            400));
                                    imageView.setScaleType(ImageView.ScaleType.CENTER);

                                    imageView.setImageBitmap(bitmap);

                                    imageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(getApplicationContext(), PhotoShowActivity.class);
                                            intent.putExtra("Image", object.getObjectId());
                                            startActivity(intent);
                                        }
                                    });

                                    photoGridLayout.addView(imageView);
                                }
                            }
                        });
                    }
                }
            }
        });

        Button openSettingsButton = findViewById(R.id.OpenSettingsButton);
        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserProfileImage() {
        ParseFile file = (ParseFile) ParseUser.getCurrentUser().get("ava");
        if (file != null) {
            file.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    ImageView AvaImage = findViewById(R.id.AvaImage);

                    AvaImage.setImageBitmap(bitmap);

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.user_profile_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.settingsProfileMenu) {
            Intent intent = new Intent(this, SettingsProfileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set Users info/parameters as Followers, Whom Following and amount of images
     *
     * @param username Username of person who opened
     */
    private void SetUserFeedInfo(String username) {
        ParseQuery<ParseUser> usersQuery = ParseUser.getQuery();

        usersQuery.whereEqualTo("username", username);

        usersQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    TextView isFollowingCountTextView = findViewById(R.id.isFollowingCountTextView);
                    isFollowingCountTextView = findViewById(R.id.isFollowingCountTextView);
                    ParseUser foundUser = objects.get(0);
                    List isFollowing = foundUser.getList("isFollowing");
                    isFollowingCountTextView.setText(isFollowing.size() + "");
                }
            }
        });

        ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
        userParseQuery.whereEqualTo("username", username);
        userParseQuery.setLimit(1);
        ParseUser localUser = null;
        try {
            localUser = userParseQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseQuery<ParseObject> haveFollowersQuery = ParseQuery.getQuery("Followers");
        haveFollowersQuery.whereEqualTo("username", localUser);
        haveFollowersQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0){
                    TextView haveFollowersCountTextView = findViewById(R.id.haveFollowersCountTextView);
                    ParseObject foundUsersFollowers = objects.get(0);
                    haveFollowersCountTextView = findViewById(R.id.haveFollowersCountTextView);
                    List haveFollowers = foundUsersFollowers.getList("haveFollowers");
                    haveFollowersCountTextView.setText(haveFollowers.size() + "");
                }
            }
        });
    }
}