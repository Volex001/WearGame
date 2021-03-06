package com.joostverbraeken.weargame;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by jverb on 4/28/2016.
 */
public class WearListCallListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (MainActivity.instance != null)
            MainActivity.instance.onMessageReceived(messageEvent);
    }
}
