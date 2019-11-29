package net.io_0.property.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.io_0.property.Property;

@RequiredArgsConstructor
@Getter
@ToString
public class PropertyConstraint<T> {
  private final Property<? extends T> property;
  private final PropertyValidator<T> validator;

  public static <T> PropertyConstraint<T> of(Property<? extends T> property, PropertyValidator<T> validator) {
    return new PropertyConstraint<>(property, validator);
  }

  public Validation check() {
    return validator.apply((Property<T>) property);
  }
}
