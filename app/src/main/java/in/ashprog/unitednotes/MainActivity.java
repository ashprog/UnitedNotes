package in.ashprog.unitednotes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.facebook.ParseFacebookUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    static ParseUser currentUser;
    CardView logoCardView;
    ProgressDialog progressDialog;

    public static void printHashKey(Context pContext) {
        try {
            PackageInfo info = pContext.getPackageManager().getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("HashKey", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("HashKey", "printHashKey()", e);
        } catch (Exception e) {
            Log.e("HashKey", "printHashKey()", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        printHashKey(this);

        currentUser = ParseUser.getCurrentUser();

        logoCardView = findViewById(R.id.logoCardView);

        logoCardView.setAlpha(0f);
        logoCardView.animate().alphaBy(1f).setDuration(1000).start();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(100);
        progressDialog.setCancelable(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentUser == null) {
                    logoCardView.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addFragment(new StartFragment());
                        }
                    }, 1000);
                } else {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }
            }
        }, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FacebookSdk.getCallbackRequestCodeOffset())
            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 62987)
            MyParseGoogleUtils.onActivityResult(requestCode, resultCode, data);
    }

    void updateUser(final ParseUser user, Uri profilePicUri) {
        Picasso.with(getApplicationContext()).load(profilePicUri).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] data = stream.toByteArray();
                ParseFile file = new ParseFile("image.png", data);
                try {
                    file.save();
                    user.put("image", file);
                    user.save();
                    progressDialog.dismiss();
                    currentUser = user;
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                } catch (Exception e) {
                    try {
                        user.delete();
                    } catch (ParseException exc) {
                        exc.printStackTrace();
                    }
                    ParseUser.logOut();
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

    public void continueWithGoogle(View v) {
        progressDialog.show();

        MyParseGoogleUtils.logIn(this, new LogInCallback() {
            @Override
            public void done(final ParseUser user, final ParseException e) {
                if (e == null && user != null) {
                    if (user.isNew()) {
                        try {
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                            user.setEmail(acct.getEmail());
                            user.put("name", acct.getDisplayName());
                            final Uri profilePicUri = acct.getPhotoUrl();
                            MyParseGoogleUtils.logOut();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateUser(user, profilePicUri);
                                }
                            });
                        } catch (final Exception ex) {
                            MyParseGoogleUtils.logOut();
                            try {
                                user.delete();
                            } catch (ParseException exc) {
                                exc.printStackTrace();
                            }
                            ParseUser.logOut();
                            progressDialog.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        MyParseGoogleUtils.logOut();
                        progressDialog.dismiss();
                        currentUser = user;
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    }
                } else {
                    MyParseGoogleUtils.logOut();
                    ParseUser.logOut();
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void continueWithFb(View v) {
        progressDialog.show();

        Collection<String> permissions = Arrays.asList("public_profile", "email");
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, permissions, new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                if (err == null && user != null) {
                    if (user.isNew()) {
                        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    user.setEmail(object.getString("email"));
                                    user.put("name", object.getString("name"));

                                    final String profilePicUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                    updateUser(user, Uri.parse(profilePicUrl));
                                } catch (Exception e) {
                                    try {
                                        user.delete();
                                    } catch (ParseException exc) {
                                        exc.printStackTrace();
                                    }
                                    ParseUser.logOut();
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,picture.type(large)");
                        request.setParameters(parameters);
                        request.executeAsync();

                    } else {
                        progressDialog.dismiss();
                        currentUser = user;
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    }
                } else {
                    ParseUser.logOut();
                    progressDialog.dismiss();
                }
            }
        });
    }

    void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.start_fragment_enter, R.anim.start_fragment_enter);
        transaction.add(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

}
