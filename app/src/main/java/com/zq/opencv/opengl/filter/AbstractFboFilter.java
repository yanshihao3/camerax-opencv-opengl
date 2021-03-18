package com.zq.opencv.opengl.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.zq.opencv.opengl.utils.OpenGLUtils;

/**
 * @program: opencv
 * @description:
 * @author: 闫世豪
 * @create: 2021-03-17 16:44
 **/
public class AbstractFboFilter extends AbstractFilter {
    int[] frameBuffer;
    int[] frameTextures;

    public AbstractFboFilter(Context context, int vertexShaderId, int fragmentShaderId) {
        super(context, vertexShaderId, fragmentShaderId);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        //创建FBO
        /**
         * 1.创建FBO  + FBO 纹理
         *
         */
        frameBuffer = new int[1];
        frameTextures = new int[1];
        GLES20.glGenBuffers(1, frameBuffer, 0);
        OpenGLUtils.glGenTextures(frameTextures);
        /**
         * 2.fbo 与纹理关联
         */
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                null);
        //纹理关联 fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);  //綁定FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                frameTextures[0],
                0);
    }

    @Override
    public int onDraw(int texture) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]); //綁定fbo
        super.onDraw(texture);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);  //
        return frameTextures[0];
    }

    /**
     * 释放资源
     */
    @Override
    public void release() {
        super.release();
        if (frameTextures != null) {
            GLES20.glDeleteTextures(1, frameTextures, 0);
            frameTextures = null;
        }

        if (frameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
        }
    }
}
