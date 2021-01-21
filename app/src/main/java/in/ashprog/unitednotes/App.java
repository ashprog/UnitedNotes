package in.ashprog.unitednotes;

import android.app.Application;

import com.parse.Parse;
import com.parse.facebook.ParseFacebookUtils;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("YBxDofWbNoNGvMQHt7sVqI3T9cC0JwGkvNjDGV5x")
                .clientKey("ds49kOjxxzflXwnmUZf8mAF7sOTLS7fDmAC3tA8g")
                .server("https://parseapi.back4app.com/")
                .build()
        );
        ParseFacebookUtils.initialize(this);
        MyParseGoogleUtils.initialize("442233173424-p2nrooqp8tpdi5rgsou8j4resico5lkc.apps.googleusercontent.com");

        CustomPushBroadcastReceiver.createNotificationChannel(this);
        CustomPushBroadcastReceiver.initialize();
    }
}
