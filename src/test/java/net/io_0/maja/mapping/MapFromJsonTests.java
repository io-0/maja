package net.io_0.maja.mapping;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.PropertyIssues;
import net.io_0.maja.models.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;

import static net.io_0.maja.TestUtils.*;
import static net.io_0.maja.mapping.Assertions.*;
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
class MapFromJsonTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  void mapFromNothing() {
    assertThrows(Mapper.MappingException.class, () -> Mapper.readJson(null, null));
    assertThrows(Mapper.MappingException.class, () -> Mapper.fromJson(null, null));
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
  void mapFromFlatJson() {
    // Given a flat JSON object
    Reader jsonReader = resourceAsReader("Flat.json");
    String json = resourceAsString("Flat.json");

    // When it is mapped
    Flat pojoR = Mapper.readJson(jsonReader, Flat.class);
    Flat pojoF = Mapper.fromJson(json, Flat.class);

    // Then the data should be present in the POJO
    assertFlatDataPresent(pojoR);
    assertFlatDataPresent(pojoF);
  }

  /**
   * Scenario: A JSON object with nested objects should be mapped to a POJO
   *
   * JSON -> POJO
   *   Object -> Map or POJO
   *   Array -> List or Set
   */
  @Test
  void mapFromDeepJson() {
    // Given a deep JSON object
    Reader jsonReader = resourceAsReader("Deep.json");
    String json = resourceAsString("Deep.json");

    // When it is mapped
    Deep pojoR = Mapper.readJson(jsonReader, Deep.class);
    Deep pojoF = Mapper.fromJson(json, Deep.class);

    // Then the data should be present in the POJO
    assertDeepDataPresent(pojoR);
    assertDeepDataPresent(pojoF);
  }

  /**
   * Scenario: A JSON array of objects with nested objects should be mapped to a List of POJOs
   */
  @Test
  @SuppressWarnings("unchecked")
  void mapFromJsonArray() {
    // Given a JSON array with objects
    Reader jsonReader = resourceAsReader("DeepArray.json");
    String json = resourceAsString("DeepArray.json");

    // When it is mapped
    List<Deep> listR = Mapper.readJson(jsonReader, ArrayList.class, Deep.class);
    Set<Deep> setF = Mapper.fromJson(json, LinkedHashSet.class, Deep.class);

    // Then the data should be present in the list elements
    assertDeepDataPresent(listR.get(0));
    assertDeepDataModifiedPresent(listR.get(1));
    List<Deep> listF = new ArrayList<>(setF);
    assertDeepDataPresent(listF.get(0));
    assertDeepDataModifiedPresent(listF.get(1));
  }

