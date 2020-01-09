package net.io_0.maja.validation;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.Property;
import net.io_0.maja.PropertyIssue;
import net.io_0.maja.PropertyIssues;
import net.io_0.maja.models.*;
import net.io_0.maja.validation.Validation.Valid;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;

import static net.io_0.maja.models.Nested.*;
import static net.io_0.maja.models.StringBundle.*;
import static net.io_0.maja.models.Validatable.*;
import static net.io_0.maja.validation.PropertyConstraint.on;
import static net.io_0.maja.validation.PropertyValidators.*;
import static net.io_0.maja.validation.Validation.invalid;
import static net.io_0.maja.validation.Validation.valid;
import static net.io_0.maja.validation.Validator.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Narrative:
 *   As a validation API consumer
 *
 *   I want an easy and compact way to build a POJO validator based on business logic or property constraints
 *   so that I can use it and further data processing can consume validated data
 *
 *   and I want to be able to express that data is valid after validation and convenience
 *   so that code that uses it is easy to read and write
 *
 *   and I want a property constraints based validator to be able to distinguish between absent and null values
 *   so that I can tread null as e.g. delete
 *
 *   and I want a single report containing all that went wrong during validation
 *   so that providing valid data gets easier
 *
 *   and I want to be able to deal with names
 *   so that I don't get problems with Java naming conventions
 */
@Slf4j
public class ValidatePojoTests {
  /**
   * Scenario: It should be easy to build a POJO validator based on business logic and run it
   */
  @Test
  public void buildBusinessLogicValidatorAndRunIt() {
    // Given a business rule and error message
    Predicate<Flat> intAndLongNumberMustBePresentAndMatch = item ->
      item.getNumberToInteger() != null && item.getNumberToLong() != null &&
      item.getNumberToInteger().equals(item.getNumberToLong().intValue());
    String errorMessage = "numberToInteger value must match numberToLong";

    // When a validator is built compact and with ease
    Validator<Flat> intAndLongNumberValidator = item ->
      intAndLongNumberMustBePresentAndMatch.test(item) ? valid(item) : invalid(PropertyIssue.of("numberToInteger, numberToLong", errorMessage));

    // Then it should validate correctly
    Validation<Flat> invalidBecauseMissing = intAndLongNumberValidator.validate(Flat.builder().build());
    assertTrue(invalidBecauseMissing.isInvalid());
    assertTrue(invalidBecauseMissing.getPropertyIssues().containsPropertyName("numberToInteger, numberToLong"));
    assertEquals(
      Optional.of("numberToInteger value must match numberToLong"),
      invalidBecauseMissing.getPropertyIssues().getPropertyIssue("numberToInteger, numberToLong")
    );

    Validation<Flat> invalidBecauseMismatch = intAndLongNumberValidator.validate(Flat.builder().numberToInteger(2).numberToLong(4L).build());
    assertTrue(invalidBecauseMismatch.isInvalid());
    assertTrue(invalidBecauseMismatch.getPropertyIssues().containsPropertyName("numberToInteger, numberToLong"));
    assertEquals(
      Optional.of("numberToInteger value must match numberToLong"),
      invalidBecauseMismatch.getPropertyIssues().getPropertyIssue("numberToInteger, numberToLong")
    );

    Validation<Flat> valid = intAndLongNumberValidator.validate(Flat.builder().numberToInteger(2).numberToLong(2L).build());
    assertTrue(valid.isValid());
  }

