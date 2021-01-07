package net.io_0.maja.mapping;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.PropertyIssues;
import net.io_0.maja.mapping.Mapper.Context;
import net.io_0.maja.mapping.Mapper.Instantiator;
import net.io_0.maja.models.*;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.*;

import static net.io_0.maja.TestUtils.*;
import static net.io_0.maja.mapping.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Narrative:
 *   As an YAML to POJO mapper API consumer
 *
 *   I want to convert a YAML object to a POJO
 *   so that further data processing gets easier
 *
 *   and I want to be able to distinguish between absent and null YAML values
 *
 *   and I want a single report containing all that went wrong on errors
 *   so that providing compatible YAML data gets easier
 *
 *   and I want to be able to deal with YAML names
 *   so that I don't get problems with Java naming conventions or enums
 */
@Slf4j
class MapFromYamlTests {
  /**
   * Scenario: Passing problematic data should end in an exception
   */
  @Test
  void mapFromNothing() {
    assertThrows(Mapper.MappingException.class, () -> Mapper.readYaml(null, null));
    assertThrows(Mapper.MappingException.class, () -> Mapper.fromYaml(null, null));
  }

  /**
   * Scenario: A flat YAML object should be mapped to a POJO
   *
   * YAML -> POJO
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
  void mapFromFlatYaml() {
    // Given a flat YAML object
    Reader yamlReader = resourceAsReader("Flat.yaml");
    String yaml = resourceAsString("Flat.yaml");

    // When it is mapped
    Flat pojoR = Mapper.readYaml(yamlReader, Flat.class);
    Flat pojoF = Mapper.fromYaml(yaml, Flat.class);

    // Then the data should be present in the POJO
    assertFlatDataPresent(pojoR);
    assertFlatDataPresent(pojoF);
  }

  /**
   * Scenario: A YAML object with nested objects should be mapped to a POJO
   *
   * YAML -> POJO
   *   Object -> Map or POJO
   *   Array -> List or Set
   */
  @Test
  void mapFromDeepYaml() {
    // Given a deep YAML object
    Reader yamlReader = resourceAsReader("Deep.yaml");
    String yaml = resourceAsString("Deep.yaml");

    // When it is mapped
    Deep pojoR = Mapper.readYaml(yamlReader, Deep.class);
    Deep pojoF = Mapper.fromYaml(yaml, Deep.class);

    // Then the data should be present in the POJO
    assertDeepDataPresent(pojoR);
    assertDeepDataPresent(pojoF);
  }

