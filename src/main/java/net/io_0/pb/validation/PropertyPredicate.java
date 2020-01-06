package net.io_0.pb.validation;

import net.io_0.pb.Property;

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

  static <T> PropertyPredicate<T> not(PropertyPredicate<T> target) {
    return target.negate();
  }
}
