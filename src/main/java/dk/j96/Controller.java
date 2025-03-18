package dk.j96;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Screen;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

public class Controller {

    private static Robot robot;
    @FXML public VBox ColorSpaceVBox;
    @FXML public Label lblResponse;
    @FXML public CheckBox chkAddAllColors;
    @FXML public AnchorPane anchorPane;
    @FXML LimitedTextField txtX, txtY, txtInt, txtHex, txtAlpha, txtOffsetX, txtOffsetY;
    @FXML Canvas canvasBig, canvasSmall;
    @FXML private Slider zoomSlider;
    private final String[] zoomLevels = {"1x", "4x", "25x", "100x", "400x", "10000x"};
    private final int[] zoomValues = {500, 250, 100, 50, 25, 5}; // Mapped values
    private final List<Color> ColorBank = new ArrayList<>();

    private Color currentColor;

    private GraphicsContext gcCanvasSmall, gcCanvasBig;

    private int x, y, offsetX, offsetY;

    private static Image image;

    private static Rectangle2D screenSize;

    private int pixels_per_side = 500;
    private final int canvas_size = 500;
    private int pixel_size = canvas_size / pixels_per_side;



    //Gets the color from Location
    private static Color getPixelColor(int x, int y) {
        if (!(image == null))
            try {
                return image.getPixelReader().getColor(x,y);
            } catch (IndexOutOfBoundsException e) {
                return new Color(1,1,1,1);
            }
        return new Color(1,1,1,1);
    }

    @FXML
    private void btnApplyOffset() {
        if (txtOffsetX.getText().isEmpty())
            offsetX = 0;
        else offsetX = Integer.parseInt(txtOffsetX.getText());
        if (txtOffsetY.getText().isEmpty())
            offsetY = 0;
        else offsetY = Integer.parseInt(txtOffsetY.getText());
        render();
    }

    //Updates the screen by taking a new screenshot
    @FXML
    private void btnUpdateScreen() {
        image = getScreenCapture();
        render();
    }

    private Image getScreenCapture() {
        image = robot.getScreenCapture(null, new Rectangle2D(0, 0, screenSize.getWidth(), screenSize.getHeight()));
        return image;
    }

    @FXML
    private void btnJump() {
        //Interpret the value from the textField and force it inside the limits of the screen.
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
        int mouse_x = (int) mouseEvent.getX();
        int mouse_y = (int) mouseEvent.getY();

        int newX = (mouse_x / pixel_size + x - pixels_per_side/2);
        int newY = (mouse_y / pixel_size + y - pixels_per_side/2);
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
        alert.setContentText("https://github.com/Jonasmadsen/AdvancedPixelInspector");
        alert.showAndWait();
    }

    //Initial Function only run once.
    public void initialize() {

        //Try to create a robot.
        robot = utils.initRobot();

        //Gets the screenSize.
        screenSize = Screen.getPrimary().getBounds();

        //We take a starting screenshot.
        image = getScreenCapture();

        //Create the topBar of the Vbox
        createTopBarColorSpace();

        //Getting the GraphicsContext from the canvas
        gcCanvasSmall = canvasSmall.getGraphicsContext2D();
        gcCanvasBig = canvasBig.getGraphicsContext2D();

        //The offset defaults to (0,0)
        offsetX = 0;
        offsetY = 0;
        setLocation(0, 0);

        zoomSlider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double value) {
                return zoomLevels[value.intValue()];
            }

