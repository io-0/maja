package net.io_0.property.validation;

import net.io_0.property.models.ColorSubType;
import net.io_0.property.models.Pet;
import net.io_0.property.validation.Validation.Valid;
import org.junit.jupiter.api.Test;
import java.util.List;

import static net.io_0.property.Validators.petValidator;
import static org.junit.jupiter.api.Assertions.*;

public class ValidatorBuildingTest {
  @Test
  public void all_good_test() {
    Valid<Pet> validation = petValidator.proceedIfValid(new Pet().setName("jenny").setInteg(22));
    assertTrue(validation.isValid());
    assertTrue(validation.getPropertyIssues().isEmpty());
  }

  @Test
  public void partial_fails_test() {
    Validation<Pet> validation = petValidator.validate(new Pet().setName("jenny").setInteg(12));
    assertTrue(validation.isInvalid());
    assertEquals("PropertyIssue(propertyName=integ, issue=Must be 18 or greater)", validation.getPropertyIssues().get(0).toString());

    validation = petValidator.validate(new Pet().setName("x").setInteg(22));
    assertTrue(validation.isInvalid());
    assertEquals(1, validation.getPropertyIssues().size());
    assertEquals("PropertyIssue(propertyName=name, issue=Must be longer than 4 characters)", validation.getPropertyIssues().get(0).toString());
  }

  @Test
  public void all_fails_test() {
    Validation<Pet> validation = petValidator.validate(new Pet().setColorSubType(new ColorSubType()));
    assertTrue(validation.isInvalid());
    assertEquals(4, validation.getPropertyIssues().size());
    assertEquals("PropertyIssue(propertyName=name, issue=Is required but missing)", validation.getPropertyIssues().get(0).toString());
    assertEquals("PropertyIssue(propertyName=integ, issue=Is required but missing)", validation.getPropertyIssues().get(1).toString());
    assertEquals("PropertyIssue(propertyName=colorSubType.name, issue=Is required but missing)", validation.getPropertyIssues().get(2).toString());
    assertEquals("PropertyIssue(propertyName=colorSubType.id, issue=Is required but missing)", validation.getPropertyIssues().get(3).toString());

    validation = petValidator.validate(new Pet().setName("x").setInteg(12).setColorSubType(new ColorSubType().setName("y").setId(1L)));
    assertTrue(validation.isInvalid());
    assertEquals(4, validation.getPropertyIssues().size());
    assertEquals("PropertyIssue(propertyName=name, issue=Must be longer than 4 characters)", validation.getPropertyIssues().get(0).toString());
    assertEquals("PropertyIssue(propertyName=integ, issue=Must be 18 or greater)", validation.getPropertyIssues().get(1).toString());
    assertEquals("PropertyIssue(propertyName=colorSubType.name, issue=Must be longer than 3 characters)", validation.getPropertyIssues().get(2).toString());
    assertEquals("PropertyIssue(propertyName=colorSubType.id, issue=Must be 17 or greater)", validation.getPropertyIssues().get(3).toString());
  }

  @Test
  public void conditional_validation_test() {
    Validation<Pet> validation = petValidator.validate(new Pet().setName("x").setInteg(12).setOptionalPet(null).setColorSubType(null));
    assertTrue(validation.isInvalid());
    assertEquals(3, validation.getPropertyIssues().size());
    assertEquals("PropertyIssue(propertyName=name, issue=Must be longer than 4 characters)", validation.getPropertyIssues().get(0).toString());
    assertEquals("PropertyIssue(propertyName=integ, issue=Must be 18 or greater)", validation.getPropertyIssues().get(1).toString());
    assertEquals("PropertyIssue(propertyName=optionalPet, issue=Can't be literally null)", validation.getPropertyIssues().get(2).toString());
  }

  @Test
  public void deep_validation_all_good_test() {
    Valid<Pet> validation = petValidator.proceedIfValid(new Pet()
      .setName("jenny").setInteg(22)
      .setColorSubType(new ColorSubType().setName("purple").setId(887L))
      .setZoo(List.of(
        new Pet().setName("hugo").setInteg(61),
        new Pet().setName("luke").setInteg(59)
      ))
    );
    assertTrue(validation.isValid());
    assertTrue(validation.getPropertyIssues().isEmpty());
  }

  @Test
  public void deep_validation_partial_fails_test() {
    Validation<Pet> validation = petValidator.validate(new Pet()
      .setName("jenny")
      .setInteg(2)
      .setColorSubType(new ColorSubType().setName("purple").setId(1L))
      .setZoo(List.of(
        new Pet().setName("h").setInteg(61),
        new Pet().setName("luke").setInteg(14)
      ))
    );
    assertTrue(validation.isInvalid());
    assertEquals(4, validation.getPropertyIssues().size());
    assertEquals("PropertyIssue(propertyName=integ, issue=Must be 18 or greater)", validation.getPropertyIssues().get(0).toString());
    assertEquals("PropertyIssue(propertyName=colorSubType.id, issue=Must be 17 or greater)", validation.getPropertyIssues().get(1).toString());
    assertEquals("PropertyIssue(propertyName=zoo.0.name, issue=Must be longer than 4 characters)", validation.getPropertyIssues().get(2).toString());
    assertEquals("PropertyIssue(propertyName=zoo.1.integ, issue=Must be 18 or greater)", validation.getPropertyIssues().get(3).toString());
  }
}