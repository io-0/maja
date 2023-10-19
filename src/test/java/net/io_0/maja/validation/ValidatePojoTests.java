package net.io_0.maja.validation;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.*;
import net.io_0.maja.PropertyIssue.Issue;
import net.io_0.maja.models.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
 *   and I want some convenience
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
class ValidatePojoTests {
  /**
   * Scenario: It should be easy to build a POJO validator based on business logic and run it
   */
  @Test
  void buildBusinessLogicValidatorAndRunIt() {
    // Given a business rule and error message and code
    Predicate<Flat> intAndLongNumberMustBePresentAndMatch = item ->
      item.getNumberToInteger() != null && item.getNumberToLong() != null &&
      item.getNumberToInteger().equals(item.getNumberToLong().intValue());
    String errorCode = "Business Rule Violation";
    String errorMessage = "numberToInteger value must match numberToLong";

    // When a validator is built compact and with ease
    Validator<Flat> intAndLongNumberValidator = item ->
      intAndLongNumberMustBePresentAndMatch.test(item) ? valid(item) : invalid(PropertyIssue.of("numberToInteger, numberToLong", errorCode, errorMessage));

    // Then it should validate correctly
    Validation<Flat> invalidBecauseMissing = intAndLongNumberValidator.validate(Flat.builder().build());
    assertTrue(invalidBecauseMissing.isInvalid());
    assertTrue(invalidBecauseMissing.getPropertyIssues().containsPropertyName("numberToInteger, numberToLong"));
    assertEquals(
      Optional.of("Business Rule Violation"),
      invalidBecauseMissing.getPropertyIssues().getPropertyIssue("numberToInteger, numberToLong").map(Issue::getCode)
    );

    Validation<Flat> invalidBecauseMismatch = intAndLongNumberValidator.validate(Flat.builder().numberToInteger(2).numberToLong(4L).build());
    assertTrue(invalidBecauseMismatch.isInvalid());
    assertTrue(invalidBecauseMismatch.getPropertyIssues().containsPropertyName("numberToInteger, numberToLong"));
    assertEquals(
      Optional.of("Business Rule Violation"),
      invalidBecauseMismatch.getPropertyIssues().getPropertyIssue("numberToInteger, numberToLong").map(Issue::getCode)
    );

    Validation<Flat> valid = intAndLongNumberValidator.validate(Flat.builder().numberToInteger(2).numberToLong(2L).build());
    assertTrue(valid.isValid());

    // And it should be easy to run
    Flat model = Flat.builder().numberToInteger(4).numberToLong(4L).build();
    assertEquals(model, intAndLongNumberValidator.ensureValidity(model));
  }

