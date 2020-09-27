package net.io_0.maja;

import lombok.extern.slf4j.Slf4j;
import net.io_0.maja.mapping.Mapper;
import net.io_0.maja.models.Person;
import net.io_0.maja.validation.Validator;
import net.io_0.maja.validators.PersonValidator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Narrative:
 *   As a map and validation API consumer
 *
 *   I want mapping and validation to be able to distinguish between absent and null values
 *   so that I can tread null as e.g. delete
 *
 *   and I want a single report containing all that went wrong during mapping and validation
 *   so that providing valid data gets easier
 *
 *   and I want to be able to deal with names
 *   so that I don't get problems with Java naming conventions
 */
@Slf4j
public class MapAndValidateTests {
  @Test
  public void readmeExampleTest() {
    // Given desired results
    Predicate<AtomicReference<String>> check1 = print -> print.get().equals("Maja");
    Predicate<AtomicReference<String>> check2 = print -> print.get().equals("lastName was absent");

    // When readme code is run
    String json = "{ \"first name\": \"Maja\" }";
    PropertyIssues mappingIssues = PropertyIssues.of();

    Person person = Mapper.fromJson(json, mappingIssues::add, Person.class);

    Person validPerson = Validator.of(mappingIssues).and(PersonValidator.instance).ensureValidity(person);

    Property<String> firstName = validPerson.getProperty(Person.FIRST_NAME);
    Property<String> lastName = validPerson.getProperty(Person.LAST_NAME);

    firstName.ifAssigned(valueOrNull -> System.out.println(valueOrNull)); // prints 'Maja'
    lastName.ifUnassigned(() -> System.out.println("lastName was absent")); // prints 'lastName was absent'

    // Then prints should be as documented
    AtomicReference<String> print1 = new AtomicReference<>();
    firstName.ifAssigned(valueOrNull -> print1.set(valueOrNull));
    assertTrue(check1.test(print1));
    AtomicReference<String> print2 = new AtomicReference<>();
    lastName.ifUnassigned(() -> print2.set("lastName was absent"));
    assertTrue(check2.test(print2));
  }
}
