package com.jaredsburrows.license.internal.pom;

import java.util.List;

/**
 * Represents the information that is used to make HTML and JSON reports.
 */
public final class Project {
  private String name;
  private String description;
  private String version;
  private List<License> licenses;
  private String url;
  private List<Developer> developers;
  private String year;
  private String gav; // group/artifact/version

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public List<License> getLicenses() {
    return licenses;
  }

  public void setLicenses(List<License> licenses) {
    this.licenses = licenses;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public List<Developer> getDevelopers() {
    return developers;
  }

  public void setDevelopers(List<Developer> developers) {
    this.developers = developers;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getGav() {
    return gav;
  }

  public void setGav(String gav) {
    this.gav = gav;
  }
}
