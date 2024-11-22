package inkball;

import processing.core.PApplet;
import processing.event.MouseEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.LinkedList;
import org.junit.jupiter.api.BeforeAll;

public class SampleTest {
    private static App app;

    @BeforeAll
    public static void setUp(){
        app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.setup();
        app.delay(1000); 
    }
    
    @Test
    public void testMousePressLeft() {
        app.mouseX = 100;
        app.mouseY = 100;
        app.mouseButton = 37;
        assertNull(app.currentShape, String.format("%s", app.currentShape));
        app.mousePressed(new MouseEvent(app, 0, MouseEvent.PRESS, 0, app.mouseX, app.mouseY, app.mouseButton, 1)); // 37 left, 39 right
        assertNotNull(app.currentShape, String.format("%s", app.currentShape));
    }

    @Test
    public void testMousePressRight() {
        app.mouseX = 100;
        app.mouseY = 100;
        app.mouseButton = 39;
        assertNull(app.currentShape, String.format("%s", app.currentShape));
        app.mousePressed(new MouseEvent(app, 0, MouseEvent.PRESS, 0, app.mouseX, app.mouseY, app.mouseButton, 1)); // 37 left, 39 right
        assertNull(app.currentShape, String.format("%s", app.currentShape));
    }


}

// gradle run						Run the program
// gradle test						Run the testcases

// Please ensure you leave comments in your testcases explaining what the testcase is testing.
// Your mark will be based off the average of branches and instructions code coverage.
// To run the testcases and generate the jacoco code coverage report: 
// gradle test jacocoTestReport
