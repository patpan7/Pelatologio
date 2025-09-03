package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import org.easytech.pelatologio.dao.AnydeskDao;
import org.easytech.pelatologio.dao.impl.AnydeskDaoImpl;
import org.easytech.pelatologio.models.Anydesk;
import org.easytech.pelatologio.models.Customer;

import java.io.IOException;
import java.util.Optional;

public class AnydeskViewController {

    @FXML
    private Label customerNameText;
    @FXML
    private TableView<Anydesk> anydeskTableView;
    @FXML
    private TableColumn<Anydesk, String> descriptionColumn;
    @FXML
    private TableColumn<Anydesk, String> anydeskIdColumn;
    @FXML
    private TableColumn<Anydesk, Void> connectColumn;
    @FXML
    private TableColumn<Anydesk, Void> copyColumn;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField anydeskIdField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private Customer currentCustomer;
    private final AnydeskDao anydeskDao = new AnydeskDaoImpl();
    private final ObservableList<Anydesk> anydeskList = FXCollections.observableArrayList();

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        customerNameText.setText("Anydesk IDs for: " + (customer.getName().length() > 24 ? customer.getName().substring(0, 24) + "..." : customer.getName()));
        loadAnydeskIds();
    }

    @FXML
    private void initialize() {
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        anydeskIdColumn.setCellValueFactory(new PropertyValueFactory<>("anydeskId"));

        setupConnectButton();
        setupCopyButton();

        anydeskTableView.setItems(anydeskList);
    }

    private void loadAnydeskIds() {
        anydeskList.setAll(anydeskDao.getAnydeskIdsForCustomer(currentCustomer.getCode()));
    }

    @FXML
    private void handleAddButtonAction() {
        if (descriptionField.getText().isEmpty() || anydeskIdField.getText().isEmpty()) {
            // Show an alert
            return;
        }
        Anydesk newAnydesk = new Anydesk();
        newAnydesk.setCustomerId(currentCustomer.getCode());
        newAnydesk.setDescription(descriptionField.getText());
        newAnydesk.setAnydeskId(anydeskIdField.getText());
        anydeskDao.addAnydeskId(newAnydesk);
        loadAnydeskIds();
        descriptionField.clear();
        anydeskIdField.clear();
    }

    @FXML
    private void handleDeleteButtonAction() {
        Anydesk selectedAnydesk = anydeskTableView.getSelectionModel().getSelectedItem();
        if (selectedAnydesk != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Επιβεβαίωση διαγραφής");
            alert.setHeaderText("Είσαι βέβαιος ότι θέλετε να διαγράψετε το Anydesk ID?");
            alert.setContentText(selectedAnydesk.getDescription() + ": " + selectedAnydesk.getAnydeskId());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                anydeskDao.deleteAnydeskId(selectedAnydesk.getId());
                loadAnydeskIds();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Δεν επιλέχθηκε καταχώρηση");
            alert.setHeaderText(null);
            alert.setContentText("Επιλέξτε μια καταχώρηση για διαγραφή.");
            alert.showAndWait();
        }
    }

    private void setupConnectButton() {
        connectColumn.setCellFactory(param -> new TableCell<>() {
            private final Button connectBtn = new Button("Connect");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    connectBtn.setOnAction(event -> {
                        Anydesk anydesk = getTableView().getItems().get(getIndex());
                        try {
                            // This requires the path to anydesk.exe to be configured
                            // For now, we assume it's in a known location or in PATH
                            System.out.println("Connecting to " + anydesk.getAnydeskId());
                            Runtime.getRuntime().exec("C:\\Pelatologio\\AnyDesk.exe " + anydesk.getAnydeskId());
                        } catch (IOException e) {
                            e.printStackTrace();
                            // Show alert: Anydesk not found
                        }
                    });
                    setGraphic(connectBtn);
                }
            }
        });
    }

    private void setupCopyButton() {
        copyColumn.setCellFactory(param -> new TableCell<>() {
            private final Button copyBtn = new Button("Copy");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    copyBtn.setOnAction(event -> {
                        Anydesk anydesk = getTableView().getItems().get(getIndex());
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(anydesk.getAnydeskId());
                        clipboard.setContent(content);
                    });
                    setGraphic(copyBtn);
                }
            }
        });
    }
}
