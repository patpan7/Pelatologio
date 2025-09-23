package org.easytech.pelatologio.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.easytech.pelatologio.helper.AppSettings;
import org.easytech.pelatologio.models.Customer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PdfFormFiller {

    public File fillA1Form(Customer customer, String doy, String templatePath, String outputPath) throws IOException {
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            throw new IOException("PDF template file not found: " + templatePath);
        }

        File outputFile = new File(outputPath);

        try (PDDocument document = PDDocument.load(templateFile)) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                throw new IOException("The provided PDF is not a form.");
            }

            // Load the font that supports Greek characters
            String fontPath = AppSettings.loadSetting("datafolder") + "/Templates/arial.ttf";
            PDType0Font font = PDType0Font.load(document, new File(fontPath));
            PDResources resources = acroForm.getDefaultResources();
            if (resources == null) {
                resources = new PDResources();
            }
            String fontName = resources.add(font).getName();
            acroForm.setDefaultResources(resources);

            // Set the default appearance string with the new font
            String daString = "/" + fontName + " 10 Tf"; // Use 10pt size
            
            // Force the default appearance on all fields to override individual field settings
            for (PDField field : acroForm.getFields()) {
                if (field instanceof org.apache.pdfbox.pdmodel.interactive.form.PDTextField textField) {
                    textField.setDefaultAppearance(daString);
                } else if (field instanceof org.apache.pdfbox.pdmodel.interactive.form.PDChoice choiceField) {
                    choiceField.setDefaultAppearance(daString);
                }
            }

            // Set field values based on the names you provided
            setField(acroForm, "fill_1", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            setField(acroForm, "fill_2", customer.getName());
            setField(acroForm, "fill_3", customer.getTitle());
            setField(acroForm, "fill_4", customer.getAddress());
            setField(acroForm, "fill_5", customer.getTown());
            setField(acroForm, "TK", customer.getPostcode());
            setField(acroForm, "fill_7", customer.getPhone1());
            setField(acroForm, "fill_9", customer.getAfm());
            setField(acroForm, "fill_10", doy); // Set the DOY field
            setField(acroForm, "Email", customer.getEmail());
            setField(acroForm, "fill_13", customer.getJob());

            document.save(outputFile);
        }
        return outputFile;
    }

    private void setField(PDAcroForm form, String fieldName, String value) throws IOException {
        PDField field = form.getField(fieldName);
        if (field != null) {
            field.setValue(value != null ? value : "");
        } else {
            System.err.println("Warning: PDF field not found: " + fieldName);
        }
    }
}