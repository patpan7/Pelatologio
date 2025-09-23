package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Device;
import org.easytech.pelatologio.models.Logins;

import java.io.IOException;
import java.util.Optional;

public class CustomerDevicesController implements CustomerTabController {
    @FXML
    private Label customerLabel;

    @FXML
    private TableView<Device> devicesTable;

    @FXML
    private TableColumn<Logins, String> serialColumn;
    @FXML
    private TableColumn<Logins, String> itemColumn;

    @FXML
    private TableColumn<Logins, String> descriptionColumn;


    Customer customer;
    private Runnable onDataSaved;

    private ObservableList<Device> devicesList;

    @FXML
    public void initialize() {
        if (!Features.isEnabled("devices")) {
            devicesTable.setVisible(false);
            devicesTable.setManaged(false);
        }
        devicesList = FXCollections.observableArrayList();
        // Ρύθμιση στήλης
        serialColumn.setCellValueFactory(new PropertyValueFactory<>("serial"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        devicesTable.setItems(devicesList);

        devicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEditDevice(null);
            }
        });

    }

    // Μέθοδος για τη φόρτωση των συσκευών από τη βάση
    public void loadDevicesForCustomer(int customerId) {
        if (Features.isEnabled("devices")) {
            devicesList.clear();
            // Φέρε τα logins από τη βάση για τον συγκεκριμένο πελάτη
            // Προσθήκη των logins στη λίστα
            DBHelper dbHelper = new DBHelper();
            devicesList.addAll(DBHelper.getDeviceDao().getCustomerDevices(customerId));
        }
    }

    public void handleAddDevice(ActionEvent event) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addDevice.fxml"));
                DialogPane dialogPane = loader.load();

                AddDeviceController controller = loader.getController();
                controller.setCustomerId(customer.getCode()); // Ορίζει τον πελάτη
                controller.setCustomerName(customer.getName());
                controller.lockCustomer();
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(dialogPane);
                dialog.setTitle("Προσθήκη νέας συσκευής");
                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

                // Όταν ο χρήστης πατά το OK, θα καλέσει τη μέθοδο για αποθήκευση
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, e -> {
                    boolean success = controller.handleAssignDevice();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                    loadDevicesForCustomer(customer.getCode());
                    notifyDataSaved();
                });
                dialog.initModality(Modality.NONE);
                dialog.initOwner(null);
                dialog.show();

                dialog.setOnHidden(e -> {
                    if (dialog.getResult() == ButtonType.OK) {
                        loadDevicesForCustomer(customer.getCode());
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
            }
    }

    public void handleDeleteDevice(ActionEvent event) {
            Device selectedDevice = devicesTable.getSelectionModel().getSelectedItem();
            if (selectedDevice == null) {
                // Εμφάνιση μηνύματος αν δεν έχει επιλεγεί login
                Platform.runLater(() -> {
                    CustomNotification.create()
                            .title("Προσοχή")
                            .text("Παρακαλώ επιλέξτε συσκευή.")
                            .hideAfter(Duration.seconds(5))
                            .position(Pos.TOP_RIGHT)
                            .showWarning();
                });
                return;
            }

            // Εμφάνιση παραθύρου επιβεβαίωσης
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Επιβεβαίωση Διαγραφής");
            alert.setHeaderText(null);
            alert.setContentText("Είστε σίγουροι ότι θέλετε να επαναφέρετε την επιλεγμένη συσκευή;");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Διαγραφή από τη βάση
                if (DBHelper.getDeviceDao().recoverDevice(selectedDevice.getId())) {
                    Platform.runLater(() -> {
                        CustomNotification.create()
                                .title("Προσοχή")
                                .text("Η επαναφορά της συσκευής έγινε με επιτυχία.")
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT)
                                .showConfirmation();
                    });
                    devicesTable.getItems().remove(selectedDevice);
                    notifyDataSaved();
                } else {
                    Platform.runLater(() -> {
                        CustomNotification.create()
                                .title("Προσοχή")
                                .text("Η επαναφορά της συσκευής δεν έγινε.")
                                .hideAfter(Duration.seconds(5))
                                .position(Pos.TOP_RIGHT)
                                .showError();
                    });
                }
            }
    }

    public void handleEditDevice(ActionEvent event) {
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
                controller.lock();
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, e -> {
                    // Εκτελούμε το handleSaveAppointment
                    boolean success = controller.handleSaveDevice();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                    loadDevicesForCustomer(customer.getCode());
                    notifyDataSaved();
                });
                dialog.initModality(Modality.NONE);
                dialog.initOwner(null);
                dialog.show();

            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
            }
    }

    public void handleAddTask(ActionEvent evt) {
            Device selectedDevice = devicesTable.getSelectionModel().getSelectedItem();
            if (selectedDevice == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Προσοχή");
                alert.setContentText("Δεν έχει επιλεγεί συσκευή!");
                Optional<ButtonType> result = alert.showAndWait();
                return;
            }
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addTask.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Προσθήκη Εργασίας");
                AddTaskController controller = loader.getController();
                controller.setTaskTitle("Συσκευή: " + selectedDevice.getSerial());
                controller.setCustomerName(customer.getName());
                controller.setCustomerId(customer.getCode());
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

                dialog.initModality(Modality.NONE);
                dialog.initOwner(null);
                dialog.show();

            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
            }
    }

    @Override
    public void setCustomer(Customer customer) {
        this.customer = customer;
        //customerLabel.setText("Όνομα Πελάτη: " + customer.getName());
        if (Features.isEnabled("devices")) {
            loadDevicesForCustomer(customer.getCode()); // Κλήση φόρτωσης logins αφού οριστεί ο πελάτης
        }
    }

    @Override
    public void setOnDataSaved(Runnable callback) {
        this.onDataSaved = callback;
    }

    private void notifyDataSaved() {
        if (onDataSaved != null) {
            onDataSaved.run();
        }
    }
}
