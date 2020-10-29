package net.io_0.maja;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Narrative:
 *   As a property issues API consumer
 *
 *   I want convenient ways to create them
 */
class PropertyIssuesTest {
  /**
   * Scenario: It should be convenient to create issues
   */
  @Test
  void creationConvenience() {
    // Given property issues
    PropertyIssue pI1 = PropertyIssue.of(propertyName + "1", errorCode + "1", errorMessage + "1");
    PropertyIssue pI2 = PropertyIssue.of(propertyName + "2", errorCode + "2", errorMessage + "2");
    PropertyIssue pI3 = PropertyIssue.of(propertyName + "3", errorCode + "3", errorMessage + "3");

    // When they are collected
    PropertyIssues pIs = PropertyIssues.of(pI1, pI2, pI3);

    // Then they should be present
    assertTrue(pIs.containsPropertyName(propertyName + "1"));
    assertTrue(pIs.containsPropertyName(propertyName + "2"));
    assertTrue(pIs.containsPropertyName(propertyName + "3"));
    assertEquals(
      "name1 -> code1 (issue1); " +
      "name2 -> code2 (issue2); " +
      "name3 -> code3 (issue3)",
      pIs.toString()
    );
  }

  /**
   * Scenario: It should be convenient to add issues with a property name prefix
   */
  @Test
  void addWithPropertyNamePrefixConvenience() {
    // Given property issues
    PropertyIssue pI1 = PropertyIssue.of(propertyName + "1", errorCode + "1", errorMessage + "1");
    PropertyIssue pI2 = PropertyIssue.of(propertyName + "2", errorCode + "2", errorMessage + "2");
    PropertyIssue pI3 = PropertyIssue.of(propertyName + "3", errorCode + "3", errorMessage + "3");

    // When they are added with property name prefixes
    PropertyIssues pIs = PropertyIssues.of();
    pIs.withPropertyNamePrefix("prefix1.").add(pI1);
    pIs.withPropertyNamePrefix("prefix2.").addAll(PropertyIssues.of(pI2, pI3));

    // Then they should be present
    assertTrue(pIs.containsPropertyName("prefix1." + propertyName + "1"));
    assertTrue(pIs.containsPropertyName("prefix2." + propertyName + "2"));
    assertTrue(pIs.containsPropertyName("prefix2." + propertyName + "3"));
    assertEquals(
      "prefix1.name1 -> code1 (issue1); " +
      "prefix2.name2 -> code2 (issue2); " +
      "prefix2.name3 -> code3 (issue3)",
      pIs.toString()
    );
  }

  private static String errorCode = "code";
  private static String errorMessage = "issue";
  private static String propertyName = "name";
}