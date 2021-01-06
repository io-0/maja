package net.io_0.maja.mapping;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.PropertyIssues;
import net.io_0.maja.mapping.Mapper.Context;
import net.io_0.maja.mapping.Mapper.Instantiator;
import net.io_0.maja.models.*;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.*;

import static net.io_0.maja.TestUtils.resourceAsReader;
import static net.io_0.maja.TestUtils.resourceAsString;
import static net.io_0.maja.mapping.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Narrative:
 *   As an XML to POJO mapper API consumer
 *
 *   I want to convert a XML object to a POJO
 *   so that further data processing gets easier
 *
 *   and I want to be able to distinguish between absent and null XML values
 *
 *   and I want a single report containing all that went wrong on errors
 *   so that providing compatible XML data gets easier
 *
 *   and I want to be able to deal with XML names
 *   so that I don't get problems with Java naming conventions or enums
 */
@Slf4j
class MapFromXmlTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  void mapFromNothing() {
    assertThrows(Mapper.MappingException.class, () -> Mapper.readXml(null, null));
    assertThrows(Mapper.MappingException.class, () -> Mapper.fromXml(null, null));
  }

  /**
   * Scenario: A flat XML object should be mapped to a POJO
   *
   * XML -> POJO
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
  void mapFromFlatXml() {
    // Given a flat XML object
    Reader xmlReader = resourceAsReader("Flat.xml");
    String xml = resourceAsString("Flat.xml");

    // When it is mapped
    Flat pojoR = Mapper.readXml(xmlReader, Flat.class);
    Flat pojoF = Mapper.fromXml(xml, Flat.class);

    // Then the data should be present in the POJO
    assertFlatDataPresent(pojoR);
    assertFlatDataPresent(pojoF);
  }

  /**
   * Scenario: A XML object with nested objects should be mapped to a POJO
   *
   * XML -> POJO
   *   Object -> Map or POJO
   *   Array -> List or Set
   */
  @Test
  void mapFromDeepXml() {
    // Given a deep XML object
    Reader xmlReader = resourceAsReader("Deep.xml");
    String xml = resourceAsString("Deep.xml");

    // When it is mapped
    Deep pojoR = Mapper.readXml(xmlReader, Deep.class);
    Deep pojoF = Mapper.fromXml(xml, Deep.class);

    // Then the data should be present in the POJO
    assertDeepDataPresent(pojoR);
    assertDeepDataPresent(pojoF);
  }

  /**
   * Scenario: A XML array of objects with nested objects should be mapped to a List of POJOs
   */
  @Test
  @SuppressWarnings("unchecked")
  void mapFromXmlArray() {
    // Given a XML array with objects
    Reader xmlReader = resourceAsReader("DeepArray.xml");
    String xml = resourceAsString("DeepArray.xml");

    // When it is mapped
    List<Deep> listR = Mapper.readXml(xmlReader, ArrayList.class, Deep.class);
    Set<Deep> setF = Mapper.fromXml(xml, LinkedHashSet.class, Deep.class);

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
  void mapFromXmlWithDetailedTyping() {
    String xml = "<root><e1>1.1</e1><e2>2.2</e2></root>";

    Map<String, Float> m = Mapper.fromXml(xml, HashMap.class, String.class, Float.class);

    assertEquals(1.1F, m.get("e1"));
    assertEquals(2.2F, m.get("e2"));
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should search for a default function to instantiate
   */
  @Test
  void mapFromXmlWithPolymorphismAndDefaultInstantiator() {
    String xmlP = resourceAsString("Polymorph.xml");
    String xmlA = resourceAsString("Attribute.xml");

    PolymorphWithDefaultInstantiator p = Mapper.fromXml(xmlP, PolymorphWithDefaultInstantiator.class);
    PolymorphWithDefaultInstantiator.Attribute a = Mapper.fromXml(xmlA, PolymorphWithDefaultInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should search for a static function to instantiate
   */
  @Test
  void mapFromXmlWithPolymorphismAndStaticInstantiator() {
    String xmlP = resourceAsString("Polymorph.xml");
    String xmlA = resourceAsString("Attribute.xml");

    PolymorphWithStaticInstantiator p = Mapper.fromXml(xmlP, PolymorphWithStaticInstantiator.class);
    PolymorphWithStaticInstantiator.Attribute a = Mapper.fromXml(xmlA, PolymorphWithStaticInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should use context instantiator
   */
  @Test
  void mapFromXmlWithPolymorphismAndContextInstantiator() {
    String xmlP = resourceAsString("Polymorph.xml");
    String xmlA = resourceAsString("Attribute.xml");

    var ctx = Context.ofInstantiators(Instantiator.of(
      PolymorphWithoutInstantiator.Attribute.class, PolymorphWithoutInstantiator.Instance::instHelper
    ));

    PolymorphWithoutInstantiator p = Mapper.fromXml(xmlP, ctx, PolymorphWithoutInstantiator.class);
    PolymorphWithoutInstantiator.Attribute a = Mapper.fromXml(xmlA, ctx, PolymorphWithoutInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }
  
  /**
   * Scenario: It should be possible to differentiate XML undefined and null with the conversion result
   *
   * XML -> POJO
   *   null -> null + marker
   *   absent -> null + no marker
   *   (we don't check the undefined token, it is valid js but not allowed in XML)
   */
  @Test
  void mapFromXmlWithNullAndAbsentProperties() {
    // Given a XML object with nulls and absent properties
    Reader xmlReader = resourceAsReader("Partial.xml");
    String xml = resourceAsString("Partial.xml");

    // When it is mapped
    Nested pojoR = Mapper.readXml(xmlReader, Nested.class);
    Nested pojoF = Mapper.fromXml(xml, Nested.class);

    // Then the data should be present in the POJO
    assertNestedDataPresent(pojoR);
    assertNestedDataPresent(pojoF);

    // And the markers should be set correctly
    assertNestedDataMarkedCorrectly(pojoR);
    assertNestedDataMarkedCorrectly(pojoF);
  }

  /**
   * Scenario: It should be possible to have different names in XML and the mapped POJO (and Enums)
   */
  @Test
  void mapFromDeepNamedXml() {
    // Given a deep XML object with java special names
    Reader xmlReaderA = resourceAsReader("DeepNamed.xml");
    String xmlA = resourceAsString("DeepNamed.xml");
    Reader xmlReaderB = resourceAsReader("Named.xml");
    String xmlB = resourceAsString("Named.xml");
    Reader xmlReaderC = resourceAsReader("SpecialNamed.xml");
    String xmlC = resourceAsString("SpecialNamed.xml");

    // When it is mapped
    DeepNamed pojoRA = Mapper.readXml(xmlReaderA, DeepNamed.class);
    DeepNamed pojoFA = Mapper.fromXml(xmlA, DeepNamed.class);
    Named pojoRB = Mapper.readXml(xmlReaderB, Named.class);
    Named pojoFB = Mapper.fromXml(xmlB, Named.class);
    SpecialNamed pojoRC = Mapper.readXml(xmlReaderC, SpecialNamed.class);
    SpecialNamed pojoFC = Mapper.fromXml(xmlC, SpecialNamed.class);

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
   * Scenario: All XML to POJO conversion errors and problems should be in one report
   */
  @Test
  void mapFromDeepFlawedXmlManaged() {
    // Given a deep XML object with flawed data
    Reader xmlReader = resourceAsReader("DeepFlawed.xml");
    String xml = resourceAsString("DeepFlawed.xml");

    // When it is mapped
    PropertyIssues propertyIssuesR = PropertyIssues.of();
    PropertyIssues propertyIssuesF = PropertyIssues.of();
    DeepFlawed pojoR = Mapper.readXml(xmlReader, propertyIssuesR::add, DeepFlawed.class);
    DeepFlawed pojoF = Mapper.fromXml(xml, propertyIssuesF::add, DeepFlawed.class);

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
  void mapFromDeepFlawedXml() {
    Reader xmlReader = resourceAsReader("DeepFlawed.xml");
    String xml = resourceAsString("DeepFlawed.xml");

    Mapper.MappingException tR = assertThrows(Mapper.MappingException.class, () -> Mapper.readXml(xmlReader, DeepFlawed.class));
    Mapper.MappingException tF = assertThrows(Mapper.MappingException.class, () -> Mapper.fromXml(xml, DeepFlawed.class));

    assertDeepFlawedPropertyIssuesCollected(tR.getMessage());
    assertDeepFlawedPropertyIssuesCollected(tF.getMessage());
  }

  /**
   * Scenario: A XML object property should be mappable to String
   */
  @Test
  void mapFromDeepXmlPartiallyAsString() {
    // Given a deep XML object
    Reader xmlReader = resourceAsReader("Deep.xml");
    String xml = resourceAsString("Deep.xml");

    // When it is mapped
    DeepNestedString pojoR = Mapper.readXml(xmlReader, DeepNestedString.class);
    DeepNestedString pojoF = Mapper.fromXml(xml, DeepNestedString.class);

    // Then the data should be present in the POJO
    assertDeepNestedStringJsonDataPresent(pojoR);
    assertDeepNestedStringJsonDataPresent(pojoF);
  }

  /**
   * Scenario: A XML array should be mappable to String
   */
  @Test
  void mapFromDeepArrayXmlToString() {
    // Given a deep XML object
    Reader xmlReader = resourceAsReader("DeepArray.xml");
    String xml = resourceAsString("DeepArray.xml");

    // When it is mapped
    String stringR = Mapper.readXml(xmlReader, String.class);
    String stringF = Mapper.fromXml(xml, String.class);

    // Then the data should be present in the POJO
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepArray.xml"), stringR);
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepArray.xml"), stringF);
  }

  /**
   * Scenario: A XML object should be mappable to String
   */
  @Test
  void mapObjectXmlToString() {
    // Given a deep XML object
    Reader xmlReader = resourceAsReader("Deep.xml");
    String xml = resourceAsString("DeepNamed.xml");

    // When it is mapped
    String stringR = Mapper.readXml(xmlReader, String.class);
    String stringF = Mapper.fromXml(xml, String.class);

    // Then the data should be present in the POJO
    assertEqualsIgnoringWhitespaces(resourceAsString("Deep.xml"), stringR);
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepNamed.xml"), stringF);
  }
}
