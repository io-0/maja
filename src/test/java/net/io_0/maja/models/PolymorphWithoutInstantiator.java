package net.io_0.maja.models;

import lombok.*;
import net.io_0.maja.mapping.Mapper;

import java.util.Map;

@NoArgsConstructor
@Getter @Setter
@ToString
@Builder @AllArgsConstructor
public class PolymorphWithoutInstantiator {
  private Integer number;
  private Attribute attr;

  public interface Attribute { }

  @NoArgsConstructor
  @Getter @Setter
  @ToString
  @Builder @AllArgsConstructor
  public static class Instance implements Attribute {
    private String text;
    private Integer version;

    public static Attribute instHelper(Map<String, Object> data) {
      return Mapper.fromMap(data, Instance.class);
    }
  }
}
