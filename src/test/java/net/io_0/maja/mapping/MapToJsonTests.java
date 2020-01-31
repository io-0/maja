package net.io_0.maja.mapping;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.models.*;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import java.io.*;
import java.util.stream.Collectors;

import static net.io_0.maja.TestUtils.resourceAsReader;
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
public class MapToJsonTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  public void mapToNothing() {
    assertThrows(Mapper.MappingException.class, () -> Mapper.writeJson(null, null));
  }

  /**
   * Scenario: A flat JSON object should be mapped to a POJO and back
   */
  @Test
  public void mapToFlatJson() throws JSONException {
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
  public void mapToDeepJson() throws JSONException {
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
  public void mapToJsonWithNullAndAbsentProperties() throws JSONException {
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
  public void mapToDeepNamedJson() throws JSONException {
    // Given a deep JSON object with java special names
    Reader jsonReader = resourceAsReader("SimplifiedDeepNamed.json");

    // When it is mapped
    DeepNamed pojo = Mapper.readJson(jsonReader, DeepNamed.class);

    // Then the data should be present in the POJO
    assertDeepNamedDataPresent(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedDeepNamed.json")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer jsonWriter = new StringWriter();
    Mapper.writeJson(jsonWriter, pojo);
    String json = Mapper.toJson(pojo);

    JSONAssert.assertEquals(reference, jsonWriter.toString(), JSONCompareMode.NON_EXTENSIBLE);
    JSONAssert.assertEquals(reference, json, JSONCompareMode.NON_EXTENSIBLE);
  }
}
