//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.intel.inde.mp.IProgressListener;
import com.intel.inde.mp.samples.controls.GameGLSurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GameCapturing extends Activity {

    public static final int UPDATE_FPS = 1;

    public static final int ENABLE_BUTTON = 2;

    public static final int UPDATE_TIMER = 3;

    GameRenderer.RenderingMethod renderingMethod;

    private Spinner renderingMethodList;

    private GameGLSurfaceView surfaceView;

    protected GameRenderer gameRenderer;

    protected Button captureButton;

    private TextView fps;

    private TextView time;

    private String fpsText;

    private String timeText;

    private Timer timer;

    private long startTime;

    private String videoPath = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator;

    private String lastFileName;

    final Handler uiHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_FPS: {
                    updateFps(msg.arg1);
                }
                    break;

                case UPDATE_TIMER: {
                    updateTimer();
                }
                    break;

                case ENABLE_BUTTON: {
                    findViewById(msg.arg1).setEnabled(true);
                }
                    break;
            }
        }
    };

    public IProgressListener progressListener = new IProgressListener() {

        @Override
        public void onMediaStart() {
        }

        @Override
        public void onMediaProgress(float progress) {
        }

        @Override
        public void onMediaDone() {
        }

        @Override
        public void onMediaPause() {
        }

        @Override
        public void onMediaStop() {
        }

        @Override
        public void onError(Exception exception) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        if (configurationInfo.reqGlEsVersion < 0x20000) {
            showToast("This sample requires OpenGL ES 2.0");

            return;
        }

        setContentView(R.layout.game_capturing);

        renderingMethod = GameRenderer.RenderingMethod.FrameBuffer;
        renderingMethodList = (Spinner) findViewById(R.id.renderMethod);

        fillRenderMethodsList();

        surfaceView = (GameGLSurfaceView) findViewById(R.id.surfaceView);

        gameRenderer = new GameRenderer(getApplicationContext(), uiHandler, progressListener);
        surfaceView.setRenderer(gameRenderer);

        captureButton = (Button) findViewById(R.id.startCapturing);

        fps = (TextView) findViewById(R.id.fps);
        time = (TextView) findViewById(R.id.time);

        addItemsToUI();
    }

    @Override
    public void onPause() {
        stopCapturing();

        super.onPause();

        surfaceView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        surfaceView.onResume();
    }

    public void clickToggleCapturing(View view) throws IOException {
        if (gameRenderer.isCapturingStarted()) {
            stop();
        } else {
            start();
        }

        updateUI();
    }

    public void updateFps(int fps) {
        fpsText = String.valueOf(fps) + " FPS";
        this.fps.setText(fpsText);
    }

    public void updateTimer() {
        timeText = Format.duration(System.currentTimeMillis() - startTime);
        time.setText(timeText);
    }

    protected void addItemsToUI() {

    }

    protected void updateUI() {
        if (gameRenderer.isCapturingStarted()) {
            captureButton.setText("Stop Capturing");
        } else {
            captureButton.setText("Start Capturing");
        }
    }

    protected void startCapturing() throws IOException {
        lastFileName = "game_capturing.mp4";
        gameRenderer.startCapturing(videoPath + lastFileName);
    }

    protected void stopCapturing() {
        gameRenderer.stopCapturing();
    }

    public void start() throws IOException {
        captureButton.setEnabled(false);

        int method = renderingMethodList.getSelectedItemPosition();

        if (method == 0) {
            renderingMethod = GameRenderer.RenderingMethod.RenderTwice;
        } else {
            renderingMethod = GameRenderer.RenderingMethod.FrameBuffer;
        }

        gameRenderer.setRenderingMethod(renderingMethod);

        startCapturing();

        startTime = System.currentTimeMillis();

        timer = new Timer();
        timer.schedule(new UpdateTimerTask(), 0, 1000);

        uiHandler.sendMessageDelayed(
                uiHandler.obtainMessage(ENABLE_BUTTON, R.id.startCapturing, 0), 500);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }

        captureButton.setEnabled(false);

        stopCapturing();

        uiHandler.sendMessageDelayed(
                uiHandler.obtainMessage(ENABLE_BUTTON, R.id.startCapturing, 0), 500);

        playVideo();
    }

    protected void playVideo() {
        String videoUrl = "file:///" + videoPath + lastFileName;

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        Uri data = Uri.parse(videoUrl);
        intent.setDataAndType(data, "video/mp4");
        startActivity(intent);
    }

    private void fillRenderMethodsList() {
        List<String> list = new ArrayList<String>();

        list.add("Render Twice");
        list.add("Using Frame Buffer");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        renderingMethodList.setAdapter(dataAdapter);
    }

    protected void showToast(String title) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }

    private class UpdateTimerTask extends TimerTask {

        @Override
        public void run() {
            uiHandler.sendMessage(uiHandler.obtainMessage(UPDATE_TIMER));
        }
    }
}
