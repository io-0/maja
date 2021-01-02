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
 *   As an POJO to YAML mapper API consumer
 *
 *   I want to convert a POJO to a YAML object
 *   so that I can interact with APIs that require YAML
 *
 *   and I want to be able to distinguish between absent and null YAML values
 *   so that I can call APIs that implement e.g. RFC 7386 - YAML Merge Patch
 *
 *   and I want to be able to deal with YAML names
 *   so that I don't get problems with Java naming conventions or enums
 */
@Slf4j
class MapToYamlTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  void mapToNothing() {
    assertThrows(Mapper.MappingException.class, () -> Mapper.writeYaml(null, null));
  }

  /**
   * Scenario: A flat YAML object should be mapped to a POJO and back
   */
  @Test
  void mapToFlatYaml() {
    // Given a flat YAML object
    Reader yamlReader = resourceAsReader("SimplifiedFlat.yaml");

    // When it is mapped
    Flat pojo = Mapper.readYaml(yamlReader, Flat.class);

    // Then the data should be present in the POJO
    assertFlatDataPresent(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedFlat.yaml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer yamlWriter = new StringWriter();
    Mapper.writeYaml(yamlWriter, pojo);
    String yaml = Mapper.toYaml(pojo);

    assertEquals(reference, yamlWriter.toString());
    assertEquals(reference, yaml);
  }

  /**
   * Scenario: A YAML object with nested objects should be mapped to a POJO and back
   */
  @Test
  void mapToDeepYaml() {
    // Given a deep YAML object
    Reader yamlReader = resourceAsReader("SimplifiedDeep.yaml");

    // When it is mapped
    Deep pojo = Mapper.readYaml(yamlReader, Deep.class);

    // Then the data should be present in the POJO
    assertDeepDataPresent(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedDeep.yaml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer yamlWriter = new StringWriter();
    Mapper.writeYaml(yamlWriter, pojo);
    String yaml = Mapper.toYaml(pojo);

    assertEquals(reference, yamlWriter.toString());
    assertEquals(reference, yaml);
  }

  /**
   * Scenario: It should be possible to map YAML with absent properties and null to POJO and back
   */
  @Test
  void mapToYamlWithNullAndAbsentProperties() {
    // Given a YAML object with nulls and absent properties
    Reader yamlReader = resourceAsReader("SimplifiedPartial.yaml");

    // When it is mapped
    Nested pojo = Mapper.readYaml(yamlReader, Nested.class);

    // Then the data should be present in the POJO
    assertNestedDataPresent(pojo);

    // And the markers should be set correctly
    assertNestedDataMarkedCorrectly(pojo);

    // And mapping back should not loose information
    String reference = new BufferedReader(resourceAsReader("SimplifiedPartial.yaml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer yamlWriter = new StringWriter();
    Mapper.writeYaml(yamlWriter, pojo);
    String yaml = Mapper.toYaml(pojo);

    assertEquals(reference, yamlWriter.toString());
    assertEquals(reference, yaml);
  }

  /**
   * Scenario: It should be possible to have different names in YAML and POJOs (and Enums)
   */
  @Test
  void mapToDeepNamedYaml() {
    // Given a deep YAML object with java special names
    Reader yamlReaderA = resourceAsReader("SimplifiedDeepNamed.yaml");
    Reader yamlReaderB = resourceAsReader("Named.yaml");
    Reader yamlReaderC = resourceAsReader("SpecialNamed.yaml");

    // When it is mapped
    DeepNamed pojoA = Mapper.readYaml(yamlReaderA, DeepNamed.class);
    Named pojoB = Mapper.readYaml(yamlReaderB, Named.class);
    SpecialNamed pojoC = Mapper.readYaml(yamlReaderC, SpecialNamed.class);

    // Then the data should be present in the POJO
    assertDeepNamedDataPresent(pojoA);
    assertEquals(4, pojoB.getASpecialName());
    assertEquals(5, pojoB.getBSpecialName());
    assertEquals(6, pojoB.getMaJa());
    assertEquals(7, pojoB.getCaJa());
    assertEquals(3, pojoC.getXObj());
    assertEquals(4, pojoC.getYObj());

    // And mapping back should not loose information
    String referenceA = new BufferedReader(resourceAsReader("SimplifiedDeepNamed.yaml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer yamlWriterA = new StringWriter();
    Mapper.writeYaml(yamlWriterA, pojoA);
    String yamlA = Mapper.toYaml(pojoA);
    String referenceB = new BufferedReader(resourceAsReader("NamedRef.yaml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer yamlWriterB = new StringWriter();
    Mapper.writeYaml(yamlWriterB, pojoB);
    String yamlB = Mapper.toYaml(pojoB);
    String referenceC = new BufferedReader(resourceAsReader("SpecialNamedRef.yaml")).lines().collect(Collectors.joining(System.lineSeparator()));
    Writer yamlWriterC = new StringWriter();
    Mapper.writeYaml(yamlWriterC, pojoC);
    String yamlC = Mapper.toYaml(pojoC);

    assertEquals(referenceA, yamlWriterA.toString());
    assertEquals(referenceA, yamlA);
    assertEquals(referenceB, yamlWriterB.toString());
    assertEquals(referenceB, yamlB);
    assertEquals(referenceC, yamlWriterC.toString());
    assertEquals(referenceC, yamlC);
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
    String yaml0 = Mapper.toYaml(model0);
    String yaml1 = Mapper.toYaml(model1);
    String yaml2 = Mapper.toYaml(model2);
    String yaml3 = Mapper.toYaml(model3);
    String yaml4 = Mapper.toYaml(model4);

    // Then empty collections should be serialized, null and not set should not
    assertFalse(yaml0.contains("numberArrayToFloatList"));
    assertFalse(yaml0.contains("numberArrayToIntegerSet"));

    assertTrue(yaml1.contains("numberArrayToFloatList: []"));
    assertTrue(yaml1.contains("numberArrayToIntegerSet: []"));

    assertFalse(yaml2.contains("one"));
    assertFalse(yaml2.contains("two"));
    assertFalse(yaml2.contains("three"));

    assertTrue(yaml3.contains("one: null"));
    assertTrue(yaml3.contains("two: null"));
    assertTrue(yaml3.contains("three: null"));

    assertTrue(yaml4.contains("one: []"));
    assertTrue(yaml4.contains("two: []"));
    assertTrue(yaml4.contains("three: {}"));
  }

  /**
   * Scenario: It should be possible to serialize Java interfaces (with instantiate functions)
   */
  @Test
  void serializeWithPolymorphism() {
    // Given a yaml reference and a model
    String referenceP = resourceAsString("PolymorphRef.yaml");
    String referenceA = resourceAsString("AttributeRef.yaml");
    Polymorph p = new Polymorph(18, new Polymorph.Instance("hello", 2));
    Polymorph.Attribute a = new Polymorph.Instance("hello", 2);

    // When serialized
    String yamlP = Mapper.toYaml(p);
    String yamlA = Mapper.toYaml(a);

    // Result should match reference
    assertEquals(referenceP, yamlP);
    assertEquals(referenceA, yamlA);
  }
}
