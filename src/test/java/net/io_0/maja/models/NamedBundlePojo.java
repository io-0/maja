package net.io_0.maja.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.maja.WithUnconventionalName;

@NoArgsConstructor
@Getter @ToString
public class NamedBundlePojo {
  public static final String A_SPECIAL_NAME = "aSpecialName";
  public static final String B_SPECIAL_NAME = "BSpecialName";
  public static final String MA_JA = "MaJa";
  public static final String CA_JA = "caJa";
  public static final String FIRST_UPPER = "firstUpper";

  private Integer aSpecialName;
  private Integer BSpecialName;
  private Integer MaJa;
  private Integer caJa;
  @WithUnconventionalName("FirstUpper")
  private String firstUpper;

  public NamedBundlePojo setASpecialName(Integer aSpecialName) {
    this.aSpecialName = aSpecialName;
    return this;
  }

  public NamedBundlePojo setBSpecialName(Integer BSpecialName) {
    this.BSpecialName = BSpecialName;
    return this;
  }

  public NamedBundlePojo setMaJa(Integer maJa) {
    this.MaJa = maJa;
    return this;
  }

  public NamedBundlePojo setCaJa(Integer caJa) {
    this.caJa = caJa;
    return this;
  }

  public NamedBundlePojo setFirstUpper(String value) {
    this.firstUpper = value;
    return this;
  }
}
