package net.io_0.property;

import net.io_0.property.models.ColorSubType;
import net.io_0.property.models.Pet;
import net.io_0.property.validation.Validator;

import static net.io_0.property.validation.PropertyConstraint.of;
import static net.io_0.property.validation.PropertyConstraint.stream;
import static net.io_0.property.validation.PropertyValidators.*;

public interface Validators {
  Validator<ColorSubType> colorSubTypeValidator = Validator.of(cst -> stream(
    of(cst.getProperty(ColorSubType.NAME), required, minLength(3)),
    of(cst.getProperty(ColorSubType.ID), required, minimum(17))
  ));

  Validator<Pet> petValidator = Validator.of(pet -> stream(
    of(pet.getProperty(Pet.NAME), required, minLength(4)),
    of(pet.getProperty(Pet.INTEG), required, minimum(18)),
    of(pet.getProperty(Pet.OPTIONAL_PET), notNull),
    of(pet.getProperty(Pet.COLOR_SUB_TYPE), valid(colorSubTypeValidator)),
    of(pet.getProperty(Pet.ZOO), each(valid(Validators.petValidator)))
      // more ..
      // TODO list/set/map of primitive
      // TODO custom validators
      // TODO preload errors
  ));
}
