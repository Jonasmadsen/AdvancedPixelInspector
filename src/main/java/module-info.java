module AdvancedPixelInspector {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens dk.j96 to javafx.fxml;
    exports dk.j96;
}