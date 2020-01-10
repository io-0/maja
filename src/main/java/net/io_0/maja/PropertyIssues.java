package net.io_0.maja;

import net.io_0.maja.PropertyIssue.Issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
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

  public Optional<Issue> getPropertyIssue(String propertyName) {
    return stream().filter(pI -> pI.getPropertyName().equals(propertyName)).findFirst().map(PropertyIssue::getIssue);
  }

  @Override
  public String toString() {
    return stream()
      .map(propertyIssue -> String.format("%s -> %s", propertyIssue.getPropertyName(), propertyIssue.getIssue()))
      .collect(Collectors.joining("; "));
  }
}
