//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.effects;

import android.content.Context;
import com.intel.inde.mp.Uri;

import java.nio.ByteBuffer;

public class SubstituteAudioEffect extends AudioEffect {
    private AudioReader reader = new AudioReader();
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
    private Uri uri;

    @Override
    public void applyEffect(ByteBuffer input, long timeProgress) {
        if (reader.read(byteBuffer)) {
            int position = input.position();

            byteBuffer.position(0);
            byteBuffer.limit(position);

            input.position(0);
            input.put(byteBuffer);
        }
    }

    public void setFileUri(Context context, Uri uri) {
        this.uri = uri;

        reader.setFileUri(uri);
        reader.start(context);
    }

    public Uri getFileUri() {
        return uri;
    }
}
