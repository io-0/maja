package net.io_0.pb;

import lombok.extern.slf4j.Slf4j;
import net.io_0.pb.models.*;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static net.io_0.pb.TestUtils.assertCollectionEquals;
import static net.io_0.pb.TestUtils.loadJsonResource;
import static net.io_0.pb.models.Nested.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class JsonDeserializerTests {
  @Test
  public void deserializeNothing() {
    assertThrows(DeserializationException.class, () -> JsonDeserializer.deserialize(null, null));
  }

  /**
   * Scenario: A flat JSON object should be converted into a POJO
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
  public void deserializeFlatJson() {
    // Given a flat JSON object
    Reader json = loadJsonResource("Flat.json");

    // When it is converted
    Flat pojo = JsonDeserializer.deserialize(json, Flat.class);

    // Then the data should be present in the POJO
    assertNotNull(pojo);
    assertEquals("str", pojo.getStringToString());
    assertEquals(StringEnum.STR2, pojo.getStringToEnum());
    assertEquals(BigDecimal.valueOf(42), pojo.getStringToBigDecimal());
    assertEquals(20.1f, pojo.getStringToFloat(), 0.01);
    assertEquals(220.1d, pojo.getStringToDouble(), 0.01);
    assertEquals(5, pojo.getStringToInteger());
    assertEquals(2147483648L, pojo.getStringToLong());
    assertEquals(LocalDate.of(2019, 7, 23), pojo.getStringToLocalDate());
    assertEquals(OffsetDateTime.of(2010, 1, 1, 10, 0, 10, 0, ZoneOffset.ofHours(1)), pojo.getStringToOffsetDateTime());
    assertEquals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"), pojo.getStringToUUID());
    assertEquals(URI.create("http://www.example.com/eula"), pojo.getStringToURI());
    assertEquals("test", new String(pojo.getStringToByteArray()));
    assertEquals(new File("/path/filename.ext"), pojo.getStringToFile());
    assertEquals(BigDecimal.valueOf(43), pojo.getNumberToBigDecimal());
    assertEquals(21.1f, pojo.getNumberToFloat(), 0.01);
    assertEquals(221.1d, pojo.getNumberToDouble(), 0.01);
    assertEquals(6, pojo.getNumberToInteger());
    assertEquals(2147483649L, pojo.getNumberToLong());
    assertEquals("9001", pojo.getNumberToString());
    assertCollectionEquals(List.of("a", "b", "b", "a"), pojo.getStringArrayToStringList());
    assertCollectionEquals(Set.of(OffsetDateTime.of(2010, 1, 1, 10, 0, 10, 0, ZoneOffset.ofHours(2)),
      OffsetDateTime.of(2010, 1, 1, 10, 0, 10, 0, ZoneOffset.ofHours(3))), pojo.getStringArrayToOffsetDateTimeSet());
    assertCollectionEquals(List.of(1f, 2.3f, 4f, 3.2f, 1f), pojo.getNumberArrayToFloatList());
    assertCollectionEquals(Set.of(0, 1, 2, 3), pojo.getNumberArrayToIntegerSet());
    assertEquals(true, pojo.getBooleanToBoolean());
    assertEquals("false", pojo.getBooleanToString());
  }

  /**
   * Scenario: A JSON object with nested objects should be converted into a POJO
   *
   * JSON -> POJO
   *   Object -> Map or POJO
   *   Array -> List or Set
   */
  @Test
  public void deserializeDeepJson() {
    // Given a deep JSON object
    Reader json = loadJsonResource("Deep.json");

    // When it is converted
    Deep pojo = JsonDeserializer.deserialize(json, Deep.class);

    // Then the data should be present in the POJO
    assertNotNull(pojo);

    assertNotNull(pojo.getObjectToPojo());
    assertEquals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa1"), pojo.getObjectToPojo().getStringToUUID());
    assertEquals(BigDecimal.ONE, pojo.getObjectToPojo().getNumberToBigDecimal());
    assertCollectionEquals(List.of("a", "a", "a", "a"), pojo.getObjectToPojo().getStringArrayToStringList());
    assertCollectionEquals(Set.of(0, 1, 2, 3), pojo.getObjectToPojo().getNumberArrayToIntegerSet());
    assertEquals(true, pojo.getObjectToPojo().getBooleanToBoolean());

    assertNotNull(pojo.getObjectToMap());
    assertTrue(pojo.getObjectToMap().containsKey("stringToUUID"));
    assertTrue(pojo.getObjectToMap().containsKey("numberToBigDecimal"));
    assertTrue(pojo.getObjectToMap().containsKey("stringArrayToStringList"));
    assertTrue(pojo.getObjectToMap().containsKey("numberArrayToIntegerSet"));
    assertTrue(pojo.getObjectToMap().containsKey("booleanToBoolean"));

    assertNotNull(pojo.getObjectArrayToObjectList());
    assertEquals(2, pojo.getObjectArrayToObjectList().size());
    assertEquals("Nested(stringToUUID=3fa85f64-5717-4562-b3fc-2c963f66afa3, numberToBigDecimal=3, " +
      "stringArrayToStringList=[a, a, b, b], numberArrayToIntegerSet=[0, 1, 2, 3], booleanToBoolean=false)",
      pojo.getObjectArrayToObjectList().get(0).toString());
    assertEquals("Nested(stringToUUID=3fa85f64-5717-4562-b3fc-2c963f66afa4, numberToBigDecimal=4, " +
      "stringArrayToStringList=[a, b, b, b], numberArrayToIntegerSet=[0, 1, 2, 3], booleanToBoolean=true)",
      pojo.getObjectArrayToObjectList().get(1).toString());

    assertNotNull(pojo.getObjectArrayToObjectSet());
    assertEquals(1, pojo.getObjectArrayToObjectSet().size());
    assertEquals("Nested(stringToUUID=3fa85f64-5717-4562-b3fc-2c963f66afa6, numberToBigDecimal=43, " +
      "stringArrayToStringList=[a, b, b, a], numberArrayToIntegerSet=[0, 1, 2, 3], booleanToBoolean=true)",
      pojo.getObjectArrayToObjectSet().toArray()[0].toString());
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
  public void deserializeJsonWithNullAndAbsentProperties() {
    // Given a JSON object with nulls and absent properties
    Reader json = loadJsonResource("Partial.json");

    // When it is converted
    Nested pojo = JsonDeserializer.deserialize(json, Nested.class);

    // Then the data should be present in the POJO
    assertNotNull(pojo);
    assertNull(pojo.getStringToUUID());
    assertEquals(BigDecimal.valueOf(4), pojo.getNumberToBigDecimal());
    assertNull(pojo.getStringArrayToStringList());
    assertNull(pojo.getNumberArrayToIntegerSet());
    assertEquals(true, pojo.getBooleanToBoolean());

    // And the markers should be set correctly
    assertTrue(pojo.isPropertySet(STRING_TO_UUID));
    assertTrue(pojo.isPropertySet(NUMBER_TO_BIG_DECIMAL));
    assertFalse(pojo.isPropertySet(STRING_ARRAY_TO_STRING_LIST));
    assertTrue(pojo.isPropertySet(NUMBER_ARRAY_TO_INTEGER_SET));
    assertTrue(pojo.isPropertySet(BOOLEAN_TO_BOOLEAN));
  }

  /**
   * Scenario: It should be possible to have different names in JSON and the converted POJO (and Enums)
   */
  @Test
  public void deserializeDeepNamedJson() {
    // Given a deep JSON object with java special names
    Reader json = loadJsonResource("DeepNamed.json");

    // When it is converted
    DeepNamed pojo = JsonDeserializer.deserialize(json, DeepNamed.class);

    // Then the data should be present in the POJO
    assertNotNull(pojo);

    assertNotNull(pojo.getObjectToPojo());
    assertEquals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa1"), pojo.getObjectToPojo().getStringToUUID());
    assertEquals(BigDecimal.ONE, pojo.getObjectToPojo().getNumberToBigDecimal());
    assertCollectionEquals(List.of("a", "a", "a", "a"), pojo.getObjectToPojo().getStringArrayToStringList());
    assertCollectionEquals(Set.of(0, 1, 2, 3), pojo.getObjectToPojo().getNumberArrayToIntegerSet());
    assertEquals(true, pojo.getObjectToPojo().getBooleanToBoolean());

    assertNotNull(pojo.getObjectArrayToObjectSet());
    assertEquals(1, pojo.getObjectArrayToObjectSet().size());
    assertEquals("Nested(stringToUUID=3fa85f64-5717-4562-b3fc-2c963f66afa6, numberToBigDecimal=43, " +
        "stringArrayToStringList=[a, b, b, a], numberArrayToIntegerSet=[0, 1, 2, 3], booleanToBoolean=true)",
      pojo.getObjectArrayToObjectSet().toArray()[0].toString());

    assertCollectionEquals(List.of(StringEnumNamed.STR1, StringEnumNamed.STR2, StringEnumNamed.STR3), pojo.getStringArrayToEnumList());
  }

  /**
   * Scenario: All JSON to POJO conversion errors and problems should be in one report
   */
  @Test
  public void deserializeDeepFlawedJson() {
    // Given a deep JSON object with flawed data
    Reader json = loadJsonResource("DeepFlawed.json");

    // When it is converted
    PropertyIssues propertyIssues = PropertyIssues.of();
    DeepFlawed pojo = JsonDeserializer.deserialize(json, DeepFlawed.class, propertyIssues::add);

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
    assertEquals("[" +
      "PropertyIssue(propertyName=stringToObject, issue=com.fasterxml.jackson.databind.deser.std.StdValueInstantiator, no String-argument constructor/factory method to deserialize from String value ('42')), " +
      "PropertyIssue(propertyName=numberToObject, issue=42, Cannot construct instance of `net.io_0.pb.models.DeepFlawed$PublicInner`, problem: `java.lang.IllegalStateException` at [Source: (InputStreamReader); line: 3, column: 21]), " +
      "PropertyIssue(propertyName=numberToEnum, issue=9, index value outside legal index range [0..2]), " +
      "PropertyIssue(propertyName=objectToPojo.stringToUUID, issue=no uuid, UUID has to be represented by standard 36-char representation), " +
      "PropertyIssue(propertyName=objectToPojo.numberToBigDecimal, issue=string, not a valid representation), " +
      "PropertyIssue(propertyName=objectToPojo.stringArrayToStringList, issue=VALUE_NUMBER_INT, null), " +
      "PropertyIssue(propertyName=objectToPojo.numberArrayToIntegerSet.1, issue=string, not a valid Integer value), " +
      "PropertyIssue(propertyName=objectToPojo.booleanToBoolean, issue=string, only \"true\" or \"false\" recognized), " +
      "PropertyIssue(propertyName=objectToIntMap.string, issue=string, not a valid representation, problem: (java.lang.NumberFormatException) For input string: \"string\"), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.0.stringToUUID, issue=no uuid, UUID has to be represented by standard 36-char representation), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.0.numberToBigDecimal, issue=string, not a valid representation), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.0.stringArrayToStringList, issue=VALUE_NUMBER_INT, null), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.0.numberArrayToIntegerSet.1, issue=string, not a valid Integer value), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.0.booleanToBoolean, issue=string, only \"true\" or \"false\" recognized), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.1.stringToUUID, issue=no uuid, UUID has to be represented by standard 36-char representation), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.1.numberToBigDecimal, issue=string, not a valid representation), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.1.stringArrayToStringList, issue=VALUE_NUMBER_INT, null), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.1.numberArrayToIntegerSet.1, issue=string, not a valid Integer value), " +
      "PropertyIssue(propertyName=objectArrayToObjectList.1.booleanToBoolean, issue=string, only \"true\" or \"false\" recognized), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.0.stringToUUID, issue=no uuid, UUID has to be represented by standard 36-char representation), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.0.numberToBigDecimal, issue=string, not a valid representation), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.0.stringArrayToStringList, issue=VALUE_NUMBER_INT, null), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.0.numberArrayToIntegerSet.1, issue=string, not a valid Integer value), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.0.booleanToBoolean, issue=string, only \"true\" or \"false\" recognized), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.1.stringToUUID, issue=no uuid, UUID has to be represented by standard 36-char representation), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.1.numberToBigDecimal, issue=string, not a valid representation), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.1.stringArrayToStringList, issue=VALUE_NUMBER_INT, null), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.1.numberArrayToIntegerSet.1, issue=string, not a valid Integer value), " +
      "PropertyIssue(propertyName=objectArrayToObjectSet.1.booleanToBoolean, issue=string, only \"true\" or \"false\" recognized)]",
      propertyIssues.toString().replaceAll("@[\\w]+", ""));
  }
}
