package com.dmtfc.milia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
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

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

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

//                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(
//                                            ViewGroup.LayoutParams.MATCH_PARENT,
//                                            ViewGroup.LayoutParams.WRAP_CONTENT
//                                    ));

                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(photoGridLayout.getWidth() /3,400));
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
}