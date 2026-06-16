plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.dokka)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.maven.publish)
  alias(libs.plugins.plugin.publish)
  alias(libs.plugins.versions)
  alias(libs.plugins.license)
  id 'java-gradle-plugin'
  id 'java-library'
  id 'groovy'
}

group = GROUP
version = VERSION_NAME

// Capture the classpaths at configuration time so the task is configuration-cache safe
// (project state such as sourceSets must not be accessed during task execution).
def mainRuntimeClasspath = sourceSets.main.runtimeClasspath
def testRuntimeClasspath = sourceSets.test.runtimeClasspath
tasks.register('createClasspathManifest') {
  def outputDir = layout.buildDirectory.file(name).get().asFile

  // Only the main runtime classpath is declared as an input; the test runtime classpath
  // contains files(createClasspathManifest) (this task's own output) and would otherwise
  // create a circular dependency.
  inputs.files(mainRuntimeClasspath)
  outputs.dir outputDir

  doLast {
    outputDir.mkdirs()
    // Combine both main and test plugin classpaths
    def set = new HashSet<String>()
    mainRuntimeClasspath.files.forEach {
      set.add(it.path)
    }
    testRuntimeClasspath.files.forEach {
      set.add(it.path)
    }
    def list = new ArrayList<String>(set)
    new File(outputDir, 'plugin-classpath.txt').text = String.join('\n', list)
    // Main-only classpath (plugin + its runtime deps, no AGP, no test frameworks) for the
    // version-matrix tests, which inject their own AGP version on top.
    new File(outputDir, 'plugin-classpath-main.txt').text =
      String.join('\n', mainRuntimeClasspath.files.collect { it.path })
  }
}

dependencies {
  compileOnly gradleApi()
  compileOnly libs.android.plugin

  implementation platform(libs.kotlin.bom)
  implementation libs.kotlin.stdlib
  implementation libs.kotlinx.html
  implementation libs.moshi
  implementation libs.maven.model

  testRuntimeOnly files(createClasspathManifest)
  testRuntimeOnly libs.android.plugin
  testRuntimeOnly libs.junit.platform.launcher

  testImplementation localGroovy()
  testImplementation gradleTestKit()
  testImplementation libs.spock, { exclude module: 'groovy-all' } // Use localGroovy()
  testImplementation libs.xmlunit
  testImplementation libs.commons
}

gradlePlugin {
  website = POM_URL
  vcsUrl = POM_SCM_URL
  plugins {
    licensePlugin {
      id = PLUGIN_NAME
      implementationClass = PLUGIN_NAME_CLASS
      displayName = POM_NAME
      description = POM_DESCRIPTION
      tags.set(['license'])
    }
  }
}
