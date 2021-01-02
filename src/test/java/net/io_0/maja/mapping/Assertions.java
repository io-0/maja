package net.io_0.maja.mapping;

import net.io_0.maja.models.*;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.io_0.maja.TestUtils.assertCollectionEquals;
import static net.io_0.maja.TestUtils.resourceAsString;
import static net.io_0.maja.models.Nested.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.StringUtils.replaceWhitespaceCharacters;

public interface Assertions {
  static void assertFlatDataPresent(Flat pojo) {
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

  static void assertDeepDataPresent(Deep pojo) {
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
    assertTrue(pojo.getObjectToMap().containsKey("booleanToBoolean") || pojo.getObjectToMap().containsKey("bool"));

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

    static void assertDeepDataModifiedPresent(Deep pojo) {
    assertNotNull(pojo);

    assertNotNull(pojo.getObjectToPojo());
    assertEquals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afb1"), pojo.getObjectToPojo().getStringToUUID());
    assertEquals(BigDecimal.TEN, pojo.getObjectToPojo().getNumberToBigDecimal());
    assertCollectionEquals(List.of("a", "a", "a", "a"), pojo.getObjectToPojo().getStringArrayToStringList());
    assertCollectionEquals(Set.of(0, 1, 2, 3), pojo.getObjectToPojo().getNumberArrayToIntegerSet());
    assertEquals(true, pojo.getObjectToPojo().getBooleanToBoolean());

    assertNotNull(pojo.getObjectToMap());
    assertTrue(pojo.getObjectToMap().containsKey("stringToUUID"));
    assertTrue(pojo.getObjectToMap().containsKey("numberToBigDecimal"));
    assertTrue(pojo.getObjectToMap().containsKey("stringArrayToStringList"));
    assertTrue(pojo.getObjectToMap().containsKey("numberArrayToIntegerSet"));
    assertTrue(pojo.getObjectToMap().containsKey("booleanToBoolean") || pojo.getObjectToMap().containsKey("bool"));

    assertNotNull(pojo.getObjectArrayToObjectList());
    assertEquals(2, pojo.getObjectArrayToObjectList().size());
    assertEquals("Nested(stringToUUID=3fa85f64-5717-4562-b3fc-2c963f66afb3, numberToBigDecimal=30, " +
      "stringArrayToStringList=[a, a, b, b], numberArrayToIntegerSet=[0, 1, 2, 3], booleanToBoolean=false)",
      pojo.getObjectArrayToObjectList().get(0).toString());
    assertEquals("Nested(stringToUUID=3fa85f64-5717-4562-b3fc-2c963f66afb4, numberToBigDecimal=40, " +
      "stringArrayToStringList=[a, b, b, b], numberArrayToIntegerSet=[0, 1, 2, 3], booleanToBoolean=true)",
      pojo.getObjectArrayToObjectList().get(1).toString());

    assertNotNull(pojo.getObjectArrayToObjectSet());
    assertEquals(1, pojo.getObjectArrayToObjectSet().size());
    assertEquals("Nested(stringToUUID=3fa85f64-5717-4562-b3fc-2c963f66afb6, numberToBigDecimal=430, " +
      "stringArrayToStringList=[a, b, b, a], numberArrayToIntegerSet=[0, 1, 2, 3], booleanToBoolean=true)",
      pojo.getObjectArrayToObjectSet().toArray()[0].toString());
  }

  static void assertNestedDataPresent(Nested pojo) {
    assertNotNull(pojo);
    assertNull(pojo.getStringToUUID());
    assertEquals(BigDecimal.valueOf(4), pojo.getNumberToBigDecimal());
    assertNull(pojo.getStringArrayToStringList());
    assertNull(pojo.getNumberArrayToIntegerSet());
    assertEquals(true, pojo.getBooleanToBoolean());
  }

  static void assertNestedDataMarkedCorrectly(Nested pojo) {
    assertTrue(pojo.isPropertySet(STRING_TO_UUID));
    assertTrue(pojo.isPropertySet(NUMBER_TO_BIG_DECIMAL));
    assertFalse(pojo.isPropertySet(STRING_ARRAY_TO_STRING_LIST));
    assertTrue(pojo.isPropertySet(NUMBER_ARRAY_TO_INTEGER_SET));
    assertTrue(pojo.isPropertySet(BOOLEAN_TO_BOOLEAN));
  }

  static void assertDeepNamedDataPresent(DeepNamed pojo) {
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

  static void assertEqualsIgnoringWhitespaces(String expected, String actual) {
    assertEquals(
      replaceWhitespaceCharacters(expected, ""),
      replaceWhitespaceCharacters(actual, "")
    );
  }

  static void assertDeepNestedStringJsonDataPresent(DeepNestedString pojo) {
    assertNotNull(pojo);
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepObjectToPojoPart.json"), pojo.getObjectToPojo());

    assertNotNull(pojo.getObjectToMap());
    assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa2", pojo.getObjectToMap().get("stringToUUID"));
    assertEquals("2", pojo.getObjectToMap().get("numberToBigDecimal"));
    assertEquals("[\"a\",\"a\",\"a\",\"b\"]", pojo.getObjectToMap().get("stringArrayToStringList"));
    assertEquals("[0,1,2,3,2,1,1]", pojo.getObjectToMap().get("numberArrayToIntegerSet"));
    assertEquals("false", pojo.getObjectToMap().get("booleanToBoolean"));

    assertNotNull(pojo.getObjectArrayToObjectList());
    assertEquals(2, pojo.getObjectArrayToObjectList().size());
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepObjectArrayToObjectListFirstPart.json"),
      pojo.getObjectArrayToObjectList().get(0));
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepObjectArrayToObjectListSecondPart.json"),
      pojo.getObjectArrayToObjectList().get(1));

    assertNotNull(pojo.getObjectArrayToObjectSet());
    assertEquals(1, pojo.getObjectArrayToObjectSet().size());
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepObjectArrayToObjectSetFirstPart.json"),
      pojo.getObjectArrayToObjectSet().toArray()[0].toString());
  }

