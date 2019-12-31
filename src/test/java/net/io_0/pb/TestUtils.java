package net.io_0.pb;

import org.apache.commons.collections.CollectionUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
  public static Reader loadJsonResource(String path) {
    try {
      return new InputStreamReader(TestUtils.class.getClassLoader().getResource(path).openStream(), StandardCharsets.UTF_8);
    } catch (NullPointerException | IOException e) {
      throw new IllegalArgumentException("Can't load resource '" + path + "'");
    }
  }

  public static <U, T extends Collection<U>> void assertCollectionEquals(T expected, T actual) {
    assertTrue(CollectionUtils.isEqualCollection(expected, actual));
  }

}