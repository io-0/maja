package net.io_0.pb.models;

import lombok.*;
import net.io_0.pb.SetPropertiesAware;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@ToString @Builder
public class ColorSubType extends SetPropertiesAware {
  public static final String ID = "id";
  public static final String NAME = "name";

  private Long id = 1L;
  private String name = "violet";
/*
  public ColorSubType setId(Long id) {
    this.id = id;
    markPropertySet(ID);
    return this;
  }

  public ColorSubType setName(String name) {
    this.name = name;
    markPropertySet(NAME);
    return this;
  }
 */
}
