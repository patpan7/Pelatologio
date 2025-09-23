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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.easytech.pelatologio.helper.*;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Subscription;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CustomerSubsController implements CustomerTabController {
    @FXML
    private TableView<Subscription> subsTable;
    @FXML
    private TableColumn<Subscription, Integer> idColumn;
    @FXML
    private TableColumn<Subscription, String> titleColumn;
    @FXML
    private TableColumn<Subscription, LocalDate> endDateColumn;
    @FXML
    private TableColumn<Subscription, String> categoryColumn;
    @FXML
    private TableColumn<Subscription, String> priceColumn;
    @FXML
    private TableColumn<Subscription, Boolean> sendedColumn;
    @FXML
    private Button addTaskButton, editTaskButton, deleteTaskButton, renewButton;

    private ObservableList<Subscription> allSubs;

    Customer customer;
    private Runnable onDataSaved;

    @FXML
    private Button btnPrintReport;

    @FXML
    private TableColumn<Subscription, Boolean> activeColumn;

    @FXML
    public void initialize() {
        System.out.println("CustomerSubsController: Initializing...");

        setTooltip(addTaskButton, "Προσθήκη νέου συμβολαίου");
        setTooltip(editTaskButton, "Επεξεργασία συμβολαίου");
        setTooltip(deleteTaskButton, "Διαγραφή συμβολαίου");
        setTooltip(renewButton, "Ανανέωση συμβολαίου");
        setTooltip(btnPrintReport, "Εκτύπωση αναφοράς συνδρομών");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        sendedColumn.setCellValueFactory(new PropertyValueFactory<>("sended"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        activeColumn.setCellFactory(param -> new TableCell<Subscription, Boolean>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(event -> {
                    Subscription sub = getTableView().getItems().get(getIndex());
                    sub.setActive(checkBox.isSelected());
                    DBHelper.getSubscriptionDao().updateSubscriptionStatus(sub.getId(), sub.isActive());
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });

        // Custom cell factory for endDateColumn to format LocalDate
        endDateColumn.setCellFactory(column -> {
            return new TableCell<Subscription, LocalDate>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });

        allSubs = FXCollections.observableArrayList();
        subsTable.setItems(allSubs);

        subsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
                if (selectedSub != null) {
                    try {
                        handleEditSub();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        subsTable.setRowFactory(tv -> new TableRow<Subscription>() {
            @Override
            protected void updateItem(Subscription sub, boolean empty) {
                super.updateItem(sub, empty);
                if (empty || sub == null || sub.getEndDate() == null) { // Added null check for getEndDate()
                    setStyle("");
                } else {
                    LocalDate today = LocalDate.now();
                    LocalDate tenDaysLater = today.plusDays(10);

                    if (sub.getEndDate().isBefore(today)) {
                        getStyleClass().add("expired-row");
                    } else if (!sub.getEndDate().isBefore(today) && !sub.getEndDate().isAfter(tenDaysLater)) {
                        getStyleClass().add("expiring-soon-row");
                    }
                }
            }
        });

        // Κουμπιά
        addTaskButton.setOnAction(e -> handleAddSub());
        editTaskButton.setOnAction(e -> {
            try {
                handleEditSub();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteTaskButton.setOnAction(e -> {
            try {
                handleDeleteSub();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        renewButton.setOnAction(e -> handleRenewSub());
        btnPrintReport.setOnAction(e -> handlePrintReport());
        System.out.println("CustomerSubsController: Initialization complete.");
    }

    private void handlePrintReport() {
        ReportManager.generateSubscriptionReport(allSubs, "", "");
    }


    private void loadSubs(int customerCode) {
        allSubs.clear();
        allSubs.setAll(DBHelper.getSubscriptionDao().getAllCustomerSubs(customerCode));
    }


    @FXML
    private void handleAddSub() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Προσθήκη Συμβολαίου");
            AddSubController controller = loader.getController();
            controller.setCustomerId(customer.getCode());
            controller.setCustomerName(customer.getName());
            controller.lock();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                boolean success = controller.handleSaveSub();
                if (!success) {
                    event.consume();
                }
                notifyDataSaved();
            });

            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                    loadSubs(customer.getCode());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleEditSub() throws IOException {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχει επιλεγεί συμβόλαιο!", "", Alert.AlertType.ERROR);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία Συμβολαίου");
            AddSubController controller = loader.getController();

            controller.setSubForEdit(selectedSub);
            controller.lock();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                boolean success = controller.handleSaveSub();
                if (!success) {
                    event.consume();
                }
                notifyDataSaved();
            });
            dialog.initModality(Modality.NONE);
            dialog.initOwner(null);
            dialog.show();

            dialog.setOnHidden(e -> {
                if (dialog.getResult() == ButtonType.OK) {
                    loadSubs(customer.getCode());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleDeleteSub() throws SQLException {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχει επιλεγεί συμβόλαιο!", "", Alert.AlertType.ERROR);
            return;
        }
        Optional<ButtonType> result = AlertDialogHelper.showConfirmationDialog("Επιβεβαίωση", "Είστε βέβαιος ότι θέλετε να διαγράψετε την εργασία " + selectedSub.getTitle() + ";", "");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper.getSubscriptionDao().deleteSub(selectedSub.getId());
            loadSubs(customer.getCode());
            notifyDataSaved();
        }
    }

    private void handleRenewSub() {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχει επιλεγεί συμβόλαιο!", "", Alert.AlertType.ERROR);
            return;
        }
        List<String> choices = Arrays.asList("+1 μήνας", "+3 μήνες", "+6 μήνες", "+1 χρόνος", "+2 χρόνια", "+3 χρόνια");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("+1 χρόνος", choices);
        dialog.setTitle("Ανανέωση Συμβολαίου");
        dialog.setHeaderText("Επιλέξτε διάρκεια ανανέωσης");
        dialog.setContentText("Διάρκεια:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            int monthsToAdd = 0;
            if (selected.contains("μήνας")) {
                monthsToAdd = Integer.parseInt(selected.replaceAll("[^0-9]", ""));
            } else if (selected.contains("χρόν")) {
                monthsToAdd = Integer.parseInt(selected.replaceAll("[^0-9]", "")) * 12;
            }
            DBHelper.getSubscriptionDao().renewSub(selectedSub.getId(), monthsToAdd);
            loadSubs(customer.getCode());
            notifyDataSaved();
        });
    }

    @Override
    public void setCustomer(Customer customer) {
        this.customer = customer;
        loadSubs(customer.getCode());
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



    private void setTooltip(Button button, String text) {
        if (button != null) {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setShowDelay(Duration.seconds(0.3));
            button.setTooltip(tooltip);
        }
    }

    public void handleSendMail(ActionEvent event) {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            CustomNotification.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε ένα συμβόλαιο για να στείλετε το e-mail.")
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
            return;
        }
        Customer customer = DBHelper.getCustomerDao().getSelectedCustomer(selectedSub.getCustomerId());
        try {
            String email = customer.getEmail();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("emailDialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Αποστολή Email");
            EmailDialogController controller = loader.getController();
            controller.setCustomer(customer);
            controller.setEmail(email);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("{calculatedPrice}", String.format("%.02f", Float.parseFloat(selectedSub.getPrice().trim()) * 1.24));
            // Prepare email content using EmailTemplateHelper
            EmailTemplateHelper.EmailContent emailContent = EmailTemplateHelper.prepareEmail("subsReminder", selectedSub, customer, placeholders);

            controller.setSubject(emailContent.subject());
            controller.setBody(emailContent.body());
            controller.setSaveCopy(false);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.show();
            dialog.setOnCloseRequest(evt -> {
                if (controller.isSended) {
                    DBHelper.getSubscriptionDao().updateSubSent(selectedSub.getId());
                    loadSubs(customer.getCode());
                }
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void handleCopy(ActionEvent event) {
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            CustomNotification.create()
                    .title("Προσοχή")
                    .text("Παρακαλώ επιλέξτε ένα συμβόλαιο.")
                    .hideAfter(Duration.seconds(5))
                    .position(Pos.TOP_RIGHT)
                    .showWarning();
            return;
        }
        Customer customer = DBHelper.getCustomerDao().getSelectedCustomer(selectedSub.getCustomerId());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{calculatedPrice}", String.format("%.02f", Float.parseFloat(selectedSub.getPrice().trim()) * 1.24));

        EmailTemplateHelper.EmailContent emailContent = EmailTemplateHelper.prepareEmail("subsReminder", customer, selectedSub, placeholders);
        String plainText = EmailTemplateHelper.htmlToPlainText(emailContent.body());
        AppUtils.copyTextToClipboard(plainText);
    }
}