package net.io_0.pb.experiments;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class PropertyIssue {
  private final String propertyName;
  private final String issue;

  public static PropertyIssue of(String propertyName, String issue) {
    return new PropertyIssue(propertyName, issue);
  }
}