  /**
   * Scenario: It should be easy to build a POJO validator based on property constraints and run it
   */
  @Test
  public void buildPropertyConstraintsValidatorAndRunIt() {
    // Given property constraints
    //   flat.stringToString must be present and at least 4 chars long
    //   flat.stringToInteger can be null or must be greater than 18
    //   flat.stringArrayToStringList must be present and has at least 2 items and each is at least 4 chars long

    // When a validator is built compact and with ease
    Validator<Flat> flatValidator = of(
      on("stringToString", notNull, minLength(4)),
      on("stringToInteger", minimum(18)),
      on("stringArrayToStringList", notNull, minItems(2), each(minLength(4)))
    );

    // Then it should validate correctly
    Validation<Flat> invalidBecauseEmpty = flatValidator.validate(Flat.builder().build());
    assertTrue(invalidBecauseEmpty.isInvalid());
    assertTrue(invalidBecauseEmpty.getPropertyIssues().containsPropertyName("stringToString"));
    assertTrue(invalidBecauseEmpty.getPropertyIssues().containsPropertyName("stringArrayToStringList"));
    assertEquals(Optional.of("Can't be literally null"), invalidBecauseEmpty.getPropertyIssues().getPropertyIssue("stringToString"));
    assertEquals(Optional.of("Can't be literally null"), invalidBecauseEmpty.getPropertyIssues().getPropertyIssue("stringArrayToStringList"));

    Validation<Flat> invalid = flatValidator.validate(
      Flat.builder().stringToString("two").stringToInteger(7).stringArrayToStringList(List.of("one")).build()
    );
    assertTrue(invalid.isInvalid());
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringToString"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringToInteger"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringArrayToStringList"));
    assertEquals(Optional.of("Must be longer than 4 characters"), invalid.getPropertyIssues().getPropertyIssue("stringToString"));
    assertEquals(Optional.of("Must be 18 or greater"), invalid.getPropertyIssues().getPropertyIssue("stringToInteger"));
    assertEquals(Optional.of("Must contain 2 items or more"), invalid.getPropertyIssues().getPropertyIssue("stringArrayToStringList"));

    Validation<Flat> valid = flatValidator.validate(
      Flat.builder().stringToString("four").stringToInteger(21).stringArrayToStringList(List.of("five", "seven")).build());
    assertTrue(valid.isValid());
  }

  /**
   * Scenario: It should be possible to express validity
   */
  @Test
  public void expressValid() {
    // Given a validator
    Validator<Nested> validator = of(on(BOOLEAN_TO_BOOLEAN, required));

    // When validation was done
    Validation<Nested> valid = validator.validate(new Nested().setBooleanToBoolean(true));

    // Then it should be possible to express certain validity (or exception)
    Valid<Nested> validForSure = valid.proceedIfValid();
    assertTrue(validForSure.isValid());

    // And same should be doable when validating
    Valid<Nested> soValid = validator.proceedIfValid(new Nested().setBooleanToBoolean(true));
    assertTrue(soValid.isValid());
  }

  /**
   * Scenario: It should be convenient to work with validators and their components
   */
  @Test
  public void testConvenienceAndUsabilityFurther() {
    Validator<Nested> validator = of(on(BOOLEAN_TO_BOOLEAN, required));

    // Process validation exception on validator when we went the "express validity" route
    // (if no exception is desired, use .validate instead of .proceedIfValid)
    Validator.ValidationException ex = assertThrows(Validator.ValidationException.class, () -> validator.proceedIfValid(new Nested()));
    assertTrue(ex.getValidation().getPropertyIssues().containsPropertyName(BOOLEAN_TO_BOOLEAN));

    // Same with custom exception
    class MyEx extends IllegalStateException { public MyEx(String s) { super(s); } }
    MyEx ex1 = assertThrows(MyEx.class, () -> validator.proceedIfValid(new Nested(), invalid -> new MyEx(invalid.getPropertyIssues().toString())));
    assertTrue(ex1.getMessage().contains(BOOLEAN_TO_BOOLEAN) && ex1.getMessage().contains("required"));

    // Process validation exception on validation
    Validation<Nested> inValid = validator.validate(new Nested());
    Validator.ValidationException ex2 = assertThrows(Validator.ValidationException.class, inValid::proceedIfValid);
    assertTrue(ex2.getValidation().getPropertyIssues().containsPropertyName(BOOLEAN_TO_BOOLEAN));

    // Validators can be combined and created from issues e.g. from mapping
    Validator<Object> combinedValidator = of(PropertyIssues.of())
      .and(of(PropertyIssue.of("property1", "issue1")))
      .and(of(PropertyIssues.of(PropertyIssue.of("property2", "issue2"), PropertyIssue.of("property3", "issue3"))));
    Validation<Object> combinedValidation = combinedValidator.validate(null);
    assertTrue(combinedValidation.isInvalid());
    assertEquals(3, combinedValidation.getPropertyIssues().size());
    assertEquals(Optional.of("issue1"), combinedValidation.getPropertyIssues().getPropertyIssue("property1"));
    assertEquals(Optional.of("issue2"), combinedValidation.getPropertyIssues().getPropertyIssue("property2"));
    assertEquals(Optional.of("issue3"), combinedValidation.getPropertyIssues().getPropertyIssue("property3"));

    // Code can be generalized, even Validation.valid getPropertyIssues exists but is always empty
    assertTrue(Validation.valid(null).getPropertyIssues().isEmpty());

    // It is easy to extract the valid object of a valid validation
    Nested payload = new Nested();
    assertEquals(payload, Validation.valid(payload).getValue());

    // It is possible to unmark marked properties, e.g. if utils set something
    Nested markedProperties = new Nested();
    markedProperties.setBooleanToBoolean(null);
    assertTrue(markedProperties.isPropertySet(BOOLEAN_TO_BOOLEAN));
    markedProperties.unmarkPropertySet(BOOLEAN_TO_BOOLEAN);
    assertFalse(markedProperties.isPropertySet(BOOLEAN_TO_BOOLEAN));

    // If one tries to access a non existing property a explanatory exception is thrown
    IllegalArgumentException iAEx = assertThrows(IllegalArgumentException.class, () -> Property.from(new Nested(), "property1"));
    assertEquals("Property with name 'property1' not found on Nested", iAEx.getMessage());

    // Same goes for other problems
    iAEx = assertThrows(IllegalArgumentException.class, () -> Property.<String> from(new Nested() {
      @Override public Boolean getBooleanToBoolean() { throw new IllegalStateException(); }
    }, BOOLEAN_TO_BOOLEAN));
    assertTrue(iAEx.getMessage().startsWith("Couldn't access property with name 'booleanToBoolean' on"));

    // PropertyIssue is easy to work with
    PropertyIssue pI = new PropertyIssue("name", "issue");
    assertEquals("name", pI.getPropertyName());
    assertEquals("issue", pI.getIssue());
    assertEquals("PropertyIssue(propertyName=name, issue=issue)", pI.toString());
  }

