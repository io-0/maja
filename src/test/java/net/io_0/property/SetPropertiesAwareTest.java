package net.io_0.property;

import net.io_0.property.models.ColorSubType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SetPropertiesAwareTest {
  @Test
  void markPropertySetTest() {
    // given
    ColorSubType cst = new ColorSubType();

    // when
    cst.setName("test1");

    // then
    assertTrue(cst.isPropertySet(ColorSubType.NAME));
    assertFalse(cst.isPropertySet(ColorSubType.ID));
  }

  @Test
  void markPropertySetMultipleTimesTest() {
    // given
    ColorSubType cst = new ColorSubType();

    // when
    cst.setName("test1");
    cst.setName("test2");
    cst.setName("test3");

    // then
    assertTrue(cst.isPropertySet(ColorSubType.NAME));
    assertFalse(cst.isPropertySet(ColorSubType.ID));
  }

  @Test
  void markPropertySetWithNullTest() {
    // given
    ColorSubType cst = new ColorSubType();

    // when
    cst.setName(null);

    // then
    assertTrue(cst.isPropertySet(ColorSubType.NAME));
    assertFalse(cst.isPropertySet(ColorSubType.ID));
  }

}