package net.io_0.property.validation;

import net.io_0.property.SetPropertiesAware;
import net.io_0.property.validation.Validation.Invalid;
import net.io_0.property.validation.Validation.Valid;
import java.util.Arrays;
import java.util.function.Function;

public interface Validator<T> {
  Validation validate(T t);

  default Valid<T> proceedIfValid(T t) {
    return proceedIfValid(t, ValidationException::new);
  }

  @SuppressWarnings("unchecked")
  default Valid<T> proceedIfValid(T t, Function<Invalid, ? extends RuntimeException> orThrow) {
    Validation validation = validate(t);

    if (validation.isValid()) {
      return (Valid<T>) validation;
    }

    throw orThrow.apply((Invalid) validation);
  }

  static <T extends SetPropertiesAware> Validator<T> of(NameBoundPropertyConstraint<?>... nameBoundPropertyConstraints) {
    return model -> Arrays.stream(nameBoundPropertyConstraints)
      .map(nameBoundPropertyConstraint -> nameBoundPropertyConstraint.apply(model))
      .map(PropertyConstraint::check)
      .filter(Validation::isInvalid)
      .reduce(Validation.valid(model), Validation::and);
  }
}
