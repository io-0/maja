package net.io_0.pb.experiments;

import java.util.ArrayList;
import java.util.Arrays;

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
}
