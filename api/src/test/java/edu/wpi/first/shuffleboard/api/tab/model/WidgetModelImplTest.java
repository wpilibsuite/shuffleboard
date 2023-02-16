package edu.wpi.first.shuffleboard.api.tab.model;

import static org.junit.jupiter.api.Assertions.assertSame;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class WidgetModelImplTest {

  @Test
  public void getGetSource() {
    DataSource<?> source = DataSource.none();
    WidgetModel widget = new WidgetModelImpl("", null, () -> source, "", Collections.emptyMap());
    assertSame(source, widget.getDataSource(), "Wrong source returned by getDataSource()");
  }
}
