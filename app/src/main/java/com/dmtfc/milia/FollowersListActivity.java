package com.dmtfc.milia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Show all user's in system.
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class FollowersListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_list);

        // Component for display this
        ListView followerList = findViewById(R.id.followerList);

        // Get current Intent with some extra parameters
        Intent intent = getIntent();
        String title = intent.getStringExtra("Name");           // Name of layout (Читачи or Підписники)
        String type = intent.getStringExtra("Type Followers");  // Type of followers (isFoolowing or haveFollowedrs)
        String username = intent.getStringExtra("username");    // Username of whom user opened activity

        // Set Activity name
        setTitle(title);

        // There will be followers who are following or followed by
        List followers = new ArrayList<>();

        switch (type) {
            // Show users followed by this profile
            case "isFollowing":
                ParseQuery<ParseUser> isFollowingUsers = ParseUser.getQuery();
                isFollowingUsers.whereEqualTo("username", username);
                try {
                    ParseUser isFollowingObject = isFollowingUsers.getFirst();
                    followers = isFollowingObject.getList("isFollowing");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            // Show users who following this profile
            case "haveFollowers":
                /* Getting user */
                ParseQuery<ParseUser> userParseQuery = ParseUser.getQuery();
                userParseQuery.whereEqualTo("username", username);
                userParseQuery.setLimit(1);
                ParseUser localUser = null;
                try {
                    localUser = userParseQuery.getFirst();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                /* Getting followers who following this account */
                ParseQuery<ParseObject> haveFollowersQuery = ParseQuery.getQuery("Followers");
                haveFollowersQuery.whereEqualTo("username", localUser);
                try {
                    ParseObject foundUsersFollowers = haveFollowersQuery.getFirst();
                    followers = foundUsersFollowers.getList("haveFollowers");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }

        // Show users
        if (followers.size() > 0) {
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, followers);
            followerList.setAdapter(adapter);
        }

    }
}