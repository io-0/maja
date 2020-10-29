package net.io_0.maja;

import net.io_0.maja.models.NamedBundle;
import net.io_0.maja.models.Nested;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Narrative:
 *   As a property API consumer
 *
 *   I want convenient ways to deal with present and absent values
 */
class PropertyTest {
  /**
   * Scenario: It should be convenient to work if absent values
   */
  @Test
  void absentConvenience() {
    AtomicReference<String> cbCalled = new AtomicReference<>("");
    // Given a property
    Property<Boolean> property = new Nested().getProperty(Nested.BOOLEAN_TO_BOOLEAN);

    // When a callback for absent values is supplied
    Runnable cb = () -> cbCalled.set("called");
    property.ifUnassigned(cb);
    property.ifAssigned(b -> cbCalled.set("fail"), () -> cbCalled.set("fail"));

    // Then it should be called if value is absent
    assertEquals("called", cbCalled.get());
  }

  /**
   * Scenario: It should be convenient to work if present values
   */
  @Test
  void presentConvenience() {
    AtomicReference<String> cbCalled = new AtomicReference<>("init");
    // Given a property
    Property<Boolean> property = new Nested().setBooleanToBoolean(false).getProperty(Nested.BOOLEAN_TO_BOOLEAN);

    // When a callback for values is supplied
    Consumer<Boolean> cb = b -> cbCalled.set(format("%s, %s", cbCalled.get(), b));
    property.ifAssigned(cb);
    property.ifAssigned(cb, () -> cb.accept(true));

    // Then it should be called if value is assigned
    assertEquals("init, false, false", cbCalled.get());
  }

  /**
   * Scenario: It should be convenient to work if present null values
   */
  @Test
  void presentNullConvenience() {
    AtomicReference<String> cbCalled = new AtomicReference<>("init");
    // Given a property
    Property<Boolean> property = new Nested().setBooleanToBoolean(null).getProperty(Nested.BOOLEAN_TO_BOOLEAN);

    // When a callback for values is supplied
    Consumer<Boolean> cb = b -> cbCalled.set(format("%s, %s", cbCalled.get(), b));
    property.ifAssigned(cb);
    property.ifAssigned(cb, () -> cb.accept(true));

    // Then it should be called if value is assigned
    assertEquals("init, null, true", cbCalled.get());
  }

  /**
   * Scenario: It should be possible to extract a property even when it breaks bean naming convention
   */
  @Test
  void extractNamedTest() {
    // Given a propertyBundle
    NamedBundle propertyBundle = new NamedBundle().setASpecialName(21).setBSpecialName(9).setMaJa(100).setCaJa(73);

    // When a property is extracted that breaks bean naming conventions
    Property<Integer> propertyA = propertyBundle.getProperty(NamedBundle.A_SPECIAL_NAME);
    Property<Integer> propertyM = propertyBundle.getProperty(NamedBundle.MA_JA);

    // And a property is extracted that doesn't break them
    Property<Integer> propertyB = propertyBundle.getProperty(NamedBundle.B_SPECIAL_NAME);
    Property<Integer> propertyC = propertyBundle.getProperty(NamedBundle.CA_JA);

    // Then it should still work
    assertEquals(21, propertyA.getValue());
    assertEquals(100, propertyM.getValue());
    assertEquals(9, propertyB.getValue());
    assertEquals(73, propertyC.getValue());
  }
}