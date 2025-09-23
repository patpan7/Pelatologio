package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.JobTeam;
import org.easytech.pelatologio.models.SubJobTeam;

public class AddSubJobTeamController {
    @FXML
    private TextField nameField;
    @FXML
    public ComboBox<JobTeam> jobTeamField;

    private TextField currentTextField; // Αναφορά στο τρέχον TextField

    private Customer customer;
    private final ObservableList<JobTeam> jobTeamList = FXCollections.observableArrayList();
    private FilteredList<JobTeam> filteredJobTeams;

    // Μέθοδος για να ορίσεις τον πελάτη
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @FXML
    public void initialize() {
        // Μπορείς να κάνεις επιπλέον ρυθμίσεις εδώ αν χρειάζεται
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Αντιγραφή");
        MenuItem pasteItem = new MenuItem("Επικόλληση");
        MenuItem clearItem = new MenuItem("Εκκαθάριση");
        copyItem.setOnAction(event -> AppUtils.copyTextToClipboard(currentTextField.getText()));
        pasteItem.setOnAction(event -> AppUtils.pasteText(currentTextField));
        clearItem.setOnAction(event -> AppUtils.clearText(currentTextField));
        contextMenu.getItems().addAll(copyItem, pasteItem, clearItem);
        setupTextFieldContextMenu(nameField, contextMenu);
        loadJobTeams();

    }

    private void loadJobTeams() {
        jobTeamList.clear();
        jobTeamList.addAll(DBHelper.getJobTeamDao().getJobTeams());
//        tfRecommendation.setItems(recommendationList);
        filteredJobTeams = new FilteredList<>(jobTeamList);
        jobTeamField.setItems(filteredJobTeams);
    }

    // Μέθοδος για να αναθέτει το contextMenu και να αποθηκεύει το ενεργό TextField
    private void setupTextFieldContextMenu(TextField textField, ContextMenu contextMenu) {
        textField.setContextMenu(contextMenu);
        textField.setOnContextMenuRequested(e -> currentTextField = textField);
    }


    // Μέθοδος για την αποθήκευση του νέου login
    @FXML
    public void handleSave(ActionEvent event) {
        String name = nameField.getText();
        int jobTeamId = jobTeamField.getSelectionModel().getSelectedItem().getId();
        System.out.println("jobteamid " + jobTeamId);
        SubJobTeam newSubJobTeam = new SubJobTeam(0, name, jobTeamId);
        if (!name.isEmpty()) {
            DBHelper.getSubJobTeamDao().saveSubJobTeam(newSubJobTeam); // Υποθέτοντας ότι έχεις αυτή τη μέθοδο στον DBHelper

            // Κλείσιμο του διαλόγου
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("Η ομάδα προστέθηκε επιτυχώς!");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

            // Καθαρισμός των πεδίων
            nameField.clear();
        } else {
            // Μήνυμα σφάλματος αν κάποια πεδία είναι κενά
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("Παρακαλώ συμπληρώστε όλα τα πεδία!");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();
        }
    }
}
