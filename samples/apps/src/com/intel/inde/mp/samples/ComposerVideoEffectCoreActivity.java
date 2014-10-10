//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import android.os.Bundle;
import com.intel.inde.mp.IVideoEffect;
import com.intel.inde.mp.MediaComposer;
import com.intel.inde.mp.Uri;
import com.intel.inde.mp.domain.Pair;
import com.intel.inde.mp.effects.*;

import java.io.IOException;


public class ComposerVideoEffectCoreActivity extends ComposerTranscodeCoreActivity {

    private int effectIndex;

    @Override
    protected void getActivityInputs() {

        Bundle b = getIntent().getExtras();
        srcMediaName1 = b.getString("srcMediaName1");
        dstMediaPath = b.getString("dstMediaPath");
        mediaUri1 = new Uri(b.getString("srcUri1"));

        effectIndex = b.getInt("effectIndex");
    }

    @Override
    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {
        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);

        configureVideoEffect(mediaComposer);
    }

    private void configureVideoEffect(MediaComposer mediaComposer) {
        IVideoEffect effect = null;

        switch (effectIndex) {
            case 0:
                effect = new SepiaEffect(0, factory.getEglUtil());
                break;
            case 1:
                effect = new GrayScaleEffect(0, factory.getEglUtil());
                break;
            case 2:
                effect = new InverseEffect(0, factory.getEglUtil());
                break;
            case 3:
                effect = new TextOverlayEffect(0, factory.getEglUtil());
                break;
            default:
                break;
        }

        if (effect != null) {
            effect.setSegment(new Pair<Long, Long>(0l, 0l)); // Apply to the entire stream
            mediaComposer.addVideoEffect(effect);
        }
    }

    @Override
    protected void printEffectDetails() {
        effectDetails.append(String.format("Video effect = %s\n", getVideoEffectName(effectIndex)));
    }

    private String getVideoEffectName(int videoEffectIndex) {
        switch (videoEffectIndex) {
            case 0:
                return "Sepia";
            case 1:
                return "Grayscale";
            case 2:
                return "Inverse";
            case 3:
                return "Text Overlay";
            default:
                return "Unknown";
        }
    }
}


