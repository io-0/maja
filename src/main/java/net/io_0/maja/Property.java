package net.io_0.maja;

import java.util.function.Consumer;

public interface Property<T> {
  String getName();
  
  /**
   * Get the value. Check if there is one first with #isEmpty().
   * @throws NullPointerException thrown if there is no value
   * @return non null property value
   */
  T getValue();
  
  boolean isAssigned();
  
  /**
   * Check if value is null.
   * @return true if property was assigned null or if property defaults to null and was not assigned
   */
  boolean isEmpty();

  static <T> Property<T> from(Object model, String propertyName) {
    return model instanceof PropertyBundle ?
      ((PropertyBundle) model).getProperty(propertyName) : PojoProperty.from(model, propertyName);
  }

  default void ifAbsent(Runnable onAbsent) {
    if (!isAssigned()) {
      onAbsent.run();
    }
  }

  default void ifPresent(Consumer<T> onValueOrNull) {
    if (isAssigned()) {
      onValueOrNull.accept(isEmpty()? null : getValue());
    }
  }

  default void ifPresent(Consumer<T> onValue, Runnable onNull) {
    if (!isAssigned()) {
      return;
    }

    if (isEmpty()) {
      onNull.run();
    } else {
      onValue.accept(getValue());
    }
  }
}