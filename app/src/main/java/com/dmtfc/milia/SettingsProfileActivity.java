package com.dmtfc.milia;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

public class SettingsProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        setTitle("Налаштування");

        Button changeProfileImage = findViewById(R.id.changeProfileImage);

        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProfileImage();
            }
        });

        loadUserProfileImage();

    }

    private void loadUserProfileImage() {
        ParseFile file = (ParseFile) ParseUser.getCurrentUser().get("ava");
        if (file != null) {
            file.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    ImageView profileImageSettings = findViewById(R.id.profileImageSettings);

                    profileImageSettings.setImageBitmap(bitmap);

                }
            });
        }
    }

    private void CheckExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void changeProfileImage() {
        CheckExternalStoragePermission();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoSettingsResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> photoSettingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();

                        //Code GO Brrrr
                        if (data != null) {
                            Uri selectedImage = data.getData();
                            try {
                                Log.i("Image-Settings", "Image Selected!");

                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);
                                byte[] byteArray = stream.toByteArray();
                                ParseFile file = new ParseFile(ParseUser.getCurrentUser().getUsername() + "-ava.png", byteArray);
                                ParseUser currentUser = ParseUser.getCurrentUser();
                                currentUser.put("ava", file);
                                currentUser.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Toast.makeText(getApplicationContext(), "Ваша світлина була обновлена", Toast.LENGTH_SHORT).show();
                                            Log.i("Photo Ava Update", "Success photo update for " + currentUser.getUsername());
                                            loadUserProfileImage();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Оновлення світлини не успішне: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e("Photo Ava Update", "Error with updating photo for " + currentUser.getUsername());
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
}