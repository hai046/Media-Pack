//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.view.SurfaceHolder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.intel.inde.mp.domain.Resolution;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ComposerMediaFileInfoCoreActivity extends ComposerTranscodeCoreActivity implements SurfaceHolder.Callback {

    private TextView sliderPositionTextView;
    private long sliderPosition = 0;

    @Override
    protected void setupUI()
    {
        buttonStart.setVisibility(View.GONE);
        buttonStop.setVisibility(View.GONE);

        findViewById(R.id.transcodeParametersLayout).setVisibility(View.GONE);

        sliderPositionTextView = (TextView) findViewById(R.id.pathInfo);

        SeekBar seekBar = new SeekBar(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(8, 8, 8, 0);
        seekBar.setLayoutParams(lp);
        seekBar.setProgress(0);
        seekBar.setMax(100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                try {
                    // call preview method right here
                    sliderPosition = (long) ((double) progressChanged / (100.0) * (double) duration);
                    sliderPositionTextView.setText(String.format("slider position = %.1f sec\n", (double) sliderPosition / 10e5));
                    pathInfo.append(String.format("mediaFileName = %s\n", srcMediaName1));

                    ByteBuffer buffer = ByteBuffer.allocate(1);
                    mediaFileInfo.getFrameAtPosition(sliderPosition, buffer);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
        layout.addView(seekBar, 0);
    }

    protected void printFileInfo() {

        String videoInfoString = "No video info available\n";
        String audioInfoString = "No audio info available\n";

        sliderPositionTextView.setText(String.format("slider position = %.1f sec\n", (double) sliderPosition / 1e6));

        //video format
        try {
            Resolution resolution = videoFormat.getVideoFrameSize();
            String videoCodec = videoFormat.getVideoCodec();

            pathInfo =  (TextView) findViewById(R.id.pathInfo);
            pathInfo.append(String.format("mediaFileName = %s\n", srcMediaName1));

            durationInfo = (TextView) findViewById(R.id.durationInfo);
            durationInfo.setText(String.format("duration = %d sec", TimeUnit.MICROSECONDS.toSeconds(duration)));

            videoInfoString = String.format(Locale.getDefault(), "Video Info\n\n" +
                "video codec = %s\n" +
                "width = %d\n" +
                "height = %d\n\n",
                videoCodec, resolution.width(), resolution.height());

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //audio format
        try {
            int aacProfile = audioFormat.getAudioProfile();
            String audioCodec = audioFormat.getAudioCodec();

            String mimeType = audioFormat.getMimeType();

            int channelCount = audioFormat.getAudioChannelCount();
            int sampleRate = audioFormat.getAudioSampleRateInHz();

            audioInfoString = String.format(Locale.getDefault(), "Audio Info\n\n" +
                "channel count = %d\n" +
                "sample rate = %d Hz\n" +
                "mime type = %s\n" +
                "aac profile = %d\n" +
                "audio codec = %s\n",
                channelCount, sampleRate, mimeType,
                aacProfile, audioCodec );

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        transcodeInfoView.setText(videoInfoString);
        transcodeInfoView.append(audioInfoString);
    }
}
