package net.io_0.pb.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter @Setter
@ToString
public class Flat {
  private String stringToString;
  private StringEnum stringToEnum;
  private BigDecimal stringToBigDecimal;
  private Float stringToFloat;
  private Double stringToDouble;
  private Integer stringToInteger;
  private Long stringToLong;
  private LocalDate stringToLocalDate;
  private OffsetDateTime stringToOffsetDateTime;
  private UUID stringToUUID;
  private URI stringToURI;
  private byte[] stringToByteArray;
  private File stringToFile;
  private BigDecimal numberToBigDecimal;
  private Float numberToFloat;
  private Double numberToDouble;
  private Integer numberToInteger;
  private Long numberToLong;
  private String numberToString;
  private List<String> stringArrayToStringList;
  private Set<OffsetDateTime> stringArrayToOffsetDateTimeSet;
  private List<Float> numberArrayToFloatList;
  private Set<Integer> numberArrayToIntegerSet;
  private Boolean booleanToBoolean;
  private String booleanToString;
}
