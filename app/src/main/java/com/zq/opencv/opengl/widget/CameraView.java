package com.zq.opencv.opengl.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;



/**
 * @program: opencv
 * @description:
 * @author: 闫世豪
 * @create: 2021-03-16 18:34
 **/
public class CameraView extends GLSurfaceView {
    private CameraRender renderer;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //使用
        setEGLContextClientVersion(2);
        //設置渲染回調接口
        renderer = new CameraRender(this);
        setRenderer(renderer);


        /**
         * 刷新方式：
         *     RENDERMODE_WHEN_DIRTY 手动刷新，調用requestRender();
         *     RENDERMODE_CONTINUOUSLY 自動刷新，大概16ms自動回調一次onDraw方法
         */
        //注意必须在setRenderer 后面。
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

    }

}
