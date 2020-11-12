package net.io_0.maja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Function;

import static net.io_0.maja.PropertyBuildingUtils.*;

@RequiredArgsConstructor
@Getter
@Slf4j
public class PojoProperty<T> implements Property<T> {
  private final String name;
  private final T value;

  public static <T> Property<T> from(Object pojo, String propertyName) {
    return PropertyBuildingUtils.<T> extractProperty(pojo, propertyName, constructWith(pojo, propertyName, propertyName))
      .or(() -> annotatedNameToJavaName(pojo, propertyName).flatMap(javaName ->
        extractProperty(pojo, javaName, constructWith(pojo, propertyName, javaName))
      ))
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("Property with name '%s' not found on %s", propertyName, pojo.getClass().getSimpleName())
      ));
  }

  @Override
  public boolean isNull() {
    return Objects.isNull(value);
  }

  @Override
  public T getValue() {
    Objects.requireNonNull(value);
    return value;
  }

  @Override
  public boolean isAssigned() {
    return true;
  }

  @SuppressWarnings("unchecked")
  private static <T> Function<PropertyDescriptor, Property<T>> constructWith(Object pojo, String propertyName, String javaName) {
    return propertyDescriptor -> {
      try {
        return new PojoProperty<>(propertyName, (T) propertyDescriptor.getReadMethod().invoke(pojo));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new IllegalArgumentException(
          String.format("Couldn't access property with name '%s' on %s", javaName, pojo.getClass().getSimpleName()), e
        );
      }
    };
  }
}
