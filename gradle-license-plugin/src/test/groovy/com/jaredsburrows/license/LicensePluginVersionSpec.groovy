package com.jaredsburrows.license

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

final class LicensePluginVersionSpec extends Specification {
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder()
  private int compileSdkVersion = 34
  private List<File> pluginClasspath
  private String classpathString
  private File buildFile
  private String reportFolder

  def 'setup'() {
    def pluginClasspathResource = getClass().classLoader.getResource('plugin-classpath.txt')
    if (pluginClasspathResource == null) {
      throw new IllegalStateException(
        'Did not find plugin classpath resource, run `testClasses` build task.')
    }

    pluginClasspath = pluginClasspathResource.readLines().collect { new File(it) }
    classpathString = pluginClasspath
      .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
      .collect { "'$it'" }
      .join(", ")
    buildFile = testProjectDir.newFile('build.gradle')
    // In case we're on Windows, fix the \s in the string containing the name
    reportFolder = "${testProjectDir.root.path.replaceAll('\\\\', '/')}/build/reports/licenses"
  }

  @Unroll
  def 'licenseReport using with gradle #gradleVersion'() {
    given:
    buildFile <<
      """
      plugins {
        id 'java'
        id 'com.jaredsburrows.license'
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseReport', '-s')
      .withPluginClasspath()
      .build()

    then:
    result.task(':licenseReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseReport.txt.")

    where:
    gradleVersion << [
      '7.3.3',
      '7.4.2',
      '7.5.1',
      '7.6.3',
      '8.0.2',
      '8.1.1',
      '8.2.1',
    ]
  }

  @Unroll
  def 'agp 3.6+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7+, 3.6.4
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
      ],
      [
        '3.6.4',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 4+ - agp version  #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7+, 4
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
      ],
      [
        '4.0.2',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 4.1+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7+, 4.1.0+
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
      ],
      [
        '4.1.3',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 4.2+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7+, 4.2
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
      ],
      [
        '4.2.2',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 7+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.0+, 7.0
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
        '7.5.1',
        '7.6.3',
        '8.0.2',
        '8.1.1',
        '8.2.1',
      ],
      [
        '7.0.4',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 7.1+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.2+, 7.1
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
        '7.5.1',
        '7.6.3',
        '8.0.2',
        '8.1.1',
        '8.2.1',
      ],
      [
        '7.1.3',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 7.2+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.3+, 7.2
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
        '7.5.1',
        '7.6.3',
        '8.0.2',
        '8.1.1',
        '8.2.1',
      ],
      [
        '7.2.2',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 7.3+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.3+, 7.2
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
        '7.5.1',
        '7.6.3',
        '8.0.2',
        '8.1.1',
        '8.2.1',
      ],
      [
        '7.3.1',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 7.4+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.3+, 7.2
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
        '7.5.1',
        '7.6.3',
        '8.0.2',
        '8.1.1',
        '8.2.1',
      ],
      [
        '7.4.2',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 8+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.3+, 7.2
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
        '7.5.1',
        '7.6.3',
        '8.0.2',
        '8.1.1',
        '8.2.1',
      ],
      [
        '8.0.2',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 8.1+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.3+, 7.2
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
        '7.5.1',
        '7.6.3',
        '8.0.2',
        '8.1.1',
        '8.2.1',
      ],
      [
        '8.1.4',
      ]
    ].combinations()
  }

  @Unroll
  def 'agp 8.2+ - agp version #agpVersion and gradle version #gradleVersion'() {
    given:
    buildFile <<
      """
      buildscript {
        repositories {
          mavenCentral()
          google()
        }

        dependencies {
          classpath "com.android.tools.build:gradle:${agpVersion}"
          classpath files($classpathString)
        }
      }

      apply plugin: 'com.android.application'
      apply plugin: 'com.jaredsburrows.license'

      android {
        compileSdkVersion $compileSdkVersion

        defaultConfig {
          applicationId 'com.example'
        }
      }
      """

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion as String)
      .withProjectDir(testProjectDir.root)
      .withArguments('licenseDebugReport', '-s')
      .build()

    then:
    result.task(':licenseDebugReport').outcome == SUCCESS
    result.output.find("Wrote CSV report to .*${reportFolder}/licenseDebugReport.csv.")
    result.output.find("Wrote HTML report to .*${reportFolder}/licenseDebugReport.html.")
    result.output.find("Wrote JSON report to .*${reportFolder}/licenseDebugReport.json.")
    result.output.find("Wrote Text report to .*${reportFolder}/licenseDebugReport.txt.")

    where:
    // https://docs.gradle.org/current/userguide/compatibility.html
    // https://developer.android.com/studio/releases/gradle-plugin
    // 7.3+, 7.2
    [gradleVersion, agpVersion] << [
      [
        '7.3.3',
        '7.4.2',
        '7.5.1',
        '7.6.3',
        '8.0.2',
        '8.1.1',
        '8.2.1',
      ],
      [
        '8.2.2',
      ]
    ].combinations()
  }
}
