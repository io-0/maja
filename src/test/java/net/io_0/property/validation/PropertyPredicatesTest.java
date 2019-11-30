package net.io_0.property.validation;

import net.io_0.property.Property;
import org.junit.jupiter.api.Disabled;
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

  @Test
  @Disabled("Not implemented yet")
  void unassignedOrNotEmptyAnd() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void lte() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void lt() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void gte() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void gt() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void lengthGte() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void lengthLte() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void sizeGt() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void sizeLt() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void regexMatch() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void multipleOf() { //TODO
  }

  @Test
  @Disabled("Not implemented yet")
  void compare() { //TODO
  }
}