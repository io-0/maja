package net.io_0.maja.models;

import lombok.*;
import net.io_0.maja.PropertyBundle;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter @ToString
public class Validatable extends PropertyBundle {
  public static final String NOT_NULL = "notNull";
  public static final String REQUIRED = "required";
  public static final String PATTERN_STRINGS = "patternStrings";
  public static final String MORE_PATTERN_STRINGS = "morePatternStrings";
  public static final String LENGTH_RESTRICTED_STRINGS = "lengthRestrictedStrings";
  public static final String NUMBERS = "numbers";
  public static final String MORE_NUMBERS = "moreNumbers";
  public static final String EVEN_MORE_NUMBERS = "evenMoreNumbers";
  public static final String NUMBER_LIST = "numberList";
  public static final String NUMBER_SET = "numberSet";
  public static final String ENUM_MAP = "enumMap";
  public static final String POJO = "pojo";
  public static final String SAME_POJO = "samePojo";
  public static final String POJOS = "pojos";
  public static final String POJO_MAP = "pojoMap";

  private String notNull;
  private String required;
  private StringBundle patternStrings;
  private StringBundle morePatternStrings;
  private StringBundle lengthRestrictedStrings;
  private IntegerBundle numbers;
  private FloatBundle moreNumbers;
  private BigDecimalBundle evenMoreNumbers;
  private List<Integer> numberList;
  private Set<Double> numberSet;
  private Map<String, StringEnum> enumMap;
  private Nested pojo;
  private Validatable samePojo;
  private List<Nested> pojos;
  private Map<String, Nested> pojoMap;

  public Validatable setNotNull(String notNull) {
    this.notNull = notNull;
    markPropertySet(NOT_NULL);
    return this;
  }

  public Validatable setRequired(String required) {
    this.required = required;
    markPropertySet(REQUIRED);
    return this;
  }

  public Validatable setPatternStrings(StringBundle patternStrings) {
    this.patternStrings = patternStrings;
    markPropertySet(PATTERN_STRINGS);
    return this;
  }

  public Validatable setMorePatternStrings(StringBundle morePatternStrings) {
    this.morePatternStrings = morePatternStrings;
    markPropertySet(MORE_PATTERN_STRINGS);
    return this;
  }

  public Validatable setLengthRestrictedStrings(StringBundle lengthRestrictedStrings) {
    this.lengthRestrictedStrings = lengthRestrictedStrings;
    markPropertySet(LENGTH_RESTRICTED_STRINGS);
    return this;
  }

  public Validatable setNumbers(IntegerBundle numbers) {
    this.numbers = numbers;
    markPropertySet(NUMBERS);
    return this;
  }

  public Validatable setMoreNumbers(FloatBundle moreNumbers) {
    this.moreNumbers = moreNumbers;
    markPropertySet(MORE_NUMBERS);
    return this;
  }

  public Validatable setEvenMoreNumbers(BigDecimalBundle evenMoreNumbers) {
    this.evenMoreNumbers = evenMoreNumbers;
    markPropertySet(EVEN_MORE_NUMBERS);
    return this;
  }

  public Validatable setNumberList(List<Integer> numberList) {
    this.numberList = numberList;
    markPropertySet(NUMBER_LIST);
    return this;
  }

  public Validatable setNumberSet(Set<Double> numberSet) {
    this.numberSet = numberSet;
    markPropertySet(NUMBER_SET);
    return this;
  }

  public Validatable setEnumMap(Map<String, StringEnum> enumMap) {
    this.enumMap = enumMap;
    markPropertySet(ENUM_MAP);
    return this;
  }

  public Validatable setPojo(Nested pojo) {
    this.pojo = pojo;
    markPropertySet(POJO);
    return this;
  }

  public Validatable setSamePojo(Validatable samePojo) {
    this.samePojo = samePojo;
    markPropertySet(SAME_POJO);
    return this;
  }

  public Validatable setPojos(List<Nested> pojos) {
    this.pojos = pojos;
    markPropertySet(POJOS);
    return this;
  }

  public Validatable setPojoMap(Map<String, Nested> pojoMap) {
    this.pojoMap = pojoMap;
    markPropertySet(POJO_MAP);
    return this;
  }
}
