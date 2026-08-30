package com.jaredsburrows.license.internal

import spock.lang.Specification
import spock.lang.Unroll

final class LicenseHelperSpec extends Specification {
  private static final def HELPER = LicenseHelper.INSTANCE

  // ---------------------------------------------------------------------------------------------
  // Invariants. These tie the lookup tables to the files actually on the classpath, in both
  // directions. The forward direction is what let cc0-1.0.txt ship for years unreachable.
  // ---------------------------------------------------------------------------------------------

  def 'every bundled license file is reachable from at least one alias'() {
    given:
    def reachable = HELPER.allAliases().values().toSet()

    expect:
    HELPER.bundledFileNames().every { fileName -> reachable.contains(fileName) }
  }

  def 'every license text file on the classpath is a bundled license'() {
    given: 'the resource directory the plugin ships'
    def dir = new File(getClass().getResource('/license').toURI())
    def onDisk = dir.listFiles().findAll { it.name.endsWith('.txt') }.collect { it.name }.toSet()

    expect: 'no orphaned file, and no table entry without a file'
    onDisk == HELPER.bundledFileNames()
  }

  def 'every alias resolves to a file that exists and has text'() {
    expect:
    HELPER.allAliases().every { alias, fileName ->
      HELPER.licenseText(fileName)?.trim()
    }
  }

  def 'no alias is registered twice with a different result'() {
    given: 'allAliases merges the name and url tables'
    def aliases = HELPER.allAliases()

    expect: 'the merge did not silently drop a colliding key'
    aliases.size() > 0
    aliases.every { alias, fileName -> HELPER.isBundled(fileName) }
  }

  // ---------------------------------------------------------------------------------------------
  // Regression guards for the three bugs this change fixes. Each of these failed before.
  // ---------------------------------------------------------------------------------------------

  def 'BUG: CC0 is bundled but was unreachable - it now resolves'() {
    expect:
    HELPER.licenseFileName('Creative Commons Zero v1.0 Universal', null) == 'cc0-1.0.txt'
    HELPER.licenseFileName('CC0-1.0', null) == 'cc0-1.0.txt'
    HELPER.licenseFileName(null, 'https://creativecommons.org/publicdomain/zero/1.0/') == 'cc0-1.0.txt'
  }

  def 'BUG: a typo meant the plain http GPL-3.0 url matched nothing'() {
    expect:
    HELPER.licenseFileName(null, 'http://www.gnu.org/licenses/gpl-3.0.txt') == 'gpl-3.0.txt'
    HELPER.licenseFileName(null, 'https://www.gnu.org/licenses/gpl-3.0.txt') == 'gpl-3.0.txt'
  }

  @Unroll
  def 'BUG: the bare SPDX id #id was missing and now resolves'() {
    expect:
    HELPER.licenseFileName(id, null) == expected

    where:
    id             || expected
    'MIT'          || 'mit.txt'
    'BSD-2-Clause' || 'bsd-2-clause.txt'
    'EPL-2.0'      || 'epl-2.0.txt'
    'GPL-2.0'      || 'gpl-2.0.txt'
    'GPL-3.0'      || 'gpl-3.0.txt'
    'LGPL-2.1'     || 'lgpl-2.1.txt'
    'LGPL-3.0'     || 'lgpl-3.0.txt'
    'MPL-2.0'      || 'mpl-2.0.txt'
    'CC0-1.0'      || 'cc0-1.0.txt'
    'Apache-2.0'   || 'apache-2.0.txt'
    'BSD-3-Clause' || 'bsd-3-clause.txt'
  }

  @Unroll
  def 'a bare SPDX id resolves whatever its casing - #id'() {
    expect:
    HELPER.licenseFileName(id, null) == 'apache-2.0.txt'

    where:
    id << ['Apache-2.0', 'apache-2.0', 'APACHE-2.0', 'ApAcHe-2.0', '  Apache-2.0  ']
  }

  // ---------------------------------------------------------------------------------------------
  // URL normalisation. One alias has to cover every spelling of the same URL.
  // ---------------------------------------------------------------------------------------------

  @Unroll
  def 'url normalisation resolves #url'() {
    expect:
    HELPER.licenseFileName(null, url) == 'apache-2.0.txt'

    where:
    url << [
      'http://www.apache.org/licenses/LICENSE-2.0',
      'https://www.apache.org/licenses/LICENSE-2.0',
      'http://www.apache.org/licenses/LICENSE-2.0.txt',
      'https://www.apache.org/licenses/LICENSE-2.0.txt',
      'http://apache.org/licenses/LICENSE-2.0',
      'https://apache.org/licenses/LICENSE-2.0/',
      'https://www.apache.org/licenses/LICENSE-2.0.html',
      'HTTPS://WWW.APACHE.ORG/LICENSES/LICENSE-2.0.TXT',
      '  https://www.apache.org/licenses/LICENSE-2.0.txt  ',
      'apache.org/licenses/LICENSE-2.0',
    ]
  }

