package com.jaredsburrows.license.internal

/**
 * Map License name and URL to license text file.
 *
 * Based on "popular and widely-used or with strong communities" found here:
 * https://opensource.org/licenses/category.
 * License text from: https://github.com/github/choosealicense.com/blob/gh-pages/_licenses.
 *
 * Names and URLs are matched after normalisation, so a single alias covers every spelling of the
 * same thing: `http` and `https`, a leading `www.`, a trailing slash, a `.txt`/`.html`/`.php`
 * suffix, any casing, and surrounding whitespace all collapse to one key. That is why the tables
 * below list each name or URL only once even though POMs spell them many ways.
 */
object LicenseHelper {
  /** Canonical SPDX identifier to the bundled license text file. */
  private val texts: Map<String, String> =
    linkedMapOf(
      "Apache-2.0" to "apache-2.0.txt",
      "BSD-2-Clause" to "bsd-2-clause.txt",
      "BSD-3-Clause" to "bsd-3-clause.txt",
      "CC0-1.0" to "cc0-1.0.txt",
      "EPL-2.0" to "epl-2.0.txt",
      "GPL-2.0" to "gpl-2.0.txt",
      "GPL-3.0" to "gpl-3.0.txt",
      "LGPL-2.1" to "lgpl-2.1.txt",
      "LGPL-3.0" to "lgpl-3.0.txt",
      "MIT" to "mit.txt",
      "MPL-2.0" to "mpl-2.0.txt",
    )

  /** Normalised license name to canonical SPDX identifier. */
  private val nameAliases: Map<String, String> =
    linkedMapOf(
      // Apache License 2.0
      "apache 2.0" to "Apache-2.0",
      "apache license 2.0" to "Apache-2.0",
      "apache software license 2.0" to "Apache-2.0",
      "apache software license" to "Apache-2.0",
      // BSD 2-Clause "Simplified" License
      "bsd 2-clause simplified license" to "BSD-2-Clause",
      "bsd 2-clause license" to "BSD-2-Clause",
      // BSD 3-Clause "New" or "Revised" License
      "bsd 3-clause new or revised license" to "BSD-3-Clause",
      "bsd 3-clause license" to "BSD-3-Clause",
      // Creative Commons Zero v1.0 Universal
      "creative commons zero 1.0 universal" to "CC0-1.0",
      "cc0 1.0 universal" to "CC0-1.0",
      // Eclipse Public License 2.0
      "eclipse public license 2.0" to "EPL-2.0",
      // GNU General Public License v2.0
      "gnu general public license 2.0" to "GPL-2.0",
      // GNU General Public License v3.0
      "gnu general public license 3.0" to "GPL-3.0",
      // GNU Lesser General Public License v2.1
      "gnu lesser general public license 2.1" to "LGPL-2.1",
      // GNU Lesser General Public License v3.0
      "gnu lesser general public license 3.0" to "LGPL-3.0",
      // MIT License
      "mit license" to "MIT",
      // Mozilla Public License 2.0
      "mozilla public license 2.0" to "MPL-2.0",
    )

  /** Normalised license URL to canonical SPDX identifier. */
  private val urlAliases: Map<String, String> =
    linkedMapOf(
      // Apache License 2.0
      "apache.org/licenses/license-2.0" to "Apache-2.0",
      "opensource.org/licenses/apache-2.0" to "Apache-2.0",
      // BSD 2-Clause "Simplified" License
      "opensource.org/licenses/bsd-2-clause" to "BSD-2-Clause",
      "opensource.org/licenses/bsd-license" to "BSD-2-Clause",
      // BSD 3-Clause "New" or "Revised" License
      "opensource.org/licenses/bsd-3-clause" to "BSD-3-Clause",
      // Creative Commons Zero v1.0 Universal
      "creativecommons.org/publicdomain/zero/1.0" to "CC0-1.0",
      "opensource.org/licenses/cc0-1.0" to "CC0-1.0",
      // Eclipse Public License 2.0
      "eclipse.org/org/documents/epl-2.0/epl-2.0" to "EPL-2.0",
      "eclipse.org/legal/epl-2.0" to "EPL-2.0",
      "opensource.org/licenses/epl-2.0" to "EPL-2.0",
      // GNU General Public License v2.0
      "gnu.org/licenses/gpl-2.0" to "GPL-2.0",
      "opensource.org/licenses/gpl-2.0" to "GPL-2.0",
      // GNU General Public License v3.0
      "gnu.org/licenses/gpl-3.0" to "GPL-3.0",
      "opensource.org/licenses/gpl-3.0" to "GPL-3.0",
      // GNU Lesser General Public License v2.1
      "gnu.org/licenses/lgpl-2.1" to "LGPL-2.1",
      "opensource.org/licenses/lgpl-2.1" to "LGPL-2.1",
      // GNU Lesser General Public License v3.0
      "gnu.org/licenses/lgpl-3.0" to "LGPL-3.0",
      "opensource.org/licenses/lgpl-3.0" to "LGPL-3.0",
      // MIT License
      "opensource.org/licenses/mit" to "MIT",
      "opensource.org/licenses/mit-license" to "MIT",
      // Mozilla Public License 2.0
      "mozilla.org/media/mpl/2.0/index" to "MPL-2.0",
      "mozilla.org/mpl/2.0" to "MPL-2.0",
      "opensource.org/licenses/mpl-2.0" to "MPL-2.0",
    )

