package test

import spock.lang.Specification

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
class BaseSpecification extends Specification {
  static def COMPILE_SDK_VERSION = 26
  static def BUILD_TOOLS_VERSION = "26.0.2"
  static def APPLICATION_ID = "com.example"
  static def SUPPORT_VERSION = "26.1.0"
  // Test fixture that emulates a google()/mavenCentral()/jcenter()/"https://plugins.gradle.org/m2/"
  static def TEST_MAVEN_REPOSITORY = this.getResource("/maven").toURI()
  // Test fixture that emulates a local android sdk
  static def TEST_ANDROID_SDK = this.getResource("/android-sdk").toURI()
  static def PROJECT_SOURCE_DIR = "src/test/resources/project"
  static def MANIFEST_FILE_PATH = "src/main/AndroidManifest.xml"
  static def MANIFEST = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"$APPLICATION_ID\"/>"
}
