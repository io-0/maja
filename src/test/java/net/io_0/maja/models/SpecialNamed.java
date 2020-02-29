package net.io_0.maja.models;

import lombok.*;
import net.io_0.maja.WithUnconventionalName;

@NoArgsConstructor
@Getter @Setter
@ToString
@Builder @AllArgsConstructor
public class SpecialNamed {
  @WithUnconventionalName("x-obj")
  private Integer xObj;
  @WithUnconventionalName("y-obj")
  private Integer YObj;
}
