package net.io_0.pb.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.pb.SetPropertiesAware;
import java.math.BigDecimal;

@NoArgsConstructor
@Getter @ToString
public class BigDecimalBundle extends SetPropertiesAware {
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
