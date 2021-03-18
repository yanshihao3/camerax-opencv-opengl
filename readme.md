使用opencv +camerax 实现人脸检测
以及camerax 的简单使用
 camerax 输出到surfaceview 以及textureview中
 camerax 输出到opengl中
存在问题用最新版camerax+opengl 显示摄像头图像不清晰。
不知道代码问题，还是什么问题。有大佬可以解决一下

    implementation "androidx.camera:camera-core:1.0.0-alpha05"
    implementation "androidx.camera:camera-camera2:1.0.0-alpha05"


    这个版本可以在preview中直接获取SurfaceTexture对象，
    preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
                @Override
                public void onUpdated(Preview.PreviewOutput output) {
                    SurfaceTexture surfaceTexture = output.getSurfaceTexture();
                }
            });

    新版中 MyGlSurfaceProvider
    class MyGlSurfaceProvider implements Preview.SurfaceProvider {
                  // This executor must have also been used with Preview.setSurfaceProvider() to
                  // ensure onSurfaceRequested() is called on our GL thread.
                  Executor mGlExecutor;

                  @Override
                  public void onSurfaceRequested(@NonNull SurfaceRequest request) {
                      // If our GL thread/context is shutting down. Signal we will not fulfill
                      // the request.
                      if (isShuttingDown()) {
                          request.willNotProvideSurface();
                          return;
                      }

                      // Create the surface and attempt to provide it to the camera.
                      Surface surface = resetGlInputSurface(request.getResolution());

                      // Provide the surface and wait for the result to clean up the surface.
                      request.provideSurface(surface, mGlExecutor, (result) -> {
                          // In all cases (even errors), we can clean up the state. As an
                          // optimization, we could also optionally check for REQUEST_CANCELLED
                          // since we may be able to reuse the surface on subsequent surface requests.
                          closeGlInputSurface(surface);
                      });
                  }
              }

（使用以前版本没有发现这个问题）