package dk.j96;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;

public class LimitedTextField extends TextField {

    private final IntegerProperty maxLength;

    public LimitedTextField() {
        super();
        this.maxLength = new SimpleIntegerProperty(-1);
    }

    //IntelliJ is Wrong it must be public to be accessed in AdvancedPixelInspector.fxml
    public final Integer getMaxLength() {
        return this.maxLength.getValue();
    }

    @Override
    public void replaceText(int start, int end, String insertedText) {
        if (this.getMaxLength() <= 0) {
            // Default behavior, in case of no max length
            super.replaceText(start, end, insertedText);
        } else {
            // Get the text in the textField, before the user enters something
            String currentText = this.getText() == null ? "" : this.getText();

            // Compute the text that should normally be in the textField now
            String finalText = currentText.substring(0, start) + insertedText + currentText.substring(end);

            // If the max length is not exceeded
            int numberExceedingCharacters = finalText.length() - this.getMaxLength();
            if (numberExceedingCharacters <= 0) {
                // Normal behavior
                super.replaceText(start, end, insertedText);
            } else {
                // Otherwise, cut the the text that was going to be inserted
                String cutInsertedText = insertedText.substring(
                        0,
                        insertedText.length() - numberExceedingCharacters
                );

                // And replace this text
                super.replaceText(start, end, cutInsertedText);
            }
        }
    }
}
