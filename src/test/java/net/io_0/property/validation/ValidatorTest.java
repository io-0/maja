package net.io_0.property.validation;

import net.io_0.property.PropertyIssues;
import org.junit.jupiter.api.Test;

import static net.io_0.property.validation.Validation.invalid;
import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {
  @Test
  void proceedIfValidWithValid() {
    String message = "valid";
    Validator<?> validator = Validation::valid;

    try { validator.proceedIfValid(null); }
    catch (ValidationException e) { message = e.getMessage(); }

    assertEquals("valid", message);
  }

  @Test
  void proceedIfValidWithInvalid() {
    String message = "valid";
    Validator<?> validator = ignored -> invalid(PropertyIssues.of());

    try { validator.proceedIfValid(null); }
    catch (ValidationException e) { message = e.getMessage(); }

    assertEquals("Validation failed.", message);
  }
}