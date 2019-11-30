package net.io_0.property.validation;

import net.io_0.property.SetPropertiesAware;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Validator<T> extends Function<T, Validation> {
  static <T extends SetPropertiesAware> Validator<T> of(Function<T, Stream<PropertyConstraint<?>>> getConstraints) {
    return (T model) -> getConstraints.apply(model)
      .map(PropertyConstraint::check)
      .filter(Validation::isInvalid)
      .reduce(Validation.valid(model), Validation::and);
  }
}
