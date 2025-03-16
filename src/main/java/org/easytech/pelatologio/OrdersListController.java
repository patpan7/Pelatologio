package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
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
    private CheckBox showAllCheckbox, showCompletedCheckbox, showPendingCheckbox, showWithCustomerCheckbox, showWithoutCustomerCheckbox, showErgentCheckBox, showWaitCheckBox, showWithSupplierCheckbox, showWithoutSupplierCheckbox;
    @FXML
    private Button addOrderButton, editOrderButton, deleteOrderButton, completeOrderButton, uncompletedOrderButton;

    private ObservableList<Order> allOrders = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(addOrderButton, "Προσθήκη νέας παραγγελίας");
        setTooltip(editOrderButton, "Επεξεργασία παραγγελίας");
        setTooltip(deleteOrderButton, "Διαγραφή παραγγελίας");
        setTooltip(completeOrderButton, "Σημείωση παραγγελίας ως ολοκληρωμένη");
        setTooltip(uncompletedOrderButton,"Σημείωση παραγγελίας ως σε επεξεργασία");

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
                    if (order.getCompleted()) {
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
        CheckBox[] checkBoxes1 = {
                showAllCheckbox,
                showCompletedCheckbox,
                showPendingCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes1);

        CheckBox[] checkBoxes2 = {
                showWithCustomerCheckbox,
                showWithoutCustomerCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes2);

        CheckBox[] checkBoxes3 = {
                showErgentCheckBox,
                showWaitCheckBox
        };
        configureSingleSelectionCheckBoxes(checkBoxes3);

        CheckBox[] checkBoxes4 = {
                showWithSupplierCheckbox,
                showWithoutSupplierCheckbox
        };
        configureSingleSelectionCheckBoxes(checkBoxes4);

        showAllCheckbox.setOnAction(e -> updateOrdersTable());
        showCompletedCheckbox.setOnAction(e -> updateOrdersTable());
        showPendingCheckbox.setOnAction(e -> updateOrdersTable());
        showWithCustomerCheckbox.setOnAction(e -> updateOrdersTable());
        showWithoutCustomerCheckbox.setOnAction(e -> updateOrdersTable());
        showErgentCheckBox.setOnAction(e -> updateOrdersTable());
        showWaitCheckBox.setOnAction(e -> updateOrdersTable());
        showWithSupplierCheckbox.setOnAction(e -> updateOrdersTable());
        showWithoutSupplierCheckbox.setOnAction(e -> updateOrdersTable());

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
                notifications.showError();});
            return;
        }

        DBHelper dbHelper = new DBHelper();
        if (dbHelper.completeOrder(selectedOrder.getId(), complete)) {
            System.out.println("Order completion status updated.");
            Platform.runLater(() -> {
                Notifications notifications = Notifications.create()
                        .title("Ενημέρωση")
                        .text("Ενημέρωση παραγγελίας επιτυχής.")
                        .graphic(null)
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT);
                notifications.showConfirm();});
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
                notifications.showError();});
        }
    }



    private void loadOrders() {
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allOrders.setAll(dbHelper.getAllOrders());
        updateOrdersTable();
    }

    private void updateOrdersTable() {
        // Ξεκινάμε με όλες τις εργασίες
        ObservableList<Order> filteredOrders = FXCollections.observableArrayList(allOrders);

        // Φιλτράρισμα βάσει ολοκλήρωσης
        if (!showAllCheckbox.isSelected()) {
            if (showCompletedCheckbox.isSelected()) {
                filteredOrders.removeIf(order -> !order.getCompleted());
            } else if (showPendingCheckbox.isSelected()) {
                filteredOrders.removeIf(Order::getCompleted);
            }
        }

        // Φιλτράρισμα βάσει πελάτη
        if (showWithCustomerCheckbox.isSelected()) {
            filteredOrders.removeIf(order -> order.getCustomerId() == 0);
        }
        if (showWithoutCustomerCheckbox.isSelected()) {
            filteredOrders.removeIf(order -> order.getCustomerId() != 0);
        }

        // Φιλτράρισμα βάσει προμηθευτή
        if (showWithSupplierCheckbox.isSelected()) {
            filteredOrders.removeIf(order -> order.getSupplierId() == 0);
        }
        if (showWithoutSupplierCheckbox.isSelected()) {
            filteredOrders.removeIf(order -> order.getSupplierId() != 0);
        }

        if (showErgentCheckBox.isSelected()) {
            filteredOrders.removeIf(order -> !order.getErgent());
        }
        if (showWaitCheckBox.isSelected()) {
            filteredOrders.removeIf(order -> !order.getWait());
        } else {
            filteredOrders.removeIf(order -> order.getWait());
        }


        // Ανανεώνουμε τα δεδομένα του πίνακα
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

                dialog.showAndWait();
                loadOrders();
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
            dialog.showAndWait();
            loadOrders();
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
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε την παραγγελία " + selectedOrder.getTitle() + ";" );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteOrder(selectedOrder.getId());
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
