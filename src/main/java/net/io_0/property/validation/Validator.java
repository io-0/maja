package net.io_0.property.validation;

import net.io_0.property.SetPropertiesAware;
import java.util.Arrays;
import java.util.function.Function;

public interface Validator<T> extends Function<T, Validation> {
  static <T extends SetPropertiesAware> Validator<T> of(NameBoundPropertyConstraint<?>... nameBoundPropertyConstraints) {
    return model -> Arrays.stream(nameBoundPropertyConstraints)
      .map(nameBoundPropertyConstraint -> nameBoundPropertyConstraint.apply(model))
      .map(PropertyConstraint::check)
      .filter(Validation::isInvalid)
      .reduce(Validation.valid(model), Validation::and);
  }
}
