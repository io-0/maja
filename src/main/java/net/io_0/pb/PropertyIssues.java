package net.io_0.pb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PropertyIssues extends ArrayList<PropertyIssue> {
  public static PropertyIssues of() {
    return new PropertyIssues();
  }

  public static PropertyIssues of(PropertyIssue... propertyIssues) {
    PropertyIssues container = new PropertyIssues();
    container.addAll(Arrays.asList(propertyIssues));
    return container;
  }

  public boolean containsPropertyName(String propertyName) {
    return stream().anyMatch(pI -> pI.getPropertyName().equals(propertyName));
  }

  @Override
  public String toString() {
    return stream()
      .map(propertyIssue -> String.format("%s -> %s", propertyIssue.getPropertyName(), propertyIssue.getIssue()))
      .collect(Collectors.joining("; "));
  }
}
