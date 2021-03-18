package com.zq.opencv.opengl.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.zq.opencv.R;

/**
 * 摄像头过滤器
 */
public class CameraFilter extends AbstractFboFilter {

    private float[] mtx;
    private int vMatrix;


    public CameraFilter(Context context) {
        super(context, R.raw.camera_vert, R.raw.camera_frag);
    }

    @Override
    public void initGL(Context context, int vertexShaderId, int fragmentShaderId) {
        super.initGL(context, vertexShaderId, fragmentShaderId);
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix");
    }


    @Override
    public void beforeDraw() {
        super.beforeDraw();
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0);
    }

    public void setTransformMatrix(float[] mtx) {
        this.mtx = mtx;
    }

}
