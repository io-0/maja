package net.io_0.property;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class PropertyIssue {
  private final String propertyName;
  private final String issue;

  public static PropertyIssue of(String name, String format) {
    return new PropertyIssue(name, format);
  }
}
