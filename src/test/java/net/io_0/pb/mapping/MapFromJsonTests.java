package net.io_0.pb.mapping;

import lombok.extern.slf4j.Slf4j;
import net.io_0.pb.PropertyIssues;
import net.io_0.pb.models.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;

import static net.io_0.pb.TestUtils.*;
import static net.io_0.pb.mapping.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Narrative:
 *   As an JSON to POJO mapper API consumer
 *
 *   I want to convert a JSON object to a POJO
 *   so that further data processing gets easier
 *
 *   and I want to be able to distinguish between absent and null JSON values
 *   so that I can implement e.g. RFC 7386 - JSON Merge Patch
 *
 *   and I want a single report containing all that went wrong on errors
 *   so that providing compatible JSON data gets easier
 *
 *   and I want to be able to deal with JSON names
 *   so that I don't get problems with Java naming conventions or enums
 */
@Slf4j
public class MapFromJsonTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  public void mapFromNothing() {
    assertThrows(Mapper.MappingException.class, () -> Mapper.readJson(null, null));
  }

  /**
   * Scenario: A flat JSON object should be mapped to a POJO
   *
   * JSON -> POJO
   *   String -> String or
   *             Enum or
   *             BigDecimal or Float or Double or Integer or Long or
   *             LocalDate or OffsetDateTime or
   *             UUID or
   *             URI or
   *             byte[] or File
   *   Number -> BigDecimal or Float or Double or Integer or Long or
   *             String
   *   Array -> List or Set
   *   Boolean -> Boolean or
   *              String
   */
  @Test
  public void mapFromFlatJson() {
    // Given a flat JSON object
    Reader json = loadJsonResource("Flat.json");

    // When it is mapped
    Flat pojo = Mapper.readJson(json, Flat.class);

    // Then the data should be present in the POJO
    assertFlatDataPresent(pojo);
  }

  /**
   * Scenario: A JSON object with nested objects should be mapped to a POJO
   *
   * JSON -> POJO
   *   Object -> Map or POJO
   *   Array -> List or Set
   */
  @Test
  public void mapFromDeepJson() {
    // Given a deep JSON object
    Reader json = loadJsonResource("Deep.json");

    // When it is mapped
    Deep pojo = Mapper.readJson(json, Deep.class);

    // Then the data should be present in the POJO
    assertDeepDataPresent(pojo);
  }

  /**
   * Scenario: It should be possible to differentiate JSON undefined and null with the conversion result
   *
   * JSON -> POJO
   *   null -> null + marker
   *   absent -> null + no marker
   *   (we don't check the undefined token, it is valid js but not allowed in JSON)
   */
  @Test
  public void mapFromJsonWithNullAndAbsentProperties() {
    // Given a JSON object with nulls and absent properties
    Reader json = loadJsonResource("Partial.json");

    // When it is mapped
    Nested pojo = Mapper.readJson(json, Nested.class);

    // Then the data should be present in the POJO
    assertNestedDataPresent(pojo);

    // And the markers should be set correctly
    assertNestedDataMarkedCorrectly(pojo);
  }

  /**
   * Scenario: It should be possible to have different names in JSON and the mapped POJO (and Enums)
   */
  @Test
  public void mapFromDeepNamedJson() {
    // Given a deep JSON object with java special names
    Reader json = loadJsonResource("DeepNamed.json");

    // When it is mapped
    DeepNamed pojo = Mapper.readJson(json, DeepNamed.class);

    // Then the data should be present in the POJO
    assertDeepNamedDataPresent(pojo);
  }

  /**
   * Scenario: All JSON to POJO conversion errors and problems should be in one report
   */
  @Test
  public void mapFromDeepFlawedJsonManaged() {
    // Given a deep JSON object with flawed data
    Reader json = loadJsonResource("DeepFlawed.json");

    // When it is mapped
    PropertyIssues propertyIssues = PropertyIssues.of();
    DeepFlawed pojo = Mapper.readJson(json, DeepFlawed.class, propertyIssues::add);

    // Then the convertible data should be present in the POJO
    assertNotNull(pojo);

    assertNull(pojo.getStringToObject());
    assertNull(pojo.getNumberToObject());
    assertNull(pojo.getNumberToEnum());

    assertNotNull(pojo.getObjectToPojo());
    assertNull(pojo.getObjectToPojo().getStringToUUID());
    assertNull(pojo.getObjectToPojo().getNumberToBigDecimal());
    assertNull(pojo.getObjectToPojo().getStringArrayToStringList());
    Set<Integer> intSet = new HashSet<>(); intSet.add(0); intSet.add(null);
    assertCollectionEquals(intSet, pojo.getObjectToPojo().getNumberArrayToIntegerSet());
    assertNull(pojo.getObjectToPojo().getBooleanToBoolean());

    assertNotNull(pojo.getObjectToMap());
    assertTrue(pojo.getObjectToMap().containsKey("stringToUUID"));
    assertTrue(pojo.getObjectToMap().containsKey("numberToBigDecimal"));
    assertTrue(pojo.getObjectToMap().containsKey("stringArrayToStringList"));
    assertTrue(pojo.getObjectToMap().containsKey("numberArrayToIntegerSet"));
    assertTrue(pojo.getObjectToMap().containsKey("booleanToBoolean"));

    assertNotNull(pojo.getObjectArrayToObjectList());
    assertEquals(2, pojo.getObjectArrayToObjectList().size());
    assertEquals("Nested(stringToUUID=null, numberToBigDecimal=null, stringArrayToStringList=null, numberArrayToIntegerSet=[0, null], booleanToBoolean=null)",
      pojo.getObjectArrayToObjectList().get(0).toString());
    assertEquals("Nested(stringToUUID=null, numberToBigDecimal=null, stringArrayToStringList=null, numberArrayToIntegerSet=[0, null], booleanToBoolean=null)",
      pojo.getObjectArrayToObjectList().get(1).toString());

    assertNotNull(pojo.getObjectArrayToObjectSet());
    assertEquals(1, pojo.getObjectArrayToObjectSet().size());
    assertEquals("Nested(stringToUUID=null, numberToBigDecimal=null, stringArrayToStringList=null, numberArrayToIntegerSet=[0, null], booleanToBoolean=null)",
      pojo.getObjectArrayToObjectSet().toArray()[0].toString());

    // And the issues should be collected
    assertEquals(deepFlawedJsonIssuesString, propertyIssues.toString().replaceAll("@[\\w]+", ""));
  }

  private static final String deepFlawedJsonIssuesString =
    "stringToObject -> com.fasterxml.jackson.databind.deser.std.StdValueInstantiator, no String-argument constructor/factory method to deserialize from String value ('42'); " +
    "numberToObject -> 42, Cannot construct instance of `net.io_0.pb.models.DeepFlawed$PublicInner`, problem: `java.lang.IllegalStateException` at [Source: (InputStreamReader); line: 3, column: 21]; " +
    "numberToEnum -> 9, index value outside legal index range [0..2]; " +
    "objectToPojo.stringToUUID -> no uuid, UUID has to be represented by standard 36-char representation; " +
    "objectToPojo.numberToBigDecimal -> string, not a valid representation; " +
    "objectToPojo.stringArrayToStringList -> VALUE_NUMBER_INT, null; " +
    "objectToPojo.numberArrayToIntegerSet.1 -> string, not a valid Integer value; " +
    "objectToPojo.booleanToBoolean -> string, only \"true\" or \"false\" recognized; " +
    "objectToIntMap.string -> string, not a valid representation, problem: (java.lang.NumberFormatException) For input string: \"string\"; " +
    "objectArrayToObjectList.0.stringToUUID -> no uuid, UUID has to be represented by standard 36-char representation; " +
    "objectArrayToObjectList.0.numberToBigDecimal -> string, not a valid representation; " +
    "objectArrayToObjectList.0.stringArrayToStringList -> VALUE_NUMBER_INT, null; " +
    "objectArrayToObjectList.0.numberArrayToIntegerSet.1 -> string, not a valid Integer value; " +
    "objectArrayToObjectList.0.booleanToBoolean -> string, only \"true\" or \"false\" recognized; " +
    "objectArrayToObjectList.1.stringToUUID -> no uuid, UUID has to be represented by standard 36-char representation; " +
    "objectArrayToObjectList.1.numberToBigDecimal -> string, not a valid representation; " +
    "objectArrayToObjectList.1.stringArrayToStringList -> VALUE_NUMBER_INT, null; " +
    "objectArrayToObjectList.1.numberArrayToIntegerSet.1 -> string, not a valid Integer value; " +
    "objectArrayToObjectList.1.booleanToBoolean -> string, only \"true\" or \"false\" recognized; " +
    "objectArrayToObjectSet.0.stringToUUID -> no uuid, UUID has to be represented by standard 36-char representation; " +
    "objectArrayToObjectSet.0.numberToBigDecimal -> string, not a valid representation; " +
    "objectArrayToObjectSet.0.stringArrayToStringList -> VALUE_NUMBER_INT, null; " +
    "objectArrayToObjectSet.0.numberArrayToIntegerSet.1 -> string, not a valid Integer value; " +
    "objectArrayToObjectSet.0.booleanToBoolean -> string, only \"true\" or \"false\" recognized; " +
    "objectArrayToObjectSet.1.stringToUUID -> no uuid, UUID has to be represented by standard 36-char representation; " +
    "objectArrayToObjectSet.1.numberToBigDecimal -> string, not a valid representation; " +
    "objectArrayToObjectSet.1.stringArrayToStringList -> VALUE_NUMBER_INT, null; " +
    "objectArrayToObjectSet.1.numberArrayToIntegerSet.1 -> string, not a valid Integer value; " +
    "objectArrayToObjectSet.1.booleanToBoolean -> string, only \"true\" or \"false\" recognized";

  /**
   * Scenario: If no report is requested but conversion errors happen, an exception should contain the report
   */
  @Test
  public void mapFromDeepFlawedJson() {
    Reader json = loadJsonResource("DeepFlawed.json");
    Mapper.MappingException t = assertThrows(Mapper.MappingException.class, () -> Mapper.readJson(json, DeepFlawed.class));
    assertEquals("java.lang.IllegalStateException: " + deepFlawedJsonIssuesString, t.getMessage().replaceAll("@[\\w]+", ""));
  }
}
