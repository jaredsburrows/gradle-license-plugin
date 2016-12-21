# License Gradle Plugin

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![TravisCI](https://img.shields.io/travis/jaredsburrows/gradle-license-plugin/master.svg?label=OSX%20Build)](https://travis-ci.org/jaredsburrows/gradle-license-plugin)
[![Coveralls Code Coverage](https://img.shields.io/coveralls/jaredsburrows/gradle-license-plugin/master.svg?label=Code%20Coverage)](https://coveralls.io/github/jaredsburrows/gradle-license-plugin?branch=master)
[![Twitter Follow](https://img.shields.io/twitter/follow/jaredsburrows.svg?style=social)](https://twitter.com/jaredsburrows)

This plugin provides a task to generate a HTML license report based on the 
configuration variant. (eg. `licenseDebugReport` for all debug dependencies).

Applying this to an Android App or Library project will generate a the license 
file(`open_source_licenses.html`) and copy it to `<project>/src/main/assets/`.

## Usage

This plugin is available from [Bintray's JCenter repository](https://bintray.com/jaredsburrows/maven/gradle-license-plugin). You can
add it to your build script using the following configuration:

### `buildscript` block:
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

## Tasks

**`license${variant}Report`**

Generates a HTML report of the all open source licenses. (eg. `licenseDebugReport` for all debug dependencies).

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
