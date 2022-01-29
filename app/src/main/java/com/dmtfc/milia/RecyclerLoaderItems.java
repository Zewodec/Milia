package com.dmtfc.milia;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class RecyclerLoaderItems extends RecyclerView.Adapter<RecyclerLoaderItems.ViewClass> {
    private List<String> username;
    private List<String> comment;
    //    private List<Bitmap> avaImage;
    private Context context;

    public RecyclerLoaderItems(List<String> username, List<String> comment, Context context) {
        this.username = username;
        this.comment = comment;
//        this.avaImage = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments, parent, false);
        return new ViewClass(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewClass holder, int position) {
        holder.username.setText(username.get(position));
        holder.comment.setText(comment.get(position));

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
//        holder.ava.setImageBitmap(avaImage.get(position));
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(context, "Пункт вибраний", Toast.LENGTH_SHORT).show();
//            }
//        });

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
