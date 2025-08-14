package org.easytech.pelatologio;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.easytech.pelatologio.dao.PartnerEarningDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Partner;
import org.easytech.pelatologio.models.PartnerEarning;
import org.easytech.pelatologio.service.CommissionService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class PartnerEarningsController {

    @FXML private TableView<PartnerEarning> earningsTable;
    @FXML private TableColumn<PartnerEarning, String> partnerColumn;
    @FXML private TableColumn<PartnerEarning, String> customerColumn;
    @FXML private TableColumn<PartnerEarning, String> supplierColumn;
    @FXML private TableColumn<PartnerEarning, LocalDate> earningDateColumn;
    @FXML private TableColumn<PartnerEarning, BigDecimal> earningAmountColumn;
    @FXML private TableColumn<PartnerEarning, String> invoiceStatusColumn;
    @FXML private TableColumn<PartnerEarning, String> invoiceRefColumn;
    @FXML private TableColumn<PartnerEarning, String> paymentStatusColumn;
    @FXML private TableColumn<PartnerEarning, LocalDate> paymentDateColumn;

    @FXML private ComboBox<Partner> partnerFilterComboBox;
    @FXML private JFXButton btnCalculateCommissions;

    private PartnerEarningDao partnerEarningDao;
    private ObservableList<PartnerEarning> masterData;
    private FilteredList<PartnerEarning> filteredData;

    @FXML
    public void initialize() {
        this.partnerEarningDao = DBHelper.getPartnerEarningDao();
        setupTable();
        setupFilters();
        loadEarnings();
    }

    private void setupTable() {
        // Set up cell value factories
        partnerColumn.setCellValueFactory(new PropertyValueFactory<>("partnerName"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        earningDateColumn.setCellValueFactory(new PropertyValueFactory<>("earningDate"));
        earningAmountColumn.setCellValueFactory(new PropertyValueFactory<>("earningAmount"));
        invoiceStatusColumn.setCellValueFactory(new PropertyValueFactory<>("partnerInvoiceStatus"));
        invoiceRefColumn.setCellValueFactory(new PropertyValueFactory<>("partnerInvoiceRef"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentToPartnerStatus"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentToPartnerDate"));

        // Make table and relevant columns editable
        earningsTable.setEditable(true);
        invoiceStatusColumn.setEditable(true);
        invoiceRefColumn.setEditable(true);
        paymentStatusColumn.setEditable(true);
        paymentDateColumn.setEditable(true);

        // Set cell factories for editable columns
        invoiceStatusColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Pending", "Received", "Checked"));
        invoiceRefColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        paymentStatusColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Unpaid", "Partially Paid", "Paid"));
                paymentDateColumn.setCellFactory(column -> new DatePickerCellFactory()); // Correct way to set custom cell factory

        // Set onEditCommit handlers
        invoiceStatusColumn.setOnEditCommit(this::handleEditCommit);
        invoiceRefColumn.setOnEditCommit(this::handleEditCommit);
        paymentStatusColumn.setOnEditCommit(this::handleEditCommit);
        paymentDateColumn.setOnEditCommit(this::handleEditCommit);

        // Custom cell factory for earningAmountColumn to format BigDecimal
        earningAmountColumn.setCellFactory(tc -> new TableCell<PartnerEarning, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f€", amount));
                }
            }
        });

        // Custom cell factory for LocalDate columns to format dates
        earningDateColumn.setCellFactory(tc -> new TableCell<PartnerEarning, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });
        paymentDateColumn.setCellFactory(tc -> new TableCell<PartnerEarning, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        masterData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(masterData, p -> true);
        earningsTable.setItems(filteredData);

        // Double-click for editing status (opens dialog for more complex edits)
        earningsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                PartnerEarning selectedEarning = earningsTable.getSelectionModel().getSelectedItem();
                if (selectedEarning != null) {
                    openStatusEditDialog(selectedEarning);
                }
            }
        });
    }

    private void setupFilters() {
        // Load partners for the filter ComboBox
        partnerFilterComboBox.getItems().add(new Partner(0, "Όλοι")); // "All" option
        partnerFilterComboBox.getItems().addAll(DBHelper.getPartnerDao().findAll()); // Assuming getPartners() exists and returns List<Partner>

        partnerFilterComboBox.setConverter(new StringConverter<Partner>() {
            @Override
            public String toString(Partner partner) {
                return partner == null ? "" : partner.getName();
            }
            @Override
            public Partner fromString(String string) { return null; } // Not needed for selection
        });

        partnerFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            applyFilter();
        });
        partnerFilterComboBox.getSelectionModel().selectFirst(); // Select "All" by default
    }

    private void loadEarnings() {
        masterData.clear();
        masterData.addAll(partnerEarningDao.getAllEarnings());
        applyFilter(); // Apply filter after loading all data
    }

    private void applyFilter() {
        Partner selectedPartner = partnerFilterComboBox.getSelectionModel().getSelectedItem();
        filteredData.setPredicate(earning -> {
            if (selectedPartner == null || selectedPartner.getId() == 0) {
                return true; // Show all if "All" is selected
            }
            return earning.getPartnerId() == selectedPartner.getId();
        });
    }

    // --- Status Update Logic ---

    @FXML
    void handleMarkInvoiceReceived(ActionEvent event) {
        List<PartnerEarning> selectedEarnings = earningsTable.getSelectionModel().getSelectedItems();
        if (selectedEarnings.isEmpty()) {
            AlertDialogHelper.showDialog("Προσοχή", "Επιλέξτε τουλάχιστον μία οφειλή.", "", Alert.AlertType.WARNING);
            return;
        }
        
        // Confirm with user
        Optional<ButtonType> result = AlertDialogHelper.showConfirmationDialog(
            "Επιβεβαίωση", "Σήμανση Τιμολογίου ως Ελήφθη", "Είστε σίγουροι ότι θέλετε να σημάνετε τα επιλεγμένα τιμολόγια ως 'Ελήφθη';"
        );
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (PartnerEarning earning : selectedEarnings) {
                partnerEarningDao.updateEarningStatus(earning.getId(), "Received", earning.getPaymentToPartnerStatus(), earning.getPaymentToPartnerDate());
            }
            AlertDialogHelper.showInfoDialog("Επιτυχία", "Τα τιμολόγια σημάνθηκαν ως 'Ελήφθη'.");
            loadEarnings(); // Refresh table
        }
    }

    @FXML
    void handleMarkPaid(ActionEvent event) {
        List<PartnerEarning> selectedEarnings = earningsTable.getSelectionModel().getSelectedItems();
        if (selectedEarnings.isEmpty()) {
            AlertDialogHelper.showDialog("Προσοχή", "Επιλέξτε τουλάχιστον μία οφειλή.", "", Alert.AlertType.WARNING);
            return;
        }

        // Confirm with user
        Optional<ButtonType> result = AlertDialogHelper.showConfirmationDialog(
            "Επιβεβαίωση", "Σήμανση ως Πληρωμένο", "Είστε σίγουροι ότι θέλετε να σημάνετε τις επιλεγμένες οφειλές ως 'Πληρωμένο';"
        );
        if (result.isPresent() && result.get() == ButtonType.OK) {
            for (PartnerEarning earning : selectedEarnings) {
                partnerEarningDao.updateEarningStatus(earning.getId(), earning.getPartnerInvoiceStatus(), "Paid", LocalDate.now());
            }
            AlertDialogHelper.showInfoDialog("Επιτυχία", "Οι οφειλές σημάνθηκαν ως 'Πληρωμένο'.");
            loadEarnings(); // Refresh table
        }
    }

    private void openStatusEditDialog(PartnerEarning earning) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editPartnerEarningStatusDialog.fxml"));
            DialogPane pane = loader.load();

            EditPartnerEarningStatusDialogController controller = loader.getController();
            controller.setPartnerEarning(earning);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Επεξεργασία Κατάστασης Οφειλής");

            dialog.showAndWait(); // Just show and wait, dialog controller handles closing
            loadEarnings(); // Refresh table after dialog closes

        } catch (IOException e) {
            e.printStackTrace();
            AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα του διαλόγου επεξεργασίας.", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private <T> void handleEditCommit(TableColumn.CellEditEvent<PartnerEarning, T> event) {
        PartnerEarning earning = event.getRowValue();
        TableColumn<PartnerEarning, T> column = event.getTableColumn();

        if (column == invoiceStatusColumn) {
            earning.setPartnerInvoiceStatus((String) event.getNewValue());
        } else if (column == invoiceRefColumn) {
            earning.setPartnerInvoiceRef((String) event.getNewValue());
        } else if (column == paymentStatusColumn) {
            earning.setPaymentToPartnerStatus((String) event.getNewValue());
        } else if (column == paymentDateColumn) {
            earning.setPaymentToPartnerDate((LocalDate) event.getNewValue());
        }

        // Save to DB
        partnerEarningDao.updateEarningStatus(
                earning.getId(),
                earning.getPartnerInvoiceStatus(),
                earning.getPaymentToPartnerStatus(),
                earning.getPaymentToPartnerDate()
        );
        earningsTable.refresh(); // Refresh the table to show the updated value
    }

    // Custom CellFactory for DatePicker in TableView
    private static class DatePickerCellFactory extends TableCell<PartnerEarning, LocalDate> {
        private final DatePicker datePicker;

        public DatePickerCellFactory() {
            this.datePicker = new DatePicker();
            this.datePicker.setConverter(new StringConverter<LocalDate>() {
                String pattern = "dd/MM/yyyy";
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

                @Override
                public String toString(LocalDate date) {
                    if (date != null) {
                        return dateFormatter.format(date);
                    } else {
                        return "";
                    }
                }

                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        return LocalDate.parse(string, dateFormatter);
                    } else {
                        return null;
                    }
                }
            });
            this.datePicker.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    commitEdit(datePicker.getValue());
                }
            });
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (isEmpty()) {
                return;
            }
            datePicker.setValue(getItem());
            setText(null);
            setGraphic(datePicker);
            datePicker.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem() != null ? getItem().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            setGraphic(null);
        }

        @Override
        public void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    setText(null);
                    setGraphic(datePicker);
                } else {
                    setText(item != null ? item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
                    setGraphic(null);
                }
            }
        }
    }


    @FXML
    void handleCalculateCommissions(ActionEvent event) throws SQLException {
        CommissionService commissionService = new CommissionService();
        CommissionService.CalculationResult result = commissionService.calculatePartnerEarnings();

        String message = String.format("Υπολογισμός Ολοκληρώθηκε:\nΔημιουργήθηκαν: %d οφειλές\nΠαραλείφθηκαν: %d οφειλές\n\nΛεπτομέρειες:\n%s",
        result.getCreatedCount(), result.getSkippedCount(), result.getDetails());

        AlertDialogHelper.showInfoDialog("Υπολογισμός Προμηθειών", message);
        loadEarnings(); // Refresh the table to show new earnings
    }
}
