package net.io_0.maja;

import java.util.HashSet;
import java.util.Set;

/**
 * Helps remember what properties were set trough setter.
 * Can only work if all properties are set via setter and each setter calls markPropertySet.
 * This means one can't use e.g. lombok @Setter, @AllArgsConstructor, or @Builder
 */
public abstract class PropertyBundle {
  private Set<String> setProperties = new HashSet<>();

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
}
