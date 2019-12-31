package net.io_0.pb.experiments.validation;

import net.io_0.pb.experiments.PropertyIssue;
import net.io_0.pb.experiments.PropertyIssues;
import net.io_0.pb.experiments.validation.Validation.Invalid;
import org.junit.jupiter.api.Test;

import static net.io_0.pb.experiments.validation.Validation.invalid;
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