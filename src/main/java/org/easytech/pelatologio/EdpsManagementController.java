package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.function.Consumer;

public class EdpsManagementController {

    @FXML private AnchorPane globalProgressContainer;
    @FXML private AnchorPane stepManagementContainer;
    private TabPane mainTabPane;

    public void setMainTabPane(TabPane mainTabPane) {
        this.mainTabPane = mainTabPane;
        // Reload views to pass the mainTabPane to their controllers
        loadGlobalProgressView();
    }

    @FXML
    public void initialize() {
        // Initial load without mainTabPane
        loadGlobalProgressView();
        loadStepManagementView();
    }

    private void loadGlobalProgressView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("globalEdpsProgressView.fxml"));
            Parent content = loader.load();
            GlobalEdpsProgressController controller = loader.getController();
            if (mainTabPane != null) {
                controller.setMainTabPane(mainTabPane);
            }
            globalProgressContainer.getChildren().setAll(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStepManagementView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("stepManagementView.fxml"));
            Parent content = loader.load();
            stepManagementContainer.getChildren().setAll(content);
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}