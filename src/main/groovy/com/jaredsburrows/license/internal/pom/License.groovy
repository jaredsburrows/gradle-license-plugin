package com.jaredsburrows.license.internal.pom

import groovy.transform.EqualsAndHashCode

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
@EqualsAndHashCode(includes = "url", includeFields = true, useCanEqual = false)
final class License {
  String name
  String url
}
