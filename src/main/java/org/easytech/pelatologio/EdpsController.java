package org.easytech.pelatologio;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.easytech.pelatologio.dao.CustomerProjectDao;
import org.easytech.pelatologio.dao.ProjectStepProgressDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.CustomerFolderManager;
import org.easytech.pelatologio.helper.CustomerTabController;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.CustomerProject;
import org.easytech.pelatologio.models.ProjectStepProgress;
import org.easytech.pelatologio.service.ActionExecutor;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EdpsController implements CustomerTabController {

    private static final int EDPS_APPLICATION_ID = 8; // IMPORTANT: Set the correct ApplicationID for EDPS from your DB

    @FXML
    private ComboBox<CustomerProject> projectComboBox;
    @FXML
    private TableView<ProjectStepProgress> stepsTable;
    @FXML
    private TableColumn<ProjectStepProgress, Boolean> completedColumn;
    @FXML
    private TableColumn<ProjectStepProgress, String> stepNameColumn;
    @FXML
    private TableColumn<ProjectStepProgress, LocalDate> completionDateColumn;
    @FXML
    private TableColumn<ProjectStepProgress, String> notesColumn;
    @FXML
    private TableColumn<ProjectStepProgress, Void> actionColumn;

    private Customer customer;
    private Runnable onDataSavedCallback;
    private CustomerProjectDao customerProjectDao;
    private ProjectStepProgressDao progressDao;

    @FXML
    public void initialize() {
        this.customerProjectDao = DBHelper.getCustomerProjectDao();
        this.progressDao = DBHelper.getCustomerProjectTaskDao(); // Using the name from your DBHelper
        setupTable();

        projectComboBox.setConverter(new StringConverter<CustomerProject>() {
            @Override
            public String toString(CustomerProject project) {
                return project != null ? project.getProjectName() : "";
            }

            @Override
            public CustomerProject fromString(String string) {
                return null;
            }
        });

        projectComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadStepsForProject(newVal.getId());
            }
        });
    }

    @Override
    public void setCustomer(Customer customer) {
        this.customer = customer;
        loadProjects();
    }


    @Override
    public void setOnDataSaved(Runnable callback) {
        this.onDataSavedCallback = callback;
    }

    private void loadProjects() {
        List<CustomerProject> projects = customerProjectDao.getProjectsForCustomer(customer.getCode());
        projectComboBox.getItems().setAll(projects);
        if (!projects.isEmpty()) {
            projectComboBox.getSelectionModel().selectFirst();
        }
    }

    private void loadStepsForProject(int projectId) {
        List<ProjectStepProgress> progressList = progressDao.getProgressForProject(projectId);
        stepsTable.getItems().setAll(progressList);
    }

    private void setupTable() {
        stepsTable.setEditable(true); // Make the table editable

        stepNameColumn.setCellValueFactory(new PropertyValueFactory<>("stepName"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        completionDateColumn.setCellValueFactory(new PropertyValueFactory<>("completionDate"));

        // Editable Notes Column
        notesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        notesColumn.setOnEditCommit(event -> {
            ProjectStepProgress progress = event.getRowValue();
            progress.setNotes(event.getNewValue());
            progressDao.updateProgress(progress);
        });

        // CheckBox Column
        completedColumn.setEditable(true); // Make the column editable
        completedColumn.setCellValueFactory(cellData -> {
            ProjectStepProgress progress = cellData.getValue();
            SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(progress.isCompleted());
            booleanProp.addListener((observable, oldValue, newValue) -> {
                progress.setCompleted(newValue);
                progress.setCompletionDate(newValue ? LocalDate.now() : null);
                progressDao.updateProgress(progress);
                stepsTable.refresh();
            });
            return booleanProp;
        });
        completedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completedColumn));

        // Action Button Column
        Callback<TableColumn<ProjectStepProgress, Void>, TableCell<ProjectStepProgress, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<ProjectStepProgress, Void> call(final TableColumn<ProjectStepProgress, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Ενέργεια");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            ProjectStepProgress progress = getTableView().getItems().get(getIndex());
                            handleAction(progress);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            ProjectStepProgress progress = getTableView().getItems().get(getIndex());
                            String actionType = progress.getActionType();
                            // Show the button if the action type is not null and not a simple checkbox
                            if (actionType != null && !actionType.isEmpty() && !actionType.equalsIgnoreCase("CHECKBOX")) {
                                btn.setDisable(progress.isCompleted()); // Disable button if step is completed
                                setGraphic(btn);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    private void handleAction(ProjectStepProgress progress) {
        ActionExecutor actionExecutor = new ActionExecutor();
        actionExecutor.execute(progress, this.customer);

        // Optionally, refresh the step after action
        // This assumes the action itself doesn't update the DB, which it should.
        // For now, we refresh to see potential changes if the executor updated the DB.
        loadStepsForProject(progress.getProjectId());
    }

    @FXML
    private void handleNewProject() {
        TextInputDialog dialog = new TextInputDialog("EDPS - " + customer.getName());
        dialog.setTitle("Νέο Project");
        dialog.setHeaderText("Δώστε ένα όνομα για το νέο EDPS project.");
        dialog.setContentText("Όνομα Project:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(projectName -> {
            if (projectName.isEmpty()) return;

            CustomerProject newProject = new CustomerProject();
            newProject.setCustomerId(customer.getCode());
            newProject.setApplicationId(EDPS_APPLICATION_ID);
            newProject.setProjectName(projectName);
            newProject.setStartDate(LocalDate.now());

            customerProjectDao.addProjectForCustomer(newProject);

            // After creating the project, create the initial progress steps for it
            if (newProject.getId() > 0) {
                progressDao.createInitialProgressForProject(newProject.getId(), EDPS_APPLICATION_ID);
                loadProjects(); // Refresh the project list
            }
        });
    }

    @FXML
    private void handleOpenEdpsFolder() {
        if (customer == null) return;

        try {
            CustomerFolderManager folderManager = new CustomerFolderManager();
            File customerFolder = folderManager.customerFolder(customer.getName(), customer.getAfm());
            if (customerFolder == null) {
                throw new IOException("Could not create or find customer folder.");
            }

            File edpsFolder = new File(customerFolder, "EDPS");
            if (!edpsFolder.exists()) {
                if (!edpsFolder.mkdirs()) {
                    throw new IOException("Could not create EDPS subfolder.");
                }
            }

            java.awt.Desktop.getDesktop().open(edpsFolder);

        } catch (Exception e) {
            e.printStackTrace();
            AlertDialogHelper.showErrorDialog("Folder Error", "Could not open the EDPS folder: " + e.getMessage());
        }
    }

}