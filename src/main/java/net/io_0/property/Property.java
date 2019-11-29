package net.io_0.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Representation of a property.
 * @param <T>
 */
@RequiredArgsConstructor
@Getter
public class Property<T> {
  private final String name;
  private final T value;
  private final boolean assigned;

  /**
   * Checks if the value is empty/null
   * @return true if empty/null, false otherwise
   */
  public boolean isEmpty() {
    return Objects.isNull(value);
  }

  /**
   * Get the value. Check if there is one first with #isEmpty().
   * @throws NullPointerException thrown if there is no value
   * @return non null property value
   */
  public T getValue() {
    Objects.requireNonNull(value);
    return value;
  }
}
