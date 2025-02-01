package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DeviceController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Device> devicesTable;
    @FXML
    private TableColumn idColumn, serialColumn, itemColumn, descriptionColumn, customerColumn;

    @FXML
    private CheckBox showAllCheckbox, showWithCustomerCheckbox, showWithoutCustomerCheckbox;

    @FXML
    private ComboBox <Item> itemFilterComboBox;
    @FXML
    private Button addDeviceButton;

    private ObservableList<Device> allDevices = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serial"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        // Αρχικό γέμισμα του πίνακα
        loadDevices();


        devicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Device selectedDevice = devicesTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedDevice != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        handleEditDevice();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // Φίλτρα
        CheckBox[] checkBoxes1 = {
                showAllCheckbox,
                showWithCustomerCheckbox,
                showWithoutCustomerCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes1);

        DBHelper dbHelper = new DBHelper();
        List<Item> items = null;
        try {
            items = dbHelper.getItems();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        itemFilterComboBox.getItems().add(new Item(0,"Όλα",""));
        itemFilterComboBox.getItems().addAll(items);
        itemFilterComboBox.getSelectionModel().selectFirst();
        itemFilterComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Item item) {
                return item != null ? item.getName() : "";
            }

            @Override
            public Item fromString(String string) {
                return itemFilterComboBox.getItems().stream()
                        .filter(taskCategory -> taskCategory.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        itemFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateDevicesTable());


        showAllCheckbox.setOnAction(e -> updateDevicesTable());
        showWithCustomerCheckbox.setOnAction(e -> updateDevicesTable());
        showWithoutCustomerCheckbox.setOnAction(e -> updateDevicesTable());

        // Κουμπιά
        addDeviceButton.setOnAction(e -> handleAddDevice());
    }


    private void configureSingleSelectionCheckBoxes(CheckBox[] checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    for (CheckBox otherCheckBox : checkBoxes) {
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.setSelected(false);
                        }
                    }
                }
            });
        }
    }

    private void loadDevices() {
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allDevices.setAll(dbHelper.getAllDevices());
        updateDevicesTable();
    }

    private void updateDevicesTable() {
        // Ξεκινάμε με όλες τις εργασίες
        ObservableList<Device> filteredTasks = FXCollections.observableArrayList(allDevices);

        // Φιλτράρισμα βάσει Πελάτη
        if (!showAllCheckbox.isSelected()) {
            if (showWithCustomerCheckbox.isSelected()) {
                filteredTasks.removeIf(task -> task.getCustomerId() == 0);
            } else if (showWithoutCustomerCheckbox.isSelected()) {
                filteredTasks.removeIf(task -> task.getCustomerId() != 0);
            }
        }


        // Φιλτράρισμα βάσει κατηγορίας
        Item selectedItem = itemFilterComboBox.getValue(); // Η επιλεγμένη κατηγορία από το ComboBox
        if (selectedItem != null && selectedItem.getId() != 0) { // Εξαιρείται η κατηγορία "Όλες"
            filteredTasks.removeIf(task -> task.getItemId() != selectedItem.getId());
        }


        // Ανανεώνουμε τα δεδομένα του πίνακα
        devicesTable.setItems(filteredTasks);
    }



    private void handleAddDevice() {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addDevice.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Προσθήκη Συσκευής");
                AddDeviceController controller = loader.getController();
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, event -> {
                    // Εκτελούμε το handleSaveAppointment
                    boolean success = controller.handleSaveTask();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                });

                dialog.showAndWait();
                loadDevices();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void handleEditDevice() throws IOException {
        // Επεξεργασία επιλεγμένης εργασίας
        Device selectedDevice = devicesTable.getSelectionModel().getSelectedItem();
        if (selectedDevice == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί συσκευή!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addDevice.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία Συσκευής");
            AddDeviceController controller = loader.getController();

            // Ορισμός δεδομένων για επεξεργασία
            controller.setDeviceForEdit(selectedDevice);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveTask();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });
            dialog.showAndWait();
            loadDevices();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }

}
