package net.io_0.pb.models;

import lombok.Getter;
import net.io_0.pb.mapping.PropertyName;

public enum StringEnumNamed {
  @PropertyName("str 1") STR1(1),
  @PropertyName("str-2") STR2(2),
  STR3(3);

  @Getter
  private int numericValue;

  StringEnumNamed(int numericValue) {
    this.numericValue = numericValue;
  }
}