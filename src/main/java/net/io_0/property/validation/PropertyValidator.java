package net.io_0.property.validation;

import net.io_0.property.Property;
import net.io_0.property.PropertyIssue;
import net.io_0.property.PropertyIssues;

import static net.io_0.property.validation.Validation.invalid;
import static net.io_0.property.validation.Validation.valid;

public interface PropertyValidator<T> extends Validator<Property<T>> {
  static <T> PropertyValidator<T> of(PropertyPredicate<T> predicate, String errorMessage) {
    return property -> predicate.test(property) ?
      valid(property) :
      invalid(PropertyIssues.of(PropertyIssue.of(property.getName(), String.format(errorMessage, property.isEmpty() ? null : property.getValue()))));
  }

  default PropertyValidator<T> and(PropertyValidator<T> other) {
    return t -> this.apply(t).and(other.apply(t));
  }
}