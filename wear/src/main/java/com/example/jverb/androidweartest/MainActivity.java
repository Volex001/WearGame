package com.example.jverb.androidweartest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends WearableActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Button btnTest;
    private GoogleApiClient mGoogleApiClient;
    private static final long CONNECTION_TIME_OUT_MS = 100;
    private String nodeId;
    private Node mNode;
    public static final String TAG = "WearApp";

    private boolean ready = false;
    float mGravity[] = new float[3];
    float mGeomagnetic[] = new float[3];

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetomer;
    private Sensor gyroscope;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetomer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Log.d(TAG, "testing - 1");
        /*SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float azimuth_angle = sensorEvent.values[0];
                float pitch_angle = sensorEvent.values[1];
                float roll_angle = sensorEvent.values[2];
                Log.d("azimuth_angle", Float.toString(azimuth_angle));
                Log.d("pitch_angle", Float.toString(pitch_angle));
                Log.d("roll_angle", Float.toString(roll_angle));


                if (mNode != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, mNode.getId(), Float.toString(azimuth_angle), null
                    );
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };*/
        Log.d(TAG, "testing - 2");

        Log.d(TAG, "testing - 2.5");
        btnTest = (Button) findViewById(R.id.btn_test);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNode != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Log.d(TAG, "-- " + mGoogleApiClient.isConnected());
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, mNode.getId(), "boodschap", null
                    ).setResultCallback(
                            new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                                    if (!sendMessageResult.getStatus().isSuccess()) {
                                        Log.d(TAG, "Failed to send message");
                                    }
                                }
                            }
                    );
                }
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d(TAG, "resumed");
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, magnetomer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "testing - 3");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*float azimuth_angle = event.values[0];
        float pitch_angle = event.values[1];
        float roll_angle = event.values[2];
        Log.d("azimuth_angle", Float.toString(azimuth_angle));
        Log.d("pitch_angle", Float.toString(pitch_angle));
        Log.d("roll_angle", Float.toString(roll_angle));


        if (mNode != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), Float.toString(azimuth_angle), null
            );
        }*/

        if (mNode == null)
            return;
        if (mNode.getId() == null)
            return;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeomagnetic = event.values.clone();
                ready = true;
                break;
        }
        if (mGravity != null && mGeomagnetic != null && ready) {
            ready = false;
            float matrixR[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(matrixR, null, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(matrixR, orientation);
                sendOrientation(mNode.getId(), orientation[0], orientation[1], orientation[2]);
            } else {
                Log.e(TAG, "Couldn't get rotation matrix");
            }
        }
    }

    private void sendOrientation(String node, final float azimuth, final float pitch, final float roll) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        byteBuffer.putFloat(azimuth);
        byteBuffer.putFloat(pitch);
        byteBuffer.putFloat(roll);
        final byte[] data = byteBuffer.array();
        Wearable.MessageApi.sendMessage(mGoogleApiClient, node,
                "WEAR_ORIENTATION", data).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Couldn't send message: " + sendMessageResult);
                }
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        //mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
