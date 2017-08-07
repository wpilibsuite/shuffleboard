package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import edu.wpi.first.shuffleboard.app.json.TileSizeSaver;
import edu.wpi.first.shuffleboard.app.widget.TileSize;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TileSizeSaverTest {
  Gson gson;

  @Before
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

  @Test(expected = JsonParseException.class)
  public void deserializeInvalid() throws Exception {
    gson.fromJson("[1,-2]", TileSize.class);
  }

}