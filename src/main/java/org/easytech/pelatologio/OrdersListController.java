package org.easytech.pelatologio;

import atlantafx.base.controls.ToggleSwitch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Order;
import org.easytech.pelatologio.models.Supplier;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OrdersListController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn idColumn, titleColumn, descriptionColumn, dueDateColumn, customerColumn, supplierColumn;
    @FXML
    private ToggleSwitch showAllCheckbox, showCompletedCheckbox, showPendingCheckbox, showReceivedCheckbox, showWithCustomerCheckbox, showWithoutCustomerCheckbox, showErgentCheckBox, showWaitCheckBox, showWithSupplierCheckbox, showWithoutSupplierCheckbox;
    @FXML
    private Button addOrderButton, editOrderButton, deleteOrderButton, completeOrderButton, uncompletedOrderButton;
    @FXML
    private ComboBox<Supplier> supplierFilterComboBox;

    private final ObservableList<Order> allOrders = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(addOrderButton, "Προσθήκη νέας παραγγελίας");
        setTooltip(editOrderButton, "Επεξεργασία παραγγελίας");
        setTooltip(deleteOrderButton, "Διαγραφή παραγγελίας");
        setTooltip(completeOrderButton, "Σημείωση παραγγελίας ως ολοκληρωμένη");
        setTooltip(uncompletedOrderButton, "Σημείωση παραγγελίας ως σε επεξεργασία");

        // Σύνδεση στηλών πίνακα με πεδία του Order
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));

