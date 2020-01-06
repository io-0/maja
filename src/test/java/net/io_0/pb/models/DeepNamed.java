package net.io_0.pb.models;

import lombok.*;
import net.io_0.pb.WithUnconventionalName;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Getter @Setter
@ToString
@Builder @AllArgsConstructor
public class DeepNamed {
  @WithUnconventionalName("obj") private Nested objectToPojo;
  private Set<Nested> objectArrayToObjectSet;
  private List<StringEnumNamed> stringArrayToEnumList;
}
