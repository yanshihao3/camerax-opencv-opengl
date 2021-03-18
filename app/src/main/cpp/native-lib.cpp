#include <jni.h>
#include <string>
#include "opencv2/opencv.hpp"
#include <android/log.h>
#include <android/native_window_jni.h>
#include <opencv2/imgproc/types_c.h>

#define LOG_TAG "FaceDetection/DetectionBasedTracker"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

using namespace cv;
using namespace std;


class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {
        LOGD("CascadeDetectorAdapter::Detect::Detect");
        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {
        LOGD("CascadeDetectorAdapter::Detect: begin");
        LOGD("CascadeDetectorAdapter::Detect: scaleFactor=%.2f, minNeighbours=%d, minObjSize=(%dx%d), maxObjSize=(%dx%d)",
             scaleFactor, minNeighbours, minObjSize.width, minObjSize.height, maxObjSize.width,
             maxObjSize.height);
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                   maxObjSize);
        LOGD("CascadeDetectorAdapter::Detect: end");
    }

    virtual ~CascadeDetectorAdapter() {
        LOGD("CascadeDetectorAdapter::Detect::~Detect");
    }

private:
    CascadeDetectorAdapter();

    cv::Ptr<cv::CascadeClassifier> Detector;
};

class FaceTracker {
public:
    FaceTracker(Ptr<DetectionBasedTracker> tracker) : tracker(tracker) {
        pthread_mutex_init(&mutex, 0);
    };

    ~FaceTracker() {
        pthread_mutex_destroy(&mutex);
        if (window) {
            ANativeWindow_release(window);
            this->window = 0;
        }
    }

public:
    void setANativeWindow(ANativeWindow *window) {
        pthread_mutex_lock(&mutex);
        // 替换问题
        if (this->window) {
            ANativeWindow_release(this->window);
            this->window = 0;
        }
        this->window = window;
        pthread_mutex_unlock(&mutex);
    }

    void draw(Mat img) {
        pthread_mutex_lock(&mutex);
        do {
            if (!window) {
                break;
            }
            ANativeWindow_setBuffersGeometry(window, img.cols, img.rows, WINDOW_FORMAT_RGBA_8888);
            ANativeWindow_Buffer buffer;
            if (ANativeWindow_lock(window, &buffer, 0)) {
                ANativeWindow_release(window);
                window = 0;
                break;
            }

            uint8_t *dstData = static_cast<uint8_t *>(buffer.bits);
            int dstlineSize = buffer.stride * 4;

            uint8_t *srcData = img.data;
            int srclineSize = img.cols * 4;
            for (int i = 0; i < buffer.height; ++i) {
                memcpy(dstData + i * dstlineSize, srcData + i * srclineSize, srclineSize);
            }
            ANativeWindow_unlockAndPost(window);
        } while (0);


        pthread_mutex_unlock(&mutex);
    }

public:
    Ptr<DetectionBasedTracker> tracker;
    pthread_mutex_t mutex;
    ANativeWindow *window = 0;


};

extern "C"
JNIEXPORT jlong JNICALL
Java_com_zq_opencv_FaceTracker_nativeCreateObject(JNIEnv *env, jobject thiz, jstring model) {
    const char *jnamestr = env->GetStringUTFChars(model, NULL);
    string stdFileName(jnamestr);
    jlong result = 0;
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(stdFileName));
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(stdFileName));
    DetectionBasedTracker::Parameters DetectorParams;
    FaceTracker *tracker = new FaceTracker(
            makePtr<DetectionBasedTracker>(DetectionBasedTracker(mainDetector, trackingDetector,
                                                                 DetectorParams)));
    env->ReleaseStringUTFChars(model, jnamestr);
    result = (jlong) tracker;
    return result;

}



extern "C"
JNIEXPORT void JNICALL
Java_com_zq_opencv_FaceTracker_nativeSetSurface(JNIEnv *env, jobject thiz, jlong thiz_1,
                                                jobject surface) {
    if (thiz_1 != 0) {
        FaceTracker *tracker = reinterpret_cast<FaceTracker *>(thiz_1);
        if (!surface) {
            tracker->setANativeWindow(0);
            return;
        }
        tracker->setANativeWindow(ANativeWindow_fromSurface(env, surface));
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zq_opencv_FaceTracker_nativeDetect(JNIEnv *env, jobject thiz, jlong thiz_1,
                                            jbyteArray input_image, jint width, jint height,
                                            jint rotationDegrees, jobject lensFacing) {
    jbyte *inputImage = env->GetByteArrayElements(input_image, NULL);
    Mat src(height * 3 / 2, width, CV_8UC1, inputImage);
    FaceTracker *tracker = reinterpret_cast<FaceTracker *>(thiz_1);
    // 转为RGBA
    cvtColor(src, src, CV_YUV2RGBA_I420);

    //旋转
    if (rotationDegrees == 90) {
        rotate(src, src, ROTATE_90_CLOCKWISE);
    } else if (rotationDegrees == 270) {
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);
    }
    if (lensFacing == 0) {
        //镜像问题，可以使用此方法进行垂直翻转
        flip(src, src, 1);
    }

    Mat gray;
    cvtColor(src, gray, CV_RGBA2GRAY);
    equalizeHist(gray, gray);

    tracker->tracker->process(gray);
    std::vector<Rect> faces;
    tracker->tracker->getObjects(faces);
    for (Rect face:faces) {
        //画矩形
        rectangle(src, face, Scalar(255, 0, 255));
    }
    tracker->draw(src);
    env->ReleaseByteArrayElements(input_image, inputImage, 0);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_zq_opencv_FaceTracker_nativeStart(JNIEnv *env, jobject thiz, jlong thiz_1) {
    if (thiz_1 != 0) {
        FaceTracker *tracker = reinterpret_cast<FaceTracker *>(thiz_1);
        tracker->tracker->run();
    }
}


extern "C"
JNIEXPORT void JNICALL
Java_com_zq_opencv_FaceTracker_nativeStop(JNIEnv *env, jobject thiz, jlong thiz_1) {
    if (thiz_1 != 0) {
        FaceTracker *tracker = reinterpret_cast<FaceTracker *>(thiz_1);
        tracker->tracker->stop();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zq_opencv_FaceTracker_nativeDestroyObject(JNIEnv *env, jobject thiz, jlong thiz_1) {
    if (thiz_1 != 0) {
        FaceTracker *tracker = reinterpret_cast<FaceTracker *>(thiz_1);
        tracker->tracker->stop();
        delete tracker;
    }
}
