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
import net.io_0.property.PropertyIssue;
import net.io_0.property.PropertyIssues;
import java.io.IOException;

/**
 * Instead of stopping deserialization on the first problem this module collects failure messages as issues.
 * Fields with deserialization problems / exceptions will be set to null.
 * The collecting issue container has to be provided via attribute.
 *
 * Implementation example:
 *     PropertyIssues propertyIssues = PropertyIssues.of();
 *
 *     Pet pet = objectMapper // objectMapper has PropertyIssueCollectorModule installed
 *       .reader()
 *       .forType(Pet.class)
 *       .withAttribute(PROPERTY_ISSUES_ATTR, propertyIssues)
 *       .readValue(json);
 */
@Slf4j
public class PropertyIssueCollectorModule extends Module {
  public static final String PROPERTY_ISSUES_ATTR = "propertyIssues";
  public static final String MODULE_NAME = "PropertyIssueCollectorModule";

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
    private Object addErrorAndReturnNull(DeserializationContext ctx, String msg) {
      try {
        PropertyIssues propertyIssues = (PropertyIssues) ctx.getAttribute(PROPERTY_ISSUES_ATTR);

        propertyIssues.add(PropertyIssue.of(extractAttributeName(ctx.getParser()), msg));
      } catch (ClassCastException | NullPointerException e) {
        log.error("Deserialization context attribute \"{}\" is either not set or not of type PropertyIssues.", PROPERTY_ISSUES_ATTR);
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
