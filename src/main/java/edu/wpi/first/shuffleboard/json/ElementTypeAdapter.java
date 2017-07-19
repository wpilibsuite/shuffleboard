package edu.wpi.first.shuffleboard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

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
