# Maja

## About
**Ma**ps **Ja**va to JSON Objects and back without information loss.
Provides validators to tighten mapping.

Combining those features enables one to implement e.g. [OpenAPI Specification](https://github.com/OAI/OpenAPI-Specification).

### Information loss?
A property in JSON can have a value (null or otherwise) or be absent/undefined. A property in Java can't be absent/undefined at runtime. There are multiple ways to deal with that in Java. Since Java 8 one could e.g. utilize `Optional` for properties where this difference matters. Maja's approach is to store in a `Set` if a property was set.

Furthermore JSON naming conventions are not as strict as in Java. Maja's approach to deal with this are `@WithUnconventionalName` Annotations.

## Usage
### Import
Use Maven or Gradle, e.g. add repository and dependency to `build.gradle`:
```Gradle
repositories {
  ...
  maven { url "https://jitpack.io" }
  ...
}
```
```Gradle
dependencies {
  ...
  implementation "com.github.io-0:maja:1.0.0"
  ...
}
```
### Define Mapping via Java Classes
We can work with POJOs if the difference between null and absent should be ignored. Otherwise `PropertyBundle` should be implemented and it's marker method should be used in Setters. The following example uses fluent Setters and Lombok Annotations (constants for property names are useful for Validators):
```Java
@NoArgsConstructor
@Getter
@ToString
public class Person extends PropertyBundle {
  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";

  @WithUnconventionalName("first name")
  private String firstName;
  private String lastName;

  public Person setFirstName(String value) {
    this.firstName = value;
    markPropertySet(FIRST_NAME);
    return this;
  }

  public Person setLastName(String value) {
    this.lastName = value;
    markPropertySet(LAST_NAME);
    return this;
  }
}
```
### Validation
To further tighten the mapping validators can be defined:
```Java
public interface PersonValidator {
  Validator<Person> instance = Validator.of(
    PropertyConstraint.on(FIRST_NAME, required, notNull, minLength(2)),
    PropertyConstraint.on(LAST_NAME, notNull)
  );
}
```
For a list of all available validators, see **Built In Property Validators**.
For further information check the Tests.

### Bringing it all together
Mapping can be done from POJO or `PropertyBundle` to JSON `String` or a `Map` and back. An issue collector needs to be supplied.
The collected issues can then be fed into validation, which will throw an Exception on problems (containing them all).
Example:
```Java
String json = "{ \"first name\": \"Maja\" }";
PropertyIssues mappingIssues = PropertyIssues.of();

Person person = Mapper.fromJson(json, Person.class, mappingIssues::add);

Person validPerson = Validator.of(mappingIssues).and(PersonValidator.instance).ensureValidity(person);

Property<String> firstName = validPerson.getProperty(Person.FIRST_NAME);
Property<String> lastName = validPerson.getProperty(Person.LAST_NAME);

firstName.ifAssigned(valueOrNull -> System.out.println(valueOrNull)); // prints 'Maja'
lastName.ifUnassigned(() -> System.out.println("lastName was absent")); // prints 'lastName was absent'
```
For further information check the Tests.

## Built In Property Validators
### All Types
  * notNull
  * required
  * valid (for validator stacking)
  * lazy (for validator stacking)

### String Types
  * pattern
  * passwordFormat
  * binaryFormat
  * byteFormat
  * emailFormat
  * hostnameFormat
  * ipV4Format
  * ipV6Format
  * maxLength
  * minLength

### Integer, Long, Float, Double, BigDecimal Types
  * exclusiveMaximum
  * exclusiveMinimum
  * maximum
  * minimum
  * multipleOf

### Collection and Map Types
  * maxItems
  * minItems
  * each (for validator stacking)

## Property Issue Error Codes
### From  Mapping
  * Weird String Value
  * Instantiation Problem
  * Weird Key
  * Weird Number Value
  * Unexpected Token
  * Missing Instantiator

### From Validation
  * Not Null Violation
  * Required Violation
  * Pattern Violation, %value%
  * Password Format Violation
  * Binary Format Violation
  * Byte Format Violation
  * Email Format Violation
  * Hostname Format Violation
  * IP V4 Format Violation
  * IP V6 Format Violation
  * Max Length Violation, %value%
  * Min Length Violation, %value%
  * Exclusive Maximum Violation, %value%
  * Exclusive Minimum Violation, %value%
  * Maximum Violation, %value%
  * Minimum Violation, %value%
  * Multiple Of Violation, %value%
  * Max Items Violation, %value%
  * Min Items Violation, %value%
