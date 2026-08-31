package com.jaredsburrows.license.internal

import groovy.transform.TypeChecked
import spock.lang.Specification
import spock.lang.Unroll

@TypeChecked
final class LicenseHelperSpec extends Specification {
  private static final LicenseHelper HELPER = LicenseHelper.INSTANCE

  def 'every bundled license file is reachable from at least one alias'() {
    given:
    Set<String> reachable = HELPER.allAliases().values().toSet()
    Set<String> unreachable = HELPER.bundledFileNames().findAll { String fileName -> !reachable.contains(fileName) }

    expect: 'naming the unreachable files, rather than just failing'
    unreachable == [] as Set
  }

  def 'every license text file on the classpath is a bundled license'() {
    given: 'the resource directory the plugin ships'
    File dir = new File(getClass().getResource('/license').toURI())
    Set<String> onDisk = dir.listFiles().findAll { it.name.endsWith('.txt') }.collect { it.name }.toSet()

    Set<String> bundled = HELPER.bundledFileNames()

    expect: 'no orphaned file, and no table entry without a file'
    onDisk == bundled
  }

  def 'every alias resolves to a file that exists and has text'() {
    given:
    Collection<String> withoutText = HELPER.allAliases().values().findAll { String fileName ->
      !HELPER.licenseText(fileName)?.trim()
    }

    expect:
    withoutText == []
  }

  def 'no alias is registered twice with a different result'() {
    given: 'allAliases merges the name and url tables'
    Map<String, String> aliases = HELPER.allAliases()
    boolean anyAliases = !aliases.isEmpty()
    Collection<String> notBundled = aliases.values().findAll { String fileName -> !HELPER.isBundled(fileName) }

    expect: 'the merge did not silently drop a colliding key'
    anyAliases
    notBundled == []
  }

  def 'BUG: CC0 is bundled but was unreachable - it now resolves'() {
    given:
    String byFullName = HELPER.licenseFileName('Creative Commons Zero v1.0 Universal', null)
    String bySpdxId = HELPER.licenseFileName('CC0-1.0', null)
    String byUrl = HELPER.licenseFileName(null, 'https://creativecommons.org/publicdomain/zero/1.0/')

    expect:
    byFullName == 'cc0-1.0.txt'
    bySpdxId == 'cc0-1.0.txt'
    byUrl == 'cc0-1.0.txt'
  }

  def 'BUG: a typo meant the plain http GPL-3.0 url matched nothing'() {
    given:
    String plainHttp = HELPER.licenseFileName(null, 'http://www.gnu.org/licenses/gpl-3.0.txt')
    String https = HELPER.licenseFileName(null, 'https://www.gnu.org/licenses/gpl-3.0.txt')

    expect:
    plainHttp == 'gpl-3.0.txt'
    https == 'gpl-3.0.txt'
  }

