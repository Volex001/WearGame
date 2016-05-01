package com.example.jverb.androidweartest;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.wearable.DataApi.DataListener;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener, ConnectionCallbacks, DataListener {
    private final String TAG = "MyApp";
    private GoogleApiClient mGoogleApiClient;
    private Button btnConnect;
    public static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mGoogleApiClient.connect();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

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
