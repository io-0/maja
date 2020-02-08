package net.io_0.maja;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.Character.toLowerCase;
import static java.lang.String.format;
import static java.util.Objects.isNull;

public interface PropertyBuildingUtils {
  static <T> Optional<Property<T>> extractProperty(Object model, String propertyName, Function<PropertyDescriptor, Property<T>> constructor) {
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(model.getClass()).getPropertyDescriptors()) {
        if (pd.getReadMethod() != null && propertyName.equals(firstCharToLowerCase(pd.getName()))) {
          return Optional.of(constructor.apply(pd));
        }
      }
    } catch (IntrospectionException e) {
      throw new IllegalArgumentException(
        format("Couldn't access property with name '%s' on %s", propertyName, model.getClass().getSimpleName()), e
      );
    }
    return Optional.empty();
  }

  static String firstCharToLowerCase(String input) {
    if (isNull(input) || input.isEmpty()) {
      return input;
    }
    return toLowerCase(input.charAt(0)) + input.substring(1);
  }

  static Optional<String> annotatedNameToJavaName(Object model, String annotatedName) {
    for (Field field : model.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(WithUnconventionalName.class) &&
        annotatedName.equals(field.getAnnotation(WithUnconventionalName.class).value())) {
        return Optional.of(field.getName());
      }
    }
    return Optional.empty();
  }
}
