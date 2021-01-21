package in.ashprog.unitednotes;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class PostsCustomAdapter extends RecyclerView.Adapter<PostsCustomAdapter.PostsViewHolder> {

    final File path;
    Context context;
    ArrayList<ParseObject> posts;
    OnBottomReachedListener onBottomReachedListener;

    public PostsCustomAdapter(Context context, ArrayList<ParseObject> posts) {
        this.context = context;
        this.posts = posts;
        path = new File(Environment.getExternalStorageDirectory(), "United Notes");
    }

    public void setOnBottomReachedListener(OnBottomReachedListener onBottomReachedListener) {
        this.onBottomReachedListener = onBottomReachedListener;
    }

    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.posts_list_view, parent, false);
        return new PostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostsViewHolder holder, final int position) {

        if (position == posts.size() - 1) {
            onBottomReachedListener.onBottomReached();
        } else {

        }

        final String topic = posts.get(position).get("topic").toString(), authorName = posts.get(position).get("authorName").toString();
        holder.topicTextView.setText(topic);
        holder.downloadsTextView.setText("Downloads: " + posts.get(position).getNumber("downloads").toString());
        holder.authorTextView.setText("by " + authorName);

        String fileType = posts.get(position).get("fileType").toString();
        switch (fileType) {
            case "doc":
            case "docx":
                holder.fileTypeImage.setImageResource(R.drawable.doc_100);
                break;
            case "ppt":
            case "pptx":
                holder.fileTypeImage.setImageResource(R.drawable.ppt_100);
                break;
            case "pdf":
                holder.fileTypeImage.setImageResource(R.drawable.pdf_100);
                break;
        }

        final String fileName = topic + " by " + authorName + " (" + posts.get(position).getObjectId() + ")" + "."
                + posts.get(position).get("fileType");
        final File downloadedFile = new File(path, fileName);
        if (downloadedFile.exists())
            holder.downloadImage.setImageResource(R.drawable.checkmark_50);
        else
            holder.downloadImage.setImageResource(R.drawable.download_50);

        holder.downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    if (!path.exists())
                        path.mkdirs();

                    if (!downloadedFile.exists()) {
                        final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setCancelable(false);
                        progressDialog.setTitle("Downloading file...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setMax(100);
                        progressDialog.show();
                        final ParseFile file = posts.get(position).getParseFile("file");
                        file.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null && data != null) {
                                    try {
                                        FileOutputStream fos = new FileOutputStream(downloadedFile.getPath());
                                        fos.write(data);
                                        fos.close();
                                        Snackbar.make(holder.itemView, "File stored at " + path.toString() + "/" + fileName, BaseTransientBottomBar.LENGTH_LONG).show();
                                        holder.downloadImage.setImageResource(R.drawable.checkmark_50);
                                        posts.get(position).increment("downloads");
                                        posts.get(position).saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException pe) {
                                                if (pe == null)
                                                    holder.downloadsTextView.setText("Downloads: " + posts.get(position).get("downloads").toString());
                                            }
                                        });
                                    } catch (Exception ex) {
                                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
                                } else {
                                    Snackbar.make(holder.itemView, "Unable to download file.", BaseTransientBottomBar.LENGTH_SHORT).show();
                                }
                            }
                        }, new ProgressCallback() {
                            @Override
                            public void done(Integer percentDone) {
                                progressDialog.setProgress(percentDone);
                            }
                        });
                    } else {
                        Snackbar.make(holder.itemView, "File already exists.", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostsActivity.class);
                intent.putExtra("postId", posts.get(position).getObjectId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder {

        ImageView fileTypeImage, downloadImage;
        TextView topicTextView, authorTextView, downloadsTextView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            fileTypeImage = itemView.findViewById(R.id.fileTypeImage);
            downloadImage = itemView.findViewById(R.id.downloadImage);
            topicTextView = itemView.findViewById(R.id.topicTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            downloadsTextView = itemView.findViewById(R.id.downloadsTextView);
        }
    }
}
