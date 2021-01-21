package in.ashprog.unitednotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

public class MyParseGoogleUtils {
    private static final String AUTH_TYPE = "google";
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 62987;
    private static String clientId = null;
    private static GoogleSignInClient googleSignInClient = null;
    private static boolean isInitialized = false;
    private static LogInCallback currentCallback = null;

    public static void initialize(String clientId) {
        MyParseGoogleUtils.clientId = clientId;
        MyParseGoogleUtils.isInitialized = true;
    }

    public static void logIn(Activity activity, LogInCallback logInCallback) {
        MyParseGoogleUtils.currentCallback = logInCallback;
        MyParseGoogleUtils.googleSignInClient = MyParseGoogleUtils.buildGoogleSignInClient(activity);
        activity.startActivityForResult(googleSignInClient.getSignInIntent(), MyParseGoogleUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private static GoogleSignInClient buildGoogleSignInClient(Context context) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder()
                .requestId()
                .requestEmail()
                .requestProfile()
                .requestIdToken(MyParseGoogleUtils.clientId)
                .build();
        return GoogleSignIn.getClient(context, signInOptions);
    }

    public static boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_GOOGLE_SIGN_IN) {
            return false;
        }
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            if (data != null) {
                MyParseGoogleUtils.handleSignInResult(data);
            } else {
                MyParseGoogleUtils.onSignInCallbackResult(null, null);
            }
        }
        return true;
    }

    private static void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        onSignedIn(googleSignInAccount);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onSignInCallbackResult(null, e);
                    }
                });
    }

    private static void onSignedIn(GoogleSignInAccount account) {
        HashMap<String, String> authData = (HashMap<String, String>) getAuthData(account);
        ParseUser.logInWithInBackground(MyParseGoogleUtils.AUTH_TYPE, authData)
                .continueWith(new Continuation<ParseUser, Void>() {
                    @Override
                    public Void then(Task<ParseUser> task) throws Exception {
                        if (task.isCompleted()) {
                            MyParseGoogleUtils.onSignInCallbackResult(task.getResult(), null);
                        } else if (task.isFaulted()) {
                            MyParseGoogleUtils.onSignInCallbackResult(null, task.getError());
                        } else {
                            MyParseGoogleUtils.onSignInCallbackResult(null, null);
                        }
                        return null;
                    }
                });
    }

    private static void onSignInCallbackResult(ParseUser user, Exception e) {
        ParseException parseException;
        if (e == null)
            parseException = null;
        else if (e instanceof ParseException)
            parseException = (ParseException) e;
        else
            parseException = new ParseException(e);
        MyParseGoogleUtils.currentCallback.done(user, parseException);
    }

    private static Map<String, String> getAuthData(GoogleSignInAccount account) {
        HashMap<String, String> authData = new HashMap<>();
        authData.put("id", account.getId());
        authData.put("id_token", account.getIdToken());
        authData.put("email", account.getEmail());
        return authData;
    }

    public static void logOut() {
        MyParseGoogleUtils.googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
            }
        });
    }
}
