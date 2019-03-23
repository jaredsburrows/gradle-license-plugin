package com.jaredsburrows.license

import org.gradle.api.DefaultTask
import java.io.File
import java.net.URI

abstract class LicenseReportTaskKt : DefaultTask() {
  fun getClickableFileUrl(file: File): String {
    return URI("file", "", file.toURI().path, null, null).toString()
  }
}
