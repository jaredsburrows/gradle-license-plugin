plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.dokka)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.plugin.publish)
  alias(libs.plugins.versions)
  alias(libs.plugins.license)
  id("java-gradle-plugin")
  id("java-library")
  id("groovy")
}

group = property("GROUP") as String
version = property("VERSION_NAME") as String

// Capture the classpaths at configuration time so the task is configuration-cache safe
// (project state such as sourceSets must not be accessed during task execution).
val mainRuntimeClasspath = sourceSets.main.get().runtimeClasspath
val testRuntimeClasspath = sourceSets.test.get().runtimeClasspath
val createClasspathManifest =
  tasks.register("createClasspathManifest") {
    val mainClasspath = mainRuntimeClasspath
    val testClasspath = testRuntimeClasspath
    val outputDir =
      project.layout.buildDirectory
        .file(name)
        .get()
        .asFile

    // Only the main runtime classpath is declared as an input; the test runtime classpath
    // contains files(createClasspathManifest) (this task's own output) and would otherwise
    // create a circular dependency.
    inputs.files(mainClasspath)
    outputs.dir(outputDir)

    doLast {
      outputDir.mkdirs()
      // Combine both main and test plugin classpaths
      val set = HashSet<String>()
      mainClasspath.files.forEach {
        set.add(it.path)
      }
      testClasspath.files.forEach {
        set.add(it.path)
      }
      val list = ArrayList<String>(set)
      File(outputDir, "plugin-classpath.txt").writeText(list.joinToString("\n"))
      // Main-only classpath (plugin + its runtime deps, no AGP, no test frameworks) for the
      // version-matrix tests, which inject their own AGP version on top.
      File(outputDir, "plugin-classpath-main.txt").writeText(
        mainClasspath.files.map { it.path }.joinToString("\n"),
      )
    }
  }

dependencies {
  compileOnly(gradleApi())
  compileOnly(libs.android.plugin)

  implementation(platform(libs.kotlin.bom))
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.html)
  implementation(libs.moshi)
  implementation(libs.maven.model)

  testRuntimeOnly(files(createClasspathManifest))
  testRuntimeOnly(libs.android.plugin)
  testRuntimeOnly(libs.junit.platform.launcher)

  testImplementation(localGroovy())
  testImplementation(gradleTestKit())
  testImplementation(libs.spock) { exclude(module = "groovy-all") } // Use localGroovy()
  testImplementation(libs.xmlunit)
  testImplementation(libs.commons)
}

gradlePlugin {
  website = property("POM_URL") as String
  vcsUrl = property("POM_SCM_URL") as String
  plugins {
    create("licensePlugin") {
      id = property("PLUGIN_NAME") as String
      implementationClass = property("PLUGIN_NAME_CLASS") as String
      displayName = property("POM_NAME") as String
      description = property("POM_DESCRIPTION") as String
      tags.set(listOf("license"))
    }
  }
}
