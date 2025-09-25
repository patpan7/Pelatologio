package org.easytech.pelatologio;

import atlantafx.base.controls.ToggleSwitch;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.AppSettings;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.SimplyStatus;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SimplyStatusController implements Initializable {
    @FXML
    StackPane stackPane;
    @FXML
    private TableView<SimplyStatus> statusTable;
    @FXML
    private TableColumn idColumn;
    @FXML
    private TableColumn customerColumn;
    @FXML
    private TableColumn usernameColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> registerColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> authColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> acceptColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> mailColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> paramColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> myDataColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> deliveredColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> paidColumn;
    @FXML
    private TableColumn<SimplyStatus, Boolean> stockColumn;
    @FXML
    private TableColumn yearsColumn;
    @FXML
    private Label countLabel;

    @FXML
    private ToggleSwitch stockCheckbox, registerCheckbox, acceptCheckbox, paramCheckbox, myDataCheckBox, deliveredCheckBox, paidCheckBox;

    @FXML
    private ComboBox<String> yearsFilter;
    @FXML
    private Button addContractButton, renewButton;

    private final ObservableList<SimplyStatus> allSimplyStatus = FXCollections.observableArrayList();

    private TabPane mainTabPane;

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        DBHelper dbHelper = new DBHelper();
        Platform.runLater(() -> stackPane.requestFocus());
        setTooltip(addContractButton, "Προσθήκη συμβολαίου");
        setTooltip(renewButton, "Ανανέωση συμβολαίου");
        // Σύνδεση στηλών πίνακα με πεδία του Task
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("custMail"));
        yearsColumn.setCellValueFactory(new PropertyValueFactory<>("years"));
        registerColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isRegister());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "register", true); // Ενημέρωση στη βάση
            });
            return property;
        });

        authColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isAuth());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "auth", true); // Ενημέρωση στη βάση
            });
            return property;
        });

        acceptColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isAccept());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "accept", true); // Ενημέρωση στη βάση
            });
            return property;
        });

        mailColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isMail());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "mail", true); // Ενημέρωση στη βάση
            });
            return property;
        });

        paramColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isParam());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "param", true); // Ενημέρωση στη βάση
            });
            return property;
        });

        myDataColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isMydata());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "mydata", true); // Ενημέρωση στη βάση
            });
            return property;
        });

        deliveredColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isDelivered());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "delivered", true); // Ενημέρωση στη βάση
            });
            return property;
        });

        paidColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isPaid());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "paid", true); // Ενημέρωση στη βάση
            });
            return property;
        });

        stockColumn.setCellValueFactory(cellData -> {
            SimplyStatus data = cellData.getValue();
            BooleanProperty property = new SimpleBooleanProperty(data.isStock());

            // Αν το ToggleSwitch αλλάξει, ενημερώνουμε την κλάση Tasks και τη βάση
            property.addListener((obs, oldValue, newValue) -> {
                DBHelper.getSimplyStatusDao().updateSimplyStatus(data.getApp_login_id(), "stock", true); // Ενημέρωση στη βάση
            });
            return property;
        });


        // Σωστή χρήση του CheckBoxTableCell
        registerColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });
        authColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });
        acceptColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });
        mailColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });
        paramColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });
        myDataColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });
        deliveredColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });
        paidColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });
        stockColumn.setCellFactory(col -> {
            CheckBoxTableCell<SimplyStatus, Boolean> cell = new CheckBoxTableCell<>();
            cell.setEditable(true); // Επιτρέπει το click
            return cell;
        });

