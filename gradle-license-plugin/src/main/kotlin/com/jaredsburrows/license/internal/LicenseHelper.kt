package com.jaredsburrows.license.internal

/**
 * Map License name and URL to license text file.
 *
 * Based on "popular and widely-used or with strong communities" found here:
 * https://opensource.org/licenses/category.
 * License text from: https://github.com/github/choosealicense.com/blob/gh-pages/_licenses.
 */
object LicenseHelper {
  val licenseMap: Map<String, String>

  init {
    licenseMap = linkedMapOf(
      // Apache License 2.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/apache-2.0.txt
      "Apache 2.0" to "apache-2.0.txt",
      "Apache License 2.0" to "apache-2.0.txt",
      "The Apache Software License" to "apache-2.0.txt",
      "The Apache Software License, Version 2.0" to "apache-2.0.txt",
      "http://www.apache.org/licenses/LICENSE-2.0.txt" to "apache-2.0.txt",
      "https://www.apache.org/licenses/LICENSE-2.0.txt" to "apache-2.0.txt",
      "http://opensource.org/licenses/Apache-2.0" to "apache-2.0.txt",
      "https://opensource.org/licenses/Apache-2.0" to "apache-2.0.txt",

      // BSD 2-Clause "Simplified" License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-2-clause.txt
      "BSD 2-Clause \"Simplified\" License" to "bsd-2-clause.txt",
      "http://opensource.org/licenses/BSD-2-Clause" to "bsd-2-clause.txt",
      "https://opensource.org/licenses/BSD-2-Clause" to "bsd-2-clause.txt",

      // BSD 3-Clause "New" or "Revised" License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-3-clause.txt
      "BSD 3-Clause \"New\" or \"Revised\" License" to "bsd-3-clause.txt",
      "http://opensource.org/licenses/BSD-3-Clause" to "bsd-3-clause.txt",
      "https://opensource.org/licenses/BSD-3-Clause" to "bsd-3-clause.txt",

      // Eclipse Public License 2.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/epl-2.0.txt
      "Eclipse Public License 2.0" to "epl-2.0.txt",
      "http://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt" to "epl-2.0.txt",
      "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt" to "epl-2.0.txt",
      "http://opensource.org/licenses/EPL-2.0" to "epl-2.0.txt",
      "https://opensource.org/licenses/EPL-2.0" to "epl-2.0.txt",

      // GNU General Public License v2.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-2.0.txt
      "GNU General Public License v2.0" to "gpl-2.0.txt",
      "http://www.gnu.org/licenses/gpl-2.0.txt" to "gpl-2.0.txt",
      "https://www.gnu.org/licenses/gpl-2.0.txt" to "gpl-2.0.txt",
      "http://opensource.org/licenses/GPL-2.0" to "gpl-2.0.txt",
      "https://opensource.org/licenses/GPL-2.0" to "gpl-2.0.txt",

      // GNU General Public License v3.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-3.0.txt
      "GNU General Public License v3.0" to "gpl-3.0.txt",
      "https//www.gnu.org/licenses/gpl-3.0.txt" to "gpl-3.0.txt",
      "https://www.gnu.org/licenses/gpl-3.0.txt" to "gpl-3.0.txt",
      "http://opensource.org/licenses/GPL-3.0" to "gpl-3.0.txt",
      "https://opensource.org/licenses/GPL-3.0" to "gpl-3.0.txt",

      // GNU Lesser General Public License v2.1
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-2.1.txt
      "GNU Lesser General Public License v2.1" to "lgpl-2.1.txt",
      "http://www.gnu.org/licenses/lgpl-2.1.txt" to "lgpl-2.1.txt",
      "https://www.gnu.org/licenses/lgpl-2.1.txt" to "lgpl-2.1.txt",
      "http://opensource.org/licenses/LGPL-2.1" to "lgpl-2.1.txt",
      "https://opensource.org/licenses/LGPL-2.1" to "lgpl-2.1.txt",

      // GNU Lesser General Public License v3.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-3.0.txt
      "GNU Lesser General Public License v3.0" to "lgpl-3.0.txt",
      "http://www.gnu.org/licenses/lgpl-3.0.txt" to "lgpl-3.0.txt",
      "https://www.gnu.org/licenses/lgpl-3.0.txt" to "lgpl-3.0.txt",
      "http://opensource.org/licenses/LGPL-3.0" to "lgpl-3.0.txt",
      "https://opensource.org/licenses/LGPL-3.0" to "lgpl-3.0.txt",

      // MIT License
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mit.txt
      "MIT License" to "mit.txt",
      "http://opensource.org/licenses/MIT" to "mit.txt",
      "https://opensource.org/licenses/MIT" to "mit.txt",
      "http://www.opensource.org/licenses/mit-license.php" to "mit.txt",

      // Mozilla Public License 2.0
      // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mpl-2.0.txt
      "Mozilla Public License 2.0" to "mpl-2.0.txt",
      "http://www.mozilla.org/media/MPL/2.0/index.txt" to "mpl-2.0.txt",
      "https://www.mozilla.org/media/MPL/2.0/index.txt" to "mpl-2.0.txt",
      "http://opensource.org/licenses/MPL-2.0" to "mpl-2.0.txt",
      "https://opensource.org/licenses/MPL-2.0" to "mpl-2.0.txt"
    )
  }
}