  /**
   * Scenario: It should be easy to build a POJO validator based on property constraints and run it
   */
  @Test
  @SuppressWarnings("unchecked")
  void buildPropertyConstraintsValidatorAndRunIt() {
    // Given property constraints
    //   flat.stringToString must be present and at least 4 chars long
    //   flat.stringToInteger can be null or Exclusive Minimum Violation, 18
    //   flat.stringArrayToStringList must be present and has at least 3 items and each can't be null and has between 4 and 10 chars
    //   flat.numberArrayToFloatList must be present and has exactly 4 items and each can't be null and is between 2 and 2.5
    //   flat.stringArrayToOffsetDateTimeSet must be present and each can't be null

    // When a validator is built compact and with ease
    Validator<Flat> flatValidator = of(
      on("stringToString", notNull, minLength(4)),
      on("stringToInteger", minimum(18)),
      on("stringArrayToStringList", notNull, minItems(3), each(notNull, minLength(4), maxLength(10))),
      on("numberArrayToFloatList", notNull, minItems(4), maxItems(4), each(notNull, minimum(2F), maximum(2.5F))),
      on("stringArrayToOffsetDateTimeSet", notNull, each(notNull))
    );

    // Then it should validate correctly
    Validation<Flat> invalidBecauseEmpty = flatValidator.validate(Flat.builder().build());
    assertTrue(invalidBecauseEmpty.isInvalid());
    assertTrue(invalidBecauseEmpty.getPropertyIssues().containsPropertyName("stringToString"));
    assertTrue(invalidBecauseEmpty.getPropertyIssues().containsPropertyName("stringArrayToStringList"));
    assertTrue(invalidBecauseEmpty.getPropertyIssues().containsPropertyName("numberArrayToFloatList"));
    assertTrue(invalidBecauseEmpty.getPropertyIssues().containsPropertyName("stringArrayToOffsetDateTimeSet"));
    assertEquals(4, invalidBecauseEmpty.getPropertyIssues().size());
    assertEquals(Optional.of("Not Null Violation"),
      invalidBecauseEmpty.getPropertyIssues().getPropertyIssue("stringToString").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      invalidBecauseEmpty.getPropertyIssues().getPropertyIssue("stringArrayToStringList").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      invalidBecauseEmpty.getPropertyIssues().getPropertyIssue("numberArrayToFloatList").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      invalidBecauseEmpty.getPropertyIssues().getPropertyIssue("stringArrayToOffsetDateTimeSet").map(Issue::getCode));

    Validation<Flat> invalid = flatValidator.validate(
      Flat.builder()
        .stringToString("two")
        .stringToInteger(7)
        .stringArrayToStringList(List.of("one", "five hundred ten"))
        .numberArrayToFloatList(List.of(1F, 2F, 2.5F, 2.6F, 3F))
        .stringArrayToOffsetDateTimeSet(new HashSet<>(Arrays.asList(null, OffsetDateTime.now())))
        .build()
    );
    assertTrue(invalid.isInvalid());
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringToString"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringToInteger"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringArrayToStringList"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringArrayToStringList.0"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringArrayToStringList.1"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("numberArrayToFloatList"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("numberArrayToFloatList.0"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("numberArrayToFloatList.3"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("numberArrayToFloatList.4"));
    assertTrue(invalid.getPropertyIssues().containsPropertyName("stringArrayToOffsetDateTimeSet.0"));
    assertEquals(Optional.of("Min Length Violation, 4"),
      invalid.getPropertyIssues().getPropertyIssue("stringToString").map(Issue::getCode));
    assertEquals(Optional.of("Minimum Violation, 18"),
      invalid.getPropertyIssues().getPropertyIssue("stringToInteger").map(Issue::getCode));
    assertEquals(Optional.of("Min Items Violation, 3"),
      invalid.getPropertyIssues().getPropertyIssue("stringArrayToStringList").map(Issue::getCode));
    assertEquals(Optional.of("Min Length Violation, 4"),
      invalid.getPropertyIssues().getPropertyIssue("stringArrayToStringList.0").map(Issue::getCode));
    assertEquals(Optional.of("Max Length Violation, 10"),
      invalid.getPropertyIssues().getPropertyIssue("stringArrayToStringList.1").map(Issue::getCode));
    assertEquals(Optional.of("Max Items Violation, 4"),
      invalid.getPropertyIssues().getPropertyIssue("numberArrayToFloatList").map(Issue::getCode));
    assertEquals(Optional.of("Minimum Violation, 2.0"),
      invalid.getPropertyIssues().getPropertyIssue("numberArrayToFloatList.0").map(Issue::getCode));
    assertEquals(Optional.of("Maximum Violation, 2.5"),
      invalid.getPropertyIssues().getPropertyIssue("numberArrayToFloatList.3").map(Issue::getCode));
    assertEquals(Optional.of("Maximum Violation, 2.5"),
      invalid.getPropertyIssues().getPropertyIssue("numberArrayToFloatList.4").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      invalid.getPropertyIssues().getPropertyIssue("stringArrayToOffsetDateTimeSet.0").map(Issue::getCode));

    Validation<Flat> valid = flatValidator.validate(
      Flat.builder()
        .stringToString("four")
        .stringToInteger(21)
        .stringArrayToStringList(List.of("five", "seven", "twelve"))
        .numberArrayToFloatList(List.of(2.1F, 2.2F, 2.3F, 2.4F))
        .stringArrayToOffsetDateTimeSet(Set.of())
        .build()
    );
    assertTrue(valid.isValid());
  }

