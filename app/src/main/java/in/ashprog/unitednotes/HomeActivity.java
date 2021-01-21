package in.ashprog.unitednotes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class HomeActivity extends AppCompatActivity {

    static String name;
    static FloatingActionButton fab;
    ParseUser currentUser;
    Bitmap profileBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fab = findViewById(R.id.floatingActionButton);

        currentUser = ParseUser.getCurrentUser();
        initializePush();
        getProfile();

        addFragment(new HomeFragment());
    }

    void getProfile() {
        name = currentUser.get("name").toString();
        ParseFile file = currentUser.getParseFile("image");
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                profileBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                ImageView personImageView = findViewById(R.id.personImageView);
                personImageView.setImageBitmap(RescaleImage.getRoundedResizedBitmap(profileBitmap, 60));
            }
        });
    }

    void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    public void fab(View v) {
        startActivity(new Intent(this, UploadActivity.class));
    }

    void initializePush() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (!installation.has("user") || installation.getParseUser("user").getObjectId().equals(currentUser.getObjectId())) {
            installation.put("GCMSenderId", "442233173424");
            installation.put("user", currentUser);
            installation.saveInBackground();
        }
    }
}
