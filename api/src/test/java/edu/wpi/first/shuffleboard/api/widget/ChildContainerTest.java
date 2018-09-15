package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.widget.LayoutBase.ChildContainer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("UI")
public class ChildContainerTest extends ApplicationTest {

  @Description(name = "Mock Widget", dataTypes = Object.class)
  private static final class MockWidget extends SimpleAnnotatedWidget {

    private final Pane view = new Pane();

    @Override
    public Pane getView() {
      return view;
    }
  }

  @Test
  public void testTitleUpdates() {
    ChildContainer container = new ChildContainer(null);
    MockWidget firstChild = new MockWidget();
    MockWidget secondChild = new MockWidget();
    firstChild.setTitle("First child");
    secondChild.setTitle("Second child");

    container.setChild(firstChild);
    assertEquals("First child", ((EditableLabel) container.getBottom()).getText());

    container.setChild(secondChild);
    assertEquals("Second child", ((EditableLabel) container.getBottom()).getText());
    secondChild.setTitle("Second title");
    assertEquals("Second title", ((EditableLabel) container.getBottom()).getText());
  }

  @Test
  public void testMoveLabel() {
    ChildContainer container = new ChildContainer(null);
    Node label = container.getBottom();

    container.setLabelSide(LayoutBase.LabelPosition.TOP);
    assertSame(label, container.getTop());
    assertNull(container.getLeft());
    assertNull(container.getRight());
    assertNull(container.getBottom());

    container.setLabelSide(LayoutBase.LabelPosition.LEFT);
    assertSame(label, container.getLeft());
    assertNull(container.getTop());
    assertNull(container.getRight());
    assertNull(container.getBottom());

    container.setLabelSide(LayoutBase.LabelPosition.RIGHT);
    assertSame(label, container.getRight());
    assertNull(container.getTop());
    assertNull(container.getLeft());
    assertNull(container.getBottom());

    container.setLabelSide(LayoutBase.LabelPosition.BOTTOM);
    assertSame(label, container.getBottom());
    assertNull(container.getTop());
    assertNull(container.getLeft());
    assertNull(container.getRight());

    container.setLabelSide(LayoutBase.LabelPosition.HIDDEN);
    assertNull(container.getTop());
    assertNull(container.getLeft());
    assertNull(container.getRight());
    assertNull(container.getBottom());
  }

}
