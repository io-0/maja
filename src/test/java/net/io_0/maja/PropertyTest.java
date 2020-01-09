package net.io_0.maja;

import net.io_0.maja.models.Nested;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Narrative:
 *   As a property API consumer
 *
 *   I want convenient ways to deal with present and absent values
 */
public class PropertyTest {
  /**
   * Scenario: It should be convenient to work if absent values
   */
  @Test
  public void absentConvenience() {
    AtomicReference<String> cbCalled = new AtomicReference<>("");
    // Given a property
    Property<Boolean> property = new Nested().getProperty(Nested.BOOLEAN_TO_BOOLEAN);

    // When a callback for absent values is supplied
    Runnable cb = () -> cbCalled.set("called");
    property.ifAbsent(cb);
    property.ifPresent(b -> cbCalled.set("fail"), () -> cbCalled.set("fail"));

    // Then it should be called if value is absent
    assertEquals("called", cbCalled.get());
  }

  /**
   * Scenario: It should be convenient to work if present values
   */
  @Test
  public void presentConvenience() {
    AtomicReference<String> cbCalled = new AtomicReference<>("init");
    // Given a property
    Property<Boolean> property = new Nested().setBooleanToBoolean(false).getProperty(Nested.BOOLEAN_TO_BOOLEAN);

    // When a callback for values is supplied
    Consumer<Boolean> cb = b -> cbCalled.set(String.format("%s, %s", cbCalled.get(), b));
    property.ifPresent(cb);
    property.ifPresent(cb, () -> cb.accept(true));

    // Then it should be called if value is present
    assertEquals("init, false, false", cbCalled.get());
  }

  /**
   * Scenario: It should be convenient to work if present null values
   */
  @Test
  public void presentNullConvenience() {
    AtomicReference<String> cbCalled = new AtomicReference<>("init");
    // Given a property
    Property<Boolean> property = new Nested().setBooleanToBoolean(null).getProperty(Nested.BOOLEAN_TO_BOOLEAN);

    // When a callback for values is supplied
    Consumer<Boolean> cb = b -> cbCalled.set(String.format("%s, %s", cbCalled.get(), b));
    property.ifPresent(cb);
    property.ifPresent(cb, () -> cb.accept(true));

    // Then it should be called if value is present
    assertEquals("init, null, true", cbCalled.get());
  }
}