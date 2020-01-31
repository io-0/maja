package net.io_0.maja;

import net.io_0.maja.PropertyIssue.Issue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

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
      .map(propertyIssue -> format("%s -> %s (%s)",
        propertyIssue.getPropertyName(), propertyIssue.getIssue().getCode(), propertyIssue.getIssue().getMessage()
      ))
      .collect(Collectors.joining("; "));
  }

  public AdderWithPropertyNamePrefix withPropertyNamePrefix(String prefix) {
    PropertyIssues self = this;
    return new AdderWithPropertyNamePrefix() {
      @Override
      public boolean add(PropertyIssue propertyIssue) {
        return self.add(propertyIssue.withPropertyNamePrefix(prefix));
      }

      @Override
      public boolean addAll(Collection<? extends PropertyIssue> propertyIssues) {
        return self.addAll(propertyIssues.stream()
          .map(propertyIssue -> propertyIssue.withPropertyNamePrefix(prefix))
          .collect(Collectors.toList())
        );
      }
    };
  }

  public interface AdderWithPropertyNamePrefix {
    boolean add(PropertyIssue e);
    boolean addAll(Collection<? extends PropertyIssue> c);
  }
}
