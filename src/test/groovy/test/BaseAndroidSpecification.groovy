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
  static def SRC_FOLDER = "src"
  static def MAIN_FOLDER = "main"
  static def MANIFEST_FILE_PATH = "src/main/AndroidManifest.xml"
  static def MANIFEST = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"$APPLICATION_ID\"/>"

  def "setup"() {
    // Make sure Android projects have a manifest
    testProjectDir.newFolder(SRC_FOLDER, MAIN_FOLDER)
    testProjectDir.newFile(MANIFEST_FILE_PATH) << MANIFEST

    // Set mock test sdk, we only need to test the plugins tasks
    SdkHandler.sTestSdkFolder = project.file(TEST_ANDROID_SDK)
  }
}
