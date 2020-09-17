package dk.j96;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.VK_PRINTSCREEN;
import static java.awt.event.KeyEvent.VK_SHIFT;

public class Controller {

    private static Robot robot;
    private static Dimension screenSize;
    private static BufferedImage image;
    @FXML
    public VBox ColorSpaceVBox;
    @FXML
    public Label lblResponse;
    @FXML
    public CheckBox chkAddAllColors;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    LimitedTextField txtX, txtY, txtInt, txtRGB, txtAlpha, txtOffsetX, txtOffsetY;
    @FXML
    Canvas canvasBig, canvasSmall;
    private final List<Color> ColorBank = new ArrayList<>();

    private Color currentColor;

    private GraphicsContext gcCanvasSmall, gcCanvasBig;

    private int x, y, offsetx, offsety;

    //Gets the color from Location
    private static int getPixelColor(int x, int y) {
        return image.getRGB(x, y);
    }

    @FXML
    private void btnApplyOffset() {
        offsetx = Integer.parseInt(txtOffsetX.getText());
        offsety = Integer.parseInt(txtOffsetY.getText());
        render();
    }

    //Updates the screen by taking a new screenshot
    @FXML
    private void btnUpdateScreen() {
        image = getScreenCapture();
        render();
    }

    private BufferedImage getScreenCapture() {
        robot.keyPress(VK_SHIFT);
        robot.delay(40);
        robot.keyPress(VK_PRINTSCREEN);
        robot.delay(40);
        robot.keyRelease(VK_PRINTSCREEN);
        robot.delay(40);
        robot.keyRelease(VK_SHIFT);
        robot.delay(40);

        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            return (BufferedImage) (Image) transferable.getTransferData(DataFlavor.imageFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            //We try again
            return getScreenCapture();
        }
        //TODO: Enable when bug is fixed?? or security is no longer needed.
        //robot.createScreenCapture(new Rectangle(0, 0, screenSize.width, screenSize.height));
    }

    @FXML
    private void btnJump() {
        //Interpret the value from the textfield and force it inside the limits of the screen.
        if (txtX.getText().isEmpty())
            x = 0;
        else x = Integer.parseInt(txtX.getText());
        if (txtY.getText().isEmpty())
            y = 0;
        else y = Integer.parseInt(txtY.getText());
        setLocation(x, y);
        render();
    }

    @FXML
    public void bigCanvasClickedWithMouse(MouseEvent mouseEvent) {
        if (mouseEvent == null) {
            return;
        }
        int newX = (int) mouseEvent.getX() / 10 - 25 + x;
        int newY = (int) mouseEvent.getY() / 10 - 25 + y;
        setLocation(newX, newY);
    }

    @FXML
    void panePressedWithKey(KeyEvent event) {
        switch (event.getCode()) {
            case LEFT:
            case KP_LEFT:
                btnLeft();
                break;
            case RIGHT:
            case KP_RIGHT:
                btnRight();
                break;
            case DOWN:
            case KP_DOWN:
                btnDown();
                break;
            case UP:
            case KP_UP:
                btnUp();
                break;
            default:
                break;
        }
    }

    @FXML
    private void btnUp() {
        setLocation(x, y - 1);
    }

    @FXML
    private void btnLeft() {
        setLocation(x - 1, y);
    }

    @FXML
    private void btnRight() {
        setLocation(x + 1, y);
    }

    @FXML
    private void btnDown() {
        setLocation(x, y + 1);
    }

