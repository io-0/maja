package net.io_0.property.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import net.io_0.property.SetPropertiesAware;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@NoArgsConstructor
@Getter
@ToString
public class Pet extends SetPropertiesAware {
  public static final String NAME = "name";
  public static final String TAG = "tag";
  public static final String ID = "id";
  public static final String COLOR_ENUM = "colorEnum";
  public static final String COLOR_LIST = "colorList";
  public static final String COLOR_SET = "colorSet";
  public static final String COLOR_SUB_TYPE = "colorSubType";
  public static final String NUM = "num";
  public static final String NUM_FLOAT = "numFloat";
  public static final String NUM_DOUBLE = "numDouble";
  public static final String INTEG = "integ";
  public static final String INT_INT = "intInt";
  public static final String INT_LONG = "intLong";
  public static final String STR_LEN = "strLen";
  public static final String STR_DATE = "strDate";
  public static final String STR_DATE_TIME = "strDateTime";
  public static final String STR_PASSWORD = "strPassword";
  public static final String STR_BYTE = "strByte";
  public static final String STR_BINARY = "strBinary";
  public static final String STR_EMAIL = "strEmail";
  public static final String STR_UUID = "strUuid";
  public static final String STR_URI = "strUri";
  public static final String STRHOSTNAME = "strhostname";
  public static final String STR_IPV4 = "strIpv4";
  public static final String STR_IPV6 = "strIpv6";
  public static final String INNER = "inner";
  public static final String SSN = "ssn";
  public static final String TO_KINDS_OF_PEOPLE = "toKindsOfPeople";
  public static final String NULLABLE = "nullable";
  public static final String ZOO = "zoo";
  public static final String ENUMS = "enums";
  public static final String ARRAY_RANGE = "arrayRange";
  public static final String BIN_FILE = "binFile";
  public static final String BASE64FILE = "base64file";
  public static final String STR_MAP = "strMap";
  public static final String LONG_MAP = "longMap";
  public static final String PET_MAP = "petMap";
  public static final String ENUM_MAP = "enumMap";
  public static final String OPTIONAL_PET = "optionalPet";
  public static final String MAYBE_NULL = "maybeNull";
    
  private String name;
  private String tag;
  private Long id;
  private ColorEnum colorEnum;
  private List<String> colorList = new ArrayList<>();
  private Set<String> colorSet = new HashSet<>();
  private ColorSubType colorSubType;
  private BigDecimal num = new BigDecimal(15);
  private Float numFloat = 15.1F;
  private Double numDouble = 16D;
  private Integer integ = 17;
  private Integer intInt = 18;
  private Long intLong = 44L;
  private String strLen = "test";
  private LocalDate strDate;
  private OffsetDateTime strDateTime;
  private String strPassword = "iAMgod";
  private byte[] strByte;
  private File strBinary;
  private String strEmail = "test@test.at";
  private UUID strUuid = UUID.fromString("ad1d37fb-bd6d-47b6-b908-cdb5ff146c18");
  private String strUri = "http://test.xyz.xx/adilk";
  private String strhostname = "dunno";
  private String strIpv4 = "127.0.0.1";
  private String strIpv6 = "::1";
  private Pet inner;
  private String ssn = "42";
  private Boolean toKindsOfPeople;
  private Boolean nullable;
  private List<Pet> zoo = new ArrayList<>();
  private List<ColorEnum> enums = new ArrayList<>();
  private List<Integer> arrayRange = new ArrayList<>();
  private File binFile;
  private byte[] base64file;
  private Map<String, String> strMap = new HashMap<>();
  private Map<String, Long> longMap = new HashMap<>();
  private Map<String, Pet> petMap = new HashMap<>();
  private Map<String, ColorEnum> enumMap = new HashMap<>();
  private Pet optionalPet;
  private String maybeNull;

  public Pet setName(String name) {
    this.name = name;
    markPropertySet(NAME);
    return this;
  }

  public Pet setTag(String tag) {
    this.tag = tag;
    markPropertySet(TAG);
    return this;
  }

  public Pet setId(Long id) {
    this.id = id;
    markPropertySet(ID);
    return this;
  }

  public Pet setColorEnum(ColorEnum colorEnum) {
    this.colorEnum = colorEnum;
    markPropertySet(COLOR_ENUM);
    return this;
  }

  public Pet setColorList(List<String> colorList) {
    this.colorList = colorList;
    markPropertySet(COLOR_LIST);
    return this;
  }

  public Pet setColorSet(Set<String> colorSet) {
    this.colorSet = colorSet;
    markPropertySet(COLOR_SET);
    return this;
  }

  public Pet setColorSubType(ColorSubType colorSubType) {
    this.colorSubType = colorSubType;
    markPropertySet(COLOR_SUB_TYPE);
    return this;
  }

  public Pet setNum(BigDecimal num) {
    this.num = num;
    markPropertySet(NUM);
    return this;
  }

