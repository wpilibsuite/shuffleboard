package edu.wpi.first.shuffleboard.api.util;

import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationTest;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FxUtilsTest extends UtilityClassTest<FxUtils> {

  @Nested
  @Tag("UI")
  public class FxTests extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
      // Setup FxToolkit
    }

    @Test
    public void runOnFxThreadTest() throws InterruptedException, ExecutionException, TimeoutException {
      CompletableFuture<Boolean> isOnFxThread = new CompletableFuture<>();
      FxUtils.runOnFxThread(() -> isOnFxThread.complete(Platform.isFxApplicationThread()));

      assertTrue(isOnFxThread.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void runOnFxThreadAlreadyOnTest() throws InterruptedException, ExecutionException, TimeoutException {
      CompletableFuture<Boolean> isOnFxThread = new CompletableFuture<>();
      Platform.runLater(() -> FxUtils.runOnFxThread(() -> isOnFxThread.complete(Platform.isFxApplicationThread())));

      assertTrue(isOnFxThread.get(5, TimeUnit.SECONDS));
    }

  }

  @Test
  public void menuItemTest() {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    MenuItem menuItem = FxUtils.menuItem("Test item", event -> future.complete(true));

    menuItem.fire();
    assertTimeoutPreemptively(Duration.ofSeconds(5), future::join);
  }

  private static Stream<Arguments> toHexStringArguments() {
    return Stream.of(
        Arguments.of("#000000FF", Color.BLACK),
        Arguments.of("#FF0000FF", Color.RED),
        Arguments.of("#00FF00FF", Color.LIME),
        Arguments.of("#0000FFFF", Color.BLUE),
        Arguments.of("#FFFFFFFF", Color.WHITE),
        Arguments.of("#FFFFFF00", new Color(1.0, 1.0, 1.0, 0.0))
    );
  }

  @ParameterizedTest
  @MethodSource(value = "toHexStringArguments")
  public void toHexStringTest(String expectedResult, Color color) {
    assertEquals(expectedResult, FxUtils.toHexString(color));
  }

}
