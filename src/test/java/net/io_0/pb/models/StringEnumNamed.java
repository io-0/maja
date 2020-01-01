package net.io_0.pb.models;

import lombok.Getter;
import net.io_0.pb.mapping.JsonName;

public enum StringEnumNamed {
  @JsonName("str 1") STR1(1),
  @JsonName("str-2") STR2(2),
  STR3(3);

  @Getter
  private int numericValue;

  StringEnumNamed(int numericValue) {
    this.numericValue = numericValue;
  }
}