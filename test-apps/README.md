# Test Apps

Three Jetpack Compose applications that apply `com.jaredsburrows.license` and display the licenses
of their own dependencies. They are a separate Gradle build that includes the plugin build
(`pluginManagement { includeBuild('..') }`), so they always run against the plugin sources in this
repository rather than a published version.

| Module | Report | Screen |
|---|---|---|
| `test-app-compose-html` | `open_source_licenses.html` | The generated HTML in a `WebView` dialog |
| `test-app-compose-json` | `open_source_licenses.json` | A Compose list linking out to each license URL |
| `test-app-compose-fulljson` | `open_source_licenses.full.json` | A custom Compose license screen rendering the bundled license text offline |

Every app enables exactly one report and copies it into `src/main/assets`, and wires the report task
into `merge<Variant>Assets` so the packaged asset is always up to date.

## Test

Each app has Robolectric unit tests that run against the report `licenseDebugReport` just generated
into its assets, so a change to a report format fails here instead of at runtime. They cover both the
parsing and, with Jetpack Compose UI tests, what the screens actually display.

```console
./gradlew -p test-apps test
```

## Build

```console
./gradlew -p test-apps build
```

## Install

```console
./gradlew -p test-apps :test-app-compose-fulljson:installDebug
```

## Regenerate a report only

```console
./gradlew -p test-apps :test-app-compose-fulljson:licenseDebugReport
```
