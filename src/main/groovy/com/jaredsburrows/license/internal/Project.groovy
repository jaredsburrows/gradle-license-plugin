package com.jaredsburrows.license.internal

import groovy.transform.builder.Builder

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
@Builder
final class Project {
  String name
  License license
  String url
  String developers
  String year
}
