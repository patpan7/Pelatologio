package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.helper.CustomNotification;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Item;

public class AddItemController {

    @FXML
    private TextField tfName;
    @FXML
    private TextArea taDescription;

    int code = 0;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField
    private Item item;

    public void initialize() {
        // Δημιουργία του βασικού ContextMenu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);


        // Προσθήκη του contextMenu στα υπόλοιπα TextFields
        setupTextFieldContextMenu(tfName, contextMenu);

        // Ενέργειες για τα copy, paste, clear items στο βασικό contextMenu
        copyItem.setOnAction(e -> AppUtils.copyTextToClipboard(currentTextField.getText()));
        pasteItem.setOnAction(e -> AppUtils.pasteText(currentTextField));
        clearItem.setOnAction(e -> AppUtils.clearText(currentTextField));
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        // Έλεγχος για διπλό κλικ
        if (event.getClickCount() == 2) {
            openNotesDialog(taDescription.getText());
        }
    }

    private void openNotesDialog(String currentNotes) {
        // Ο κώδικας για το παράθυρο διαλόγου, όπως περιγράφεται
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Επεξεργασία Περιγραφής");

        TextArea expandedTextArea = new TextArea(currentNotes);
        expandedTextArea.setWrapText(true);
        expandedTextArea.setPrefSize(400, 300);
        expandedTextArea.setStyle("-fx-font-size: 24px;");
        if (currentNotes != null && !currentNotes.isEmpty()) {
            expandedTextArea.setText(currentNotes);
            expandedTextArea.positionCaret(currentNotes.length());
        } else {
            expandedTextArea.setText(""); // Βεβαιωθείτε ότι το TextArea είναι κενό
            expandedTextArea.positionCaret(0); // Τοποθετήστε τον κέρσορα στην αρχή
        }

        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(100);
        btnOk.setOnAction(event -> {
            taDescription.setText(expandedTextArea.getText()); // Ενημέρωση του αρχικού TextArea
            dialogStage.close();
        });

        VBox vbox = new VBox(10, expandedTextArea, btnOk);
        vbox.setAlignment(Pos.CENTER);
        //vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }


    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }

    public void setItemData(Item item) {
        // Ρύθμιση των πεδίων με τα υπάρχοντα στοιχεία του πελάτη
        tfName.setText(item.getName());
        taDescription.setText(item.getDescription());

        // Αποθήκευση του κωδικού του πελάτη για χρήση κατά την ενημέρωση
        this.code = item.getId();
        this.item = item;
    }


    public void handleOkButton() {
        if (code == 0) { // Αν δεν υπάρχει κωδικός, είναι νέα προσθήκη
            addItem();
        } else { // Αν υπάρχει, είναι ενημέρωση
            updateItem();
        }
    }

    void addItem() {
        String name = tfName.getText();
        String description = taDescription.getText();

        // Έλεγχος για ύπαρξη πελάτη με το ίδιο ΑΦΜ
        if (DBHelper.getItemDao().isItemExists(name)) {
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Προσοχή")
                        .text("Το είδος " + name + " υπάρχει ήδη.")
                        .hideAfter(Duration.seconds(3))
                        .position(Pos.TOP_RIGHT)
                        .showError();
            });
        } else {
            // Εισαγωγή του πελάτη στον κύριο πίνακα με την πρώτη διεύθυνση
            int itemId = DBHelper.getItemDao().insertItem(name, description);
            if (itemId != -1) {
                // Εμφάνιση επιτυχίας
                Platform.runLater(() -> {
                    CustomNotification.create()
                            .title("Επιτυχία")
                            .text("Το είδος εισήχθη με επιτυχία στη βάση δεδομένων.")
                            .hideAfter(Duration.seconds(3))
                            .position(Pos.TOP_RIGHT)
                            .showConfirmation();
                });
            } else {
                // Εμφάνιση σφάλματος
                Platform.runLater(() -> {
                    CustomNotification.create()
                            .title("Σφάλμα")
                            .text("Παρουσιάστηκε σφάλμα κατά την εισαγωγή του είδους στη βάση δεδομένων.")
                            .hideAfter(Duration.seconds(3))
                            .position(Pos.TOP_RIGHT)
                            .showError();
                });
            }
        }
    }

    void updateItem() {
        String name = tfName.getText();
        String description = taDescription.getText();

        DBHelper.getItemDao().updateItem(code, name, description);
        //showAlert("Επιτυχία", "Ο πελάτης ενημερώθηκε με επιτυχία στη βάση δεδομένων.");
        CustomNotification.create()
                .title("Επιτυχία")
                .text("Το είδος ενημερώθηκε με επιτυχία στη βάση δεδομένων.")
                .hideAfter(Duration.seconds(5))
                .position(Pos.TOP_RIGHT)
                .showConfirmation();
    }
}
