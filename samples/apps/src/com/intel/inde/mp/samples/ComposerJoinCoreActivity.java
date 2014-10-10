//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.os.Bundle;
import android.widget.TextView;
import com.intel.inde.mp.MediaComposer;
import com.intel.inde.mp.MediaFileInfo;
import com.intel.inde.mp.android.AndroidMediaObjectFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ComposerJoinCoreActivity extends ComposerTranscodeCoreActivity {

    @Override
    protected void getActivityInputs() {
        Bundle b = getIntent().getExtras();
        srcMediaName1 = b.getString("srcMediaName1");
        srcMediaName2 = b.getString("srcMediaName2");
        dstMediaPath = b.getString("dstMediaPath");
        mediaUri1 = new com.intel.inde.mp.Uri(b.getString("srcUri1"));
        mediaUri2 = new com.intel.inde.mp.Uri(b.getString("srcUri2"));
    }

    @Override
    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {
        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);

        mediaComposer.addSourceFile(mediaUri2);
    }

    @Override
    protected void printDuration() {
        long duration1 = duration;
        long duration2 = 0;

        try {
            MediaFileInfo mediaFileInfo = new MediaFileInfo(new AndroidMediaObjectFactory(getApplicationContext()));
            mediaFileInfo.setUri(mediaUri2);
            duration2 = mediaFileInfo.getDurationInMicroSec();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView v = (TextView)findViewById(R.id.durationInfo);
        v.setText(String.format("durationSrc1 = %d sec\n", TimeUnit.MICROSECONDS.toSeconds(duration1)));
        v.append(String.format("durationSrc2 = %d sec\n", TimeUnit.MICROSECONDS.toSeconds(duration2)));
    }

    @Override
    protected void getDstDuration() {
        try {
            mediaFileInfo = new MediaFileInfo(new AndroidMediaObjectFactory(getApplicationContext()));
            mediaFileInfo.setUri(mediaUri2);

            duration += mediaFileInfo.getDurationInMicroSec();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void printPaths() {
        pathInfo.setText(String.format("srcMediaFileName1 = %s\nsrcMediaFileName2 = %s\ndstMediaPath = %s\n", srcMediaName1, srcMediaName2, dstMediaPath));
    }
}


