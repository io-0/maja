package net.io_0.property;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.io_0.property.models.Pet;
import net.io_0.property.validation.Validation;
import net.io_0.property.validation.Validation.Valid;
import net.io_0.property.validation.Validator;
import org.junit.jupiter.api.Test;

import static net.io_0.property.Deserializer.*;
import static net.io_0.property.TestUtils.loadJsonResource;
import static net.io_0.property.Validators.petValidator;
import static net.io_0.property.validation.Validation.invalid;
import static org.junit.jupiter.api.Assertions.*;

class DeserializerTest {
  private ObjectMapper objectMapper = new ObjectMapper()
    .registerModules(
      new JavaTimeModule()
    )
    .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(
      DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
      DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES // ignore unknown fields
    )
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

  @Test
  public void deserializeAndValidate_test() {
    String json = loadJsonResource("Pet.json");
    json = json
      .replace(": 21,", ": \"A\",")
      .replace(": 220.1,", ": \"B\",")
      .replace(": \"2019-07-23\",", ": \"C\",")
      .replace("\"colorEnum\": \"green\"", "\"colorEnum\": \"greeni\"");

    Validator<Pet> customValidator1 = p -> invalid(PropertyIssue.of(Pet.NAME, "I don't like " + p.getName()));
    Validator<Pet> customValidator2 = Validation::valid;
    Validator<Pet> customValidators = customValidator1.and(customValidator2);

    Validation<Pet> validation = deserialize(objectMapper, json, Pet.class).apply(petValidator.and(customValidators));

    assertTrue(validation.isInvalid());
    assertEquals("[" +
      "PropertyIssue(propertyName=numDouble, issue=B, not a valid Double value), " +
      "PropertyIssue(propertyName=integ, issue=A, not a valid Integer value), " +
      "PropertyIssue(propertyName=strDate, issue=C, Failed to deserialize java.time.LocalDate: (java.time.format.DateTimeParseException) Text 'C' could not be parsed at index 0), " +
      "PropertyIssue(propertyName=zoo.1.colorEnum, issue=greeni, Unexpected value 'greeni'), " +
      "PropertyIssue(propertyName=petMap.aPet.integ, issue=A, not a valid Integer value), " +
      "PropertyIssue(propertyName=integ, issue=Must be 18 or greater), " +
      "PropertyIssue(propertyName=petMap.aPet.integ, issue=Must be 18 or greater), " +
      "PropertyIssue(propertyName=name, issue=I don't like Gerti)" +
      "]", validation.getPropertyIssues().toString());
  }

  @Test
  public void deserializeAndProceedIfValid_test() {
    String json = loadJsonResource("Pet.json");

    Valid<Pet> validation = deserialize(objectMapper, json, Pet.class).apply(petValidator).proceedIfValid();

    assertTrue(validation.isValid());
    assertTrue(validation.getPropertyIssues().isEmpty());
  }
}