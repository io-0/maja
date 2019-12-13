package net.io_0.property.validation;

import net.io_0.property.Property;
import org.junit.jupiter.api.Test;

import static net.io_0.property.validation.PropertyPredicates.*;
import static org.junit.jupiter.api.Assertions.*;

class PropertyPredicatesTest {
  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void assignedAndNotEmpty() {
    Property<String> p = new Property<>("name", null, false);

    assertTrue(empty.test((Property) p));
    assertFalse(assigned.test((Property) p));
    assertFalse(assignedAndNotEmpty.test((Property) p));
  }
}