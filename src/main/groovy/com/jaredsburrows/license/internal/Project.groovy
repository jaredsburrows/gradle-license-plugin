package com.jaredsburrows.license.internal

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
@Builder
@EqualsAndHashCode(includeFields = true, useCanEqual = false)
@ToString(includeNames = true, includePackage = false)
final class Project {
  String name
  License license
  String url
  String developers
  String year
}
