package net.io_0.maja.models;

import lombok.*;
import net.io_0.maja.mapping.Mapper;
import java.util.Map;

@NoArgsConstructor
@Getter @Setter
@ToString
@Builder @AllArgsConstructor
public class PolymorphWithDefaultInstantiator {
  private Integer number;
  private Attribute attr;

  public interface Attribute {
    Attribute trap(Map<String, Object> data);

    default Attribute getInstance(Map<String, Object> data) {
      return Mapper.fromMap(data, Instance.class);
    }
  }

  @NoArgsConstructor
  @Getter @Setter
  @ToString
  @Builder @AllArgsConstructor
  public static class Instance implements Attribute {
    private String text;
    private Integer version;

    @Override
    public Attribute trap(Map<String, Object> data) {
      throw new RuntimeException("Trap triggered");
    }
  }
}
