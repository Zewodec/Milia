package com.dmtfc.milia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FollowersListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_list);

        ListView followerList = findViewById(R.id.followerList);

        Intent intent = getIntent();
        String title = intent.getStringExtra("Name");
        String type = intent.getStringExtra("Type Followers");
        String username = intent.getStringExtra("username");

        setTitle(title);

        List followers = new ArrayList<>();

        switch (type) {
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
            case "haveFollowers":
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
                try {
                    ParseObject foundUsersFollowers = haveFollowersQuery.getFirst();
                    followers = foundUsersFollowers.getList("haveFollowers");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (followers.size() > 0) {
                    ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, followers);
                    followerList.setAdapter(adapter);
                }
                break;
        }

    }
}