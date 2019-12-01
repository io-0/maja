package net.io_0.property.validation;

import net.io_0.property.Property;
import org.junit.jupiter.api.Test;

import static net.io_0.property.validation.PropertyPredicates.*;
import static org.junit.jupiter.api.Assertions.*;

class PropertyPredicatesTest {
  @Test
  public void assignedAndNotEmpty() {
    Property<String> p = new Property<>("name", null, false);

    assertTrue(empty.test(p));
    assertFalse(assigned.test(p));
    assertFalse(assignedAndNotEmpty.test(p));
  }
}