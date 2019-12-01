package net.io_0.property.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.io_0.property.Property;
import java.util.Arrays;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
@ToString
public class PropertyConstraint<T> {
  private final Property<T> property;
  private final PropertyValidator<T> validator;

  public static <T> PropertyConstraint<T> of(Property<T> property, PropertyValidator<T> validator) {
    return new PropertyConstraint<>(property, validator);
  }

  @SafeVarargs
  public static <T> PropertyConstraint<T> of(Property<T> property, PropertyValidator<T>... validators) {
    return new PropertyConstraint<>(property, Arrays.stream(validators).reduce(PropertyValidator::and).get());
  }

  public static Stream<PropertyConstraint<?>> stream(PropertyConstraint<?>... validators) {
    return Arrays.stream(validators);
  }

  public Validation check() {
    return validator.apply(property);
  }
}
