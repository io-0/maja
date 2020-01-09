package net.io_0.maja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
@Slf4j
public class Property<T> {
  private final String name;
  private final T value;
  private final boolean assigned;

  public static <T> Property<T> from(Object pojo, String name) {
    return Property.<T> build(pojo, name, name)
      .or(() -> annotatedNameToJavaName(pojo, name).flatMap(javaName -> build(pojo, name, javaName)))
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("Property with name '%s' not found on %s", name, pojo.getClass().getSimpleName())
      ));
  }

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

  @SuppressWarnings("unchecked")
  private static <T> Optional<Property<T>> build(Object pojo, String propertyName, String javaName) {
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(pojo.getClass()).getPropertyDescriptors()) {
        if (pd.getReadMethod() != null && javaName.equals(pd.getName())) {
          return Optional.of(new Property<>(propertyName, (T) pd.getReadMethod().invoke(pojo), isPropertySet(pojo, javaName)));
        }
      }
    } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
      throw new IllegalArgumentException(
        String.format("Couldn't access property with name '%s' on %s", javaName, pojo.getClass().getSimpleName()), e
      );
    }
    return Optional.empty();
  }

  private static Optional<String> annotatedNameToJavaName(Object pojo, String annotatedName) {
    for (Field field : pojo.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(WithUnconventionalName.class) &&
          annotatedName.equals(field.getAnnotation(WithUnconventionalName.class).value())) {
        return Optional.of(field.getName());
      }
    }
    return Optional.empty();
  }

  private static boolean isPropertySet(Object pojo, String propertyName) {
    return !(pojo instanceof SetPropertiesAware) || ((SetPropertiesAware) pojo).isPropertySet(propertyName);
  }
}
