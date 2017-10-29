package test

import com.android.build.gradle.internal.SdkHandler

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class BaseAndroidSpecification extends BaseJavaSpecification {
  static def COMPILE_SDK_VERSION = 26
  static def BUILD_TOOLS_VERSION = "26.0.2"
  static def APPLICATION_ID = "com.example"
  // Test fixture that emulates a local android sdk
  static def TEST_ANDROID_SDK = this.getResource("/android-sdk").toURI()

  def "setup"() {
    // Set mock test sdk, we only need to test the plugins tasks
    SdkHandler.sTestSdkFolder = project.file(TEST_ANDROID_SDK)
  }
}
