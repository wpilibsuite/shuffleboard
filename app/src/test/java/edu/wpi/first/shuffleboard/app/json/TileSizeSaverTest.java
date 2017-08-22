package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import edu.wpi.first.shuffleboard.api.widget.TileSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TileSizeSaverTest {
  private Gson gson;

  @BeforeEach
  public void setUp() throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(TileSize.class, new TileSizeSaver())
        .create();
  }

  @Test
  public void serialize() throws Exception {
    String json = gson.toJson(new TileSize(1, 2));
    assertEquals("[1,2]", json);
  }

  @Test
  public void deserialize() throws Exception {
    TileSize tileSize = gson.fromJson("[1,2]", TileSize.class);
    assertEquals(tileSize, new TileSize(1, 2));
  }

  @Test
  public void deserializeInvalid() throws Exception {
    assertThrows(JsonParseException.class, () -> gson.fromJson("[1,-2]", TileSize.class));
  }

}
