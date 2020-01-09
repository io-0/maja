package net.io_0.maja.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.maja.PropertyBundle;

@NoArgsConstructor
@Getter @ToString
public class StringBundle extends PropertyBundle {
  public static final String ONE = "one";
  public static final String TWO = "two";
  public static final String THREE = "three";
  public static final String FOUR = "four";

  private String one;
  private String two;
  private String three;
  private String four;

  public StringBundle setOne(String one) {
    this.one = one;
    markPropertySet(ONE);
    return this;
  }

  public StringBundle setTwo(String two) {
    this.two = two;
    markPropertySet(TWO);
    return this;
  }

  public StringBundle setThree(String three) {
    this.three = three;
    markPropertySet(THREE);
    return this;
  }

  public StringBundle setFour(String four) {
    this.four = four;
    markPropertySet(FOUR);
    return this;
  }
}
