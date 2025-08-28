package org.easytech.pelatologio;

import com.jfoenix.controls.JFXCheckBox;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import org.easytech.pelatologio.helper.AlertDialogHelper;
import org.easytech.pelatologio.helper.Features;

public class FeatureManagementDialogController {
    @FXML
    private CheckBox suppliersCheck, tasksCheck, ordersCheck, offersCheck, devicesCheck, subsCheck, callsCheck, partnersCheck, d11Check;
    @FXML
    private CheckBox taxisCheck, myposCheck, simplyCheck, emblemCheck, erganiCheck, pelatologioCheck, nineposheck, edpsCheck, megasoftCheck;
    @FXML
    public void initialize() {
        // Load current feature states
        suppliersCheck.setSelected(Features.isEnabled("suppliers"));
        tasksCheck.setSelected(Features.isEnabled("tasks"));
        ordersCheck.setSelected(Features.isEnabled("orders"));
        offersCheck.setSelected(Features.isEnabled("offers"));
        devicesCheck.setSelected(Features.isEnabled("devices"));
        subsCheck.setSelected(Features.isEnabled("subs"));
        callsCheck.setSelected(Features.isEnabled("calls"));
        partnersCheck.setSelected(Features.isEnabled("partners"));
        d11Check.setSelected(Features.isEnabled("d11"));
        myposCheck.setSelected(Features.isEnabled("mypos"));
        simplyCheck.setSelected(Features.isEnabled("simply"));
        taxisCheck.setSelected(Features.isEnabled("taxis"));
        emblemCheck.setSelected(Features.isEnabled("emblem"));
        erganiCheck.setSelected(Features.isEnabled("ergani"));
        pelatologioCheck.setSelected(Features.isEnabled("pelatologio"));
        nineposheck.setSelected(Features.isEnabled("ninepos"));
        edpsCheck.setSelected(Features.isEnabled("edps"));
        megasoftCheck.setSelected(Features.isEnabled("megasoft"));
    }

    @FXML
    private void handleSave() {
        // Save new feature states
        Features.setFeatureEnabled("suppliers", suppliersCheck.isSelected());
        Features.setFeatureEnabled("tasks", tasksCheck.isSelected());
        Features.setFeatureEnabled("orders", ordersCheck.isSelected());
        Features.setFeatureEnabled("offers", offersCheck.isSelected());
        Features.setFeatureEnabled("devices", devicesCheck.isSelected());
        Features.setFeatureEnabled("subs", subsCheck.isSelected());
        Features.setFeatureEnabled("calls", callsCheck.isSelected());
        Features.setFeatureEnabled("partners", partnersCheck.isSelected());
        Features.setFeatureEnabled("d11", d11Check.isSelected());
        Features.setFeatureEnabled("mypos", myposCheck.isSelected());
        Features.setFeatureEnabled("simply", simplyCheck.isSelected());
        Features.setFeatureEnabled("taxis", taxisCheck.isSelected());
        Features.setFeatureEnabled("emblem", emblemCheck.isSelected());
        Features.setFeatureEnabled("ergani", erganiCheck.isSelected());
        Features.setFeatureEnabled("pelatologio", pelatologioCheck.isSelected());
        Features.setFeatureEnabled("ninepos", nineposheck.isSelected());
        Features.setFeatureEnabled("edps", edpsCheck.isSelected());
        Features.setFeatureEnabled("megasoft", megasoftCheck.isSelected());


        AlertDialogHelper.showDialog("Επιτυχία", "Οι ρυθμίσεις αποθηκεύτηκαν.", "Απαιτείται επανεκκίνηση της εφαρμογής για να εφαρμοστούν οι αλλαγές.", Alert.AlertType.INFORMATION);

        // Close the dialog
        ((Stage) suppliersCheck.getScene().getWindow()).close();
    }
}
