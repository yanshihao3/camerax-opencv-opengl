package com.zq.opencv.opengl.utils;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Size;
import android.view.Surface;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.zq.opencv.opengl.widget.CameraView;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 打开摄像头
 */
public class CameraHelper {

    public CameraHelper(Context context, LifecycleOwner lifecycleOwner, SurfaceTexture surfaceTexture) {
        Executor executor = Executors.newSingleThreadExecutor();
        Surface surface = new Surface(surfaceTexture);
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(context);
        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder()
                        .setTargetResolution(new Size(1080, 1920))
                        .build();
                preview.setSurfaceProvider(request -> {
                    request.provideSurface(surface, executor, result -> {
                        surfaceTexture.release();
                    });

                });
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview);

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));

    }


}