// Κάνει τον πίνακα επεξεργάσιμο, αλλιώς το ToggleSwitch δεν θα λειτουργεί
        statusTable.setEditable(true);
        registerColumn.setEditable(true);
        authColumn.setEditable(true);
        acceptColumn.setEditable(true);
        mailColumn.setEditable(true);
        paramColumn.setEditable(true);
        myDataColumn.setEditable(true);
        deliveredColumn.setEditable(true);
        paidColumn.setEditable(true);
        stockColumn.setEditable(true);

        statusTable.setOnMouseClicked(event -> {
            SimplyStatus selectedSimplyStatus = statusTable.getSelectionModel().getSelectedItem();
            Customer selectedCustomer = DBHelper.getCustomerDao().getSelectedCustomer(selectedSimplyStatus.getCustId());
            if (event.getClickCount() == 2) { // Έλεγχος για δύο κλικ

                // Έλεγχος αν υπάρχει επιλεγμένο προϊόν
                if (selectedCustomer != null) {
                    // Ανοίξτε το dialog box για επεξεργασία
                    if (selectedCustomer == null) {
                        CustomNotification.create()
                                .title("Προσοχή")
                                .text("Δεν έχει επιλεγεί Πελάτης!")
                                .hideAfter(Duration.seconds(3))
                                .position(Pos.TOP_RIGHT)
                                .showWarning();
                        return;
                    }
                    try {
                        String res = DBHelper.getCustomerDao().checkCustomerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                        if (res.equals("unlocked")) {
                            DBHelper.getCustomerDao().customerLock(selectedCustomer.getCode(), AppSettings.loadSetting("appuser"));
                            // Ψάχνουμε αν υπάρχει ήδη tab για το συγκεκριμένο πελάτη
                            for (Tab tab : mainTabPane.getTabs()) {
                                if (tab.getText().equals(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)))) {
                                    mainTabPane.getSelectionModel().select(tab); // Επιλογή του υπάρχοντος tab
                                    // Πάρε τον controller και άλλαξε tab στο "Simply"
                                    AddCustomerController controller = (AddCustomerController) tab.getUserData();
                                    Platform.runLater(() -> controller.selectSimplyTab());
                                    return;
                                }
                            }
                            // Φόρτωση του FXML
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
                            Parent customerForm = loader.load();

                            // Δημιουργία νέου tab για την ενημέρωση του πελάτη
                            Tab customerTab = new Tab(selectedCustomer.getName().substring(0, Math.min(selectedCustomer.getName().length(), 18)));
                            customerTab.setContent(customerForm);

                            AddCustomerController controller = loader.getController();
                            // Αν είναι ενημέρωση, φόρτωσε τα στοιχεία του πελάτη
                            controller.setCustomerForEdit(selectedCustomer);

                            // Προσθήκη του tab στο TabPane
                            mainTabPane.getTabs().add(customerTab);
                            mainTabPane.getSelectionModel().select(customerTab); // Επιλογή του νέου tab
                            Platform.runLater(() -> controller.selectSimplyTab());
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Προσοχή");
                            alert.setContentText(res);
                            alert.showAndWait();
                        }
                    } catch (IOException e) {
                        Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα.", e.getMessage(), Alert.AlertType.ERROR));
                    }
                }
            }
        });


        // Αρχικό γέμισμα του πίνακα
        loadStatus();


        yearsFilter.getItems().add(0, "Όλα");
        yearsFilter.getItems().add(1, "1 Έτος");
        yearsFilter.getItems().add(2, "2 Έτη");
        yearsFilter.getSelectionModel().selectFirst();

        stockCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateStatusTable());
        registerCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateStatusTable());
        acceptCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateStatusTable());
        paramCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> updateStatusTable());
        myDataCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateStatusTable());
        deliveredCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateStatusTable());
        paidCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> updateStatusTable());


        yearsFilter.valueProperty().addListener((obs, oldVal, newVal) -> updateStatusTable());

    }


    private void configureSingleSelectionCheckBoxes(ToggleSwitch[] checkBoxes) {
        for (ToggleSwitch ToggleSwitch : checkBoxes) {
            ToggleSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    for (ToggleSwitch otherCheckBox : checkBoxes) {
                        if (otherCheckBox != ToggleSwitch) {
                            otherCheckBox.setSelected(false);
                        }
                    }
                }
            });
        }
    }


    private void loadStatus() {
        // Φόρτωση όλων των εργασιών από τη βάση
        DBHelper dbHelper = new DBHelper();
        List<TableColumn<SimplyStatus, ?>> sortOrder = new ArrayList<>(statusTable.getSortOrder());
        allSimplyStatus.setAll(DBHelper.getSimplyStatusDao().getAllSimplyStatus());
        updateStatusTable();
        statusTable.getSortOrder().setAll(sortOrder);
    }

    private void updateStatusTable() {
        // Ξεκινάμε με όλες τις εργασίες
        ObservableList<SimplyStatus> filteredStatus = FXCollections.observableArrayList(allSimplyStatus);


        // Φιλτράρισμα
        if (stockCheckbox.isSelected()) {
            filteredStatus.removeIf(status -> !status.isStock());
        }

        if (registerCheckbox.isSelected()) {
            filteredStatus.removeIf(status -> status.isRegister());
        }

        if (acceptCheckbox.isSelected()) {
            filteredStatus.removeIf(status -> status.isAccept());
        }
        if (paramCheckbox.isSelected()) {
            filteredStatus.removeIf(status -> status.isParam());
        }
        if (myDataCheckBox.isSelected()) {
            filteredStatus.removeIf(status -> status.isMydata());
        }
        if (deliveredCheckBox.isSelected()) {
            filteredStatus.removeIf(status -> status.isDelivered());
        }
        if (paidCheckBox.isSelected()) {
            filteredStatus.removeIf(status -> status.isPaid());
        }

        // Φιλτράρισμα βάσει κατηγορίας
        if (yearsFilter.getSelectionModel().getSelectedIndex() == 1) { // Εξαιρείται η κατηγορία "Όλες"
            filteredStatus.removeIf(status -> !status.getYears().equals("1"));
        } else if (yearsFilter.getSelectionModel().getSelectedIndex() == 2) {
            filteredStatus.removeIf(status -> !status.getYears().equals("2"));
        }

        // Ανανεώνουμε τα δεδομένα του πίνακα
        statusTable.setItems(filteredStatus);
        countLabel.setText("Πλήθος: " + filteredStatus.size());
    }

    private void setTooltip(Button button, String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.seconds(0.3));
        tooltip.setText(text);
        button.setTooltip(tooltip);
    }

    public void refresh(MouseEvent mouseEvent) {
        loadStatus();
    }
}
