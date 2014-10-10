//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;
import com.intel.inde.mp.*;
import com.intel.inde.mp.android.AndroidMediaObjectFactory;
import com.intel.inde.mp.android.AudioFormatAndroid;
import com.intel.inde.mp.android.VideoFormatAndroid;
import com.intel.inde.mp.android.graphics.VideoEffect;
import com.intel.inde.mp.domain.IPreview;
import com.intel.inde.mp.domain.Pair;
import com.intel.inde.mp.domain.Resolution;
import com.intel.inde.mp.effects.GrayScaleEffect;
import com.intel.inde.mp.effects.InverseEffect;
import com.intel.inde.mp.effects.SepiaEffect;
import com.intel.inde.mp.samples.controls.CameraCaptureSettingsPopup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraCapturerActivity extends Activity implements CameraCaptureSettingsPopup.CameraCaptureSettings {

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
            exception.printStackTrace();
        }
    };
    private AndroidMediaObjectFactory factory;

    class AllEffects implements IVideoEffect {
        private Pair<Long, Long> segment = new Pair<Long, Long>(0l, 0l);
        private ArrayList<IVideoEffect> videoEffects = new ArrayList<IVideoEffect>();
        private int activeEffectId;

        @Override
        public Pair<Long, Long> getSegment() {
            return segment;
        }

        @Override
        public void setSegment(Pair<Long, Long> segment) {
        }

        @Override
        public void start() {
            for (IVideoEffect effect : videoEffects) {
                effect.start();
            }
        }

        @Override
        public void applyEffect(int inTextureId, long timeProgress, float[] transformMatrix) {
            videoEffects.get(activeEffectId).applyEffect(inTextureId, timeProgress, transformMatrix);
        }

        @Override
        public void setInputResolution(Resolution resolution) {
            for (IVideoEffect videoEffect : videoEffects) {
                videoEffect.setInputResolution(resolution);
            }
        }

        @Override
        public boolean fitToCurrentSurface(boolean should) {
            boolean fitValue = false;
            for (IVideoEffect videoEffect : videoEffects) {
                fitValue = videoEffect.fitToCurrentSurface(should);
            }
            return fitValue;
        }

        public void setActiveEffectId(int activeEffectId) {
            this.activeEffectId = activeEffectId;
        }

        public ArrayList<IVideoEffect> getVideoEffects() {
            return videoEffects;
        }
    }

    boolean isRecordingInProgress = false;

    CameraCaptureSettingsPopup settingsPopup;

    Camera camera = null;
    CameraCapture capture;
    List<Camera.Size> supportedResolutions;
    Resolution encodedResolution = null;
    private IPreview preview;
    AllEffects alleffects = new AllEffects();

    public void onCreate(Bundle icicle) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        super.onCreate(icicle);

        factory = new AndroidMediaObjectFactory(getApplicationContext());
        capture = new CameraCapture(factory, progressListener);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_capturer_activity);

        createCamera();

        settingsPopup = new CameraCaptureSettingsPopup(this, supportedResolutions);
        settingsPopup.setEventListener(this);

        configureEffects(factory);
        createPreview();

        updateVideoPreview();
    }

    private void configureEffects(final AndroidMediaObjectFactory factory) {
        alleffects.getVideoEffects().add(new VideoEffect(0, factory.getEglUtil()) {
        });
        alleffects.getVideoEffects().add(new GrayScaleEffect(0, factory.getEglUtil()));
        alleffects.getVideoEffects().add(new SepiaEffect(0, factory.getEglUtil()));
        alleffects.getVideoEffects().add(new InverseEffect(0, factory.getEglUtil()));
    }

    @Override
    public void onDestroy() {
        stopRecording();

        preview.stop();
        preview = null;

        camera.release();
        capture = null;

        super.onDestroy();
    }

    private void createCamera() {
        camera = Camera.open(1); //default camera device
        supportedResolutions = camera.getParameters().getSupportedPreviewSizes();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(supportedResolutions.get(0).width, supportedResolutions.get(0).height);
        parameters.setRecordingHint(true);
        camera.setParameters(parameters);
    }

    private void configureMediaStreamFormat() {

        VideoFormat videoFormat = new VideoFormatAndroid("video/avc", encodedResolution.width(), encodedResolution.height());
        videoFormat.setVideoBitRateInKBytes(3000);
        videoFormat.setVideoFrameRate(25);
        videoFormat.setVideoIFrameInterval(1);
        capture.setTargetVideoFormat(videoFormat);

        AudioFormat audioFormat = new AudioFormatAndroid("audio/mp4a-latm", 44100, 1);
        capture.setTargetAudioFormat(audioFormat);
    }

    public void toggleStreaming(View view) {
        updateUI();

        if (isRecordingInProgress) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    public void startRecording() {
        if (!isRecordingInProgress) {
            capture();

            isRecordingInProgress = true;

        } else {
            Toast.makeText(this, "Can have only one active session.", Toast.LENGTH_SHORT).show();
        }
    }

    private void capture() {
        if(encodedResolution == null) {
            encodedResolution = new Resolution(640, 480);
        }

        capture.addVideoEffect(alleffects);

        try {
            capture.setTargetFile(getVideoFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        configureMediaStreamFormat();

        capture.start();
    }

    public void stopRecording() {
        if (isRecordingInProgress) {
            capture.stop();

            isRecordingInProgress = false;

            showToast("Video saved to " + getVideoFilePath());

            updateVideoPreview();
        }
    }

    private void createPreview() {
        preview = capture.createPreview(findViewById(R.id.surfaceView), camera);
    }

    private File getAndroidMoviesFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
    }

    public String getVideoFilePath() {
        return getAndroidMoviesFolder().getAbsolutePath() + "/capture.mp4";
    }

    public void onClickEffect(View v)
    {
        if(isRecordingInProgress) {
            return;
        }

        switch (v.getId())
        {
            default:
            {
                String tag = (String) v.getTag();

                if(tag != null)
                {
                    alleffects.setActiveEffectId(Integer.parseInt(tag));
                    preview.setActiveEffect(alleffects);
                }
            }
            break;
        }
    }

    public void onClickPreview(View v) {
        playVideo();
    }

    public void updateVideoPreview() {
        Bitmap thumb;

        thumb = ThumbnailUtils.createVideoThumbnail(getVideoFilePath(), MediaStore.Video.Thumbnails.MINI_KIND);

        ImageButton preview = (ImageButton)findViewById(R.id.preview);
        preview.setImageBitmap(thumb);
    }

    protected void playVideo() {
        String videoUrl = "file:///" + getVideoFilePath();

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        android.net.Uri data = android.net.Uri.parse(videoUrl);
        intent.setDataAndType(data, "video/mp4");
        startActivity(intent);
    }

    public void showSettings(View view) {
        settingsPopup.show(view, false);
    }

    @Override
    public void displayResolutionChanged(int width, int height) {
        Camera.Parameters params = camera.getParameters();

        params.setPreviewSize(width, height);
        preview.stop();
        camera.setParameters(params);
        preview.updateCameraParameters();
        preview.start();
    }

    @Override
    public void videoResolutionChanged(int width, int height) {
        encodedResolution = new Resolution(width, height);
    }

    private void updateUI() {
        ImageButton streamingButton = (ImageButton)findViewById(R.id.streaming);
        ImageButton settingsButton = (ImageButton)findViewById(R.id.settings);
        ImageButton previewButton = (ImageButton)findViewById(R.id.preview);

        ScrollView container = (ScrollView)findViewById(R.id.effectsContainer);

        if (isRecordingInProgress) {
            streamingButton.setImageResource(R.drawable.rec_inact);

            container.setVisibility(View.VISIBLE);
            settingsButton.setVisibility(View.VISIBLE);
            previewButton.setVisibility(View.VISIBLE);
        } else {
            streamingButton.setImageResource(R.drawable.rec_act);

            container.setVisibility(View.INVISIBLE);
            settingsButton.setVisibility(View.INVISIBLE);
            previewButton.setVisibility(View.INVISIBLE);
        }
    }

    protected void showToast(String title) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
    }
}
