package test

class BaseAndroidSpecification extends BaseJavaSpecification {
  def COMPILE_SDK_VERSION = 27
  def BUILD_TOOLS_VERSION = "27.0.3"
  def APPLICATION_ID = "com.example"
  def SRC_FOLDER = "src"
  def MAIN_FOLDER = "main"
  def MANIFEST_FILE_PATH = "src/main/AndroidManifest.xml"
  def MANIFEST = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"$APPLICATION_ID\"/>"

  def "setup"() {
    // Make sure Android projects have a manifest
    testProjectDir.newFolder(SRC_FOLDER, MAIN_FOLDER)
    testProjectDir.newFile(MANIFEST_FILE_PATH) << MANIFEST
  }
}