  /**
   * Scenario: A validator (property constraints based) should be able to distinguish between absent and null values
   *           given a POJO that implements SetPropertiesAware correctly
   */
  @Test
  public void validateAbsentAndNull() {
    // Given a POJO (that implements SetPropertiesAware correctly) with absent and null values
    Nested pojo = new Nested().setStringToUUID(null).setBooleanToBoolean(false);

    // When a validator is built
    Validator<Nested> nestedValidator = of(
      on(STRING_TO_UUID, required, notNull),
      on(NUMBER_TO_BIG_DECIMAL, required, notNull, minimum(18)),
      on(BOOLEAN_TO_BOOLEAN, required, notNull)
    );

    // Then it should validate correctly
    Validation<Nested> invalid = nestedValidator.validate(pojo);
    assertTrue(invalid.isInvalid());
    assertTrue(invalid.getPropertyIssues().containsPropertyName(STRING_TO_UUID));
    assertTrue(invalid.getPropertyIssues().containsPropertyName(NUMBER_TO_BIG_DECIMAL));
    assertFalse(invalid.getPropertyIssues().containsPropertyName(BOOLEAN_TO_BOOLEAN));
    assertEquals(Optional.of("Can't be literally null"), invalid.getPropertyIssues().getPropertyIssue(STRING_TO_UUID));
    assertEquals(Optional.of("Is required but missing"), invalid.getPropertyIssues().getPropertyIssue(NUMBER_TO_BIG_DECIMAL));
  }

  /**
   * Scenario: It should be possible to validate deep and nested POJOs
   */
  @Test
  public void validateDeepAndNested() {
    // Given a POJO (that implements SetPropertiesAware correctly) witch is deep and nested
    Validatable pojo = deepAndNested;

    // When a validator is built
    Validator<Validatable> validator = deepAndNestedValidator;

    // Then it should validate correctly
    Validation<Validatable> valid = validator.validate(pojo);
    assertTrue(valid.isValid());
  }

