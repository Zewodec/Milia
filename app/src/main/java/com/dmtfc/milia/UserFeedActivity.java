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
import androidx.gridlayout.widget.GridLayout;
import android.widget.ImageView;
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

        GridLayout photoGridLayout = (GridLayout) findViewById(R.id.FeedGridLayout);

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
                                            photoGridLayout.getWidth() / 3,
                                            photoGridLayout.getWidth() / 3
                                    ));

                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

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

        SetUserFeedInfo(username);

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("username", username);
        userQuery.setLimit(1);
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
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

        followerRemoveFromFollowUser(username);

        CheckIsFollowingAndSetButtonStyle(FollowingButton, username);
    }

    private void followerRemoveFromFollowUser(String username) {

        ProgressDialog dialog = new ProgressDialog(UserFeedActivity.this);

        ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
        userParseQuery.whereEqualTo("username", username);
        userParseQuery.setLimit(1);
        ParseUser localUser = null;
        try {
            localUser = userParseQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dialog.show();

        ParseQuery<ParseObject> haveFollowerObjectQuery = ParseQuery.getQuery("Followers");
        haveFollowerObjectQuery.whereEqualTo("username", localUser);
        haveFollowerObjectQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    dialog.show();
                    ParseObject userHaveFollowers = objects.get(0);
                    userHaveFollowers.getList("haveFollowers").remove(username);
                    List tempHaveFollowers = userHaveFollowers.getList("haveFollowers");
                    userHaveFollowers.remove("haveFollowers");
                    userHaveFollowers.put("haveFollowers", tempHaveFollowers);
                    userHaveFollowers.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            dialog.dismiss();
                            if (e == null) {
                                Log.i("Remove Follower", "Successful remove have Followers");
                            } else {
                                Log.e("Remove Follower", "Unsuccessful remove have Followers: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (e != null) {
                    Log.e("Find Object in Follower", "Failed Find: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void FollowUser(Button FollowingButton, String username) {
        ParseUser.getCurrentUser().add("isFollowing", username);
        ParseUser.getCurrentUser().saveInBackground();

        followerAddToFollowUser(username);

        CheckIsFollowingAndSetButtonStyle(FollowingButton, username);
    }

    private void followerAddToFollowUser(String username) {

        ProgressDialog dialog = new ProgressDialog(UserFeedActivity.this);

        ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
        userParseQuery.whereEqualTo("username", username);
        userParseQuery.setLimit(1);
        ParseUser localUser = null;
        try {
            localUser = userParseQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dialog.show();
        ParseQuery<ParseUser> haveFollowerUserQuery = ParseQuery.getQuery("Followers");
        haveFollowerUserQuery.whereEqualTo("username", localUser);
        ParseUser finalLocalUser = localUser;
        haveFollowerUserQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    dialog.show();
                    if (objects == null || objects.size() == 0) {
                        ParseObject userHaveFollowers = new ParseObject("Followers");
                        userHaveFollowers.put("username", finalLocalUser);
                        userHaveFollowers.addUnique("haveFollowers", ParseUser.getCurrentUser().getUsername());
                        userHaveFollowers.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                dialog.dismiss();
                                if (e == null) {
                                    Log.i("Add User in Followers", "Successful add [zero] have Followers");
                                } else {
                                    Log.e("Add User in Followers", "Unsuccessful add [zero] have Followers: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else if (e != null){
                    Log.e("Find User in Followers", "Failed Find: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });


        ParseQuery<ParseObject> haveFollowerObjectQuery = ParseQuery.getQuery("Followers");
        haveFollowerObjectQuery.whereEqualTo("username", localUser);
        haveFollowerObjectQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    dialog.show();
                    ParseObject userHaveFollowers = objects.get(0);
                    userHaveFollowers.addUnique("haveFollowers", ParseUser.getCurrentUser().getUsername());
                    userHaveFollowers.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            dialog.dismiss();
                            if (e == null) {
                                Log.i("Add User in Followers", "Successful add have Followers");
                            } else {
                                Log.e("Add User in Followers", "Unsuccessful add have Followers: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (e != null) {
                    Log.e("Find Object in Follower", "Failed Find: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

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
                    isFollowingCountTextView = findViewById(R.id.isFollowingCountTextView);
                    ParseUser foundUser = objects.get(0);
                    List isFollowing = foundUser.getList("isFollowing");
                    isFollowingCountTextView.setText(isFollowing.size() + "");

                    isFollowingCountTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), FollowersListActivity.class);
                            intent.putExtra("Name", "Підписки:");
                            intent.putExtra("Type Followers", "isFollowing");
                            intent.putExtra("username", username);
                            startActivity(intent);
                        }
                    });
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
                    ParseObject foundUsersFollowers = objects.get(0);
                    haveFollowersCountTextView = findViewById(R.id.haveFollowersCountTextView);
                    List haveFollowers = foundUsersFollowers.getList("haveFollowers");
                    haveFollowersCountTextView.setText(haveFollowers.size() + "");
                    haveFollowersCountTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), FollowersListActivity.class);
                            intent.putExtra("Name", "Читачи:");
                            intent.putExtra("Type Followers", "haveFollowers");
                            intent.putExtra("username", username);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }
}