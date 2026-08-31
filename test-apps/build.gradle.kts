import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  // No version: the plugin comes from the build included in settings.gradle.kts.
  id("com.jaredsburrows.license") apply false
  alias(libs.plugins.ktlint)
  idea
}

tasks.withType<Wrapper>().configureEach {
  distributionType = Wrapper.DistributionType.ALL
}

idea {
  module {
    isDownloadSources = true
    isDownloadJavadoc = true
  }
}

subprojects {
  tasks.withType<Test>().configureEach {
    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
      showCauses = true
      showExceptions = true
      showStackTraces = true
      events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }

    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)

    // https://robolectric.org/getting-started/#running-with-java-17-and-higher
    jvmArgs(
      "--add-opens=java.base/java.lang=ALL-UNNAMED",
      "--add-opens=java.base/java.util=ALL-UNNAMED",
      "--add-opens=java.base/java.io=ALL-UNNAMED",
      "--add-opens=java.base/java.net=ALL-UNNAMED",
      "--add-opens=java.base/java.security=ALL-UNNAMED",
      "--add-opens=java.base/java.text=ALL-UNNAMED",
      "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
      "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
      "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
    )
  }
}
