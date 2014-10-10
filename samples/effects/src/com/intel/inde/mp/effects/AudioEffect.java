//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.effects;


import com.intel.inde.mp.IAudioEffect;
import com.intel.inde.mp.domain.Pair;

public abstract class AudioEffect implements IAudioEffect {
    private Pair<Long, Long> segment;

    @Override
    public void setSegment(Pair<Long, Long> segment) {
        this.segment = segment;
    }

    @Override
    public Pair<Long, Long> getSegment() {
        return segment;
    }
}
