package net.io_0.maja.models;

import lombok.*;
import net.io_0.maja.WithUnconventionalName;
import net.io_0.maja.SetPropertiesAware;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor @Getter
@ToString @EqualsAndHashCode(callSuper = false)
public class Nested extends SetPropertiesAware {
  public static final String STRING_TO_UUID = "stringToUUID";
  public static final String NUMBER_TO_BIG_DECIMAL = "numberToBigDecimal";
  public static final String STRING_ARRAY_TO_STRING_LIST = "stringArrayToStringList";
  public static final String NUMBER_ARRAY_TO_INTEGER_SET = "numberArrayToIntegerSet";
  public static final String BOOLEAN_TO_BOOLEAN = "booleanToBoolean";

  private UUID stringToUUID;
  private BigDecimal numberToBigDecimal;
  private List<String> stringArrayToStringList;
  private Set<Integer> numberArrayToIntegerSet;
  @WithUnconventionalName("bool")  private Boolean booleanToBoolean;

  public Nested setStringToUUID(UUID stringToUUID) {
    this.stringToUUID = stringToUUID;
    markPropertySet(STRING_TO_UUID);
    return this;
  }

  public Nested setNumberToBigDecimal(BigDecimal numberToBigDecimal) {
    this.numberToBigDecimal = numberToBigDecimal;
    markPropertySet(NUMBER_TO_BIG_DECIMAL);
    return this;
  }

  public Nested setStringArrayToStringList(List<String> stringArrayToStringList) {
    this.stringArrayToStringList = stringArrayToStringList;
    markPropertySet(STRING_ARRAY_TO_STRING_LIST);
    return this;
  }

  public Nested setNumberArrayToIntegerSet(Set<Integer> numberArrayToIntegerSet) {
    this.numberArrayToIntegerSet = numberArrayToIntegerSet;
    markPropertySet(NUMBER_ARRAY_TO_INTEGER_SET);
    return this;
  }

  public Nested setBooleanToBoolean(Boolean booleanToBoolean) {
    this.booleanToBoolean = booleanToBoolean;
    markPropertySet(BOOLEAN_TO_BOOLEAN);
    return this;
  }
}
