package net.io_0.maja;

import net.io_0.maja.PropertyIssue.Issue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Narrative:
 *   As a property issue API consumer
 *
 *   I want convenient ways to create and manipulate them
 */
class PropertyIssueTest {
  /**
   * Scenario: It should be convenient to create issues
   */
  @Test
  void creationConvenience() {
    // Given an error code, an error message and a property name they relate to
    String errorCode = "code";
    String errorMessage = "issue";
    String propertyName = "name";

    // When an issue is conveniently created
    PropertyIssue pI = PropertyIssue.of(propertyName, errorCode, errorMessage);

    // Then its content should represent our data
    assertEquals(propertyName, pI.getPropertyName());
    assertEquals(errorCode, pI.getIssue().getCode());
    assertEquals(errorMessage, pI.getIssue().getMessage());
    assertEquals("PropertyIssue(propertyName=name, issue=Issue(code=code, message=issue))", pI.toString());
  }

  /**
   * Scenario: It should be convenient to adapt a issue message
   */
  @Test
  void messageAdaptionConvenience() {
    // Given a property issue
    PropertyIssue pI = PropertyIssue.of(propertyName, Issue.of(errorCode, errorMessage));

    // When its message is conveniently adapted (it's immutable therefore we work with the new instance)
    String newErrorMessage = "other issue";
    pI = pI.withMessage(newErrorMessage);

    // Then its content should represent our data
    assertEquals(propertyName, pI.getPropertyName());
    assertEquals(errorCode, pI.getIssue().getCode());
    assertEquals(newErrorMessage, pI.getIssue().getMessage());
    assertEquals("PropertyIssue(propertyName=name, issue=Issue(code=code, message=other issue))", pI.toString());
  }

  /**
   * Scenario: It should be convenient to prefix an issue property name
   */
  @Test
  void propertyNameAdaptionConvenience() {
    // Given a property issue
    PropertyIssue pI = PropertyIssue.of(propertyName, Issue.of(errorCode, errorMessage));

    // When its property name is conveniently prefixed (it's immutable therefore we work with the new instance)
    String prefix = "obj.";
    pI = pI.withPropertyNamePrefix(prefix);

    // Then its content should represent our data
    assertEquals(prefix + propertyName, pI.getPropertyName());
    assertEquals(errorCode, pI.getIssue().getCode());
    assertEquals(errorMessage, pI.getIssue().getMessage());
    assertEquals("PropertyIssue(propertyName=obj.name, issue=Issue(code=code, message=issue))", pI.toString());
  }

  private static String errorCode = "code";
  private static String errorMessage = "issue";
  private static String propertyName = "name";
}