//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.samples;

import com.intel.inde.mp.MediaComposer;
import com.intel.inde.mp.effects.SubstituteAudioEffect;

import java.io.IOException;

public class ComposerAudioEffectCoreActivity extends ComposerJoinCoreActivity {

    @Override
    protected void setTranscodeParameters(MediaComposer mediaComposer) throws IOException {
        mediaComposer.addSourceFile(mediaUri1);
        mediaComposer.setTargetFile(dstMediaPath);

        configureVideoEncoder(mediaComposer, videoWidthOut, videoHeightOut);
        configureAudioEncoder(mediaComposer);

        ///////////////////////////

        SubstituteAudioEffect effect = new SubstituteAudioEffect();
        effect.setFileUri(this, mediaUri2);
        mediaComposer.addAudioEffect(effect);
    }      

    @Override
    protected void getDstDuration() {
 	
    }    
}
