package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Device;
import org.easytech.pelatologio.models.Item;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DeviceController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    TextField filterField;
    @FXML
    private TableView<Device> devicesTable;
    @FXML
    private TableColumn idColumn, serialColumn, itemColumn, descriptionColumn, customerColumn, rateColumn;

    @FXML
    private CheckBox showAllCheckbox, showWithCustomerCheckbox, showWithoutCustomerCheckbox;

    @FXML
    private ComboBox <Item> itemFilterComboBox;
    @FXML
    private ComboBox<String> rateFilterComboBox;
    @FXML
    private Label countLabel;
    @FXML
    private Button addDeviceButton;

    private ObservableList<Device> allDevices = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serial"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
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

        filterField.textProperty().addListener((observable, oldValue, newValue) -> updateDevicesTable());


        DBHelper dbHelper = new DBHelper();
        List<Item> items = null;
        List<String> rates = null;
        try {
            items = dbHelper.getItems();
            rates = dbHelper.getRates();
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

        rateFilterComboBox.getItems().add("Όλα");
        rateFilterComboBox.getItems().addAll(rates);
        rateFilterComboBox.getSelectionModel().selectFirst();

        //Φίλτρα
        itemFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateDevicesTable());
        rateFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateDevicesTable());

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
        List<TableColumn<Device, ?>> sortOrder = new ArrayList<>(devicesTable.getSortOrder());
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allDevices.setAll(dbHelper.getAllDevices());
        updateDevicesTable();
        devicesTable.getSortOrder().setAll(sortOrder);
    }

    private void updateDevicesTable() {
        // Ξεκινάμε με όλες τις εργασίες
        ObservableList<Device> filteredDevices  = FXCollections.observableArrayList(allDevices);

        // Φιλτράρισμα βάσει Πελάτη
        if (!showAllCheckbox.isSelected()) {
            if (showWithCustomerCheckbox.isSelected()) {
                filteredDevices.removeIf(device -> device.getCustomerId() == 0);
            } else if (showWithoutCustomerCheckbox.isSelected()) {
                filteredDevices.removeIf(device -> device.getCustomerId() != 0);
            }
        }


        // Φιλτράρισμα βάσει τύπου συσκευής
        Item selectedItem = itemFilterComboBox.getValue(); // Η επιλεγμένη κατηγορία από το ComboBox
        if (selectedItem != null && selectedItem.getId() != 0) { // Εξαιρείται η κατηγορία "Όλες"
            filteredDevices.removeIf(device -> device.getItemId() != selectedItem.getId());
        }

        // Φιλτράρισμα βάσει σειριακού αριθμού
        String serialFilter = filterField.getText().trim().toLowerCase();
        if (!serialFilter.isEmpty()) {
            filteredDevices.removeIf(device -> !device.getSerial().toLowerCase().contains(serialFilter));
        }

        // Φιλτράρισμα βάσει ποσοστού
        String selectedRate = rateFilterComboBox.getValue(); // Η επιλεγμένη κατηγορία από το ComboBox
        if (selectedRate != null && !selectedRate.equals("Όλα")) { // Εξαιρείται η κατηγορία "Όλες"
            System.out.println(selectedRate);
            filteredDevices.removeIf(device -> !device.getRate().equals(selectedRate));
        }

        // Ανανεώνουμε τα δεδομένα του πίνακα
        devicesTable.setItems(filteredDevices);
        countLabel.setText("Πλήθος: " + filteredDevices.size());
    }


    @FXML
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
                    boolean success = controller.handleSaveDevice();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                });

                dialog.showAndWait();
                loadDevices();
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));

            }
    }

    @FXML
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
                boolean success = controller.handleSaveDevice();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });
            dialog.showAndWait();
            loadDevices();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την ενημέρωση.", e.getMessage(), Alert.AlertType.ERROR));

        }
    }

    @FXML
    private void handleDeleteDevice() {
        Device selectedDevice = devicesTable.getSelectionModel().getSelectedItem();
        if (selectedDevice == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί συσκευή!");
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε την συσκευή " + selectedDevice.getSerial() + ";" );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper dbHelper = new DBHelper();
            boolean deleted = dbHelper.deleteDevice(selectedDevice.getId());
            if (deleted) {
                Platform.runLater(() -> {
                    Notifications notifications = Notifications.create()
                            .title("Επιτυχία")
                            .text("Ο σειριακός αριθμός " + selectedDevice.getSerial() + " διαγράψετε!")
                            .graphic(null)
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT);
                    notifications.showInformation();});
                loadDevices();
            }
            else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText("Η συσκευή δεν μπορεί να διαγραφεί επειδή είναι δεσμευμένη σε πελάτη!");
                alert.showAndWait();
                return;
            }
        }
    }

    public void clean(ActionEvent actionEvent) {
        filterField.setText("");
        devicesTable.getSelectionModel().clearSelection();
        filterField.requestFocus();
    }

}
