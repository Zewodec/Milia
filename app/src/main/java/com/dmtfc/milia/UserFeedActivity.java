package com.dmtfc.milia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * The Content of User's photo Activity
 */
public class UserFeedActivity extends AppCompatActivity {

    private boolean isFollowing;

    private TextView haveFollowersCountTextView;
    private TextView isFollowingCountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        setTitle("@" + username);

        LinearLayout photoLayoutList = (LinearLayout) findViewById(R.id.photoLayoutList);

        Button FollowingButton = findViewById(R.id.FollowingButton);

        isFollowing = ParseUser.getCurrentUser().getList("isFollowing").contains(username);

        CheckIsFollowingAndSetButtonStyle(FollowingButton, username);

        FollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFollowing) {
                    UnFollowFromUser(FollowingButton, username);
                } else {
                    FollowUser(FollowingButton, username);
                }
            }
        });

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Image");

        query.whereEqualTo("username", username);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {

                    TextView postCountTextView = findViewById(R.id.PostCountTextView);
                    postCountTextView.setText(objects.size() + "");

                    for (ParseObject object : objects) {
                        ParseFile file = (ParseFile) object.get("image");

                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null && data != null) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                    ImageView imageView = new ImageView(getApplicationContext());

                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    ));

                                    imageView.setImageBitmap(bitmap);
                                    imageView.setPadding(0, 0, 0, 20);

                                    photoLayoutList.addView(imageView);
                                }
                            }
                        });
                    }
                    SetUserFeedInfo(username);
                }
            }
        });

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username", username);
        userQuery.setLimit(1);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects.size() > 0){
                    ParseUser foundUser = objects.get(0);
                    loadUserProfileImage(foundUser);
                }
            }
        });
    }

    private void loadUserProfileImage(ParseUser user) {
        ParseFile file = (ParseFile) user.get("ava");
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

    private void CheckIsFollowingAndSetButtonStyle(Button FollowingButton, String username) {
        isFollowing = ParseUser.getCurrentUser().getList("isFollowing").contains(username);
        if (isFollowing) {
            FollowingButton.setText("Відстужується");
            FollowingButton.setBackgroundColor(Color.GRAY);
        } else {
            FollowingButton.setText("Стежити");
            FollowingButton.setBackgroundColor(Color.parseColor("#FF6200EE"));
        }
    }

    private void UnFollowFromUser(Button FollowingButton, String username) {
        ParseUser.getCurrentUser().getList("isFollowing").remove(username);
        List tempIsFollowing = ParseUser.getCurrentUser().getList("isFollowing");
        ParseUser.getCurrentUser().remove("isFollowing");
        ParseUser.getCurrentUser().put("isFollowing", tempIsFollowing);
        ParseUser.getCurrentUser().saveInBackground();
/*
        ParseQuery<ParseUser> haveUnFollowerQuery = ParseQuery.getQuery("User");
        haveUnFollowerQuery.whereEqualTo("username", username);
        haveUnFollowerQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    ParseUser foundUser = objects.get(0);
                    if (foundUser.getList("haveFollowers").contains(ParseUser.getCurrentUser().getUsername())) {
                        foundUser.getList("haveFollowers").remove(ParseUser.getCurrentUser().getUsername());
                        List tempHaveUnFollower = foundUser.getList("haveFollowers");
                        foundUser.remove("haveFollowers");
                        foundUser.put("haveFollowers", tempHaveUnFollower);
                        foundUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.i("Remove haveFollowers", "Successful remove haveFollower " + ParseUser.getCurrentUser().getUsername() + " from " + username);
                                } else {
                                    Log.e("Remove haveFollowers", "Unsuccessful remove haveFollower " + ParseUser.getCurrentUser().getUsername() + " from " + username + "\n" + e.getMessage() + "\n\n");
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });*/

        CheckIsFollowingAndSetButtonStyle(FollowingButton, username);
    }

    private void FollowUser(Button FollowingButton, String username) {
        ParseUser.getCurrentUser().add("isFollowing", username);
        ParseUser.getCurrentUser().saveInBackground();
/*
        ParseQuery<ParseUser> haveFollowerQuery = ParseQuery.getQuery("_User");
        haveFollowerQuery.whereEqualTo("username", username);
        haveFollowerQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    ParseUser foundUser = objects.get(0);
                    if (!foundUser.getList("haveFollowers").contains(ParseUser.getCurrentUser().getUsername())) {
                        foundUser.add("haveFollowers", ParseUser.getCurrentUser().getUsername());
                        foundUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.i("Add haveFollowers", "Successful add haveFollower " + ParseUser.getCurrentUser().getUsername() + " to " + username);
                                } else {
                                    Log.e("Add haveFollowers", "Unsuccessful add haveFollower " + ParseUser.getCurrentUser().getUsername() + " to " + username + "\n" + e.getMessage() + "\n\n");
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });*/

        CheckIsFollowingAndSetButtonStyle(FollowingButton, username);
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
//                    haveFollowersCountTextView = findViewById(R.id.haveFollowersCountTextView);
                    isFollowingCountTextView = findViewById(R.id.isFollowingCountTextView);

                    ParseUser foundUser = objects.get(0);
//                    List haveFollowers = foundUser.getList("haveFollowers");
                    List isFollowing = foundUser.getList("isFollowing");

//                    haveFollowersCountTextView.setText(haveFollowers.size() + "");
                    isFollowingCountTextView.setText(isFollowing.size() + "");
                }
            }
        });
    }

}