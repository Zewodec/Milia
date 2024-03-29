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
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class UserFeedActivity extends AppCompatActivity {

    private boolean isFollowing;                    // Is current user Following opened user?

    private TextView haveFollowersCountTextView;    // User's followers TextView
    private TextView isFollowingCountTextView;      // Amount User's who is followed by current User TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");    // Getting username from previous activity

        setTitle("@" + username);

        GridLayout photoGridLayout = (GridLayout) findViewById(R.id.FeedGridLayout);    // Images showing in grid
        Button FollowingButton = findViewById(R.id.FollowingButton);                    // Button for follow/unfollow

        // Check if current user is following opened profile
        isFollowing = ParseUser.getCurrentUser().getList("isFollowing").contains(username);

        // Set style for button based of isFollowing
        CheckIsFollowingAndSetButtonStyle(FollowingButton, username);

        FollowingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If Following:
                if (isFollowing) {
                    // true: unfollow
                    UnFollowFromUser(FollowingButton, username);
                } else {
                    // false: follow
                    FollowUser(FollowingButton, username);
                }
            }
        });

        /* Load images to grid view */
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Image");
        query.whereEqualTo("username", username);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {

                    // Get amount of images in profile
                    TextView postCountTextView = findViewById(R.id.PostCountTextView);
                    postCountTextView.setText(objects.size() + "");

                    // Load every image from profile
                    for (ParseObject object : objects) {
                        ParseFile file = (ParseFile) object.get("image");

                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null && data != null) {
                                    // Decode and get full image
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                    ImageView imageView = new ImageView(getApplicationContext());

                                    // Set size of this image
                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                            photoGridLayout.getWidth() / 3,
                                            photoGridLayout.getWidth() / 3
                                    ));

                                    // Set how it will look like in this square
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                    // Set image to image View
                                    imageView.setImageBitmap(bitmap);

                                    // Add possibility to open image with comments
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

        // Update info like amount of images, followers, readers
        SetUserFeedInfo(username);

        // Load ava for profile
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

    /**
     * Load Ava for opened profile
     *
     * @param user [ParseUser] a user object need to get user's ava
     */
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

    /**
     * Check if current user follows user and set for
     * button special style if follows or not.
     * @param FollowingButton The Button on which style will be applied
     * @param username Check if current user is following specific user
     */
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

    /**
     * Method implements a process unfollowing from user.
     * It delete username of current user as in own followers and
     * user's readers.
     * @param FollowingButton For editing style of following button
     * @param username current user who will be unfollowed
     */
    private void UnFollowFromUser(Button FollowingButton, String username) {
        ParseUser.getCurrentUser().getList("isFollowing").remove(username);
        List tempIsFollowing = ParseUser.getCurrentUser().getList("isFollowing");
        ParseUser.getCurrentUser().remove("isFollowing");
        ParseUser.getCurrentUser().put("isFollowing", tempIsFollowing);
        ParseUser.getCurrentUser().saveInBackground();

        followerRemoveFromFollowUser(username);

        CheckIsFollowingAndSetButtonStyle(FollowingButton, username);
    }

    /**
     * Unfollow from user and delete info about current follower
     * from his profile.
     * @param username current user who will be unfollowed from subscribed person
     */
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

        /* Unfollowing query in follower profile */
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

    /**
     * Follow some user and change follow button style.
     * @param FollowingButton Button which changes its style
     * @param username Add to current user list who will be followed by
     */
    private void FollowUser(Button FollowingButton, String username) {
        ParseUser.getCurrentUser().add("isFollowing", username);
        ParseUser.getCurrentUser().saveInBackground();

        followerAddToFollowUser(username);

        CheckIsFollowingAndSetButtonStyle(FollowingButton, username);
    }

    /**
     * Add current user to follower list of some user
     * @param username Some user
     */
    private void followerAddToFollowUser(String username) {

        ProgressDialog dialog = new ProgressDialog(UserFeedActivity.this);

        /* Get user to edit */
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

        /* Add current user to some user's follower list */
        ParseQuery<ParseUser> haveFollowerUserQuery = ParseQuery.getQuery("Followers");
        haveFollowerUserQuery.whereEqualTo("username", localUser);
        ParseUser finalLocalUser = localUser;
        haveFollowerUserQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    /* Add current user to some user's follower list
                    * if it is empty */
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
                } else if (e != null) {
                    Log.e("Find User in Followers", "Failed Find: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        /* Add current user to some user's follower list */
        ParseQuery<ParseObject> haveFollowerObjectQuery = ParseQuery.getQuery("Followers");
        haveFollowerObjectQuery.whereEqualTo("username", localUser);
        dialog.show();
        haveFollowerObjectQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
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

        /* Update amount of "Following for" someone */
        usersQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    isFollowingCountTextView = findViewById(R.id.isFollowingCountTextView);
                    ParseUser foundUser = objects.get(0);
                    List isFollowing = foundUser.getList("isFollowing");
                    isFollowingCountTextView.setText(isFollowing.size() + "");

                    /* Possibility to watch "Following for" */
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

        /* Getting specific user from opened profile */
        ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
        userParseQuery.whereEqualTo("username", username);
        userParseQuery.setLimit(1);
        ParseUser localUser = null;
        try {
            localUser = userParseQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /* Update amount of readers */
        ParseQuery<ParseObject> haveFollowersQuery = ParseQuery.getQuery("Followers");
        haveFollowersQuery.whereEqualTo("username", localUser);
        haveFollowersQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    ParseObject foundUsersFollowers = objects.get(0);
                    haveFollowersCountTextView = findViewById(R.id.haveFollowersCountTextView);
                    List haveFollowers = foundUsersFollowers.getList("haveFollowers");
                    haveFollowersCountTextView.setText(haveFollowers.size() + "");

                    /* Possibility to view readers */
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