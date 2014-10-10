package com.intel.inde.mp.samples;

import android.content.Context;
import com.intel.inde.mp.*;
import com.intel.inde.mp.android.AndroidMediaObjectFactory;
import com.intel.inde.mp.android.AudioFormatAndroid;
import com.intel.inde.mp.android.VideoFormatAndroid;

import java.io.IOException;

//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

public class VideoCapture {
    private static final String TAG = "VideoCapture";

    private static final int width = 1280;
    private static final int height = 720;
    private static final int frameRate = 30;
    private static final int iFrameInterval = 1;
    private static final int bitRate = 3000;
    private static final String codec = "video/avc";

    private static final Object syncObject = new Object();

    private VideoFormat videoFormat;
    private GLCapture capture;

    private boolean isStarted;
    private boolean isConfigured;
    private Context context;
    private IProgressListener progressListener;

    public VideoCapture(Context context, IProgressListener progressListener) {
        this.context = context;
        this.progressListener = progressListener;
        initVideoFormat();
    }

    public void start(String videoPath) throws IOException {
        if (isStarted()) {
            throw new IllegalStateException(TAG + " already started!");
        }

        capture = new GLCapture(new AndroidMediaObjectFactory(context), progressListener);

        capture.setTargetFile(videoPath);
        capture.setTargetVideoFormat(videoFormat);

        AudioFormat audioFormat = new AudioFormatAndroid("audio/mp4a-latm", 44100, 1);
        capture.setTargetAudioFormat(audioFormat);

        capture.start();

        isStarted = true;
        isConfigured = false;
    }

    public void start(StreamingParameters params) throws IOException {
        if (isStarted()) {
            throw new IllegalStateException(TAG + " already started!");
        }

        capture = new GLCapture(new AndroidMediaObjectFactory(context), progressListener);

        capture.setTargetConnection(params);
        capture.setTargetVideoFormat(videoFormat);

        capture.start();

        isStarted = true;
        isConfigured = false;
    }

    public void stop() {
        if (!isStarted()) {
            throw new IllegalStateException(TAG + " not started or already stopped!");
        }

        capture.stop();
        capture = null;
        isConfigured = false;
    }

    private boolean configure() {
        if (isConfigured()) {
            return true;
        }

        try {
            capture.setSurfaceSize(width, height);
            isConfigured = true;
        } catch (Exception ex) {

        }

        return isConfigured;
    }

    public boolean beginCaptureFrame() {
        if (!isStarted()) {
            return false;
        }

        if (!isConfigured()) {
            if (!configure()) {
                return false;
            }
        }

        capture.beginCaptureFrame();

        return true;
    }

    public void endCaptureFrame() {
        if (!isStarted()) {
            return;
        }

        if (!isConfigured()) {
            return;
        }

        capture.endCaptureFrame();
    }

    public boolean isStarted() {
        if (capture == null) {
            return false;
        }

        return isStarted;
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    public int getFrameWidth() {
        return width;
    }

    public int getFrameHeight() {
        return height;
    }

    private void initVideoFormat() {
        videoFormat = new VideoFormatAndroid(codec, width, height);

        videoFormat.setVideoBitRateInKBytes(bitRate);
        videoFormat.setVideoFrameRate(frameRate);
        videoFormat.setVideoIFrameInterval(iFrameInterval);
    }
}
