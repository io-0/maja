package net.io_0.pb.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.io_0.pb.WithUnconventionalName;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter @Setter
@ToString
public class DeepFlawed {
  private PrivateInner stringToObject;
  private PublicInner numberToObject;
  private StringEnum numberToEnum;
  @WithUnconventionalName("obj") private Nested objectToPojo;
  private Map<String, Object> objectToMap;
  private Map<Integer, Integer> objectToIntMap;
  private List<Nested> objectArrayToObjectList;
  private Set<Nested> objectArrayToObjectSet;

  private static class PrivateInner {}

  public static class PublicInner {
    public PublicInner(Integer i) {
      throw new IllegalStateException();
    }
  }
}
