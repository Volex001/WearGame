package com.example.jverb.androidweartest;

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;

/**
 * Created by jverb on 4/28/2016.
 */
public class WearListCallListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //super.onMessageReceived(messageEvent);
        byte[] payload = messageEvent.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
        float azimuth = byteBuffer.getFloat();
        float pitch = byteBuffer.getFloat();
        float roll = byteBuffer.getFloat();

        String message = azimuth + " " + pitch + " " + roll;

        Log.d("resultaat:", message);
    }
}
