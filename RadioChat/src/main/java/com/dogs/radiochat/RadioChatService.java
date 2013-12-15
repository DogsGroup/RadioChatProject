package com.dogs.radiochat;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by XRPQ48 on 12/8/13.
 */
public class RadioChatService extends IntentService {
    public RadioChatService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
