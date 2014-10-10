//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.effects;

import com.intel.inde.mp.android.graphics.VideoEffect;
import com.intel.inde.mp.domain.graphics.IEglUtil;

public class InverseEffect extends VideoEffect {

    public InverseEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
    }

    @Override
    protected String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
            "  float colorR = (1.0 - color.r) / 1.0;\n" +
            "  float colorG = (1.0 - color.g) / 1.0;\n" +
            "  float colorB = (1.0 - color.b) / 1.0;\n" +
            "  gl_FragColor = vec4(colorR, colorG, colorB, color.a);\n" +
            "}\n";
    }
}
