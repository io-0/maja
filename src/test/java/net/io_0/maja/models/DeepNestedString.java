package net.io_0.maja.models;

import lombok.*;
import net.io_0.maja.WithUnconventionalName;

import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter @Setter
@ToString
@Builder @AllArgsConstructor
public class DeepNestedString {
  @WithUnconventionalName("obj") private String objectToPojo;
  private Map<String, String> objectToMap;
  private List<String> objectArrayToObjectList;
  private Set<String> objectArrayToObjectSet;
}
