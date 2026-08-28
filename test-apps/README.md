# Test Apps

Three Jetpack Compose applications that apply `com.jaredsburrows.license` and display the licenses
of their own dependencies. They are a separate Gradle build that includes the plugin build
(`pluginManagement { includeBuild('..') }`), so they always run against the plugin sources in this
repository rather than a published version.

| Module | Report | Screen |
|---|---|---|
| `test-app-compose-html` | `open_source_licenses.html` | The generated HTML full screen in a `WebView` |
| `test-app-compose-json` | `open_source_licenses.json` | A Compose list linking out to each license URL |
| `test-app-compose-fulljson` | `open_source_licenses.full.json` | A custom Compose license screen rendering the bundled license text offline |

Every app enables exactly one report and wires the report task into `merge<Variant>Assets` so the
packaged asset is always up to date. `useVariantSpecificAssetDirs` puts each variant's report in its
own source set (`src/debug/assets`, `src/release/assets`); sharing `src/main/assets` would let the
variant that ran last overwrite the other one's report.

## Open in the IDE

These apps are a separate Gradle build, so importing the repository only syncs the plugin build and
the IDE cannot resolve anything here - version catalog accessors included. The root build cannot
include this one either: this build already includes the root through `pluginManagement`, and Gradle
rejects two builds including each other.

Link it as a second Gradle project in the same window instead: **Gradle** tool window -> **+** (Link
Gradle Project) -> pick `test-apps/settings.gradle`.

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
