package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.AppUtils;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Item;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

public class ItemsController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    TextField filterField;
    @FXML
    public TableColumn idColumn, nameColumn, descriptionColumn;
    @FXML
    TableView<Item> itemsTable;
    ObservableList<Item> observableList;
    FilteredList<Item> filteredData;
    DBHelper dbHelper;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(() -> stackPane.requestFocus());
        dbHelper = new DBHelper();

        // Δημιουργία και αρχικοποίηση των στηλών
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Αρχικοποίηση πίνακα
        initializeTable();

        // Διπλό κλικ για επεξεργασία πελάτη
        itemsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ
                // Πάρτε τα δεδομένα από την επιλεγμένη γραμμή
                Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedItem != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    try {
                        itemUpdate(new ActionEvent());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        itemsTable.getSelectionModel().clearSelection();
    }

    private void initializeTable() {
        // Δημιουργία του ObservableList και φόρτωση δεδομένων
        observableList = FXCollections.observableArrayList();
        try {
            observableList.addAll(fetchDataFromMySQL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Δημιουργία του FilteredList
        filteredData = new FilteredList<>(observableList, b -> true);

        // Σύνδεση φιλτραρίσματος
        setupFilter();

        // Σύνδεση του SortedList με τον πίνακα
        SortedList<Item> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(itemsTable.comparatorProperty());
        itemsTable.setItems(sortedData);
    }

    private List<Item> fetchDataFromMySQL() throws SQLException {
        List<Item> items;
        try {
            items = DBHelper.getItemDao().getItems();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return items;
    }

    private void setupFilter() {
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(newValue);
        });
        applyFilters(filterField.getText()); // Αρχική εφαρμογή φίλτρου
    }

    private void applyFilters(String filterValue) {
        filteredData.setPredicate(item -> {
            if (filterValue == null || filterValue.isEmpty()) {
                return true;
            }
            String filter = filterValue.toUpperCase();

            String search1 = AppUtils.toEnglish(filter);
            String search2 = AppUtils.toGreek(filter);

            // Εφαρμογή φίλτρου
            return (item.getName() != null && (item.getName().toUpperCase().contains(search1) || item.getName().toUpperCase().contains(search2)))
                    || (item.getDescription() != null && (item.getDescription().toUpperCase().contains(search1) || item.getDescription().toUpperCase().contains(search2)))
                    || (String.valueOf(item.getId()).contains(search1) || String.valueOf(item.getId()).contains(search2));
        });
    }

    private void refreshTableData() {
        List<TableColumn<Item, ?>> sortOrder = new ArrayList<>(itemsTable.getSortOrder());
        observableList.clear();
        try {
            observableList.addAll(fetchDataFromMySQL());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        applyFilters(filterField.getText());
        itemsTable.getSortOrder().setAll(sortOrder);
    }

    public void itemAddNew(ActionEvent actionEvent) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newItem.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Εισαγωγή Νέου Είδους");

            AddItemController controller = loader.getController();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setOnAction(event -> {
                controller.handleOkButton();
                refreshTableData();
            });
            // Add a key listener to save when Enter is pressed
            dialog.getDialogPane().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    okButton.fire();  // Triggers the OK button action
                    refreshTableData();
                }
            });
            dialog.show();
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void itemUpdate(ActionEvent actionEvent) throws IOException {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newItem.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.setTitle("Ενημέρωση Είδους");
            dialog.initModality(Modality.WINDOW_MODAL);

            AddItemController controller = loader.getController();

            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
            controller.setItemData(selectedItem);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setOnAction(event -> {
                controller.handleOkButton();
                // Reinitialize the table and apply the search filter when OK is pressed
                //tableInit();
                refreshTableData();
                filteredData = new FilteredList<>(observableList, b -> true);

                filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                    applyFilters(newValue);
                });

                applyFilters(filterField.getText());

                SortedList<Item> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(itemsTable.comparatorProperty());
                itemsTable.setItems(sortedData);
            });

            // Προσθήκη listener για το κλείσιμο του παραθύρου
            dialog.setOnHidden(event -> {
                DBHelper.getCustomerDao().customerUnlock(selectedItem.getId());
            });

            // Add a key listener to save when Enter is pressed
            dialog.getDialogPane().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    okButton.fire();  // Triggers the OK button action
                }
            });
            dialog.show();

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την ενημέρωση.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void handleEditOption(ActionEvent event) throws IOException {
        Item selectedItem = itemsTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Προσοχή");
            alert.setContentText("Δεν έχει επιλεγεί είδος!");
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }
        itemUpdate(event);
    }


    public void clean(ActionEvent actionEvent) {
        filterField.setText("");
        itemsTable.getSelectionModel().clearSelection();
    }

}
