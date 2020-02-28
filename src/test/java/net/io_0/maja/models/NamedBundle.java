package net.io_0.maja.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.maja.PropertyBundle;

@NoArgsConstructor
@Getter @ToString
public class NamedBundle extends PropertyBundle {
  public static final String A_SPECIAL_NAME = "aSpecialName";
  public static final String B_SPECIAL_NAME = "BSpecialName";
  public static final String MA_JA = "MaJa";
  public static final String CA_JA = "caJa";

  private Integer aSpecialName;
  private Integer BSpecialName;
  private Integer MaJa;
  private Integer caJa;

  public NamedBundle setASpecialName(Integer aSpecialName) {
    this.aSpecialName = aSpecialName;
    markPropertySet(A_SPECIAL_NAME);
    return this;
  }

  public NamedBundle setBSpecialName(Integer BSpecialName) {
    this.BSpecialName = BSpecialName;
    markPropertySet(B_SPECIAL_NAME);
    return this;
  }

  public NamedBundle setMaJa(Integer maJa) {
    this.MaJa = maJa;
    markPropertySet(MA_JA);
    return this;
  }

  public NamedBundle setCaJa(Integer caJa) {
    this.caJa = caJa;
    markPropertySet(CA_JA);
    return this;
  }
}
