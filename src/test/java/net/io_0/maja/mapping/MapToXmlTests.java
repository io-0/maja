package net.io_0.maja.mapping;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.models.*;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
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
 *   As an POJO to XML mapper API consumer
 *
 *   I want to convert a POJO to a XML object
 *   so that I can interact with APIs that require XML
 *
 *   and I want to be able to distinguish between absent and null XML values
 *
 *   and I want to be able to deal with XML names
 *   so that I don't get problems with Java naming conventions or enums
 */
@Slf4j
class MapToXmlTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  void mapToNothing() {
    assertThrows(Mapper.MappingException.class, () -> Mapper.writeXml(null, null));
  }

  /**
   * Scenario: A flat XML object should be mapped to a POJO and back
   */
  @Test
  void mapToFlatXml() {
    // Given a flat XML object
    Reader xmlReader = resourceAsReader("SimplifiedFlat.xml");

    // When it is mapped
    Flat pojo = Mapper.readXml(xmlReader, Flat.class);

    // Then the data should be present in the POJO
    assertFlatDataPresent(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedFlat.xml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer xmlWriter = new StringWriter();
    Mapper.writeXml(xmlWriter, pojo);
    String xml = Mapper.toXml(pojo);

    assertEquals(reference, xmlWriter.toString());
    assertEquals(reference, xml);
  }

  /**
   * Scenario: A XML object with nested objects should be mapped to a POJO and back
   */
  @Test
  void mapToDeepXml() {
    // Given a deep XML object
    Reader xmlReader = resourceAsReader("SimplifiedDeep.xml");

    // When it is mapped
    Deep pojo = Mapper.readXml(xmlReader, Deep.class);

    // Then the data should be present in the POJO
    assertDeepDataPresent(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedDeep.xml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer xmlWriter = new StringWriter();
    Mapper.writeXml(xmlWriter, pojo);
    String xml = Mapper.toXml(pojo);

    assertEquals(reference, xmlWriter.toString());
    assertEquals(reference, xml);
  }

  /**
   * Scenario: It should be possible to map XML with absent properties and null to POJO and back
   */
  @Test
  void mapToXmlWithNullAndAbsentProperties() {
    // Given a XML object with nulls and absent properties
    Reader xmlReader = resourceAsReader("SimplifiedPartial.xml");

    // When it is mapped
    Nested pojo = Mapper.readXml(xmlReader, Nested.class);

    // Then the data should be present in the POJO
    assertNestedDataPresent(pojo);

    // And the markers should be set correctly
    assertNestedDataMarkedCorrectly(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedPartial.xml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer xmlWriter = new StringWriter();
    Mapper.writeXml(xmlWriter, pojo);
    String xml = Mapper.toXml(pojo);

    assertEquals(reference, xmlWriter.toString());
    assertEquals(reference, xml);
  }

  /**
   * Scenario: It should be possible to have different names in XML and POJOs (and Enums)
   */
  @Test
  void mapToDeepNamedXml() {
    // Given a deep XML object with java special names
    Reader xmlReaderA = resourceAsReader("SimplifiedDeepNamed.xml");
    Reader xmlReaderB = resourceAsReader("Named.xml");
    Reader xmlReaderC = resourceAsReader("SpecialNamed.xml");

    // When it is mapped
    DeepNamed pojoA = Mapper.readXml(xmlReaderA, DeepNamed.class);
    Named pojoB = Mapper.readXml(xmlReaderB, Named.class);
    SpecialNamed pojoC = Mapper.readXml(xmlReaderC, SpecialNamed.class);

    // Then the data should be present in the POJO
    assertDeepNamedDataPresent(pojoA);
    assertEquals(4, pojoB.getASpecialName());
    assertEquals(5, pojoB.getBSpecialName());
    assertEquals(6, pojoB.getMaJa());
    assertEquals(7, pojoB.getCaJa());
    assertEquals(3, pojoC.getXObj());
    assertEquals(4, pojoC.getYObj());

    // And mapping back should not loose information
    String referenceA = new BufferedReader(resourceAsReader("SimplifiedDeepNamed.xml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer xmlWriterA = new StringWriter();
    Mapper.writeXml(xmlWriterA, pojoA);
    String xmlA = Mapper.toXml(pojoA);
    String referenceB = new BufferedReader(resourceAsReader("NamedRef.xml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer xmlWriterB = new StringWriter();
    Mapper.writeXml(xmlWriterB, pojoB);
    String xmlB = Mapper.toXml(pojoB);
    String referenceC = new BufferedReader(resourceAsReader("SpecialNamedRef.xml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer xmlWriterC = new StringWriter();
    Mapper.writeXml(xmlWriterC, pojoC);
    String xmlC = Mapper.toXml(pojoC);

    assertEquals(referenceA, xmlWriterA.toString());
    assertEquals(referenceA, xmlA);
    assertEquals(referenceB, xmlWriterB.toString());
    assertEquals(referenceB, xmlB);
    assertEquals(referenceC, xmlWriterC.toString());
    assertEquals(referenceC, xmlC);
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
    String xml0 = Mapper.toXml(model0);
    String xml1 = Mapper.toXml(model1);
    String xml2 = Mapper.toXml(model2);
    String xml3 = Mapper.toXml(model3);
    String xml4 = Mapper.toXml(model4);

    // Then empty collections should be serialized, null and not set should not
    assertFalse(xml0.contains("numberArrayToFloatList"));
    assertFalse(xml0.contains("numberArrayToIntegerSet"));

    assertTrue(xml1.contains("numberArrayToFloatList: []"));
    assertTrue(xml1.contains("numberArrayToIntegerSet: []"));

    assertFalse(xml2.contains("one"));
    assertFalse(xml2.contains("two"));
    assertFalse(xml2.contains("three"));

    assertTrue(xml3.contains("one: null"));
    assertTrue(xml3.contains("two: null"));
    assertTrue(xml3.contains("three: null"));

    assertTrue(xml4.contains("one: []"));
    assertTrue(xml4.contains("two: []"));
    assertTrue(xml4.contains("three: {}"));
  }

  /**
   * Scenario: It should be possible to serialize Java interfaces (with default instantiate functions)
   */
  @Test
  void serializeWithPolymorphismWithDefaultInstantiator() {
    // Given a xml reference and a model
    String referenceP = resourceAsString("PolymorphRef.xml");
    String referenceA = resourceAsString("AttributeRef.xml");
    PolymorphWithDefaultInstantiator p = new PolymorphWithDefaultInstantiator(18, new PolymorphWithDefaultInstantiator.Instance("hello", 2));
    PolymorphWithDefaultInstantiator.Attribute a = new PolymorphWithDefaultInstantiator.Instance("hello", 2);

    // When serialized
    String xmlP = Mapper.toXml(p);
    String xmlA = Mapper.toXml(a);

    // Result should match reference
    assertEquals(referenceP, xmlP);
    assertEquals(referenceA, xmlA);
  }

  /**
   * Scenario: It should be possible to serialize Java interfaces (with static instantiate functions)
   */
  @Test
  void serializeWithPolymorphismWithStaticInstantiator() {
    // Given a xml reference and a model
    String referenceP = resourceAsString("PolymorphRef.xml");
    String referenceA = resourceAsString("AttributeRef.xml");
    PolymorphWithStaticInstantiator p = new PolymorphWithStaticInstantiator(18, new PolymorphWithStaticInstantiator.Instance("hello", 2));
    PolymorphWithStaticInstantiator.Attribute a = new PolymorphWithStaticInstantiator.Instance("hello", 2);

    // When serialized
    String xmlP = Mapper.toXml(p);
    String xmlA = Mapper.toXml(a);

    // Result should match reference
    assertEquals(referenceP, xmlP);
    assertEquals(referenceA, xmlA);
  }

  /**
   * Scenario: It should be possible to serialize Java interfaces (without instantiate functions)
   */
  @Test
  void serializeWithPolymorphismWithoutInstantiator() {
    // Given a xml reference and a model
    String referenceP = resourceAsString("PolymorphRef.xml");
    String referenceA = resourceAsString("AttributeRef.xml");
    PolymorphWithoutInstantiator p = new PolymorphWithoutInstantiator(18, new PolymorphWithoutInstantiator.Instance("hello", 2));
    PolymorphWithoutInstantiator.Attribute a = new PolymorphWithoutInstantiator.Instance("hello", 2);

    // When serialized
    String xmlP = Mapper.toXml(p);
    String xmlA = Mapper.toXml(a);

    // Result should match reference
    assertEquals(referenceP, xmlP);
    assertEquals(referenceA, xmlA);
  }
}
