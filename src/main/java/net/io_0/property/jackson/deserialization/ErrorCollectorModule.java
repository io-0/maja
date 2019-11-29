package net.io_0.property.jackson.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Instead of stopping deserialization on the first problem this module collects failure messages in a map.
 * Fields with deserialization problems / exceptions will be set to null.
 * The collecting map has to be provided via attribute.
 *
 * Implementation example:
 *     Map<String, String> deserializationErrors = new HashMap<>();
 *
 *     Pet pet = objectMapper // objectMapper has ErrorCollectorModule installed
 *       .reader()
 *       .forType(Pet.class)
 *       .withAttribute(DESERIALIZATION_ERRORS_ATTR, deserializationErrors)
 *       .readValue(json);
 */
@Slf4j
public class ErrorCollectorModule extends Module {
  public static final String DESERIALIZATION_ERRORS_ATTR = "deserializationErrors";
  public static final String MODULE_NAME = "ErrorCollectorModule";

  @Override
  public String getModuleName() {
    return MODULE_NAME;
  }

  @Override
  public Version version() {
    return new Version(1, 0, 0, null);
  }

  @Override
  public void setupModule(SetupContext context) {
    context.addDeserializationProblemHandler(problemHandler);
  }

  private static DeserializationProblemHandler problemHandler = new DeserializationProblemHandler() {
    @Override
    public Object handleWeirdStringValue(DeserializationContext ctx, Class<?> targetType, String valueToConvert, String failureMsg) throws IOException {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", valueToConvert, failureMsg));
    }

    @Override
    public Object handleInstantiationProblem(DeserializationContext ctx, Class<?> instClass, Object argument, Throwable t) throws IOException {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", argument, t.getMessage()));
    }

    @Override
    public Object handleWeirdKey(DeserializationContext ctx, Class<?> rawKeyType, String keyValue, String failureMsg) throws IOException {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", keyValue, failureMsg));
    }

    @Override
    public Object handleWeirdNumberValue(DeserializationContext ctx, Class<?> targetType, Number valueToConvert, String failureMsg) throws IOException {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", valueToConvert, failureMsg));
    }

    @Override
    public Object handleWeirdNativeValue(DeserializationContext ctx, JavaType targetType, Object valueToConvert, JsonParser p) throws IOException {
      return addErrorAndReturnNull(ctx, String.format("%s, doesn't fit %s", valueToConvert, targetType));
    }

    @Override
    public Object handleUnexpectedToken(DeserializationContext ctx, JavaType targetType, JsonToken t, JsonParser p, String failureMsg) throws IOException {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", t, failureMsg));
    }

    @Override
    public Object handleMissingInstantiator(DeserializationContext ctx, Class<?> instClass, ValueInstantiator instantiator, JsonParser p, String msg) throws IOException {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", instantiator, msg));
    }

    /**
     * Add error to map in context and return null as value substitute
     * @param ctx context to get error map from and field name
     * @param msg message to add as error
     * @return null, as value substitute
     */
    @SuppressWarnings("unchecked")
    private Object addErrorAndReturnNull(DeserializationContext ctx, String msg) {
      try {
        Map<String, String> deserializationErrors = (Map<String, String>) ctx.getAttribute(DESERIALIZATION_ERRORS_ATTR);
        Objects.requireNonNull(deserializationErrors);

        deserializationErrors.put(extractAttributeName(ctx.getParser()), msg);
      } catch (ClassCastException | NullPointerException e) {
        log.error("Deserialization context attribute \"{}\" is either not set or not of type Map<String, String>.", DESERIALIZATION_ERRORS_ATTR);
      }

      return null;
    }
    
    /**
     * Json path e.g. "/zoo/1/colorEnum" to simple attribute name e.g. "zoo.1.colorEnum"
     * @param parser parser to extract json path from
     * @return simplified path
     */
    private String extractAttributeName(JsonParser parser) {
      return parser.getParsingContext().pathAsPointer().toString().substring(1).replace("/", ".");
    }
  };
}
