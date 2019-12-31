package net.io_0.pb.experiments.validation;

import net.io_0.pb.experiments.Property;
import net.io_0.pb.experiments.PropertyIssue;

import static net.io_0.pb.experiments.validation.Validation.invalid;
import static net.io_0.pb.experiments.validation.Validation.valid;

public interface PropertyValidator<T> extends Validator<Property<T>> {
  static <T> PropertyValidator<T> of(PropertyPredicate<T> predicate, String issue) {
    return property -> predicate.test(property) ?
      valid(property) :
      invalid(PropertyIssue.of(property.getName(), String.format(issue, property.isEmpty() ? null : property.getValue())));
  }

  default PropertyValidator<T> and(PropertyValidator<T> other) {
    return t -> this.validate(t).and(other.validate(t));
  }
}