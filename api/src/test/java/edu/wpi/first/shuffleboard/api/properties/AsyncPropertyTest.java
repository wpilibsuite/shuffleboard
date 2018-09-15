package edu.wpi.first.shuffleboard.api.properties;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UI")
public class AsyncPropertyTest extends ApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    // Setup FxToolkit
  }

  @Test
  public void listenerAddedTwiceIsCalledTest() {
    AsyncProperty<String> asyncProperty = new AsyncProperty<>();
    CompletableFuture<Boolean> listenerFired = new CompletableFuture<>();
    ChangeListener<String> listener = (observable, oldValue, newValue) -> listenerFired.complete(true);

    asyncProperty.addListener(listener);
    asyncProperty.addListener(listener);
    asyncProperty.set("Value");
    WaitForAsyncUtils.waitForFxEvents();

    assertTrue(listenerFired.getNow(false));
  }

  @Test
  public void removeListenerTest() {
    AsyncProperty<String> asyncProperty = new AsyncProperty<>();
    CompletableFuture<Boolean> listenerFired = new CompletableFuture<>();
    ChangeListener<String> listener = (observable, oldValue, newValue) -> listenerFired.complete(true);

    asyncProperty.addListener(listener);
    asyncProperty.removeListener(listener);
    asyncProperty.set("Value");
    WaitForAsyncUtils.waitForFxEvents();

    assertThrows(TimeoutException.class, () -> listenerFired.get(1, TimeUnit.SECONDS));
  }

  @Test
  public void listenerIsRunOnFxThreadTest() {
    AsyncProperty<String> asyncProperty = new AsyncProperty<>();
    SimpleObjectProperty<String> boundProperty = new SimpleObjectProperty<>();
    asyncProperty.bind(boundProperty);

    CompletableFuture<Boolean> listenerActionThread = new CompletableFuture<>();
    asyncProperty.addListener((observable, oldValue, newValue)
        -> listenerActionThread.complete(Platform.isFxApplicationThread()));
    boundProperty.set("Test");

    assertTimeoutPreemptively(Duration.ofSeconds(3),
        () -> assertTrue(listenerActionThread.get(), "Listener was not run on JavaFX Thread"));
  }

  @Nested
  @Tag("UI")
  public class SetTest extends ApplicationTest {

    private Label label;
    private CompletableFuture<Throwable> exceptionThrown;

    @Override
    public void start(Stage stage) throws Exception {
      label = new Label();
      stage.setScene(new Scene(label));
      stage.show();

      exceptionThrown = new CompletableFuture<>();
      Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> exceptionThrown.complete(throwable));
    }

    @Test
    public void setRunsOnFxThreadTest() throws Throwable {
      AsyncProperty<String> property = new AsyncProperty<>();
      Platform.runLater(() -> label.textProperty().bind(property));
      WaitForAsyncUtils.waitForFxEvents();

      property.set("Test");
      WaitForAsyncUtils.waitForFxEvents();

      assertFalse(exceptionThrown.isDone(), exceptionThrown.getNow(new Throwable()).getMessage());
    }
  }

  @Nested
  public class ConstructorTest {

    @Test
    public void initialValueConstructorTest() {
      AsyncProperty<String> asyncProperty = new AsyncProperty<>("Default");
      assertEquals("Default", asyncProperty.getValue());
    }

    @Test
    public void beanNameConstructorBeanSetTest() {
      Object bean = new Object();
      AsyncProperty<String> asyncProperty = new AsyncProperty<>(bean, "");

      assertEquals(bean, asyncProperty.getBean());
    }

    @Test
    public void beanNameConstructorNameSetTest() {
      AsyncProperty<String> asyncProperty = new AsyncProperty<>(null, "aName");

      assertEquals("aName", asyncProperty.getName());
    }

    @Test
    public void beanNameValueConstructorBeanSetTest() {
      Object bean = new Object();
      AsyncProperty<String> asyncProperty = new AsyncProperty<>(bean, "", "");

      assertEquals(bean, asyncProperty.getBean());
    }

    @Test
    public void beanNameValueConstructorNameSetTest() {
      AsyncProperty<String> asyncProperty = new AsyncProperty<>(null, "myName", "");

      assertEquals("myName", asyncProperty.getName());
    }

    @Test
    public void beanNameValueConstructorValueSetTest() {
      AsyncProperty<String> asyncProperty = new AsyncProperty<>(null, "", "testValue");

      assertEquals("testValue", asyncProperty.getValue());
    }

  }

}
