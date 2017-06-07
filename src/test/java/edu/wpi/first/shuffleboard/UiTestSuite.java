package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.NetworkTableTreeTest;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;

import static org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for all UI tests.
 */
@RunWith(Categories.class)
@SuiteClasses({MainWindowControllerTest.class,
               NetworkTableTreeTest.class})
public class UiTestSuite {
}
