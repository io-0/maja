package net.io_0.property.validation;

import net.io_0.property.models.ColorSubType;
import net.io_0.property.models.Pet;
import org.junit.jupiter.api.Test;

import static net.io_0.property.validation.PropertyConstraint.of;
import static net.io_0.property.validation.PropertyConstraint.stream;
import static net.io_0.property.validation.PropertyValidators.*;
import static org.junit.jupiter.api.Assertions.*;

public class ValidatorBuildingTest {
  Validator<ColorSubType> colorSubTypeValidator = Validator.of(cst -> stream(
    of(cst.getProperty(ColorSubType.NAME), required, minLength(3)),
    of(cst.getProperty(ColorSubType.ID), required, minimum(17))
  ));

  Validator<Pet> petValidator = Validator.of(pet -> stream(
    of(pet.getProperty(Pet.NAME), required, minLength(4)),
    of(pet.getProperty(Pet.INTEG), required, minimum(18)),
    of(pet.getProperty(Pet.OPTIONAL_PET), notNull),
    of(pet.getProperty(Pet.COLOR_SUB_TYPE), validator(colorSubTypeValidator))
    // more ..
    // TODO list/set/map of obj
  ));


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
    Validation validation = petValidator.apply(new Pet().setColorSubType(new ColorSubType()));
    assertTrue(validation.isInvalid());
    assertEquals(4, ((Validation.Invalid) validation).getReasons().size());
    assertEquals("Reason(subject=name, argument=Is required but missing)", ((Validation.Invalid) validation).getReasons().get(0).toString());
    assertEquals("Reason(subject=integ, argument=Is required but missing)", ((Validation.Invalid) validation).getReasons().get(1).toString());
    assertEquals("Reason(subject=colorSubType.name, argument=Is required but missing)", ((Validation.Invalid) validation).getReasons().get(2).toString());
    assertEquals("Reason(subject=colorSubType.id, argument=Is required but missing)", ((Validation.Invalid) validation).getReasons().get(3).toString());

    validation = petValidator.apply(new Pet().setName("x").setInteg(12).setColorSubType(new ColorSubType().setName("y").setId(1L)));
    assertTrue(validation.isInvalid());
    assertEquals(4, ((Validation.Invalid) validation).getReasons().size());
    assertEquals("Reason(subject=name, argument=Must be longer than 4 characters)", ((Validation.Invalid) validation).getReasons().get(0).toString());
    assertEquals("Reason(subject=integ, argument=Must be 18 or greater)", ((Validation.Invalid) validation).getReasons().get(1).toString());
    assertEquals("Reason(subject=colorSubType.name, argument=Must be longer than 3 characters)", ((Validation.Invalid) validation).getReasons().get(2).toString());
    assertEquals("Reason(subject=colorSubType.id, argument=Must be 17 or greater)", ((Validation.Invalid) validation).getReasons().get(3).toString());
  }

  @Test
  public void conditional_validation_test() {
    Validation validation = petValidator.apply(new Pet().setName("x").setInteg(12).setOptionalPet(null).setColorSubType(null));
    assertTrue(validation.isInvalid());
    assertEquals(3, ((Validation.Invalid) validation).getReasons().size());
    assertEquals("Reason(subject=name, argument=Must be longer than 4 characters)", ((Validation.Invalid) validation).getReasons().get(0).toString());
    assertEquals("Reason(subject=integ, argument=Must be 18 or greater)", ((Validation.Invalid) validation).getReasons().get(1).toString());
    assertEquals("Reason(subject=optionalPet, argument=Can't be literally null)", ((Validation.Invalid) validation).getReasons().get(2).toString());
  }

  @Test
  public void deep_validation_all_good_test() {
    Validation validation = petValidator.apply(new Pet().setName("jenny").setInteg(22).setColorSubType(new ColorSubType().setName("purple").setId(887L)));
    assertTrue(validation.isValid());
  }

  @Test
  public void deep_validation_partial_fails_test() {
    Validation validation = petValidator.apply(new Pet().setName("jenny").setInteg(2).setColorSubType(new ColorSubType().setName("purple").setId(1L)));
    assertTrue(validation.isInvalid());
    assertEquals(2, ((Validation.Invalid) validation).getReasons().size());
    assertEquals("Reason(subject=integ, argument=Must be 18 or greater)", ((Validation.Invalid) validation).getReasons().get(0).toString());
    assertEquals("Reason(subject=colorSubType.id, argument=Must be 17 or greater)", ((Validation.Invalid) validation).getReasons().get(1).toString());
  }
}