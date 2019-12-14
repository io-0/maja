package net.io_0.pb.validation;

import net.io_0.pb.PropertyIssue;
import net.io_0.pb.PropertyIssues;
import net.io_0.pb.SetPropertiesAware;
import net.io_0.pb.validation.Validation.Invalid;
import net.io_0.pb.validation.Validation.Valid;
import java.util.Arrays;
import java.util.function.Function;

public interface Validator<T> {
  Validation<T> validate(T t);

  default Valid<T> proceedIfValid(T t) {
    return proceedIfValid(t, ValidationException::new);
  }

  default Valid<T> proceedIfValid(T t, Function<Invalid<T>, ? extends RuntimeException> orThrow) {
    return validate(t).proceedIfValid(orThrow);
  }

  default Validator<T> and(Validator<T> other) {
    return t -> this.validate(t).and(other.validate(t));
  }

  static <T> Validator<T> of(PropertyIssue... propertyIssues) {
    return of(PropertyIssues.of(propertyIssues));
  }

  static <T> Validator<T> of(PropertyIssues propertyIssues) {
    return model -> Validation.of(model, propertyIssues);
  }

  static <T extends SetPropertiesAware> Validator<T> of(NameBoundPropertyConstraint<?>... nameBoundPropertyConstraints) {
    return model -> Arrays.stream(nameBoundPropertyConstraints)
      .map(nameBoundPropertyConstraint -> nameBoundPropertyConstraint.apply(model))
      .map(PropertyConstraint::check)
      .filter(Validation::isInvalid)
      .reduce(Validation::and)
      .map(validation -> Validation.of(model, validation.getPropertyIssues()))
      .orElse(Validation.valid(model));
  }
}
