pluginManagement {
  // Build the plugin from this repository so the test apps always exercise the local sources.
  includeBuild("..")

  repositories {
    gradlePluginPortal()
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
  }
}

plugins {
  id("com.gradle.develocity") version "4.5.0"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

  repositories {
    gradlePluginPortal()
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
  }
}

develocity {
  buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
    val isCI = System.getenv("CI") != null
    publishing.onlyIf { isCI }
  }
}

rootProject.name = "test-apps"

include(":test-app-compose-html")
include(":test-app-compose-json")
include(":test-app-compose-fulljson")
