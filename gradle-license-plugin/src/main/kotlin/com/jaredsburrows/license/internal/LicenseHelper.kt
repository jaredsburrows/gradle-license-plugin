package com.jaredsburrows.license.internal

/**
 * Map License name and URL to license text file, matched after normalisation so one alias covers
 * every spelling of the same thing (scheme, casing, www, trailing slash, extension, punctuation).
 *
 * Based on https://opensource.org/licenses/category; text from choosealicense and the SPDX list.
 */
object LicenseHelper {
  // Deliberately not "." - it separates the parts of a version number ("2.0").
  private val PUNCTUATION = Regex("[\",'()]")

  // \s misses the non-breaking and zero-width spaces that turn up in POMs copied from a web page.
  private val WHITESPACE = Regex("[\\s\\u00a0\\u200b\\ufeff]+")

  // "License - v 1.0" and "License 1.0" must reach the same key.
  private val SEPARATOR = Regex("\\s+-\\s+")

  // "Version 2.0", "v2.0", "v 2.0" and "v. 2.0" all mean "2.0".
  private val VERSION_PREFIX = Regex("\\b(version\\s+|v\\.?\\s*(?=\\d))")

  // The GNU licenses SPDX re-spelled with an explicit -only / -or-later suffix.
  private val GNU_FAMILY = Regex("(A|L)?GPL-[0-9.]+")

  private val EXTENSION = Regex("(\\.(txt|html?|php))+$")
  private val LOCALE = Regex("\\.[a-z]{2}$")
  private val PORT = Regex("^([^/]+):\\d+")

  /** URLs cited for more than one variant of a family, so a name stating the variant wins. */
  private val AMBIGUOUS_URLS: Set<String> = setOf("opensource.org/licenses/bsd-license")

  /** Canonical SPDX identifier to the bundled license text file. */
  private val texts =
    linkedMapOf(
      // BSD Zero Clause License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/0bsd.txt
      "0BSD" to "0bsd.txt",
      // GNU Affero General Public License v3.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/agpl-3.0.txt
      "AGPL-3.0" to "agpl-3.0.txt",
      // Apache License 1.1 - choosealicense does not carry it
      // https://github.com/spdx/license-list-data/blob/main/text/Apache-1.1.txt
      "Apache-1.1" to "apache-1.1.txt",
      // Apache License 2.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/apache-2.0.txt
      "Apache-2.0" to "apache-2.0.txt",
      // BSD 2-Clause "Simplified" License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-2-clause.txt
      "BSD-2-Clause" to "bsd-2-clause.txt",
      // BSD 3-Clause "New" or "Revised" License, also the text of the Eclipse Distribution License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-3-clause.txt
      "BSD-3-Clause" to "bsd-3-clause.txt",
      // BSD 4-Clause "Original" or "Old" License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-4-clause.txt
      "BSD-4-Clause" to "bsd-4-clause.txt",
      // Creative Commons Attribution 4.0 International
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/cc-by-4.0.txt
      "CC-BY-4.0" to "cc-by-4.0.txt",
      // Creative Commons Attribution Share Alike 4.0 International
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/cc-by-sa-4.0.txt
      "CC-BY-SA-4.0" to "cc-by-sa-4.0.txt",
      // Creative Commons Zero v1.0 Universal
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/cc0-1.0.txt
      "CC0-1.0" to "cc0-1.0.txt",
      // Common Development and Distribution License 1.0 - choosealicense does not carry it
      // https://github.com/spdx/license-list-data/blob/main/text/CDDL-1.0.txt
      "CDDL-1.0" to "cddl-1.0.txt",
      // Common Development and Distribution License 1.1 - choosealicense does not carry it
      // https://github.com/spdx/license-list-data/blob/main/text/CDDL-1.1.txt
      "CDDL-1.1" to "cddl-1.1.txt",
      // Eclipse Public License 1.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/epl-1.0.txt
      "EPL-1.0" to "epl-1.0.txt",
      // Eclipse Public License 2.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/epl-2.0.txt
      "EPL-2.0" to "epl-2.0.txt",
      // GNU General Public License v2.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-2.0.txt
      "GPL-2.0" to "gpl-2.0.txt",
      // GPL-2.0-only WITH Classpath-exception-2.0 is an SPDX expression rather than a license, so
      // it has no text of its own: gpl-2.0.txt followed by the exception.
      // https://github.com/spdx/license-list-data/blob/main/text/Classpath-exception-2.0.txt
      "GPL-2.0-with-classpath-exception" to "gpl-2.0-with-classpath-exception.txt",
      // GNU General Public License v3.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-3.0.txt
      "GPL-3.0" to "gpl-3.0.txt",
      // ISC License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/isc.txt
      "ISC" to "isc.txt",
      // GNU Library General Public License v2.0 - choosealicense does not carry it
      // https://github.com/spdx/license-list-data/blob/main/text/LGPL-2.0-only.txt
      "LGPL-2.0" to "lgpl-2.0.txt",
      // GNU Lesser General Public License v2.1
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-2.1.txt
      "LGPL-2.1" to "lgpl-2.1.txt",
      // GNU Lesser General Public License v3.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-3.0.txt
      "LGPL-3.0" to "lgpl-3.0.txt",
      // MIT License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mit.txt
      "MIT" to "mit.txt",
      // MIT No Attribution
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mit-0.txt
      "MIT-0" to "mit-0.txt",
      // Mozilla Public License 2.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mpl-2.0.txt
      "MPL-2.0" to "mpl-2.0.txt",
      // The Unlicense
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/unlicense.txt
      "Unlicense" to "unlicense.txt",
    )

