package net.io_0.pb.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.io_0.pb.mapping.JsonName;

import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@Getter @Setter
@ToString
public class Deep {
  @JsonName("obj") private Nested objectToPojo;
  private Map<String, Object> objectToMap;
  private List<Nested> objectArrayToObjectList;
  private Set<Nested> objectArrayToObjectSet;
}
