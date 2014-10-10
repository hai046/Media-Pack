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
import com.intel.inde.mp.MediaFile;
import com.intel.inde.mp.Uri;
import com.intel.inde.mp.domain.Pair;

import java.io.IOException;

public class ComposerCutCoreActivity extends ComposerTranscodeCoreActivity {

    private long segmentFrom = 0;
    private long segmentTo = 0;

    @Override
    protected void getActivityInputs() {

        Bundle b = getIntent().getExtras();
        srcMediaName1 = b.getString("srcMediaName1");
        dstMediaPath = b.getString("dstMediaPath");
        mediaUri1 = new Uri(b.getString("srcUri1"));

        segmentFrom = b.getLong("segmentFrom");
        segmentTo = b.getLong("segmentTo");
    }

    @Override
    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {
        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);

        ///////////////////////////

        MediaFile mediaFile = mediaComposer.getSourceFiles().get(0);
        mediaFile.addSegment(new Pair<Long, Long>(segmentFrom, segmentTo));
    }

    @Override
    protected void printDuration() {

    	TextView v = (TextView)findViewById(R.id.durationInfo);
        v.setText(String.format("duration = %.1f sec\n", (float)(segmentTo - segmentFrom)/1e6));
        v.append(String.format("from = %.1f sec\nto = %.1f sec\n", (float)segmentFrom/1e6, (float)segmentTo/1e6));
    }
}

