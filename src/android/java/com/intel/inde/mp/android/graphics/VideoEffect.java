//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.inde.mp.android.graphics;


import com.intel.inde.mp.IVideoEffect;
import com.intel.inde.mp.domain.Pair;
import com.intel.inde.mp.domain.Resolution;
import com.intel.inde.mp.domain.graphics.IEglUtil;
import com.intel.inde.mp.domain.graphics.Program;
import com.intel.inde.mp.domain.graphics.TextureType;
import com.intel.inde.mp.domain.pipeline.TriangleVerticesCalculator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class VideoEffect implements IVideoEffect {
    protected static final int FLOAT_SIZE_BYTES = 4;
    protected static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    protected static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    protected static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    protected Resolution inputResolution = new Resolution(0, 0);
    private   Pair<Long, Long> segment = new Pair<Long, Long>(0l, 0l);
    protected IEglUtil eglUtil;
    protected Program eglProgram = new Program();
    // OpenGL handlers
    protected boolean wasStarted;
    protected float[] mvpMatrix = new float[16];
    private FloatBuffer triangleVertices;
    private int angle;
    protected ShaderProgram shaderProgram;
    private boolean fitToContext = true;

    public VideoEffect(int angle, IEglUtil eglUtil) {
        this.angle = angle;
        this.eglUtil = eglUtil;
    }

    protected String getVertexShader() {
        return eglUtil.VERTEX_SHADER;
    }

    protected String getFragmentShader() {
        return eglUtil.FRAGMENT_SHADER_OES;
    }

    @Override
    public Pair<Long, Long> getSegment() {
        return segment;
    }

    @Override
    public void setSegment(Pair<Long, Long> segment) {
        this.segment = segment;
    }

    protected void addEffectSpecific() {
    }

    /**
     * Initializes GL state.  Call this after the encoder EGL surface has been created and made current.
     */
    public void start() {
        triangleVertices = ByteBuffer
                .allocateDirect(TriangleVerticesCalculator.getDefaultTriangleVerticesData().length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        createProgram(getVertexShader(), getFragmentShader());

        eglProgram.programHandle = shaderProgram.getProgramHandle();
        eglProgram.positionHandle = shaderProgram.getAttributeLocation("aPosition");
        eglProgram.textureHandle = shaderProgram.getAttributeLocation("aTextureCoord");
        eglProgram.mvpMatrixHandle = shaderProgram.getAttributeLocation("uMVPMatrix");
        eglProgram.stMatrixHandle = shaderProgram.getAttributeLocation("uSTMatrix");

        wasStarted = true;
    }

    @Override
    public void applyEffect(int inputTextureId, long timeProgress, float[] transformMatrix) {
        if (!wasStarted) {
            start();
        }
        triangleVertices.clear();
        triangleVertices.put(TriangleVerticesCalculator.getDefaultTriangleVerticesData()).position(0);

        eglUtil.drawFrameStart(eglProgram, triangleVertices, mvpMatrix, transformMatrix, angle, TextureType.GL_TEXTURE_EXTERNAL_OES, inputTextureId, inputResolution, fitToContext);
        addEffectSpecific();
        eglUtil.drawFrameFinish();
    }

    @Override
    public void setInputResolution(Resolution resolution) {
        inputResolution = resolution;
    }

    @Override
    public boolean fitToCurrentSurface(boolean should) {
        boolean toRet = fitToContext;
        fitToContext = should;
        return toRet;
    }

    protected int createProgram(String vertexSource, String fragmentSource) {
        shaderProgram = new ShaderProgram(eglUtil);
        shaderProgram.create(vertexSource, fragmentSource);
        return shaderProgram.getProgramHandle();
    }

    protected void checkGlError(String component) {
        eglUtil.checkEglError(component);
    }

    protected void checkGlError() {
        eglUtil.checkEglError("VideoEffect");
    }
}
