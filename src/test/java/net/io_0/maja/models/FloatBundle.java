package net.io_0.maja.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.maja.SetPropertiesAware;

@NoArgsConstructor
@Getter @ToString
public class FloatBundle extends SetPropertiesAware {
  public static final String ONE = "one";
  public static final String TWO = "two";
  public static final String THREE = "three";
  public static final String FOUR = "four";

  private Float one;
  private Float two;
  private Float three;
  private Float four;

  public FloatBundle setOne(Float one) {
    this.one = one;
    markPropertySet(ONE);
    return this;
  }

  public FloatBundle setTwo(Float two) {
    this.two = two;
    markPropertySet(TWO);
    return this;
  }

  public FloatBundle setThree(Float three) {
    this.three = three;
    markPropertySet(THREE);
    return this;
  }

  public FloatBundle setFour(Float four) {
    this.four = four;
    markPropertySet(FOUR);
    return this;
  }
}
