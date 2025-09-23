package org.easytech.pelatologio;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.easytech.pelatologio.dao.ApplicationStepDao;
import org.easytech.pelatologio.dao.ProjectStepProgressDao;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.AppSettings;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.ApplicationStep;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.CustomerProjectSummary; // Changed import
import org.easytech.pelatologio.models.ProjectStepProgress; // Added import

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class GlobalEdpsProgressController {

    // IMPORTANT: Set the correct ApplicationID for EDPS from your Applications table
    private static final int EDPS_APPLICATION_ID = 8;

    @FXML private ComboBox<ApplicationStep> stepFilterComboBox;
    @FXML private TextField customerFilterField;
    @FXML private TreeTableView<Object> progressTable; // Changed to Object to hold both summary and progress
    @FXML private TreeTableColumn<Object, String> customerNameColumn;
    @FXML private TreeTableColumn<Object, String> projectNameColumn;
    @FXML private TreeTableColumn<Object, Integer> totalStepsColumn;
    @FXML private TreeTableColumn<Object, Integer> completedStepsColumn;
    @FXML private TreeTableColumn<Object, String> progressColumn;
    @FXML private TreeTableColumn<Object, String> nextPendingStepColumn;
    @FXML private TreeTableColumn<Object, String> lastCompletedStepColumn;
    @FXML private TreeTableColumn<Object, LocalDate> lastCompletionDateColumn;
    private TabPane mainTabPane;

    private ProjectStepProgressDao progressDao;
    private ApplicationStepDao applicationStepDao;
    private ObservableList<CustomerProjectSummary> masterSummaries; // Holds the top-level summaries
    private FilteredList<CustomerProjectSummary> filteredSummaries; // Filtered top-level summaries

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }

    @FXML
    public void initialize() {
        this.progressDao = DBHelper.getCustomerProjectTaskDao(); // Using the name from your DBHelper
        this.applicationStepDao = DBHelper.getApplicationStepDao();

        setupTable();
        setupFilters();
        loadGlobalProgressData(); // This initializes masterSummaries and filteredSummaries
        applyFilters(); // Now it's safe to call applyFilters
    }

    private void setupTable() {
        // Common CellValueFactory for columns that display data from both types
        customerNameColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();
            if (value instanceof CustomerProjectSummary) {
                return new SimpleStringProperty(((CustomerProjectSummary) value).getCustomerName());
            }
            return new SimpleStringProperty(""); // Steps don't have customer name
        });


        projectNameColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();
            if (value instanceof CustomerProjectSummary) {
                return new SimpleStringProperty(((CustomerProjectSummary) value).getProjectName());
            } else if (value instanceof ProjectStepProgress) {
                // For steps, we might want to show the step name here or leave blank
                return new SimpleStringProperty(((ProjectStepProgress) value).getStepName()); // Show step name for child rows
            }
            return new SimpleStringProperty("");
        });

        totalStepsColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();
            if (value instanceof CustomerProjectSummary) {
                return new SimpleObjectProperty<>(((CustomerProjectSummary) value).getTotalSteps());
            }
            return new SimpleObjectProperty<>(null);
        });

        completedStepsColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();
            if (value instanceof CustomerProjectSummary) {
                return new SimpleObjectProperty<>(((CustomerProjectSummary) value).getCompletedSteps());
            }
            return new SimpleObjectProperty<>(null);
        });

        progressColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();
            if (value instanceof CustomerProjectSummary summary) {
                return new SimpleStringProperty(summary.getCompletedSteps() + "/" + summary.getTotalSteps());
            }
            return new SimpleStringProperty("");
        });

        nextPendingStepColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();
            if (value instanceof CustomerProjectSummary) {
                return new SimpleStringProperty(((CustomerProjectSummary) value).getNextPendingStepName());
            }
            return new SimpleStringProperty("");
        });

        lastCompletedStepColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();
            if (value instanceof CustomerProjectSummary) {
                return new SimpleStringProperty(((CustomerProjectSummary) value).getLastCompletedStepName());
            }
            return new SimpleStringProperty("");
        });

        lastCompletionDateColumn.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();
            if (value instanceof CustomerProjectSummary) {
                return new SimpleObjectProperty<>(((CustomerProjectSummary) value).getLastCompletionDate());
            } else if (value instanceof ProjectStepProgress) {
                return new SimpleObjectProperty<>(((ProjectStepProgress) value).getCompletionDate());
            }
            return new SimpleObjectProperty<>(null);
        });

        // Date formatting for completion date
        lastCompletionDateColumn.setCellFactory(tc -> new TreeTableCell<Object, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) { // Added 'date == null' check
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        // Set the root for the TreeTableView (will be updated in loadGlobalProgressData)
        progressTable.setShowRoot(false); // Hide the invisible root
        // NEW Double-click logic, based on your SimplyStatusController
        progressTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<Object> selectedItem =
                        progressTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue() instanceof CustomerProjectSummary summary) {
                    openCustomerTab(summary.getCustomerId());
                }
            }
        });

    }

    private void openCustomerTab(int customerId) {
        if (mainTabPane == null) return;

        Customer selectedCustomer = DBHelper.getCustomerDao().getCustomerByCode(customerId);
        if (selectedCustomer == null) return;

        try {
            String lockResult =
                    DBHelper.getCustomerDao().checkCustomerLock(selectedCustomer.getCode(),
                            AppSettings.loadSetting("appuser"));
            if (!lockResult.equals("unlocked")) {
                AlertDialogHelper.showDialog("Προσοχή", lockResult, "", Alert.AlertType.ERROR);
                return;
            }

            // Check if tab is already open
            for (Tab tab : mainTabPane.getTabs()) {
                if (Integer.valueOf(selectedCustomer.getCode()).equals(tab.getUserData())) {
                    mainTabPane.getSelectionModel().select(tab);
                    if (tab.getContent().getUserData() instanceof AddCustomerController controller) {
                        controller.selectEdpsTab(); // Select the EDPS sub-tab
                    }
                    return;
                }
            }

            // If not open, create a new tab
            DBHelper.getCustomerDao().customerLock(selectedCustomer.getCode(),
                    AppSettings.loadSetting("appuser"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newCustomer.fxml"));
            Parent customerForm = loader.load();

            String tabTitle = selectedCustomer.getName().substring(0,
                    Math.min(selectedCustomer.getName().length(), 18));
            Tab customerTab = new Tab(tabTitle);
            customerTab.setUserData(selectedCustomer.getCode());
            customerTab.setContent(customerForm);
            customerForm.setUserData(loader.getController()); // Store controller

            AddCustomerController controller = loader.getController();
            controller.setMainTabPane(mainTabPane, customerTab);
            controller.setCustomerForEdit(selectedCustomer);

            mainTabPane.getTabs().add(customerTab);
            mainTabPane.getSelectionModel().select(customerTab);
            controller.selectEdpsTab(); // Select the EDPS sub-tab

            customerTab.setOnCloseRequest(event -> {
                DBHelper.getCustomerDao().customerUnlock(selectedCustomer.getCode());
                if (!controller.handleTabCloseRequest()) {
                    event.consume();
                }
            });

        } catch (IOException e) {
            Platform.runLater(() -> AlertDialogHelper.showDialog("Σφάλμα", "Προέκυψε σφάλμα κατά το άνοιγμα της καρτέλας πελάτη.", e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private void setupFilters() {
        // Load steps for filter ComboBox
        List<ApplicationStep> steps = applicationStepDao.getStepsForApplication(EDPS_APPLICATION_ID);
        stepFilterComboBox.getItems().add(new ApplicationStep(0, EDPS_APPLICATION_ID, "Όλα", 0, null)); // "All" option
        stepFilterComboBox.getItems().addAll(steps);

        stepFilterComboBox.setConverter(new StringConverter<ApplicationStep>() {
            @Override
            public String toString(ApplicationStep step) {
                return step != null ? step.getStepName() : "";
            }

            @Override
            public ApplicationStep fromString(String string) {
                return null;
            }
        });

        // Add listeners to filters
        stepFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                applyFilters());
        customerFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Do NOT selectFirst here, it will trigger applyFilters too early
        // stepFilterComboBox.getSelectionModel().selectFirst(); // Removed this line
    }

    private void loadGlobalProgressData() {
        masterSummaries =
                FXCollections.observableArrayList(progressDao.getGlobalProgressSummary(EDPS_APPLICATION_ID));
        filteredSummaries = new FilteredList<>(masterSummaries, p -> true);

        // Build the TreeTableView hierarchy
        TreeItem<Object> root = new TreeItem<>(); // Invisible root
        root.setExpanded(true); // Expand root by default

        for (CustomerProjectSummary summary : filteredSummaries) { // Use filteredSummaries here
            TreeItem<Object> projectNode = new TreeItem<>(summary);
            projectNode.setExpanded(false); // Projects are collapsed by default

            // Load detailed steps for each project
            List<ProjectStepProgress> steps = progressDao.getProgressForProject(summary.getProjectId());
            for (ProjectStepProgress step : steps) {
                projectNode.getChildren().add(new TreeItem<>(step));
            }
            root.getChildren().add(projectNode);
        }
        progressTable.setRoot(root);
        // progressTable.setShowRoot(false); // Already set in setupTable()
    }

    private void applyFilters() {
        ApplicationStep selectedStep = stepFilterComboBox.getSelectionModel().getSelectedItem();
        String customerFilterText = customerFilterField.getText() != null ?
                customerFilterField.getText().toLowerCase() : "";

        filteredSummaries.setPredicate(summary -> { // Changed type
            boolean stepMatch = (selectedStep == null || selectedStep.getId() == 0 ||
                    (summary.getNextPendingStepName() != null &&
                            summary.getNextPendingStepName().equals(selectedStep.getStepName())) ||
                    (summary.getLastCompletedStepName() != null &&
                            summary.getLastCompletedStepName().equals(selectedStep.getStepName()) && summary.getCompletedSteps() ==
                            summary.getTotalSteps())); // Filter by next pending or last completed if all done

            boolean customerMatch = (customerFilterText.isEmpty() ||
                    (summary.getCustomerName() != null &&
                            summary.getCustomerName().toLowerCase().contains(customerFilterText)) ||
                    (summary.getCustomerAfm() != null &&
                            summary.getCustomerAfm().toLowerCase().contains(customerFilterText)));
            return stepMatch && customerMatch;
        });

        // Rebuild the TreeTableView based on filteredSummaries
        TreeItem<Object> root = new TreeItem<>();
        root.setExpanded(true);
        for (CustomerProjectSummary summary : filteredSummaries) { // Use filteredSummaries here
            TreeItem<Object> projectNode = new TreeItem<>(summary);
            projectNode.setExpanded(false); // Projects are collapsed by default

            // Load detailed steps for each project (re-fetch or use cached if available)
            List<ProjectStepProgress> steps = progressDao.getProgressForProject(summary.getProjectId());
            for (ProjectStepProgress step : steps) {
                projectNode.getChildren().add(new TreeItem<>(step));
            }
            root.getChildren().add(projectNode);
        }
        progressTable.setRoot(root);
        // progressTable.setShowRoot(false); // Already set in setupTable()
    }

}