    @FXML
    public void btnAbout() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("About");
        alert.setHeaderText("For License And Source Code See:");
        alert.setContentText("https://github.com/Jonasmadsen/PixelColorGetter");
        alert.showAndWait();
    }

    //Initial Function only run once.
    public void initialize() {

        //Try to create a robot.
        robot = utils.initRobot();

        //Gets the screenSize.
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        //We take a starting screenshot.
        image = getScreenCapture();

        //Create the topbar of the Vbox
        createTopBarColorSpace();

        //Getting the GraphicsContext from the canvas
        gcCanvasSmall = canvasSmall.getGraphicsContext2D();
        gcCanvasBig = canvasBig.getGraphicsContext2D();

        //The offset starts at (0,0)
        offsetx = 0;
        offsety = 0;

        //Initially we start at (0,0)
        setLocation(0, 0);

        //We can now do the first render
        render();
    }

    //Sets a custom Location
    private void setLocation(int newx, int newy) {

        //Force it within screenSize
        if (newx > screenSize.width - 1) {
            newx = screenSize.width - 1;
            txtX.setText("" + newx);
        }
        if (newy > screenSize.height - 1) {
            newy = screenSize.height - 1;
            txtY.setText("" + newy);
        }
        if (newx < 0) {
            newx = 0;
            txtX.setText("" + newx);
        }
        if (newy < 0) {
            newy = 0;
            txtY.setText("" + newy);
        }

        txtX.setText("" + newx);
        txtY.setText("" + newy);
        x = Integer.parseInt(txtX.getText());
        y = Integer.parseInt(txtY.getText());
        render();
    }

    //Creates the topbar in the ColorSpace in the GUI
    private void createTopBarColorSpace() {
        HBox hBox = new HBox();
        Button deleteListButton = new Button();
        deleteListButton.setText("  Delete All  ");
        deleteListButton.setOnAction(e -> deleteAllColors());
        Button copyToJavaListButton = new Button();
        copyToJavaListButton.setText("Copy Java List");
        copyToJavaListButton.setOnAction(e -> makeJavaPrintOutToClipboard());
        hBox.getChildren().addAll(deleteListButton, new Label("      int         "), new Label("         rgb      "), new Label("   alpha"), new Label("hex"), copyToJavaListButton);
        hBox.setSpacing(50);
        ColorSpaceVBox.getChildren().add(hBox);
    }

    //Deletes all the collected colors
    private void deleteAllColors() {
        ColorBank.clear();
        ColorSpaceVBox.getChildren().clear();
        createTopBarColorSpace();
    }

    //Copies the colors collected to the Clipboard in Java list 'Style'
    private void makeJavaPrintOutToClipboard() {
        StringBuilder colorList = new StringBuilder();

        for (Color color : ColorBank) {
            colorList.append("new Color(").append(color.getRGB()).append("),").append(System.lineSeparator());
        }

        colorList.setCharAt(colorList.length() - 2, ' ');

        StringSelection selection = new StringSelection(colorList.toString());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    //Renders the graphics.
    private void render() {

        //Reset the response text.
        lblResponse.setText("");

        //Clear the 2 canvas
        gcCanvasBig.clearRect(0, 0, 500, 500);
        gcCanvasSmall.clearRect(0, 0, 80, 80);

        //loop that the paints the big canvas
        int zoom = 100;
        for (int i = 0; i < zoom; i++)
            for (int j = 0; j < zoom; j++) {
                if (!((offsetx + x) + i - 25 < 0 || (offsety + y) + j - 25 < 0 || (offsetx + x) + i - 25 > screenSize.width - 1 || (offsety + y) + j - 25 > screenSize.height - 1))
                    utils.paintRectWithColor(i * 10, j * 10, 8, 8, new Color(getPixelColor((offsetx + x) + i - 25, (offsety + y) + j - 25)), gcCanvasBig);
            }

        //Current pixel color found
        currentColor = new Color(getPixelColor(offsetx + x, offsety + y));

        //AddAllColors checkMark is checked we add this color.
        if (chkAddAllColors.isSelected()) {
            addCurrentColor();
        }

        //Current Pixel enlarged on Big canvas
        gcCanvasBig.setFill(utils.awtColortofxColor(currentColor));
        gcCanvasBig.fillRect(246, 246, 18, 18);

        //Information extracted
        txtInt.setText("" + currentColor.getRGB());
        txtRGB.setText("R: " + currentColor.getRed() + " G: " + currentColor.getGreen() + " B: " + currentColor.getBlue());
        txtAlpha.setText("" + currentColor.getAlpha());

        //Paint the small canvas with current color
        utils.paintRectWithColor(0, 0, 80, 80, Color.BLACK, gcCanvasSmall);
        utils.paintRectWithColor(4, 4, 72, 72, currentColor, gcCanvasSmall);
    }

    //Steals back the focus from the Tabs!
    public void paneClickedWithMouse(MouseEvent mouseEvent) {
        System.out.println("User wanted focus back." + mouseEvent.toString());
        canvasBig.requestFocus();
    }

    //Tries to add the currentColor
    public void addCurrentColor() {
        if (ColorBank.contains(currentColor)) {
            lblResponse.setText("Color is already in the set.");
            return;
        }

        if (currentColor == null) {
            lblResponse.setText("Color is null??");
            return;
        }

        //Adds currentColor to ColorBank
        ColorBank.add(currentColor);

        //We make a new Hbox to be placed in the ColorSpaceVbox
        HBox hBox = new HBox();
        Button button = new Button();
        button.setStyle("-fx-background-color: " + utils.toHexString(currentColor));
        button.setText("Delete Color");

        //Defines the function that removes the color when Delete Button is clicked.
        button.setOnMouseClicked(t -> {
            for (Node node : ColorSpaceVBox.getChildren()) {
                if (node.getClass() == HBox.class) {
                    HBox hBox1 = (HBox) node;
                    for (Node node1 : hBox1.getChildren()) {
                        if (node1.getClass() == Button.class) {
                            Button button1 = (Button) node1;
                            if (button1 == button) {
                                ColorSpaceVBox.getChildren().remove(hBox1);
                                ColorBank.remove(currentColor);
                                //We have to return if we dont we will try to iterate through the NOW deleted hBox!
                                return;
                            }
                        }
                    }
                }
            }
        });

        String rgb = utils.concatSpacesToString("" + currentColor.getRGB(), 10 - ("" + currentColor.getRGB()).length());
        String r = utils.concatSpacesToString("" + currentColor.getRed(), 3 - ("" + currentColor.getRed()).length());
        String g = utils.concatSpacesToString("" + currentColor.getGreen(), 3 - ("" + currentColor.getGreen()).length());
        String b = utils.concatSpacesToString("" + currentColor.getBlue(), 3 - ("" + currentColor.getBlue()).length());
        String alpha = utils.concatSpacesToString("" + currentColor.getAlpha(), 3 - ("" + currentColor.getAlpha()).length());
        String hex = utils.concatSpacesToString("" + utils.toHexString(currentColor), 10 - ("" + utils.toHexString(currentColor)).length());

        hBox.getChildren().addAll(button,
                new Label("" + rgb),
                new Label("R: " + r + " G: " + g + " B: " + b),
                new Label("" + alpha),
                new Label("" + hex));
        hBox.setSpacing(40);
        ColorSpaceVBox.getChildren().add(hBox);

        lblResponse.setText("Added Color: " + currentColor.getRGB());
    }
}

