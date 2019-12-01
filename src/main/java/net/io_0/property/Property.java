package net.io_0.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Objects;

@RequiredArgsConstructor
@Getter
public class Property<T> {
  private final String name;
  private final T value;
  private final boolean assigned;

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
