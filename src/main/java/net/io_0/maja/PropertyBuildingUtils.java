package net.io_0.maja;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

public interface PropertyBuildingUtils {
  static <T> Optional<Property<T>> extractProperty(Object model, String propertyName, Function<PropertyDescriptor, Property<T>> constructor) {
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(model.getClass()).getPropertyDescriptors()) {
        if (pd.getReadMethod() != null && propertyName.equals(pd.getName())) {
          return Optional.of(constructor.apply(pd));
        }
      }
    } catch (IntrospectionException e) {
      throw new IllegalArgumentException(
        String.format("Couldn't access property with name '%s' on %s", propertyName, model.getClass().getSimpleName()), e
      );
    }
    return Optional.empty();
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
