package net.io_0.property.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.io_0.property.jackson.deserialization.PropertyIssueCollectorModule;
import net.io_0.property.models.ColorEnum;
import net.io_0.property.models.Pet;
import net.io_0.property.PropertyIssues;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static net.io_0.property.TestUtils.loadJsonResource;
import static net.io_0.property.jackson.deserialization.PropertyIssueCollectorModule.PROPERTY_ISSUES_ATTR;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class DeserializationTests {
  private ObjectMapper objectMapper = new ObjectMapper()
    .registerModules(
      new JavaTimeModule(),
      new PropertyIssueCollectorModule()
    )
    .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(
      DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
      DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES // ignore unknown fields
    )
    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

  @Test
  public void jackson_can_deserialize() throws IOException {
    String json = loadJsonResource("Pet.json");

    Pet pet = objectMapper.readValue(json, Pet.class);

    assertEquals("Gerti", pet.getName());
    assertTrue(pet.isPropertySet(Pet.NAME));
    assertEquals("GuterTag", pet.getTag());
    assertTrue(pet.isPropertySet(Pet.TAG));
    assertEquals(12, (long) pet.getId());
    assertTrue(pet.isPropertySet(Pet.ID));
    assertEquals(ColorEnum.BLUE, pet.getColorEnum());
    assertTrue(pet.isPropertySet(Pet.COLOR_ENUM));
    assertEquals(BigDecimal.valueOf(20000000), pet.getNum());
    assertTrue(pet.isPropertySet(Pet.NUM));
    assertEquals(20.1, pet.getNumFloat(), 0.01);
    assertTrue(pet.isPropertySet(Pet.NUM_FLOAT));
    assertEquals(220.1, pet.getNumDouble(), 0.01);
    assertTrue(pet.isPropertySet(Pet.NUM_DOUBLE));
    assertEquals(21, (int) pet.getInteg());
    assertTrue(pet.isPropertySet(Pet.INTEG));
    assertEquals(5, (int) pet.getIntInt());
    assertTrue(pet.isPropertySet(Pet.INT_INT));
    assertEquals((long) Integer.MAX_VALUE + 1, (long) pet.getIntLong());
    assertTrue(pet.isPropertySet(Pet.INT_LONG));
    assertEquals("some text", pet.getStrLen());
    assertTrue(pet.isPropertySet(Pet.STR_LEN));
    assertEquals(LocalDate.of(2019, 7, 23), pet.getStrDate());
    assertTrue(pet.isPropertySet(Pet.STR_DATE));
    assertEquals(OffsetDateTime.of(2010, 1, 1, 10, 0, 10, 0, ZoneOffset.ofHours(1)), pet.getStrDateTime());
    assertTrue(pet.isPropertySet(Pet.STR_DATE_TIME));
    assertEquals("superSecure", pet.getStrPassword());
    assertTrue(pet.isPropertySet(Pet.STR_PASSWORD));
    assertEquals("test", new String(pet.getStrByte()));
    assertTrue(pet.isPropertySet(Pet.STR_BYTE));
    assertEquals(new File("/1f03ff"), pet.getStrBinary());
    assertTrue(pet.isPropertySet(Pet.STR_BINARY));
    assertEquals("user@example.com", pet.getStrEmail());
    assertTrue(pet.isPropertySet(Pet.STR_EMAIL));
    assertEquals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"), pet.getStrUuid());
    assertTrue(pet.isPropertySet(Pet.STR_UUID));
    assertEquals(URI.create("http://www.example.com/eula").toString(), pet.getStrUri());
    assertTrue(pet.isPropertySet(Pet.STR_URI));
    assertEquals("example.com", pet.getStrhostname());
    assertTrue(pet.isPropertySet(Pet.STRHOSTNAME));
    assertEquals("198.51.100.42", pet.getStrIpv4());
    assertTrue(pet.isPropertySet(Pet.STR_IPV4));
    assertEquals("2001:0db8:5b96:0000:0000:426f:8e17:642a", pet.getStrIpv6());
    assertTrue(pet.isPropertySet(Pet.STR_IPV6));
    assertEquals("102-21-2001", pet.getSsn());
    assertTrue(pet.isPropertySet(Pet.SSN));
    assertEquals(true, pet.getToKindsOfPeople());
    assertTrue(pet.isPropertySet(Pet.TO_KINDS_OF_PEOPLE));
    assertEquals(false, pet.getNullable());
    assertTrue(pet.isPropertySet(Pet.NULLABLE));
    assertEquals(new File("/1f03ff"), pet.getBinFile());
    assertTrue(pet.isPropertySet(Pet.BIN_FILE));
    assertEquals("test", new String(pet.getBase64file()));
    assertTrue(pet.isPropertySet(Pet.BASE64FILE));
    assertNull(pet.getOptionalPet());
    assertFalse(pet.isPropertySet(Pet.OPTIONAL_PET));
    assertNull(pet.getMaybeNull());
    assertTrue(pet.isPropertySet(Pet.MAYBE_NULL));
    assertEquals("MapPet1", pet.getPetMap().get("aPet").getName());
    assertTrue(pet.isPropertySet(Pet.PET_MAP));
  }

  @Test
  public void jackson_can_deserialize_with_exceptions() throws IOException {
    String json = loadJsonResource("Pet.json");
    json = json
      .replace(": 21,", ": \"A\",")
      .replace(": 220.1,", ": \"B\",")
      .replace(": \"2019-07-23\",", ": \"C\",")
      .replace("\"colorEnum\": \"green\"", "\"colorEnum\": \"greeni\"");

    PropertyIssues propertyIssues = PropertyIssues.of();

    Pet pet = objectMapper // objectMapper has PropertyIssueCollectorModule installed
      .reader()
      .forType(Pet.class)
      .withAttribute(PROPERTY_ISSUES_ATTR, propertyIssues)
      .readValue(json);

    assertEquals("Gerti", pet.getName());
    assertTrue(pet.isPropertySet(Pet.NAME));
    assertEquals("GuterTag", pet.getTag());
    assertTrue(pet.isPropertySet(Pet.TAG));
    assertEquals(12, (long) pet.getId());
    assertTrue(pet.isPropertySet(Pet.ID));
    assertEquals(ColorEnum.BLUE, pet.getColorEnum());
    assertTrue(pet.isPropertySet(Pet.COLOR_ENUM));
    assertEquals(BigDecimal.valueOf(20000000), pet.getNum());
    assertTrue(pet.isPropertySet(Pet.NUM));
    assertEquals(20.1, pet.getNumFloat(), 0.01);
    assertTrue(pet.isPropertySet(Pet.NUM_FLOAT));
    assertNull(pet.getNumDouble());
    assertTrue(pet.isPropertySet(Pet.NUM_DOUBLE));
    assertNull(pet.getInteg());
    assertTrue(pet.isPropertySet(Pet.INTEG));
    assertEquals(5, (int) pet.getIntInt());
    assertTrue(pet.isPropertySet(Pet.INT_INT));
    assertEquals((long) Integer.MAX_VALUE + 1, (long) pet.getIntLong());
    assertTrue(pet.isPropertySet(Pet.INT_LONG));
    assertEquals("some text", pet.getStrLen());
    assertTrue(pet.isPropertySet(Pet.STR_LEN));
    assertNull(pet.getStrDate());
    assertTrue(pet.isPropertySet(Pet.STR_DATE));
    assertEquals(OffsetDateTime.of(2010, 1, 1, 10, 0, 10, 0, ZoneOffset.ofHours(1)), pet.getStrDateTime());
    assertTrue(pet.isPropertySet(Pet.STR_DATE_TIME));
    assertEquals("superSecure", pet.getStrPassword());
    assertTrue(pet.isPropertySet(Pet.STR_PASSWORD));
    assertEquals("test", new String(pet.getStrByte()));
    assertTrue(pet.isPropertySet(Pet.STR_BYTE));
    assertEquals(new File("/1f03ff"), pet.getStrBinary());
    assertTrue(pet.isPropertySet(Pet.STR_BINARY));
    assertEquals("user@example.com", pet.getStrEmail());
    assertTrue(pet.isPropertySet(Pet.STR_EMAIL));
    assertEquals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"), pet.getStrUuid());
    assertTrue(pet.isPropertySet(Pet.STR_UUID));
    assertEquals(URI.create("http://www.example.com/eula").toString(), pet.getStrUri());
    assertTrue(pet.isPropertySet(Pet.STR_URI));
    assertEquals("example.com", pet.getStrhostname());
    assertTrue(pet.isPropertySet(Pet.STRHOSTNAME));
    assertEquals("198.51.100.42", pet.getStrIpv4());
    assertTrue(pet.isPropertySet(Pet.STR_IPV4));
    assertEquals("2001:0db8:5b96:0000:0000:426f:8e17:642a", pet.getStrIpv6());
    assertTrue(pet.isPropertySet(Pet.STR_IPV6));
    assertEquals("102-21-2001", pet.getSsn());
    assertTrue(pet.isPropertySet(Pet.SSN));
    assertEquals(true, pet.getToKindsOfPeople());
    assertTrue(pet.isPropertySet(Pet.TO_KINDS_OF_PEOPLE));
    assertEquals(false, pet.getNullable());
    assertTrue(pet.isPropertySet(Pet.NULLABLE));
    assertEquals(new File("/1f03ff"), pet.getBinFile());
    assertTrue(pet.isPropertySet(Pet.BIN_FILE));
    assertEquals("test", new String(pet.getBase64file()));
    assertTrue(pet.isPropertySet(Pet.BASE64FILE));
    assertNull(pet.getOptionalPet());
    assertFalse(pet.isPropertySet(Pet.OPTIONAL_PET));
    assertNull(pet.getMaybeNull());
    assertTrue(pet.isPropertySet(Pet.MAYBE_NULL));
    assertEquals("MapPet1", pet.getPetMap().get("aPet").getName());
    assertTrue(pet.isPropertySet(Pet.PET_MAP));

    assertEquals(5, propertyIssues.size());
    assertTrue(propertyIssues.containsPropertyName("integ"));
    assertTrue(propertyIssues.containsPropertyName("numDouble"));
    assertTrue(propertyIssues.containsPropertyName("strDate"));
    assertTrue(propertyIssues.containsPropertyName("zoo.1.colorEnum"));
    assertTrue(propertyIssues.containsPropertyName("petMap.aPet.integ"));
  }
}
