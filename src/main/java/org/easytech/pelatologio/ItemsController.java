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
            items = dbHelper.getItems();
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

            // Υποστήριξη Ελληνικών/Αγγλικών
            char[] chars1 = filter.toCharArray();
            IntStream.range(0, chars1.length).forEach(i -> {
                Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                if (repl != null) chars1[i] = repl;
            });
            char[] chars2 = filter.toCharArray();
            IntStream.range(0, chars2.length).forEach(i -> {
                Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                if (repl != null) chars2[i] = repl;
            });
            String search1 = new String(chars1);
            String search2 = new String(chars2);

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
            okButton.setOnAction(event -> {controller.handleOkButton();
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
                    dbHelper.customerUnlock(selectedItem.getId());
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

    private static final Map<Character, Character> ENGLISH_TO_GREEK = new HashMap<>();

    static {
        ENGLISH_TO_GREEK.put('\u0041', '\u0391');  // uppercase A
        ENGLISH_TO_GREEK.put('\u0042', '\u0392');  // uppercase B
        ENGLISH_TO_GREEK.put('\u0043', '\u03A8');  // uppercase C
        ENGLISH_TO_GREEK.put('\u0044', '\u0394');  // uppercase D
        ENGLISH_TO_GREEK.put('\u0045', '\u0395');  // uppercase E
        ENGLISH_TO_GREEK.put('\u0046', '\u03A6');  // uppercase F
        ENGLISH_TO_GREEK.put('\u0047', '\u0393');  // uppercase G
        ENGLISH_TO_GREEK.put('\u0048', '\u0397');  // uppercase H
        ENGLISH_TO_GREEK.put('\u0049', '\u0399');  // uppercase I
        ENGLISH_TO_GREEK.put('\u004A', '\u039E');  // uppercase J
        ENGLISH_TO_GREEK.put('\u004B', '\u039A');  // uppercase K
        ENGLISH_TO_GREEK.put('\u004C', '\u039B');  // uppercase L
        ENGLISH_TO_GREEK.put('\u004D', '\u039C');  // uppercase M
        ENGLISH_TO_GREEK.put('\u004E', '\u039D');  // uppercase N
        ENGLISH_TO_GREEK.put('\u004F', '\u039F');  // uppercase O
        ENGLISH_TO_GREEK.put('\u0050', '\u03A0');  // uppercase P
        //ENGLISH_TO_GREEK.put('\u0051', '\u0391');  // uppercase Q
        ENGLISH_TO_GREEK.put('\u0052', '\u03A1');  // uppercase R
        ENGLISH_TO_GREEK.put('\u0053', '\u03A3');  // uppercase S
        ENGLISH_TO_GREEK.put('\u0054', '\u03A4');  // uppercase T
        ENGLISH_TO_GREEK.put('\u0055', '\u0398');  // uppercase U
        ENGLISH_TO_GREEK.put('\u0056', '\u03A9');  // uppercase V
        ENGLISH_TO_GREEK.put('\u0057', '\u03A3');  // uppercase W
        ENGLISH_TO_GREEK.put('\u0058', '\u03A7');  // uppercase X
        ENGLISH_TO_GREEK.put('\u0059', '\u03A5');  // uppercase Y
        ENGLISH_TO_GREEK.put('\u005A', '\u0396');  // uppercase Z
    }

    private static final Map<Character, Character> GREEK_TO_ENGLISH = new HashMap<>();

    static {
        GREEK_TO_ENGLISH.put('\u0391', '\u0041');  // uppercase Α
        GREEK_TO_ENGLISH.put('\u0392', '\u0042');  // uppercase Β
        GREEK_TO_ENGLISH.put('\u03A8', '\u0043');  // uppercase Ψ
        GREEK_TO_ENGLISH.put('\u0394', '\u0044');  // uppercase Δ
        GREEK_TO_ENGLISH.put('\u0395', '\u0045');  // uppercase Ε
        GREEK_TO_ENGLISH.put('\u03A6', '\u0046');  // uppercase Φ
        GREEK_TO_ENGLISH.put('\u0393', '\u0047');  // uppercase Γ
        GREEK_TO_ENGLISH.put('\u0397', '\u0048');  // uppercase Η
        GREEK_TO_ENGLISH.put('\u0399', '\u0049');  // uppercase Ι
        GREEK_TO_ENGLISH.put('\u039E', '\u004A');  // uppercase Ξ
        GREEK_TO_ENGLISH.put('\u039A', '\u004B');  // uppercase Κ
        GREEK_TO_ENGLISH.put('\u039B', '\u004C');  // uppercase Λ
        GREEK_TO_ENGLISH.put('\u039C', '\u004D');  // uppercase Μ
        GREEK_TO_ENGLISH.put('\u039D', '\u004E');  // uppercase Ν
        GREEK_TO_ENGLISH.put('\u039F', '\u004F');  // uppercase Ο
        GREEK_TO_ENGLISH.put('\u03A0', '\u0050');  // uppercase Π
        //GREEK_TO_ENGLISH.put('\u0051', '\u0391');  // uppercase Q
        GREEK_TO_ENGLISH.put('\u03A1', '\u0052');  // uppercase Ρ
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase Σ
        GREEK_TO_ENGLISH.put('\u03A4', '\u0054');  // uppercase Τ
        GREEK_TO_ENGLISH.put('\u0398', '\u0055');  // uppercase Θ
        GREEK_TO_ENGLISH.put('\u03A9', '\u0056');  // uppercase Ω
        GREEK_TO_ENGLISH.put('\u03A3', '\u0053');  // uppercase ς
        GREEK_TO_ENGLISH.put('\u03A7', '\u0058');  // uppercase Χ
        GREEK_TO_ENGLISH.put('\u03A5', '\u0059');  // uppercase Υ
        GREEK_TO_ENGLISH.put('\u0396', '\u005A');  // uppercase Ζ
    }

    public void clean(ActionEvent actionEvent) {
        filterField.setText("");
        itemsTable.getSelectionModel().clearSelection();
    }

    public void mainMenuClick(ActionEvent event) throws IOException {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.mainMenuClick(stackPane);
    }
}