  @Unroll
  def 'BUG: the bare SPDX id #id was missing and now resolves'(String id, String expected) {
    given:
    String actual = HELPER.licenseFileName(id, null)

    expect:
    actual == expected

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
  def 'a bare SPDX id resolves whatever its casing - #id'(String id) {
    given:
    String actual = HELPER.licenseFileName(id, null)

    expect:
    actual == 'apache-2.0.txt'

    where:
    id << ['Apache-2.0', 'apache-2.0', 'APACHE-2.0', 'ApAcHe-2.0', '  Apache-2.0  ']
  }

  @Unroll
  def 'url normalisation resolves #url'(String url) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect:
    actual == 'apache-2.0.txt'

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
  def 'the spdx.org url family resolves - #url'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect:
    actual == expected

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
  def 'known urls resolve for every bundled license - #url'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect:
    actual == expected

    where:
    url                                                        || expected
    'https://opensource.org/licenses/Apache-2.0'               || 'apache-2.0.txt'
    'http://opensource.org/licenses/BSD-2-Clause'              || 'bsd-2-clause.txt'
    'http://www.opensource.org/licenses/bsd-license.php'       || null
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

  @Unroll
  def 'name normalisation resolves #name'(String name) {
    given:
    String actual = HELPER.licenseFileName(name, null)

    expect:
    actual == 'apache-2.0.txt'

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
  def 'known names resolve for every bundled license - #name'(String name, String expected) {
    given:
    String actual = HELPER.licenseFileName(name, null)

    expect:
    actual == expected

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

  @Unroll
  def 'distinct licenses do not collapse into each other - #a vs #b'(String a, String b) {
    given:
    String actual = HELPER.licenseFileName(a, null)
    String other = HELPER.licenseFileName(b, null)

    expect:
    actual != other

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
  def 'an unrelated license is not matched - #name / #url'(String name, String url) {
    given:
    String actual = HELPER.licenseFileName(name, url)

    expect:
    actual == null

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

  @Unroll
  def 'licenseKey resolves #description'(String description, String name, String url, String expected) {
    given:
    String actual = HELPER.licenseKey(name, url)

    expect:
    actual == expected

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
    given:
    String actual = HELPER.licenseKey('Some license', 'HTTPS://WWW.Example.COM/LICENSE.txt/')

    expect: 'the fallback must not leak normalisation into the report'
    actual == 'HTTPS://WWW.Example.COM/LICENSE.txt/'
  }

  @Unroll
  def 'the url wins over a conflicting name - #url'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName('MIT License', url)

    expect:
    actual == expected

    where:
    url                                              || expected
    'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'https://opensource.org/licenses/GPL-3.0'        || 'gpl-3.0.txt'
    'http://website.tld/'                            || 'mit.txt' // unknown url falls through to the name
  }

  def 'licenseText returns the bundled text of a known license'() {
    when:
    String text = HELPER.licenseText('apache-2.0.txt')
    List<String> missing = ['Apache License', 'Version 2.0, January 2004'].findAll { String marker ->
      !text?.contains(marker)
    }

    then:
    text != null

    and: 'naming the marker that went missing, rather than just failing'
    missing == []
  }

  def 'licenseText returns null when the plugin does not bundle the license'() {
    given:
    String actual = HELPER.licenseText('does-not-exist.txt')

    expect:
    actual == null
  }

  @Unroll
  def 'licenseText is non-empty for every bundled license - #fileName'(String fileName) {
    given:
    String text = HELPER.licenseText(fileName)?.trim()

    expect:
    text

    where:
    fileName << LicenseHelper.INSTANCE.bundledFileNames()
  }

  @Unroll
  def 'isBundled is true only for bundled file names - #fileName'(String fileName, boolean expected) {
    given:
    boolean actual = HELPER.isBundled(fileName)

    expect:
    actual == expected

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
  def 'the added license #id resolves by bare SPDX id'(String id, String expected) {
    given:
    String actual = HELPER.licenseFileName(id, null)

    expect:
    actual == expected

    where:
    id                                 || expected
    '0BSD'                             || '0bsd.txt'
    'AGPL-3.0'                         || 'agpl-3.0.txt'
    'Apache-1.1'                       || 'apache-1.1.txt'
    'BSD-4-Clause'                     || 'bsd-4-clause.txt'
    'CC-BY-4.0'                        || 'cc-by-4.0.txt'
    'CC-BY-SA-4.0'                     || 'cc-by-sa-4.0.txt'
    'CDDL-1.0'                         || 'cddl-1.0.txt'
    'CDDL-1.1'                         || 'cddl-1.1.txt'
    'EPL-1.0'                          || 'epl-1.0.txt'
    'GPL-2.0-with-classpath-exception' || 'gpl-2.0-with-classpath-exception.txt'
    'ISC'                              || 'isc.txt'
    'LGPL-2.0'                         || 'lgpl-2.0.txt'
    'MIT-0'                            || 'mit-0.txt'
    'Unlicense'                        || 'unlicense.txt'
  }

  @Unroll
  def 'the added license resolves by name - #name'(String name, String expected) {
    given:
    String actual = HELPER.licenseFileName(name, null)

    expect:
    actual == expected

    where:
    name                                                     || expected
    'BSD Zero Clause License'                                || '0bsd.txt'
    'GNU Affero General Public License v3.0'                 || 'agpl-3.0.txt'
    'Apache License 1.1'                                     || 'apache-1.1.txt'
    'The Apache Software License, Version 1.1'               || 'apache-1.1.txt'
    'BSD 4-Clause "Original" or "Old" License'               || 'bsd-4-clause.txt'
    'Creative Commons Attribution 4.0 International'         || 'cc-by-4.0.txt'
    'Common Development and Distribution License (CDDL) v1.0'|| 'cddl-1.0.txt'
    'CDDL 1.1'                                               || 'cddl-1.1.txt'
    'Eclipse Public License 1.0'                             || 'epl-1.0.txt'
    'Eclipse Public License v1.0'                            || 'epl-1.0.txt'
    'Eclipse Public License - v 1.0'                         || 'epl-1.0.txt'
    'ISC License'                                            || 'isc.txt'
    'GNU Lesser General Public License v2.0'                 || 'lgpl-2.0.txt'
    'MIT No Attribution'                                     || 'mit-0.txt'
    'The Unlicense'                                          || 'unlicense.txt'
    'GNU General Public License v2.0 w/Classpath exception'  || 'gpl-2.0-with-classpath-exception.txt'
  }

  @Unroll
  def 'the added license resolves by url - #url'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect:
    actual == expected

    where:
    url                                                  || expected
    'https://www.eclipse.org/legal/epl-v10.html'         || 'epl-1.0.txt'
    'http://www.opensource.org/licenses/cddl1.php'       || 'cddl-1.0.txt'
    'https://opensource.org/licenses/CDDL-1.0'           || 'cddl-1.0.txt'
    'https://opensource.org/licenses/ISC'                || 'isc.txt'
    'https://unlicense.org/'                             || 'unlicense.txt'
    'https://opensource.org/licenses/0BSD'               || '0bsd.txt'
    'https://www.gnu.org/licenses/agpl-3.0.txt'          || 'agpl-3.0.txt'
    'https://www.gnu.org/licenses/lgpl-2.0.txt'          || 'lgpl-2.0.txt'
    'https://creativecommons.org/licenses/by/4.0/'       || 'cc-by-4.0.txt'
    'https://creativecommons.org/licenses/by-sa/4.0/'    || 'cc-by-sa-4.0.txt'
    'https://openjdk.java.net/legal/gplv2+ce.html'       || 'gpl-2.0-with-classpath-exception.txt'
    'https://spdx.org/licenses/EPL-1.0.html'             || 'epl-1.0.txt'
    'https://spdx.org/licenses/ISC.html'                 || 'isc.txt'
  }

  @Unroll
  def 'the Eclipse Distribution License maps to the BSD 3-Clause text - #nameOrUrl'(String nameOrUrl, String name, String url) {
    given:
    String actual = HELPER.licenseFileName(name, url)

    expect: 'EDL-1.0 is textually BSD-3-Clause, so it aliases rather than duplicating the file'
    actual == 'bsd-3-clause.txt'

    where:
    nameOrUrl                   | name                              | url
    'the name'                  | 'Eclipse Distribution License 1.0'| null
    'the dashed name'           | 'Eclipse Distribution License - v 1.0' | null
    'the short name'            | 'EDL 1.0'                         | null
    'the url'                   | null | 'http://www.eclipse.org/org/documents/edl-v10.html'
  }

  @Unroll
  def 'the added licenses stay distinct from their neighbours - #a vs #b'(String a, String b) {
    given:
    String actual = HELPER.licenseFileName(a, null)
    String other = HELPER.licenseFileName(b, null)

    expect:
    actual != other

    where:
    a          | b
    'EPL-1.0'  | 'EPL-2.0'
    'CDDL-1.0' | 'CDDL-1.1'
    'LGPL-2.0' | 'LGPL-2.1'
    'GPL-2.0'  | 'GPL-2.0-with-classpath-exception'
    'AGPL-3.0' | 'GPL-3.0'
    'MIT'      | 'MIT-0'
    'Apache-1.1' | 'Apache-2.0'
    'BSD-3-Clause' | 'BSD-4-Clause'
    'CC-BY-4.0' | 'CC-BY-SA-4.0'
    'CC0-1.0'  | 'CC-BY-4.0'
  }

  def 'the classpath exception text contains both the GPL and the exception'() {
    when:
    String text = HELPER.licenseText('gpl-2.0-with-classpath-exception.txt')
    List<String> missing = [
      'GNU GENERAL PUBLIC LICENSE',
      'CLASSPATH EXCEPTION',
      'link this library with independent modules',
    ].findAll { String marker -> !text?.contains(marker) }

    then: 'naming the marker that went missing, rather than just failing'
    missing == []
  }

  // ---------------------------------------------------------------------------------------------
  // Real-world POM spellings that used to miss, or worse, resolve to the wrong license.
  // ---------------------------------------------------------------------------------------------

  def 'BUG: an ambiguous url no longer overrides a name that names the BSD variant'() {
    given:
    String actual = HELPER.licenseFileName('New BSD License', 'http://www.opensource.org/licenses/bsd-license.php')
    String twoClause = HELPER.licenseFileName('BSD 2-Clause License', 'http://www.opensource.org/licenses/bsd-license.php')
    String urlAlone = HELPER.licenseFileName(null, 'http://www.opensource.org/licenses/bsd-license.php')

    expect: 'hamcrest declares "New BSD License" with the retired OSI bsd-license.php url'
    actual == 'bsd-3-clause.txt'

    and: 'the same url with a 2-clause name still resolves to 2-clause'
    twoClause == 'bsd-2-clause.txt'

    and: 'on its own it names no variant, so no text is asserted - the report links it instead'
    urlAlone == null
  }

  @Unroll
  def 'an unambiguous url still wins over a conflicting name - #url'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName('MIT License', url)

    expect: 'only the urls listed as ambiguous defer to the name'
    actual == expected

    where:
    url                                              || expected
    'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'https://opensource.org/licenses/GPL-3.0'        || 'gpl-3.0.txt'
  }

  @Unroll
  def 'BUG: the modern SPDX id #id resolves'(String id, String expected) {
    given:
    String actual = HELPER.licenseFileName(id, null)

    expect: 'SPDX deprecated the bare GNU ids in 2018; current tooling emits -only/-or-later'
    actual == expected

    where:
    id                                          || expected
    'GPL-2.0-only'                              || 'gpl-2.0.txt'
    'GPL-2.0-or-later'                          || 'gpl-2.0.txt'
    'GPL-3.0-only'                              || 'gpl-3.0.txt'
    'GPL-3.0-or-later'                          || 'gpl-3.0.txt'
    'LGPL-2.0-only'                             || 'lgpl-2.0.txt'
    'LGPL-2.1-only'                             || 'lgpl-2.1.txt'
    'LGPL-2.1-or-later'                         || 'lgpl-2.1.txt'
    'LGPL-3.0-only'                             || 'lgpl-3.0.txt'
    'AGPL-3.0-only'                             || 'agpl-3.0.txt'
    'AGPL-3.0-or-later'                         || 'agpl-3.0.txt'
    'GPL-2.0-only WITH Classpath-exception-2.0' || 'gpl-2.0-with-classpath-exception.txt'
    'GPL-2.0 WITH Classpath-exception-2.0'      || 'gpl-2.0-with-classpath-exception.txt'
  }

  @Unroll
  def 'BUG: the gnu.org retired-license url #url resolves'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect: 'gnu.org serves every retired license under /old-licenses/'
    actual == expected

    where:
    url                                                              || expected
    'https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html'        || 'lgpl-2.1.txt'
    'http://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html'      || 'lgpl-2.1.txt'
    'https://www.gnu.org/licenses/old-licenses/gpl-2.0.html'         || 'gpl-2.0.txt'
    'https://www.gnu.org/licenses/old-licenses/lgpl-2.0.html'        || 'lgpl-2.0.txt'
  }

  @Unroll
  def 'a query string, fragment or port does not defeat the url - #url'(String url) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect:
    actual == 'apache-2.0.txt'

    where:
    url << [
      'https://www.apache.org/licenses/LICENSE-2.0.txt?raw=true',
      'https://www.apache.org/licenses/LICENSE-2.0#section',
      'https://www.apache.org/licenses/LICENSE-2.0.txt?a=b#c',
      'https://www.apache.org:443/licenses/LICENSE-2.0.txt',
      '//www.apache.org/licenses/LICENSE-2.0.txt',
      'https://www.apache.org/licenses/LICENSE-2.0.txt.html',
      'https://www.apache.org/licenses/LICENSE-2.0.html.txt',
    ]
  }

  @Unroll
  def 'unusual whitespace inside a name does not block a match - #description'(String description, String name) {
    given:
    String actual = HELPER.licenseFileName(name, null)

    expect:
    actual == 'apache-2.0.txt'

    where:
    description            | name
    'a non-breaking space' | 'Apache License 2.0'
    'a zero-width space'   | 'Apache\u200bLicense 2.0'
    'a tab'                | 'Apache\tLicense 2.0'
    'a newline'            | 'Apache\nLicense 2.0'
    'padded'               | '  Apache License 2.0  '
  }

  @Unroll
  def 'version separators are folded consistently - #name'(String name, String expected) {
    given:
    String actual = HELPER.licenseFileName(name, null)

    expect: 'the dashed and dotted spellings reach the same alias as the plain one'
    actual == expected

    where:
    name                              || expected
    'Eclipse Public License - v 1.0'  || 'epl-1.0.txt'
    'Eclipse Public License v1.0'     || 'epl-1.0.txt'
    'Eclipse Public License - v 2.0'  || 'epl-2.0.txt'
    'Eclipse Public License v. 2.0'   || 'epl-2.0.txt'
    'Eclipse Public License v2.0'     || 'epl-2.0.txt'
    'Eclipse Public License - v. 2.0' || 'epl-2.0.txt'
  }

  @Unroll
  def 'common BSD and Apache name spellings resolve - #name'(String name, String expected) {
    given:
    String actual = HELPER.licenseFileName(name, null)

    expect:
    actual == expected

    where:
    name                     || expected
    'New BSD License'        || 'bsd-3-clause.txt'
    'Modified BSD License'   || 'bsd-3-clause.txt'
    'Revised BSD License'    || 'bsd-3-clause.txt'
    'Simplified BSD License' || 'bsd-2-clause.txt'
    'FreeBSD License'        || 'bsd-2-clause.txt'
    'Apache 2'               || 'apache-2.0.txt'
    'Apache Software License 2' || 'apache-2.0.txt'
  }

  @Unroll
  def 'licenseFileName only resolves licenses the plugin bundles - #description'(String description, String name, String url, String expected) {
    given:
    String actual = HELPER.licenseFileName(name, url)

    expect:
    actual == expected

    where:
    description               | name              | url                                              || expected
    'a bundled license'       | 'The MIT License' | 'https://spdx.org/licenses/MIT.html'             || 'mit.txt'
    'a bundled license by url'| 'Anything at all' | 'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'an unbundled license'    | 'Some license'    | 'http://website.tld/'                            || null
    'no license at all'       | null              | null                                             || null
  }

  // -------------------------------------------------------------------------------------------
  // Real POMs that produced a WRONG license, found by probing ~1300 POMs from the local caches.
  // -------------------------------------------------------------------------------------------

  def 'BUG: an ambiguous url is not used as a fallback when the name says nothing'() {
    given:
    String actual = HELPER.licenseFileName('The BSD License', 'http://www.opensource.org/licenses/bsd-license.php')
    String key = HELPER.licenseKey('The BSD License', 'http://www.opensource.org/licenses/bsd-license.php')
    String named = HELPER.licenseFileName('New BSD License', 'http://www.opensource.org/licenses/bsd-license.php')

    expect: 'jline declares "The BSD License" against the retired OSI page but is BSD-3-Clause'
    actual == null

    and: 'so the report links the page instead of asserting a variant'
    key == 'http://www.opensource.org/licenses/bsd-license.php'

    and: 'a name that does state the variant still wins'
    named == 'bsd-3-clause.txt'
  }

  def 'BUG: the glassfish CDDL+GPL page names two licenses, so the name decides'() {
    given: 'jaxb-api cites the same dual-license url for both of its license entries'
    String url = 'https://glassfish.java.net/public/CDDL+GPL_1_1.html'
    String actual = HELPER.licenseFileName('GPL2 w/ CPE', url)
    String cddl = HELPER.licenseFileName('CDDL 1.1', url)
    String gplKey = HELPER.licenseKey('GPL2 w/ CPE', url)
    String cddlKey = HELPER.licenseKey('CDDL 1.1', url)

    expect: 'the GPL arm is no longer swallowed and reported as CDDL'
    actual == 'gpl-2.0-with-classpath-exception.txt'

    and: 'the CDDL arm still resolves'
    cddl == 'cddl-1.1.txt'

    and: 'the two arms no longer collapse onto one key'
    gplKey != cddlKey
  }

  def 'BUG: the Common Public License is not the Eclipse Public License'() {
    given:
    String actual = HELPER.licenseFileName('Common Public License Version 1.0', 'http://www.opensource.org/licenses/cpl1.0.txt')
    String byName = HELPER.licenseFileName('Common Public License 1.0', null)

    expect: 'junit 4.10 declares CPL-1.0, which the plugin does not bundle'
    actual == null

    and: 'it must not be labelled with the EPL text, which is a different license'
    byName != 'epl-1.0.txt'
  }

  @Unroll
  def 'a locale path segment does not defeat the url - #url'(String url) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect: 'h2 cites the localised Mozilla url'
    actual == 'mpl-2.0.txt'

    where:
    url << [
      'https://www.mozilla.org/en-US/MPL/2.0/',
      'https://www.mozilla.org/MPL/2.0/',
      'https://www.mozilla.org/fr/MPL/2.0/',
    ]
  }

