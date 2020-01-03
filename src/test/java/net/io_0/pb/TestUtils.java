package net.io_0.pb;

import org.apache.commons.collections.CollectionUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
  public static Reader resourceAsReader(String path) {
    try {
      return new InputStreamReader(TestUtils.class.getClassLoader().getResource(path).openStream(), StandardCharsets.UTF_8);
    } catch (NullPointerException | IOException e) {
      throw new IllegalArgumentException("Can't load resource '" + path + "'");
    }
  }

  public static String resourceAsString(String name) {
    try {
      URI uri = net.io_0.pb.experiments.TestUtils.class.getClassLoader().getResource(name).toURI();
      return Files.readString(Paths.get(uri));
    } catch (NullPointerException | URISyntaxException | IOException e) {
      throw new IllegalArgumentException("Can't load resource '" + name + "'");
    }
  }

  public static <U, T extends Collection<U>> void assertCollectionEquals(T expected, T actual) {
    assertTrue(CollectionUtils.isEqualCollection(expected, actual));
  }

  public static Map<String, String> toStringMap(Map<String, Object> map, String... withoutKeys) {
    return map
      .entrySet()
      .stream()
      .filter(entry -> !Arrays.asList(withoutKeys).contains(entry.getKey()))
      .map(entry -> Map.entry(entry.getKey(), entry.getValue().toString()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}