package net.io_0.property.validation;

import net.io_0.property.Property;
import java.util.Objects;

@FunctionalInterface
public interface PropertyPredicate<T> {
  boolean test(Property<? extends T> var1);

  default PropertyPredicate<T> and(PropertyPredicate<? super T> other) {
    return t -> this.test(t) && other.test(t);
  }

  default PropertyPredicate<T> negate() {
    return t -> !this.test(t);
  }

  default PropertyPredicate<T> or(PropertyPredicate<? super T> other) {
    return t -> this.test(t) || other.test(t);
  }

  static <T> PropertyPredicate<? super T> isEqual(Object targetRef) {
    return null == targetRef ? Objects::isNull : targetRef::equals;
  }

  static <T> PropertyPredicate<? super T> not(PropertyPredicate<? super T> target) {
    return target.negate();
  }
}
