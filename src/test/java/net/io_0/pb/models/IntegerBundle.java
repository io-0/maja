package net.io_0.pb.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.pb.SetPropertiesAware;

@NoArgsConstructor
@Getter @ToString
public class IntegerBundle extends SetPropertiesAware {
  public static final String ONE = "one";
  public static final String TWO = "two";
  public static final String THREE = "three";
  public static final String FOUR = "four";

  private Integer one;
  private Integer two;
  private Integer three;
  private Integer four;

  public IntegerBundle setOne(Integer one) {
    this.one = one;
    markPropertySet(ONE);
    return this;
  }

  public IntegerBundle setTwo(Integer two) {
    this.two = two;
    markPropertySet(TWO);
    return this;
  }

  public IntegerBundle setThree(Integer three) {
    this.three = three;
    markPropertySet(THREE);
    return this;
  }

  public IntegerBundle setFour(Integer four) {
    this.four = four;
    markPropertySet(FOUR);
    return this;
  }
}
