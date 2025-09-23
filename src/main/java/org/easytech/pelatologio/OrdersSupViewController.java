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
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.CustomNotification;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Order;
import org.easytech.pelatologio.models.Supplier;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class OrdersSupViewController {
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn idColumn, titleColumn, descriptionColumn, dueDateColumn, customerColumn;
    @FXML
    private Button addOrderButton, editOrderButton, deleteOrderButton, completeOrderButton, uncompletedOrderButton;

    private final ObservableList<Order> allOrders = FXCollections.observableArrayList();
    private Supplier supplier;

    @FXML
    public void initialize() {
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

        ordersTable.setItems(allOrders);

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

        ordersTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) { // Έλεγχος για δύο κλικ
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
                    controller.setSupplierName(supplier.getName());
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
                            loadOrders(supplier.getId());
                        }
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
                }
            }
        });

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

    @FXML
    private void handleAddOrder() {
        try {
            // Φόρτωση του FXML για προσθήκη ραντεβού
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addOrder.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Παραγγελίας");
            AddOrderController controller = loader.getController();
            controller.setSupplierName(supplier.getName());
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
                    loadOrders(supplier.getId());
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
                    loadOrders(supplier.getId());
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
            DBHelper dbHelper = new DBHelper();
            DBHelper.getOrderDao().deleteOrder(selectedOrder.getId());
            loadOrders(supplier.getId());
        }
    }

    public void toggleComplete(ActionEvent event) {
        toggleComplete(true);
    }


    public void toggleRecall(ActionEvent event) {
        toggleComplete(false);
    }

    private void toggleComplete(boolean complete) {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Προσοχή")
                        .text("Δεν έχει επιλεγεί παραγγελία.")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showWarning();
            });
            return;
        }

        if (DBHelper.getOrderDao().completeOrder(selectedOrder.getId(), complete)) {
            System.out.println("Order completion status updated.");
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Ενημέρωση")
                        .text("Ενημέρωση παραγγελίας επιτυχής.")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showConfirmation();
            });
            loadOrders(supplier.getId()); // Φορτώνει ξανά τις εργασίες
        } else {
            System.out.println("Failed to update order completion status.");
            Platform.runLater(() -> {
                CustomNotification.create()
                        .title("Ενημέρωση")
                        .text("Αποτυχία ενημέρωση παραγγελίας.")
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .showError();
            });
        }
    }

    private void loadOrders(int id) {
        allOrders.setAll(DBHelper.getOrderDao().getAllOrdersSup(id));
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        loadOrders(supplier.getId());
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

}
