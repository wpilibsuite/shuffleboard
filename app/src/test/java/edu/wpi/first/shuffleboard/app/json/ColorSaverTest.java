package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.paint.Color;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("UI")
public class ColorSaverTest extends ApplicationTest {

  private Gson gson;

  @Override
  public void start(Stage stage) throws Exception {
    gson = new GsonBuilder()
        .registerTypeAdapter(Color.class, new ColorSaver())
        .create();
  }

  @Test
  public void testSerialize() throws Exception {
    String colorString = "#12345678";
    Color color = Color.web(colorString);
    String json = gson.toJson(color);
    assertEquals('"' + colorString + '"', json);
  }

  @Test
  public void testDeserialize() throws Exception {
    String json = "\"#12345678\"";
    Color color = gson.fromJson(json, Color.class);
    assertEquals(Color.web("#12345678"), color);
  }

}
