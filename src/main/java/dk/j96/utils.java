package dk.j96;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;

public abstract class utils {

    //Converts a javafx.scene.paint.Color to Hex String.
    public static String toHexString(Color color) {
        String hexColour = Integer.toHexString(color.hashCode());
        hexColour = hexColour.substring(0, hexColour.length() - 2);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }

    //Tries to generate a robot
    public static Robot initRobot() {
        try {
            return new Robot();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to create robot exiting now!");
            System.exit(0);
        }
        return null;
    }

    //Concat a number of spaces to a string
    public static String concatSpacesToString(String string, int spacesToBeConcatenated) {
        return string + "  ".repeat(Math.max(0, spacesToBeConcatenated));
    }

    //Creates a Rect with Color on specific GraphicsContext
    public static void paintRectWithColor(int x, int y, int w, int h, Color color, GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x, y, w, h);
    }

}
