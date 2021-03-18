package com.zq.opencv

import android.view.Surface

/**
 * @program: opencv
 *
 * @description: 人脸检测
 *
 * @author: 闫世豪
 *
 * @create: 2021-03-16 15:30
 **/
class FaceTracker(model: String) {
    private var mNativeObj: Long = 0

    init {
        mNativeObj = nativeCreateObject(model)

    }

    fun setSurface(surface: Surface?) {
        nativeSetSurface(mNativeObj, surface)
    }

    fun release() {
        nativeDestroyObject(mNativeObj)
        mNativeObj = 0
    }

    fun start() {
        nativeStart(mNativeObj)
    }

    fun stop() {
        nativeStop(mNativeObj)
    }

    fun detect(
        inputImage: ByteArray,
        width: Int,
        height: Int,
        rotationDegrees: Int,
        lensFacing: Int
    ) {
        nativeDetect(mNativeObj, inputImage, width, height, rotationDegrees, lensFacing)
    }

    private external fun nativeCreateObject(model: String): Long

    private external fun nativeDestroyObject(thiz: Long)

    private external fun nativeSetSurface(thiz: Long, surface: Surface?)

    private external fun nativeStart(thiz: Long)

    private external fun nativeStop(thiz: Long)

    private external fun nativeDetect(
        thiz: Long,
        inputImage: ByteArray,
        width: Int,
        height: Int,
        rotationDegrees: Int,
        lensFacing: Int
    )

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}