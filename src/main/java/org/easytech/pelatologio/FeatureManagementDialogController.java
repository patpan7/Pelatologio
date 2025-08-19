package org.easytech.pelatologio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.Features;

public class FeatureManagementDialogController {

    @FXML private CheckBox suppliersCheck;
    @FXML private CheckBox tasksCheck;
    @FXML private CheckBox ordersCheck;
    @FXML private CheckBox myposCheck;
    @FXML private CheckBox devicesCheck;
    @FXML private CheckBox subsCheck;
    @FXML private CheckBox d11Check;
    @FXML private CheckBox simplyCheck;
    @FXML private CheckBox callsCheck;
    @FXML private CheckBox partnersCheck;
    @FXML private CheckBox offersCheck;

    @FXML
    public void initialize() {
        // Load current feature states
        suppliersCheck.setSelected(Features.isEnabled("suppliers"));
        tasksCheck.setSelected(Features.isEnabled("tasks"));
        ordersCheck.setSelected(Features.isEnabled("orders"));
        myposCheck.setSelected(Features.isEnabled("mypos"));
        devicesCheck.setSelected(Features.isEnabled("devices"));
        subsCheck.setSelected(Features.isEnabled("subs"));
        d11Check.setSelected(Features.isEnabled("d11"));
        simplyCheck.setSelected(Features.isEnabled("simply"));
        callsCheck.setSelected(Features.isEnabled("calls"));
        partnersCheck.setSelected(Features.isEnabled("partners"));
        offersCheck.setSelected(Features.isEnabled("offers"));
    }

    @FXML
    private void handleSave() {
        // Save new feature states
        Features.setFeatureEnabled("suppliers", suppliersCheck.isSelected());
        Features.setFeatureEnabled("tasks", tasksCheck.isSelected());
        Features.setFeatureEnabled("orders", ordersCheck.isSelected());
        Features.setFeatureEnabled("mypos", myposCheck.isSelected());
        Features.setFeatureEnabled("devices", devicesCheck.isSelected());
        Features.setFeatureEnabled("subs", subsCheck.isSelected());
        Features.setFeatureEnabled("d11", d11Check.isSelected());
        Features.setFeatureEnabled("simply", simplyCheck.isSelected());
        Features.setFeatureEnabled("calls", callsCheck.isSelected());
        Features.setFeatureEnabled("partners", partnersCheck.isSelected());
        Features.setFeatureEnabled("offers", offersCheck.isSelected());

        AlertDialogHelper.showDialog("Επιτυχία", "Οι ρυθμίσεις αποθηκεύτηκαν.", "Απαιτείται επανεκκίνηση της εφαρμογής για να εφαρμοστούν οι αλλαγές.", Alert.AlertType.INFORMATION);

        // Close the dialog
        ((Stage) suppliersCheck.getScene().getWindow()).close();
    }
}