  @Unroll
  def 'a space-separated SPDX id resolves like its hyphenated form - #name'(String name, String expected) {
    given:
    String actual = HELPER.licenseFileName(name, null)

    expect:
    actual == expected

    where:
    name          || expected
    'MPL 2.0'     || 'mpl-2.0.txt'
    'EPL 2.0'     || 'epl-2.0.txt'
    'GPL 3.0'     || 'gpl-3.0.txt'
    'LGPL 2.1'    || 'lgpl-2.1.txt'
    'AGPL 3.0'    || 'agpl-3.0.txt'
    'CC0 1.0'     || 'cc0-1.0.txt'
    'MIT 0'       || 'mit-0.txt'
  }

  @Unroll
  def 'the bundled text really is #fileName, not another license'(String fileName, String mustContain, List<String> mustNotContain) {
    given: 'the phrase, together with what must be absent, identifies exactly one bundled license'
    String text = HELPER.licenseText(fileName)
    boolean present = text.contains(mustContain)
    List<String> wronglyPresent = mustNotContain.findAll { String marker -> text.contains(marker) }

    expect:
    present

    and: 'naming the phrase that should not be there, rather than just failing'
    wronglyPresent == []

    where:
    fileName | mustContain | mustNotContain
    '0bsd.txt'                             | 'BSD Zero Clause License'                               | []
    'agpl-3.0.txt'                         | 'Version 3, 19 November 2007'                           | []
    'apache-1.1.txt'                       | 'The Apache Software License, Version 1.1'              | []
    'apache-2.0.txt'                       | 'Version 2.0, January 2004'                             | []
    'bsd-2-clause.txt'                     | 'BSD 2-Clause License'                                  | []
    'bsd-3-clause.txt'                     | 'BSD 3-Clause License'                                  | []
    'bsd-4-clause.txt'                     | 'BSD 4-Clause License'                                  | []
    'cc-by-4.0.txt'                        | 'Attribution 4.0 International'                         | ['ShareAlike']
    'cc-by-sa-4.0.txt'                     | 'Attribution-ShareAlike 4.0 International'              | []
    'cc0-1.0.txt'                          | 'CC0 1.0 Universal'                                     | []
    'cddl-1.0.txt'                         | 'Sun Microsystems, Inc. is the initial license steward' | []
    'cddl-1.1.txt'                         | 'Oracle is the initial license steward'                 | []
    'epl-1.0.txt'                          | 'Eclipse Public License - v 1.0'                        | []
    'epl-2.0.txt'                          | 'Eclipse Public License - v 2.0'                        | []
    'gpl-2.0-with-classpath-exception.txt' | 'CLASSPATH EXCEPTION'                                   | []
    'gpl-2.0.txt'                          | 'Version 2, June 1991'                                  | ['CLASSPATH EXCEPTION', 'LIBRARY GENERAL PUBLIC LICENSE']
    'gpl-3.0.txt'                          | 'Version 3, 29 June 2007'                               | ['LESSER GENERAL PUBLIC LICENSE']
    'isc.txt'                              | 'ISC License'                                           | []
    'lgpl-2.0.txt'                         | 'GNU LIBRARY GENERAL PUBLIC LICENSE'                    | []
    'lgpl-2.1.txt'                         | 'Version 2.1, February 1999'                            | []
    'lgpl-3.0.txt'                         | 'GNU LESSER GENERAL PUBLIC LICENSE'                     | ['Version 2.1, February 1999']
    'mit-0.txt'                            | 'MIT No Attribution'                                    | []
    'mit.txt'                              | 'MIT License'                                           | ['No Attribution']
    'mpl-2.0.txt'                          | 'Mozilla Public License Version 2.0'                    | []
    'unlicense.txt'                        | 'unencumbered software released into the public domain' | []
  }

