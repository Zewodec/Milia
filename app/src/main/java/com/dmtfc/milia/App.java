package com.dmtfc.milia;

import android.app.Application;

import com.onesignal.OneSignal;
import com.parse.Parse;
import com.parse.ParseACL;

/**
 * Class where setup begins.
 *
 * @author Adam Ivaniush
 * @version 0.1.0
 */
public class App extends Application {

    /* ID for registering app in ONESIGNAL for push messages */
    private static final String ONESIGNAL_APP_ID = String.valueOf(R.string.ONESIGNAL_APP_ID);

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(getString(R.string.back4app_app_id))
                .clientKey(getString(R.string.back4app_client_key))
                .server(getString(R.string.back4app_server_url))
                .build());

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }
}
