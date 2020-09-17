package dk.j96;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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

import java.util.ArrayList;
import java.util.List;

public class Controller {

    private static Robot robot;
    @FXML
    public VBox ColorSpaceVBox;
    @FXML
    public Label lblResponse;
    @FXML
    public CheckBox chkAddAllColors;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    LimitedTextField txtX, txtY, txtInt, txtHex, txtAlpha, txtOffsetX, txtOffsetY;
    @FXML
    Canvas canvasBig, canvasSmall;
    private final List<Color> ColorBank = new ArrayList<>();

    private Color currentColor;

    private GraphicsContext gcCanvasSmall, gcCanvasBig;

    private int x, y, offsetX, offsetY;

    private static Image image;

    private static Rectangle2D screenSize;

    //Gets the color from Location
    private static Color getPixelColor(int x, int y) {
        if (!(image == null))
        return image.getPixelReader().getColor(x,y);
        return new Color(1,1,1,1);
    }

    @FXML
    private void btnApplyOffset() {
        offsetX = Integer.parseInt(txtOffsetX.getText());
        offsetY = Integer.parseInt(txtOffsetY.getText());
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
        deleteListButton.setOnAction(e -> deleteAllColors());
        Button copyToJavaListButton = new Button();
        copyToJavaListButton.setText("Copy Java List");
        copyToJavaListButton.setOnAction(e -> makeJavaPrintOutToClipboard());
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
            colorList.append("Color.web(\"").append(utils.toHexString(color)).append("\"),").append(System.lineSeparator());
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
        gcCanvasBig.clearRect(0, 0, 500, 500);
        gcCanvasSmall.clearRect(0, 0, 80, 80);

        //loop that the paints the big canvas
        int zoom = 100;
        for (int i = 0; i < zoom; i++)
            for (int j = 0; j < zoom; j++) {
                if (!((offsetX + x) + i - 25 < 0 || (offsetY + y) + j - 25 < 0 || (offsetX + x) + i - 25 > screenSize.getWidth() - 1 || (offsetY + y) + j - 25 > screenSize.getHeight() - 1))
                    utils.paintRectWithColor(i * 10, j * 10, 8, 8, getPixelColor((offsetX + x) + i - 25, (offsetY + y) + j - 25), gcCanvasBig);
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
        txtHex.setText("" + utils.toHexString(currentColor));
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

        String alpha = utils.concatSpacesToString("" + currentColor.getOpacity(), 3 - ("" + currentColor.getOpacity()).length());
        String hex = utils.concatSpacesToString("" + utils.toHexString(currentColor), 10 - ("" + utils.toHexString(currentColor)).length());

        hBox.getChildren().addAll(button,
                new Label("" + alpha),
                new Label("" + hex));
        hBox.setSpacing(40);
        ColorSpaceVBox.getChildren().add(hBox);

        lblResponse.setText("Added Color: " + utils.toHexString(currentColor));
    }
}

