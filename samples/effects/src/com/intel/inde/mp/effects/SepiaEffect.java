//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.effects;

import android.opengl.GLES20;
import com.intel.inde.mp.android.graphics.VideoEffect;
import com.intel.inde.mp.domain.graphics.IEglUtil;

public class SepiaEffect extends VideoEffect {
    public SepiaEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
    }

    @Override
    protected String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform mat3 uWeightsMatrix;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "void main() {\n" +
                "  vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                "  vec3 color_new = min(uWeightsMatrix * color.rgb, 1.0);\n" +
                "  gl_FragColor = vec4(color_new.rgb, color.a);\n" +
                "}\n";

    }

    protected float[] getWeights() {
        return new float[]{
                805.0f / 2048.0f, 715.0f / 2048.0f, 557.0f / 2048.0f,
                1575.0f / 2048.0f, 1405.0f / 2048.0f, 1097.0f / 2048.0f,
                387.0f / 2048.0f, 344.0f / 2048.0f, 268.0f / 2048.0f
        };
    }

    protected int weightsMatrixHandle;

    @Override
    public void start() {
        super.start();
        weightsMatrixHandle = shaderProgram.getAttributeLocation("uWeightsMatrix");
    }

    @Override
    protected void addEffectSpecific() {
        GLES20.glUniformMatrix3fv(weightsMatrixHandle, 1, false, getWeights(), 0);
    }
}
