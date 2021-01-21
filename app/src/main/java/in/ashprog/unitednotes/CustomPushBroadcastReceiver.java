package in.ashprog.unitednotes;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class CustomPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    static ArrayList<String> postIds;
    static ArrayList<ParseObject> posts;

    public static void initialize() {
        postIds = new ArrayList<>();
        posts = new ArrayList<>();
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Notes", "New Notes Notifications", importance);
            channel.setDescription("Get notifications for new notes.");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        onPushReceive(context, intent);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        if ("DOWNLOAD_ACTION".equals(intent.getAction())) {
            downloadFile(context, intent);
        } else {
            showNotification(context, intent);
        }
    }

    void showNotification(Context context, Intent intent) {
        JSONObject data = getPushData(intent);
        try {
            String postId = data.getString("postId");
            ParseObject post = getPost(postId);

            if (!postIds.contains(postId)) {
                postIds.add(postId);
                posts.add(post);
            }

            if (post != null) {
                String topic = post.getString("topic"), authorName = post.getString("authorName");
                Bitmap largeIcon = getLargeIcon(post.getParseUser("author"));

                Intent in = new Intent(context, PostsActivity.class);
                in.putExtra("postId", postId);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, postIds.indexOf(postId), in, 0);

                Intent downloadIntent = new Intent(context, CustomPushBroadcastReceiver.class);
                downloadIntent.setAction("DOWNLOAD_ACTION");
                downloadIntent.putExtra("id", postIds.indexOf(postId));
                PendingIntent downloadPI = PendingIntent.getBroadcast(context, postIds.indexOf(postId), downloadIntent, 0);

                NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "Notes")
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(authorName + " just uploaded a new note.")
                        .setContentText(topic)
                        .setLargeIcon(largeIcon)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setColor(Color.parseColor("#505662"));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    notification.addAction(R.drawable.download_50, "Download Now", downloadPI);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
                notificationManagerCompat.notify(context.getPackageName(), postIds.indexOf(postId), notification.build());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            super.onPushReceive(context, intent);
        }
    }

    void downloadFile(final Context context, final Intent intent) {
        final int position = intent.getIntExtra("id", -1);
        if (position != -1) {

            final String topic = posts.get(position).getString("topic");
            final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "Notes")
                    .setColor(Color.parseColor("#505662"))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Downloading file...")
                    .setContentText(topic)
                    .setLargeIcon(getLargeIcon(posts.get(position).getParseUser("author")))
                    .setAutoCancel(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setOnlyAlertOnce(true);

            final ParseFile file = posts.get(position).getParseFile("file");
            file.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    try {
                        File downloadedFile = writeFile(data, position);

                        posts.get(position).increment("downloads");
                        posts.get(position).saveInBackground();

                        Intent openFileIntent = new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(Uri.parse("content://" + downloadedFile.getPath()), "*/*")
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        PendingIntent openFilePI = PendingIntent.getActivity(context, position, openFileIntent, 0);

                        notification.setContentTitle("File downloaded.")
                                .setContentText("Stored at " + Environment.getExternalStorageDirectory() + "/United Notes/")
                                .setProgress(0, 0, false)
                                .setContentIntent(openFilePI)
                                .setAutoCancel(true);
                    } catch (Exception ex) {
                        notification.setContentTitle("Unable to download file.")
                                .setContentText(ex.getLocalizedMessage())
                                .setProgress(0, 0, false);
                        Toast.makeText(context, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        ex.printStackTrace();
                    }
                    notificationManagerCompat.cancel(context.getPackageName(), position);
                    notificationManagerCompat.notify(context.getPackageName(), position, notification.build());
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer percentDone) {
                    notification.setProgress(100, percentDone, false);
                    notificationManagerCompat.notify(context.getPackageName(), position, notification.build());
                }
            });
        }
    }

    File writeFile(byte[] data, int position) throws Exception {
        File downloadedFile = null;
        if (position != -1) {
            String fileName = posts.get(position).getString("topic") + " by " + posts.get(position).getString("authorName") + " ("
                    + posts.get(position).getObjectId() + ")" + "."
                    + posts.get(position).get("fileType");
            File path = new File(Environment.getExternalStorageDirectory(), "United Notes");
            downloadedFile = new File(path, fileName);

            if (!path.exists())
                path.mkdirs();

            FileOutputStream fos = new FileOutputStream(downloadedFile.getPath());
            fos.write(data);
            fos.close();
        }
        return downloadedFile;
    }

    ParseObject getPost(String postId) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Posts");
        query.include("author");
        try {
            return query.get(postId);
        } catch (Exception e) {
            return null;
        }
    }

    Bitmap getLargeIcon(ParseUser user) {
        Bitmap largeIcon = null;
        ParseFile file = user.getParseFile("image");
        try {
            byte[] data = file.getData();
            largeIcon = RescaleImage.getRoundedResizedBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 50);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return largeIcon;
    }
}
