package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.models.TaskCategory;

public class EditTaskCategoryController {
    @FXML
    private TextField nameField;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField

    private TaskCategory taskCategory;

    @FXML
    public void initialize() {
        // Μπορείς να κάνεις επιπλέον ρυθμίσεις εδώ αν χρειάζεται
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);
        setupTextFieldContextMenu(nameField, contextMenu);


        copyItem.setOnAction(e -> AppUtils.copyTextToClipboard(currentTextField.getText()));
        pasteItem.setOnAction(e -> AppUtils.pasteText(currentTextField));
        clearItem.setOnAction(e -> AppUtils.clearText(currentTextField));
    }

    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }


    // Μέθοδος για να ρυθμίσεις το login προς επεξεργασία
    public void setTaskCategory(TaskCategory calendars) {
        this.taskCategory = calendars;
        nameField.setText(calendars.getName());

    }

    // Επιστρέφει το επεξεργασμένο login
    public TaskCategory getUpdatedTaskCategory() {
        taskCategory.setName(nameField.getText());
        return taskCategory;
    }
}
