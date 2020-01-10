# Maja

## About
_Ma_ps _Ja_va to JSON Objects and back without information loss regarding the difference between absent and null values.
Includes validation to tighten mapping.

### Properties
I often implement APIs that use JSON. A property in JSON can have a value (null or otherwise) or be absent/undefined. There are multiple ways to deal with that in Java. A lot of designs just ignore the difference between the value null and absent/undefined which is fine in most cases. Since Java 8 one could utilize java.util.Optional for properties where this difference matters and a lot of designs do this successfully. I however strive for simplicity and in my opinion it is simpler to store a boolean if a property was (re) assigned. This project aims to test how far I can push this concept and if it's viable.

## Built In Property Validators
### All Types
  notNull
  required
  valid (for validator stacking)
  lazy (for validator stacking)

### String Types
  pattern
  passwordFormat
  binaryFormat
  byteFormat
  emailFormat
  hostnameFormat
  ipV4Format
  ipV6Format
  maxLength
  minLength

### Integer, Long, Float, Double, BigDecimal Types
  exclusiveMaximum
  exclusiveMinimum
  maximum
  minimum
  multipleOf

### Collection and Map Types
  maxItems
  minItems
  each (for validator stacking)

## Property Issue Error Codes
### From  Mapping
  Weird String Value
  Instantiation Problem
  Weird Key
  Weird Number Value
  Unexpected Token
  Missing Instantiator

### From Validation
  Not Null Violation
  Required Violation
  Pattern Violation, %value%
  Password Format Violation
  Binary Format Violation
  Byte Format Violation
  Email Format Violation
  Hostname Format Violation
  IP V4 Format Violation
  IP V6 Format Violation
  Max Length Violation, %value%
  Min Length Violation, %value%
  Exclusive Maximum Violation, %value%
  Exclusive Minimum Violation, %value%
  Maximum Violation, %value%
  Minimum Violation, %value%
  Multiple Of Violation, %value%
  Max Items Violation, %value%
  Min Items Violation, %value%
