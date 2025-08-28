package org.easytech.pelatologio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.easytech.pelatologio.dao.ApplicationStepDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.AppItem;
import org.easytech.pelatologio.models.ApplicationStep;

import java.util.List;
import java.util.Optional;

public class StepManagementController {

    @FXML private ComboBox<AppItem> applicationComboBox;
    @FXML private TableView<ApplicationStep> stepsTable;
    @FXML private TableColumn<ApplicationStep, Integer> orderColumn;
    @FXML private TableColumn<ApplicationStep, String> nameColumn;
    @FXML private TableColumn<ApplicationStep, String> actionTypeColumn;
    @FXML private TableColumn<ApplicationStep, Void> actionConfigColumn;

    private ApplicationStepDao stepDao;
    private ObservableList<AppItem> applications;
    private ObservableList<ApplicationStep> steps;

    @FXML
    public void initialize() {
        this.stepDao = DBHelper.getApplicationStepDao();
        setupTable();
        loadApplications();

        applicationComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadStepsForApplication(newVal.getId());
            }
        });
    }

    private void loadApplications() {
        List<AppItem> appList = DBHelper.getApplicationDao().getAllApplications();
        applications = FXCollections.observableArrayList(appList);
        applicationComboBox.setItems(applications);

        applicationComboBox.setConverter(new StringConverter<AppItem>() {
            @Override
            public String toString(AppItem application) {
                return application != null ? application.getName() : "";
            }

            @Override
            public AppItem fromString(String string) {
                return null; // Not needed for selection
            }
        });
    }

    private void loadStepsForApplication(int applicationId) {
        steps.clear();
        steps.addAll(stepDao.getStepsForApplication(applicationId));
    }

    private void setupTable() {
        steps = FXCollections.observableArrayList();
        stepsTable.setItems(steps);

        orderColumn.setCellValueFactory(new PropertyValueFactory<>("stepOrder"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("stepName"));
        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));

        // Editable columns
        orderColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        actionTypeColumn.setCellFactory(ComboBoxTableCell.forTableColumn("CHECKBOX", "SEND_EMAIL_CUSTOMER_TEMPLATE", "FILL_AND_SEND_A1", "OPEN_PROPOSAL_DIALOG", "SEND_ATTACHMENT_EMAIL"));

        // Edit commit handlers
        orderColumn.setOnEditCommit(event -> {
            ApplicationStep step = event.getRowValue();
            step.setStepOrder(event.getNewValue());
            stepDao.updateStep(step);
        });
        nameColumn.setOnEditCommit(event -> {
            ApplicationStep step = event.getRowValue();
            step.setStepName(event.getNewValue());
            stepDao.updateStep(step);
        });
        actionTypeColumn.setOnEditCommit(event -> {
            ApplicationStep step = event.getRowValue();
            step.setActionType(event.getNewValue());
            stepDao.updateStep(step);
        });

        // Add button to edit JSON config
        Callback<TableColumn<ApplicationStep, Void>, TableCell<ApplicationStep, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<ApplicationStep, Void> call(final TableColumn<ApplicationStep, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Edit JSON");
                    {
                        btn.setOnAction((ActionEvent event) -> {
                            ApplicationStep step = getTableView().getItems().get(getIndex());
                            openJsonEditor(step);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
        actionConfigColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void handleAddStep() {
        AppItem selectedApp = applicationComboBox.getSelectionModel().getSelectedItem();
        if (selectedApp == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχετε επιλέξει εφαρμογή.", "", Alert.AlertType.WARNING);
            return;
        }

        ApplicationStep newStep = new ApplicationStep();
        newStep.setApplicationId(selectedApp.getId());
        newStep.setStepName("Νέο Βήμα");
        newStep.setStepOrder(steps.size() + 1);
        newStep.setActionType("CHECKBOX");

        stepDao.addStep(newStep);
        loadStepsForApplication(selectedApp.getId()); // Refresh table
    }

    @FXML
    private void handleDeleteStep() {
        ApplicationStep selectedStep = stepsTable.getSelectionModel().getSelectedItem();
        if (selectedStep == null) {
            AlertDialogHelper.showDialog("Προσοχή", "Δεν έχετε επιλέξει βήμα για διαγραφή.", "", Alert.AlertType.WARNING);
            return;
        }

        Optional<ButtonType> result = AlertDialogHelper.showConfirmationDialog("Επιβεβαίωση Διαγραφής", "Είστε σίγουροι;", "Η διαγραφή του βήματος είναι οριστική.");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            stepDao.deleteStep(selectedStep.getId());
            loadStepsForApplication(selectedStep.getApplicationId()); // Refresh table
        }
    }

    private void openJsonEditor(ApplicationStep step) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Επεξεργασία JSON Config");
        dialog.setHeaderText("Επεξεργασία παραμέτρων για το βήμα: " + step.getStepName());

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea textArea = new TextArea(step.getActionConfigJson());
        textArea.setWrapText(true);
        dialog.getDialogPane().setContent(textArea);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(jsonConfig -> {
            step.setActionConfigJson(jsonConfig);
            stepDao.updateStep(step);
        });
    }
}
