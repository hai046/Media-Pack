//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.intel.inde.mp.IRecognitionPlugin;
import com.intel.inde.mp.android.AndroidMediaObjectFactory;
import com.intel.inde.mp.android.AudibleMagicPlugin;
import com.intel.inde.mp.domain.IAudioContentRecognition;
import com.intel.inde.mp.samples.controls.PlaybackToolbar;
import com.intel.inde.mp.samples.controls.PopupMessage;
import com.intel.inde.mp.samples.controls.VideoPlayer;

import java.io.*;
import java.nio.ByteBuffer;

public class RecognitionActivity extends Activity implements IRecognitionPlugin.RecognitionEvent,
        SurfaceHolder.Callback,
        VideoPlayer.VideoPlayerEvents,
        View.OnTouchListener, PlaybackToolbar.OnCommandListener
{
    private static final String DB_FILENAME = "demo.amdb";

    String lastMediaId = "bunny.mp4";

    Object syncObject;

    AudibleMagicPlugin plugin;
    IAudioContentRecognition recognition;

    SurfaceView videoSurface;
    VideoPlayer videoPlayer;
    PlaybackToolbar playbackToolbar;
    PopupMessage popupMessage;

    enum State {Idle, Running};

    State state;

    private String getMoviesFolder() {
        return (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + File.separator);
    }

    private String getAppFolder() {
        return (getCacheDir().getAbsolutePath() + File.separator);
    }

    private String getDBFilePath() {
        return getMoviesFolder() + DB_FILENAME;
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        syncObject = new Object();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.recognition_activity);

        try {
            init();
        } catch (IOException e) {
            showToast("Error failed to initialize: " + e.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        stopRecognition();

        videoPlayer.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // plugin.close();
    }

    @Override
    public void onContentRecognized(IRecognitionPlugin plugin, IRecognitionPlugin.RecognitionOutput output) {
        if (plugin instanceof AudibleMagicPlugin) {

            if(videoPlayer.playing()) {
                return;
            }

            AudibleMagicPlugin.ContentRecognitionOutput recognitionOutput = (AudibleMagicPlugin.ContentRecognitionOutput) output;

            if (recognitionOutput.getContent() == null) {
                return;
            }

            if (lastMediaId.equals(recognitionOutput.getContent().getItemId()) == false) {
                lastMediaId = recognitionOutput.getContent().getItemId();

                try {
                    videoPlayer.open(getMoviesFolder() + lastMediaId);
                } catch (IOException e) {
                    showToast("Error while opening recognized video");

                    return;
                }
            }

            AudibleMagicPlugin.ContentInfo contentInfo = recognitionOutput.getContent();

            long seekToMs = (long) (contentInfo.getCurrentLocationMs());

            videoPlayer.seekTo(seekToMs);
            videoPlayer.resume();

            showToast("Content recognized at " + formatDuration(seekToMs));
        }
    }

    private void startRecognition() {
        if (recognition == null || plugin == null) {
            showToast("Recognition is not initialized properly!");

            return;
        }

        if (state == State.Running) {
            showToast("Recognition is already in progress");

            return;
        }

        synchronized (syncObject) {
            recognition.start();
        }

        updateUI(State.Running);
    }

    private void stopRecognition() {
        if (recognition == null) {
            return;
        }

        if (state != State.Running) {
            return;
        }

        synchronized (syncObject) {
            recognition.stop();
        }

        updateUI(State.Idle);
    }

    private void init() throws IOException {
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        videoSurface.getHolder().addCallback(this);

        playbackToolbar = (PlaybackToolbar) findViewById(R.id.toolBar);
        playbackToolbar.setOnCommandListener(this);

        popupMessage = (PopupMessage)findViewById(R.id.status);

        initResources();

        initRecognition();

        updateUI(State.Idle);
    }

    private boolean checkNetworkConnection() {
        if (!isConnectedToNetwork()) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    System.exit(0);
                }
            };

            showMessageBox("Error while initializing recognition engine, please check your internet connection!", listener);

            return false;
        }

        return true;
    }

    protected void showMessageBox(String message, DialogInterface.OnClickListener listener) {

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage(message);
        b.setPositiveButton("OK", listener);
        AlertDialog d = b.show();

        ((TextView) d.findViewById(android.R.id.message)).setGravity(Gravity.CENTER);
    }

    private String rawResourceToString(int resource) throws IOException {
        InputStream inputStream = getResources().openRawResource(resource);
        ByteBuffer configBuffer = ByteBuffer.allocate(inputStream.available());
        inputStream.read(configBuffer.array());

        return new String(configBuffer.array());
    }

    void initRecognition() throws IOException {
        if (checkRequiredComponents() == false) {
            return;
        }

        String dbPath = getDBFilePath();
        String config1 = rawResourceToString(R.raw.intel_sdk_listen_to_samples);
        String config2 = rawResourceToString(R.raw.intel_sdk);
        String appPath = getAppFolder();

        recognition = (new AndroidMediaObjectFactory(getApplicationContext())).createAudioContentRecognition();

        try {
            plugin = new AudibleMagicPlugin(dbPath, config1, config2, appPath);
        } catch (Exception ex) {
            showToast("Failed to init recognition engine: " + ex.getMessage());

            return;
        }

        plugin.setOnRecognitionEventListener(this);

        recognition.setRecognizer(plugin);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        videoPlayer.pause();

        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (videoPlayer == null) {
            videoPlayer = new VideoPlayer(holder.getSurface());
            videoPlayer.setEventListener(this);
            videoSurface.setOnTouchListener(this);

            try {
                videoPlayer.open(getMoviesFolder() + lastMediaId);
            } catch (IOException e) {
                showToast(e.getMessage());

                return;
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (videoPlayer != null) {
            videoPlayer.close();
        }
    }

    @Override
    public void onVideoPlayerDurationChanged(long duration) {
        playbackToolbar.setDuration(duration);
    }

    @Override
    public void onVideoPlayerPositionChanged(boolean outside, long position) {
        playbackToolbar.setPosition(position);
    }

    @Override
    public void onVideoPlayerPlaybackResumed() {
        stopRecognition();
        playbackToolbar.setPlayState();
    }

    @Override
    public void onVideoPlayerPlaybackPaused() {
        startRecognition();
        playbackToolbar.setPauseState();
    }

    @Override
    public void onPlaybackToolbarPlay() {
        videoPlayer.resume();
    }

    @Override
    public void onPlaybackToolbarPause() {
        videoPlayer.pause();
    }

    @Override
    public void onPlaybackToolbarPositionChanged(long value) {
        videoPlayer.seekTo(value);
    }

    private void updateUI(State state) {
        this.state = state;

        showStatus(this.state == State.Running);
    }

    private void showStatus(boolean show) {
        if (show) {
            popupMessage.show(getResources().getString(R.string.listening_for_audio));
        } else {
            popupMessage.hide();
        }
    }

    private void showToast(String title) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }

    private boolean checkRequiredComponents() {
        String dbPath = getDBFilePath();

        if (checkFileExist(dbPath) == false) {
            showToast("AM database file is missing!");

            return false;
        }

        if (checkNetworkConnection() == false) {

            return false;
        }

        return true;
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    private boolean checkFileExist(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void initResources() {

        if (checkFileExist(getDBFilePath()) == false) {
            copyFromRawToAppFolder(R.raw.demo, getDBFilePath());
        }
    }

    private void copyFromRawToAppFolder(int resId, String filePath) {
        File destFile = new File(filePath);

        if (destFile.exists()) {
            destFile.delete();
        }

        FileOutputStream out;

        try {
            out = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            return;
        }

        InputStream in = getResources().openRawResource(resId);

        byte[] buff = new byte[1024];

        int read = 0;

        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (IOException e) {
        }

        try {
            in.close();
        } catch (IOException e) {
        }

        try {
            out.close();
        } catch (IOException e) {
        }
    }

    private String formatDuration(long durationMs) {
        long duration = durationMs / 1000;

        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);

        String durationValue;

        if (h == 0) {
            durationValue = asTwoDigit(m) + ":" + asTwoDigit(s);
        } else {
            durationValue = asTwoDigit(h) + ":" + asTwoDigit(m) + ":" + asTwoDigit(s);
        }

        return durationValue;
    }

    private String asTwoDigit(long digit) {
        String value = "";

        if (digit < 10) {
            value = "0";
        }

        value += String.valueOf(digit);

        return value;
    }
}