            @Override
            public Double fromString(String string) {
                for (int i = 0; i < zoomLevels.length; i++) {
                    if (zoomLevels[i].equals(string)) {
                        return (double) i;
                    }
                }
                return 0.0; // Default case
            }
        });

        // Update zoomValue when slider changes
        zoomSlider.valueProperty().addListener((_, _, newVal) -> {
            if (pixels_per_side != zoomValues[newVal.intValue()]) {
                pixels_per_side = zoomValues[newVal.intValue()];
                pixel_size = canvas_size / pixels_per_side;
                render();
            }
        });

        //We can now do the first render
        render();
    }

    //Sets a custom Location
    private void setLocation(int newX, int newY) {

        //Force it within screenSize
        if (newX > screenSize.getWidth() - 1) {
            newX = (int) (screenSize.getWidth() - 1);
            txtX.setText("" + newX);
        }
        if (newY > screenSize.getHeight() - 1) {
            newY = (int) (screenSize.getHeight() - 1);
            txtY.setText("" + newY);
        }
        if (newX < 0) {
            newX = 0;
            txtX.setText("" + newX);
        }
        if (newY < 0) {
            newY = 0;
            txtY.setText("" + newY);
        }

        txtX.setText("" + newX);
        txtY.setText("" + newY);
        x = Integer.parseInt(txtX.getText());
        y = Integer.parseInt(txtY.getText());
        render();
    }

    //Creates the topBar in the ColorSpace in the GUI
    private void createTopBarColorSpace() {
        HBox hBox = new HBox();
        Button deleteListButton = new Button();
        deleteListButton.setText("  Delete All  ");
        deleteListButton.setOnAction(_ -> deleteAllColors());
        Button copyToJavaListButton = new Button();
        copyToJavaListButton.setText("Copy Java List");
        copyToJavaListButton.setOnAction(_ -> makeJavaPrintOutToClipboard());
        hBox.getChildren().addAll(deleteListButton, new Label("alpha"), new Label("hex"), copyToJavaListButton);
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
            colorList.append("new GlobalPoint(Color.web(\"").append(utils.toHexString(color)).append("\")),").append(System.lineSeparator());
        }

        colorList.setCharAt(colorList.length() - 2, ' ');

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(colorList.toString());
        clipboard.setContent(content);
    }

    //Renders the graphics.
    private void render() {

        //Reset the response text.
        lblResponse.setText("");

        //Clear the 2 canvas
        gcCanvasBig.clearRect(0, 0, canvas_size, canvas_size);
        gcCanvasSmall.clearRect(0, 0, 80, 80);

        //loop that the paints the big canvas
        for (int i = 0; i < pixels_per_side; i++)
            for (int j = 0; j < pixels_per_side; j++){
                int pixel_x = i * pixel_size;
                int pixel_y = j * pixel_size;
                int pixel_draw = pixel_size;
                if (pixels_per_side < 101)
                    pixel_draw -= 1;
                if (pixels_per_side < 26)
                    pixel_draw -= 1;
                Color pixel_color = getPixelColor((offsetX + x) + i - pixels_per_side / 2, (offsetY + y) + j - pixels_per_side / 2);
                utils.paintRectWithColor(pixel_x, pixel_y, pixel_draw, pixel_draw, pixel_color, gcCanvasBig);
            }





        //Current pixel color found
        currentColor = getPixelColor(offsetX + x, offsetY + y);

        //AddAllColors checkMark is checked we add this color.
        if (chkAddAllColors.isSelected()) {
            addCurrentColor();
        }

        //Current Pixel enlarged on Big canvas
        gcCanvasBig.setFill(currentColor);
        gcCanvasBig.fillRect(246, 246, 18, 18);

        //Information extracted
        txtInt.setText("" + currentColor.hashCode());
        txtHex.setText(utils.toHexString(currentColor));
        txtAlpha.setText("" + currentColor.getOpacity());

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

        //We make a new HBox to be placed in the ColorSpaceVbox
        HBox hBox = new HBox();
        Button button = new Button();
        button.setStyle("-fx-background-color: " + utils.toHexString(currentColor));
        button.setText("Delete Color");

        //Defines the function that removes the color when Delete Button is clicked.
        button.setOnMouseClicked(_ -> {
            for (Node node : ColorSpaceVBox.getChildren()) {
                if (node.getClass() == HBox.class) {
                    HBox hBox1 = (HBox) node;
                    for (Node node1 : hBox1.getChildren()) {
                        if (node1.getClass() == Button.class) {
                            Button button1 = (Button) node1;
                            if (button1 == button) {
                                ColorSpaceVBox.getChildren().remove(hBox1);
                                ColorBank.remove(currentColor);
                                //We have to return if we don't we will try to iterate through the NOW deleted hBox!
                                return;
                            }
                        }
                    }
                }
            }
        });

        String alpha = utils.concatSpacesToString("" + currentColor.getOpacity(), 3 - ("" + currentColor.getOpacity()).length());
        String hex = utils.concatSpacesToString(utils.toHexString(currentColor), 10 - (utils.toHexString(currentColor)).length());

        hBox.getChildren().addAll(button,
                new Label(alpha),
                new Label(hex));
        hBox.setSpacing(40);
        ColorSpaceVBox.getChildren().add(hBox);

        lblResponse.setText("Added Color: " + utils.toHexString(currentColor));
    }
}