  /**
   * Scenario: It should be convenient to work with validators and their components
   */
  @Test
  void testConvenienceAndUsabilityFurther() {
    Validator<Nested> validator = of(on(BOOLEAN_TO_BOOLEAN, required));

    // Process validation exception on validator when we went the assure route
    // (if no exception is desired, use .validate instead of .ensureValidity)
    Validator.ValidationException ex = assertThrows(Validator.ValidationException.class, () -> validator.ensureValidity(new Nested()));
    assertTrue(ex.getValidation().getPropertyIssues().containsPropertyName(BOOLEAN_TO_BOOLEAN));

    // Same with custom exception
    class MyEx extends IllegalStateException { public MyEx(String s) { super(s); } }
    MyEx ex1 = assertThrows(MyEx.class, () -> validator.ensureValidity(new Nested(), invalid -> new MyEx(invalid.getPropertyIssues().toString())));
    assertTrue(ex1.getMessage().contains(BOOLEAN_TO_BOOLEAN) && ex1.getMessage().contains("required"));

    // Process validation exception on validation
    Validation<Nested> inValid = validator.validate(new Nested());
    Validator.ValidationException ex2 = assertThrows(Validator.ValidationException.class, inValid::getValue);
    assertTrue(ex2.getValidation().getPropertyIssues().containsPropertyName(BOOLEAN_TO_BOOLEAN));

    // Validators can be combined and created from issues e.g. from mapping
    Validator<Object> validator1 = obj -> valid(null);
    Validator<String> validator2 = obj -> invalid(PropertyIssue.of("property1", "code1", "issue1"));
    Validator<String> validator3 = of(PropertyIssues.of(
      PropertyIssue.of("property2", "code2", "issue2"),
      PropertyIssue.of("property3", "code3", "issue3"))
    );
    Validator<String> combinedValidator = validator1.and(validator2).and(validator3);
    Validation<String> combinedValidation = combinedValidator.validate(null);
    assertTrue(combinedValidation.isInvalid());
    assertEquals(3, combinedValidation.getPropertyIssues().size());
    assertEquals(Optional.of("code1"), combinedValidation.getPropertyIssues().getPropertyIssue("property1").map(Issue::getCode));
    assertEquals(Optional.of("code2"), combinedValidation.getPropertyIssues().getPropertyIssue("property2").map(Issue::getCode));
    assertEquals(Optional.of("code3"), combinedValidation.getPropertyIssues().getPropertyIssue("property3").map(Issue::getCode));

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
    IllegalArgumentException iAEx = assertThrows(IllegalArgumentException.class, () -> Property.from(new Object(), "property1"));
    assertEquals("Property with name 'property1' not found on Object", iAEx.getMessage());
    iAEx = assertThrows(IllegalArgumentException.class, () -> Property.from(new Nested(), "property1"));
    assertEquals("Property with name 'property1' not found on Nested", iAEx.getMessage());

    // Same goes for other problems
    iAEx = assertThrows(IllegalArgumentException.class, () -> Property.from(new Object() {
      public Boolean getBooleanToBoolean() { throw new IllegalStateException(); }
    }, BOOLEAN_TO_BOOLEAN));
    assertTrue(iAEx.getMessage().startsWith("Couldn't access property with name 'booleanToBoolean' on"));
    iAEx = assertThrows(IllegalArgumentException.class, () -> Property.from(new Nested() {
      @Override public Boolean getBooleanToBoolean() { throw new IllegalStateException(); }
    }, BOOLEAN_TO_BOOLEAN).isNull());
    assertTrue(iAEx.getMessage().startsWith("Couldn't access property with name 'booleanToBoolean' on"));
  }

