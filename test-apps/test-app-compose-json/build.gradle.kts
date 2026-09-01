plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ktlint)
  // No version: the plugin comes from the build included in settings.gradle.kts.
  id("com.jaredsburrows.license")
}

android {
  namespace = "com.jaredsburrows.license.testapp.json"
  compileSdk =
    libs.versions.targetSdk
      .get()
      .toInt()

  defaultConfig {
    applicationId = "com.jaredsburrows.license.testapp.json"
    minSdk =
      libs.versions.minSdk
        .get()
        .toInt()
    targetSdk =
      libs.versions.targetSdk
        .get()
        .toInt()
    versionCode = 1
    versionName = "1.0"
  }

  buildFeatures {
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.sourceCompatibility.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.targetCompatibility.get())
  }

  testOptions {
    unitTests {
      // Robolectric needs the merged resources and the generated report in the assets.
      isIncludeAndroidResources = true
    }
  }
}

licenseReport {
  // Only the JSON report, copied to src/<variant>/assets/open_source_licenses.json.
  generateCsvReport = false
  generateHtmlReport = false
  generateJsonReport = true
  generateJsonFullReport = false
  generateTextReport = false

  copyHtmlReportToAssets = false
  copyJsonReportToAssets = true
}

// Generate (and copy) the report before the assets are packaged so each variant always ships
// an up to date open_source_licenses.json.
listOf("Debug", "Release").forEach { variant ->
  tasks.matching { it.name == "merge${variant}Assets" }.configureEach {
    dependsOn("license${variant}Report")
  }
}

dependencies {
  implementation(platform(libs.compose.bom))
  implementation(libs.activity.compose)
  implementation(libs.compose.material3)
  implementation(libs.compose.ui)
  implementation(libs.core.ktx)
  implementation(libs.gif.drawable)

  testImplementation(platform(libs.compose.bom))
  testImplementation(libs.androidx.test.ext.junit)
  testImplementation(libs.compose.ui.test.junit4)
  testImplementation(libs.junit)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.robolectric)
  debugImplementation(libs.compose.ui.test.manifest)

  // See https://github.com/robolectric/robolectric/issues/11344
  constraints {
    testImplementation("androidx.test.espresso:espresso-core:3.7.0") {
      because("Force espresso-core 3.7.0+ for API 36 support (InputManager.getInstance was removed).")
    }
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0") {
      because("Force espresso-core 3.7.0+ for API 36 support (InputManager.getInstance was removed).")
    }
  }
}