  /** SPDX identifiers, lower cased, for case-insensitive lookup of a bare identifier. */
  private val spdxIds: Map<String, String> = texts.keys.associateBy { it.lowercase() }

  /**
   * Strip everything that varies between spellings of the same URL: the scheme, a leading "www.",
   * a trailing slash and a file extension. `https://www.Apache.org/licenses/LICENSE-2.0.txt` and
   * `http://apache.org/licenses/LICENSE-2.0` both become `apache.org/licenses/license-2.0`.
   */
  private fun normalizeUrl(url: String): String {
    var value = url.trim().lowercase()
    value = value.substringAfter("://", value)
    value = value.removePrefix("www.")
    value = value.trimEnd('/')
    for (extension in listOf(".txt", ".html", ".htm", ".php")) {
      value = value.removeSuffix(extension)
    }
    return value.trimEnd('/')
  }

  /**
   * Reduce a license name to its distinguishing words: lower case, punctuation removed, runs of
   * whitespace collapsed, a leading "the" dropped and "version 2.0"/"v2.0" folded to "2.0".
   * `The Apache Software License, Version 2.0` becomes `apache software license 2.0`.
   */
  private fun normalizeName(name: String): String {
    var value = name.trim().lowercase()
    value = value.replace(PUNCTUATION, " ")
    value = value.replace(WHITESPACE, " ").trim()
    value = value.removePrefix("the ")
    value = value.replace(VERSION_PREFIX, "")
    return value.replace(WHITESPACE, " ").trim()
  }

  /** The canonical SPDX identifier for [name]/[url], or null when it is not one we bundle. */
  private fun spdxId(
    name: String?,
    url: String?,
  ): String? {
    if (!url.isNullOrBlank()) {
      val normalized = normalizeUrl(url)
      // https://spdx.org/licenses/MIT.html names the identifier directly.
      val fromSpdxUrl = normalized.substringAfter("spdx.org/licenses/", "")
      spdxIds[fromSpdxUrl]?.let { return it }
      urlAliases[normalized]?.let { return it }
    }
    if (!name.isNullOrBlank()) {
      spdxIds[name.trim().lowercase()]?.let { return it }
      nameAliases[normalizeName(name)]?.let { return it }
    }
    return null
  }

  /**
   * See if the license is one the plugin bundles (which coalesces differing names and URLs to the
   * same license text). If not, use the URL if present. Else "".
   */
  fun licenseKey(
    name: String?,
    url: String?,
  ): String = licenseFileName(name, url) ?: url.orEmpty()

  /** The bundled license text for a [texts] value (eg. "apache-2.0.txt"), null when unknown. */
  fun licenseText(fileName: String): String? = LicenseHelper::class.java.getResource("/license/$fileName")?.readText()

  /**
   * The bundled file name (eg. "apache-2.0.txt") for the license identified by [name]/[url], or
   * null when the plugin does not bundle that license.
   */
  fun licenseFileName(
    name: String?,
    url: String?,
  ): String? = spdxId(name, url)?.let { texts[it] }

  /** Whether [fileName] is a license text this plugin bundles. */
  fun isBundled(fileName: String): Boolean = texts.containsValue(fileName)

  /** Every bundled license text file name. Used to assert the tables and resources agree. */
  fun bundledFileNames(): Set<String> = texts.values.toSet()

  /** Every alias, name and URL alike, paired with the file it must resolve to. Used by tests. */
  fun allAliases(): Map<String, String> = (nameAliases + urlAliases).mapValues { (_, id) -> texts.getValue(id) }

  // Deliberately not "." - it separates the parts of a version number ("2.0").
  private val PUNCTUATION = Regex("[\",'()]")
  private val WHITESPACE = Regex("\\s+")
  private val VERSION_PREFIX = Regex("\\b(version\\s+|v(?=\\d))")
}
