package net.io_0.pb;

import net.io_0.pb.models.ColorSubType;
import net.io_0.pb.models.Pet;
import net.io_0.pb.validation.Validator;

import static net.io_0.pb.validation.PropertyConstraint.on;
import static net.io_0.pb.validation.PropertyValidators.*;
import static net.io_0.pb.validation.Validator.of;

public interface Validators {
  Validator<ColorSubType> colorSubTypeValidator = of(
    on(ColorSubType.NAME, required, minLength(3)),
    on(ColorSubType.ID, required, minimum(17))
  );

  Validator<Pet> petValidator = of(
    on(Pet.NAME, required, minLength(4)),
    on(Pet.INTEG, required, minimum(18)),
    on(Pet.OPTIONAL_PET, notNull),
    on(Pet.COLOR_SUB_TYPE, valid(colorSubTypeValidator)),
    on(Pet.ZOO, each(valid(lazy(() -> Validators.petValidator)))),
    on(Pet.COLOR_LIST, each(minLength(4))),
    on(Pet.COLOR_SET, each(maxLength(6))),
    on(Pet.LONG_MAP, maxItems(2), each(multipleOf(3))),
    on(Pet.PET_MAP, minItems(1), each(valid(lazy(() -> Validators.petValidator))))
  );
}
