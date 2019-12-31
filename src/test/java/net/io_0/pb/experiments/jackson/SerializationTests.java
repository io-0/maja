package net.io_0.pb.experiments.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.io_0.pb.experiments.TestUtils;
import net.io_0.pb.experiments.jackson.serialization.SetPropertiesAwareModule;
import net.io_0.pb.experiments.models.ColorEnum;
import net.io_0.pb.experiments.models.ColorSubType;
import net.io_0.pb.experiments.models.Pet;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class SerializationTests {
  private ObjectMapper objectMapper = new ObjectMapper()
    .registerModules(
      new JavaTimeModule(),
      new SetPropertiesAwareModule()
    )
    .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(
      DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
      DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES // ignore unknown fields
    )
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

  @Test
  public void jackson_can_serialize() throws IOException, JSONException {
    Pet pet = new Pet();
    pet.setName("Gerti");
    pet.setTag("GuterTag");
    pet.setId(12L);
    pet.setColorEnum(ColorEnum.BLUE);
    pet.setNum(BigDecimal.valueOf(20000000));
    pet.setNumFloat(20.1F);
    pet.setNumDouble(220.1);
    pet.setInteg(4);
    pet.setIntInt(5);
    pet.setIntLong((long) Integer.MAX_VALUE + 1);
    pet.setStrLen("some text");
    pet.setStrDate(LocalDate.of(2019, 7, 23));
    pet.setStrDateTime(OffsetDateTime.of(2010, 1, 1, 10, 0, 10, 0, ZoneOffset.ofHours(1)));
    pet.setStrPassword("superSecure");
    pet.setStrByte("test".getBytes());
    pet.setStrBinary(new File("/1f03ff"));
    pet.setStrEmail("user@example.com");
    pet.setStrUuid(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
    pet.setStrUri(URI.create("http://www.example.com/eula").toString());
    pet.setStrhostname("example.com");
    pet.setStrIpv4("198.51.100.42");
    pet.setStrIpv6("2001:0db8:5b96:0000:0000:426f:8e17:642a");
    pet.setSsn("102-21-2001");
    pet.setToKindsOfPeople(true);
    pet.setNullable(false);
    pet.setBinFile(new File("/1f03ff"));
    pet.setBase64file("test".getBytes());
    pet.setColorList(List.of(
      "violet",
      "gray",
      "yellow",
      "violet"
    ));
    pet.setColorSet(Set.of(
      "violet",
      "gray",
      "yellow"
    ));
    pet.setColorSubType(new ColorSubType().setId(19L).setName("rgb123"));
    pet.setStrMap(Map.of(
      "additionalProp1", "I",
      "additionalProp2", "am",
      "additionalProp3", "a",
      "additionalProp4", "map"
    ));
    pet.setLongMap(Map.of(
      "additionalProp1", 1L,
      "additionalProp2", 2L,
      "additionalProp3", 3L
    ));
    pet.setEnumMap(Map.of(
      "additionalProp1", ColorEnum.RED,
      "additionalProp2", ColorEnum.BLUE,
      "additionalProp3", ColorEnum.GREEN
    ));
    pet.setPetMap(Map.of(
      "aPet", new Pet().setName("MapPet1").setTag("MapTag1").setId(42L).setColorEnum(ColorEnum.RED)
    ));
    pet.setZoo(List.of(
      new Pet().setName("ZooPet1").setTag("ZooTag1").setId(93L).setColorEnum(ColorEnum.BLUE),
      new Pet().setName("ZooPet2").setTag("ZooTag2").setId(94L).setColorEnum(ColorEnum.GREEN)
    ));

    pet.setMaybeNull(null);

    String json = objectMapper.writeValueAsString(pet);
    String reference = TestUtils.loadJsonResource("PetWithDefaults.json");

    JSONAssert.assertEquals(reference, json, JSONCompareMode.NON_EXTENSIBLE);
  }
}