  def 'the identity table covers every bundled license'() {
    given: 'so a license added without an identity assertion fails here'
    Set<String> asserted = [
      '0bsd.txt',
      'agpl-3.0.txt',
      'apache-1.1.txt',
      'apache-2.0.txt',
      'bsd-2-clause.txt',
      'bsd-3-clause.txt',
      'bsd-4-clause.txt',
      'cc-by-4.0.txt',
      'cc-by-sa-4.0.txt',
      'cc0-1.0.txt',
      'cddl-1.0.txt',
      'cddl-1.1.txt',
      'epl-1.0.txt',
      'epl-2.0.txt',
      'gpl-2.0-with-classpath-exception.txt',
      'gpl-2.0.txt',
      'gpl-3.0.txt',
      'isc.txt',
      'lgpl-2.0.txt',
      'lgpl-2.1.txt',
      'lgpl-3.0.txt',
      'mit-0.txt',
      'mit.txt',
      'mpl-2.0.txt',
      'unlicense.txt',
    ] as Set

    Set<String> bundled = HELPER.bundledFileNames()

    expect:
    asserted == bundled
  }

  def 'no two bundled licenses have identical text'() {
    given: 'a copy-paste that duplicates a license would otherwise ship silently'
    List<String> texts = HELPER.bundledFileNames().collect { String fileName -> HELPER.licenseText(fileName) }
    int distinct = texts.toSet().size()
    int total = texts.size()

    expect:
    distinct == total
  }


