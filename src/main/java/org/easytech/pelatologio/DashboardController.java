package org.easytech.pelatologio;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.easytech.pelatologio.helper.DBHelper;
import org.easytech.pelatologio.models.CallLog;
import org.easytech.pelatologio.models.Customer;
import org.easytech.pelatologio.models.Subscription;
import org.easytech.pelatologio.models.Tasks;
import org.easytech.pelatologio.helper.Features;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardController {
    // Table for Today's Tasks
    @FXML
    private TableView<Tasks> tasksTableView;
    @FXML
    private TableColumn<Tasks, String> taskCustomerColumn;
    @FXML
    private TableColumn<Tasks, String> taskTitleColumn;
    @FXML
    private TableColumn<Tasks, String> taskDescriptionColumn;

    // Table for Expiring Subscriptions
    @FXML
    private TableView<Subscription> subscriptionsTableView;
    @FXML
    private TableColumn<Subscription, String> subsCustomerColumn;
    @FXML
    private TableColumn<Subscription, String> subsTypeColumn;
    @FXML
    private TableColumn<Subscription, String> subsExpiryColumn;

    @FXML
    private TableView<Customer> balanceTableView;
    @FXML
    private TableColumn<Customer, String> balanceCustomerColumn;
    @FXML
    private TableColumn<Customer, BigDecimal> balanceColumn;

    // Chart for New Customers
    @FXML
    private BarChart<String, Number> customersChart;
    @FXML
    public BarChart recommendationChart;
    @FXML
    private PieChart jobTeamPieChart, subJobTeamPieChart;


    // ListView for Recent Calls
    @FXML
    private ListView<CallLog> callsListView;
    private TabPane mainTabPane;
    private java.util.function.Consumer<Integer> openCustomerCallback;

    public void setOpenCustomerCallback(java.util.function.Consumer<Integer> openCustomerCallback) {
        this.openCustomerCallback = openCustomerCallback;
    }

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
    }
    @FXML
    public void initialize() {
        System.out.println("Dashboard Initialized!");

        if (Features.isEnabled("tasks")) {
            loadTasks();
        }
        if (Features.isEnabled("contracts")) {
            loadSubscriptions();
        }
        loadBalance();
        loadChartData();
        loadRecentCalls();
        loadRecommendationChartData();
        loadJobTeamChartData();

        // Add click listeners for tables and list
        if (Features.isEnabled("tasks")) {
            tasksTableView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Tasks selectedTask = tasksTableView.getSelectionModel().getSelectedItem();
                    if (selectedTask != null && selectedTask.getCustomerId() != null && openCustomerCallback != null) {
                        openCustomerCallback.accept(selectedTask.getCustomerId());
                    }
                }
            });
        }

        if (Features.isEnabled("contracts")) {
            subscriptionsTableView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Subscription selectedSub = subscriptionsTableView.getSelectionModel().getSelectedItem();
                    if (selectedSub != null && selectedSub.getCustomerId() != null && openCustomerCallback != null) {
                        System.out.println("Selected Subscription: " + selectedSub.getCustomerId());
                        openCustomerCallback.accept(selectedSub.getCustomerId());
                    }
                }
            });
        }

        balanceTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Customer selectedCustomer = balanceTableView.getSelectionModel().getSelectedItem();
                if (selectedCustomer != null &&  openCustomerCallback != null) {
                    System.out.println("Selected Subscription: " + selectedCustomer.getCode());
                    openCustomerCallback.accept(selectedCustomer.getCode());
                }
            }
        });

        callsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                CallLog selectedCall = callsListView.getSelectionModel().getSelectedItem();
                if (selectedCall != null && selectedCall.getCustomerId() != 0 && openCustomerCallback != null) {
                    openCustomerCallback.accept(selectedCall.getCustomerId());
                }
            }
        });
    }

    private void loadTasks() {
        if (Features.isEnabled("tasks")) {
            taskCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            taskDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

            List<Tasks> todaysTasks = DBHelper.getTaskDao().getTodaysTasks();
            tasksTableView.getItems().setAll(todaysTasks);
        } else {
            tasksTableView.getItems().clear();
            // Optionally add a placeholder or message
            // tasksTableView.setPlaceholder(new Label("Tasks module is disabled."));
        }
    }

    private void loadSubscriptions() {
        if (Features.isEnabled("contracts")) {
            subsCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            subsTypeColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            subsExpiryColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

            List<Subscription> expiringSubs = DBHelper.getSubscriptionDao().getExpiringSubscriptions(30);
            subscriptionsTableView.getItems().setAll(expiringSubs);
        } else {
            subscriptionsTableView.getItems().clear();
            // Optionally add a placeholder or message
            // subscriptionsTableView.setPlaceholder(new Label("Contracts module is disabled."));
        }
    }


    private void loadBalance() {
        // Ορίζουμε σωστά το cellValueFactory για name (παραμένει String)
        balanceCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Ορίζουμε το balanceColumn να κρατά BigDecimal, όχι String
        balanceColumn.setCellValueFactory(cellData -> {
            String balanceStr = cellData.getValue().getBalance();
            balanceStr = balanceStr.replace(",", "."); // <-- αυτή η γραμμή είναι το κλειδί

            try {
                BigDecimal balance = new BigDecimal(balanceStr);
                return new ReadOnlyObjectWrapper<>(balance);
            } catch (NumberFormatException e) {
                return new ReadOnlyObjectWrapper<>(BigDecimal.ZERO); // ή null, ανάλογα με τι προτιμάς
            }
        });

        // Προαιρετικά: μορφοποίηση για εμφάνιση 2 δεκαδικών
        balanceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        // Λήψη της λίστας πελατών με υπόλοιπα
        List<Customer> balanceList = DBHelper.getCustomerDao().getCustomersWithBalance();

        // Δημιουργία ObservableList & SortedList
        ObservableList<Customer> observableList = FXCollections.observableArrayList(balanceList);
        SortedList<Customer> sortedList = new SortedList<>(observableList);

        // Σύνδεση comparator με την TableView ώστε να λειτουργεί το click για sort
        sortedList.comparatorProperty().bind(balanceTableView.comparatorProperty());

        // Εμφάνιση της λίστας
        balanceTableView.setItems(sortedList);
    }


    private void loadChartData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("New Customers");

        Map<String, Integer> monthlyData = DBHelper.getCustomerDao().getNewCustomersPerMonth();

        for (Map.Entry<String, Integer> entry : monthlyData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        customersChart.getData().add(series);
    }

    private void loadRecentCalls() {
        List<CallLog> recentCalls = DBHelper.getCallLogDao().getRecentCalls(10);
        callsListView.getItems().setAll(recentCalls);
    }

    private void loadRecommendationChartData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Customers by Recommendation");

        Map<String, Integer> recommendationData = DBHelper.getCustomerDao().getCustomersByRecommendation();

        // Sort the data by count in descending order
        List<Map.Entry<String, Integer>> sortedList = new java.util.ArrayList<>(recommendationData.entrySet());
        sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        for (Map.Entry<String, Integer> entry : sortedList) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        recommendationChart.getData().add(series);
    }

    private void loadJobTeamChartData() {
        Map<String, Integer> jobTeamData = DBHelper.getJobTeamDao().getCustomerCountPerJobTeam();
        double totalCustomers = jobTeamData.values().stream().mapToInt(Integer::intValue).sum();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : jobTeamData.entrySet()) {
            double percentage = (totalCustomers > 0) ? (entry.getValue() / totalCustomers) * 100 : 0;
            String label = String.format("%s: %d (%.1f%%)", entry.getKey(), entry.getValue(), percentage);
            pieChartData.add(new PieChart.Data(label, entry.getValue()));
        }

        // Sort the data by value (customer count) in descending order
        pieChartData.sort((d1, d2) -> Double.compare(d2.getPieValue(), d1.getPieValue()));

        jobTeamPieChart.setData(pieChartData);

        // Define a list of 20 distinct colors to use
        String[] colors = {
            "#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2", "#7f7f7f",
            "#bcbd22", "#17becf", "#aec7e8", "#ffbb78", "#98df8a", "#ff9896", "#c5b0d5", "#c49c94",
            "#f7b6d2", "#c7c7c7", "#dbdb8d", "#9edae5"
        };

        int i = 0;
        for (PieChart.Data data : jobTeamPieChart.getData()) {
            String color = colors[i % colors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");

            // Add click listener to each pie slice
            data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                String teamName = data.getName().split(":")[0]; // Extract name before the colon
                int teamId = DBHelper.getJobTeamDao().getJobTeamIdByName(teamName);
                if (teamId != -1) {
                    loadSubJobTeamChartData(teamId, teamName);
                }
            });

            i++;
        }

        // Fix for legend colors and add click listener
        jobTeamPieChart.applyCss();
        i = 0;
        for (Node node : jobTeamPieChart.lookupAll(".chart-legend-item")) {
            if (node instanceof Label) {
                String color = colors[i % colors.length];
                ((Label) node).getGraphic().setStyle("-fx-background-color: " + color + ";");

                // Add click listener to each legend item
                node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    String teamName = ((Label) node).getText().split(":")[0]; // Extract name before the colon
                    int teamId = DBHelper.getJobTeamDao().getJobTeamIdByName(teamName);
                    if (teamId != -1) {
                        loadSubJobTeamChartData(teamId, teamName);
                    }
                });
                i++;
            }
        }
    }

    private void loadSubJobTeamChartData(int jobTeamId, String teamName) {
        subJobTeamPieChart.setTitle("Υπο-ομάδες για: " + teamName);
        subJobTeamPieChart.getData().clear();
        Map<String, Integer> subJobTeamData = DBHelper.getJobTeamDao().getCustomerCountPerSubJobTeam(jobTeamId);
        double totalCustomers = subJobTeamData.values().stream().mapToInt(Integer::intValue).sum();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : subJobTeamData.entrySet()) {
            double percentage = (totalCustomers > 0) ? (entry.getValue() / totalCustomers) * 100 : 0;
            String label = String.format("%s: %d (%.1f%%)", entry.getKey(), entry.getValue(), percentage);
            pieChartData.add(new PieChart.Data(label, entry.getValue()));
        }

        // Sort the data by value (customer count) in descending order
        pieChartData.sort((d1, d2) -> Double.compare(d2.getPieValue(), d1.getPieValue()));

        subJobTeamPieChart.setData(pieChartData);

        // Define a list of 20 distinct colors to use
        String[] colors = {
            "#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2", "#7f7f7f",
            "#bcbd22", "#17becf", "#aec7e8", "#ffbb78", "#98df8a", "#ff9896", "#c5b0d5", "#c49c94",
            "#f7b6d2", "#c7c7c7", "#dbdb8d", "#9edae5"
        };

        int i = 0;
        for (PieChart.Data data : subJobTeamPieChart.getData()) {
            String color = colors[i % colors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }

        // Fix for legend colors
        subJobTeamPieChart.applyCss();
        i = 0;
        for (Node node : subJobTeamPieChart.lookupAll(".chart-legend-item")) {
            if (node instanceof Label) {
                String color = colors[i % colors.length];
                ((Label) node).getGraphic().setStyle("-fx-background-color: " + color + ";");
                i++;
            }
        }
    }
}
