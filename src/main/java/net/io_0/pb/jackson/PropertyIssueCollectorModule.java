package net.io_0.pb.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import lombok.extern.slf4j.Slf4j;
import net.io_0.pb.PropertyIssue;
import java.util.function.Consumer;

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
 *       .withAttribute(PROPERTY_ISSUE_CONSUMER_ATTR, propertyIssues::add)
 *       .readValue(json);
 */
@Slf4j
public class PropertyIssueCollectorModule extends Module {
  public static final String PROPERTY_ISSUE_CONSUMER_ATTR = "propertyIssueConsumer";
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
    public Object handleWeirdStringValue(DeserializationContext ctx, Class<?> targetType, String valueToConvert, String failureMsg) {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", valueToConvert, failureMsg));
    }

    @Override
    public Object handleInstantiationProblem(DeserializationContext ctx, Class<?> instClass, Object argument, Throwable t) {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", argument, t.getMessage()));
    }

    @Override
    public Object handleWeirdKey(DeserializationContext ctx, Class<?> rawKeyType, String keyValue, String failureMsg) {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", keyValue, failureMsg));
    }

    @Override
    public Object handleWeirdNumberValue(DeserializationContext ctx, Class<?> targetType, Number valueToConvert, String failureMsg) {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", valueToConvert, failureMsg));
    }

    @Override
    public Object handleWeirdNativeValue(DeserializationContext ctx, JavaType targetType, Object valueToConvert, JsonParser p) {
      return addErrorAndReturnNull(ctx, String.format("%s, doesn't fit %s", valueToConvert, targetType));
    }

    @Override
    public Object handleUnexpectedToken(DeserializationContext ctx, JavaType targetType, JsonToken t, JsonParser p, String failureMsg) {
      return addErrorAndReturnNull(ctx, String.format("%s, %s", t, failureMsg));
    }

    @Override
    public Object handleMissingInstantiator(DeserializationContext ctx, Class<?> instClass, ValueInstantiator instantiator, JsonParser p, String msg) {
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
      ((Consumer<PropertyIssue>) ctx.getAttribute(PROPERTY_ISSUE_CONSUMER_ATTR))
        .accept(PropertyIssue.of(extractAttributeName(ctx.getParser()), msg));

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