  public Pet setNumFloat(Float numFloat) {
    this.numFloat = numFloat;
    markPropertySet(NUM_FLOAT);
    return this;
  }

  public Pet setNumDouble(Double numDouble) {
    this.numDouble = numDouble;
    markPropertySet(NUM_DOUBLE);
    return this;
  }

  public Pet setInteg(Integer integ) {
    this.integ = integ;
    markPropertySet(INTEG);
    return this;
  }

  public Pet setIntInt(Integer intInt) {
    this.intInt = intInt;
    markPropertySet(INT_INT);
    return this;
  }

  public Pet setIntLong(Long intLong) {
    this.intLong = intLong;
    markPropertySet(INT_LONG);
    return this;
  }

  public Pet setStrLen(String strLen) {
    this.strLen = strLen;
    markPropertySet(STR_LEN);
    return this;
  }

  public Pet setStrDate(LocalDate strDate) {
    this.strDate = strDate;
    markPropertySet(STR_DATE);
    return this;
  }

  public Pet setStrDateTime(OffsetDateTime strDateTime) {
    this.strDateTime = strDateTime;
    markPropertySet(STR_DATE_TIME);
    return this;
  }

  public Pet setStrPassword(String strPassword) {
    this.strPassword = strPassword;
    markPropertySet(STR_PASSWORD);
    return this;
  }

  public Pet setStrByte(byte[] strByte) {
    this.strByte = strByte;
    markPropertySet(STR_BYTE);
    return this;
  }

  public Pet setStrBinary(File strBinary) {
    this.strBinary = strBinary;
    markPropertySet(STR_BINARY);
    return this;
  }

  public Pet setStrEmail(String strEmail) {
    this.strEmail = strEmail;
    markPropertySet(STR_EMAIL);
    return this;
  }

  public Pet setStrUuid(UUID strUuid) {
    this.strUuid = strUuid;
    markPropertySet(STR_UUID);
    return this;
  }

  public Pet setStrUri(String strUri) {
    this.strUri = strUri;
    markPropertySet(STR_URI);
    return this;
  }

  public Pet setStrhostname(String strhostname) {
    this.strhostname = strhostname;
    markPropertySet(STRHOSTNAME);
    return this;
  }

  public Pet setStrIpv4(String strIpv4) {
    this.strIpv4 = strIpv4;
    markPropertySet(STR_IPV4);
    return this;
  }

  public Pet setStrIpv6(String strIpv6) {
    this.strIpv6 = strIpv6;
    markPropertySet(STR_IPV6);
    return this;
  }

  public Pet setInner(Pet inner) {
    this.inner = inner;
    markPropertySet(INNER);
    return this;
  }

  public Pet setSsn(String ssn) {
    this.ssn = ssn;
    markPropertySet(SSN);
    return this;
  }

  public Pet setToKindsOfPeople(Boolean toKindsOfPeople) {
    this.toKindsOfPeople = toKindsOfPeople;
    markPropertySet(TO_KINDS_OF_PEOPLE);
    return this;
  }

  public Pet setNullable(Boolean nullable) {
    this.nullable = nullable;
    markPropertySet(NULLABLE);
    return this;
  }

  public Pet setZoo(List<Pet> zoo) {
    this.zoo = zoo;
    markPropertySet(ZOO);
    return this;
  }

  public Pet setEnums(List<ColorEnum> enums) {
    this.enums = enums;
    markPropertySet(ENUMS);
    return this;
  }

  public Pet setArrayRange(List<Integer> arrayRange) {
    this.arrayRange = arrayRange;
    markPropertySet(ARRAY_RANGE);
    return this;
  }

  public Pet setBinFile(File binFile) {
    this.binFile = binFile;
    markPropertySet(BIN_FILE);
    return this;
  }

  public Pet setBase64file(byte[] base64file) {
    this.base64file = base64file;
    markPropertySet(BASE64FILE);
    return this;
  }

  public Pet setStrMap(Map<String, String> strMap) {
    this.strMap = strMap;
    markPropertySet(STR_MAP);
    return this;
  }

  public Pet setLongMap(Map<String, Long> longMap) {
    this.longMap = longMap;
    markPropertySet(LONG_MAP);
    return this;
  }

  public Pet setPetMap(Map<String, Pet> petMap) {
    this.petMap = petMap;
    markPropertySet(PET_MAP);
    return this;
  }

  public Pet setEnumMap(Map<String, ColorEnum> enumMap) {
    this.enumMap = enumMap;
    markPropertySet(ENUM_MAP);
    return this;
  }

  public Pet setOptionalPet(Pet optionalPet) {
    this.optionalPet = optionalPet;
    markPropertySet(OPTIONAL_PET);
    return this;
  }

  public Pet setMaybeNull(String maybeNull) {
    this.maybeNull = maybeNull;
    markPropertySet(MAYBE_NULL);
    return this;
  }
}
