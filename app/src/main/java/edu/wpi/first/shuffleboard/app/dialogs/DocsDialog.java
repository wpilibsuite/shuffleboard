package edu.wpi.first.shuffleboard.app.dialogs;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dialog for navigating users to the Shuffleboard tutorials page.
 */
public class DocsDialog {

    private static final Logger log = Logger.getLogger(DocsDialog.class.getName());

    /**
     * Opens the users default browser and navigates to the WPILib Shuffleboard page.
     */
    public void show() {
        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI("https://docs.wpilib.org/en/latest/docs/software/wpilib-tools/shuffleboard/index.html"));
            } catch (IOException | URISyntaxException e) {
                log.log(Level.WARNING, "Could not open users default browser!", e);
            }
        }
    }

}
