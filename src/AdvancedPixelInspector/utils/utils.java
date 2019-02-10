package AdvancedPixelInspector.utils;

import javafx.scene.canvas.GraphicsContext;

import java.awt.*;

public abstract class utils {

    //Converts a javafx.scene.paint.Color to Hex String.
    public static String toHexString(Color color) {
        String hexColour = Integer.toHexString(color.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }

    //Converts a java.awt.Color to javafx.scene.paint.Color.
    public static javafx.scene.paint.Color awtColortofxColor(Color color) {
        return javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 255.0);
    }

    //Tries to generate a robot
    public static Robot initRobot() {
        try {
            return new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            System.out.println("Failed to create robot exiting now!");
            System.exit(0);
        }
        return null;
    }

    //Concat a number of spaces to a string
    public static String concatSpacesToString(String string, int spacesToBeConcatinated) {
        StringBuilder rgbBuilder = new StringBuilder(string);
        for (int i = spacesToBeConcatinated; i > 0; i--) {
            rgbBuilder.append("  ");
        }
        return rgbBuilder.toString();
    }

    //Creates a Rect with Color on specific GraphicsContext
    public static void paintRectWithColor(int x, int y, int w, int h, Color color, GraphicsContext gc) {
        gc.setFill(utils.awtColortofxColor(color));
        gc.fillRect(x, y, w, h);
    }

}
