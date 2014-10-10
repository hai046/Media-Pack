//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.intel.inde.mp.android.graphics.VideoEffect;
import com.intel.inde.mp.domain.graphics.IEglUtil;

public abstract class OverlayEffect extends VideoEffect {

    private int oTextureHandle;
    private int[] textures = new int[1];

    public OverlayEffect(int angle, IEglUtil eglUtil) {
        super(angle, eglUtil);
    }

    @Override
    protected String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "uniform sampler2D oTexture;\n" +
                "void main() {\n" +
                "  vec4 bg_color = texture2D(sTexture, vTextureCoord);\n" +
                "  vec4 fg_color = texture2D(oTexture, vTextureCoord);\n" +
                "  float colorR = (1.0 - fg_color.a) * bg_color.r + fg_color.a * fg_color.r;\n" +
                "  float colorG = (1.0 - fg_color.a) * bg_color.g + fg_color.a * fg_color.g;\n" +
                "  float colorB = (1.0 - fg_color.a) * bg_color.b + fg_color.a * fg_color.b;\n" +
                "  gl_FragColor = vec4(colorR, colorG, colorB, bg_color.a);\n" +
                "}\n";

    }

    @Override
    public void start() {
        super.start();
        oTextureHandle = shaderProgram.getAttributeLocation("oTexture");

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    }

    @Override
    protected void addEffectSpecific() {
        Bitmap bitmap = Bitmap.createBitmap(inputResolution.width(), inputResolution.height(), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.argb(0, 0, 0, 0));
        Canvas bitmapCanvas = new Canvas(bitmap);
        drawCanvas(bitmapCanvas);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        checkGlError("glBindTexture");
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
        checkGlError("texImage2d");
        GLES20.glUniform1i(oTextureHandle, 1);
        checkGlError("oTextureHandle - glUniform1i");
        bitmap.recycle();
    }

    protected abstract void drawCanvas(Canvas canvas);
}
