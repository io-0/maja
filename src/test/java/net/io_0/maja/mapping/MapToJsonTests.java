package net.io_0.maja.mapping;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.models.*;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.io_0.maja.TestUtils.resourceAsReader;
import static net.io_0.maja.TestUtils.resourceAsString;
import static net.io_0.maja.mapping.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Narrative:
 *   As an POJO to JSON mapper API consumer
 *
 *   I want to convert a POJO to a JSON object
 *   so that I can interact with APIs that require JSON
 *
 *   and I want to be able to distinguish between absent and null JSON values
 *   so that I can call APIs that implement e.g. RFC 7386 - JSON Merge Patch
 *
 *   and I want to be able to deal with JSON names
 *   so that I don't get problems with Java naming conventions or enums
 */
@Slf4j
class MapToJsonTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  void mapToNothing() {
    assertThrows(Mapper.MappingException.class, () -> Mapper.writeJson(null, null));
  }

  /**
   * Scenario: A flat JSON object should be mapped to a POJO and back
   */
  @Test
  void mapToFlatJson() throws JSONException {
    // Given a flat JSON object
    Reader jsonReader = resourceAsReader("SimplifiedFlat.json");

    // When it is mapped
    Flat pojo = Mapper.readJson(jsonReader, Flat.class);

    // Then the data should be present in the POJO
    assertFlatDataPresent(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedFlat.json")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer jsonWriter = new StringWriter();
    Mapper.writeJson(jsonWriter, pojo);
    String json = Mapper.toJson(pojo);

    JSONAssert.assertEquals(reference, jsonWriter.toString(), JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(reference, json, JSONCompareMode.NON_EXTENSIBLE);
  }

  /**
   * Scenario: A JSON object with nested objects should be mapped to a POJO and back
   */
  @Test
  void mapToDeepJson() throws JSONException {
    // Given a deep JSON object
    Reader jsonReader = resourceAsReader("SimplifiedDeep.json");

    // When it is mapped
    Deep pojo = Mapper.readJson(jsonReader, Deep.class);

    // Then the data should be present in the POJO
    assertDeepDataPresent(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedDeep.json")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer jsonWriter = new StringWriter();
    Mapper.writeJson(jsonWriter, pojo);
    String json = Mapper.toJson(pojo);

    JSONAssert.assertEquals(reference, jsonWriter.toString(), JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(reference, json, JSONCompareMode.NON_EXTENSIBLE);
  }

  /**
   * Scenario: It should be possible to map JSON with absent properties and null to POJO and back
   */
  @Test
  void mapToJsonWithNullAndAbsentProperties() throws JSONException {
    // Given a JSON object with nulls and absent properties
    Reader jsonReader = resourceAsReader("SimplifiedPartial.json");

    // When it is mapped
    Nested pojo = Mapper.readJson(jsonReader, Nested.class);

    // Then the data should be present in the POJO
    assertNestedDataPresent(pojo);

    // And the markers should be set correctly
    assertNestedDataMarkedCorrectly(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedPartial.json")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer jsonWriter = new StringWriter();
    Mapper.writeJson(jsonWriter, pojo);
    String json = Mapper.toJson(pojo);

    JSONAssert.assertEquals(reference, jsonWriter.toString(), JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(reference, json, JSONCompareMode.NON_EXTENSIBLE);
  }

  /**
   * Scenario: It should be possible to have different names in JSON and POJOs (and Enums)
   */
  @Test
  void mapToDeepNamedJson() throws JSONException {
    // Given a deep JSON object with java special names
    Reader jsonReaderA = resourceAsReader("SimplifiedDeepNamed.json");
    Reader jsonReaderB = resourceAsReader("Named.json");
    Reader jsonReaderC = resourceAsReader("SpecialNamed.json");

    // When it is mapped
    DeepNamed pojoA = Mapper.readJson(jsonReaderA, DeepNamed.class);
    Named pojoB = Mapper.readJson(jsonReaderB, Named.class);
    SpecialNamed pojoC = Mapper.readJson(jsonReaderC, SpecialNamed.class);

    // Then the data should be present in the POJO
    assertDeepNamedDataPresent(pojoA);
    assertEquals(4, pojoB.getASpecialName());
    assertEquals(5, pojoB.getBSpecialName());
    assertEquals(6, pojoB.getMaJa());
    assertEquals(7, pojoB.getCaJa());
    assertEquals(3, pojoC.getXObj());
    assertEquals(4, pojoC.getYObj());

    // And mapping back should not loose information
    String referenceA = new BufferedReader(resourceAsReader("SimplifiedDeepNamed.json")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer jsonWriterA = new StringWriter();
    Mapper.writeJson(jsonWriterA, pojoA);
    String jsonA = Mapper.toJson(pojoA);
    String referenceB = new BufferedReader(resourceAsReader("Named.json")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer jsonWriterB = new StringWriter();
    Mapper.writeJson(jsonWriterB, pojoB);
    String jsonB = Mapper.toJson(pojoB);
    String referenceC = new BufferedReader(resourceAsReader("SpecialNamed.json")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer jsonWriterC = new StringWriter();
    Mapper.writeJson(jsonWriterC, pojoC);
    String jsonC = Mapper.toJson(pojoC);

    JSONAssert.assertEquals(referenceA, jsonWriterA.toString(), JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(referenceA, jsonA, JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(referenceB, jsonWriterB.toString(), JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(referenceB, jsonB, JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(referenceC, jsonWriterC.toString(), JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(referenceC, jsonC, JSONCompareMode.NON_EXTENSIBLE);
  }

  /**
   * Scenario: It should be possible to serialize empty collections
   */
  @Test
  void serializeEmptyCollections() {
    // Given a model with set empty collections
    Flat model0 = new Flat();
    Flat model1 = new Flat();
    model1.setNumberArrayToFloatList(Collections.emptyList());
    model1.setNumberArrayToIntegerSet(Set.of());
    CollectionBundle model2 = new CollectionBundle();
    CollectionBundle model3 = new CollectionBundle().setOne(null).setTwo(null).setThree(null);
    CollectionBundle model4 = new CollectionBundle().setOne(List.of()).setTwo(Set.of()).setThree(Map.of());

    // When serialized
    String json0 = Mapper.toJson(model0);
    String json1 = Mapper.toJson(model1);
    String json2 = Mapper.toJson(model2);
    String json3 = Mapper.toJson(model3);
    String json4 = Mapper.toJson(model4);

    // Then empty collections should be serialized, null and not set should not
    assertFalse(json0.contains("\"numberArrayToFloatList\""));
    assertFalse(json0.contains("\"numberArrayToIntegerSet\""));

    assertTrue(json1.contains("\"numberArrayToFloatList\":[]"));
    assertTrue(json1.contains("\"numberArrayToIntegerSet\":[]"));

    assertFalse(json2.contains("\"one\""));
    assertFalse(json2.contains("\"two\""));
    assertFalse(json2.contains("\"three\""));

    assertTrue(json3.contains("\"one\":null"));
    assertTrue(json3.contains("\"two\":null"));
    assertTrue(json3.contains("\"three\":null"));

    assertTrue(json4.contains("\"one\":[]"));
    assertTrue(json4.contains("\"two\":[]"));
    assertTrue(json4.contains("\"three\":{}"));
  }

  /**
   * Scenario: It should be possible to serialize Java interfaces (with instantiate functions)
   */
  @Test
  void serializeWithPolymorphism() throws JSONException {
    // Given a json reference and a model
    String referenceP = resourceAsString("Polymorph.json");
    String referenceA = resourceAsString("Attribute.json");
    Polymorph p = new Polymorph(18, new Polymorph.Instance("hello", 2));
    Polymorph.Attribute a = new Polymorph.Instance("hello", 2);

    // When serialized
    String jsonP = Mapper.toJson(p);
    String jsonA = Mapper.toJson(a);

    // Result should match reference
    JSONAssert.assertEquals(referenceP, jsonP, JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(referenceA, jsonA, JSONCompareMode.NON_EXTENSIBLE);
  }
}
