package test

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BaseJavaSpecification extends Specification {
  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
  def buildFile
  // Test fixture that emulates a google()/mavenCentral()/jcenter()/"https://plugins.gradle.org/m2/"
  def TEST_MAVEN_REPOSITORY = getClass().getResource("/maven").toURI()
  def SUPPORT_VERSION = "26.1.0"
  // Maven repo - "file://${System.env.ANDROID_HOME}/extras/google/m2repository"
  def FIREBASE_CORE = "com.google.firebase:firebase-core:10.0.1"
  // Maven repo - "file://${System.env.ANDROID_HOME}/extras/android/m2repository"
  def APPCOMPAT_V7 = "com.android.support:appcompat-v7:$SUPPORT_VERSION"
  def DESIGN = "com.android.support:design:$SUPPORT_VERSION"
  def SUPPORT_ANNOTATIONS = "com.android.support:support-annotations:$SUPPORT_VERSION"
  def SUPPORT_V4 = "com.android.support:support-v4:$SUPPORT_VERSION"
  // Others
  def ANDROID_GIF_DRAWABLE = "pl.droidsonroids.gif:android-gif-drawable:1.2.3"
  def FAKE_DEPENDENCY = "group:name:1.0.0"                               // Single license
  def FAKE_DEPENDENCY2 = "group:name2:1.0.0"                             // Multiple license
  def FAKE_DEPENDENCY3 = "group:name3:1.0.0"                             // Bad license
  def CHILD_DEPENDENCY = "group:child:1.0.0"                             // Child license -> Parent license
  def RETROFIT_DEPENDENCY = "com.squareup.retrofit2:retrofit:2.3.0"      // Child license -> Parent license

  // Projects
  Project project
  Project subproject

  def "setup"() {
    buildFile = testProjectDir.newFile("build.gradle")

    // Setup project
    project = ProjectBuilder.builder()
      .withProjectDir(testProjectDir.root)
      .withName("project")
      .build()
    project.repositories {
      maven { url TEST_MAVEN_REPOSITORY }
    }

    // Setup subproject
    subproject = ProjectBuilder.builder()
      .withParent(project)
      .withName("subproject")
      .build()
    subproject.repositories {
      maven { url TEST_MAVEN_REPOSITORY }
    }
  }

  def getLicenseText(def fileName) {
    getClass().getResource("/license/${fileName}").text
  }
}
