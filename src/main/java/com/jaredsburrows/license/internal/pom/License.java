package com.jaredsburrows.license.internal.pom;

import java.util.Objects;

/**
 * Represents license information in a POM file.
 */
public final class License {
  private String name;
  private String url;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof License)) return false;
    License license = (License) o;
    return Objects.equals(url, license.url);
  }

  @Override public int hashCode() {
    return Objects.hash(url);
  }
}
