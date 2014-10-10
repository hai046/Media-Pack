//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import com.intel.inde.mp.StreamingParameters;

public class GameStreaming extends GameCapturing {

    private TableLayout table;

    @Override
    protected void addItemsToUI() {

        updateUI();

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        frameLayout.setVisibility(View.VISIBLE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        table = (TableLayout) inflater.inflate(R.layout.streaming_params, null);

        frameLayout.addView(table);
        findViewById(R.id.host).requestFocus(Gravity.END);
    }

    ///////////////////////////////////////////////////////////////////////////

    private StreamingParameters prepareStreamingParams() throws Exception {

        StreamingParameters parameters = new StreamingParameters();

        parameters.Host = ((EditText) findViewById(R.id.host)).getText().toString();
        parameters.Port = Integer.parseInt(((EditText) findViewById(R.id.port)).getText().toString());
        parameters.ApplicationName = ((EditText) findViewById(R.id.applicationName)).getText().toString();
        parameters.StreamName = ((EditText) findViewById(R.id.streamName)).getText().toString();

        parameters.isToPublishAudio = false;
        parameters.isToPublishVideo = true;

        return parameters;
    }

    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void startCapturing() {
        try {
            gameRenderer.startCapturing(prepareStreamingParams());
        } catch (Exception e) {
            showToast("Failed to setup streaming parameters.");
            e.printStackTrace();
        }
    }

    @Override
    protected void updateUI() {
        if (gameRenderer.isCapturingStarted()) {

            captureButton.setText("Stop Streaming");

            if(table != null) {
                table.setVisibility(View.INVISIBLE);
            }
        } else {
            captureButton.setText("Start Streaming");

            if(table != null) {
                table.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void playVideo() {

    }
}
