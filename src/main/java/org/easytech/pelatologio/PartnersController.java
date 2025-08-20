package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.easytech.pelatologio.dao.PartnerDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Partner;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.IntStream;

public class PartnersController implements Initializable {

    @FXML
    private TableView<Partner> partnersTable;
    @FXML
    public TableColumn codeColumn, nameColumn, titleColumn, afmColumn, phone1Column, mobileColumn, townColumn, emailColumn;
    @FXML
    private TextField filterField;

    private TabPane mainTabPane; // Injected from MainMenuController
    ObservableList<Partner> partnerList;
    FilteredList<Partner> filteredData;
    private final PartnerDao partnerDao = DBHelper.getPartnerDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadPartners();

        // Double-click to edit
        partnersTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && partnersTable.getSelectionModel().getSelectedItem() != null) {
                openPartnerTab(partnersTable.getSelectionModel().getSelectedItem().getId());
            }
        });
    }

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    private void setupTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        afmColumn.setCellValueFactory(new PropertyValueFactory<>("afm"));
        phone1Column.setCellValueFactory(new PropertyValueFactory<>("phone1"));
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobile"));
        townColumn.setCellValueFactory(new PropertyValueFactory<>("town"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

    }

    private void loadPartners() {
        // Δημιουργία του ObservableList και φόρτωση δεδομένων
        partnerList = FXCollections.observableArrayList();
        partnerList.addAll(partnerDao.findAll());

        // Δημιουργία του FilteredList
        filteredData = new FilteredList<>(partnerList, b -> true);

        // Σύνδεση φιλτραρίσματος
        setupFilter();

        // Σύνδεση του SortedList με τον πίνακα
        SortedList<Partner> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(partnersTable.comparatorProperty());
        partnersTable.setItems(sortedData);
    }

    private void setupFilter() {
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(newValue);
        });
        applyFilters(filterField.getText()); // Αρχική εφαρμογή φίλτρου
    }

    @FXML
    private void handleAddPartner() {
        partnerAddNew();
    }

    private void partnerAddNew() {
        try {
            // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals("Νέος Συνεργάτης")) {
                    mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                    return;
                }
            }
            // Φόρτωση του FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addPartner.fxml"));
            Parent partnerForm = loader.load();
            // Δημιουργία νέου tab για τη δημιουργία του πελάτη
            Tab partnerTab = new Tab("Νέος Συνεργάτης");
            partnerTab.setContent(partnerForm);

            AddPartnerController controller = loader.getController();
            controller.setMainTabPane(mainTabPane, partnerTab);
            controller.setPartnersController(this); // Περνάμε το instance του CustomersController
            String filterValue = filterField.getText();
            if (filterValue != null && filterValue.matches("\\d{9}")) {
                controller.setInitialAFM(filterValue); // Προ-συμπλήρωση ΑΦΜ
            }

            // Προσθήκη του tab στο TabPane
            mainTabPane.getTabs().add(partnerTab);
            mainTabPane.getSelectionModel().select(partnerTab); // Επιλογή του νέου tab
            partnerTab.setOnClosed(event -> {
                //refreshTableData(); // Ανανεώνει τη λίστα πελατών
                filteredData = new FilteredList<>(partnerList, b -> true);

                filterField.textProperty().addListener((observable, oldValue, newValue) ->
                        applyFilters(newValue)
                );

                applyFilters(filterField.getText());

                SortedList<Partner> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(partnersTable.comparatorProperty());
                partnersTable.setItems(sortedData);
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την προσθήκη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    public void openPartnerTab(int partnerId) {
        System.out.println("partner: " + partnerId);
        filteredData = new FilteredList<>(partnerList, b -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(newValue);
        });

        applyFilters(filterField.getText());

        SortedList<Partner> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(partnersTable.comparatorProperty());
        partnersTable.setItems(sortedData);
        // Έλεγχος αν υπάρχει ήδη ανοικτό tab για τον συγκεκριμένο πελάτη
        Partner selectedPartner = DBHelper.getPartnerDao().findById(partnerId);
        if (selectedPartner == null) return;
        System.out.println("selectedCustomer: " + selectedPartner);
        try {
            // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
            for (Tab tab : mainTabPane.getTabs()) {
                if (tab.getText().equals(selectedPartner.getName().substring(0, Math.min(selectedPartner.getName().length(), 18)))) {
                    mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                    return;
                }
            }
            // Φόρτωση του FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addPartner.fxml"));
            Parent partnerForm = loader.load();

            // Δημιουργία νέου tab για την ενημέρωση του πελάτη
            Tab partnerTab = new Tab(selectedPartner.getName().substring(0, Math.min(selectedPartner.getName().length(), 18)));

            partnerTab.setContent(partnerForm);

            AddPartnerController controller = loader.getController();
            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
            controller.setPartnerForEdit(selectedPartner);
            controller.setMainTabPane(mainTabPane, partnerTab);
            // Προσθήκη του tab στο TabPane
            Platform.runLater(() -> {
                mainTabPane.getTabs().add(partnerTab);
                mainTabPane.getSelectionModel().select(partnerTab);
                System.out.println("Tab added successfully: " + partnerTab.getText());
            });

            partnerTab.setOnClosed(event -> {
                refreshTableData(); // Ανανεώνει τη λίστα πελατών
                filteredData = new FilteredList<>(partnerList, b -> true);

                filterField.textProperty().addListener((observable, oldValue, newValue) -> {
                    applyFilters(newValue);
                });

                applyFilters(filterField.getText());

                SortedList<Partner> sortedData1 = new SortedList<>(filteredData);
                sortedData1.comparatorProperty().bind(partnersTable.comparatorProperty());
                partnersTable.setItems(sortedData1);
            });
        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά την επεξεργασία.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void applyFilters(String filterValue) {
        String filterText = filterField.getText() == null ? "" : filterField.getText().toUpperCase();

        filteredData.setPredicate(partner -> {

            // Free Text Filter
            if (!filterText.isEmpty()) {
                // Υποστήριξη Ελληνικών/Αγγλικών
                char[] chars1 = filterText.toCharArray();
                IntStream.range(0, chars1.length).forEach(i -> {
                    Character repl = ENGLISH_TO_GREEK.get(chars1[i]);
                    if (repl != null) chars1[i] = repl;
                });
                char[] chars2 = filterText.toCharArray();
                IntStream.range(0, chars2.length).forEach(i -> {
                    Character repl = GREEK_TO_ENGLISH.get(chars2[i]);
                    if (repl != null) chars2[i] = repl;
                });
                String search1 = new String(chars1);
                String search2 = new String(chars2);
                //String search1 = filterText; // Simplified for clarity, assuming no greek/english conversion needed for now

                boolean textMatch = (partner.getName() != null && (partner.getName().toUpperCase().contains(search1) || partner.getName().toUpperCase().contains(search2)))
                        || (partner.getTitle() != null && (partner.getTitle().toUpperCase().contains(search1) || partner.getTitle().toUpperCase().contains(search2)))
                        || (partner.getJob() != null && (partner.getJob().toUpperCase().contains(search1) || partner.getJob().toUpperCase().contains(search2)))
                        || (String.valueOf(partner.getId()).contains(search1) || String.valueOf(partner.getId()).contains(search2))
                        || (partner.getPhone1() != null && (partner.getPhone1().contains(search1) || partner.getPhone1().contains(search2)))
                        || (partner.getPhone2() != null && (partner.getPhone2().contains(search1) || partner.getPhone2().contains(search2)))
                        || (partner.getMobile() != null && (partner.getMobile().contains(search1) || partner.getMobile().contains(search2)))
                        || (partner.getAfm() != null && (partner.getAfm().contains(search1) || partner.getAfm().contains(search2)))
                        || (partner.getManager() != null && (partner.getManager().toUpperCase().contains(search1) || partner.getManager().toUpperCase().contains(search2)))
                        || (partner.getManagerPhone() != null && (partner.getManagerPhone().toUpperCase().contains(search1) || partner.getManagerPhone().toUpperCase().contains(search2)))
                        || (partner.getEmail() != null && (partner.getEmail().toUpperCase().contains(search1) || partner.getEmail().toUpperCase().contains(search2)))
                        || (partner.getEmail2() != null && (partner.getEmail2().toUpperCase().contains(search1) || partner.getEmail2().toUpperCase().contains(search2)))
                        || (partner.getTown() != null && (partner.getTown().toUpperCase().contains(search1) || partner.getTown().toUpperCase().contains(search2)))
                        || (partner.getAddress() != null && (partner.getAddress().toUpperCase().contains(search1) || partner.getAddress().toUpperCase().contains(search2)));
                return textMatch;
            }

            return true; // If all filters pass
        });
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

    private void refreshTableData() {
        List<TableColumn<Partner, ?>> sortOrder = new ArrayList<>(partnersTable.getSortOrder());
        partnerList.clear();
        partnerList.addAll(partnerDao.findAll());
        applyFilters(filterField.getText());
        partnersTable.getSortOrder().setAll(sortOrder);
    }
}
