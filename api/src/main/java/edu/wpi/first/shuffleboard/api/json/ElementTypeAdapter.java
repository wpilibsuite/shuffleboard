package edu.wpi.first.shuffleboard.api.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Serializes values to and from JSON. Implementing classes <i>must</i> be annotated with {@link AnnotatedTypeAdapter}.
 *
 * @param <T> the type of values to serialize and deserialize
 */
public interface ElementTypeAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {

  JsonElement serialize(T src, JsonSerializationContext context);

  @Override
  default JsonElement serialize(T src, Type type, JsonSerializationContext context) {
    return serialize(src, context);
  }

  T deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException;

  @Override
  default T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
    return deserialize(json, context);
  }
}
