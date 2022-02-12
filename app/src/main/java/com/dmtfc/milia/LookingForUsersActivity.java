package com.dmtfc.milia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class LookingForUsersActivity extends AppCompatActivity {

    private EditText LookingForUserByUsername;
    private ListView FoundUserList;
    private Button FindUsersByUsernameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looking_for_users);

        LookingForUserByUsername = findViewById(R.id.lookingForUserField);
        FoundUserList = findViewById(R.id.foundUserList);
        FindUsersByUsernameButton = findViewById(R.id.findUsersByUsernameButton);

        ArrayList<String> foundUsernames = new ArrayList<>();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, foundUsernames);

        FindUsersByUsernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String LookedUsername = LookingForUserByUsername.getText().toString();

                foundUsernames.clear();

                ParseQuery<ParseUser> LookedUsernameQuery = ParseUser.getQuery();
                LookedUsernameQuery.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
                LookedUsernameQuery.whereStartsWith("username", LookedUsername);

                ProgressDialog dialog = new ProgressDialog(LookingForUsersActivity.this);
                dialog.show();

                LookedUsernameQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        dialog.dismiss();
                        if (e == null) {
                            if (objects.size() > 0) {
                                for (ParseUser user : objects) {
                                    foundUsernames.add(user.getUsername());
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
                FoundUserList.setAdapter(adapter);

                /* If some user CLICKED than opens its profile */
                FoundUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(getApplicationContext(), UserFeedActivity.class);
                        intent.putExtra("username", foundUsernames.get(i));
                        startActivity(intent);
                    }
                });

                Toast.makeText(LookingForUsersActivity.this, "Успішний пошук", Toast.LENGTH_SHORT).show();
            }
        });
    }
}