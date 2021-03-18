package com.zq.opencv

import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * opencv 实现人脸检测
 */
class MainActivity : AppCompatActivity(), ImageAnalysis.Analyzer, SurfaceHolder.Callback {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var faceTracker: FaceTracker
    private var path = ""
    private val mSurfaceView: SurfaceView by lazy {
        findViewById(R.id.surfaceView)
    }

    private var cuurentCamera = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        copy()
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
        findViewById<Button>(R.id.toggleCamera).setOnClickListener {
            toggleCamera()
        }
        faceTracker = FaceTracker(path)
        faceTracker.start()
        mSurfaceView.holder.addCallback(this)
    }

    /**
     * 切换摄像头
     */
    private fun toggleCamera() {

        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            cuurentCamera = 1
            CameraSelector.DEFAULT_BACK_CAMERA

        } else {
            cuurentCamera = 0
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        imageAnalyzer = ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, this)
            }
        cameraProvider.unbindAll()
        // Bind use cases to camera
        cameraProvider.bindToLifecycle(
            this, cameraSelector, imageAnalyzer
        )
    }

    /**
     * 准备摄像头
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            cameraSelector =
                CameraSelector.DEFAULT_FRONT_CAMERA

            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, this)
                }
            try {
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalyzer
                )

            } catch (exc: Exception) {
            }


        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 拷贝文件
     */
    private fun copy() {
        path = "${getExternalFilesDir("Caches")}/lbpcascade_frontalface.xml"
        AssetsUtils.copyOneFileFromAssetsToSD(
            this,
            "lbpcascade_frontalface.xml",
            path,
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    //获取图像信息
    override fun analyze(image: ImageProxy) {
        Log.e("TAG", "analyze: ")
        val bytes = ImageUtils.getBytes(image)
        faceTracker.detect(
            bytes,
            image.width,
            image.height,
            image.imageInfo.rotationDegrees,
            cuurentCamera
        )
        image.close()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        faceTracker.setSurface(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        faceTracker.setSurface(null)

    }

}