package net.io_0.property.validation;

import net.io_0.property.Property;
import net.io_0.property.models.Pet;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static net.io_0.property.validation.PropertyConstraint.of;
import static net.io_0.property.validation.Predicates.*;
import static org.junit.jupiter.api.Assertions.*;

public class ValidatorBuilderTest {

  /* validation concept start */


  // TODO make builder/helpers out of following

  Validator<Pet> petValidator = (pet) -> Stream.of(
    of(pet.<String> getProperty(Pet.NAME), Validators.required),
    of(pet.getProperty(Pet.NAME), Validators.minLength(4)),
    of(pet.<Integer> getProperty(Pet.INTEG), Validators.required),
    of(pet.<Integer> getProperty(Pet.INTEG), Validators.minimum(18)),
    of(pet.<Pet> getProperty(Pet.OPTIONAL_PET), Validators.notNull)  // TODO inverse... explicit nullable
    // more ..
  )
    .map(PropertyConstraint::check)
    .filter(Validation::isInvalid)
    .reduce(Validation.valid(pet), Validation::and);

  /* validation concept end */

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
  public void deep_validation_test() {
    // TODO errors field path construction where -> adapter smtg to property validator
  }

  @Test
  public void present_test() {
    Property<String> p = new Property<>("x", null, false);

    assertTrue(empty.test(p));
    assertFalse(assigned.test(p));
    assertFalse(present.test(p));
  }

    /*

https://www.vavr.io/vavr-docs/#_validation
https://github.com/vavr-io/vavr/blob/master/src/main/java/io/vavr/control/Validation.java
https://github.com/making/yavi
https://github.com/making/yavi/blob/develop/src/main/java/am/ik/yavi/core/Validator.java
https://www.konform.io/


    add validateSmtgOnCondition(condition, ...)


  SchemaWithValidators<Person> schema =
    ValidatorSchema
      .<Person> builder()
      .validateRequired(name)
      .validateRequired(age)
      .validateRequired(address)
      .validateMinimum(age, 0)
      .validateSchema(address, Address.schema)
      .get();


---------------------------------------------------------------------------------------

- takes deserialization errors
- can add custom validation - full obj?




.. haufen ifPresentValidators
      incl notnull                  <------ default kinda
.. requiredValidator
.. schemaValidator?



["fieldname", "[BinaryFormat] Must be a sequence of octets"]



Validator
  Predicate (+ parameter)  +  error msg

  .validate(prop[fieldname+valueGetter], errorsCollector)




--------------------------------------------------------------------------------------------


import static PropertyValidators.*


PersonValidator extends Validator {

  Validation<Person> validate(Person p) {
    return Stream.of(
        ( p -> unassigned(p).or(greaterThen(4, p)) ) // do this as prop helper
            .apply(new Property("age", Person::getAge, Person.isPropertySet(Person.AGE))),
        patternMatch("123", new Property(..)),
        ..wrap pojo validator within propertyValidator??  e.g. unassigned OR validatAddress
    )
    .map(Validator::validate)
    .filter(Validation::isInvalid)
    .collect(
      init -> Valid(Person)
      aggregate -> .. make to invalid and collect error
    )

  }

}


@RequiredArgsConstructor
@Component
Controller {
  private final Optional<CustomValidator<Person>> customValidator;
  private final Validator<Person> validator;

  smtg() {
    <call with> deserValidator(errors).and(validator.and(customValidator)).assertValid(p)
  }
}




-----------------------------------------------


V2:

  Validation

    getReasons
    orElseThrow

    Validation.Valid<>
    Validation.Invalid<>

    and
    or ?



  Validator

    Validation<S> validate(S subject)
    Validation.Valid<S> assertValid(S subject)
    Validation.Valid<S> assertValid(S subject, Supplier<? extends Throwable> orElseTrow)

    Validator and(Validator v)    jeweils nur selbes subject
    Validator or(Validator v)




----------------------------------------------


V1:

  Validation

    ValidationResult
      Valid<Model>
      Invalid<Model>
        List<String> errors

    ValidationException
      RequestValidationException
      ResponseValidationException


  Validator

    ValidationResult<M> validate(M model)
    Valid<M> assertValid(M model)
    Valid<M> assertValid(M model, Supplier<? extends ValidationException> orTrow)

    Validator and(Validator v)      jeweils nur selbes feld
    Validator or(Validator v)



     */

  }