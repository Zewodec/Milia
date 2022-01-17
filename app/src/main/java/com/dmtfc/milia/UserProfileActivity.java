package com.dmtfc.milia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.gridlayout.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
                                            photoGridLayout.getWidth() /3,
                                            400));
                                    imageView.setScaleType(ImageView.ScaleType.CENTER);

                                    imageView.setImageBitmap(bitmap);

                                    photoGridLayout.addView(imageView);
                                }
                            }
                        });
                    }
                }
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

        switch (item.getItemId()){
            case R.id.settingsProfileMenu:
                Intent intent = new Intent(this, SettingsProfileActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}