// Κάνει τον πίνακα επεξεργάσιμο, αλλιώς το CheckBox δεν θα λειτουργεί
        ordersTable.setEditable(true);

        showAllCheckbox.setSelected(false);
        showPendingCheckbox.setSelected(true);
        // Αρχικό γέμισμα του πίνακα
        loadOrders();

        // RowFactory για διαφορετικά χρώματα
        ordersTable.setRowFactory(tv -> new TableRow<Order>() {
            @Override
            protected void updateItem(Order order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setStyle("");
                } else {
                    if (order.getCompleted() && order.getReceived() && order.getDelivered()) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;"); // Πράσινο
                    } else {
                        setStyle(""); // Προεπιλογή
                    }
                }
            }
        });

        ordersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedOrder != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        handleEditOrder();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        // Φίλτρα
        ToggleSwitch[] checkBoxes1 = {
                showAllCheckbox,
                showCompletedCheckbox,
                showPendingCheckbox,
                showReceivedCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes1);

        ToggleSwitch[] checkBoxes2 = {
                showWithCustomerCheckbox,
                showWithoutCustomerCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes2);

        ToggleSwitch[] checkBoxes3 = {
                showErgentCheckBox,
                showWaitCheckBox
        };
        configureSingleSelectionCheckBoxes(checkBoxes3);

        ToggleSwitch[] checkBoxes4 = {
                showWithSupplierCheckbox,
                showWithoutSupplierCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes4);

        DBHelper dbHelper = new DBHelper();
        List<Supplier> suppliers = DBHelper.getSupplierDao().getSuppliersFromOrders();
        supplierFilterComboBox.getItems().add(new Supplier(-1, "Όλα", "", "", "", "", "", "", "", "", "", false)); // Προσθήκη επιλογής "Όλα"
        supplierFilterComboBox.getItems().addAll(suppliers);
        supplierFilterComboBox.getSelectionModel().selectFirst();
        supplierFilterComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier != null ? supplier.getName() : "";
            }

            @Override
            public Supplier fromString(String string) {
                return supplierFilterComboBox.getItems().stream()
                        .filter(supplier -> supplier.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        supplierFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateOrdersTable());


        showAllCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showCompletedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showPendingCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showReceivedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showWithCustomerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showWithoutCustomerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showErgentCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showWaitCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showWithSupplierCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());
        showWithoutSupplierCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateOrdersTable());

        // Κουμπιά
        addOrderButton.setOnAction(e -> handleAddOrder());
        editOrderButton.setOnAction(e -> {
            try {
                handleEditOrder();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteOrderButton.setOnAction(e -> {
            try {
                handleDeleteOrder();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        completeOrderButton.setOnAction(e -> toggleComplete(true));
        uncompletedOrderButton.setOnAction(e -> toggleComplete(false));
    }


    private void configureSingleSelectionCheckBoxes(ToggleSwitch[] checkBoxes) {
        for (ToggleSwitch checkBox : checkBoxes) {
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    for (ToggleSwitch otherCheckBox : checkBoxes) {
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.setSelected(false);
                        }
                    }
                }
            });
        }
    }

    private void toggleComplete(boolean complete) {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Προσοχή")
                        .text("Δεν έχει επιλεγεί παραγγελία.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
            return;
        }

        DBHelper dbHelper = new DBHelper();
        if (DBHelper.getOrderDao().completeOrder(selectedOrder.getId(), complete)) {
            System.out.println("Order completion status updated.");
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Ενημέρωση παραγγελίας επιτυχής.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();
            });
            loadOrders(); // Φορτώνει ξανά τις εργασίες
        } else {
            System.out.println("Failed to update order completion status.");
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Αποτυχία ενημέρωση παραγγελίας.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showError();
            });
        }
    }


    private void loadOrders() {
        List<TableColumn<Order, ?>> sortOrder = new ArrayList<>(ordersTable.getSortOrder());
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allOrders.setAll(DBHelper.getOrderDao().getAllOrders());
        updateOrdersTable();
        ordersTable.getSortOrder().setAll(sortOrder);
    }

    private void updateOrdersTable() {
        FilteredList<Order> filteredOrders = new FilteredList<>(allOrders);

        filteredOrders.setPredicate(order -> {
            boolean matches = true;

            if (showPendingCheckbox.isSelected()) {
                matches = !order.getCompleted();
            }
            if (showCompletedCheckbox.isSelected()) {
                matches = order.getCompleted() && !order.getReceived() && !order.getDelivered();
            }
            if (showReceivedCheckbox.isSelected()) {
                matches = order.getCompleted() && order.getReceived() && !order.getDelivered();
            }

            if (showWithCustomerCheckbox.isSelected() && order.getCustomerId() == 0) {
                return false;
            }
            if (showWithoutCustomerCheckbox.isSelected() && order.getCustomerId() != 0) {
                return false;
            }
            if (showWithSupplierCheckbox.isSelected() && order.getSupplierId() == 0) {
                return false;
            }
            if (showWithoutSupplierCheckbox.isSelected() && order.getSupplierId() != 0) {
                return false;
            }
            if (showErgentCheckBox.isSelected() && !order.getErgent()) {
                return false;
            }
            if (showWaitCheckBox.isSelected() && !order.getWait()) {
                return false;
            }
            if (!showWaitCheckBox.isSelected() && order.getWait()) {
                return false;
            }

            return matches;
        });

        ordersTable.setItems(filteredOrders);

    }


    @FXML
    private void handleAddOrder() {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addOrder.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Παραγγελίας");
            AddOrderController controller = loader.getController();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveOrder();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                    loadOrders();
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleEditOrder() throws IOException {
        // Επεξεργασία επιλεγμένης εργασίας
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί παραγγελία!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addOrder.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία παραγγελίας");
            AddOrderController controller = loader.getController();

            // Ορισμός δεδομένων για επεξεργασία
            controller.setOrderForEdit(selectedOrder);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveOrder();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                    loadOrders();
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleDeleteOrder() throws SQLException {
        // Διαγραφή επιλεγμένης εργασίας
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί παραγγελία!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε την παραγγελία " + selectedOrder.getTitle() + ";");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper.getOrderDao().deleteOrder(selectedOrder.getId());
            loadOrders();
        }
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

    public void toggleComplete(ActionEvent event) {
        toggleComplete(true);
    }


    public void toggleRecall(ActionEvent event) {
        toggleComplete(false);
    }

    public void refresh(MouseEvent mouseEvent) {
        loadOrders();
    }
}