  /** Normalised license name to canonical SPDX identifier. */
  private val nameAliases: Map<String, String> =
    linkedMapOf(
      // Apache License 2.0
      "apache 2.0" to "Apache-2.0",
      "apache license 2.0" to "Apache-2.0",
      "apache software license 2.0" to "Apache-2.0",
      "apache software license" to "Apache-2.0",
      "apache 2" to "Apache-2.0",
      "apache license" to "Apache-2.0",
      "apache software license 2" to "Apache-2.0",
      // BSD 2-Clause "Simplified" License
      "bsd 2-clause simplified license" to "BSD-2-Clause",
      "bsd 2-clause license" to "BSD-2-Clause",
      "simplified bsd license" to "BSD-2-Clause",
      "freebsd license" to "BSD-2-Clause",
      // BSD 3-Clause "New" or "Revised" License
      "bsd 3-clause new or revised license" to "BSD-3-Clause",
      "bsd 3-clause license" to "BSD-3-Clause",
      "new bsd license" to "BSD-3-Clause",
      "modified bsd license" to "BSD-3-Clause",
      "revised bsd license" to "BSD-3-Clause",
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
      // BSD Zero Clause License
      "bsd zero clause license" to "0BSD",
      "zero clause bsd license" to "0BSD",
      // GNU Affero General Public License v3.0
      "gnu affero general public license 3.0" to "AGPL-3.0",
      "affero general public license 3.0" to "AGPL-3.0",
      // Apache License 1.1
      "apache license 1.1" to "Apache-1.1",
      "apache software license 1.1" to "Apache-1.1",
      // BSD 4-Clause "Original" or "Old" License
      "bsd 4-clause original or old license" to "BSD-4-Clause",
      "bsd 4-clause license" to "BSD-4-Clause",
      // Creative Commons Attribution 4.0 International
      "creative commons attribution 4.0 international" to "CC-BY-4.0",
      // Creative Commons Attribution Share Alike 4.0 International
      "creative commons attribution share alike 4.0 international" to "CC-BY-SA-4.0",
      "creative commons attribution-sharealike 4.0 international" to "CC-BY-SA-4.0",
      // Common Development and Distribution License 1.0
      "common development and distribution license 1.0" to "CDDL-1.0",
      "common development and distribution license cddl 1.0" to "CDDL-1.0",
      "cddl 1.0" to "CDDL-1.0",
      // Common Development and Distribution License 1.1
      "common development and distribution license 1.1" to "CDDL-1.1",
      "common development and distribution license cddl 1.1" to "CDDL-1.1",
      "cddl 1.1" to "CDDL-1.1",
      // Eclipse Distribution License 1.0 is the BSD 3-Clause text
      "eclipse distribution license 1.0" to "BSD-3-Clause",
      "edl 1.0" to "BSD-3-Clause",
      // Eclipse Public License 1.0
      "eclipse public license 1.0" to "EPL-1.0",
      "common public license 1.0" to "EPL-1.0",
      // GNU General Public License v2.0 with the Classpath exception
      "gnu general public license 2.0 w/classpath exception" to "GPL-2.0-with-classpath-exception",
      "gnu general public license 2.0 with the classpath exception" to "GPL-2.0-with-classpath-exception",
      "gpl2 w/ cpe" to "GPL-2.0-with-classpath-exception",
      // ISC License
      "isc license" to "ISC",
      // GNU Lesser General Public License v2.0
      "gnu lesser general public license 2.0" to "LGPL-2.0",
      "gnu library general public license 2.0" to "LGPL-2.0",
      "gnu library general public license 2" to "LGPL-2.0",
      // MIT No Attribution
      "mit no attribution" to "MIT-0",
      // The Unlicense
      "unlicense" to "Unlicense",
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
      "gnu.org/licenses/old-licenses/gpl-2.0" to "GPL-2.0",
      "opensource.org/licenses/gpl-2.0" to "GPL-2.0",
      // GNU General Public License v3.0
      "gnu.org/licenses/gpl-3.0" to "GPL-3.0",
      "opensource.org/licenses/gpl-3.0" to "GPL-3.0",
      // GNU Lesser General Public License v2.1
      "gnu.org/licenses/lgpl-2.1" to "LGPL-2.1",
      "gnu.org/licenses/old-licenses/lgpl-2.1" to "LGPL-2.1",
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
      // BSD Zero Clause License
      "opensource.org/licenses/0bsd" to "0BSD",
      // GNU Affero General Public License v3.0
      "gnu.org/licenses/agpl-3.0" to "AGPL-3.0",
      "opensource.org/licenses/agpl-3.0" to "AGPL-3.0",
      // Apache License 1.1
      "apache.org/licenses/license-1.1" to "Apache-1.1",
      "opensource.org/licenses/apache-1.1" to "Apache-1.1",
      // BSD 4-Clause "Original" or "Old" License
      "opensource.org/licenses/bsd-4-clause" to "BSD-4-Clause",
      // Creative Commons Attribution 4.0 International
      "creativecommons.org/licenses/by/4.0" to "CC-BY-4.0",
      // Creative Commons Attribution Share Alike 4.0 International
      "creativecommons.org/licenses/by-sa/4.0" to "CC-BY-SA-4.0",
      // Common Development and Distribution License 1.0
      "opensource.org/licenses/cddl-1.0" to "CDDL-1.0",
      "opensource.org/licenses/cddl1" to "CDDL-1.0",
      "glassfish.dev.java.net/public/cddlv1.0" to "CDDL-1.0",
      // Common Development and Distribution License 1.1
      "glassfish.java.net/public/cddl+gpl_1_1" to "CDDL-1.1",
      "oracle.com/technetwork/licenses/cddl-1.1" to "CDDL-1.1",
      // Eclipse Distribution License 1.0 is the BSD 3-Clause text
      "eclipse.org/org/documents/edl-v10" to "BSD-3-Clause",
      "eclipse.org/org/documents/edl-v1.0" to "BSD-3-Clause",
      // Eclipse Public License 1.0
      "eclipse.org/legal/epl-v10" to "EPL-1.0",
      "opensource.org/licenses/epl-1.0" to "EPL-1.0",
      // GNU General Public License v2.0 with the Classpath exception
      "openjdk.java.net/legal/gplv2+ce" to "GPL-2.0-with-classpath-exception",
      "gnu.org/software/classpath/license" to "GPL-2.0-with-classpath-exception",
      // ISC License
      "opensource.org/licenses/isc" to "ISC",
      // GNU Lesser General Public License v2.0
      "gnu.org/licenses/lgpl-2.0" to "LGPL-2.0",
      "gnu.org/licenses/old-licenses/lgpl-2.0" to "LGPL-2.0",
      "opensource.org/licenses/lgpl-2.0" to "LGPL-2.0",
      // MIT No Attribution
      "opensource.org/licenses/mit-0" to "MIT-0",
      // The Unlicense
      "unlicense.org" to "Unlicense",
      "opensource.org/licenses/unlicense" to "Unlicense",
    )

