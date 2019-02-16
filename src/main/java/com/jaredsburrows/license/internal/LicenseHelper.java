package com.jaredsburrows.license.internal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map License name and URL to license text file.
 *
 * Based on "popular and widely-used or with strong communities" found here:
 * https://opensource.org/licenses/category. License text from: https://github.com/github/choosealicense.com/blob/gh-pages/_licenses.
 */
public final class LicenseHelper {
  private static final LinkedHashMap<String, String> LICENSE_MAP = new LinkedHashMap<>(47);

  public static Map<String, String> getLicenseMap() {
    return LICENSE_MAP;
  }

  static {
    // Apache License 2.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/apache-2.0.txt
    LICENSE_MAP.put("Apache 2.0",                                               "apache-2.0.txt");
    LICENSE_MAP.put("Apache License 2.0",                                       "apache-2.0.txt");
    LICENSE_MAP.put("The Apache Software License",                              "apache-2.0.txt");
    LICENSE_MAP.put("The Apache Software License, Version 2.0",                 "apache-2.0.txt");
    LICENSE_MAP.put("http://www.apache.org/licenses/LICENSE-2.0.txt",           "apache-2.0.txt");
    LICENSE_MAP.put("https://www.apache.org/licenses/LICENSE-2.0.txt",          "apache-2.0.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/Apache-2.0",                "apache-2.0.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/Apache-2.0",               "apache-2.0.txt");

    // BSD 2-Clause "Simplified" License
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-2-clause.txt
    LICENSE_MAP.put("BSD 2-Clause \"Simplified\" License",                      "bsd-2-clause.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/BSD-2-Clause",              "bsd-2-clause.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/BSD-2-Clause",             "bsd-2-clause.txt");

    // BSD 3-Clause "New" or "Revised" License
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-3-clause.txt
    LICENSE_MAP.put("BSD 3-Clause \"New\" or \"Revised\" License",              "bsd-3-clause.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/BSD-3-Clause",              "bsd-3-clause.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/BSD-3-Clause",             "bsd-3-clause.txt");

    // Eclipse Public License 2.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/epl-2.0.txt
    LICENSE_MAP.put("Eclipse Public License 2.0",                               "epl-2.0.txt");
    LICENSE_MAP.put("http://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt", "epl-2.0.txt");
    LICENSE_MAP.put("https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt","epl-2.0.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/EPL-2.0",                   "epl-2.0.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/EPL-2.0",                  "epl-2.0.txt");

    // GNU General Public License v2.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-2.0.txt
    LICENSE_MAP.put("GNU General Public License v2.0",                          "gpl-2.0.txt");
    LICENSE_MAP.put("http://www.gnu.org/licenses/gpl-2.0.txt",                  "gpl-2.0.txt");
    LICENSE_MAP.put("https://www.gnu.org/licenses/gpl-2.0.txt",                 "gpl-2.0.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/GPL-2.0",                   "gpl-2.0.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/GPL-2.0",                  "gpl-2.0.txt");

    // GNU General Public License v3.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-3.0.txt
    LICENSE_MAP.put("GNU General Public License v3.0",                          "gpl-3.0.txt");
    LICENSE_MAP.put("https//www.gnu.org/licenses/gpl-3.0.txt",                  "gpl-3.0.txt");
    LICENSE_MAP.put("https://www.gnu.org/licenses/gpl-3.0.txt",                 "gpl-3.0.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/GPL-3.0",                   "gpl-3.0.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/GPL-3.0",                  "gpl-3.0.txt");

    // GNU Lesser General Public License v2.1
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-2.1.txt
    LICENSE_MAP.put("GNU Lesser General Public License v2.1",                   "lgpl-2.1.txt");
    LICENSE_MAP.put("http://www.gnu.org/licenses/lgpl-2.1.txt",                 "lgpl-2.1.txt");
    LICENSE_MAP.put("https://www.gnu.org/licenses/lgpl-2.1.txt",                "lgpl-2.1.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/LGPL-2.1",                  "lgpl-2.1.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/LGPL-2.1",                 "lgpl-2.1.txt");

    // GNU Lesser General Public License v3.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-3.0.txt
    LICENSE_MAP.put("GNU Lesser General Public License v3.0",                   "lgpl-3.0.txt");
    LICENSE_MAP.put("http://www.gnu.org/licenses/lgpl-3.0.txt",                 "lgpl-3.0.txt");
    LICENSE_MAP.put("https://www.gnu.org/licenses/lgpl-3.0.txt",                "lgpl-3.0.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/LGPL-3.0",                  "lgpl-3.0.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/LGPL-3.0",                 "lgpl-3.0.txt");

    // MIT License
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mit.txt
    LICENSE_MAP.put("MIT License",                                              "mit.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/MIT",                       "mit.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/MIT",                      "mit.txt");

    // Mozilla Public License 2.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mpl-2.0.txt
    LICENSE_MAP.put("Mozilla Public License 2.0",                               "mpl-2.0.txt");
    LICENSE_MAP.put("http://www.mozilla.org/media/MPL/2.0/index.txt",           "mpl-2.0.txt");
    LICENSE_MAP.put("https://www.mozilla.org/media/MPL/2.0/index.txt",          "mpl-2.0.txt");
    LICENSE_MAP.put("http://opensource.org/licenses/MPL-2.0",                   "mpl-2.0.txt");
    LICENSE_MAP.put("https://opensource.org/licenses/MPL-2.0",                  "mpl-2.0.txt");
  }
}