  /**
   * Scenario: A validator (property constraints based) should be able to distinguish between absent and null values
   *           given a POJO that implements PropertyBundle correctly
   */
  @Test
  void validateAbsentAndNull() {
    // Given a POJO (that implements PropertyBundle correctly) with absent and null values
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
    assertEquals(Optional.of("Not Null Violation"), invalid.getPropertyIssues().getPropertyIssue(STRING_TO_UUID).map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"), invalid.getPropertyIssues().getPropertyIssue(NUMBER_TO_BIG_DECIMAL).map(Issue::getCode));
  }

  /**
   * Scenario: It should be possible to validate deep and nested POJOs
   */
  @Test
  void validateDeepAndNested() {
    // Given a POJO (that implements PropertyBundle correctly) which is deep and nested
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
  void rAV() {
    // Given a POJO (that implements PropertyBundle correctly) which is flawed
    Validatable pojo = deepAndNestedAndFlawed;

    // When a validator is built
    Validator<Validatable> validator = deepAndNestedValidator;

    // Then it should validate correctly and contain all errors in one report (we count propertyIssues as "report")
    Validation<Validatable> inValid = validator.validate(pojo);
    assertTrue(inValid.isInvalid());
    assertEquals(Optional.of("Not Null Violation"),
      inValid.getPropertyIssues().getPropertyIssue("notNull").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("required").map(Issue::getCode));
    assertEquals(Optional.of("Pattern Violation, '^\\d+$'"),
      inValid.getPropertyIssues().getPropertyIssue("patternStrings.one").map(Issue::getCode));
    assertEquals(Optional.of("Password Format Violation"),
      inValid.getPropertyIssues().getPropertyIssue("patternStrings.two").map(Issue::getCode));
    assertEquals(Optional.of("Binary Format Violation"),
      inValid.getPropertyIssues().getPropertyIssue("patternStrings.three").map(Issue::getCode));
    assertEquals(Optional.of("Byte Format Violation"),
      inValid.getPropertyIssues().getPropertyIssue("patternStrings.four").map(Issue::getCode));
    assertEquals(Optional.of("Email Format Violation"),
      inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.one").map(Issue::getCode));
    assertEquals(Optional.of("Hostname Format Violation"),
      inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.two").map(Issue::getCode));
    assertEquals(Optional.of("IP V4 Format Violation"),
      inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.three").map(Issue::getCode));
    assertEquals(Optional.of("IP V6 Format Violation"),
      inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.four").map(Issue::getCode));
    assertEquals(Optional.of("Url Format Violation"),
      inValid.getPropertyIssues().getPropertyIssue("morePatternStrings.five").map(Issue::getCode));
    assertEquals(Optional.of("Max Length Violation, 4"),
      inValid.getPropertyIssues().getPropertyIssue("lengthRestrictedStrings.one").map(Issue::getCode));
    assertEquals(Optional.of("Min Length Violation, 4"),
      inValid.getPropertyIssues().getPropertyIssue("lengthRestrictedStrings.two").map(Issue::getCode));
    assertEquals(Optional.of("Exclusive Maximum Violation, 5"),
      inValid.getPropertyIssues().getPropertyIssue("numbers.one").map(Issue::getCode));
    assertEquals(Optional.of("Exclusive Minimum Violation, 3"),
      inValid.getPropertyIssues().getPropertyIssue("numbers.two").map(Issue::getCode));
    assertEquals(Optional.of("Maximum Violation, 4"),
      inValid.getPropertyIssues().getPropertyIssue("numbers.three").map(Issue::getCode));
    assertEquals(Optional.of("Minimum Violation, 4"),
      inValid.getPropertyIssues().getPropertyIssue("numbers.four").map(Issue::getCode));
    assertEquals(Optional.of("Exclusive Maximum Violation, 4.5"),
      inValid.getPropertyIssues().getPropertyIssue("moreNumbers.one").map(Issue::getCode));
    assertEquals(Optional.of("Exclusive Minimum Violation, 4.3"),
      inValid.getPropertyIssues().getPropertyIssue("moreNumbers.two").map(Issue::getCode));
    assertEquals(Optional.of("Minimum Violation, 4.4"),
      inValid.getPropertyIssues().getPropertyIssue("moreNumbers.four").map(Issue::getCode));
    assertEquals(Optional.of("Maximum Violation, 4.5"),
      inValid.getPropertyIssues().getPropertyIssue("evenMoreNumbers.one").map(Issue::getCode));
    assertEquals(Optional.of("Minimum Violation, 4.3"),
      inValid.getPropertyIssues().getPropertyIssue("evenMoreNumbers.two").map(Issue::getCode));
    assertEquals(Optional.of("Multiple Of Violation, 2.2"),
      inValid.getPropertyIssues().getPropertyIssue("evenMoreNumbers.three").map(Issue::getCode));
    assertEquals(Optional.of("Multiple Of Violation, 1.1"),
      inValid.getPropertyIssues().getPropertyIssue("evenMoreNumbers.four").map(Issue::getCode));
    assertEquals(Optional.of("Max Items Violation, 4"),
      inValid.getPropertyIssues().getPropertyIssue("numberList").map(Issue::getCode));
    assertEquals(Optional.of("Min Items Violation, 4"),
      inValid.getPropertyIssues().getPropertyIssue("numberSet").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      inValid.getPropertyIssues().getPropertyIssue("samePojo.notNull").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("samePojo.required").map(Issue::getCode));
    assertEquals(Optional.of("Max Items Violation, 5"),
      inValid.getPropertyIssues().getPropertyIssue("pojos").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.0.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.0.booleanToBoolean").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.1.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.1.booleanToBoolean").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.2.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.2.booleanToBoolean").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.3.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.3.booleanToBoolean").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.4.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.4.booleanToBoolean").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.5.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.5.booleanToBoolean").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.6.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Required Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojos.6.booleanToBoolean").map(Issue::getCode));
    assertEquals(Optional.of("Min Items Violation, 3"),
      inValid.getPropertyIssues().getPropertyIssue("pojoMap").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojoMap.one.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojoMap.one.booleanToBoolean").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojoMap.two.stringToUUID").map(Issue::getCode));
    assertEquals(Optional.of("Not Null Violation"),
      inValid.getPropertyIssues().getPropertyIssue("pojoMap.two.booleanToBoolean").map(Issue::getCode));
  }

  /**
   * Scenario: It should be possible to validate POJOs with annotated (possibly java incompatible) real names
   */
  @Test
  void validateDeepNamed() {
    // Given a POJO with annotated names
    DeepNamed validPojo = DeepNamed.builder().objectToPojo(new Nested().setBooleanToBoolean(true)).build();
    DeepNamed invalidPojo = DeepNamed.builder().objectToPojo(new Nested()).build();

    // When a validator is built with the annotated names
    Validator<Nested> nestedValidator = of(on("bool", required, notNull));
    Validator<DeepNamed> deepNamedValidator = of(on("x-obj", notNull, PropertyValidators.valid(nestedValidator)));

    // Then it should validate correctly
    Validation<DeepNamed> valid = deepNamedValidator.validate(validPojo);
    assertTrue(valid.isValid());

    Validation<DeepNamed> invalid = deepNamedValidator.validate(invalidPojo);
    assertTrue(invalid.isInvalid());
    assertTrue(invalid.getPropertyIssues().containsPropertyName("x-obj.bool"));
    assertEquals(Optional.of("Required Violation"), invalid.getPropertyIssues().getPropertyIssue("x-obj.bool").map(Issue::getCode));
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
      on(FOUR, ipV6Format),
      on(FIVE, urlFormat)
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
      .setFive("invalidUrl")
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
