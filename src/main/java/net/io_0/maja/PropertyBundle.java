package net.io_0.maja;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;
import static net.io_0.maja.PropertyBuildingUtils.annotatedNameToJavaName;
import static net.io_0.maja.PropertyBuildingUtils.extractProperty;

/**
 * Helps remember what properties were set trough setter.
 * Can only work if all properties are set via setter and each setter calls markPropertySet.
 * This means one can't use e.g. lombok @Setter, @AllArgsConstructor, or @Builder
 */
public abstract class PropertyBundle {
  private final Set<String> setProperties = new HashSet<>();

  /**
   * Marks a property as set, meant to be called within setter
   */
  public void markPropertySet(String name) {
    setProperties.add(name);
  }

  public void unmarkPropertySet(String name) {
    setProperties.remove(name);
  }

  public boolean isPropertySet(String name) {
    return setProperties.contains(name);
  }

  /**
   * Get property representation
   *
   * @param name property name
   * @return property
   */
  public <T> Property<T> getProperty(String name) {
    return annotatedNameToJavaName(this, name)
      .flatMap(javaName -> extractProperty(this, javaName, this.<T> constructWith(name, javaName)))
      .or(() -> extractProperty(this, name, this.constructWith(name, name)))
      .orElseThrow(() -> new IllegalArgumentException(
        format("Property with name '%s' not found on %s", name, this.getClass().getSimpleName())
      ));
  }

  @SuppressWarnings("unchecked")
  private <T> Function<PropertyDescriptor, Property<T>> constructWith(String propertyName, String javaName) {
    return propertyDescriptor -> {
      Supplier<String> name = () -> propertyName;
      Supplier<T> value = () -> {
        try {
          return (T) propertyDescriptor.getReadMethod().invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new IllegalArgumentException(
            format("Couldn't access property with name '%s' on %s", javaName, this.getClass().getSimpleName()), e
          );
        }
      };
      BooleanSupplier assigned = () -> isPropertySet(javaName);

      return new Property<>() {
        @Override
        public String getName() {
          return name.get();
        }

        @Override
        public T getValue() {
          T v = value.get();
          Objects.requireNonNull(v);
          return v;
        }

        @Override
        public boolean isAssigned() {
          return assigned.getAsBoolean();
        }

        @Override
        public boolean isNull() {
          T v = value.get();
          return Objects.isNull(v);
        }
      };
    };
  }
}