package org.easytech.pelatologio;


import gr.gsis.aade.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.util.Map;

public class AfmSearchService {
    private final RgWsPublic2ServiceInterface service;

    public AfmSearchService() {
        RgWsPublic2Service serviceLocator = new RgWsPublic2Service();
        this.service = serviceLocator.getRgWsPublic2ServicePort();
    }

    public RgWsPublic2ResultRtType getAfmDetails(String afm) {
        RgWsPublic2AfmMethodRequestType request = new RgWsPublic2AfmMethodRequestType();
        // Δημιουργία του INPUT_REC και ορισμός των ΑΦΜ
        RgWsPublic2InputRtType inputRec = new RgWsPublic2InputRtType();

// Δημιουργία του JAXBElement για το afm_called_by
        JAXBElement<String> afmCalledByElement = new JAXBElement<>(new QName("http://rgwspublic2/RgWsPublic2", "afm_called_by"), String.class, "054909468");
        inputRec.setAfmCalledBy(afmCalledByElement);

// Δημιουργία του JAXBElement για το afm_called_for
        JAXBElement<String> afmCalledForElement = new JAXBElement<>(new QName("http://rgwspublic2/RgWsPublic2", "afm_called_for"), String.class, "054909468");
        inputRec.setAfmCalledFor(afmCalledForElement);

// Τοποθέτηση του INPUT_REC στο αίτημα
        request.setINPUTREC(inputRec);

        RgWsPublic2ResultRtType response = new RgWsPublic2ResultRtType();
        GenWsErrorRtType error = new GenWsErrorRtType();

        try {
            response = service.rgWsPublic2AfmMethod(inputRec).getRgWsPublic2ResultRtType();
            if (error.getErrorCode() != null) {
                System.out.println("Σφάλμα: " + error.getErrorDescr());
            }
        } catch (Exception e) {
            System.out.println("Σφάλμα κατά την αναζήτηση: " + e.getMessage());
        }

        return response;
    }

    public static void main(String[] args) {
        AfmSearchService afmSearchService = new AfmSearchService();
        String afm = "123456789"; // Το ΑΦΜ που θέλεις να αναζητήσεις
        RgWsPublic2ResultRtType details = afmSearchService.getAfmDetails(afm);

        if (details != null) {
            System.out.println("Όνομα Επιχείρησης: " + details);
        }
    }
}