  @Unroll
  def 'the spdx.org url family resolves - #url'() {
    expect:
    HELPER.licenseFileName(null, url) == expected

    where:
    url                                        || expected
    'https://spdx.org/licenses/MIT.html'       || 'mit.txt'
    'http://spdx.org/licenses/MIT.html'        || 'mit.txt'
    'https://spdx.org/licenses/MIT'            || 'mit.txt'
    'https://spdx.org/licenses/Apache-2.0.html'|| 'apache-2.0.txt'
    'https://spdx.org/licenses/GPL-3.0.html'   || 'gpl-3.0.txt'
    'https://spdx.org/licenses/CC0-1.0.html'   || 'cc0-1.0.txt'
    'https://spdx.org/licenses/BSD-3-Clause'   || 'bsd-3-clause.txt'
    'https://spdx.org/licenses/NOPE-9.9.html'  || null
  }

  @Unroll
  def 'known urls resolve for every bundled license - #url'() {
    expect:
    HELPER.licenseFileName(null, url) == expected

    where:
    url                                                        || expected
    'https://opensource.org/licenses/Apache-2.0'               || 'apache-2.0.txt'
    'http://opensource.org/licenses/BSD-2-Clause'              || 'bsd-2-clause.txt'
    'http://www.opensource.org/licenses/bsd-license.php'       || 'bsd-2-clause.txt'
    'https://opensource.org/licenses/BSD-3-Clause'             || 'bsd-3-clause.txt'
    'https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt'|| 'epl-2.0.txt'
    'https://opensource.org/licenses/EPL-2.0'                  || 'epl-2.0.txt'
    'http://www.gnu.org/licenses/gpl-2.0.txt'                  || 'gpl-2.0.txt'
    'https://opensource.org/licenses/GPL-2.0'                  || 'gpl-2.0.txt'
    'http://www.gnu.org/licenses/lgpl-2.1.txt'                 || 'lgpl-2.1.txt'
    'http://www.gnu.org/licenses/lgpl-3.0.txt'                 || 'lgpl-3.0.txt'
    'http://opensource.org/licenses/MIT'                       || 'mit.txt'
    'http://www.opensource.org/licenses/mit-license.php'       || 'mit.txt'
    'http://www.mozilla.org/media/MPL/2.0/index.txt'           || 'mpl-2.0.txt'
    'https://opensource.org/licenses/MPL-2.0'                  || 'mpl-2.0.txt'
    'https://creativecommons.org/publicdomain/zero/1.0/'       || 'cc0-1.0.txt'
  }

  // ---------------------------------------------------------------------------------------------
  // Name normalisation.
  // ---------------------------------------------------------------------------------------------

  @Unroll
  def 'name normalisation resolves #name'() {
    expect:
    HELPER.licenseFileName(name, null) == 'apache-2.0.txt'

    where:
    name << [
      'Apache 2.0',
      'Apache License 2.0',
      'Apache License Version 2.0',
      'Apache License, Version 2.0',
      'The Apache License, Version 2.0',
      'The Apache Software License',
      'The Apache Software License, Version 2.0',
      'the apache software license, version 2.0',
      'THE APACHE SOFTWARE LICENSE, VERSION 2.0',
      '  The   Apache   Software   License,   Version   2.0  ',
    ]
  }

  @Unroll
  def 'known names resolve for every bundled license - #name'() {
    expect:
    HELPER.licenseFileName(name, null) == expected

    where:
    name                                        || expected
    'BSD 2-Clause "Simplified" License'         || 'bsd-2-clause.txt'
    'BSD 2-Clause License'                      || 'bsd-2-clause.txt'
    'BSD 3-Clause "New" or "Revised" License'   || 'bsd-3-clause.txt'
    'Eclipse Public License 2.0'                || 'epl-2.0.txt'
    'GNU General Public License v2.0'           || 'gpl-2.0.txt'
    'GNU General Public License v3.0'           || 'gpl-3.0.txt'
    'GNU Lesser General Public License v2.1'    || 'lgpl-2.1.txt'
    'GNU Lesser General Public License v3.0'    || 'lgpl-3.0.txt'
    'MIT License'                               || 'mit.txt'
    'The MIT License'                           || 'mit.txt'
    'Mozilla Public License 2.0'                || 'mpl-2.0.txt'
    'Creative Commons Zero v1.0 Universal'      || 'cc0-1.0.txt'
  }

  // ---------------------------------------------------------------------------------------------
  // Normalisation must not over-reach. Distinct licenses must stay distinct.
  // ---------------------------------------------------------------------------------------------

