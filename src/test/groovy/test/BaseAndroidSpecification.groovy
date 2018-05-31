package test

import com.android.build.gradle.internal.SdkHandler
import groovy.json.StringEscapeUtils

class BaseAndroidSpecification extends BaseJavaSpecification {
  def COMPILE_SDK_VERSION = 26
  def BUILD_TOOLS_VERSION = "26.0.2"
  def APPLICATION_ID = "com.example"
  // Test fixture that emulates a local android sdk
  def TEST_ANDROID_SDK = getClass().getResource("/android-sdk").toURI()
  def SRC_FOLDER = "src"
  def MAIN_FOLDER = "main"
  def MANIFEST_FILE_PATH = "src/main/AndroidManifest.xml"
  def MANIFEST = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"$APPLICATION_ID\"/>"

  def "setup"() {
    // Make sure Android projects have a manifest
    testProjectDir.newFolder(SRC_FOLDER, MAIN_FOLDER)
    testProjectDir.newFile(MANIFEST_FILE_PATH) << MANIFEST

    // Set mock test sdk, we only need to test the plugins tasks
    def testAndroidSdk = new File(TEST_ANDROID_SDK)
    SdkHandler.sTestSdkFolder = testAndroidSdk
    new File (testProjectDir.root, "local.properties") << "sdk.dir=${StringEscapeUtils.escapeJava(testAndroidSdk.path)}"
  }
}