  /**
   * SPDX identifiers, lower cased. SPDX deprecated the bare GNU ids in 2018, so the "-only" and
   * "-or-later" spellings current tooling emits map to the same text.
   */
  private val spdxIds: Map<String, String> =
    buildMap {
      texts.keys.forEach { id ->
        put(id.lowercase(), id)
        if (GNU_FAMILY.matches(id)) {
          put("$id-only".lowercase(), id)
          put("$id-or-later".lowercase(), id)
        }
      }
      // "GPL-2.0-only WITH Classpath-exception-2.0" is the modern spelling of the deprecated
      // GPL-2.0-with-classpath-exception identifier.
      listOf("gpl-2.0", "gpl-2.0-only", "gpl-2.0-or-later")
        .forEach { put("$it with classpath-exception-2.0", "GPL-2.0-with-classpath-exception") }
    }

  /** Strip everything that varies between spellings: scheme, www, port, query, slash, extension. */
  private fun normalizeUrl(url: String): String {
    var value = url.trim().lowercase().replace(WHITESPACE, "")
    value = value.substringAfter("://", value)
    // Protocol-relative "//host/path".
    value = value.removePrefix("//")
    // A fragment or query string never identifies the license.
    value = value.substringBefore('#').substringBefore('?')
    value = value.removePrefix("www.")
    value = value.replace(PORT, "$1")
    value = value.trimEnd('/')
    // A repeated group in one pass, so ".txt.html" folds the same way as ".html.txt" without
    // depending on the order a list of suffixes would impose, and without a loop.
    value = value.replace(EXTENSION, "")
    // gnu.org serves its retired licenses as "lgpl-2.1.en.html"; drop the locale segment. Only
    // when there is a path, so a bare host such as "unlicense.org" keeps its ".org".
    if (value.contains('/')) {
      value = value.replace(LOCALE, "")
    }
    return value.trimEnd('/')
  }

