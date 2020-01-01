package net.io_0.pb.mapping.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import lombok.extern.slf4j.Slf4j;
import net.io_0.pb.PropertyIssue;

import java.util.function.Consumer;

/**
 * Instead of stopping deserialization on the first problem this handler collects failure messages as issues.
 * Fields with deserialization problems / exceptions will be set to null.
 */
@Slf4j
public class PropertyIssueCollectingDeserializationProblemHandler extends DeserializationProblemHandler {
  private final Consumer<PropertyIssue> propertyIssueConsumer;

  public PropertyIssueCollectingDeserializationProblemHandler(Consumer<PropertyIssue> propertyIssueConsumer) {
    this.propertyIssueConsumer = propertyIssueConsumer;
  }

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
  public Object handleUnexpectedToken(DeserializationContext ctx, JavaType targetType, JsonToken t, JsonParser p, String failureMsg) {
    return addErrorAndReturnNull(ctx, String.format("%s, %s", t, failureMsg));
  }

  @Override
  public Object handleMissingInstantiator(DeserializationContext ctx, Class<?> instClass, ValueInstantiator instantiator, JsonParser p, String msg) {
    return addErrorAndReturnNull(ctx, String.format("%s, %s", instantiator, msg));
  }

  /**
   * Add error to map in context and return null as value substitute
   *
   * @param ctx context to get error map from and field name
   * @param msg message to add as error
   * @return null, as value substitute
   */
  private Object addErrorAndReturnNull(DeserializationContext ctx, String msg) {
    propertyIssueConsumer.accept(PropertyIssue.of(extractAttributeName(ctx.getParser()), removeLineBreaks(msg)));

    return null;
  }

  /**
   * Json path e.g. "/zoo/1/colorEnum" to simple attribute name e.g. "zoo.1.colorEnum"
   *
   * @param parser parser to extract json path from
   * @return simplified path
   */
  private String extractAttributeName(JsonParser parser) {
    return parser.getParsingContext().pathAsPointer().toString().substring(1).replace("/", ".");
  }

  private String removeLineBreaks(String string) {
    return string.replaceAll("\\R+", "");
  }
}
