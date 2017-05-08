package com.jaredsburrows.license.internal.report

import com.jaredsburrows.license.internal.pom.License
import com.jaredsburrows.license.internal.pom.Project
import groovy.xml.MarkupBuilder

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class HtmlReport {
  final static BODY_CSS = "body{font-family: sans-serif} "
  final static PRE_CSS = "pre{background-color: #eeeeee; padding: 1em; white-space: pre-wrap}"
  final static CSS_STYLE = BODY_CSS + PRE_CSS
  final static OPEN_SOURCE_LIBRARIES = "Open source licenses"
  final static NO_OPEN_SOURCE_LIBRARIES = "No open source libraries"
  final static NOTICE_LIBRARIES = "Notice for libraries:"
  final List<Project> projects

  HtmlReport(projects) {
    this.projects = projects
  }

  /**
   * Html report when there are no open source licenses.
   */
  static noOpenSourceHtml() {
    final writer = new StringWriter()
    final markup = new MarkupBuilder(writer)
    markup.html {
      head {
        style(CSS_STYLE)
        title(OPEN_SOURCE_LIBRARIES)
      }

      body {
        h3(NO_OPEN_SOURCE_LIBRARIES)
      }
    }
    writer.toString()
  }

  /**
   * Html report when there are open source licenses.
   */
  def openSourceHtml() {
    final writer = new StringWriter()
    final markup = new MarkupBuilder(writer)
    final Set<License> licenses = new HashSet<>()
    markup.html {
      head {
        style(CSS_STYLE)
        title(OPEN_SOURCE_LIBRARIES)
      }

      body {
        h3(NOTICE_LIBRARIES)
        ul {
          projects.each { project ->
            licenses << project.license
            li {
              a(href: String.format("%s%s", "#", project.license.hashCode()), project.name)
            }
          }
        }

        licenses.each { license ->
          a(name: license.hashCode())
          h3(license.name)
          pre(String.format("%s, %s", license.name, license.url))
        }
      }
    }
    writer.toString()
  }

  /**
   * Return Html as a String.
   */
  def string() {
    projects.empty ? noOpenSourceHtml() : openSourceHtml()
  }
}
