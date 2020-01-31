package net.io_0.maja.validators;

import net.io_0.maja.models.Person;
import net.io_0.maja.validation.PropertyConstraint;
import net.io_0.maja.validation.Validator;

import static net.io_0.maja.models.Person.FIRST_NAME;
import static net.io_0.maja.models.Person.LAST_NAME;
import static net.io_0.maja.validation.PropertyValidators.*;

public interface PersonValidator {
  Validator<Person> instance = Validator.of(
    PropertyConstraint.on(FIRST_NAME, required, notNull, minLength(2)),
    PropertyConstraint.on(LAST_NAME, notNull)
  );
}
