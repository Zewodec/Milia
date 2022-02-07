package com.dmtfc.milia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * The class which load comments on PhotoShowActivity
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class RecyclerLoaderItems extends RecyclerView.Adapter<RecyclerLoaderItems.ViewClass> {
    private final List<String> username;
    private List<String> comment;
    private Context context;

    public RecyclerLoaderItems(List<String> username, List<String> comment, Context context) {
        this.username = username;
        this.comment = comment;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments, parent, false);
        return new ViewClass(view);
    }

    /**
     * Setting info like username, comments, images in right place
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewClass holder, int position) {
        /* Sets username and comment */
        holder.username.setText(username.get(position));
        holder.comment.setText(comment.get(position));

        /* Sets ava for them */
        ParseQuery<ParseUser> imageQuery = ParseUser.getQuery();
        imageQuery.whereEqualTo("username", username.get(position));
        try {
            ParseObject user = imageQuery.getFirst();
            if (user != null) {
                ParseFile file = (ParseFile) user.get("ava");
                if (file != null) {
                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            if (e == null && data != null) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                holder.ava.setImageBitmap(bitmap);
                            }
                        }
                    });
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return username.size();
    }


    public class ViewClass extends RecyclerView.ViewHolder {
        TextView username;
        TextView comment;
        ImageView ava;

        public ViewClass(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.usernameCommentTextView);
            comment = itemView.findViewById(R.id.commentTextView);
            ava = itemView.findViewById(R.id.AvaImageComment);
        }
    }

}