  /** Reduce a name to its distinguishing words: "The Apache Software License, Version 2.0" -> "apache software license 2.0". */
  private fun normalizeName(name: String): String {
    var value = name.trim().lowercase()
    value = value.replace(PUNCTUATION, " ")
    value = value.replace(WHITESPACE, " ").trim()
    value = value.removePrefix("the ")
    value = value.replace(SEPARATOR, " ")
    value = value.replace(VERSION_PREFIX, "")
    return value.replace(WHITESPACE, " ").trim()
  }

  /** The SPDX identifier a URL names, or null. */
  private fun spdxIdFromUrl(url: String): String? {
    val normalized = normalizeUrl(url)
    // https://spdx.org/licenses/MIT.html names the identifier directly.
    val fromSpdxUrl = normalized.substringAfter("spdx.org/licenses/", "")
    return spdxIds[fromSpdxUrl] ?: urlAliases[normalized]
  }

  /** The SPDX identifier a name states, or null. */
  private fun spdxIdFromName(name: String): String? =
    spdxIds[name.trim().lowercase().replace(WHITESPACE, " ")] ?: nameAliases[normalizeName(name)]

  /**
   * The SPDX id for [name]/[url], or null. URL first, since it is the more precise signal - unless
   * it is in [AMBIGUOUS_URLS], where a name that states the variant wins (hamcrest, #846).
   */
  private fun spdxId(
    name: String?,
    url: String?,
  ): String? {
    var fromUrl: String? = null
    var urlIsAmbiguous = false
    if (!url.isNullOrBlank()) {
      fromUrl = spdxIdFromUrl(url)
      urlIsAmbiguous = normalizeUrl(url) in AMBIGUOUS_URLS
    }
    if (fromUrl != null && !urlIsAmbiguous) return fromUrl
    if (!name.isNullOrBlank()) {
      spdxIdFromName(name)?.let { return it }
    }
    return fromUrl
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
}
