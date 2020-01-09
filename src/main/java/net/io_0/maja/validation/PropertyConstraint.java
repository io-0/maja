package net.io_0.maja.validation;

import lombok.RequiredArgsConstructor;
import net.io_0.maja.Property;
import java.util.Arrays;

@RequiredArgsConstructor
public class PropertyConstraint<T> {
  private final Property<T> property;
  private final PropertyValidator<T> validator;

  public static <T> NameBoundPropertyConstraint<T> on(String propertyName, PropertyValidator<T> validator) {
    return model -> new PropertyConstraint<T>(Property.from(model, propertyName), validator);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <T> NameBoundPropertyConstraint<T> on(String propertyName, PropertyValidator<? extends T>... validators) {
    return model -> new PropertyConstraint<T>(
        Property.from(model, propertyName),
        Arrays.stream(validators).map(v -> (PropertyValidator<T>) v).reduce(PropertyValidator::and).orElseThrow(IllegalArgumentException::new)
      );
  }

  public Validation<Property<T>> check() {
    return validator.validate(property);
  }
}
