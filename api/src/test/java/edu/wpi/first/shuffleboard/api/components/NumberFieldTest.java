package edu.wpi.first.shuffleboard.api.components;

import javafx.scene.Scene;
import javafx.stage.Stage;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("UI")
public class NumberFieldTest extends ApplicationTest {

  private NumberField numberField;

  @Override
  public void start(Stage stage) throws Exception {
    numberField = new NumberField(0.0);
    stage.setScene(new Scene(numberField));
    stage.show();
  }

  @Test
  public void numberPropertyIsUpdatedTest() {
    interact(numberField::clear);
    clickOn(".text-field").write("123.456");

    assertEquals(123.456, numberField.numberProperty().getValue().doubleValue());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0.0", "1.0", "12.56", "-0", "-1", "-10", "+0", "+55.99", "-54.21"})
  public void validNumberTest(String number) {
    interact(numberField::clear);
    clickOn(".text-field").write(number);

    assertEquals(Double.parseDouble(number), numberField.getNumber().doubleValue());
  }

  @ParameterizedTest
  @CsvSource({"0.0, a0.0",
                 "0.0, 0.0b",
                 "0.0, a0.0b",
                 "0.0, 0.......0",
                 "0.0, 0.0."})
  public void invalidNumberTest(String expectedResult, String test) {
    interact(numberField::clear);
    clickOn(".text-field").write(test);

    assertEquals(expectedResult, numberField.getText());
  }

}
