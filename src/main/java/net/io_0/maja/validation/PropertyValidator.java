package net.io_0.maja.validation;

import net.io_0.maja.Property;
import net.io_0.maja.PropertyIssue;
import net.io_0.maja.PropertyIssue.Issue;
import java.util.Arrays;

import static java.lang.String.*;
import static net.io_0.maja.validation.Validation.invalid;
import static net.io_0.maja.validation.Validation.valid;

public interface PropertyValidator<T> extends Validator<Property<T>> {
  static <T> PropertyValidator<T> of(PropertyPredicate<T> predicate, Issue issue) {
    return property -> predicate.test(property) ?
      valid(property) :
      invalid(PropertyIssue.of(
        property.getName(),
        issue.withMessage(format(issue.getMessage(), property.isNull() ? null : property.getValue()))
      ));
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  static <T> PropertyValidator<T> andAll(PropertyValidator<? extends T>... validators) {
    return Arrays.stream(validators)
      .map(v -> (PropertyValidator<T>) v)
      .reduce((a, b) -> t -> a.validate(t).and(b.validate(t)))
      .orElseThrow(IllegalArgumentException::new);
  }
}