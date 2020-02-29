package net.io_0.maja.validation;

import lombok.RequiredArgsConstructor;
import net.io_0.maja.Property;

import static net.io_0.maja.validation.PropertyValidator.andAll;

@RequiredArgsConstructor
public class PropertyConstraint<T> {
  private final Property<T> property;
  private final PropertyValidator<T> validator;

  public static <T> NameBoundPropertyConstraint<T> on(String propertyName, PropertyValidator<T> validator) {
    return model -> new PropertyConstraint<T>(Property.from(model, propertyName), validator);
  }

  @SafeVarargs
  public static <T> NameBoundPropertyConstraint<T> on(String propertyName, PropertyValidator<? extends T>... validators) {
    return model -> new PropertyConstraint<T>(Property.from(model, propertyName), andAll(validators));
  }

  public Validation<Property<T>> check() {
    return validator.validate(property);
  }
}
