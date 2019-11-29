package net.io_0.property.validation;

import net.io_0.property.Property;

public interface PropertyValidator<T> extends Validator<Property<T>> {
}

/* TODO
import lombok.RequiredArgsConstructor;
import java.util.function.Predicate;
import static at.upstream_mobility.utils.validation.modular.Validation.invalid;
import static at.upstream_mobility.utils.validation.modular.Validation.valid;

@RequiredArgsConstructor
public class Validator<T> implements Function<T, Validation> {
  private final Predicate<T> condition;
  private final MessageGenerator<T> messageGenerator;

  @Override
  public Validation apply(T t) {
    return condition.test(t) ? valid(t) : invalid(messageGenerator.apply(t));
  }
}
*/
