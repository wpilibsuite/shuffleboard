package edu.wpi.first.shuffleboard.app.json;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import edu.wpi.first.shuffleboard.app.Shuffleboard;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.stage.Stage;

public class BasicLoadTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // Just here so we can run on the FX thread
        new Shuffleboard().start(stage);
    }

    @Test
    public void loadBasicFile() {
        Reader reader = new InputStreamReader(
                getClass().getResourceAsStream("/simpleshuffleboard.json"),
                StandardCharsets.UTF_8);
        JsonBuilder.forSaveFile().fromJson(reader, DashboardTabPane.class);
    }
}
