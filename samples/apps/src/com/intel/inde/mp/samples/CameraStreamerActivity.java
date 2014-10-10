//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.intel.inde.mp.CameraCapture;
import com.intel.inde.mp.IProgressListener;
import com.intel.inde.mp.StreamingParameters;
import com.intel.inde.mp.android.AndroidMediaObjectFactory;
import com.intel.inde.mp.android.AudioFormatAndroid;
import com.intel.inde.mp.android.VideoFormatAndroid;

import java.util.List;

public class CameraStreamerActivity extends ActivityWithTimeline {

    ToggleButton buttonStart;
    private TableLayout table;

    Camera camera = null;
    CameraCapture capture = null;

    private AudioFormatAndroid audioFormat;
    private VideoFormatAndroid videoFormat;
    private boolean inProgress = false;
    private boolean isActive = false;

    public IProgressListener progressListener = new IProgressListener() {
        @Override
        public void onMediaStart() {
        }

        @Override
        public void onMediaProgress(float progress) {
        }

        @Override
        public void onMediaDone() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        inProgress = false;
                        if (!isActive) {
                            return;
                        }
                        onStreamingDone();
                    }
                });
            } catch (Exception ex) {
            }
        }

        @Override
        public void onMediaPause() {
        }

        @Override
        public void onMediaStop() {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        inProgress = false;
                    }
                });
            } catch (Exception ex) {
            }
        }

        @Override
        public void onError(Exception exception) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        inProgress = false;
                        if (!isActive) {
                            return;
                        }
                        onStreamingDone();
                    }
                });
            } catch (Exception ex) {
            }
        }
    };


    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.camera_streamer_activity);

        setupUI();

        camera = createCamera();
        capture = new CameraCapture(new AndroidMediaObjectFactory(getApplicationContext()), progressListener);
        capture.createPreview(findViewById(R.id.surfaceView), camera);
    }

    @Override
    protected void onPause() {
        isActive = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        isActive = true;
        super.onResume();
    }

    @Override
    public void onDestroy() {
        stopStreaming();
        super.onDestroy();
    }

    protected void setupUI()
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        table = (TableLayout)inflater.inflate(R.layout.streaming_params, null);
        ((LinearLayout)findViewById(R.id.linearLayout)).addView(table, 0);
        findViewById(R.id.host).requestFocus(Gravity.END);

        buttonStart = (ToggleButton) findViewById(R.id.toggleButtonStart);
        buttonStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startStreaming();
                } else {
                    stopStreaming();
                }
            }
        });
    }

    private StreamingParameters prepareStreamingParams() {
        StreamingParameters parameters = new StreamingParameters();
        parameters.Host = ((EditText) findViewById(R.id.host)).getText().toString();
        parameters.Port = Integer.parseInt(((EditText) findViewById(R.id.port)).getText().toString());
        parameters.ApplicationName = ((EditText) findViewById(R.id.applicationName)).getText().toString();
        parameters.StreamName = ((EditText) findViewById(R.id.streamName)).getText().toString();

        parameters.isToPublishAudio = true;
        parameters.isToPublishVideo = true;

        return parameters;
    }

    private void configureMediaStreamFormat() {

        videoFormat = new VideoFormatAndroid("video/avc", 640, 480);
        videoFormat.setVideoBitRateInKBytes(1000);
        videoFormat.setVideoFrameRate(25);
        videoFormat.setVideoIFrameInterval(1);

        audioFormat = new AudioFormatAndroid("audio/mp4a-latm", 44100, 1);
    }

    private Camera createCamera() {
        Camera camera = Camera.open(); //default camera device

        Camera.Parameters cameraParams = camera.getParameters();
        List<Camera.Size> supportedResolutions = cameraParams.getSupportedPreviewSizes();

        Camera.Size maxCameraResolution = supportedResolutions.get(0);

        for (Camera.Size size : supportedResolutions) {

            if (maxCameraResolution.width < size.width) {
                maxCameraResolution = size;
            }
        }

        List<int[]> supportedPreviewFpsRange = cameraParams.getSupportedPreviewFpsRange();
        int maxFps1 = 1;
        int maxFps0 = 1;
        for (int[] fpsRange : supportedPreviewFpsRange) {
            if ((fpsRange[1] > maxFps1 && fpsRange[0] > maxFps0) ||
                    (fpsRange[1] > maxFps1 && fpsRange[0] == maxFps0) ||
                    (fpsRange[1] == maxFps1 && fpsRange[0] > maxFps0)){
                maxFps0 = fpsRange[0];
                maxFps1 = fpsRange[1];
            }
        }
        cameraParams = camera.getParameters();
        cameraParams.setPreviewSize(maxCameraResolution.width, maxCameraResolution.height);
        cameraParams.setPreviewFpsRange(maxFps0, maxFps1);
        camera.setParameters(cameraParams);

        return camera;
    }

    public void startStreaming() {

        updateUI(true);

        configureMediaStreamFormat();

        capture.setTargetVideoFormat(videoFormat);
        capture.setTargetAudioFormat(audioFormat);
        capture.setTargetConnection(prepareStreamingParams());
        capture.start();

        inProgress = true;
    }

    public void stopStreaming() {
        updateUI(false);

        if(inProgress && capture != null) {
            capture.stop();
        }
        if (camera != null) {
            camera.release();
        }
        inProgress = false;
    }

    private void updateUI(boolean inProgress) {

        if(inProgress) {
            table.setVisibility(View.INVISIBLE);
        } else {
            buttonStart.setVisibility(View.INVISIBLE);
        }
    }

    public void onStreamingDone() {

        updateUI(false);

        showMessageBox("Streaming finished.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
    }
}
