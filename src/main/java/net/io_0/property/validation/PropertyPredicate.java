package net.io_0.property.validation;

import net.io_0.property.Property;
import java.util.Objects;

@FunctionalInterface
public interface PropertyPredicate<T> {
  boolean test(Property<T> property);

  default PropertyPredicate<T> and(PropertyPredicate<T> other) {
    return t -> this.test(t) && other.test(t);
  }

  default PropertyPredicate<T> negate() {
    return t -> !this.test(t);
  }

  default PropertyPredicate<T> or(PropertyPredicate<T> other) {
    return t -> this.test(t) || other.test(t);
  }

  static <T> PropertyPredicate<T> isEqual(Object targetRef) {
    return null == targetRef ? Objects::isNull : targetRef::equals;
  }

  static <T> PropertyPredicate<T> not(PropertyPredicate<T> target) {
    return target.negate();
  }
}