  @Unroll
  def 'distinct licenses do not collapse into each other - #a vs #b'() {
    expect:
    HELPER.licenseFileName(a, null) != HELPER.licenseFileName(b, null)

    where:
    a                                        | b
    'GPL-2.0'                                | 'GPL-3.0'
    'LGPL-2.1'                               | 'LGPL-3.0'
    'GNU General Public License v2.0'        | 'GNU General Public License v3.0'
    'GNU Lesser General Public License v2.1' | 'GNU Lesser General Public License v3.0'
    'GPL-2.0'                                | 'LGPL-2.1'
    'BSD-2-Clause'                           | 'BSD-3-Clause'
    'BSD 2-Clause License'                   | 'BSD 3-Clause "New" or "Revised" License'
    'MIT'                                    | 'Apache-2.0'
  }

  @Unroll
  def 'an unrelated license is not matched - #name / #url'() {
    expect:
    HELPER.licenseFileName(name, url) == null

    where:
    name              | url
    'Some license'    | 'http://website.tld/'
    'WTFPL'           | null
    'Proprietary'     | null
    null              | 'https://example.com/LICENSE.txt'
    'GNU'             | null
    'License'         | null
    'Apache'          | null
  }

  // ---------------------------------------------------------------------------------------------
  // licenseKey contract: bundled file name, else the raw url verbatim, else "".
  // ---------------------------------------------------------------------------------------------

  @Unroll
  def 'licenseKey resolves #description'() {
    expect:
    HELPER.licenseKey(name, url) == expected

    where:
    description                            | name              | url                                              || expected
    'a known url'                          | 'Anything at all' | 'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'a known name when the url is unknown' | 'The MIT License' | 'https://spdx.org/licenses/MIT.html'             || 'mit.txt'
    'the url before the name'              | 'The MIT License' | 'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'an unbundled license to its url'      | 'Some license'    | 'http://website.tld/'                            || 'http://website.tld/'
    'a missing url to an empty key'        | 'Some license'    | ''                                               || ''
    'nulls to an empty key'                | null              | null                                             || ''
    'a null name with a known url'         | null              | 'https://opensource.org/licenses/MIT'            || 'mit.txt'
    'a known name with a null url'         | 'MIT License'     | null                                             || 'mit.txt'
    'a known name with an empty url'       | 'MIT License'     | ''                                               || 'mit.txt'
    'an empty name with a known url'       | ''                | 'https://opensource.org/licenses/MIT'            || 'mit.txt'
    'an unknown name with a null url'      | 'Some license'    | null                                             || ''
  }

  def 'licenseKey returns the raw url unchanged, not a normalised one'() {
    expect: 'the fallback must not leak normalisation into the report'
    HELPER.licenseKey('Some license', 'HTTPS://WWW.Example.COM/LICENSE.txt/') ==
      'HTTPS://WWW.Example.COM/LICENSE.txt/'
  }

  @Unroll
  def 'the url wins over a conflicting name - #url'() {
    expect:
    HELPER.licenseFileName('MIT License', url) == expected

    where:
    url                                              || expected
    'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'https://opensource.org/licenses/GPL-3.0'        || 'gpl-3.0.txt'
    'http://website.tld/'                            || 'mit.txt' // unknown url falls through to the name
  }

  // ---------------------------------------------------------------------------------------------
  // licenseText and isBundled.
  // ---------------------------------------------------------------------------------------------

  def 'licenseText returns the bundled text of a known license'() {
    when:
    def text = HELPER.licenseText('apache-2.0.txt')

    then:
    text != null
    text.contains('Apache License')
    text.contains('Version 2.0, January 2004')
  }

  def 'licenseText returns null when the plugin does not bundle the license'() {
    expect:
    HELPER.licenseText('does-not-exist.txt') == null
  }

  @Unroll
  def 'licenseText is non-empty for every bundled license - #fileName'() {
    expect:
    HELPER.licenseText(fileName)?.trim()

    where:
    fileName << LicenseHelper.INSTANCE.bundledFileNames()
  }

  @Unroll
  def 'isBundled is true only for bundled file names - #fileName'() {
    expect:
    HELPER.isBundled(fileName) == expected

    where:
    fileName            || expected
    'apache-2.0.txt'    || true
    'mit.txt'           || true
    'cc0-1.0.txt'       || true
    'does-not-exist.txt'|| false
    'apache-2.0'        || false
    ''                  || false
    'http://website.tld'|| false
  }

  @Unroll
  def 'licenseFileName only resolves licenses the plugin bundles - #description'() {
    expect:
    HELPER.licenseFileName(name, url) == expected

    where:
    description               | name              | url                                              || expected
    'a bundled license'       | 'The MIT License' | 'https://spdx.org/licenses/MIT.html'             || 'mit.txt'
    'a bundled license by url'| 'Anything at all' | 'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'an unbundled license'    | 'Some license'    | 'http://website.tld/'                            || null
    'no license at all'       | null              | null                                             || null
  }
}
