package net.io_0.pb.experiments.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.io_0.pb.experiments.Property;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
@ToString
public class PropertyConstraint<T> {
  private final Property<T> property;
  private final PropertyValidator<T> validator;

  public static <T> NameBoundPropertyConstraint<T> on(String propertyName, PropertyValidator<T> validator) {
    return on(propertyName, propertyName, validator);
  }

  public static <T> NameBoundPropertyConstraint<T> on(String propertyName, String propertyLabel, PropertyValidator<T> validator) {
    return model -> new PropertyConstraint<>(model.getProperty(propertyName, propertyLabel), validator);
  }

  @SafeVarargs
  public static <T> NameBoundPropertyConstraint<T> on(String propertyName, PropertyValidator<? extends T>... validators) {
    return on(propertyName, propertyName, validators);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <T> NameBoundPropertyConstraint<T> on(String propertyName, String propertyLabel, PropertyValidator<? extends T>... validators) {
    return model -> new PropertyConstraint<>(
        model.getProperty(propertyName, propertyLabel),
        Arrays.stream(validators).map(v -> (PropertyValidator<T>) v).reduce(PropertyValidator::and).orElseThrow(IllegalArgumentException::new)
      );
  }

  public Validation<Property<T>> check() {
    return validator.validate(property);
  }
}
