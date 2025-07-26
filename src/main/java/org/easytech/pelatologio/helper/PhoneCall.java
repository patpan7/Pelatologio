package org.easytech.pelatologio.helper;

import java.awt.*;
import java.net.URI;

public class PhoneCall {

    public static void callHandle(String phoneNumber) {
//        Button clickedButton = (Button) actionEvent.getSource(); // Ποιο κουμπί πατήθηκε;
//        TextField phoneNumber = (TextField) clickedButton.getUserData(); // Παίρνουμε το TextField που είναι συνδεδεμένο με το κουμπί
//        String phoneNumberText = phoneNumber.getText().trim(); // Λαμβάνουμε το κείμενο από το TextField
//        if (phoneNumberText.isEmpty()) {
//            return; // Εάν το κείμενο είναι κενό, δεν κάνουμε τη κλήση
//        }
        try {
            URI uri = new URI("callto:" + phoneNumber);
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void callHandle2(String phoneNumber) {
//        Button clickedButton = (Button) actionEvent.getSource(); // Ποιο κουμπί πατήθηκε;
//        TextField phoneNumber = (TextField) clickedButton.getUserData(); // Παίρνουμε το TextField που είναι συνδεδεμένο με το κουμπί
//        String phoneNumberText = phoneNumber.getText().trim(); // Λαμβάνουμε το κείμενο από το TextField
//        if (phoneNumberText.isEmpty()) {
//            return; // Εάν το κείμενο είναι κενό, δεν κάνουμε τη κλήση
//        }
        try {
            FanvilDialer.dial("0"+phoneNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
