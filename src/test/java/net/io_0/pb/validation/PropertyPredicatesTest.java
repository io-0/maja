package net.io_0.pb.validation;

import net.io_0.pb.Property;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropertyPredicatesTest {
  @Test
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void assignedAndNotEmpty() {
    Property<String> p = new Property<>("name", null, false);

    assertTrue(PropertyPredicates.empty.test((Property) p));
    assertFalse(PropertyPredicates.assigned.test((Property) p));
    assertFalse(PropertyPredicates.assignedAndNotEmpty.test((Property) p));
  }
}