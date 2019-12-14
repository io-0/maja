package net.io_0.pb.validation;

import net.io_0.pb.PropertyIssue;
import net.io_0.pb.PropertyIssues;
import net.io_0.pb.validation.Validation.Invalid;
import org.junit.jupiter.api.Test;

import static net.io_0.pb.validation.Validation.invalid;
import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {
  @Test
  void getValidation() {
    PropertyIssues issues = PropertyIssues.of(
      PropertyIssue.of("name", "Is too short"),
      PropertyIssue.of("dateOfBirth", "No valid date")
    );
    Invalid validation = invalid(issues);
    ValidationException ex = new ValidationException(validation);
    assertEquals("Validation failed. Issues: name -> Is too short, dateOfBirth -> No valid date", ex.getMessage());
  }
}