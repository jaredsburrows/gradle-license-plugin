package test

final class TestUtils {
  private TestUtils() {
    throw new AssertionError('No instances')
  }

  static def getLicenseText(def fileName) {
    getClass().getResource("/license/${fileName}").text
  }
}
