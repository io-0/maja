package net.io_0.maja;

import net.io_0.maja.mapping.Mapper;
import net.io_0.maja.models.Person;
import net.io_0.maja.validation.Validator;
import net.io_0.maja.validators.PersonValidator;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadmeExampleTest {
  @Test
  public void exampleTest() {
    // Given
    AtomicReference<String> print1 = new AtomicReference<>();
    AtomicReference<String> print2 = new AtomicReference<>();

    // When
    String json = "{ \"first name\": \"Maja\" }";
    PropertyIssues mappingIssues = PropertyIssues.of();

    Person person = Mapper.fromJson(json, Person.class, mappingIssues::add);

    Person validPerson = Validator.of(mappingIssues).and(PersonValidator.instance).ensureValidity(person);

    Property<String> firstName = validPerson.getProperty(Person.FIRST_NAME);
    Property<String> lastName = validPerson.getProperty(Person.LAST_NAME);

    firstName.ifAssigned(valueOrNull -> System.out.println(valueOrNull)); // prints 'Maja'
    lastName.ifUnassigned(() -> System.out.println("lastName was absent")); // prints 'lastName was absent'

    // Then
    firstName.ifAssigned(valueOrNull -> print1.set(valueOrNull));
    lastName.ifUnassigned(() -> print2.set("lastName was absent"));
    assertEquals("Maja", print1.get());
    assertEquals("lastName was absent", print2.get());
  }
}
