# Maja

## About
**Ma**ps **Ja**va to JSON Objects and back without information loss regarding the difference between absent and null values.
Includes validation to tighten mapping.

### Properties
I often implement APIs that use JSON. A property in JSON can have a value (null or otherwise) or be absent/undefined. There are multiple ways to deal with that in Java. A lot of designs just ignore the difference between the value null and absent/undefined which is fine in most cases. Since Java 8 one could utilize java.util.Optional for properties where this difference matters and a lot of designs do this successfully. I however strive for simplicity and in my opinion it is simpler to store a boolean if a property was (re) assigned. This project aims to test how far I can push this concept and if it's viable.

## Usage
### Define Mapping via Java Class
We can work with POJOs if the difference between null and absent should be ignored. Otherwise `PropertyBundle` should be implemented and it's marker method should be used in Setters. The following example uses fluent Setters and Lombok annotations (constants for property names are useful for later):
```
@NoArgsConstructor
@Getter @ToString
public class BigDecimalBundle extends PropertyBundle {
  public static final String ONE = "one";
  public static final String TWO = "two";
  public static final String THREE = "three";
  public static final String FOUR = "four";

  private BigDecimal one;
  private BigDecimal two;
  private BigDecimal three;
  private BigDecimal four;

  public BigDecimalBundle setOne(BigDecimal one) {
    this.one = one;
    markPropertySet(ONE);
    return this;
  }

  public BigDecimalBundle setTwo(BigDecimal two) {
    this.two = two;
    markPropertySet(TWO);
    return this;
  }

  public BigDecimalBundle setThree(BigDecimal three) {
    this.three = three;
    markPropertySet(THREE);
    return this;
  }

  public BigDecimalBundle setFour(BigDecimal four) {
    this.four = four;
    markPropertySet(FOUR);
    return this;
  }
}
```
### Validation
To further tighten the mapping validators can be defined:
```
static Validator<Validatable> bundleValidator = Validator.of(
  PropertyConstraint.on(ONE, required, notNull, exclusiveMaximum(4.5f)),
  PropertyConstraint.on(TWO, exclusiveMinimum(4.3f)),
  PropertyConstraint.on(THREE, notNull, maximum(4.4f)),
  PropertyConstraint.on(FOUR, minimum(4.4f))
);
```
For a list of all available validators, see **Built In Property Validators**.
For further information check the Tests.

### Bringing it all together
Mapping can be done from POJO or PropertyBundle to JSON string or a Map and back. An issue collector needs to be supplied. 
The collected issues can then be fed into validation, which will throw one Exception on problems containing them all.
Example:
```
String json = "{ \"one\": 1 }";
PropertyIssues mappingIssues = PropertyIssues.of();

BigDecimalBundle bundle = Mapper.fromJson(json, BigDecimalBundle.class, mappingIssues::add);

BigDecimalBundle validBundle = Validator.of(mappingIssues).and(bundleValidator).ensureValidity(bundle)

Property<Boolean> one = validBundle.getProperty(BigDecimalBundle.ONE);
Property<Boolean> two = validBundle.getProperty(BigDecimalBundle.TWO);

one.ifAssigned(valueOrNull -> System.out.println(valueOrNull)); // will print '1'
two.ifUnassigned(() -> System.out.println("two was absent")); // will print 'two was absent'
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
