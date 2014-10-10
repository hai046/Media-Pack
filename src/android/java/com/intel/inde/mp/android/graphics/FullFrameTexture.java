//
//               INTEL CORPORATION PROPRIETARY INFORMATION
//  This software is supplied under the terms of a license agreement or
//  nondisclosure agreement with Intel Corporation and may not be copied
//  or disclosed except in accordance with the terms of that agreement.
//        Copyright (c) 2013-2014 Intel Corporation. All Rights Reserved.
//

package com.intel.inde.mp.android.graphics;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;

public class FullFrameTexture {
    private static final String VERTEXT_SHADER =
        "uniform mat4 uOrientationM;\n" +
            "uniform mat4 uTransformM;\n" +
            "attribute vec2 aPosition;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "gl_Position = vec4(aPosition, 0.0, 1.0);\n" +
            "vTextureCoord = (uTransformM * ((uOrientationM * gl_Position + 1.0) * 0.5)).xy;" +
            "}";

    private static final String FRAGMENT_SHADER =
        "precision mediump float;\n" +
            "uniform sampler2D sTexture;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}";

    private final byte[] FULL_QUAD_COORDINATES = {-1, 1, -1, -1, 1, 1, 1, -1};

    private ShaderProgram shader;

    private ByteBuffer fullQuadVertices;

    private final float[] orientationMatrix = new float[16];
    private final float[] transformMatrix = new float[16];

    public FullFrameTexture() {
        if (shader != null) {
            shader = null;
        }

        shader = new ShaderProgram(EglUtil.getInstance());

        shader.create(VERTEXT_SHADER, FRAGMENT_SHADER);

        fullQuadVertices = ByteBuffer.allocateDirect(4 * 2);

        fullQuadVertices.put(FULL_QUAD_COORDINATES).position(0);

        Matrix.setRotateM(orientationMatrix, 0, 0, 0f, 0f, 1f);
        Matrix.setIdentityM(transformMatrix, 0);
    }

    public void release() {
        shader = null;
        fullQuadVertices = null;
    }

    public void draw(int textureId) {
        shader.use();

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        int uOrientationM = shader.getAttributeLocation("uOrientationM");
        int uTransformM = shader.getAttributeLocation("uTransformM");

        GLES20.glUniformMatrix4fv(uOrientationM, 1, false, orientationMatrix, 0);
        GLES20.glUniformMatrix4fv(uTransformM, 1, false, transformMatrix, 0);

        // Trigger actual rendering.
        renderQuad(shader.getAttributeLocation("aPosition"));

        shader.unUse();
    }

    private void renderQuad(int aPosition) {
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false, 0, fullQuadVertices);
        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
