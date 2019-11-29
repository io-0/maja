package net.io_0.property;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {
  public static String loadJsonResource(String name) {
    try {
      URI uri = TestUtils.class.getClassLoader().getResource(name).toURI();
      return new String(Files.readAllBytes(Paths.get(uri)), Charset.forName("utf-8"));
    } catch (NullPointerException | URISyntaxException | IOException e) {
      throw new IllegalArgumentException("Can't load resource '" + name + "'");
    }
  }
/*
  @SafeVarargs
  public static <T> void assertCollectionEquals(Collection<T> actual, T... expectedItems) {
    assertTrue(CollectionUtils.isEqualCollection(actual, Arrays.asList(expectedItems)));
  }

  public static <T> void assertMapEquals(Map<String, T> actual, T2<String, T>... expectedItems) {
    assertCollectionEquals(actual.entrySet().stream().map(entry -> Tuple.of(entry.getKey(), entry.getValue())).collect(Collectors.toList()), expectedItems);
  }
*/
}