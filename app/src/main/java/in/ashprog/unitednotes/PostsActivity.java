package in.ashprog.unitednotes;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostsActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener {

    CommentsFragment commentsFragment;

    SlidingUpPanelLayout slidingUpPanelLayout;
    TextView seeCommentsTV, topicTV, descriptionTV, authorTV, emailTV, downloadsTV;
    ImageView fileTypeIV, authorProfileIV;
    FloatingActionButton downloadFAB;

    String postId, fileName;
    File path, downloadedFile;

    ParseObject currentPost;
    ParseUser author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.addPanelSlideListener(this);
        seeCommentsTV = findViewById(R.id.seeCommentsTV);
        fileTypeIV = findViewById(R.id.fileTypeIV);
        authorProfileIV = findViewById(R.id.authorProfileIV);
        topicTV = findViewById(R.id.topicTV);
        descriptionTV = findViewById(R.id.descriptionTV);
        authorTV = findViewById(R.id.authorTV);
        emailTV = findViewById(R.id.emailTV);
        downloadsTV = findViewById(R.id.downloadsTV);
        downloadFAB = findViewById(R.id.downloadFAB);

        path = new File(Environment.getExternalStorageDirectory(), "United Notes");

        downloadFAB.hide();
        postId = getIntent().getStringExtra("postId");
        getPost();

        commentsFragment = (CommentsFragment) getSupportFragmentManager().findFragmentById(R.id.commentsFragmentContainer);
        commentsFragment.setPostId(postId);
    }

    void getPost() {
        if (postId != null && postId.length() > 0) {
            ParseQuery query = ParseQuery.getQuery("Posts");
            query.include("author");
            try {
                currentPost = query.get(postId);
                author = currentPost.getParseUser("author");
                updateProfile();
                updateUI();
            } catch (Exception e) {
                Log.i("MyApp", e.getMessage());
            }
        }
    }

    void updateUI() {
        if (currentPost != null) {
            String topic = currentPost.get("topic").toString(),
                    authorName = currentPost.get("authorName").toString();
            topicTV.setText(topic);
            descriptionTV.setText(currentPost.get("description").toString());
            authorTV.setText(authorName);
            emailTV.setText(author.getString("email"));
            downloadsTV.setText("Downloads: " + currentPost.get("downloads").toString());

            switch (currentPost.get("fileType").toString()) {
                case "doc":
                case "docx":
                    fileTypeIV.setImageResource(R.drawable.doc_100);
                    break;
                case "ppt":
                case "pptx":
                    fileTypeIV.setImageResource(R.drawable.ppt_100);
                    break;
                case "pdf":
                    fileTypeIV.setImageResource(R.drawable.pdf_100);
                    break;
            }

            fileName = topic + " by " + authorName + " (" + currentPost.getObjectId() + ")" + "."
                    + currentPost.get("fileType");
            downloadedFile = new File(path, fileName);
            downloadFAB.show();

            if (downloadedFile.exists())
                downloadFAB.setImageResource(R.drawable.checkmark_50);
            else
                downloadFAB.setImageResource(R.drawable.download_50);
        }
    }

    void updateProfile() {
        ParseFile file = author.getParseFile("image");
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (e == null && data != null) {
                    Bitmap bitmap = RescaleImage.getRoundedResizedBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 50);
                    authorProfileIV.setImageBitmap(bitmap);
                }
            }
        });
    }

    public void download(final View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            if (!path.exists())
                path.mkdirs();

            if (!downloadedFile.exists()) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        doInBackground();
                    }
                });
            } else {
                Snackbar.make(view, "File already exists.", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    void doInBackground() {
        final String path = downloadedFile.getPath();
        final ParseObject post = currentPost;
        final int id = Integer.parseInt(new SimpleDateFormat("ddHHmmssSS", Locale.getDefault()).format(new Date()));

        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Notes")
                .setAutoCancel(false)
                .setContentTitle("Downloading file...")
                .setContentText(post.getString("topic"))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setColor(Color.parseColor("#505662"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true);

        final ParseFile file = post.getParseFile("file");
        file.getDataInBackground(new GetDataCallback() {

            @Override
            public void done(byte[] data, ParseException e) {
                try {
                    FileOutputStream fos = new FileOutputStream(path);
                    fos.write(data);
                    fos.close();

                    post.increment("downloads");
                    post.save();

                    if (downloadsTV != null || downloadFAB != null) {
                        downloadsTV.setText("Downloads: " + post.get("downloads").toString());
                        downloadFAB.setImageResource(R.drawable.checkmark_50);
                    }

                    Intent openFileIntent = new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(Uri.parse("content://" + path), "*/*")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    final PendingIntent openFilePI = PendingIntent.getActivity(getApplicationContext(), id, openFileIntent, 0);

                    builder.setContentTitle("File downloaded.")
                            .setContentText("Stored at " + Environment.getExternalStorageDirectory() + "/United Notes/")
                            .setProgress(0, 0, false)
                            .setContentIntent(openFilePI)
                            .setAutoCancel(true);
                } catch (final Exception ex) {
                    builder.setContentTitle("Unable to download file.")
                            .setContentText(ex.getLocalizedMessage())
                            .setProgress(0, 0, false);
                    Toast.makeText(getApplicationContext(), ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
                }
                notificationManagerCompat.cancel(getPackageName(), id);
                notificationManagerCompat.notify(getPackageName(), id, builder.build());
            }
        }, new ProgressCallback() {

            @Override
            public void done(Integer percentDone) {
                builder.setProgress(100, percentDone, false)
                        .setAutoCancel(false);
                notificationManagerCompat.notify(getPackageName(), id, builder.build());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                download(findViewById(R.id.downloadFAB));
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (slideOffset == 1f) {
            seeCommentsTV.setText("Comments");
            seeCommentsTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_down_black_24dp, 0, 0, 0);
        } else if (slideOffset == 0) {
            seeCommentsTV.setText("See Comments");
            seeCommentsTV.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_up_black_24dp, 0, 0, 0);
        }
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

    }
}