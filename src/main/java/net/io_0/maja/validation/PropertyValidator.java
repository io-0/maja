package net.io_0.maja.validation;

import net.io_0.maja.Property;
import net.io_0.maja.PropertyIssue;
import net.io_0.maja.PropertyIssue.Issue;

import static net.io_0.maja.validation.Validation.invalid;
import static net.io_0.maja.validation.Validation.valid;

public interface PropertyValidator<T> extends Validator<Property<T>> {
  static <T> PropertyValidator<T> of(PropertyPredicate<T> predicate, Issue issue) {
    return property -> predicate.test(property) ?
      valid(property) :
      invalid(PropertyIssue.of(property.getName(), issue.withMessage(
        String.format(issue.getMessage(), property.isEmpty() ? null : property.getValue())
      )));
  }

  default PropertyValidator<T> and(PropertyValidator<T> other) {
    return t -> this.validate(t).and(other.validate(t));
  }
}