  /**
   * Scenario: A single report should contain all that went wrong during validation
   */
  @Test
  public void rAV() {
    // Given a POJO (that implements SetPropertiesAware correctly) witch is flawed
    Validatable pojo = deepAndNestedAndFlawed;

    // When a validator is built
    Validator<Validatable> validator = deepAndNestedValidator;

    // Then it should validate correctly and contain all errors in one report (we count propertyIssues as "report")
    Validation<Validatable> inValid = validator.validate(pojo);
    assertTrue(inValid.isInvalid());
    assertEquals(Optional.of("Can't be literally null"), inValid.getPropertyIssues().getPropertyIssue("notNull"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("required"));
    assertEquals(Optional.of("Must match '^\\d+$' pattern"), inValid.getPropertyIssues().getPropertyIssue("patternStrings.one"));
    assertEquals(Optional.of("Must at least contain 1 number, 1 lower case letter, 1 upper case letter and 1 special character"), inValid.getPropertyIssues().getPropertyIssue("patternStrings.two"));
    assertEquals(Optional.of("Must be a sequence of octets"), inValid.getPropertyIssues().getPropertyIssue("patternStrings.three"));
    assertEquals(Optional.of("Must be base64 format"), inValid.getPropertyIssues().getPropertyIssue("patternStrings.four"));
    assertEquals(Optional.of("Must fit email format"), inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.one"));
    assertEquals(Optional.of("Must fit hostname format"), inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.two"));
    assertEquals(Optional.of("Must fit IP v4 format"), inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.three"));
    assertEquals(Optional.of("Must fit IP v6 format"), inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.four"));
    assertEquals(Optional.of("Must be shorter than 4 characters"), inValid.getPropertyIssues().getPropertyIssue("lengthRestrictedStrings.one"));
    assertEquals(Optional.of("Must be longer than 4 characters"), inValid.getPropertyIssues().getPropertyIssue("lengthRestrictedStrings.two"));
    assertEquals(Optional.of("Must be lessen than 5"), inValid.getPropertyIssues().getPropertyIssue("numbers.one"));
    assertEquals(Optional.of("Must be greater than 3"), inValid.getPropertyIssues().getPropertyIssue("numbers.two"));
    assertEquals(Optional.of("Must be 4 or lesser"), inValid.getPropertyIssues().getPropertyIssue("numbers.three"));
    assertEquals(Optional.of("Must be 4 or greater"), inValid.getPropertyIssues().getPropertyIssue("numbers.four"));
    assertEquals(Optional.of("Must be lessen than 4.5"), inValid.getPropertyIssues().getPropertyIssue("moreNumbers.one"));
    assertEquals(Optional.of("Must be greater than 4.3"), inValid.getPropertyIssues().getPropertyIssue("moreNumbers.two"));
    assertEquals(Optional.of("Must be 4.4 or greater"), inValid.getPropertyIssues().getPropertyIssue("moreNumbers.four"));
    assertEquals(Optional.of("Must be 4.5 or lesser"), inValid.getPropertyIssues().getPropertyIssue("evenMoreNumbers.one"));
    assertEquals(Optional.of("Must be 4.3 or greater"), inValid.getPropertyIssues().getPropertyIssue("evenMoreNumbers.two"));
    assertEquals(Optional.of("Must be a multiple of 2.2"), inValid.getPropertyIssues().getPropertyIssue("evenMoreNumbers.three"));
    assertEquals(Optional.of("Must be a multiple of 1.1"), inValid.getPropertyIssues().getPropertyIssue("evenMoreNumbers.four"));
    assertEquals(Optional.of("Must contain 4 items or less"), inValid.getPropertyIssues().getPropertyIssue("numberList"));
    assertEquals(Optional.of("Must contain 4 items or more"), inValid.getPropertyIssues().getPropertyIssue("numberSet"));
    assertEquals(Optional.of("Can't be literally null"), inValid.getPropertyIssues().getPropertyIssue("samePojo.notNull"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("samePojo.required"));
    assertEquals(Optional.of("Must contain 5 items or less"), inValid.getPropertyIssues().getPropertyIssue("pojos"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.0.stringToUUID"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.0.booleanToBoolean"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.1.stringToUUID"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.1.booleanToBoolean"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.2.stringToUUID"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.2.booleanToBoolean"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.3.stringToUUID"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.3.booleanToBoolean"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.4.stringToUUID"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.4.booleanToBoolean"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.5.stringToUUID"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.5.booleanToBoolean"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.6.stringToUUID"));
    assertEquals(Optional.of("Is required but missing"), inValid.getPropertyIssues().getPropertyIssue("pojos.6.booleanToBoolean"));
    assertEquals(Optional.of("Must contain 3 items or more"), inValid.getPropertyIssues().getPropertyIssue("pojoMap"));
    assertEquals(Optional.of("Can't be literally null"), inValid.getPropertyIssues().getPropertyIssue("pojoMap.one.stringToUUID"));
    assertEquals(Optional.of("Can't be literally null"), inValid.getPropertyIssues().getPropertyIssue("pojoMap.one.booleanToBoolean"));
    assertEquals(Optional.of("Can't be literally null"), inValid.getPropertyIssues().getPropertyIssue("pojoMap.two.stringToUUID"));
    assertEquals(Optional.of("Can't be literally null"), inValid.getPropertyIssues().getPropertyIssue("pojoMap.two.booleanToBoolean"));
  }

  /**
   * Scenario: It should be possible to validate POJOs with annotated (possibly java incompatible) real names
   */
  @Test
  public void validateDeepNamed() {
    // Given a POJO with annotated names
    DeepNamed validPojo = DeepNamed.builder().objectToPojo(new Nested().setBooleanToBoolean(true)).build();
    DeepNamed invalidPojo = DeepNamed.builder().objectToPojo(new Nested()).build();

    // When a validator is built with the annotated names
    Validator<Nested> nestedValidator = of(on("bool", required, notNull));
    Validator<DeepNamed> deepNamedValidator = of(on("obj", notNull, PropertyValidators.valid(nestedValidator)));

    // Then it should validate correctly
    Validation<DeepNamed> valid = deepNamedValidator.validate(validPojo);
    assertTrue(valid.isValid());

    Validation<DeepNamed> invalid = deepNamedValidator.validate(invalidPojo);
    assertTrue(invalid.isInvalid());
    assertTrue(invalid.getPropertyIssues().containsPropertyName("obj.bool"));
    assertEquals(Optional.of("Is required but missing"), invalid.getPropertyIssues().getPropertyIssue("obj.bool"));
  }

  private static Validator<Validatable> deepAndNestedValidator = of(
    on(NOT_NULL, notNull),
    on(REQUIRED, required),
    on(PATTERN_STRINGS, PropertyValidators.valid(of(
      on(ONE, pattern("^\\d+$")),
      on(TWO, passwordFormat),
      on(THREE, binaryFormat),
      on(FOUR, byteFormat)
    ))),
    on(MORE_PATTERN_STRINGS, PropertyValidators.valid(of(
      on(ONE, emailFormat),
      on(TWO, hostnameFormat),
      on(THREE, ipV4Format),
      on(FOUR, ipV6Format)
    ))),
    on(LENGTH_RESTRICTED_STRINGS, PropertyValidators.valid(of(
      on(ONE, maxLength(4)),
      on(TWO, minLength(4)),
      on(THREE, maxLength(5)),
      on(FOUR, minLength(3))
    ))),
    on(NUMBERS, PropertyValidators.valid(of(
      on(ONE, exclusiveMaximum(5)),
      on(TWO, exclusiveMinimum(3)),
      on(THREE, maximum(4)),
      on(FOUR, minimum(4))
    ))),
    on(MORE_NUMBERS, PropertyValidators.valid(of(
      on(ONE, exclusiveMaximum(4.5f)),
      on(TWO, exclusiveMinimum(4.3f)),
      on(THREE, maximum(4.4f)),
      on(FOUR, minimum(4.4f))
    ))),
    on(EVEN_MORE_NUMBERS, PropertyValidators.valid(of(
      on(ONE, maximum(4.5f)),
      on(TWO, minimum(4.3f)),
      on(THREE, multipleOf(2.2f)),
      on(FOUR, multipleOf(1.1f))
    ))),
    on(NUMBER_LIST, maxItems(4)),
    on(NUMBER_SET, minItems(4)),
    on(ENUM_MAP, maxItems(9)),
    on(SAME_POJO, PropertyValidators.valid(lazy(() -> ValidatePojoTests.deepAndNestedValidator))),
    on(POJOS, maxItems(5), each(PropertyValidators.valid(of(
      on(STRING_TO_UUID, required),
      on(BOOLEAN_TO_BOOLEAN, required)
    )))),
    on(POJO_MAP, minItems(3), each(PropertyValidators.valid(of(
      on(STRING_TO_UUID, notNull),
      on(BOOLEAN_TO_BOOLEAN, notNull)
    ))))
  );

  private static Validatable deepAndNested = new Validatable()
    .setNotNull("notNull")
    .setRequired("required")
    .setPatternStrings(new StringBundle()
      .setOne("123")
      .setTwo("0aA!")
      .setThree("1f03ff")
      .setFour("dGVzdA==")
    )
    .setMorePatternStrings(new StringBundle()
      .setOne("test@test.at")
      .setTwo("wikipedia.org")
      .setThree("127.0.0.1")
      .setFour("0000:0000:0000:0000:0000:0000:0000:0001")
    )
    .setLengthRestrictedStrings(new StringBundle()
      .setOne("0123")
      .setTwo("0123")
      .setThree("0123")
      .setFour("0123")
    )
    .setNumbers(new IntegerBundle()
      .setOne(4)
      .setTwo(4)
      .setThree(4)
      .setFour(4)
    )
    .setMoreNumbers(new FloatBundle()
      .setOne(4.4f)
      .setTwo(4.4f)
      .setThree(4.4f)
      .setFour(4.4f)
    )
    .setEvenMoreNumbers(new BigDecimalBundle()
      .setOne(BigDecimal.valueOf(4.4f))
      .setTwo(BigDecimal.valueOf(4.4f))
      .setThree(BigDecimal.valueOf(4.4f))
      .setFour(BigDecimal.valueOf(4.4f))
    )
    .setNumberList(List.of(1, 2, 3, 4))
    .setNumberSet(Set.of(4.1d, 4.2d, 4.3d, 4.4d))
    .setEnumMap(Map.of(
      "one", StringEnum.STR1,
      "two", StringEnum.STR2,
      "three", StringEnum.STR3
    ))
    .setPojo(new Nested()
      .setStringToUUID(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb0"))
      .setBooleanToBoolean(true)
    )
    .setSamePojo(new Validatable()
      .setNotNull("innerNotNull")
      .setRequired("innerRequired")
    )
    .setPojos(List.of(
      new Nested()
        .setStringToUUID(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb1"))
        .setBooleanToBoolean(true),
      new Nested()
        .setStringToUUID(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb2"))
        .setBooleanToBoolean(false),
      new Nested()
        .setStringToUUID(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb3"))
        .setBooleanToBoolean(true)
    ))
    .setPojoMap(Map.of(
      "one", new Nested()
        .setStringToUUID(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb4"))
        .setBooleanToBoolean(false),
      "two", new Nested()
        .setStringToUUID(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb5"))
        .setBooleanToBoolean(true),
      "three", new Nested()
        .setStringToUUID(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb6"))
        .setBooleanToBoolean(false)
    ));

  private static Validatable deepAndNestedAndFlawed = new Validatable()
    .setNotNull(null)
    .setPatternStrings(new StringBundle()
      .setOne("123a")
      .setTwo("a")
      .setThree("zzz")
      .setFour("123")
    )
    .setMorePatternStrings(new StringBundle()
      .setOne("test@test")
      .setTwo("wikipedia")
      .setThree("127.0.0")
      .setFour("0000:0000:0000:0000:0000:0000:0001")
    )
    .setLengthRestrictedStrings(new StringBundle()
      .setOne("01234")
      .setTwo("012")
    )
    .setNumbers(new IntegerBundle()
      .setOne(5)
      .setTwo(3)
      .setThree(5)
      .setFour(3)
    )
    .setMoreNumbers(new FloatBundle()
      .setOne(4.5f)
      .setTwo(4.3f)
      .setThree(4.4f)
      .setFour(4.3f)
    )
    .setEvenMoreNumbers(new BigDecimalBundle()
      .setOne(BigDecimal.valueOf(4.51f))
      .setTwo(BigDecimal.valueOf(4.29f))
      .setThree(BigDecimal.valueOf(4.43f))
      .setFour(BigDecimal.valueOf(4.42f))
    )
    .setNumberList(List.of(1, 2, 3, 4, 5))
    .setNumberSet(Set.of(4.1d, 4.2d, 4.3d))
    .setEnumMap(Map.of(
      "one", StringEnum.STR1,
      "two", StringEnum.STR2,
      "three", StringEnum.STR3
    ))
    .setPojo(new Nested()
      .setStringToUUID(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb0"))
      .setBooleanToBoolean(true)
    )
    .setSamePojo(new Validatable()
      .setNotNull(null)
    )
    .setPojos(List.of(
      new Nested(),
      new Nested(),
      new Nested(),
      new Nested(),
      new Nested(),
      new Nested(),
      new Nested()
    ))
    .setPojoMap(Map.of(
      "one", new Nested()
        .setStringToUUID(null)
        .setBooleanToBoolean(null),
      "two", new Nested()
        .setStringToUUID(null)
        .setBooleanToBoolean(null)
    ));
}
