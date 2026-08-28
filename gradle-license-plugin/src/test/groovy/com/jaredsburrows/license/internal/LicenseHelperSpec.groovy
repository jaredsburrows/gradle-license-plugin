package com.jaredsburrows.license.internal

import spock.lang.Specification
import spock.lang.Unroll

final class LicenseHelperSpec extends Specification {
  @Unroll
  def 'licenseKey resolves #description'() {
    expect:
    LicenseHelper.INSTANCE.licenseKey(name, url) == expected

    where:
    description                            | name               | url                                              || expected
    'a known url'                          | 'Anything at all'  | 'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'a known name when the url is unknown' | 'The MIT License'  | 'https://spdx.org/licenses/MIT.html'             || 'mit.txt'
    'the url before the name'              | 'The MIT License'  | 'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'an unbundled license to its url'      | 'Some license'     | 'http://website.tld/'                            || 'http://website.tld/'
    'a missing url to an empty key'        | 'Some license'     | ''                                               || ''
    'nulls to an empty key'                | null               | null                                             || ''
  }

  def 'licenseText returns the bundled text of a known license'() {
    when:
    def text = LicenseHelper.INSTANCE.licenseText('apache-2.0.txt')

    then:
    text != null
    text.contains('Apache License')
    text.contains('Version 2.0, January 2004')
  }

  def 'licenseText returns null when the plugin does not bundle the license'() {
    expect:
    LicenseHelper.INSTANCE.licenseText('does-not-exist.txt') == null
  }

  @Unroll
  def 'licenseFileName only resolves licenses the plugin bundles - #description'() {
    expect:
    LicenseHelper.INSTANCE.licenseFileName(name, url) == expected

    where:
    description              | name              | url                                              || expected
    'a bundled license'      | 'The MIT License' | 'https://spdx.org/licenses/MIT.html'             || 'mit.txt'
    'a bundled license byurl'| 'Anything at all' | 'http://www.apache.org/licenses/LICENSE-2.0.txt' || 'apache-2.0.txt'
    'an unbundled license'   | 'Some license'    | 'http://website.tld/'                            || null
    'no license at all'      | null              | null                                             || null
  }

  def 'every licenseMap value has bundled text'() {
    expect:
    LicenseHelper.INSTANCE.licenseMap.values().toSet().every { fileName ->
      LicenseHelper.INSTANCE.licenseText(fileName)?.trim()
    }
  }
}
