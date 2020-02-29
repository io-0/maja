package net.io_0.maja.validation;

import net.io_0.maja.Property;
import net.io_0.maja.models.IntegerBundle;
import org.junit.jupiter.api.Test;

import static net.io_0.maja.validation.PropertyValidator.andAll;
import static net.io_0.maja.validation.PropertyValidators.*;
import static org.junit.jupiter.api.Assertions.*;

class PropertyValidatorTest {
  // helper to check if and() has problems with types e.g. if Integer is properly downcast to Number
  @Test
  void andTypeTest() {
    Property<Number> property = Property.from(new IntegerBundle().setTwo(9), IntegerBundle.TWO);

    assertTrue(andAll(minimum(1), maximum(10)).validate(property).isValid());
    assertTrue(minimum(1).and(maximum(10)).validate(property).isValid());
  }
}