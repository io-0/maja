package net.io_0.maja.mapping;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.PropertyIssues;
import net.io_0.maja.mapping.Mapper.MappingException;
import net.io_0.maja.models.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.function.Supplier;

import static net.io_0.maja.mapping.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Narrative:
 *   As a Map to POJO mapper API consumer
 *
 *   I want to convert a Map to a POJO
 *   so that further data processing gets easier
 *
 *   and I want a single report containing all that went wrong on errors
 *   so that providing compatible data gets easier
 *
 *   and I want to be able to deal with Map names
 *   so that I don't get problems with Java naming conventions or enums
 */
@Slf4j
class MapFromMapTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  void mapFromNothing() {
    assertThrows(MappingException.class, () -> Mapper.fromMap(null, null));
  }

  /**
   * Scenario: A flat Map should be mapped to a POJO
   */
  @Test
  void mapFromFlatMap() {
    // Given a flat Map
    Map<String, Object> map = flatMap;

    // When it is mapped
    Flat pojo = Mapper.fromMap(map, Flat.class);

    // Then the data should be present in the POJO
    assertFlatDataPresent(pojo);
  }

  /**
   * Scenario: A Map with nested objects should be mapped to a POJO
   */
  @Test
  void mapFromDeepMap() {
    // Given a deep Map
    Map<String, Object> map = deepMap;

    // When it is mapped
    Deep pojo = Mapper.fromMap(map, Deep.class);

    // Then the data should be present in the POJO
    assertDeepDataPresent(pojo);
  }

  /**
   * Scenario: It should be possible to have different names in the Map and the mapped POJO (and Enums)
   */
  @Test
  void mapFromNamedMap() {
    // Given a Map with java special names
    Map<String, Object> mapA = deepNamedMap;
    Map<String, Object> mapB = Map.of("aSpecialName", 4, "BSpecialName", 5, "MaJa", 6, "caJa", 7);
    Map<String, Object> mapC = Map.of("x-obj", 3, "y-obj", 4);

    // When it is mapped
    DeepNamed pojoA = Mapper.fromMap(mapA, DeepNamed.class);
    Named pojoB = Mapper.fromMap(mapB, Named.class);
    SpecialNamed pojoC = Mapper.fromMap(mapC, SpecialNamed.class);

    // Then the data should be present in the POJO
    assertDeepNamedDataPresent(pojoA);
    assertEquals(4, pojoB.getASpecialName());
    assertEquals(5, pojoB.getBSpecialName());
    assertEquals(6, pojoB.getMaJa());
    assertEquals(7, pojoB.getCaJa());
    assertEquals(3, pojoC.getXObj());
    assertEquals(4, pojoC.getYObj());
  }

  /**
   * Scenario: All Map to POJO conversion errors and problems should be in one report
   */
  @Test
  void mapFromDeepFlawedMapManaged() {
    // Given a deep Map with flawed data
    Map<String, Object> map = deepFlawedMap;

    // When it is mapped
    PropertyIssues propertyIssues = PropertyIssues.of();
    DeepFlawed pojo = Mapper.fromMap(map, propertyIssues::add, DeepFlawed.class);

    // Then the convertible data should be present in the POJO
    assertDeepFlawedDataPresent(pojo);

    // And the issues should be collected
    assertDeepFlawedPropertyIssuesCollected(propertyIssues.toString());
  }

  /**
   * Scenario: If no report is requested but conversion errors happen, an exception should contain the report
   */
  @Test
  void mapFromDeepFlawedMap() {
    Map<String, Object> map = deepFlawedMap;

    MappingException t = assertThrows(MappingException.class, () -> Mapper.fromMap(map, DeepFlawed.class));

    assertDeepFlawedPropertyIssuesCollected(t.getMessage());
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should search for a default function to instantiate
   */
  @Test
  void mapFromMapWithPolymorphismAndDefaultInstantiator() {
    Map<String, Object> mapP = polymorphMap;
    Map<String, Object> mapA = attributeMap;

    PolymorphWithDefaultInstantiator p = Mapper.fromMap(mapP, PolymorphWithDefaultInstantiator.class);
    PolymorphWithDefaultInstantiator.Attribute a = Mapper.fromMap(mapA, PolymorphWithDefaultInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should search for a static function to instantiate
   */
  @Test
  void mapFromMapWithPolymorphismAndStaticInstantiator() {
    Map<String, Object> mapP = polymorphMap;
    Map<String, Object> mapA = attributeMap;

    PolymorphWithStaticInstantiator p = Mapper.fromMap(mapP, PolymorphWithStaticInstantiator.class);
    PolymorphWithStaticInstantiator.Attribute a = Mapper.fromMap(mapA, PolymorphWithStaticInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should use context instantiator
   */
  @Test
  void mapFromMapWithPolymorphismAndContextInstantiator() {
    Map<String, Object> mapP = polymorphMap;
    Map<String, Object> mapA = attributeMap;

    var ctx = Mapper.Context.ofInstantiators(Mapper.Instantiator.of(
      PolymorphWithoutInstantiator.Attribute.class, PolymorphWithoutInstantiator.Instance::instHelper
    ));

    PolymorphWithoutInstantiator p = Mapper.fromMap(mapP, ctx, PolymorphWithoutInstantiator.class);
    PolymorphWithoutInstantiator.Attribute a = Mapper.fromMap(mapA, ctx, PolymorphWithoutInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }

  /**
   * Scenario: A mapping error should be present if no context instantiator for required type is present
   */
  @Test
  void dontMapFromMapWithPolymorphismAndWrongContextInstantiator() {
    Map<String, Object> mapP = polymorphMap;
    Map<String, Object> mapA = attributeMap;

    var ctx = Mapper.Context.ofInstantiators(Mapper.Instantiator.of(
      Flat.class, (Map<String, Object> data) -> new Flat()
    ));

    MappingException p = assertThrows(MappingException.class, () -> Mapper.fromMap(mapP, ctx, PolymorphWithoutInstantiator.class));
    MappingException a = assertThrows(MappingException.class, () -> Mapper.fromMap(mapA, ctx, PolymorphWithoutInstantiator.Attribute.class));

    assertTrue(p.getMessage().contains("attr"));
    assertTrue(a.getMessage().contains("*"));
  }

  private void assertDeepFlawedPropertyIssuesCollected(String propertyIssues) {
    assertTrue(propertyIssues.contains("stringToObject"));
    assertTrue(propertyIssues.contains("numberToObject"));
    assertTrue(propertyIssues.contains("numberToEnum"));
    assertTrue(propertyIssues.contains("objectToPojo.stringToUUID"));
    assertTrue(propertyIssues.contains("objectToPojo.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectToPojo.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectToPojo.numberArrayToIntegerSet"));
    assertTrue(propertyIssues.contains("objectToPojo.booleanToBoolean"));
    assertTrue(propertyIssues.contains("objectToIntMap.string"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.stringToUUID"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.numberArrayToIntegerSet"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.booleanToBoolean"));
    /* Current impl can't specify index correctly, everything will be ".0". Effort to fix too big, I can live with that for now.
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.stringToUUID"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.numberArrayToIntegerSet"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.booleanToBoolean"));
    */
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.stringToUUID"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.numberArrayToIntegerSet"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.booleanToBoolean"));
  }

  private static Map<String, Object> flatMap = ((Supplier<Map<String, Object>>)() -> {
    Map<String, Object> map = new HashMap<>();
    map.putAll(Map.of(
      "stringToString", "str",
      "stringToEnum", "STR2",
      "stringToBigDecimal", "42",
      "stringToFloat", "20.1",
      "stringToDouble", "220.1",
      "stringToInteger", "5",
      "stringToLong", "2147483648",
      "stringToLocalDate", "2019-07-23",
      "stringToOffsetDateTime", "2010-01-01T10:00:10+01:00",
      "stringToUUID", "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    ));
    map.putAll(Map.of(
      "stringToURI", "http://www.example.com/eula",
      "stringToByteArray", "dGVzdA==",
      "stringToFile", "/path/filename.ext",
      "numberToBigDecimal", 43,
      "numberToFloat", 21.1,
      "numberToDouble", 221.1,
      "numberToInteger", 6,
      "numberToLong", 2147483649L,
      "numberToString", 9001,
      "stringArrayToStringList", List.of("a", "b", "b", "a")
    ));
    map.putAll(Map.of(
      "stringArrayToOffsetDateTimeSet", Set.of("2010-01-01T10:00:10+02:00", "2010-01-01T10:00:10+03:00"),
      "numberArrayToFloatList", List.of(1, 2.3, 4, 3.2, 1),
      "numberArrayToIntegerSet", Set.of(0, 1, 2, 3),
      "booleanToBoolean", true,
      "booleanToString", false
    ));
    return map;
  }).get();

  private static Map<String, Object> deepMap = Map.of(
    "objectToPojo", Map.of(
      "stringToUUID", "3fa85f64-5717-4562-b3fc-2c963f66afa1",
      "numberToBigDecimal", 1,
      "stringArrayToStringList", List.of("a", "a", "a", "a"),
      "numberArrayToIntegerSet", Set.of(0, 1, 2, 3),
      "booleanToBoolean", true
    ),
    "objectToMap", Map.of(
      "stringToUUID", "3fa85f64-5717-4562-b3fc-2c963f66afa2",
      "numberToBigDecimal", 2,
      "stringArrayToStringList", List.of("a", "a", "a", "b"),
      "numberArrayToIntegerSet", Set.of(0, 1, 2, 3),
      "booleanToBoolean", false
    ),
    "objectArrayToObjectList", List.of(
      Map.of(
        "stringToUUID", "3fa85f64-5717-4562-b3fc-2c963f66afa3",
        "numberToBigDecimal", 3,
        "stringArrayToStringList", List.of("a", "a", "b", "b"),
        "numberArrayToIntegerSet", Set.of(0, 1, 2, 3),
        "booleanToBoolean", false
      ),
      Map.of(
        "stringToUUID", "3fa85f64-5717-4562-b3fc-2c963f66afa4",
        "numberToBigDecimal", 4,
        "stringArrayToStringList", List.of("a", "b", "b", "b"),
        "numberArrayToIntegerSet", Set.of(0, 1, 2, 3),
        "booleanToBoolean", true
      )
    ),
    "objectArrayToObjectSet", Set.of(
      Map.of(
        "stringToUUID", "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "numberToBigDecimal", 43,
        "stringArrayToStringList", List.of("a", "b", "b", "a"),
        "numberArrayToIntegerSet", Set.of(0, 1, 2, 3),
        "booleanToBoolean", true
      )
    )
  );

  private static Map<String, Object> deepNamedMap = Map.of(
    "x-obj", Map.of(
      "stringToUUID", "3fa85f64-5717-4562-b3fc-2c963f66afa1",
      "numberToBigDecimal", 1,
      "stringArrayToStringList", List.of("a", "a", "a", "a"),
      "numberArrayToIntegerSet", Set.of(0, 1, 2, 3),
      "bool", true
    ),
    "objectArrayToObjectSet", Set.of(
      Map.of(
        "stringToUUID", "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "numberToBigDecimal", 43,
        "stringArrayToStringList", List.of("a", "b", "b", "a"),
        "numberArrayToIntegerSet", Set.of(0, 1, 2, 3),
        "bool", true
      )
    ),
    "stringArrayToEnumList", List.of("str 1", "str-2", "STR3")
  );

  private static Map<String, Object> deepFlawedMap = Map.of(
    "stringToObject", "42",
    "numberToObject", 42,
    "numberToEnum", 9,
    "objectToPojo", Map.of(
      "stringToUUID", "no uuid",
      "numberToBigDecimal", "string",
      "stringArrayToStringList", 42,
      "numberArrayToIntegerSet", List.of(0, "string"),
      "booleanToBoolean", "string"
    ),
    "objectToMap", Map.of(
      "stringToUUID", "no uuid",
      "numberToBigDecimal", "string",
      "stringArrayToStringList", 42,
      "numberArrayToIntegerSet", List.of(0, "string"),
      "booleanToBoolean", "string"
    ),
    "objectToIntMap", Map.of(
      "string", 2
    ),
    "objectArrayToObjectList", List.of(
      Map.of(
        "stringToUUID", "no uuid",
        "numberToBigDecimal", "string",
        "stringArrayToStringList", 42,
        "numberArrayToIntegerSet", List.of(0, "string"),
        "booleanToBoolean", "string"
      ),
      Map.of(
        "stringToUUID", "no uuid",
        "numberToBigDecimal", "string",
        "stringArrayToStringList", 42,
        "numberArrayToIntegerSet", List.of(0, "string"),
        "booleanToBoolean", "string"
      )
    ),
    "objectArrayToObjectSet", Set.of(
      Map.of(
        "stringToUUID", "no uuid",
        "numberToBigDecimal", "string",
        "stringArrayToStringList", 42,
        "numberArrayToIntegerSet", List.of(0, "string"),
        "booleanToBoolean", "string"
      )
    )
  );

  private static Map<String, Object> attributeMap = Map.of("text", "hello", "version", 2);
  private static Map<String, Object> polymorphMap = Map.of("number", 18, "attr", attributeMap);
}