  // ---------------------------------------------------------------------------------------------
  // URL matching: OSI's current path, spdx.org anchoring, and locale stripping.
  // ---------------------------------------------------------------------------------------------

  @Unroll
  def 'BUG: OSI now serves the singular /license/ path - #url'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect: 'both the current and the retired form resolve'
    actual == expected

    where:
    url                                              || expected
    'https://opensource.org/license/mit'             || 'mit.txt'
    'https://opensource.org/license/apache-2-0'      || 'apache-2.0.txt'
    'https://opensource.org/license/bsd-3-clause'    || 'bsd-3-clause.txt'
    'https://opensource.org/license/gpl-2-0'         || 'gpl-2.0.txt'
    'https://opensource.org/license/mpl-2-0'         || 'mpl-2.0.txt'
    'https://opensource.org/license/epl-2-0'         || 'epl-2.0.txt'
    'https://opensource.org/licenses/MIT'            || 'mit.txt'
    'https://opensource.org/licenses/Apache-2.0'     || 'apache-2.0.txt'
    'https://opensource.org/licenses/BSD-3-Clause'   || 'bsd-3-clause.txt'
  }

  @Unroll
  def 'BUG: spdx.org is matched as a prefix, not anywhere in the url - #url'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect:
    actual == expected

    where:
    url                                                    || expected
    'https://spdx.org/licenses/MIT.html'                   || 'mit.txt'
    'https://spdx.org/licenses/MIT'                        || 'mit.txt'
    'https://example.com/spdx.org/licenses/MIT'            || null
    'https://evil.example.com/mirror/spdx.org/licenses/MIT'|| null
    'https://notspdx.org/licenses/MIT'                     || null
  }

  @Unroll
  def 'BUG: only a real language code is stripped from a url - #url'(String url, String expected) {
    given:
    String actual = HELPER.licenseFileName(null, url)

    expect:
    actual == expected

    where:
    url                                             || expected
    'https://www.gnu.org/licenses/gpl-3.0.de'       || 'gpl-3.0.txt'
    'http://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html' || 'lgpl-2.1.txt'
    'https://opensource.org/licenses/MIT.rb'        || null
    'https://opensource.org/licenses/MIT.py'        || null
    'https://opensource.org/licenses/MIT.gz'        || null
  }

}
