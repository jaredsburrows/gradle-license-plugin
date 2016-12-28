# Gradle License Plugin

[![License](https://img.shields.io/badge/license-apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/jaredsburrows/gradle-license-plugin.svg?branch=master)](https://travis-ci.org/jaredsburrows/gradle-license-plugin)
[![Coverage Status](https://coveralls.io/repos/github/jaredsburrows/gradle-license-plugin/badge.svg?branch=master)](https://coveralls.io/github/jaredsburrows/gradle-license-plugin?branch=master)
[![Twitter Follow](https://img.shields.io/twitter/follow/jaredsburrows.svg?style=social)](https://twitter.com/jaredsburrows)

This plugin provides a task to generate a HTML license report based on the 
configuration. (eg. `licenseDebugReport` for all debug dependencies in an Android project).

Applying this to an Android or Java project will generate a the license 
file(`open_source_licenses.html`) in the `<project>/build/reports/licenses/`.

Also, for Android projects the license HTML file will be copied to `<project>/src/main/assets/`.

## Download

This plugin's latest release is available from [JFrog Bintray's JCenter repository](https://bintray.com/jaredsburrows/maven/gradle-license-plugin). You can
add it to your build script using the following configuration:

```groovy
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath "com.jaredsburrows:gradle-license-plugin:0.1.0"
  }
}

apply plugin: "com.jaredsburrows.license"
```

Snapshots of the development version are available in [JFrog Artifactory repository](https://oss.jfrog.org/webapp/#/builds/gradle-license-plugin).

```groovy
buildscript {
  repositories {
    maven { url "https://oss.jfrog.org/oss-snapshot-local" }
  }

  dependencies {
    classpath "com.jaredsburrows:gradle-license-plugin:0.2.0-SNAPSHOT"
  }
}

apply plugin: "com.jaredsburrows.license"
```

## Tasks

- **`license${variant}Report`** for Android
- **`licenseReport`** for Java

Generates a HTML report of the all open source licenses. (eg. `licenseDebugReport` for all debug dependencies in an Android project).

Example `build.gradle`:

```groovy
dependencies {
  compile "com.android.support:design:25.1.0"
  compile "pl.droidsonroids.gif:android-gif-drawable:1.2.1"

  debugCompile "com.squareup.leakcanary:leakcanary-android:1.4"
}
```

HTML:
```html
<html>
   <head>
      <style>body{font-family:sans-serif;}pre{background-color:#eeeeee;padding:1em;white-space:pre-wrap;}</style>
      <title>Open source licenses</title>
   </head>
   <body>
      <h3>Notice for libraries:</h3>
      <ul>
         <li><a href="#-989311426">Android GIF Drawable Library</a></li>
         <li><a href="#1288288048">Design</a></li>
         <li><a href="#1288288048">LeakCanary for Android</a></li>
      </ul>
      <h3><a name="-989311426"></a>The MIT License</h3>
      <pre>The MIT License, http://opensource.org/licenses/MIT</pre>
      <h3><a name="1288288048"></a>The Apache Software License, Version 2.0</h3>
      <pre>The Apache Software License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0.txt</pre>
   </body>
</html>
```

JSON:
```json
[
   {
      "project":"Android GIF Drawable Library",
      "license":"The MIT License",
      "license_url":"http://opensource.org/licenses/MIT"
   },
   {
      "project":"Design",
      "license":"The Apache Software License, Version 2.0",
      "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
   },
   {
      "project":"LeakCanary for Android",
      "license":"The Apache Software License, Version 2.0",
      "license_url":"http://www.apache.org/licenses/LICENSE-2.0.txt"
   }
]
```

## Developing

### Building
```bash
$ gradlew assemble
```

### Testing

The [Spock](http://spockframework.org/) tests run on the JVM.
```bash
$ gradlew test
    
```

### Reports

The [Jacoco](http://www.eclemma.org/jacoco/) plugin generates coverage reports based off the unit tests.
```bash
$ gradlew jacocoTestReport
```
