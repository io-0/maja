package net.io_0.maja;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {
  @Test
  void ignoreFirstCharCaseEquals() {
    assertTrue(StringUtils.ignoreFirstCharCaseEquals("hi", "Hi"));
    assertFalse(StringUtils.ignoreFirstCharCaseEquals("hi", "hI"));
  }

  @Test
  void firstCharToLowerCase() {
    assertNull(StringUtils.firstCharToLowerCase(null));
    assertEquals("", StringUtils.firstCharToLowerCase(""));
    assertEquals("hi", StringUtils.firstCharToLowerCase("hi"));
    assertEquals("hi", StringUtils.firstCharToLowerCase("Hi"));
  }

  @Test
  void firstCharToUpperCase() {
    assertNull(StringUtils.firstCharToUpperCase(null));
    assertEquals("", StringUtils.firstCharToUpperCase(""));
    assertEquals("Hi", StringUtils.firstCharToUpperCase("hi"));
    assertEquals("Hi", StringUtils.firstCharToUpperCase("Hi"));
  }
}