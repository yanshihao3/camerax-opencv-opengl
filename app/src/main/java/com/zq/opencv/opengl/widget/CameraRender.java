package com.zq.opencv.opengl.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import androidx.lifecycle.LifecycleOwner;

import com.zq.opencv.opengl.filter.CameraFilter;
import com.zq.opencv.opengl.utils.CameraHelper;
import com.zq.opencv.opengl.utils.OpenGLUtils;
import com.zq.opencv.opengl.filter.ScreenFilter;

import java.util.concurrent.Executor;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @program: opencv
 * @description:
 * @author: 闫世豪
 * @create: 2021-03-17 10:18
 **/
public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    // 摄像头的图像  用OpenGL ES 画出来
    private SurfaceTexture mCameraTexure;
    private CameraView cameraView;
    private ScreenFilter screenFilter;

    float[] mtx = new float[16];
    private Context context;
    private CameraFilter cameraFilter;

    private int mCameraTextureId = -1;
    private LifecycleOwner lifecycleOwner;

    public CameraRender(CameraView cameraView) {
        this.cameraView = cameraView;
        context = cameraView.getContext();
        lifecycleOwner = (LifecycleOwner) cameraView.getContext();

        mCameraTextureId = OpenGLUtils.getExternalOESTextureID();
        mCameraTexure = new SurfaceTexture(mCameraTextureId);
        mCameraTexure.setOnFrameAvailableListener(this);
        new CameraHelper(cameraView.getContext(), lifecycleOwner, mCameraTexure);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        cameraFilter = new CameraFilter(context);
        screenFilter = new ScreenFilter(context);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        screenFilter.setSize(width, height);
        cameraFilter.setSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mCameraTexure != null) {
            mCameraTexure.updateTexImage();
            mCameraTexure.getTransformMatrix(mtx);
            cameraFilter.setTransformMatrix(mtx);
            int id = cameraFilter.onDraw(mCameraTextureId);
            screenFilter.onDraw(id);
        }

    }

    public void onSurfaceDestroyed() {
        cameraFilter.release();
        screenFilter.release();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        cameraView.requestRender();
    }
}