  /**
   * Scenario: A YAML array of objects with nested objects should be mapped to a List of POJOs
   */
  @Test
  @SuppressWarnings("unchecked")
  void mapFromYamlArray() {
    // Given a YAML array with objects
    Reader yamlReader = resourceAsReader("DeepArray.yaml");
    String yaml = resourceAsString("DeepArray.yaml");

    // When it is mapped
    List<Deep> listR = Mapper.readYaml(yamlReader, ArrayList.class, Deep.class);
    Set<Deep> setF = Mapper.fromYaml(yaml, LinkedHashSet.class, Deep.class);

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
  void mapFromYamlWithDetailedTyping() {
    String yaml = "{ \"1\":1.1, \"2\":2.2 }";

    Map<Integer, Float> m = Mapper.fromYaml(yaml, HashMap.class, Integer.class, Float.class);

    assertEquals(1.1F, m.get(1));
    assertEquals(2.2F, m.get(2));
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should search for a default function to instantiate
   */
  @Test
  void mapFromYamlWithPolymorphismAndDefaultInstantiator() {
    String yamlP = resourceAsString("Polymorph.yaml");
    String yamlA = resourceAsString("Attribute.yaml");

    PolymorphWithDefaultInstantiator p = Mapper.fromYaml(yamlP, PolymorphWithDefaultInstantiator.class);
    PolymorphWithDefaultInstantiator.Attribute a = Mapper.fromYaml(yamlA, PolymorphWithDefaultInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should search for a static function to instantiate
   */
  @Test
  void mapFromYamlWithPolymorphismAndStaticInstantiator() {
    String yamlP = resourceAsString("Polymorph.yaml");
    String yamlA = resourceAsString("Attribute.yaml");

    PolymorphWithStaticInstantiator p = Mapper.fromYaml(yamlP, PolymorphWithStaticInstantiator.class);
    PolymorphWithStaticInstantiator.Attribute a = Mapper.fromYaml(yamlA, PolymorphWithStaticInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }

  /**
   * Scenario: It should be possible to use Java interfaces. Maja should use context instantiator
   */
  @Test
  void mapFromYamlWithPolymorphismAndContextInstantiator() {
    String yamlP = resourceAsString("Polymorph.yaml");
    String yamlA = resourceAsString("Attribute.yaml");

    var ctx = Context.ofInstantiators(Instantiator.of(
      PolymorphWithoutInstantiator.Attribute.class, PolymorphWithoutInstantiator.Instance::instHelper
    ));

    PolymorphWithoutInstantiator p = Mapper.fromYaml(yamlP, ctx, PolymorphWithoutInstantiator.class);
    PolymorphWithoutInstantiator.Attribute a = Mapper.fromYaml(yamlA, ctx, PolymorphWithoutInstantiator.Attribute.class);

    assertPolymorphDataPresent(p);
    assertAttributeDataPresent(a);
  }
  
  /**
   * Scenario: It should be possible to differentiate YAML undefined and null with the conversion result
   *
   * YAML -> POJO
   *   null -> null + marker
   *   absent -> null + no marker
   *   (we don't check the undefined token, it is valid js but not allowed in YAML)
   */
  @Test
  void mapFromYamlWithNullAndAbsentProperties() {
    // Given a YAML object with nulls and absent properties
    Reader yamlReader = resourceAsReader("Partial.yaml");
    String yaml = resourceAsString("Partial.yaml");

    // When it is mapped
    Nested pojoR = Mapper.readYaml(yamlReader, Nested.class);
    Nested pojoF = Mapper.fromYaml(yaml, Nested.class);

    // Then the data should be present in the POJO
    assertNestedDataPresent(pojoR);
    assertNestedDataPresent(pojoF);

    // And the markers should be set correctly
    assertNestedDataMarkedCorrectly(pojoR);
    assertNestedDataMarkedCorrectly(pojoF);
  }

  /**
   * Scenario: It should be possible to have different names in YAML and the mapped POJO (and Enums)
   */
  @Test
  void mapFromDeepNamedYaml() {
    // Given a deep YAML object with java special names
    Reader yamlReaderA = resourceAsReader("DeepNamed.yaml");
    String yamlA = resourceAsString("DeepNamed.yaml");
    Reader yamlReaderB = resourceAsReader("Named.yaml");
    String yamlB = resourceAsString("Named.yaml");
    Reader yamlReaderC = resourceAsReader("SpecialNamed.yaml");
    String yamlC = resourceAsString("SpecialNamed.yaml");

    // When it is mapped
    DeepNamed pojoRA = Mapper.readYaml(yamlReaderA, DeepNamed.class);
    DeepNamed pojoFA = Mapper.fromYaml(yamlA, DeepNamed.class);
    Named pojoRB = Mapper.readYaml(yamlReaderB, Named.class);
    Named pojoFB = Mapper.fromYaml(yamlB, Named.class);
    SpecialNamed pojoRC = Mapper.readYaml(yamlReaderC, SpecialNamed.class);
    SpecialNamed pojoFC = Mapper.fromYaml(yamlC, SpecialNamed.class);

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
   * Scenario: All YAML to POJO conversion errors and problems should be in one report
   */
  @Test
  void mapFromDeepFlawedYamlManaged() {
    // Given a deep YAML object with flawed data
    Reader yamlReader = resourceAsReader("DeepFlawed.yaml");
    String yaml = resourceAsString("DeepFlawed.yaml");

    // When it is mapped
    PropertyIssues propertyIssuesR = PropertyIssues.of();
    PropertyIssues propertyIssuesF = PropertyIssues.of();
    DeepFlawed pojoR = Mapper.readYaml(yamlReader, propertyIssuesR::add, DeepFlawed.class);
    DeepFlawed pojoF = Mapper.fromYaml(yaml, propertyIssuesF::add, DeepFlawed.class);

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
  void mapFromDeepFlawedYaml() {
    Reader yamlReader = resourceAsReader("DeepFlawed.yaml");
    String yaml = resourceAsString("DeepFlawed.yaml");

    Mapper.MappingException tR = assertThrows(Mapper.MappingException.class, () -> Mapper.readYaml(yamlReader, DeepFlawed.class));
    Mapper.MappingException tF = assertThrows(Mapper.MappingException.class, () -> Mapper.fromYaml(yaml, DeepFlawed.class));

    assertDeepFlawedPropertyIssuesCollected(tR.getMessage());
    assertDeepFlawedPropertyIssuesCollected(tF.getMessage());
  }

  /**
   * Scenario: A YAML object property should be mappable to String
   */
  @Test
  void mapFromDeepYamlPartiallyAsString() {
    // Given a deep YAML object
    Reader yamlReader = resourceAsReader("Deep.yaml");
    String yaml = resourceAsString("Deep.yaml");

    // When it is mapped
    DeepNestedString pojoR = Mapper.readYaml(yamlReader, DeepNestedString.class);
    DeepNestedString pojoF = Mapper.fromYaml(yaml, DeepNestedString.class);

    // Then the data should be present in the POJO
    assertDeepNestedStringYamlDataPresent(pojoR);
    assertDeepNestedStringYamlDataPresent(pojoF);
  }

  /**
   * Scenario: A YAML array should be mappable to String
   */
  @Test
  void mapFromDeepArrayYamlToString() {
    // Given a deep YAML object
    Reader yamlReader = resourceAsReader("DeepArray.yaml");
    String yaml = resourceAsString("DeepArray.yaml");

    // When it is mapped
    String stringR = Mapper.readYaml(yamlReader, String.class);
    String stringF = Mapper.fromYaml(yaml, String.class);

    // Then the data should be present in the POJO
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepArray.yaml"), stringR);
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepArray.yaml"), stringF);
  }

  /**
   * Scenario: A YAML object should be mappable to String
   */
  @Test
  void mapObjectYamlToString() {
    // Given a deep YAML object
    Reader yamlReader = resourceAsReader("Deep.yaml");
    String yaml = resourceAsString("DeepNamed.yaml");

    // When it is mapped
    String stringR = Mapper.readYaml(yamlReader, String.class);
    String stringF = Mapper.fromYaml(yaml, String.class);

    // Then the data should be present in the POJO
    assertEqualsIgnoringWhitespaces(resourceAsString("Deep.yaml"), stringR);
    assertEqualsIgnoringWhitespaces(resourceAsString("DeepNamed.yaml"), stringF);
  }
}
