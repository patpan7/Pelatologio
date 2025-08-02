package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.easytech.pelatologio.models.JobTeam;
import org.easytech.pelatologio.models.SubJobTeam;

public class EditSubJobTeamController {
    @FXML
    private TextField nameField;
    @FXML
    public ComboBox<JobTeam> jobTeamField;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField


    private SubJobTeam subJobTeam;
    private ObservableList<JobTeam> jobTeamList = FXCollections.observableArrayList();
    private FilteredList<JobTeam> filteredJobTeams;

    @FXML
    public void initialize() {
        // Μπορείς να κάνεις επιπλέον ρυθμίσεις εδώ αν χρειάζεται
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);
        setupTextFieldContextMenu(nameField, contextMenu);


        copyItem.setOnAction(e -> copyText());
        pasteItem.setOnAction(e -> pasteText());
        clearItem.setOnAction(e -> clearText());
    }

    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }

    // Μέθοδοι για τις ενέργειες
    private void copyText() {
        if (currentTextField != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(currentTextField.getText());  // Replace with the desired text
            clipboard.setContent(content);
        }
    }

    private void pasteText() {
        if (currentTextField != null) {
            currentTextField.paste();
        }
    }

    private void clearText() {
        if (currentTextField != null) {
            currentTextField.clear();
        }
    }

    // Μέθοδος για να ρυθμίσεις το login προς επεξεργασία
    public void setJobTeam(SubJobTeam subJobTeam) {
        this.subJobTeam = subJobTeam;
        nameField.setText(subJobTeam.getName());

    }

    // Επιστρέφει το επεξεργασμένο login
    public SubJobTeam getUpdatedJobTeam() {
        subJobTeam.setName(nameField.getText());
        subJobTeam.setJobTeamId(jobTeamField.getSelectionModel().getSelectedItem().getId());
        return subJobTeam;
    }
}
