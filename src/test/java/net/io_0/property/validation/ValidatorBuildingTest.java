package net.io_0.property.validation;

import net.io_0.property.models.Pet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.util.stream.Stream;

import static net.io_0.property.validation.PropertyConstraint.of;
import static net.io_0.property.validation.PropertyValidators.*;
import static org.junit.jupiter.api.Assertions.*;

public class ValidatorBuildingTest {
  Validator<Pet> petValidator = (pet) -> Stream.of(
    of(pet.getProperty(Pet.NAME), required),
    of(pet.getProperty(Pet.NAME), minLength(4)),
    of(pet.getProperty(Pet.INTEG), required),
    of(pet.getProperty(Pet.INTEG), minimum(18)),
    of(pet.getProperty(Pet.OPTIONAL_PET), notNull)  // TODO inverse... explicit nullable
    // more ..
  )
    .map(PropertyConstraint::check)
    .filter(Validation::isInvalid)
    .reduce(Validation.valid(pet), Validation::and);

  @Test
  public void all_good_test() {
    Validation validation = petValidator.apply(new Pet().setName("jenny").setInteg(22));
    assertTrue(validation.isValid());
  }

  @Test
  public void partial_fails_test() {
    Validation validation = petValidator.apply(new Pet().setName("jenny").setInteg(12));
    assertTrue(validation.isInvalid());
    assertEquals("Reason(subject=integ, argument=Must be 18 or greater)", ((Validation.Invalid) validation).getReasons().get(0).toString());

    validation = petValidator.apply(new Pet().setName("x").setInteg(22));
    assertTrue(validation.isInvalid());
    assertEquals(1, ((Validation.Invalid) validation).getReasons().size());
    assertEquals("Reason(subject=name, argument=Must be longer than 4 characters)", ((Validation.Invalid) validation).getReasons().get(0).toString());
  }

  @Test
  public void all_fails_test() {
    Validation validation = petValidator.apply(new Pet());
    assertTrue(validation.isInvalid());
    assertEquals(2, ((Validation.Invalid) validation).getReasons().size());
    assertEquals("Reason(subject=name, argument=Is required but missing)", ((Validation.Invalid) validation).getReasons().get(0).toString());
    assertEquals("Reason(subject=integ, argument=Is required but missing)", ((Validation.Invalid) validation).getReasons().get(1).toString());

    validation = petValidator.apply(new Pet().setName("x").setInteg(12));
    assertTrue(validation.isInvalid());
    assertEquals(2, ((Validation.Invalid) validation).getReasons().size());
    assertEquals("Reason(subject=name, argument=Must be longer than 4 characters)", ((Validation.Invalid) validation).getReasons().get(0).toString());
    assertEquals("Reason(subject=integ, argument=Must be 18 or greater)", ((Validation.Invalid) validation).getReasons().get(1).toString());
  }

  @Test
  public void conditional_validation_test() {
    Validation validation = petValidator.apply(new Pet().setName("x").setInteg(12).setOptionalPet(null));
    assertTrue(validation.isInvalid());
    assertEquals(3, ((Validation.Invalid) validation).getReasons().size());
    assertEquals("Reason(subject=name, argument=Must be longer than 4 characters)", ((Validation.Invalid) validation).getReasons().get(0).toString());
    assertEquals("Reason(subject=integ, argument=Must be 18 or greater)", ((Validation.Invalid) validation).getReasons().get(1).toString());
    assertEquals("Reason(subject=optionalPet, argument=Can't be literally null)", ((Validation.Invalid) validation).getReasons().get(2).toString());
  }

  @Test
  @Disabled("Not implemented yet")
  public void deep_validation_test() {
    // TODO bridge property and validator
  }
}