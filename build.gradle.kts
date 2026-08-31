import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.ktlint)
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.plugin.publish) apply false
  alias(libs.plugins.versions)
  alias(libs.plugins.license)
  `java-gradle-plugin`
  `java-library`
  groovy
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
  // Pin the Java/Kotlin toolchain to JDK 17 (AGP 9 / Gradle 9 minimum) so compilation, tests and
  // IDE analysis use a consistent JDK regardless of the JDK used to launch Gradle.
  pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    kotlin {
      jvmToolchain(17)
    }
  }

  tasks.withType<Jar>().configureEach {
    val dateFile =
      layout.buildDirectory
        .file("jar-manifest-date.txt")
        .get()
        .asFile
    if (!dateFile.exists()) {
      val date =
        DateTimeFormatter
          .ofPattern("EEE MMM dd HH:mm:ss zzz yyyy")
          .format(ZonedDateTime.now())
      dateFile.parentFile.mkdirs()
      dateFile.writeText(date.trim())
    }

    manifest {
      attributes(
        "Created-By" to project.property("POM_DEVELOPER_NAME") as String,
        "Implementation-Title" to project.property("POM_NAME") as String,
        "Implementation-Version" to project.property("VERSION_NAME") as String,
        "Implementation-Vendor" to project.property("POM_DEVELOPER_NAME") as String,
        "Built-By" to System.getProperty("user.name"),
        "Built-Date" to dateFile.readText().trim(),
        "Built-JDK" to System.getProperty("java.version"),
        "Built-Gradle" to project.gradle.gradleVersion,
      )
    }
  }

  tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
      languageVersion.set(KotlinVersion.KOTLIN_2_4)
      apiVersion.set(KotlinVersion.KOTLIN_2_4)
      freeCompilerArgs.add("-progressive")
      freeCompilerArgs.add("-Xjsr305=strict")
      freeCompilerArgs.add("-Xemit-jvm-type-annotations")
      freeCompilerArgs.add("-Xassertions=jvm")
      freeCompilerArgs.add("-jvm-default=enable")
    }
  }

  tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()

    options.apply {
      compilerArgs.add("-Xlint:all")
      compilerArgs.add("-Xlint:-options")
      encoding = "utf-8"
      isFork = true
    }
  }

  tasks.withType<GroovyCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()

    options.apply {
      compilerArgs.add("-Xlint:all")
      compilerArgs.add("-Xlint:-options")
      encoding = "utf-8"
      isFork = true
    }
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform() // Ensure JUnit Platform is used if you are using JUnit 5 or Spock 2.x

    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
      showCauses = true
      showExceptions = true
      showStackTraces = true

      // Check if running on CI and set events accordingly
      events =
        if (System.getenv("CI") != null) {
          TestLogEvent.entries.toSet()
        } else {
          setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        }
    }

    val maxWorkerCount = project.gradle.startParameter.maxWorkerCount
    maxParallelForks = if (maxWorkerCount < 2) 1 else maxWorkerCount / 2
  }
}