  /**
   * Scenario: It should be possible to specify subtypes for mapping
   */
  @Test
  @SuppressWarnings("unchecked")
  void mapFromJsonWithDetailedTyping() {
    String json = "{ \"1\":1.1, \"2\":2.2 }";

    Map<Integer, Float> m = Mapper.fromJson(json, HashMap.class, Integer.class, Float.class);

    assertEquals(1.1F, m.get(1));
    assertEquals(2.2F, m.get(2));
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should search for an function to instantiate
   */
  @Test
  void mapFromJsonWithPolymorphism() {
    String jsonP = resourceAsString("Polymorph.json");
    String jsonA = resourceAsString("Attribute.json");

    Polymorph p = Mapper.fromJson(jsonP, Polymorph.class);
    Polymorph.Attribute a = Mapper.fromJson(jsonA, Polymorph.Attribute.class);

    assertEquals(18, p.getNumber());
    assertEquals("hello", ((Polymorph.Instance) p.getAttr()).getText());
    assertEquals(2, ((Polymorph.Instance) p.getAttr()).getVersion());

    assertEquals("hello", ((Polymorph.Instance) a).getText());
    assertEquals(2, ((Polymorph.Instance) a).getVersion());
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
  void mapFromJsonWithNullAndAbsentProperties() {
    // Given a JSON object with nulls and absent properties
    Reader jsonReader = resourceAsReader("Partial.json");
    String json = resourceAsString("Partial.json");

    // When it is mapped
    Nested pojoR = Mapper.readJson(jsonReader, Nested.class);
    Nested pojoF = Mapper.fromJson(json, Nested.class);

    // Then the data should be present in the POJO
    assertNestedDataPresent(pojoR);
    assertNestedDataPresent(pojoF);

    // And the markers should be set correctly
    assertNestedDataMarkedCorrectly(pojoR);
    assertNestedDataMarkedCorrectly(pojoF);
  }

  /**
   * Scenario: It should be possible to have different names in JSON and the mapped POJO (and Enums)
   */
  @Test
  void mapFromDeepNamedJson() {
    // Given a deep JSON object with java special names
    Reader jsonReaderA = resourceAsReader("DeepNamed.json");
    String jsonA = resourceAsString("DeepNamed.json");
    Reader jsonReaderB = resourceAsReader("Named.json");
    String jsonB = resourceAsString("Named.json");
    Reader jsonReaderC = resourceAsReader("SpecialNamed.json");
    String jsonC = resourceAsString("SpecialNamed.json");

    // When it is mapped
    DeepNamed pojoRA = Mapper.readJson(jsonReaderA, DeepNamed.class);
    DeepNamed pojoFA = Mapper.fromJson(jsonA, DeepNamed.class);
    Named pojoRB = Mapper.readJson(jsonReaderB, Named.class);
    Named pojoFB = Mapper.fromJson(jsonB, Named.class);
    SpecialNamed pojoRC = Mapper.readJson(jsonReaderC, SpecialNamed.class);
    SpecialNamed pojoFC = Mapper.fromJson(jsonC, SpecialNamed.class);

    // Then the data should be present in the POJO
    assertDeepNamedDataPresent(pojoRA);
    assertDeepNamedDataPresent(pojoFA);
    assertEquals(4, pojoRB.getASpecialName());
    assertEquals(5, pojoRB.getBSpecialName());
    assertEquals(6, pojoRB.getMaJa());
    assertEquals(7, pojoRB.getCaJa());
    assertEquals(4, pojoFB.getASpecialName());
    assertEquals(5, pojoFB.getBSpecialName());
    assertEquals(6, pojoFB.getMaJa());
    assertEquals(7, pojoFB.getCaJa());
    assertEquals(3, pojoRC.getXObj());
    assertEquals(4, pojoRC.getYObj());
    assertEquals(3, pojoFC.getXObj());
    assertEquals(4, pojoFC.getYObj());
  }

  /**
   * Scenario: All JSON to POJO conversion errors and problems should be in one report
   */
  @Test
  void mapFromDeepFlawedJsonManaged() {
    // Given a deep JSON object with flawed data
    Reader jsonReader = resourceAsReader("DeepFlawed.json");
    String json = resourceAsString("DeepFlawed.json");

    // When it is mapped
    PropertyIssues propertyIssuesR = PropertyIssues.of();
    PropertyIssues propertyIssuesF = PropertyIssues.of();
    DeepFlawed pojoR = Mapper.readJson(jsonReader, propertyIssuesR::add, DeepFlawed.class);
    DeepFlawed pojoF = Mapper.fromJson(json, propertyIssuesF::add, DeepFlawed.class);

    // Then the convertible data should be present in the POJO
    assertDeepFlawedDataPresent(pojoR);
    assertDeepFlawedDataPresent(pojoF);

    // And the issues should be collected
    assertDeepFlawedPropertyIssuesCollected(propertyIssuesR.toString());
    assertDeepFlawedPropertyIssuesCollected(propertyIssuesF.toString());
  }

  /**
   * Scenario: If no report is requested but conversion errors happen, an exception should contain the report
   */
  @Test
  void mapFromDeepFlawedJson() {
    Reader jsonReader = resourceAsReader("DeepFlawed.json");
    String json = resourceAsString("DeepFlawed.json");

    Mapper.MappingException tR = assertThrows(Mapper.MappingException.class, () -> Mapper.readJson(jsonReader, DeepFlawed.class));
    Mapper.MappingException tF = assertThrows(Mapper.MappingException.class, () -> Mapper.fromJson(json, DeepFlawed.class));

    assertDeepFlawedPropertyIssuesCollected(tR.getMessage());
    assertDeepFlawedPropertyIssuesCollected(tF.getMessage());
  }

  private void assertDeepFlawedDataPresent(DeepFlawed pojo) {
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
  }

  private void assertDeepFlawedPropertyIssuesCollected(String propertyIssues) {
    assertTrue(propertyIssues.contains("stringToObject"));
    assertTrue(propertyIssues.contains("numberToObject"));
    assertTrue(propertyIssues.contains("numberToEnum"));
    assertTrue(propertyIssues.contains("objectToPojo.stringToUUID"));
    assertTrue(propertyIssues.contains("objectToPojo.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectToPojo.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectToPojo.numberArrayToIntegerSet.1"));
    assertTrue(propertyIssues.contains("objectToPojo.booleanToBoolean"));
    assertTrue(propertyIssues.contains("objectToIntMap.string"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.stringToUUID"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.numberArrayToIntegerSet.1"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.0.booleanToBoolean"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.stringToUUID"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.numberArrayToIntegerSet.1"));
    assertTrue(propertyIssues.contains("objectArrayToObjectList.1.booleanToBoolean"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.stringToUUID"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.numberArrayToIntegerSet.1"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.0.booleanToBoolean"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.1.stringToUUID"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.1.numberToBigDecimal"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.1.stringArrayToStringList"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.1.numberArrayToIntegerSet.1"));
    assertTrue(propertyIssues.contains("objectArrayToObjectSet.1.booleanToBoolean"));
  }
}
