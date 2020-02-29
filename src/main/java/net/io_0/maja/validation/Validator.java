package net.io_0.maja.validation;

import lombok.Getter;
import net.io_0.maja.PropertyIssues;
import net.io_0.maja.validation.Validation.Invalid;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

@FunctionalInterface
public interface Validator<T> {
  Validation<T> validate(T t);

  static <T> Validator<T> of(PropertyIssues propertyIssues) {
    return model -> Validation.of(model, propertyIssues);
  }

  static <T> Validator<T> of(NameBoundPropertyConstraint<?>... nameBoundPropertyConstraints) {
    return model -> Arrays.stream(nameBoundPropertyConstraints)
      .map(nameBoundPropertyConstraint -> nameBoundPropertyConstraint.apply(model))
      .map(PropertyConstraint::check)
      .filter(Validation::isInvalid)
      .reduce(Validation::and)
      .map(validation -> Validation.of(model, validation.getPropertyIssues()))
      .orElse(Validation.valid(model));
  }

  default T ensureValidity(T t) {
    return ensureValidity(t, ValidationException::new);
  }

  default T ensureValidity(T t, Function<Invalid<T>, ? extends RuntimeException> orThrow) {
    return validate(t).getValue(orThrow);
  }

  @SuppressWarnings("unchecked")
  default <U extends T> Validator<U> and(Validator<? extends U> other) {
    return u -> this.validate(u).and(((Validator<U>) other).validate(u));
  }

  @Getter
  @SuppressWarnings("rawtypes")
  class ValidationException extends RuntimeException {
    private final Invalid validation;

    public ValidationException(Invalid validation) {
      super(toFullMessage(validation.getPropertyIssues().stream()
        .map(propertyIssue -> format("%s -> %s", propertyIssue.getPropertyName(), propertyIssue.getIssue()))
        .collect(Collectors.joining(", "))
      ));

      this.validation = validation;
    }

    private static String toFullMessage(String issues) {
      return issues.isBlank() ? "Validation failed." : "Validation failed. Issues: " + issues;
    }
  }
}
