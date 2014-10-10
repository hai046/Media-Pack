//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerActivity extends Activity {
    public static String VIDEO_PATH = "VideoPath";

    private VideoView mVideoView;
    private MediaController mVideoController;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.video_player_activity);

        init();

        if (icicle != null) {
            String path = icicle.getString("path");

            playVideo(path);
        }
    }

    public void onPause() {
        super.onPause();

        mVideoView.stopPlayback();
    }

    private void init() {
        mVideoView = (VideoView) findViewById(R.id.video_view);

        mVideoController = new MediaController(this, false);

        mVideoView.setMediaController(mVideoController);

        mVideoView.requestFocus();
        mVideoView.setZOrderOnTop(true);
    }

    private void playVideo(String path) {
        Uri uri = Uri.parse(path);
        mVideoView.setVideoURI(uri);

        mVideoView.start();
    }
}
