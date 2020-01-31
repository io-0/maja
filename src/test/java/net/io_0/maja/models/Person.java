package net.io_0.maja.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.maja.PropertyBundle;
import net.io_0.maja.WithUnconventionalName;

@NoArgsConstructor
@Getter
@ToString
public class Person extends PropertyBundle {
  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";

  @WithUnconventionalName("first name")
  private String firstName;
  private String lastName;

  public Person setFirstName(String value) {
    this.firstName = value;
    markPropertySet(FIRST_NAME);
    return this;
  }

  public Person setLastName(String value) {
    this.lastName = value;
    markPropertySet(LAST_NAME);
    return this;
  }
}
