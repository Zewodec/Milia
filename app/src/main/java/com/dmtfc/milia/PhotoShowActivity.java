package com.dmtfc.milia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Show image in fullscreen from profile.
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class PhotoShowActivity extends AppCompatActivity {

    private ImageView photoShow;                            // Image for showing in center
    private TextInputEditText commentView;                  // Comment writing section
    private RecyclerView recyclerView;                      // Comment section

    private List<String> usernames = new ArrayList<>();     // Username list in comments
    private List<String> comments = new ArrayList<>();      // Their comments

    // TODO: If future make better comment system (may be live?)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_show);

        commentView = findViewById(R.id.CommentEditText);

        Intent intent = getIntent();
        String objectID = intent.getStringExtra("Image");       // Get ObjectID

        /* Loading image in activity */
        GetImage(objectID);

        /* Button which send comment */
        Button sendComment = findViewById(R.id.SendCommentButton);
        sendComment.setOnClickListener(view -> AddComment(objectID));

        /* Setting up comment section */
        recyclerView = findViewById(R.id.recyclerCommentView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        /* Getting all usernames and comments for comments view */
        usernames = getUsernames(objectID);
        comments = getComments(objectID);

        RecyclerLoaderItems recyclerLoaderItems = new RecyclerLoaderItems(usernames, comments, PhotoShowActivity.this);
        recyclerView.setAdapter(recyclerLoaderItems);
    }

    /**
     * Getting comments text from user.
     * @param objectID An object ID of open image
     * @return [String] List of comments text of users.
     */
    private List<String> getComments(String objectID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        ParseObject object = null;
        try {
            object = query.get(objectID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return object.getList("Comment");
    }

    /**
     * Gettings usernames from user in comments.
     * @param objectID Object ID of open image
     * @return [String] List of usernames names
     */
    private List<String> getUsernames(String objectID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        ParseObject object = null;
        try {
            object = query.get(objectID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return object.getList("WhoComment");
    }

    /**
     * Send user's comment to backend.
     * @param objectID Object ID of open image
     */
    private void AddComment(String objectID) {
        ParseQuery<ParseObject> commentsQuery = ParseQuery.getQuery("Image");
        commentsQuery.getInBackground(objectID, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    object.add("WhoComment", ParseUser.getCurrentUser().getUsername());
                    String userComment = getComment();
                    object.add("Comment", userComment);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(PhotoShowActivity.this, "Успішно доданий коментар", Toast.LENGTH_SHORT).show();
                                commentView.setText("");
                            } else {
                                Toast.makeText(PhotoShowActivity.this, "Не успішно доданий коментар", Toast.LENGTH_SHORT).show();
                                Log.e("Error add comment", "Unsuccessful add comment: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * Get comment from field where current user
     * do some input.
     * @return Text from comment filed for current user
     */
    private String getComment() {
        return commentView.getText().toString();
    }

    /**
     * Getting Image from objectID that gets from another intent
     */
    private void GetImage(String objectID) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        query.getInBackground(objectID, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    ParseFile file = (ParseFile) object.get("image");       // Get File Image from server

                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            if (e == null) {
                                /* Decode image to body */
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                photoShow = findViewById(R.id.PhotoShowImageView);

                                /* Set Image */
                                photoShow.setImageBitmap(bitmap);
                                fullscreen(); // Change fullscreen mode

                                photoShow.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        fullscreen();   // Change fullscreen mode
                                        finish();       // Close Activity
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fullscreen();
    }

    /**
     * Open image from profile in fullscreen.
     */
    private void fullscreen() {
        //FULL SCREEN MODE
        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("Immersive", "Turning immersive mode mode off. ");
        } else {
            Log.i("Immersive", "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }
}