package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.stage.Modality;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class SubsController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<Subscription> subsTable;
    @FXML
    private TableColumn idColumn, titleColumn, endDateColumn, customerColumn, categoryColumn, priceColumn;
    @FXML
    private DatePicker dateFrom, dateTo;
    @FXML
    private ComboBox <SubsCategory> categoryFilterComboBox;
    @FXML
    private Button addCategoryButton, addSubButton, editSubButton, deleteSubButton, renewButton;

    private ObservableList<Subscription> allSubs = FXCollections.observableArrayList();

    private TabPane mainTabPane;

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(addSubButton, "Προσθήκη νέου συμβολαίου");
        setTooltip(editSubButton, "Επεξεργασία συμβολαίου");
        setTooltip(deleteSubButton, "Διαγραφή συμβολαίου");
        setTooltip(renewButton, "Aνανέωση συμβολαίου");
        setTooltip(addCategoryButton, "Προσθήκη/Επεξεργασία κατηγοριών εργασιών");
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Υπολογισμός της πρώτης και τελευταίας ημέρας του τρέχοντος μήνα
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = YearMonth.from(today).atEndOfMonth();

        // Ορισμός των ημερομηνιών στα DatePicker
        dateFrom.setValue(firstDay);
        dateTo.setValue(lastDay);

        // Προσθήκη listener ώστε να φορτώνεται ξανά η λίστα όταν αλλάζει ημερομηνία
        ChangeListener<LocalDate> dateChangeListener = (ObservableValue<? extends LocalDate> obs, LocalDate oldValue, LocalDate newValue) -> {
            loadSubs(dateFrom.getValue(), dateTo.getValue());
        };

        dateFrom.valueProperty().addListener(dateChangeListener);
        dateTo.valueProperty().addListener(dateChangeListener);

        // Αρχικό γέμισμα του πίνακα
        loadSubs(dateFrom.getValue(), dateTo.getValue());

        // RowFactory για διαφορετικά χρώματα
        subsTable.setRowFactory(tv -> new TableRow<Subscription>() {
            @Override
            protected void updateItem(Subscription sub, boolean empty) {
                super.updateItem(sub, empty);
                if (empty || sub == null) {
                    setStyle("");
                } else {
                    if (sub.getEndDate().isBefore(LocalDate.now())) {
                        setStyle("-fx-background-color: #edd4d4; -fx-text-fill: #155724;"); // Πράσινο
                    } else {
                        setStyle(""); // Προεπιλογή
                    }
                }
            }
        });

        subsTable.setRowFactory(tv -> new TableRow<Subscription>() {
            @Override
            protected void updateItem(Subscription sub, boolean empty) {
                super.updateItem(sub, empty);
                if (empty || sub == null) {
                    setStyle("");
                } else {
                    LocalDate today = LocalDate.now();
                    LocalDate tenDaysLater = today.plusDays(10);

                    if (sub.getEndDate().isBefore(today)) {
                        setStyle("-fx-background-color: #edd4d4; -fx-text-fill: #155724;"); // Κόκκινο για ληγμένες συνδρομές
                    } else if (!sub.getEndDate().isBefore(today) && !sub.getEndDate().isAfter(tenDaysLater)) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;"); // Κίτρινο για συνδρομές που λήγουν σύντομα
                    } else {
                        setStyle(""); // Κανονικό χρώμα
                    }
                }
            }
        });

        subsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedSub != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        handleEditSub();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        DBHelper dbHelper = new DBHelper();
        List<SubsCategory> categories = dbHelper.getAllSubsCategory();
        categoryFilterComboBox.getItems().add(new SubsCategory(0,"Όλες"));
        categoryFilterComboBox.getItems().addAll(categories);
        categoryFilterComboBox.getSelectionModel().selectFirst();
        categoryFilterComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SubsCategory subsCategory) {
                return subsCategory != null ? subsCategory.getName() : "";
            }

            @Override
            public SubsCategory fromString(String string) {
                return categoryFilterComboBox.getItems().stream()
                        .filter(taskCategory -> taskCategory.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });


        categoryFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateTaskTable());


        // Κουμπιά
        addCategoryButton.setOnAction(e -> TaskCategoryManager());
        addSubButton.setOnAction(e -> handleAddSub());
        editSubButton.setOnAction(e -> {
            try {
                handleEditSub();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        deleteSubButton.setOnAction(e -> {
            try {
                handleDeleteSub();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        renewButton.setOnAction(e -> {
            handleRenewSub();
        });

    }

    private void loadSubs(LocalDate  from, LocalDate  to) {
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        allSubs.setAll(dbHelper.getAllSubs(from,to));
        updateTaskTable();
    }

    private void updateTaskTable() {
        // Ξεκινάμε με όλες τις εργασίες
        ObservableList<Subscription> filteredTasks = FXCollections.observableArrayList(allSubs);
        // Φιλτράρισμα βάσει κατηγορίας
        SubsCategory selectedCategory = categoryFilterComboBox.getValue(); // Η επιλεγμένη κατηγορία από το ComboBox
        if (selectedCategory != null && selectedCategory.getId() != 0) { // Εξαιρείται η κατηγορία "Όλες"
            filteredTasks.removeIf(sub -> !sub.getCategory().equals(selectedCategory.getName()));
        }
        // Ανανεώνουμε τα δεδομένα του πίνακα
        subsTable.setItems(filteredTasks);
    }

    private void handleAddSub() {
            try {
                // Φόρτωση του FXML για προσθήκη ραντεβού
                FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader.load());
                dialog.setTitle("Προσθήκη Εργασίας");
                AddSubController controller = loader.getController();
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                // Προσθέτουμε προσαρμοσμένη λειτουργία στο "OK"
                Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                okButton.addEventFilter(ActionEvent.ACTION, event -> {
                    // Εκτελούμε το handleSaveAppointment
                    boolean success = controller.handleSaveSub();

                    if (!success) {
                        // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                        event.consume();
                    }
                });

                dialog.showAndWait();
                loadSubs(dateFrom.getValue(), dateTo.getValue());
            } catch (IOException e) {
                Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
            }
    }

    private void handleEditSub() throws IOException {
        // Επεξεργασία επιλεγμένης εργασίας
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί συμβόλαιο!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addSub.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Επεξεργασία Εργασίας");
            AddSubController controller = loader.getController();

            // Ορισμός δεδομένων για επεξεργασία
            controller.setSubForEdit(selectedSub);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Εκτελούμε το handleSaveAppointment
                boolean success = controller.handleSaveSub();

                if (!success) {
                    // Αν υπάρχει σφάλμα, σταματάμε το κλείσιμο του διαλόγου
                    event.consume();
                }
            });
            dialog.showAndWait();
            loadSubs(dateFrom.getValue(), dateTo.getValue());
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void handleDeleteSub() throws SQLException {
        // Διαγραφή επιλεγμένης εργασίας
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί συμβόλαιο!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Επιβεβαίωση");
        alert.setHeaderText("Είστε βέβαιος ότι θέλετε να διαγράψετε το συμβόλαιο " + selectedSub.getTitle() + ";" );
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DBHelper dbHelper = new DBHelper();
            dbHelper.deleteSub(selectedSub.getId());
            loadSubs(dateFrom.getValue(), dateTo.getValue());
        }
    }

    private void handleRenewSub () {
        // Επεξεργασία επιλεγμένης εργασίας
        Subscription selectedSub = subsTable.getSelectionModel().getSelectedItem();
        if (selectedSub == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί συμβόλαιο!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        // Δημιουργία διαλόγου με επιλογές
        List<String> choices = Arrays.asList("+1 χρόνο", "+2 χρόνια", "+3 χρόνια");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("+1 χρόνο", choices);
        dialog.setTitle("Ανανέωση Συμβολαίου");
        dialog.setHeaderText("Επιλέξτε διάρκεια ανανέωσης");
        dialog.setContentText("Διάρκεια:");

        // Αν ο χρήστης επιλέξει κάτι
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selected -> {
            int yearsToAdd = Integer.parseInt(selected.replaceAll("[^0-9]", ""));
            DBHelper dbHelper = new DBHelper();
            dbHelper.renewSub(selectedSub.getId(), yearsToAdd);
            loadSubs(dateFrom.getValue(), dateTo.getValue());
        });
    }

    public void TaskCategoryManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("subsCategoryManagerView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load()); // Πρώτα κάνε load το FXML

            // Τώρα μπορείς να πάρεις τον controller
            SubsCategoryManagerViewController controller = loader.getController();
            controller.loadSubsCategories();


            dialog.setTitle("Κατηγορίες Εργασιών");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.show();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα των κατηγοριών εργασιών.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }


    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

}
