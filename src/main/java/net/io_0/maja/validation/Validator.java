package net.io_0.maja.validation;

import lombok.Getter;
import net.io_0.maja.validation.Validation.Invalid;
import net.io_0.maja.validation.Validation.Valid;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

@FunctionalInterface
public interface Validator<T> {
  Validation<T> validate(T t);

  static <T> Validator<T> of(NameBoundPropertyConstraint<?>... nameBoundPropertyConstraints) {
    return model -> Arrays.stream(nameBoundPropertyConstraints)
      .map(nameBoundPropertyConstraint -> nameBoundPropertyConstraint.apply(model))
      .map(PropertyConstraint::check)
      .filter(Validation::isInvalid)
      .reduce(Validation::and)
      .map(validation -> Validation.of(model, validation.getPropertyIssues()))
      .orElse(Validation.valid(model));
  }

  default Valid<T> proceedIfValid(T t) {
    return proceedIfValid(t, ValidationException::new);
  }

  default Valid<T> proceedIfValid(T t, Function<Invalid<T>, ? extends RuntimeException> orThrow) {
    return validate(t).proceedIfValid(orThrow);
  }

  default Validator<T> and(Validator<T> other) {
    return t -> this.validate(t).and(other.validate(t));
  }

  @Getter
  @SuppressWarnings("rawtypes")
  class ValidationException extends RuntimeException {
    private final Invalid validation;

    public ValidationException(Invalid validation) {
      super(toFullMessage(validation.getPropertyIssues().stream()
        .map(propertyIssue -> String.format("%s -> %s", propertyIssue.getPropertyName(), propertyIssue.getIssue()))
        .collect(Collectors.joining(", "))
      ));

      this.validation = validation;
    }

    private static String toFullMessage(String issues) {
      return issues.isBlank() ? "Validation failed." : "Validation failed. Issues: " + issues;
    }
  }
}
