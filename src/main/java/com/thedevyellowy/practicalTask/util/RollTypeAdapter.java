package com.thedevyellowy.practicalTask.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class RollTypeAdapter extends TypeAdapter<ExternalLootTable.rolls> {
  @Override
  public void write(JsonWriter jsonWriter, ExternalLootTable.rolls rolls) throws IOException {
    return;
  }

  @Override
  public ExternalLootTable.rolls read(JsonReader jsonReader) throws IOException {
    JsonToken token = jsonReader.peek();

    return switch (token) {
      case NUMBER -> {
        int intValue = jsonReader.nextInt();
        yield new ExternalLootTable.rolls(intValue, -1);
      }
      case BEGIN_OBJECT -> {
        Gson gson = new Gson();
        yield gson.fromJson(jsonReader, ExternalLootTable.rolls.class);
      }
      default -> {
        jsonReader.nextNull();
        yield null;
      }
    };
  }
}