  static void assertDeepNestedStringYamlDataPresent(DeepNestedString pojo) {
    assertNotNull(pojo);
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepObjectToPojoPart.yaml"), pojo.getObjectToPojo());

    assertNotNull(pojo.getObjectToMap());
    assertEquals("3fa85f64-5717-4562-b3fc-2c963f66afa2", pojo.getObjectToMap().get("stringToUUID"));
    assertEquals("2", pojo.getObjectToMap().get("numberToBigDecimal"));
    assertEquals("[a,a,a,b]", replaceWhitespaceCharacters(pojo.getObjectToMap().get("stringArrayToStringList"), ""));
    assertEquals("[0,1,2,3,2,1,1]", replaceWhitespaceCharacters(pojo.getObjectToMap().get("numberArrayToIntegerSet"), ""));
    assertEquals("false", pojo.getObjectToMap().get("booleanToBoolean"));

    assertNotNull(pojo.getObjectArrayToObjectList());
    assertEquals(2, pojo.getObjectArrayToObjectList().size());
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepObjectArrayToObjectListFirstPart.yaml"),
      pojo.getObjectArrayToObjectList().get(0));
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepObjectArrayToObjectListSecondPart.yaml"),
      pojo.getObjectArrayToObjectList().get(1));

    assertNotNull(pojo.getObjectArrayToObjectSet());
    assertEquals(1, pojo.getObjectArrayToObjectSet().size());
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepObjectArrayToObjectSetFirstPart.yaml"),
      pojo.getObjectArrayToObjectSet().toArray()[0].toString());
  }

  static void assertDeepFlawedDataPresent(DeepFlawed pojo) {
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

  static void assertDeepFlawedPropertyIssuesCollected(String propertyIssues) {
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
