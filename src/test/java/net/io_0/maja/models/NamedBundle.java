package net.io_0.maja.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.maja.PropertyBundle;

@NoArgsConstructor
@Getter @ToString
public class NamedBundle extends PropertyBundle {
  public static final String A_SPECIAL_NAME = "aSpecialName";

  private Integer aSpecialName;

  public NamedBundle setASpecialName(Integer aSpecialName) {
    this.aSpecialName = aSpecialName;
    markPropertySet(A_SPECIAL_NAME);
    return this;
  }
}
