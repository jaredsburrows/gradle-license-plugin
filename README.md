# Gradle License Plugin

[![License](https://img.shields.io/badge/license-apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build](https://github.com/jaredsburrows/gradle-license-plugin/workflows/build/badge.svg)](https://github.com/jaredsburrows/gradle-license-plugin/actions)
[![Twitter Follow](https://img.shields.io/twitter/follow/jaredsburrows.svg?style=social)](https://twitter.com/jaredsburrows)

This plugin provides a task to generate a HTML license report based on the 
configuration. (eg. `licenseDebugReport` for all debug dependencies in an Android project).

Applying this to an Android or Java project will generate the license 
file(`open_source_licenses.html`) in the `<project>/build/reports/licenses/`.

Also, for Android projects the license HTML file will be copied to `<project>/src/main/assets/`.

## Download

**Release:**
```groovy
buildscript {
  repositories {
    mavenCentral()
    google()
  }

  dependencies {
    classpath 'com.jaredsburrows:gradle-license-plugin:0.8.90'
  }
}

apply plugin: 'com.android.application' // or 'java-library'
apply plugin: 'com.jaredsburrows.license'
```
Release versions are available in the [Sonatype's release repository](https://repo1.maven.org/maven2/com/jaredsburrows/gradle-license-plugin/).

**Snapshot:**
```groovy
buildscript {
  repositories {
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots' }
    google()
  }

  dependencies {
    classpath 'com.jaredsburrows:gradle-license-plugin:0.8.91-SNAPSHOT'
  }
}

apply plugin: 'com.android.application' // or 'java-library'
apply plugin: 'com.jaredsburrows.license'
```
Snapshot versions are available in the [Sonatype's snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/com/jaredsburrows/gradle-license-plugin/).

## Tasks

- **`license${variant}Report`** for Android
- **`licenseReport`** for Java

Generates a HTML report of the all open source licenses. (eg. `licenseDebugReport` for all debug dependencies in an Android project).

Example `build.gradle`:

```groovy
dependencies {
  implementation 'com.android.support:design:26.1.0'
  implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.3'
  implementation 'wsdl4j:wsdl4j:1.5.1' // Very old library with no license info available
}
```

**CSV (minimized example):**
```csv
project,description,version,developers,url,year,licenses,license urls,dependency
Design,null,26.1.0,null,null,null,The Apache Software License,http://www.apache.org/licenses/LICENSE-2.0.txt,com.android.support:design:26.1.0
```

**HTML (minimized example):**
```html
<html>
  <head>
    <style>body { font-family: sans-serif } pre { background-color: #eeeeee; padding: 1em; white-space: pre-wrap; display: inline-block }</style>
    <title>Open source licenses</title>
  </head>
  <body>
    <h3>Notice for packages:</h3>
    <ul>
      <li>
        <a href="#0">WSDL4J (1.5.1)</a>
        <dl>
          <dt>Copyright &copy; 20xx The original author or authors</dt>
        </dl>
      </li>
      <a name="0"></a>
        <pre>No license found</pre>
      <br>
      <hr>
      <li><a href="#1783810846">Android GIF Drawable Library (1.2.3)</a>
        <dl>
          <dt>Copyright &copy; 20xx Karol Wrótniak</dt>
        </dl>
      </li>
      <a name="1783810846"></a>
        <pre>mit.txt here</pre>
      <br>
      <hr>
      <li><a href="#1934118923">Design (26.1.0)</a>
        <dl>
          <dt>Copyright &copy; 20xx The original author or authors</dt>
        </dl>
      </li>
      <a name="1934118923"></a>
        <pre>apache-2.0.txt here</pre>
      <br>
      <hr>
    </ul>
  </body>
</html>
```
Note, if no license information is found in the POM for a project, "No License Found" will be used. 
Those will be listed first.
Other missing information is provided as default values that can be corrected from other sources.
Projects are grouped by license name and the license text is only provided once. 
Projects with multiple licenses are grouped as if those licenses were a single combined license.

**JSON (full example):**
```json
[
  {
    "project": "Android GIF Drawable Library",
    "description": "Views and Drawable for displaying animated GIFs for Android",
    "version": "1.2.3",
    "developers": [
      "Karol Wrótniak"
    ],
    "url": "https://github.com/koral--/android-gif-drawable",
    "year": null,
    "licenses": [
      {
        "license": "The MIT License",
        "license_url": "http://opensource.org/licenses/MIT"
      }
    ],
    "dependency": "pl.droidsonroids.gif:android-gif-drawable:1.2.3"
  },
  {
    "project": "Design",
    "description": null,
    "version": "26.1.0",
    "developers": [],
    "url": null,
    "year": null,
    "licenses": [
      {
        "license": "The Apache Software License",
        "license_url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
      }
    ],
    "dependency": "com.android.support:design:26.1.0"
  },
  {
    "project": "WSDL4J",
    "description": "Java stub generator for WSDL",
    "version": "1.5.1",
    "developers": [],
    "url": "http://sf.net/projects/wsdl4j",
    "year": null,
    "licenses": [],
    "dependency": "wsdl4j:wsdl4j:1.5.1"
  }
]
```

Note, if no license information is found for a component, the `licenses` element in the JSON output will be an empty array.

## Configuration
The plugin can be configured to generate specific reports and automatically copy the reports to the assets directory (Android projects only). The default behaviours are: 
- Java projects: Generate HTML, JSON and CSV reports.
- Android projects: Generate HTML, JSON and CSV reports, and copy the HTML report to the assets directory.

To override the defaults, add the `licenseReport` configuration closure to the build script.

```groovy
apply plugin: "com.jaredsburrows.license"

licenseReport {
  generateCsvReport = false
  generateHtmlReport = false
  generateJsonReport = true
  
  // These options are ignored for Java projects
  copyHtmlReportToAssets = false
  copyHtmlReportToAssets = true
  copyJsonReportToAssets = false
}
```

The `copyHtmlReportToAssets` option in the above example would have no effect since the HTML report is disabled.

## Usage

### Create an open source dialog
```java
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public final class OpenSourceLicensesDialog extends DialogFragment {

  public OpenSourceLicensesDialog() {
  }

  public static void showLicenses(Activity activity) {
    FragmentManager fragmentManager = activity.getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    Fragment previousFragment = fragmentManager.findFragmentByTag("dialog_licenses");
    if (previousFragment != null) {
      fragmentTransaction.remove(previousFragment);
    }
    fragmentTransaction.addToBackStack(null);

    show(fragmentTransaction, "dialog_licenses");
  }

  @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    WebView webView = new WebView(requireActivity());
    webView.loadUrl("file:///android_asset/open_source_licenses.html");

    return new AlertDialog.Builder(requireActivity())
      .setTitle("Open Source Licenses")
      .setView(webView)
      .setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
      .create();
  }
}
```

### How to use it
```java
new OpenSourceLicensesDialog().showLicenses(fragmentTransaction, "dialog_licenses");
```

Source: https://github.com/google/iosched/blob/2531cbdbe27e5795eb78bf47d27e8c1be494aad4/android/src/main/java/com/google/samples/apps/iosched/util/AboutUtils.java#L52

<img src="https://www.bignerdranch.com/assets/img/blog/2015/07/screenshot-gmail.png" />

Source: https://www.bignerdranch.com/blog/open-source-licenses-and-android/

## License

    Copyright (C) 2016 Jared Burrows

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
