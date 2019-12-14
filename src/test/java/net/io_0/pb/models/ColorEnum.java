package net.io_0.pb.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ColorEnum {
  RED("red"),
  BLUE("blue"),
  GREEN("green"),
  GREEN_PURPLE_METALLIC("green-purple-metallic"),
  BANANA_YELLOW("banana yellow"),
  PINK_PA_ION("pink pa$$ion"),
  _12_2("12.2");

  private String value;

  ColorEnum(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ColorEnum fromValue(String text) {
    for (ColorEnum b : ColorEnum.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + text + "'");
  }
}

