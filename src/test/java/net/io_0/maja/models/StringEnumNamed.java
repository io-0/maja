package net.io_0.maja.models;

import lombok.Getter;
import net.io_0.maja.WithUnconventionalName;

public enum StringEnumNamed {
  @WithUnconventionalName("str 1") STR1(1),
  @WithUnconventionalName("str-2") STR2(2),
  STR3(3);

  @Getter
  private int numericValue;

  StringEnumNamed(int numericValue) {
    this.numericValue = numericValue;
  }
}