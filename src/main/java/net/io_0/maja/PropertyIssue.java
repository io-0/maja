package net.io_0.maja;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static java.lang.String.format;

@RequiredArgsConstructor
@Getter @ToString
public class PropertyIssue {
  private final String propertyName;
  private final Issue issue;

  public static PropertyIssue of(String propertyName, Issue issue) {
    return new PropertyIssue(propertyName, issue);
  }

  public static PropertyIssue of(String propertyName, String code, String message) {
    return new PropertyIssue(propertyName, Issue.of(code, message));
  }

  @RequiredArgsConstructor
  @Getter
  public static class Issue {
    private final String code;
    private final String message;

    public static Issue of(String code, String message) {
      return new Issue(code, message);
    }

    public Issue withMessage(String message) {
      return of(this.code, message);
    }

    @Override
    public String toString() {
      return format("Issue(code=%s, message=%s)", code, message);
    }
  }
}
