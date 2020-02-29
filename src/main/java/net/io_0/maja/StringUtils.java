package net.io_0.maja;

import java.util.Objects;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.util.Objects.isNull;

public class StringUtils {
  public static boolean ignoreFirstCharCaseEquals(String a, String b) {
    return Objects.equals(firstCharToLowerCase(a), firstCharToLowerCase(b));
  }

  public static String firstCharToLowerCase(String input) {
    if (isNull(input) || input.isEmpty()) {
      return input;
    }
    return toLowerCase(input.charAt(0)) + input.substring(1);
  }

  public static String firstCharToUpperCase(String input) {
    if (isNull(input) || input.isEmpty()) {
      return input;
    }
    return toUpperCase(input.charAt(0)) + input.substring(1);
  }
}
