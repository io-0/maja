package net.io_0.pb;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * Helps remember what properties were set trough setter.
 * Can only work if all properties are set via setter and each setter calls markPropertySet.
 * This means one can't use e.g. lombok @Setter, @AllArgsConstructor, or @Builder
 */
public abstract class SetPropertiesAware {
  private Set<String> setProperties = new HashSet<>();

  /**
   * Marks a property as set, meant to be called within setter
   * @param name property name
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
   * @param name property name
   * @return property
   */
  public <T> Property<T> getProperty(String name) {
    return getProperty(name, name);
  }

  /**
   * Get property representation
   * @param name property name
   * @param label use if desired name is not java compliant
   * @return property
   */
  @SuppressWarnings("unchecked")
  public <T> Property<T> getProperty(String name, String label) {
    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
        if (pd.getReadMethod() != null && name.equals(pd.getName())) {
          return new Property<>(label, (T) pd.getReadMethod().invoke(this), isPropertySet(name));
        }
      }
    } catch (ClassCastException | IllegalAccessException | IntrospectionException | InvocationTargetException e) {
      throw new IllegalArgumentException(String.format("couldn't access property with name '%s'", name), e);
    }

    throw new IllegalArgumentException(String.format("property with name '%s' not found", name));
  }
}
